# 养殖管理系统 (Breeding Management System)

本项目是一个基于 Spring Boot + Vue 3 的前后端分离架构的养殖管理系统，融合了“事件驱动模型”与“DeepSeek大模型分析能力”。

## 系统定位与设计思想

1. **统一角色RBAC体系**：系统涵盖管理员、牧场主、兽医、饲养员四种核心业务角色，抛弃了独立的四端分离设计，通过一套系统的 `user-role-permission` 模型进行动态权限路由分发和按钮级控制。
2. **动物生命周期事件驱动 (Event)**：将动物的所有行为（如喂养、发病、治疗、疫苗接种、出栏死亡等）统一抽象为 `Event` 记录在事件中心表内，支持后续基于时间线的追溯。
3. **AI 严格数据隔离架构**：集成了 DeepSeek 智能分析助手。底层设计了 `Data Access Layer` 拦截层，AI在回答前，系统会根据当前用户的 RBAC 权限为其组装对应的上下文数据摘要（例如饲养员无法获取全局财务或未授权的库存数据），确保 AI “只知其有权知”之事。
4. **自动化预警 (Alert)**：基于 `@Scheduled` 定时任务，每日凌晨对库存过期、低存栏、异常状况等数据进行扫描并生成报警记录。
5. **严苛的命名规范**：系统强制要求代码纯净性，剔除了数据库表和实体代码中的所有业务前缀（如 `b_`, `sys_`），确保了 `User`, `Animal`, `Event`, `Inventory`, `LoginDTO` 等命名高度规范化。

## 技术栈

### 后端 (Backend)
- **核心框架**: Spring Boot 3.1.x
- **安全认证**: Spring Security + JWT
- **持久层框架**: MyBatis Plus
- **数据库**: MySQL 8.x
- **AI 对接**: Hutool Http + DeepSeek API

### 前端 (Frontend)
- **核心框架**: Vue 3 (Composition API) + Vite
- **UI 组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router (动态路由拦截注入)
- **图表展示**: ECharts
- **请求库**: Axios

## 目录结构

```text
breeding/
├── docs/                       # 数据库与文档设计
│   ├── schema.sql              # 核心业务建表与初始化数据 SQL
│   └── ai_audit.sql            # AI 审计日志建表 SQL
├── backend/                    # Spring Boot 后端工程
│   ├── pom.xml                 # Maven 依赖
│   └── src/main/java/com/breeding/
│       ├── common/             # 通用配置 (JWT, Security, 统一响应结构等)
│       ├── controller/         # API 控制层
│       ├── entity/             # 数据库映射实体 (严格无前缀)
│       ├── mapper/             # MyBatis Plus 映射接口
│       └── service/            # 业务逻辑层
│           ├── ai/             # AI 数据访问中间层与 DeepSeek 交互服务
│           └── impl/           # 各模块服务实现类
└── frontend/                   # Vue 3 前端工程
    ├── package.json            # npm 依赖
    ├── vite.config.ts          # Vite 配置及跨域代理
    └── src/
        ├── api/                # Axios 拦截器封装
        ├── layout/             # 页面整体布局 (侧边栏菜单等)
        ├── router/             # 静态与动态路由配置
        ├── store/              # Pinia 状态管理
        └── views/              # 各业务模块 Vue 组件
            ├── ai/             # AI 智能分析聊天室
            ├── alert/          # 预警系统
            ├── animal/         # 动物档案管理
            ├── disease/        # 疾病症状上报
            ├── feeding/        # 饲养计划与执行
            ├── inventory/      # 库存批次与过期管理
            └── system/         # 系统与用户管理
```

## 快速启动指南

### 1. 数据库准备
- 安装 MySQL 8.x
- 创建数据库：`CREATE DATABASE breeding_sys DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
- 导入脚本：依次执行 `docs/schema.sql` 与 `docs/ai_audit.sql`
- 默认管理员账号：`admin`，密码：`password`

### 2. 后端服务启动
1. 检查 `backend/src/main/resources/application.yml` 中的 MySQL 账号密码是否与本地一致。
2. 确保在 `application.yml` 中配置了正确的 `deepseek.api-key`。
3. 进入 `backend` 目录，执行：
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   后端服务将在 `http://localhost:8080` 启动。

### 3. 前端服务启动
1. 进入 `frontend` 目录，执行依赖安装：
   ```bash
   npm install
   ```
2. 启动开发服务器：
   ```bash
   npm run dev
   ```
3. 打开浏览器访问 `http://localhost:3000` (或控制台提示的端口)，使用 `admin` / `password` 登录。

## 主要模块演示

- **权限动态菜单**：使用 `admin` 登录可看见【系统管理】；新建一个分配了“饲养员”角色的账号登录后，只能看到【数据看板】和【饲养管理】。
- **AI 权限隔离**：在 AI 助手界面提问“现在有多少库存”，管理员可获得真实库存统计，而无库存权限的角色将会被 AI 拒绝访问。
- **预警触发**：管理员可以在【预警系统】界面点击“手动触发检测”，系统将扫描过期库存并自动生成报警条目。
