CREATE DATABASE IF NOT EXISTS breeding DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE breeding;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS system_log;
DROP TABLE IF EXISTS treatment_item;
DROP TABLE IF EXISTS alert;
DROP TABLE IF EXISTS inventory_log;
DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS vaccine_record;
DROP TABLE IF EXISTS vaccine;
DROP TABLE IF EXISTS treatment;
DROP TABLE IF EXISTS diagnosis;
DROP TABLE IF EXISTS symptom;
DROP TABLE IF EXISTS feeding_record;
DROP TABLE IF EXISTS feeding_plan;
DROP TABLE IF EXISTS event;
DROP TABLE IF EXISTS animal;
DROP TABLE IF EXISTS shed;
DROP TABLE IF EXISTS role_permission;
DROP TABLE IF EXISTS permission;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS user;
SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================================
-- 1. 用户与权限管理 (RBAC)
-- user 表通过 role_id 直接关联角色（不使用 user_role 关联表）
-- ==========================================================
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    status TINYINT DEFAULT 1 COMMENT '1:正常, 0:禁用',
    role_id BIGINT COMMENT '角色ID',
    apply_role_code VARCHAR(50) COMMENT '注册申请角色编码',
    audit_status TINYINT DEFAULT 1 COMMENT '0:待审核, 1:已通过, 2:已驳回',
    audit_remark VARCHAR(255) COMMENT '审核备注',
    audit_by BIGINT COMMENT '审核人ID',
    audit_time DATETIME COMMENT '审核时间',
    deleted TINYINT DEFAULT 0 COMMENT '0:正常, 1:已作废',
    delete_by BIGINT COMMENT '作废人ID',
    delete_time DATETIME COMMENT '作废时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role_id (role_id),
    INDEX idx_status_deleted (status, deleted),
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_user_audit_by FOREIGN KEY (audit_by) REFERENCES user(id) ON DELETE SET NULL
) COMMENT '系统用户表';

CREATE TABLE role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称(如:管理员、兽医)',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码(如:admin, owner, vet, feeder)',
    description VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '系统角色表';

CREATE TABLE permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(50) NOT NULL COMMENT '权限名称',
    code VARCHAR(50) UNIQUE COMMENT '权限编码(如:animal:add)',
    type TINYINT COMMENT '1:菜单, 2:按钮',
    path VARCHAR(100) COMMENT '前端路由路径',
    icon VARCHAR(50) COMMENT '菜单图标',
    sort INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '权限/菜单表';

CREATE TABLE role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES role(id),
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permission(id)
) COMMENT '角色权限关联表';

-- ==========================================================
-- 2. 动物管理
-- ==========================================================
CREATE TABLE shed (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '栏舍名称',
    capacity INT NOT NULL COMMENT '容量',
    current_count INT DEFAULT 0 COMMENT '当前数量',
    manager_id BIGINT COMMENT '负责人(饲养员)ID',
    deleted TINYINT DEFAULT 0 COMMENT '0:正常, 1:已作废',
    delete_by BIGINT COMMENT '作废人ID',
    delete_time DATETIME COMMENT '作废时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_manager_id (manager_id),
    CONSTRAINT fk_shed_manager FOREIGN KEY (manager_id) REFERENCES user(id) ON DELETE SET NULL
) COMMENT '栏舍表';

CREATE TABLE animal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ear_tag VARCHAR(50) NOT NULL UNIQUE COMMENT '耳标号',
    species VARCHAR(50) NOT NULL COMMENT '物种(如:猪,牛,羊)',
    variety VARCHAR(50) COMMENT '品种',
    birth_date DATE,
    gender TINYINT COMMENT '1:公, 2:母',
    shed_id BIGINT COMMENT '当前所在栏舍ID',
    status TINYINT DEFAULT 1 COMMENT '1:健康, 2:患病, 3:隔离, 4:死亡, 5:出栏',
    deleted TINYINT DEFAULT 0 COMMENT '0:正常, 1:已作废',
    delete_by BIGINT COMMENT '作废人ID',
    delete_time DATETIME COMMENT '作废时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_animal_shed FOREIGN KEY (shed_id) REFERENCES shed(id)
) COMMENT '动物档案表';

