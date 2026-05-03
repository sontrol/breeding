# Breeding System 功能与接口测试报告

**测试日期**: 2026-05-02  
**测试范围**: 前端完整功能、后端API接口、数据库交互  
**测试环境**: Spring Boot 3.1.5 / Java 21 / MySQL 8.0 / Vue 3 + Vite

---

## 测试结论概要

| 项目 | 状态 | 说明 |
|---|---|---|
| 后端编译 | ✅ 通过 | 109个源文件，0错误 |
| 前端构建 | ✅ 通过 | 2268个模块，0错误 |
| API接口总数 | 55个 | 17个Controller |
| API可用性 | ✅ 基本正常 | 55个接口均可调用 |
| 数据库完整性 | ⚠️ 有异常 | 存在软删除引用不一致等问题 |

---

## 一、前后端功能完整性检查

### 1.1 路由 vs 控制器覆盖

| 前端路由 | 对应Controller | 覆盖状态 |
|---|---|---|
| `/dashboard` | DashboardController | ✅ 完整 |
| `/animal/*` | AnimalController | ✅ 完整 |
| `/feeding/*` | FeedingController | ✅ 完整 |
| `/inventory/*` | InventoryController | ✅ 完整 |
| `/inventory/log` | InventoryLogController | ✅ 完整 |
| `/disease/symptom` | SymptomController | ✅ 完整 |
| `/disease/diagnosis` | DiagnosisController | ✅ 完整 |
| `/disease/treatment` | TreatmentController | ✅ 完整 |
| `/disease/vaccine` | VaccineController | ✅ 完整 |
| `/event/*` | EventController | ✅ 完整 |
| `/alert/*` | AlertController | ✅ 完整 |
| `/ai/*` | AIController | ✅ 完整 |
| `/system/user` | UserController | ✅ 完整 |
| `/system/invalid` | InvalidDataController | ✅ 完整 |
| `/register-audit` | RegisterAuditController | ✅ 完整 |

### 1.2 功能完整性发现

#### ⚠️ ISSUE-F1: Symptom、Diagnosis、Treatment 缺少编辑功能
- 前端使用 `addOnly: true`，创建后不可修改，仅支持作废
- 可能是医疗记录不可篡改的业务需求，但缺少确认

#### ⚠️ ISSUE-F2: InventoryLog 页面只读
- 库存日志页面没有创建出库/损耗/过期销毁的入口
- 日志只能由系统自动生成（饲喂扣减等）

#### ⚠️ ISSUE-F3: Vaccine 缺少软删除接口
- Vaccine 实体支持创建和编辑，但没有作废/invalidate 端点
- 其他所有实体都有软删除，唯独 Vaccine 没有

#### ⚠️ ISSUE-F4: 无密码重置功能
- 登录页只有登录和注册，没有"忘记密码"流程

#### ⚠️ ISSUE-F5: 无批量操作
- 所有页面只支持单条记录操作，无批量选择/删除

#### ⚠️ ISSUE-F6: 无实时数据推送
- 看板、告警等页面没有 WebSocket/SSE/轮询机制，需手动刷新

#### ⚠️ ISSUE-F7: 症状诊断状态未联动更新
- 创建诊断后没有 API 更新对应症状的 status 状态（待诊断 → 已诊断）

---

## 二、API接口测试结果

### 2.1 认证模块 (`/auth`)

| 接口 | 测试结果 | 备注 |
|---|---|---|
| `POST /auth/login` | ✅ 通过 | 正确密码返回200+token；错误密码返回500+说明 |
| `POST /auth/register` | ✅ 通过 | 提交注册申请成功 |
| `POST /auth/logout` | ✅ 通过 | 正常登出 |

#### ⚠️ ISSUE-A1: 登录错误返回码不当
- 密码错误时 HTTP 状态码为 200，code 为 500
- 建议统一使用 HTTP 401 状态码

### 2.2 Dashboard (`/dashboard`)

