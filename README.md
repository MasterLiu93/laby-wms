<p align="center">
 <img src="https://img.shields.io/badge/Spring%20Boot-3.4.5-blue.svg" alt="Spring Boot">
 <img src="https://img.shields.io/badge/Vue-3.5-blue.svg" alt="Vue">
 <img src="https://img.shields.io/badge/Element%20Plus-2.11-blue.svg" alt="Element Plus">
 <img src="https://img.shields.io/badge/TypeScript-5.3-blue.svg" alt="TypeScript">
 <img src="https://img.shields.io/github/license/MasterLiu93/laby-wms" alt="License" />
 <img src="https://img.shields.io/github/stars/MasterLiu93/laby-wms?style=social" alt="GitHub stars" />
</p>

# 🏭 WMS 仓储管理系统

基于 **芋道框架（yudao-boot-mini）** 开发的现代化仓储管理系统（WMS - Warehouse Management System），提供完整的仓库管理、库存管理、出入库管理等核心功能。

> 📦 **GitHub 仓库**：[https://github.com/MasterLiu93/laby-wms](https://github.com/MasterLiu93/laby-wms)

## 📋 项目简介

本项目是基于芋道框架精简版（yudao-boot-mini）定制开发的 WMS 仓储管理系统，专注于仓储物流业务场景，提供高效、可靠的仓储管理解决方案。

### 核心特性

- ✅ **完整的仓储业务流程**：覆盖入库、出库、拣货、盘点、移库等全流程
- ✅ **精细化库存管理**：实时库存、库存流水、库存快照、库存预警
- ✅ **智能拣货系统**：支持拣货波次、拣货任务分配、异常处理
- ✅ **多维度报表分析**：出入库统计、库存报表、趋势分析
- ✅ **iOS 风格主题**：前端采用 iOS 设计语言，界面简洁美观
- ✅ **现代化技术栈**：Spring Boot 3.4.5 + Vue 3.5 + Element Plus 2.11

## 🚀 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.4.5 | 应用开发框架 |
| JDK | 17 | Java 开发工具包 |
| MySQL | 5.7+ / 8.0+ | 关系型数据库 |
| MyBatis Plus | 3.5.10 | ORM 框架 |
| Redis | 5.0+ / 6.0+ / 7.0+ | 缓存数据库 |
| Redisson | 3.41.0 | Redis 客户端 |
| Spring Security | 6.3.1 | 安全框架 |
| Springdoc | 2.8.3 | API 文档（Swagger） |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.12 | 渐进式 JavaScript 框架 |
| Element Plus | 2.11.1 | 基于 Vue 3 的组件库 |
| TypeScript | 5.3.3 | JavaScript 的超集 |
| Vite | 5.1.4 | 下一代前端构建工具 |
| Pinia | 2.1.7 | Vue 状态管理 |
| Vue Router | 4.4.5 | Vue 路由管理器 |

### 前端特色

- 🎨 **iOS 风格主题**：深度定制 Element Plus 组件，采用 iOS 设计语言
- 🔤 **Monospace 字体**：统一使用等宽字体，提升代码和数据的可读性
- 📱 **响应式设计**：完美适配桌面端和移动端
- 🌓 **暗色模式支持**：支持亮色/暗色主题切换

## 📦 项目结构

```
laby-service/
├── laby-dependencies/          # Maven 依赖版本管理
├── laby-framework/            # Java 框架拓展
│   ├── laby-common/          # 通用工具类
│   ├── laby-spring-boot-starter-*/  # 各种 Starter
├── laby-server/               # 服务启动模块
├── laby-module-system/        # 系统功能模块（用户、角色、菜单等）
├── laby-module-infra/         # 基础设施模块（代码生成、文件服务等）
├── laby-module-wms/           # WMS 仓储管理模块 ⭐
└── laby-ui/                   # 前端项目
    ├── src/
    │   ├── views/wms/        # WMS 前端页面
    │   ├── styles/theme/ios/ # iOS 主题样式
    │   └── ...
```

## 🎯 WMS 功能模块

### 1. 基础数据管理

#### 仓库管理
- **仓库信息**：创建、编辑、删除仓库，支持多仓库管理
- **库区管理**：仓库下划分库区，支持库区层级管理
- **库位管理**：库区下管理具体库位，支持库位编码、状态管理

#### 商品管理
- **商品分类**：树形结构管理商品分类
- **商品信息**：商品基础信息、规格、单位等管理
- **商品查询**：支持多条件查询、批量操作

#### 合作伙伴管理
- **供应商管理**：供应商信息、资质管理
- **客户管理**：客户信息、联系方式管理
- **承运商管理**：物流承运商信息管理

### 2. 入库管理

- **入库单创建**：支持手动创建、批量导入
- **入库审核**：入库单审核流程
- **收货管理**：
  - 开始收货
  - 确认收货（单个明细）
  - 完成收货
- **入库取消**：支持取消未完成的入库单
- **入库查询**：多条件查询、状态跟踪

### 3. 出库管理

- **出库单创建**：支持手动创建、关联订单
- **出库审核**：出库单审核流程
- **拣货管理**：
  - 开始拣货
  - 完成拣货
- **发货管理**：完成发货操作
- **出库取消**：支持取消未完成的出库单
- **出库查询**：多条件查询、状态跟踪

### 4. 拣货管理

#### 拣货波次
- **波次创建**：手动创建或自动生成拣货波次
- **波次分配**：分配拣货员
- **波次执行**：开始拣货、完成拣货波次
- **波次取消**：取消未完成的波次

#### 拣货任务
- **任务分配**：将拣货任务分配给拣货员
- **任务执行**：执行拣货操作
- **任务完成**：完成拣货任务
- **异常处理**：标记拣货异常，支持异常类型分类

### 5. 库存管理

#### 库存信息
- **实时库存**：查看各仓库、库位的实时库存
- **库存调整**：支持库存增减操作
- **库存查询**：多维度查询库存信息

#### 库存流水
- **流水记录**：记录所有库存变动明细
- **流水查询**：按时间、商品、仓库等条件查询
- **流水类型**：入库、出库、盘点、移库等类型

#### 库存快照
- **快照记录**：定期记录库存快照
- **趋势分析**：查看库存变化趋势
- **历史查询**：查询历史快照数据

#### 库存预警
- **低库存预警**：库存低于安全库存时预警
- **过期预警**：商品即将过期时预警
- **预警查询**：查看所有预警信息

### 6. 盘点管理

#### 盘点计划
- **计划创建**：创建盘点计划
- **计划审核**：审核盘点计划
- **生成盘点单**：根据计划生成盘点单
- **计划取消**：取消未执行的盘点计划

#### 盘点单
- **盘点执行**：提交盘点数据
- **盘点复核**：复核盘点结果
- **库存调整**：根据盘点结果调整库存
- **盘点查询**：查询盘点历史记录

### 7. 移库管理

- **移库单创建**：创建库位间移库单
- **移库执行**：执行移库操作
- **移库完成**：完成移库并更新库存
- **移库取消**：取消未完成的移库单
- **移库查询**：查询移库历史

### 8. 报表分析

#### 出入库报表
- **出入库统计**：按时间、仓库、商品等维度统计
- **出入库汇总**：汇总出入库数据
- **数据导出**：支持 Excel 导出

#### 库存报表
- **库存统计**：按仓库、商品等维度统计库存
- **库存汇总**：汇总库存数据
- **数据导出**：支持 Excel 导出

## 🎨 前端特色

### iOS 风格主题

本项目前端采用深度定制的 iOS 风格主题，提供：

- **iOS 标准色系**：
  - 主色调：`#007AFF`（iOS 蓝色）
  - 成功色：`#34C759`（绿色）
  - 警告色：`#FF9500`（橙色）
  - 危险色：`#FF3B30`（红色）

- **iOS 设计语言**：
  - 圆角设计：8px（小）、12px（中）、16px（大）
  - 字体系统：Monospace 等宽字体
  - 阴影效果：柔和的阴影层次
  - 动画过渡：流畅的 iOS 标准动画

- **组件定制**：
  - Button、Input、Select、Radio、Checkbox
  - Dialog、Message、Notification
  - Table、Card、Tag、Menu、Tabs
  - Form、Switch、Slider

### 主题文件结构

```
laby-ui/src/styles/theme/ios/
├── variables.scss        # SCSS 变量定义
├── css-variables.scss    # CSS 变量定义
├── index.scss           # 组件样式定制
├── types.ts             # TypeScript 类型定义
└── utils.ts             # 主题工具函数
```

## 🛠️ 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 16+
- pnpm 8.6+
- MySQL 5.7+ / 8.0+
- Redis 5.0+

### 后端启动

1. **导入数据库**
   ```bash
   # 执行 SQL 脚本
   sql/mysql/laby.sql
   ```

2. **修改配置**
   ```yaml
   # laby-server/src/main/resources/application.yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/laby
       username: root
       password: your_password
   ```

3. **启动服务**
   ```bash
   # 运行 laby-server 模块的 Application 主类
   # 或使用 Maven
   mvn spring-boot:run -pl laby-server
   ```

### 前端启动

1. **安装依赖**
   ```bash
   cd laby-ui
   pnpm install
   ```

2. **配置环境变量**
   ```bash
   # 修改 .env.local 或 .env.dev
   VITE_BASE_URL=http://localhost:48080
   ```

3. **启动开发服务器**
   ```bash
   pnpm dev
   ```

4. **访问系统**
   ```
   http://localhost:5173
   ```

## 📚 功能演示

### 仓库管理
- 创建仓库 → 创建库区 → 创建库位
- 支持多层级仓库结构管理

### 入库流程
1. 创建入库单
2. 审核入库单
3. 开始收货
4. 确认收货明细
5. 完成收货（自动更新库存）

### 出库流程
1. 创建出库单
2. 审核出库单
3. 生成拣货波次
4. 分配拣货任务
5. 执行拣货
6. 完成发货（自动扣减库存）

### 盘点流程
1. 创建盘点计划
2. 审核盘点计划
3. 生成盘点单
4. 提交盘点数据
5. 复核盘点结果
6. 调整库存差异

## 🔧 开发规范

### 代码规范
- 遵循《阿里巴巴 Java 开发手册》
- 使用 MapStruct 进行对象转换
- 使用 Lombok 简化代码
- 完整的单元测试覆盖

### 前端规范
- 使用 TypeScript 严格模式
- 遵循 Vue 3 Composition API 规范
- 使用 ESLint + Prettier 代码格式化
- 组件采用 BEM 命名规范

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议，个人与企业可 100% 免费使用。

## 🙏 致谢

- 基于 [芋道框架（yudao-boot-mini）](https://gitee.com/labycode/laby-boot-mini) 开发
- 前端 UI 基于 [Element Plus](https://element-plus.org/)
- 感谢所有贡献者的支持

## 📞 联系方式

如有问题或建议，欢迎提交 [Issue](https://github.com/MasterLiu93/laby-wms/issues) 或 [Pull Request](https://github.com/MasterLiu93/laby-wms/pulls)。

## 🔗 相关链接

- **GitHub 仓库**：[https://github.com/MasterLiu93/laby-wms](https://github.com/MasterLiu93/laby-wms)
- **基于框架**：[芋道框架（yudao-boot-mini）](https://gitee.com/labycode/laby-boot-mini)
- **前端 UI**：[Element Plus](https://element-plus.org/)

---

**⭐ 如果这个项目对你有帮助，欢迎 Star 支持！**