-- ==========================================================
-- 3. 事件中心 (核心)
-- ==========================================================
CREATE TABLE event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    animal_id BIGINT NOT NULL COMMENT '关联动物ID',
    event_type TINYINT COMMENT '1:feeding, 2:disease, 3:treatment, 4:vaccine, 5:death, 6:sale, 7:transfer, 8:status_change',
    event_time DATETIME NOT NULL COMMENT '事件发生时间',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    description TEXT COMMENT '事件详情描述',
    related_id BIGINT COMMENT '关联的具体业务单据ID',
    deleted TINYINT DEFAULT 0 COMMENT '0:正常, 1:已作废',
    delete_by BIGINT COMMENT '作废人ID',
    delete_time DATETIME COMMENT '作废时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
    CONSTRAINT fk_event_operator FOREIGN KEY (operator_id) REFERENCES user(id)
) COMMENT '事件中心表(统一记录所有动物生命周期事件)';

-- ==========================================================
-- 4. 饲养管理
-- ==========================================================
CREATE TABLE feeding_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shed_id BIGINT NOT NULL COMMENT '目标栏舍',
    inventory_id BIGINT DEFAULT NULL COMMENT '关联库存ID',
    feed_type VARCHAR(50) NOT NULL COMMENT '饲料类型',
    amount_per_animal DECIMAL(10,2) NOT NULL COMMENT '每只动物投喂量(kg)',
    feeding_time TIME NOT NULL COMMENT '计划投喂时间',
    status TINYINT DEFAULT 1 COMMENT '1:启用, 0:停用',
    deleted TINYINT DEFAULT 0 COMMENT '0:正常, 1:已作废',
    delete_by BIGINT COMMENT '作废人ID',
    delete_time DATETIME COMMENT '作废时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_fp_shed FOREIGN KEY (shed_id) REFERENCES shed(id),
    CONSTRAINT fk_fp_inventory FOREIGN KEY (inventory_id) REFERENCES inventory(id) ON DELETE SET NULL
) COMMENT '饲养计划表';

CREATE TABLE feeding_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT COMMENT '关联计划ID(可空,表示临时加餐)',
    shed_id BIGINT NOT NULL,
    inventory_id BIGINT DEFAULT NULL COMMENT '关联库存ID',
    operator_id BIGINT NOT NULL COMMENT '饲养员',
    animal_id BIGINT DEFAULT NULL COMMENT '关联动物ID(可空,按栏投喂时为空)',
    feed_type VARCHAR(50) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总投喂量(kg)',
    time DATETIME NOT NULL COMMENT '实际执行时间',
    deleted TINYINT DEFAULT 0 COMMENT '0:正常, 1:已作废',
    delete_by BIGINT COMMENT '作废人ID',
    delete_time DATETIME COMMENT '作废时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_fr_plan FOREIGN KEY (plan_id) REFERENCES feeding_plan(id) ON DELETE SET NULL,
    CONSTRAINT fk_fr_shed FOREIGN KEY (shed_id) REFERENCES shed(id),
    CONSTRAINT fk_fr_operator FOREIGN KEY (operator_id) REFERENCES user(id),
    CONSTRAINT fk_fr_inventory FOREIGN KEY (inventory_id) REFERENCES inventory(id) ON DELETE SET NULL,
    CONSTRAINT fk_fr_animal FOREIGN KEY (animal_id) REFERENCES animal(id) ON DELETE SET NULL
) COMMENT '饲养执行记录';

-- ==========================================================
-- 5. 疾病与治疗管理 (三层架构: 症状→诊断→治疗)
-- ==========================================================
CREATE TABLE symptom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    animal_id BIGINT NOT NULL,
    observer_id BIGINT NOT NULL COMMENT '发现人(通常是饲养员)',
    symptom_desc TEXT NOT NULL COMMENT '症状描述',
    observe_time DATETIME NOT NULL COMMENT '发现时间',
    status TINYINT DEFAULT 0 COMMENT '0:待诊断, 1:已诊断',
    deleted TINYINT DEFAULT 0 COMMENT '0:正常, 1:已作废',
    delete_by BIGINT COMMENT '作废人ID',
    delete_time DATETIME COMMENT '作废时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_symptom_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
    CONSTRAINT fk_symptom_observer FOREIGN KEY (observer_id) REFERENCES user(id)
) COMMENT '症状记录表';