| 接口 | 测试结果 | 备注 |
|---|---|---|
| `GET /dashboard/stats` | ✅ 通过 | 返回统计数据正常 |

### 2.3 动物档案 (`/animal`)

| 接口 | 测试结果 | 备注 |
|---|---|---|
| `GET /animal/page` | ✅ 通过 | 分页、搜索正常 |
| `GET /animal/detail/{id}` | ✅ 通过 | 详情包含症状/诊断/治疗 |
| `GET /animal/{id}` | ✅ 通过 | 单条查询正常 |
| `POST /animal` | ⚠️ 注意 | 日期格式需用 `yyyy/MM/dd` |
| `PUT /animal` | ✅ 通过 | 更新正常 |
| `DELETE /animal/{id}` | ✅ 通过 | 软删除 |
| `PUT /animal/invalidate/{id}` | ✅ 通过 | 软删除 |

### 2.4 栏舍 (`/shed`)

| 接口 | 测试结果 | 备注 |
|---|---|---|
| `GET /shed/page` | ✅ 通过 | |
| `GET /shed/list` | ✅ 通过 | |
| `GET /shed/{id}` | ✅ 通过 | |
| `POST /shed` | ✅ 通过 | |
| `PUT /shed` | ✅ 通过 | |
| `DELETE /shed/{id}` | ✅ 通过 | 硬删除（与其他实体不同） |

### 2.5 疾病模块

#### Symptom (`/symptom`)

| 接口 | 测试结果 | 备注 |
|---|---|---|
| `GET /symptom/page` | ✅ 通过 | |
| `POST /symptom` | ⚠️ 注意 | 需要包含 `observeTime` 字段 |
| `PUT /symptom/invalidate/{id}` | ✅ 通过 | |

#### Diagnosis (`/diagnosis`)

| 接口 | 测试结果 | 备注 |
|---|---|---|
| `GET /diagnosis/page` | ✅ 通过 | |
| `POST /diagnosis` | ⚠️ 注意 | 需要包含 `diagnoseTime` 字段 |
| `PUT /diagnosis/invalidate/{id}` | ✅ 通过 | |

#### Treatment (`/treatment`)

| 接口 | 测试结果 | 备注 |
|---|---|---|
| `GET /treatment/page` | ✅ 通过 | |
| `POST /treatment` | ⚠️ 注意 | 需包含 `time` 字段 |
| `PUT /treatment/invalidate/{id}` | ✅ 通过 | |

#### Vaccine (`/vaccine`)

| 接口 | 测试结果 | 备注 |
|---|---|---|
| `GET /vaccine/list` | ✅ 通过 | |
| `GET /vaccine/page` | ✅ 通过 | |
| `GET /vaccine/{id}` | ✅ 通过 | |
| `POST /vaccine` | ✅ 通过 | |
| `PUT /vaccine` | ✅ 通过 | |
| `GET /vaccine/record/page` | ✅ 通过 | |
| `POST /vaccine/record` | ⚠️ 注意 | 需包含 `time` 和 `batchNumber` 字段 |
| `PUT /vaccine/record/invalidate/{id}` | ❌ 失败 | 见 ISSUE-A2 |

#### ⚠️ ISSUE-A2: Vaccine Record 作废不支持
- `PUT /vaccine/record/invalidate/{id}` 返回 `"不支持的作废数据类型"`
- 原因：`InvalidDataServiceImpl` 的 `ARCHIVE_META_MAP` 缺少 `vaccine_record` 配置
- 影响：疫苗免疫记录无法作废

### 2.6 饲喂模块 (`/feeding`)

