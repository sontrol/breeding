package com.breeding.service.impl;

import com.breeding.service.InvalidDataService;
import com.breeding.service.SystemLogService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class InvalidDataServiceImpl implements InvalidDataService {

    private final JdbcTemplate jdbcTemplate;
    private final SystemLogService systemLogService;

    public InvalidDataServiceImpl(JdbcTemplate jdbcTemplate, SystemLogService systemLogService) {
        this.jdbcTemplate = jdbcTemplate;
        this.systemLogService = systemLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean invalidate(String dataType, Long dataId, Long operatorId, String operatorName) {
        ArchiveMeta meta = getArchiveMeta(dataType);

        Map<String, Object> sourceRow = fetchSourceRow(meta, dataId);
        if (sourceRow == null) {
            throw new IllegalArgumentException("数据不存在");
        }
        if (toInt(sourceRow.get("deleted")) == 1) {
            throw new IllegalArgumentException("该数据已作废");
        }

        String displayName = sourceRow.get("display_name") != null ? String.valueOf(sourceRow.get("display_name")) : "";

        int updated = jdbcTemplate.update(
                "UPDATE " + meta.tableName + " SET deleted = 1, delete_by = ?, delete_time = ? WHERE id = ? AND deleted = 0",
                operatorId, LocalDateTime.now(), dataId
        );
        if (updated == 0) {
            throw new IllegalStateException("作废失败，请刷新后重试");
        }

        systemLogService.logOperation(
                operatorId,
                meta.moduleName,
                "作废",
                "InvalidDataService.invalidate",
                "数据类型: " + dataType + ", 数据ID: " + dataId + ", 数据标题: " + displayName,
                null
        );

        // 级联作废子数据
        cascadeInvalidate(dataType, dataId, operatorId, operatorName);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restore(String dataType, Long dataId, Long operatorId) {
        ArchiveMeta meta = getArchiveMeta(dataType);
        int updated = jdbcTemplate.update(
                "UPDATE " + meta.tableName + " SET deleted = 0, delete_by = NULL, delete_time = NULL WHERE id = ? AND deleted = 1",
                dataId
        );
        if (updated == 0) {
            throw new IllegalStateException("恢复失败，原始数据可能已不存在");
        }

        systemLogService.logOperation(
                operatorId,
                meta.moduleName,
                "恢复",
                "InvalidDataService.restore",
                "数据类型: " + dataType + ", 数据ID: " + dataId,
                null
        );

        return true;
    }

    @Override
    public List<Map<String, Object>> getInvalidDataList(String dataType, int page, int size) {
        ArchiveMeta meta = getArchiveMeta(dataType);
        int offset = (page - 1) * size;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, " + meta.displaySql + " AS display_name FROM " + meta.tableName
                        + " WHERE deleted = 1 ORDER BY delete_time DESC LIMIT ? OFFSET ?", size, offset);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", row.get("id"));
            item.put("dataType", dataType);
            item.put("moduleName", meta.moduleName);
            item.put("displayName", row.get("display_name") != null ? String.valueOf(row.get("display_name")) : (meta.moduleName + " #" + row.get("id")));
            result.add(item);
        }
        return result;
    }

    @Override
    public long countInvalidData(String dataType) {
        ArchiveMeta meta = getArchiveMeta(dataType);
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + meta.tableName + " WHERE deleted = 1", Long.class);
        return count != null ? count : 0;
    }

    private Map<String, Object> fetchSourceRow(ArchiveMeta meta, Long dataId) {
        try {
            return jdbcTemplate.queryForMap(
                    "SELECT id, deleted, " + meta.displaySql + " AS display_name FROM " + meta.tableName + " WHERE id = ?",
                    dataId
            );
        } catch (Exception e) {
            return null;
        }
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private ArchiveMeta getArchiveMeta(String dataType) {
        ArchiveMeta meta = ARCHIVE_META_MAP.get(dataType);
        if (meta == null) {
            throw new IllegalArgumentException("不支持的作废数据类型");
        }
        if (!ARCHIVE_TABLE_NAME_WHITELIST.contains(meta.tableName)) {
            throw new IllegalStateException("检测到非法归档表配置");
        }
        return meta;
    }

    // 级联规则: 父数据类型 -> [{子数据类型, 外键列名}]
    private static final Map<String, List<CascadeRule>> CASCADE_RULES = buildCascadeRules();

    private record CascadeRule(String childDataType, String foreignKeyColumn) {}

    private static Map<String, List<CascadeRule>> buildCascadeRules() {
        Map<String, List<CascadeRule>> rules = new HashMap<>();
        rules.put("symptom", List.of(new CascadeRule("diagnosis", "symptom_id")));
        rules.put("diagnosis", List.of(new CascadeRule("treatment", "diagnosis_id")));
        return rules;
    }

    private void cascadeInvalidate(String dataType, Long dataId, Long operatorId, String operatorName) {
        List<CascadeRule> childRules = CASCADE_RULES.get(dataType);
        if (childRules == null) return;

        for (CascadeRule rule : childRules) {
            List<Long> childIds = jdbcTemplate.queryForList(
                    "SELECT id FROM " + getArchiveMeta(rule.childDataType).tableName
                            + " WHERE " + rule.foreignKeyColumn + " = ? AND deleted = 0",
                    Long.class, dataId);
            for (Long childId : childIds) {
                invalidate(rule.childDataType, childId, operatorId, operatorName);
            }
        }
    }

    private static final Map<String, ArchiveMeta> ARCHIVE_META_MAP = buildArchiveMetaMap();
    private static final Set<String> ARCHIVE_TABLE_NAME_WHITELIST = buildArchiveTableNameWhitelist();

    private static Map<String, ArchiveMeta> buildArchiveMetaMap() {
        Map<String, ArchiveMeta> map = new HashMap<>();
        map.put("animal", new ArchiveMeta("animal", "动物档案",
                "CONCAT('耳标号:', IFNULL(ear_tag, '-'), ' / 物种:', IFNULL(species, '-'))"));
        map.put("feeding_plan", new ArchiveMeta("feeding_plan", "饲养计划",
                "CONCAT('计划#', id, ' / 栏舍:', IFNULL(shed_id, '-'), ' / ', IFNULL(feed_type, '-'))"));
        map.put("feeding_record", new ArchiveMeta("feeding_record", "饲养执行记录",
                "CONCAT('记录#', id, ' / 栏舍:', IFNULL(shed_id, '-'), ' / ', IFNULL(feed_type, '-'))"));
        map.put("symptom", new ArchiveMeta("symptom", "症状上报",
                "CONCAT('症状#', id, ' / 动物ID:', IFNULL(animal_id, '-'))"));
        map.put("diagnosis", new ArchiveMeta("diagnosis", "诊断记录",
                "CONCAT('诊断#', id, ' / 疾病:', IFNULL(disease_name, '-'))"));
        map.put("treatment", new ArchiveMeta("treatment", "治疗记录",
                "CONCAT('治疗#', id, ' / 诊断ID:', IFNULL(diagnosis_id, '-'))"));
        map.put("inventory", new ArchiveMeta("inventory", "库存列表",
                "CONCAT(IFNULL(item_name, '-'), ' / 批次:', IFNULL(batch_number, '-'))"));
        map.put("alert", new ArchiveMeta("alert", "预警消息",
                "CONCAT('预警#', id, ' / 类型:', " + buildAlertRuleTypeDisplaySql() + ")"));
        map.put("vaccine_record", new ArchiveMeta("vaccine_record", "免疫接种记录",
                "CONCAT('免疫#', id, ' / 动物:', IFNULL(animal_id, '-'), ' / 批次:', IFNULL(batch_number, '-'))"));
        return map;
    }

    private static String buildAlertRuleTypeDisplaySql() {
        return "CASE rule_type " +
                "WHEN 1 THEN '体温异常' " +
                "WHEN 2 THEN '未进食异常' " +
                "WHEN 3 THEN '死亡率异常' " +
                "WHEN 4 THEN '物品过期预警' " +
                "ELSE IFNULL(CAST(rule_type AS CHAR), '-') END";
    }

    private static Set<String> buildArchiveTableNameWhitelist() {
        Set<String> whitelist = new HashSet<>();
        for (ArchiveMeta meta : ARCHIVE_META_MAP.values()) {
            whitelist.add(meta.tableName);
        }
        return Collections.unmodifiableSet(whitelist);
    }

    private static class ArchiveMeta {
        private final String tableName;
        private final String moduleName;
        private final String displaySql;

        private ArchiveMeta(String tableName, String moduleName, String displaySql) {
            this.tableName = tableName;
            this.moduleName = moduleName;
            this.displaySql = displaySql;
        }
    }
}
