package com.breeding.service.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.LoginUser;
import com.breeding.entity.Alert;
import com.breeding.entity.Animal;
import com.breeding.entity.Diagnosis;
import com.breeding.entity.Event;
import com.breeding.entity.FeedingPlan;
import com.breeding.entity.FeedingRecord;
import com.breeding.entity.Inventory;
import com.breeding.entity.Symptom;
import com.breeding.entity.Treatment;
import com.breeding.entity.User;
import com.breeding.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 数据访问中间层
 * 负责在AI查询前进行权限拦截，只返回当前用户有权限查看的数据
 */
@Component
public class AIDataAccessLayer {

    private static final int DETAIL_LIMIT = 8;

    @Autowired
    private AnimalService animalService;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private EventService eventService;

    @Autowired
    private SymptomService symptomService;

    @Autowired
    private DiagnosisService diagnosisService;

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private FeedingPlanService feedingPlanService;

    @Autowired
    private FeedingRecordService feedingRecordService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private UserService userService;

    /**
     * 根据当前用户权限，获取AI可分析的上下文数据摘要
     */
    public Map<String, Object> getAccessibleContextData() {
        LoginUser loginUser = LoginUser.getCurrentUser();
        List<String> permissions = loginUser.getPermissions();
        
        Map<String, Object> contextData = new LinkedHashMap<>();
        List<String> accessedModules = new ArrayList<>();
        Map<String, Object> moduleContext = new LinkedHashMap<>();

        // 1. 动物数据访问权限校验
        if (hasPermission(permissions, "animal:view", "system:*", "ROLE_admin", "ROLE_owner", "ROLE_vet", "ROLE_feeder")) {
            moduleContext.put("animal", buildSummary("detail_and_aggregate",
                    Map.of("animal_total", animalService.count()),
                    Map.of("recent_animals", buildAnimalDetails())));
            accessedModules.add("animal");
        }

        // 2. 库存数据访问权限校验
        if (hasPermission(permissions, "inventory:view", "system:*", "ROLE_admin")) {
            moduleContext.put("inventory", buildSummary("detail_and_aggregate",
                    Map.of("inventory_total", inventoryService.count()),
                    Map.of("recent_inventory_items", buildInventoryDetails())));
            accessedModules.add("inventory");
        }

        // 3. 事件数据访问权限校验
        if (hasPermission(permissions, "event:view", "dashboard:view", "report:view", "system:*", "ROLE_admin", "ROLE_owner")) {
            moduleContext.put("event", buildSummary("detail_and_aggregate",
                    Map.of("event_total", eventService.count()),
                    Map.of("recent_events", buildEventDetails())));
            accessedModules.add("event");
        }

        // 4. 疾病数据访问权限校验
        if (hasPermission(permissions, "disease:view", "disease:add", "diagnosis:add", "treatment:add", "system:*", "ROLE_admin", "ROLE_owner", "ROLE_vet")) {
            moduleContext.put("disease", buildSummary("detail_and_aggregate",
                    Map.of(
                            "symptom_total", symptomService.count(),
                            "diagnosis_total", diagnosisService.count(),
                            "treatment_total", treatmentService.count()
                    ),
                    buildDiseaseDetails()));
            accessedModules.add("disease");
        }

        // 5. 饲养数据访问权限校验
        if (hasPermission(permissions, "feeding:view", "feeding:record:add", "feeding:plan:add", "system:*", "ROLE_admin", "ROLE_owner", "ROLE_feeder")) {
            moduleContext.put("feeding", buildSummary("detail_and_aggregate",
                    Map.of(
                            "feeding_plan_total", feedingPlanService.count(),
                            "feeding_record_total", feedingRecordService.count()
                    ),
                    buildFeedingDetails()));
            accessedModules.add("feeding");
        }

        // 6. 预警数据访问权限校验
        if (hasPermission(permissions, "alert:view", "alert:add", "alert:handle", "alert:check", "system:*", "ROLE_admin", "ROLE_owner", "ROLE_vet", "ROLE_feeder")) {
            moduleContext.put("alert", buildSummary("detail_and_aggregate",
                    Map.of("alert_total", alertService.count()),
                    Map.of("recent_alerts", buildAlertDetails())));
            accessedModules.add("alert");
        }

        // 7. 系统用户管理权限校验
        if (hasPermission(permissions, "system:view", "system:*", "ROLE_admin")) {
            moduleContext.put("system", buildSummary("detail_and_aggregate",
                    Map.of(
                            "user_total", userService.count(),
                            "enabled_user_total", userService.count(new LambdaQueryWrapper<User>().eq(User::getStatus, 1)),
                            "pending_audit_user_total", userService.count(new LambdaQueryWrapper<User>().eq(User::getAuditStatus, 0))
                    ),
                    Map.of("recent_users", buildUserDetails())));
            accessedModules.add("system");
        }

        // 8. 注册审核权限校验
        if (hasPermission(permissions, "system:register:view", "system:*", "ROLE_admin", "ROLE_owner")) {
            moduleContext.put("register_audit", buildSummary("detail_and_aggregate",
                    Map.of("pending_audit_user_total", userService.count(new LambdaQueryWrapper<User>().eq(User::getAuditStatus, 0))),
                    Map.of("pending_audit_users", buildPendingAuditUserDetails())));
            accessedModules.add("register_audit");
        }

        contextData.put("authorized_modules", accessedModules);
        contextData.put("module_context", moduleContext);
        contextData.put("accessedModules", String.join(",", accessedModules));
        return contextData;
    }