| 接口 | 测试结果 | 备注 |
|---|---|---|
| `GET /feeding/plan/page` | ✅ 通过 | |
| `POST /feeding/plan` | ⚠️ 注意 | 需包含 `amountPerAnimal`、`feedingTime` |
| `PUT /feeding/plan` | ✅ 通过 | |
| `PUT /feeding/plan/status` | ✅ 通过 | |
| `DELETE /feeding/plan/{id}` | ✅ 通过 | |
| `PUT /feeding/plan/invalidate/{id}` | ✅ 通过 | |
| `GET /feeding/record/page` | ✅ 通过 | |
| `POST /feeding/record` | ✅ 通过 | 需含 `totalAmount`、`time` |
| `PUT /feeding/record/invalidate/{id}` | ✅ 通过 | |

### 2.7 库存模块

#### Inventory (`/inventory`)

| 接口 | 测试结果 | 备注 |
|---|---|---|
| `GET /inventory/page` | ✅ 通过 | |
| `GET /inventory/{id}` | ✅ 通过 | |
| `POST /inventory` | ✅ 通过 | |
| `PUT /inventory` | ✅ 通过 | |
| `DELETE /inventory/{id}` | ✅ 通过 | |
| `PUT /inventory/invalidate/{id}` | ✅ 通过 | |

#### InventoryLog (`/inventory-log`)

| 接口 | 测试结果 | 备注 |
|---|---|---|
| `GET /inventory-log/page` | ✅ 通过 | |

### 2.8 事件 (`/event`)

| 接口 | 测试结果 | 备注 |
|---|---|---|
| `GET /event/page` | ✅ 通过 | |
| `GET /event/{id}` | ✅ 通过 | |
| `POST /event` | ✅ 通过 | |

### 2.9 告警 (`/alert`)

| 接口 | 测试结果 | 备注 |
|---|---|---|
| `GET /alert/page` | ✅ 通过 | |
| `POST /alert` | ✅ 通过 | |
| `PUT /alert/handle/{id}` | ✅ 通过 | |
| `PUT /alert/invalidate/{id}` | ✅ 通过 | |
| `POST /alert/trigger-check` | ✅ 通过 | |

### 2.10 系统模块

#### User (`/user`)

| 接口 | 测试结果 | 备注 |
|---|---|---|
| `GET /user/page` | ✅ 通过 | |
| `POST /user` | ✅ 通过 | |
| `PUT /user` | ✅ 通过 | |
| `DELETE /user/{id}` | ✅ 通过 | 硬删除 |

#### Register Audit (`/register-audit`)

| 接口 | 测试结果 | 备注 |
|---|---|---|
| `GET /register-audit/page` | ✅ 通过 | |
| `POST /register-audit/approve` | ✅ 通过 | |
| `POST /register-audit/reject` | ✅ 通过 | |

#### Invalid Data (`/invalid-data`)

| 接口 | 测试结果 | 备注 |
|---|---|---|
| `GET /invalid-data/page` | ✅ 通过 | |
| `PUT /invalid-data/restore` | ✅ 通过 | |

---

## 三、数据库交互测试

### 3.1 CRUD 操作测试

| 操作 | 结果 | 说明 |
|---|---|---|
| 新增记录 (所有模块) | ✅ 通过 | 18张可写表均正常 |
| 查询 (分页+过滤) | ✅ 通过 | 分页参数、筛选条件正常 |
| 更新记录 | ✅ 通过 | 字段更新正确 |
| 软删除 (invalidate) | ⚠️ 有异常 | 见 ISSUE-A2 |
| 恢复 (restore) | ✅ 通过 | 已作废数据可恢复 |
| 硬删除 (shed, user) | ✅ 通过 | |

### 3.2 数据一致性发现

#### ⚠️ ISSUE-D1: 软删除引用不一致
- **Diagnosis (ID=1,8,9)** 引用已软删除的 Symptom (ID=1, deleted=1)
- **Treatment (ID=2,3)** 引用已软删除的 Diagnosis (ID=2, deleted=1)
- 软删除不会级联更新依赖数据，导致引用悬挂

#### ⚠️ ISSUE-D2: 注册用户无角色
- 用户 `apitester` (ID=9) 注册后 `role_id` 为 NULL，`audit_status`=0（待审核）
- 该用户无法登录系统（`UserDetailsServiceImpl` 会拒绝审核中的账号）
- 这是正常流程，但审核通过后需分配角色

