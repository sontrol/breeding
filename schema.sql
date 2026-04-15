CREATE DATABASE IF NOT EXISTS breeding DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE breeding;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS operation_log;
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
DROP TABLE IF EXISTS animal_status_log;
DROP TABLE IF EXISTS animal;
DROP TABLE IF EXISTS shed;
DROP TABLE IF EXISTS role_permission;
DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS permission;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS user;
SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================================
-- 1. 用户与权限管理 (RBAC)
-- ==========================================================
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    status TINYINT DEFAULT 1 COMMENT '1:正常, 0:禁用',
    apply_role_code VARCHAR(50) COMMENT '注册申请角色编码',
    audit_status TINYINT DEFAULT 1 COMMENT '0:待审核, 1:已通过, 2:已驳回',
    audit_remark VARCHAR(255) COMMENT '审核备注',
    audit_by BIGINT COMMENT '审核人ID',
    audit_time DATETIME COMMENT '审核时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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

CREATE TABLE user_role (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id)
) COMMENT '用户角色关联表';

CREATE TABLE role_permission (
    role_id BIGINT,
    permission_id BIGINT,
    PRIMARY KEY (role_id, permission_id)
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
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
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
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '动物档案表';

CREATE TABLE animal_status_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    animal_id BIGINT NOT NULL,
    old_status TINYINT,
    new_status TINYINT NOT NULL,
    operator_id BIGINT NOT NULL,
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '动物状态变更记录表';

-- ==========================================================
-- 3. 事件中心 (核心)
-- ==========================================================
CREATE TABLE event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    animal_id BIGINT NOT NULL COMMENT '关联动物ID',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型: feeding, disease, treatment, vaccine, death, sale, transfer',
    event_time DATETIME NOT NULL COMMENT '事件发生时间',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    description TEXT COMMENT '事件详情描述',
    related_id BIGINT COMMENT '关联的具体业务单据ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '事件中心表(统一记录所有动物生命周期事件)';

-- ==========================================================
-- 4. 饲养管理
-- ==========================================================
CREATE TABLE feeding_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shed_id BIGINT NOT NULL COMMENT '目标栏舍',
    feed_type VARCHAR(50) NOT NULL COMMENT '饲料类型',
    amount_per_animal DECIMAL(10,2) NOT NULL COMMENT '每只动物投喂量(kg)',
    feeding_time TIME NOT NULL COMMENT '计划投喂时间',
    status TINYINT DEFAULT 1 COMMENT '1:启用, 0:停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '饲养计划表';

CREATE TABLE feeding_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT COMMENT '关联计划ID(可空,表示临时加餐)',
    shed_id BIGINT NOT NULL,
    operator_id BIGINT NOT NULL COMMENT '饲养员',
    feed_type VARCHAR(50) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总投喂量(kg)',
    execute_time DATETIME NOT NULL COMMENT '实际执行时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '饲养执行记录';

-- ==========================================================
-- 5. 疾病与治疗管理 (三层架构)
-- ==========================================================
CREATE TABLE symptom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    animal_id BIGINT NOT NULL,
    observer_id BIGINT NOT NULL COMMENT '发现人(通常是饲养员)',
    symptom_desc TEXT NOT NULL COMMENT '症状描述',
    observe_time DATETIME NOT NULL COMMENT '发现时间',
    status TINYINT DEFAULT 0 COMMENT '0:待诊断, 1:已诊断',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '症状记录表';

CREATE TABLE diagnosis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symptom_id BIGINT NOT NULL COMMENT '关联症状ID',
    animal_id BIGINT NOT NULL,
    vet_id BIGINT NOT NULL COMMENT '诊断兽医ID',
    disease_name VARCHAR(100) NOT NULL COMMENT '确诊疾病名称',
    severity TINYINT COMMENT '1:轻微, 2:中度, 3:严重',
    diagnose_time DATETIME NOT NULL COMMENT '诊断时间',
    status TINYINT DEFAULT 0 COMMENT '0:治疗中, 1:已治愈, 2:死亡',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '诊断记录表';

CREATE TABLE treatment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    diagnosis_id BIGINT NOT NULL COMMENT '关联诊断ID',
    animal_id BIGINT NOT NULL,
    vet_id BIGINT NOT NULL COMMENT '执行兽医ID',
    medicine_id BIGINT NOT NULL COMMENT '使用药品ID',
    dosage DECIMAL(10,2) NOT NULL COMMENT '用药剂量',
    treatment_time DATETIME NOT NULL COMMENT '治疗时间',
    result VARCHAR(200) COMMENT '治疗效果观察',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '治疗/用药记录表';

-- ==========================================================
-- 6. 疫苗管理
-- ==========================================================
CREATE TABLE vaccine (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '疫苗名称',
    target_disease VARCHAR(100) NOT NULL COMMENT '预防疾病',
    manufacturer VARCHAR(100),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '疫苗信息表';

CREATE TABLE vaccine_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    animal_id BIGINT NOT NULL,
    vaccine_id BIGINT NOT NULL,
    vet_id BIGINT NOT NULL,
    batch_number VARCHAR(50) NOT NULL COMMENT '疫苗批号',
    inject_time DATETIME NOT NULL COMMENT '接种时间',
    next_due_date DATE COMMENT '下次接种日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
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
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_item_batch (item_name, batch_number)
) COMMENT '库存表(支持批次和过期时间)';

CREATE TABLE inventory_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_id BIGINT NOT NULL,
    operation_type TINYINT COMMENT '1:入库, 2:出库, 3:损耗, 4:过期销毁',
    quantity DECIMAL(10,2) NOT NULL COMMENT '操作数量',
    operator_id BIGINT NOT NULL,
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(200)
) COMMENT '出入库记录表';