CREATE TABLE diagnosis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symptom_id BIGINT NOT NULL COMMENT '关联症状ID',
    animal_id BIGINT NOT NULL COMMENT '关联动物ID',
    vet_id BIGINT NOT NULL COMMENT '诊断兽医ID',
    disease_name VARCHAR(100) NOT NULL COMMENT '确诊疾病名称',
    severity TINYINT COMMENT '1:轻微, 2:中度, 3:严重',
    diagnose_time DATETIME NOT NULL COMMENT '诊断时间',
    status TINYINT DEFAULT 0 COMMENT '0:治疗中, 1:已治愈, 2:死亡',
    deleted TINYINT DEFAULT 0 COMMENT '0:正常, 1:已作废',
    delete_by BIGINT COMMENT '作废人ID',
    delete_time DATETIME COMMENT '作废时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_diagnosis_symptom FOREIGN KEY (symptom_id) REFERENCES symptom(id),
    CONSTRAINT fk_diagnosis_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
    CONSTRAINT fk_diagnosis_vet FOREIGN KEY (vet_id) REFERENCES user(id)
) COMMENT '诊断记录表';

CREATE TABLE treatment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    diagnosis_id BIGINT NOT NULL COMMENT '关联诊断ID',
    animal_id BIGINT NOT NULL,
    operator_id BIGINT NOT NULL COMMENT '执行兽医ID',
    medicine_id BIGINT NULL COMMENT '使用药品ID(多物品时可为空)',
    time DATETIME NOT NULL COMMENT '治疗时间',
    result VARCHAR(200) COMMENT '治疗效果观察',
    deleted TINYINT DEFAULT 0 COMMENT '0:正常, 1:已作废',
    delete_by BIGINT COMMENT '作废人ID',
    delete_time DATETIME COMMENT '作废时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_treatment_diagnosis FOREIGN KEY (diagnosis_id) REFERENCES diagnosis(id),
    CONSTRAINT fk_treatment_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
    CONSTRAINT fk_treatment_operator FOREIGN KEY (operator_id) REFERENCES user(id),
    CONSTRAINT fk_treatment_medicine FOREIGN KEY (medicine_id) REFERENCES inventory(id) ON DELETE SET NULL
) COMMENT '治疗/用药记录表';

-- 治疗明细表(支持多物品)
CREATE TABLE treatment_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    treatment_id BIGINT NOT NULL COMMENT '关联治疗记录ID',
    inventory_id BIGINT NOT NULL COMMENT '关联库存ID',
    item_name VARCHAR(100) NOT NULL COMMENT '物品名称(冗余)',
    item_type TINYINT COMMENT '1:饲料, 2:药品, 3:疫苗, 4:器械',
    dosage DECIMAL(10,2) NOT NULL COMMENT '使用剂量/数量',
    deleted TINYINT DEFAULT 0 COMMENT '0:正常, 1:已作废',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_treatment_id (treatment_id),
    INDEX idx_inventory_id (inventory_id),
    CONSTRAINT fk_ti_treatment FOREIGN KEY (treatment_id) REFERENCES treatment(id) ON DELETE CASCADE,
    CONSTRAINT fk_ti_inventory FOREIGN KEY (inventory_id) REFERENCES inventory(id)
) COMMENT '治疗明细表(支持多物品)';

-- ==========================================================
-- 6. 疫苗管理
-- ==========================================================
CREATE TABLE vaccine (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '疫苗名称',
    target_disease VARCHAR(100) NOT NULL COMMENT '预防疾病',
    manufacturer VARCHAR(100),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '疫苗信息表';

CREATE TABLE vaccine_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    animal_id BIGINT NOT NULL,
    vaccine_id BIGINT NOT NULL,
    operator_id BIGINT NOT NULL,
    batch_number VARCHAR(50) NOT NULL COMMENT '疫苗批号',
    time DATETIME NOT NULL COMMENT '接种时间',
    next_due_date DATE COMMENT '下次接种日期',
    deleted TINYINT DEFAULT 0 COMMENT '0:正常, 1:已作废',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_vr_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
    CONSTRAINT fk_vr_vaccine FOREIGN KEY (vaccine_id) REFERENCES vaccine(id),
    CONSTRAINT fk_vr_operator FOREIGN KEY (operator_id) REFERENCES user(id)
) COMMENT '疫苗接种记录表';