    private Map<String, Object> buildSummary(String contextType, Map<String, Object> metrics, Map<String, Object> details) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("authorized", true);
        summary.put("context_type", contextType);
        summary.put("metrics", metrics);
        summary.put("details", details);
        return summary;
    }

    private Map<String, Object> buildDiseaseDetails() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("recent_symptoms", buildSymptomDetails());
        details.put("recent_diagnoses", buildDiagnosisDetails());
        details.put("recent_treatments", buildTreatmentDetails());
        return details;
    }

    private Map<String, Object> buildFeedingDetails() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("recent_feeding_plans", buildFeedingPlanDetails());
        details.put("recent_feeding_records", buildFeedingRecordDetails());
        return details;
    }

    private List<Map<String, Object>> buildAnimalDetails() {
        List<Animal> animals = animalService.list(new LambdaQueryWrapper<Animal>()
                .orderByDesc(Animal::getCreateTime)
                .last("LIMIT " + DETAIL_LIMIT));
        List<Map<String, Object>> details = new ArrayList<>();
        for (Animal animal : animals) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", animal.getId());
            item.put("ear_tag", animal.getEarTag());
            item.put("species", animal.getSpecies());
            item.put("variety", animal.getVariety());
            item.put("birth_date", animal.getBirthDate());
            item.put("gender", animal.getGender());
            item.put("gender_label", getGenderLabel(animal.getGender()));
            item.put("shed_id", animal.getShedId());
            item.put("status", animal.getStatus());
            item.put("status_label", getAnimalStatusLabel(animal.getStatus()));
            item.put("create_time", animal.getCreateTime());
            details.add(item);
        }
        return details;
    }

    private List<Map<String, Object>> buildInventoryDetails() {
        List<Inventory> inventories = inventoryService.list(new LambdaQueryWrapper<Inventory>()
                .orderByDesc(Inventory::getCreateTime)
                .last("LIMIT " + DETAIL_LIMIT));
        List<Map<String, Object>> details = new ArrayList<>();
        for (Inventory inventory : inventories) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", inventory.getId());
            item.put("item_name", inventory.getItemName());
            item.put("item_type", inventory.getItemType());
            item.put("item_type_label", getInventoryTypeLabel(inventory.getItemType()));
            item.put("batch_number", inventory.getBatchNumber());
            item.put("quantity", inventory.getQuantity());
            item.put("unit", inventory.getUnit());
            item.put("produce_date", inventory.getProduceDate());
            item.put("expire_date", inventory.getExpireDate());
            item.put("create_time", inventory.getCreateTime());
            details.add(item);
        }
        return details;
    }

    private List<Map<String, Object>> buildEventDetails() {
        List<Event> events = eventService.list(new LambdaQueryWrapper<Event>()
                .orderByDesc(Event::getEventTime)
                .last("LIMIT " + DETAIL_LIMIT));
        List<Map<String, Object>> details = new ArrayList<>();
        for (Event event : events) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", event.getId());
            item.put("animal_id", event.getAnimalId());
            item.put("event_type", event.getEventType());
            item.put("event_time", event.getEventTime());
            item.put("operator_id", event.getOperatorId());
            item.put("description", event.getDescription());
            item.put("related_id", event.getRelatedId());
            details.add(item);
        }
        return details;
    }

    private List<Map<String, Object>> buildAlertDetails() {
        List<Alert> alerts = alertService.list(new LambdaQueryWrapper<Alert>()
                .orderByDesc(Alert::getCreateTime)
                .last("LIMIT " + DETAIL_LIMIT));
        List<Map<String, Object>> details = new ArrayList<>();
        for (Alert alert : alerts) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", alert.getId());
            item.put("rule_type", alert.getRuleType());
            item.put("animal_id", alert.getAnimalId());
            item.put("shed_id", alert.getShedId());
            item.put("inventory_id", alert.getInventoryId());
            item.put("alert_msg", alert.getAlertMsg());
            item.put("status", alert.getStatus());
            item.put("status_label", getAlertStatusLabel(alert.getStatus()));
            item.put("creator_id", alert.getCreatorId());
            item.put("handler_id", alert.getHandlerId());
            item.put("create_time", alert.getCreateTime());
            item.put("handle_time", alert.getHandleTime());
            details.add(item);
        }
        return details;
    }

    private List<Map<String, Object>> buildSymptomDetails() {
        List<Symptom> symptoms = symptomService.list(new LambdaQueryWrapper<Symptom>()
                .orderByDesc(Symptom::getObserveTime)
                .last("LIMIT " + DETAIL_LIMIT));
        List<Map<String, Object>> details = new ArrayList<>();
        for (Symptom symptom : symptoms) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", symptom.getId());
            item.put("animal_id", symptom.getAnimalId());
            item.put("observer_id", symptom.getObserverId());
            item.put("symptom_desc", symptom.getSymptomDesc());
            item.put("observe_time", symptom.getObserveTime());
            item.put("status", symptom.getStatus());
            details.add(item);
        }
        return details;
    }

    private List<Map<String, Object>> buildDiagnosisDetails() {
        List<Diagnosis> diagnoses = diagnosisService.list(new LambdaQueryWrapper<Diagnosis>()
                .orderByDesc(Diagnosis::getDiagnoseTime)
                .last("LIMIT " + DETAIL_LIMIT));
        List<Map<String, Object>> details = new ArrayList<>();
        for (Diagnosis diagnosis : diagnoses) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", diagnosis.getId());
            item.put("symptom_id", diagnosis.getSymptomId());
            item.put("animal_id", diagnosis.getAnimalId());
            item.put("vet_id", diagnosis.getVetId());
            item.put("disease_name", diagnosis.getDiseaseName());
            item.put("severity", diagnosis.getSeverity());
            item.put("diagnose_time", diagnosis.getDiagnoseTime());
            item.put("status", diagnosis.getStatus());
            details.add(item);
        }
        return details;
    }

    private List<Map<String, Object>> buildTreatmentDetails() {
        List<Treatment> treatments = treatmentService.list(new LambdaQueryWrapper<Treatment>()
                .orderByDesc(Treatment::getTime)
                .last("LIMIT " + DETAIL_LIMIT));
        List<Map<String, Object>> details = new ArrayList<>();
        for (Treatment treatment : treatments) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", treatment.getId());
            item.put("diagnosis_id", treatment.getDiagnosisId());
            item.put("animal_id", treatment.getAnimalId());
            item.put("operator_id", treatment.getOperatorId());
            item.put("medicine_id", treatment.getMedicineId());
            item.put("time", treatment.getTime());
            item.put("result", treatment.getResult());
            details.add(item);
        }
        return details;
    }

    private List<Map<String, Object>> buildFeedingPlanDetails() {
        List<FeedingPlan> feedingPlans = feedingPlanService.list(new LambdaQueryWrapper<FeedingPlan>()
                .orderByDesc(FeedingPlan::getCreateTime)
                .last("LIMIT " + DETAIL_LIMIT));
        List<Map<String, Object>> details = new ArrayList<>();
        for (FeedingPlan feedingPlan : feedingPlans) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", feedingPlan.getId());
            item.put("shed_id", feedingPlan.getShedId());
            item.put("feed_type", feedingPlan.getFeedType());
            item.put("amount_per_animal", feedingPlan.getAmountPerAnimal());
            item.put("feeding_time", feedingPlan.getFeedingTime());
            item.put("status", feedingPlan.getStatus());
            item.put("create_time", feedingPlan.getCreateTime());
            details.add(item);
        }
        return details;
    }

    private List<Map<String, Object>> buildFeedingRecordDetails() {
        List<FeedingRecord> feedingRecords = feedingRecordService.list(new LambdaQueryWrapper<FeedingRecord>()
                .orderByDesc(FeedingRecord::getTime)
                .last("LIMIT " + DETAIL_LIMIT));
        List<Map<String, Object>> details = new ArrayList<>();
        for (FeedingRecord feedingRecord : feedingRecords) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", feedingRecord.getId());
            item.put("plan_id", feedingRecord.getPlanId());
            item.put("shed_id", feedingRecord.getShedId());
            item.put("operator_id", feedingRecord.getOperatorId());
            item.put("feed_type", feedingRecord.getFeedType());
            item.put("total_amount", feedingRecord.getTotalAmount());
            item.put("time", feedingRecord.getTime());
            details.add(item);
        }
        return details;
    }

    private List<Map<String, Object>> buildUserDetails() {
        Page<User> page = userService.getUserPage(1, DETAIL_LIMIT, null, null, null);
        List<Map<String, Object>> details = new ArrayList<>();
        if (page == null || page.getRecords() == null) {
            return details;
        }
        for (User user : page.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", user.getId());
            item.put("username", user.getUsername());
            item.put("real_name", user.getRealName());
            item.put("phone", user.getPhone());
            item.put("role_code", user.getRoleCode());
            item.put("status", user.getStatus());
            item.put("status_label", getUserStatusLabel(user.getStatus()));
            item.put("audit_status", user.getAuditStatus());
            item.put("audit_status_label", getAuditStatusLabel(user.getAuditStatus()));
            item.put("create_time", user.getCreateTime());
            details.add(item);
        }
        return details;
    }

    private List<Map<String, Object>> buildPendingAuditUserDetails() {
        List<User> users = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getAuditStatus, 0)
                .orderByDesc(User::getCreateTime)
                .last("LIMIT " + DETAIL_LIMIT));
        List<Map<String, Object>> details = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", user.getId());
            item.put("username", user.getUsername());
            item.put("real_name", user.getRealName());
            item.put("phone", user.getPhone());
            item.put("apply_role_code", user.getApplyRoleCode());
            item.put("audit_status", user.getAuditStatus());
            item.put("audit_status_label", getAuditStatusLabel(user.getAuditStatus()));
            item.put("create_time", user.getCreateTime());
            details.add(item);
        }
        return details;
    }

    private String getGenderLabel(Integer gender) {
        if (gender == null) {
            return "未知";
        }
        return gender == 1 ? "雄性" : gender == 2 ? "雌性" : "未知";
    }

    private String getAnimalStatusLabel(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 1 -> "健康";
            case 2 -> "患病";
            case 3 -> "隔离";
            case 4 -> "死亡";
            case 5 -> "出栏";
            default -> "未知";
        };
    }

    private String getInventoryTypeLabel(Integer itemType) {
        if (itemType == null) {
            return "未知";
        }
        return switch (itemType) {
            case 1 -> "饲料";
            case 2 -> "药品";
            case 3 -> "疫苗";
            case 4 -> "器械";
            default -> "未知";
        };
    }

    private String getAlertStatusLabel(Integer status) {
        if (status == null) {
            return "未知";
        }
        return status == 1 ? "已处理" : "未处理";
    }

    private String getUserStatusLabel(Integer status) {
        if (status == null) {
            return "未知";
        }
        return status == 1 ? "启用" : "禁用";
    }

    private String getAuditStatusLabel(Integer auditStatus) {
        if (auditStatus == null) {
            return "未知";
        }
        return switch (auditStatus) {
            case 0 -> "待审核";
            case 1 -> "已通过";
            case 2 -> "已驳回";
            default -> "未知";
        };
    }

    private boolean hasPermission(List<String> userPerms, String... requiredPerms) {
        for (String perm : requiredPerms) {
            if (userPerms.contains(perm)) {
                return true;
            }
        }
        return false;
    }
}