-- ==========================================================
-- 8. 预警与系统日志
-- ==========================================================
CREATE TABLE alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_type VARCHAR(50) NOT NULL COMMENT 'temperature_anomaly, no_food_long, death_rate_high, medicine_expire',
    target_id BIGINT COMMENT '关联ID(animal_id/shed_id/inventory_id)',
    alert_msg TEXT NOT NULL COMMENT '预警内容',
    status TINYINT DEFAULT 0 COMMENT '0:未处理, 1:已处理',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    handle_time DATETIME,
    handler_id BIGINT
) COMMENT '系统预警表';

CREATE TABLE operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    module VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    method VARCHAR(200),
    params TEXT,
    ip VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '操作日志表';

-- ==========================================================
-- 9. AI审计与权限日志
-- ==========================================================
CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '发起查询的用户ID',
    query_content TEXT NOT NULL COMMENT '用户的原始查询内容',
    accessed_modules VARCHAR(255) COMMENT 'AI访问的模块列表(如: animal, inventory)',
    response_content TEXT COMMENT 'AI返回的完整内容',
    query_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '查询时间'
) COMMENT 'AI助手查询审计日志表';

-- ==========================================================
-- 10. RBAC 初始化数据
-- 默认密码统一为 123456
-- ==========================================================
INSERT INTO role (id, role_name, role_code, description) VALUES
(1, '管理员', 'admin', '系统管理员，拥有全部权限'),
(2, '牧场主', 'owner', '负责养殖经营与看板分析'),
(3, '兽医', 'vet', '负责疾病诊断与治疗'),
(4, '饲养员', 'feeder', '负责日常饲养与预警查看');