-- ==========================================================
-- 7. 库存管理
-- ==========================================================
CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL COMMENT '物品名称',
    item_type TINYINT COMMENT '1:饲料, 2:药品, 3:疫苗, 4:器械',
    batch_number VARCHAR(50) NOT NULL COMMENT '批次号',
    quantity DECIMAL(10,2) NOT NULL COMMENT '当前数量',
    unit VARCHAR(20) NOT NULL COMMENT '单位(kg/盒/支)',
    produce_date DATE COMMENT '生产日期',
    expire_date DATE NOT NULL COMMENT '过期时间',
    deleted TINYINT DEFAULT 0 COMMENT '0:正常, 1:已作废',
    delete_by BIGINT COMMENT '作废人ID',
    delete_time DATETIME COMMENT '作废时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_item_batch (item_name, batch_number),
    INDEX idx_item_type (item_type),
    INDEX idx_expire_date (expire_date),
    INDEX idx_expire_date_deleted (expire_date, deleted)
) COMMENT '库存表(支持批次和过期时间)';

CREATE TABLE inventory_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_id BIGINT NOT NULL,
    operation_type TINYINT COMMENT '1:入库, 2:出库, 3:损耗, 4:过期销毁',
    quantity DECIMAL(10,2) NOT NULL COMMENT '操作数量',
    operator_id BIGINT NOT NULL,
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(200),
    INDEX idx_inventory_operate (inventory_id, operate_time),
    INDEX idx_inventory_operator (operator_id),
    CONSTRAINT fk_il_inventory FOREIGN KEY (inventory_id) REFERENCES inventory(id),
    CONSTRAINT fk_il_operator FOREIGN KEY (operator_id) REFERENCES user(id)
) COMMENT '出入库记录表';

-- ==========================================================
-- 8. 预警管理
-- ==========================================================
CREATE TABLE alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_type TINYINT COMMENT '1:temperature_anomaly, 2:no_food_long, 3:death_rate_high, 4:medicine_expire',
    animal_id BIGINT COMMENT '关联动物ID',
    shed_id BIGINT COMMENT '关联栏舍ID',
    inventory_id BIGINT COMMENT '关联库存ID',
    alert_msg TEXT NOT NULL COMMENT '预警内容',
    status TINYINT DEFAULT 0 COMMENT '0:未处理, 1:已处理',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator_id BIGINT COMMENT '提交人ID',
    handle_time DATETIME,
    handler_id BIGINT,
    deleted TINYINT DEFAULT 0 COMMENT '0:正常, 1:已作废',
    delete_by BIGINT COMMENT '作废人ID',
    delete_time DATETIME COMMENT '作废时间',
    INDEX idx_alert_status (status, rule_type),
    CONSTRAINT fk_alert_animal FOREIGN KEY (animal_id) REFERENCES animal(id) ON DELETE SET NULL,
    CONSTRAINT fk_alert_shed FOREIGN KEY (shed_id) REFERENCES shed(id) ON DELETE SET NULL,
    CONSTRAINT fk_alert_inventory FOREIGN KEY (inventory_id) REFERENCES inventory(id) ON DELETE SET NULL,
    CONSTRAINT fk_alert_creator FOREIGN KEY (creator_id) REFERENCES user(id) ON DELETE SET NULL,
    CONSTRAINT fk_alert_handler FOREIGN KEY (handler_id) REFERENCES user(id) ON DELETE SET NULL
) COMMENT '系统预警表';

-- ==========================================================
-- 9. 系统日志（合并AI审计日志和操作日志）
-- ==========================================================
CREATE TABLE system_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    log_type TINYINT NOT NULL DEFAULT 1 COMMENT '1:AI查询审计, 2:操作日志',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    module VARCHAR(50) COMMENT '模块',
    action VARCHAR(50) COMMENT '操作',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    query_content TEXT COMMENT 'AI查询内容(log_type=1时)',
    accessed_modules VARCHAR(255) COMMENT 'AI访问的模块列表',
    response_content TEXT COMMENT 'AI响应内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_log_type (log_type),
    INDEX idx_create_time (create_time),
    CONSTRAINT fk_sl_user FOREIGN KEY (user_id) REFERENCES user(id)
) COMMENT '系统日志表(合并audit_log和operation_log)';

-- ==========================================================
-- 10. RBAC 初始化数据
-- 默认密码统一为 $2a$10$..jwM3xAH8aadde2ap0klugkyaBGEtIMJ8DBTqlbhm36JxIxejWvK (123456)
-- ==========================================================
INSERT INTO role (id, role_name, role_code, description) VALUES
(1, '管理员', 'admin', '系统管理员，拥有全部权限'),
(2, '牧场主', 'owner', '负责养殖经营与看板分析'),
(3, '兽医', 'vet', '负责疾病诊断与治疗'),
(4, '饲养员', 'feeder', '负责日常饲养');