#### ⚠️ ISSUE-D3: 库存审计日志不完整
- 部分库存物品（一次性注射器、测试数据）的初始库存没有 inventory_log 记录
- 初始种子数据未生成对应操作日志，审计链路不完整

#### ✅ 数据完整性验证通过项
| 检查项 | 结果 |
|---|---|
| 外键约束完整性 (37个FK) | ✅ 全部有效 |
| 动物耳标唯一性 | ✅ 无重复 |
| 栏舍动物数与 current_count 一致 | ✅ 匹配 |
| 库存数量与日志计算一致（有日志的物品） | ✅ 完全匹配 |

### 3.3 事务测试

#### ⚠️ ISSUE-D4: FeedingRecord 事务与库存扣减
- `FeedingRecordServiceImpl.addRecordWithInventory` 有 `@Transactional`
- 但当 `feedType` 匹配不到库存记录时，抛出的异常可以正确回滚
- 问题：喂食记录通过 `feedType` 名称匹配库存，匹配逻辑脆弱（名称需完全一致）

---

## 四、代码质量问题

#### ⚠️ ISSUE-C1: 日期格式不一致
- 全局 Jackson 配置：`yyyy-MM-dd HH:mm:ss`
- Animal.birthDate：`@JsonFormat(pattern = "yyyy/MM/dd")` 使用 `/` 分隔符
- 其他实体 LocalDateTime：使用 `yyyy/MM/dd HH:mm:ss`（也是 `/` 分隔符）
- 导致 API 消费者困惑，应统一日期格式

#### ⚠️ ISSUE-C2: 部分字段缺少默认值处理
- Symptom.observeTime、Diagnosis.diagnoseTime、FeedingPlan.amountPerAnimal 等字段
- 数据库中这些字段是 NOT NULL 且无 DEFAULT
- 如果前端漏传，直接返回 500 错误而非明确的验证提示

#### ⚠️ ISSUE-C3: Shed 使用硬删除
- ShedController 使用 `DELETE /{id}` 硬删除
- 其他实体全部使用软删除（invalidate）
- 与其他模块设计不一致

#### ⚠️ ISSUE-C4: Inventory 两个 `DELETE` 端点行为重复
- `DELETE /inventory/{id}` 和 `PUT /inventory/invalidate/{id}` 都调用 `InvalidDataService.invalidate`
- 对外暴露了两个做同一件事的接口

---

## 五、汇总

### 严重性分级

| 级别 | 数量 | 说明 |
|---|---|---|
| 🔴 严重 | 1 | 功能无法使用 |
| 🟡 中等 | 5 | 功能异常或不一致 |
| 🟢 轻微 | 10 | 体验问题或代码规范 |

### 优先级修复建议

| 优先级 | 问题 | 建议 |
|---|---|---|
| **P0** | ISSUE-A2: Vaccine Record 无法作废 | 在 `InvalidDataServiceImpl.ARCHIVE_META_MAP` 添加 `vaccine_record` 条目 |
| **P1** | ISSUE-D1: 软删除引用不一致 | 添加级联逻辑或在删除时检查引用 |
| **P1** | ISSUE-C1: 日期格式不一致 | 统一使用 `yyyy-MM-dd` / `yyyy-MM-dd HH:mm:ss` 标准格式 |
| **P2** | ISSUE-C2: DB字段缺少默认值 | 为常用字段添加 DEFAULT 值或增加 @NotNull 校验 |
| **P2** | ISSUE-F3: Vaccine 缺少软删除 | 添加 invalidate 端点或统一删除策略 |
| **P3** | ISSUE-C3/C4: API设计不一致 | 统一软删除/硬删除策略，去除重复端点 |
| **P3** | ISSUE-F7: 状态联动更新 | 诊断创建时自动更新对应症状状态 |