INSERT INTO permission (id, parent_id, name, code, type, path, sort) VALUES
(10, 0, '数据看板', 'dashboard:view', 1, '/dashboard', 10),
(11, 0, '统计报表', 'report:view', 1, '/report', 11),
(100, 0, '动物管理', 'animal:view', 1, '/animal', 100),
(101, 100, '新增动物', 'animal:add', 2, NULL, 101),
(102, 100, '修改动物', 'animal:edit', 2, NULL, 102),
(103, 100, '删除动物', 'animal:delete', 2, NULL, 103),
(200, 0, '饲养管理', 'feeding:view', 1, '/feeding', 200),
(201, 200, '饲养操作', 'feeding:add', 2, NULL, 201),
(202, 200, '新增饲养计划', 'feeding:plan:add', 2, NULL, 202),
(203, 200, '修改饲养计划', 'feeding:plan:edit', 2, NULL, 203),
(204, 200, '录入饲养记录', 'feeding:record:add', 2, NULL, 204),
(300, 0, '疾病管理', 'disease:view', 1, '/disease', 300),
(301, 300, '记录疾病', 'disease:add', 2, NULL, 301),
(302, 300, '疾病诊断', 'diagnosis:add', 2, NULL, 302),
(303, 300, '疾病治疗', 'treatment:add', 2, NULL, 303),
(304, 300, '疫苗录入', 'vaccine:add', 2, NULL, 304),
(350, 0, '事件中心', 'event:view', 1, '/event', 350),
(351, 350, '记录事件', 'event:add', 2, NULL, 351),
(400, 0, '预警中心', 'alert:view', 1, '/alert', 400),
(401, 400, '处理预警', 'alert:handle', 2, NULL, 401),
(402, 400, '触发检测', 'alert:check', 2, NULL, 402),
(500, 0, '库存管理', 'inventory:view', 1, '/inventory', 500),
(501, 500, '新增库存', 'inventory:add', 2, NULL, 501),
(502, 500, '修改库存', 'inventory:edit', 2, NULL, 502),
(503, 500, '删除库存', 'inventory:delete', 2, NULL, 503),
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
(807, 805, '驳回注册申请', 'system:register:reject', 2, NULL, 807);

INSERT INTO role_permission (role_id, permission_id) VALUES
(1, 10), (1, 11), (1, 100), (1, 101), (1, 102), (1, 103),
(1, 200), (1, 201), (1, 202), (1, 203), (1, 204),
(1, 300), (1, 301), (1, 302), (1, 303), (1, 304),
(1, 350), (1, 351),
(1, 400), (1, 401), (1, 402),
(1, 500), (1, 501), (1, 502), (1, 503),
(1, 600), (1, 601), (1, 700),
(1, 800), (1, 801), (1, 802), (1, 803), (1, 804), (1, 805), (1, 806), (1, 807),
(2, 10), (2, 11), (2, 100), (2, 101), (2, 102),
(2, 200), (2, 201), (2, 202), (2, 700), (2, 805), (2, 806), (2, 807),
(2, 300), (2, 350), (2, 600), (2, 601),
(3, 100), (3, 300), (3, 301), (3, 302), (3, 303), (3, 304), (3, 700),
(4, 100), (4, 200), (4, 201), (4, 204), (4, 400), (4, 700);

INSERT INTO user (id, username, password, real_name, phone, status, audit_status) VALUES
(1, 'admin', '$2a$10$..jwM3xAH8aadde2ap0klugkyaBGEtIMJ8DBTqlbhm36JxIxejWvK', '系统管理员', '13800000000', 1, 1),
(2, 'owner1', '$2a$10$..jwM3xAH8aadde2ap0klugkyaBGEtIMJ8DBTqlbhm36JxIxejWvK', '牧场主一号', '13800000001', 1, 1),
(3, 'vet1', '$2a$10$..jwM3xAH8aadde2ap0klugkyaBGEtIMJ8DBTqlbhm36JxIxejWvK', '兽医一号', '13800000002', 1, 1),
(4, 'feeder1', '$2a$10$..jwM3xAH8aadde2ap0klugkyaBGEtIMJ8DBTqlbhm36JxIxejWvK', '饲养员一号', '13800000003', 1, 1);

INSERT INTO user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4);

-- ==========================================================
-- 11. 示例业务数据
-- ==========================================================
INSERT INTO shed (id, name, capacity, current_count, manager_id) VALUES
(1, 'A01 栏舍', 120, 3, 4),
(2, 'B01 栏舍', 80, 2, 4);

INSERT INTO animal (id, ear_tag, species, variety, birth_date, gender, shed_id, status) VALUES
(1, 'EAR2026001', '生猪', '长白猪', DATE_SUB(CURDATE(), INTERVAL 180 DAY), 1, 1, 1),
(2, 'EAR2026002', '生猪', '约克夏猪', DATE_SUB(CURDATE(), INTERVAL 150 DAY), 2, 1, 2),
(3, 'EAR2026003', '奶牛', '荷斯坦牛', DATE_SUB(CURDATE(), INTERVAL 400 DAY), 2, 2, 1),
(4, 'EAR2026004', '肉牛', '西门塔尔牛', DATE_SUB(CURDATE(), INTERVAL 320 DAY), 1, 2, 3),
(5, 'EAR2026005', '山羊', '波尔山羊', CURDATE(), 2, 1, 1);

