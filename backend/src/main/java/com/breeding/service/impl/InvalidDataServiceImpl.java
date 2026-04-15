package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.InvalidRecord;
import com.breeding.mapper.InvalidRecordMapper;
import com.breeding.service.InvalidDataService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class InvalidDataServiceImpl extends ServiceImpl<InvalidRecordMapper, InvalidRecord> implements InvalidDataService {

    private final JdbcTemplate jdbcTemplate;

    public InvalidDataServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Page<InvalidRecord> getInvalidPage(int pageNum, int pageSize, String dataType) {
        Page<InvalidRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InvalidRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(InvalidRecord::getDataType, "user");
        if (dataType != null && !dataType.isBlank()) {
            wrapper.eq(InvalidRecord::getDataType, dataType);
        }
        wrapper.orderByDesc(InvalidRecord::getDeletedTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean invalidate(String dataType, Long dataId, Long operatorId, String operatorName) {
        ArchiveMeta meta = getArchiveMeta(dataType);
        InvalidRecord existedRecord = this.getOne(new LambdaQueryWrapper<InvalidRecord>()
                .eq(InvalidRecord::getDataType, dataType)
                .eq(InvalidRecord::getDataId, dataId));
        if (existedRecord != null) {
            throw new IllegalArgumentException("该数据已作废");
        }

        Map<String, Object> sourceRow = fetchSourceRow(meta, dataId);
        if (sourceRow == null) {
            throw new IllegalArgumentException("数据不存在");
        }
        if (toInt(sourceRow.get("deleted")) == 1) {
            throw new IllegalArgumentException("该数据已作废");
        }

        int updated = jdbcTemplate.update(
                "UPDATE " + meta.tableName + " SET deleted = 1, delete_by = ?, delete_time = NOW() WHERE id = ? AND deleted = 0",
                operatorId, dataId
        );
        if (updated == 0) {
            throw new IllegalStateException("作废失败，请刷新后重试");
        }

        InvalidRecord invalidRecord = new InvalidRecord();
        invalidRecord.setDataId(dataId);
        invalidRecord.setDataType(dataType);
        invalidRecord.setModuleName(meta.moduleName);
        invalidRecord.setDisplayName(String.valueOf(sourceRow.get("display_name")));
        invalidRecord.setDeletedBy(operatorId);
        invalidRecord.setDeletedByName(operatorName);
        invalidRecord.setDeletedTime(LocalDateTime.now());
        return this.save(invalidRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restore(Long invalidRecordId) {
        InvalidRecord invalidRecord = this.getById(invalidRecordId);
        if (invalidRecord == null) {
            throw new IllegalArgumentException("作废记录不存在");
        }

        ArchiveMeta meta = getArchiveMeta(invalidRecord.getDataType());
        int updated = jdbcTemplate.update(
                "UPDATE " + meta.tableName + " SET deleted = 0, delete_by = NULL, delete_time = NULL WHERE id = ? AND deleted = 1",
                invalidRecord.getDataId()
        );
        if (updated == 0) {
            throw new IllegalStateException("恢复失败，原始数据可能已不存在");
        }

        return this.removeById(invalidRecordId);
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
        return map;
    }

    private static String buildAlertRuleTypeDisplaySql() {
        return "CASE rule_type " +
                "WHEN 'temperature_anomaly' THEN '体温异常' " +
                "WHEN 'medicine_expire' THEN '物品过期预警' " +
                "WHEN 'no_food_long' THEN '未进食异常' " +
                "WHEN 'death_rate_high' THEN '死亡率异常' " +
                "WHEN 'manual_report' THEN '人工上报' " +
                "ELSE IFNULL(rule_type, '-') END";
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