INSERT INTO permission (id, parent_id, name, code, type, path, sort) VALUES
(10, 0, '数据看板', 'dashboard:view', 1, '/dashboard', 10),
(11, 0, '统计报表', 'report:view', 1, '/report', 11),
(100, 0, '动物管理', 'animal:view', 1, '/animal', 100),
(101, 100, '新增动物', 'animal:add', 2, NULL, 101),
(102, 100, '修改动物', 'animal:edit', 2, NULL, 102),
(103, 100, '删除动物', 'animal:delete', 2, NULL, 103),
(104, 100, '作废动物', 'animal:invalidate', 2, NULL, 104),
(200, 0, '饲养管理', 'feeding:view', 1, '/feeding', 200),
(201, 200, '饲养操作', 'feeding:add', 2, NULL, 201),
(202, 200, '新增饲养计划', 'feeding:plan:add', 2, NULL, 202),
(203, 200, '修改饲养计划', 'feeding:plan:edit', 2, NULL, 203),
(204, 200, '录入饲养记录', 'feeding:record:add', 2, NULL, 204),
(205, 200, '调整计划状态', 'feeding:plan:status', 2, NULL, 205),
(206, 200, '删除饲养计划', 'feeding:plan:delete', 2, NULL, 206),
(207, 200, '作废饲养计划', 'feeding:plan:invalidate', 2, NULL, 207),
(208, 200, '作废饲养记录', 'feeding:record:invalidate', 2, NULL, 208),
(300, 0, '疾病管理', 'disease:view', 1, '/disease', 300),
(301, 300, '记录疾病', 'disease:add', 2, NULL, 301),
(302, 300, '疾病诊断', 'diagnosis:add', 2, NULL, 302),
(303, 300, '疾病治疗', 'treatment:add', 2, NULL, 303),
(304, 300, '疫苗录入', 'vaccine:add', 2, NULL, 304),
(810, 300, '疫苗编辑', 'vaccine:edit', 2, NULL, 810),
(811, 300, '作废接种记录', 'vaccine:invalidate', 2, NULL, 811),
(305, 300, '作废症状', 'symptom:invalidate', 2, NULL, 305),
(306, 300, '作废诊断', 'diagnosis:invalidate', 2, NULL, 306),
(307, 300, '作废治疗', 'treatment:invalidate', 2, NULL, 307),
(308, 300, '症状上报', 'symptom:view', 2, NULL, 308),
(309, 300, '诊断记录', 'diagnosis:view', 2, NULL, 309),
(310, 300, '治疗记录', 'treatment:view', 2, NULL, 310),
(350, 0, '事件中心', 'event:view', 1, '/event', 350),
(351, 350, '记录事件', 'event:add', 2, NULL, 351),
(400, 0, '预警中心', 'alert:view', 1, '/alert', 400),
(401, 400, '处理预警', 'alert:handle', 2, NULL, 401),
(402, 400, '触发检测', 'alert:check', 2, NULL, 402),
(403, 400, '提交预警', 'alert:add', 2, NULL, 403),
(404, 400, '作废预警', 'alert:invalidate', 2, NULL, 404),
(500, 0, '库存管理', 'inventory:view', 1, '/inventory', 500),
(501, 500, '新增库存', 'inventory:add', 2, NULL, 501),
(502, 500, '修改库存', 'inventory:edit', 2, NULL, 502),
(503, 500, '删除库存', 'inventory:delete', 2, NULL, 503),
(504, 500, '作废库存', 'inventory:invalidate', 2, NULL, 504),
(600, 0, '批次管理', 'batch:add', 2, NULL, 600),
(601, 0, '出栏管理', 'sale:add', 2, NULL, 601),
(700, 0, 'AI助手', 'ai:view', 1, '/ai', 700),
(800, 0, '系统管理', 'system:view', 1, '/system', 800),
(801, 800, '新增用户', 'system:user:add', 2, NULL, 801),
(802, 800, '修改用户', 'system:user:edit', 2, NULL, 802),
(803, 800, '删除用户', 'system:user:delete', 2, NULL, 803),
(804, 800, '系统全权限', 'system:*', 2, NULL, 804),
(805, 800, '注册审核页面', 'system:register:view', 1, '/register-audit', 805),
(806, 805, '通过注册申请', 'system:register:approve', 2, NULL, 806),
(807, 805, '驳回注册申请', 'system:register:reject', 2, NULL, 807),
(808, 800, '作废数据页面', 'system:invalid:view', 1, '/system/invalid', 808),
(809, 808, '恢复作废数据', 'system:invalid:restore', 2, NULL, 809);