INSERT INTO animal_status_log (id, animal_id, old_status, new_status, operator_id, remark) VALUES
(1, 4, 1, 3, 3, '出现发热和采食下降，转入隔离观察');

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

INSERT INTO feeding_plan (id, shed_id, feed_type, amount_per_animal, feeding_time, status) VALUES
(1, 1, '育肥猪配合饲料', 2.50, '08:00:00', 1),
(2, 2, '反刍动物精料', 3.20, '09:00:00', 1);

INSERT INTO feeding_record (id, plan_id, shed_id, operator_id, feed_type, total_amount, execute_time) VALUES
(1, 1, 1, 4, '育肥猪配合饲料', 180.00, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 1, 1, 4, '育肥猪配合饲料', 175.00, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 1, 1, 4, '育肥猪配合饲料', 178.00, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 2, 2, 4, '反刍动物精料', 96.00, DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO symptom (id, animal_id, observer_id, symptom_desc, observe_time, status) VALUES
(1, 4, 4, '体温升高、精神沉郁、采食量下降', DATE_SUB(NOW(), INTERVAL 2 DAY), 1),
(2, 2, 4, '轻微咳嗽，待进一步观察', DATE_SUB(NOW(), INTERVAL 5 HOUR), 0);

INSERT INTO diagnosis (id, symptom_id, animal_id, vet_id, disease_name, severity, diagnose_time, status) VALUES
(1, 1, 4, 3, '牛呼吸道感染', 2, DATE_SUB(NOW(), INTERVAL 1 DAY), 0);

INSERT INTO treatment (id, diagnosis_id, animal_id, vet_id, medicine_id, dosage, treatment_time, result) VALUES
(1, 1, 4, 3, 2, 2.00, DATE_SUB(NOW(), INTERVAL 20 HOUR), '首次用药后体温有所下降，继续观察');

INSERT INTO vaccine (id, name, target_disease, manufacturer) VALUES
(1, '口蹄疫灭活疫苗', '口蹄疫', '中牧股份'),
(2, '猪瘟活疫苗', '猪瘟', '哈药集团');

INSERT INTO vaccine_record (id, animal_id, vaccine_id, vet_id, batch_number, inject_time, next_due_date) VALUES
(1, 1, 2, 3, 'VC20260320', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 150 DAY)),
(2, 3, 1, 3, 'VC20260320', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_ADD(CURDATE(), INTERVAL 160 DAY));

INSERT INTO event (id, animal_id, event_type, event_time, operator_id, description, related_id) VALUES
(1, 1, 'feeding', DATE_SUB(NOW(), INTERVAL 3 DAY), 4, '执行早间投喂计划', 1),
(2, 4, 'disease', DATE_SUB(NOW(), INTERVAL 2 DAY), 4, '发现异常症状并上报', 1),
(3, 4, 'treatment', DATE_SUB(NOW(), INTERVAL 20 HOUR), 3, '完成首次治疗', 1),
(4, 3, 'vaccine', DATE_SUB(NOW(), INTERVAL 20 DAY), 3, '完成口蹄疫疫苗接种', 2),
(5, 5, 'feeding', DATE_SUB(NOW(), INTERVAL 1 HOUR), 4, '新生羔羊补充营养', NULL);

INSERT INTO alert (id, rule_type, target_id, alert_msg, status, create_time, handle_time, handler_id) VALUES
(1, 'medicine_expire', 2, '氟苯尼考注射液库存请在到期前完成使用计划核查', 0, DATE_SUB(NOW(), INTERVAL 6 HOUR), NULL, NULL),
(2, 'temperature_anomaly', 4, '耳标 EAR2026004 体温异常，请兽医复检', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 12 HOUR), 3),
(3, 'no_food_long', 2, '耳标 EAR2026002 近 6 小时采食异常偏低', 0, DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, NULL);
