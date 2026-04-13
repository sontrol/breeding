CREATE DATABASE IF NOT EXISTS breeding_sys DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE breeding_sys;

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
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '系统用户表';

CREATE TABLE role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称(如:管理员、兽医)',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码(如:ADMIN, VET)',
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

-- 插入默认管理员数据
INSERT INTO user (username, password, real_name, status) VALUES ('admin', '$2a$10$7Q9b9Z9b9Z9b9Z9b9Z9b9e9b9Z9b9Z9b9Z9b9Z9b9Z9b9Z9b9Z9b9', '超级管理员', 1);
INSERT INTO role (role_name, role_code) VALUES ('超级管理员', 'ADMIN'), ('牧场主', 'RANCHER'), ('兽医', 'VET'), ('饲养员', 'BREEDER');
INSERT INTO user_role (user_id, role_id) VALUES (1, 1);