INSERT INTO role_permission (role_id, permission_id) VALUES
 (1, 10), (1, 11), (1, 100), (1, 101), (1, 102), (1, 103), (1, 104),
 (1, 200), (1, 201), (1, 202), (1, 203), (1, 204), (1, 205), (1, 206), (1, 207), (1, 208),
 (1, 300), (1, 301), (1, 302), (1, 303), (1, 304), (1, 305), (1, 306), (1, 307), (1, 810), (1, 811),
(1, 350), (1, 351),
 (1, 400), (1, 401), (1, 402), (1, 403), (1, 404),
 (1, 500), (1, 501), (1, 502), (1, 503), (1, 504),
(1, 600), (1, 601), (1, 700),
 (1, 800), (1, 801), (1, 802), (1, 803), (1, 804), (1, 805), (1, 806), (1, 807), (1, 808), (1, 809),
(2, 10), (2, 11), (2, 100), (2, 101), (2, 102), (2, 104),
(2, 200), (2, 201), (2, 202), (2, 205), (2, 206), (2, 207), (2, 700), (2, 805), (2, 806), (2, 807),
(2, 300), (2, 350), (2, 400), (2, 401), (2, 402), (2, 403), (2, 500), (2, 502), (2, 600), (2, 601),
(3, 100), (3, 300), (3, 301), (3, 302), (3, 303), (3, 304), (3, 810), (3, 811), (3, 400), (3, 403), (3, 700),
(4, 100), (4, 200), (4, 201), (4, 204), (4, 400), (4, 403), (4, 700);

INSERT INTO user (id, username, password, real_name, phone, status, role_id, audit_status) VALUES
(1, 'admin', '$2a$10$..jwM3xAH8aadde2ap0klugkyaBGEtIMJ8DBTqlbhm36JxIxejWvK', '系统管理员', '13800000000', 1, 1, 1),
(2, 'owner1', '$2a$10$..jwM3xAH8aadde2ap0klugkyaBGEtIMJ8DBTqlbhm36JxIxejWvK', '牧场主一号', '13800000001', 1, 2, 1),
(3, 'vet1', '$2a$10$..jwM3xAH8aadde2ap0klugkyaBGEtIMJ8DBTqlbhm36JxIxejWvK', '兽医一号', '13800000002', 1, 3, 1),
(4, 'feeder1', '$2a$10$..jwM3xAH8aadde2ap0klugkyaBGEtIMJ8DBTqlbhm36JxIxejWvK', '饲养员一号', '13800000003', 1, 4, 1);

-- ==========================================================
-- 11. 示例业务数据
-- ==========================================================
INSERT INTO shed (id, name, capacity, current_count, manager_id) VALUES
(1, 'A01 栏舍', 120, 3, 4),
(2, 'B01 栏舍', 80, 2, 4);

INSERT INTO animal (id, ear_tag, species, variety, birth_date, gender, shed_id, status) VALUES
(1, 'EAR2026001', '生猪', '长白猪', DATE_SUB(CURDATE(), INTERVAL 180 DAY), 1, 1, 1),
(2, 'EAR2026002', '生猪', '约克夏猪', DATE_SUB(CURDATE(), INTERVAL 150 DAY), 2, 1, 1),
(3, 'EAR2026003', '奶牛', '荷斯坦牛', DATE_SUB(CURDATE(), INTERVAL 400 DAY), 2, 2, 1),
(4, 'EAR2026004', '肉牛', '西门塔尔牛', DATE_SUB(CURDATE(), INTERVAL 320 DAY), 1, 2, 3),
(5, 'EAR2026005', '山羊', '波尔山羊', CURDATE(), 2, 1, 1);

INSERT INTO inventory (id, item_name, item_type, batch_number, quantity, unit, produce_date, expire_date) VALUES
(1, '育肥猪配合饲料', 1, 'FD20260401', 3500.00, 'kg', DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_ADD(CURDATE(), INTERVAL 180 DAY)),
(2, '氟苯尼考注射液', 2, 'MD20260315', 120.00, '盒', DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
(3, '口蹄疫灭活疫苗', 3, 'VC20260320', 260.00, '支', DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_ADD(CURDATE(), INTERVAL 300 DAY)),
(4, '一次性注射器', 4, 'EQ20260405', 500.00, '支', DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 720 DAY));

INSERT INTO inventory_log (id, inventory_id, operation_type, quantity, operator_id, operate_time, remark) VALUES
(1, 1, 1, 4000.00, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), '月度饲料采购入库'),
(2, 1, 2, 500.00, 4, DATE_SUB(NOW(), INTERVAL 1 DAY), '日常投喂消耗'),
(3, 2, 1, 120.00, 1, DATE_SUB(NOW(), INTERVAL 15 DAY), '兽药采购入库'),
(4, 3, 1, 260.00, 3, DATE_SUB(NOW(), INTERVAL 12 DAY), '疫苗采购入库');

INSERT INTO feeding_plan (id, shed_id, inventory_id, feed_type, amount_per_animal, feeding_time, status) VALUES
(1, 1, 1, '育肥猪配合饲料', 2.50, '08:00:00', 1),
(2, 2, 1, '反刍动物精料', 3.20, '09:00:00', 1);

INSERT INTO feeding_record (id, plan_id, shed_id, operator_id, feed_type, total_amount, time) VALUES
(1, 1, 1, 4, '育肥猪配合饲料', 180.00, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 1, 1, 4, '育肥猪配合饲料', 175.00, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 1, 1, 4, '育肥猪配合饲料', 178.00, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 2, 2, 4, '反刍动物精料', 96.00, DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO symptom (id, animal_id, observer_id, symptom_desc, observe_time, status) VALUES
(1, 4, 4, '体温升高、精神沉郁、采食量下降', DATE_SUB(NOW(), INTERVAL 2 DAY), 1),
(2, 2, 4, '轻微咳嗽，待进一步观察', DATE_SUB(NOW(), INTERVAL 5 HOUR), 0);

INSERT INTO diagnosis (id, symptom_id, animal_id, vet_id, disease_name, severity, diagnose_time, status) VALUES
(1, 1, 4, 3, '牛呼吸道感染', 2, DATE_SUB(NOW(), INTERVAL 1 DAY), 0);

INSERT INTO treatment (id, diagnosis_id, animal_id, operator_id, medicine_id, time, result) VALUES
(1, 1, 4, 3, 2, DATE_SUB(NOW(), INTERVAL 20 HOUR), '首次用药后体温有所下降，继续观察');

INSERT INTO vaccine (id, name, target_disease, manufacturer) VALUES
(1, '口蹄疫灭活疫苗', '口蹄疫', '中牧股份'),
(2, '猪瘟活疫苗', '猪瘟', '哈药集团');

INSERT INTO vaccine_record (id, animal_id, vaccine_id, operator_id, batch_number, time, next_due_date) VALUES
(1, 1, 2, 3, 'VC20260320', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 150 DAY)),
(2, 3, 1, 3, 'VC20260320', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_ADD(CURDATE(), INTERVAL 160 DAY));

INSERT INTO event (id, animal_id, event_type, event_time, operator_id, description, related_id) VALUES
(1, 1, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 4, '执行早间投喂计划', 1),
(2, 4, 2, DATE_SUB(NOW(), INTERVAL 2 DAY), 4, '发现异常症状并上报', 1),
(3, 4, 3, DATE_SUB(NOW(), INTERVAL 20 HOUR), 3, '完成首次治疗', 1),
(4, 3, 4, DATE_SUB(NOW(), INTERVAL 20 DAY), 3, '完成口蹄疫疫苗接种', 2),
(5, 5, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), 4, '新生羔羊补充营养', NULL);

INSERT INTO alert (id, rule_type, animal_id, shed_id, inventory_id, alert_msg, status, create_time, creator_id, handle_time, handler_id) VALUES
(1, 4, NULL, NULL, 2, '氟苯尼考注射液库存请在到期前完成使用计划核查', 0, DATE_SUB(NOW(), INTERVAL 6 HOUR), NULL, NULL, NULL),
(2, 1, 4, NULL, NULL, '耳标 EAR2026004 体温异常，请兽医复检', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 3, DATE_SUB(NOW(), INTERVAL 12 HOUR), 3),
(3, 2, 2, NULL, NULL, '耳标 EAR2026002 近 6 小时采食异常偏低', 0, DATE_SUB(NOW(), INTERVAL 2 HOUR), 4, NULL, NULL);