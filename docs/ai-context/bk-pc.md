# BK-PC (Bookkeeping PC) 概要设计文档

## 一、项目概述

BK-PC 是基于 Pure-Admin-Thin 框架开发的记账管理系统 PC 端，采用 Vue 3 + Element Plus + TypeScript 技术栈，实现记账数据的全面管理和可视化分析。

---

## 二、技术架构

### 2.1 技术栈与版本

| 类别 | 框架/库 | 版本 |
|------|---------|------|
| 前端框架 | Vue | ^3.5.22 |
| 路由 | Vue Router | ^4.6.3 |
| 状态管理 | Pinia | ^3.0.3 |
| UI 组件库 | Element Plus | ^2.11.5 |
| CSS 框架 | Tailwind CSS | ^4.1.16 |
| HTTP 客户端 | Axios | ^1.12.2 |
| 构建工具 | Vite | ^7.1.12 |
| 类型检查 | TypeScript | ^5.9.3 |
| 图表 | ECharts | ^6.0.0 |
| 表格组件 | @pureadmin/table | ^3.3.0 |
| 日期处理 | dayjs | ^1.11.18 |

### 2.2 目录结构

```
bk-pc/src/
├── api/                              # API 接口定义
│   ├── auth.ts                       # 认证接口
│   ├── incomeExpense.ts              # 收支接口
│   ├── accountBook.ts                # 账本接口
│   ├── classify.ts                   # 分类接口
│   ├── userConfig.ts                 # 用户配置接口
│   └── report.ts                     # 报表接口
├── assets/                           # 静态资源
│   ├── iconfont/                     # iconfont 图标
│   └── images/                       # 图片资源
├── components/                       # 公共组件
│   ├── ReAuth/                       # 权限认证组件
│   ├── ReDialog/                     # 弹窗组件
│   ├── RePureTableBar/               # 表格工具栏
│   ├── BillTypeSelect/               # 收支类型选择
│   ├── ClassifyTree/                 # 分类树选择
│   ├── DateRangePicker/              # 日期范围选择
│   └── AmountInput/                  # 金额输入
├── config/                           # 全局配置
│   └── index.ts                      # 配置项
├── directives/                       # 自定义指令
│   ├── auth/                         # 权限指令
│   └── copy/                         # 复制指令
├── layout/                           # 布局组件
│   ├── components/                   # 布局子组件
│   └── frame.vue                     # iframe 布局
├── router/                           # 路由配置
│   ├── index.ts                      # 路由入口
│   └── modules/                      # 路由模块
│       ├── login.ts                  # 登录
│       ├── dashboard.ts              # 仪表盘
│       ├── bill.ts                   # 账单管理
│       ├── accountBook.ts            # 账本管理
│       ├── classify.ts               # 分类管理
│       ├── report.ts                 # 报表统计
│       └── system.ts                 # 系统管理
├── store/                            # Pinia 状态管理
│   ├── modules/                      # store 模块
│   │   ├── app.ts                    # 应用配置
│   │   ├── user.ts                   # 用户信息
│   │   ├── bill.ts                   # 账单数据
│   │   └── report.ts                 # 报表数据
│   └── types.ts                      # 类型定义
├── types/                            # TypeScript 类型定义
│   ├── incomeExpense.ts              # 收支类型
│   ├── accountBook.ts                # 账本类型
│   ├── classify.ts                   # 分类类型
│   └── report.ts                     # 报表类型
├── utils/                            # 工具函数
│   ├── auth.ts                       # 认证工具
│   ├── http/                         # HTTP 请求封装
│   │   ├── index.ts                  # axios 封装
│   │   └── types.d.ts                # 类型定义
│   └── date.ts                       # 日期工具
├── views/                            # 页面视图
│   ├── login/                        # 登录页
│   │   └── index.vue
│   ├── dashboard/                    # 仪表盘
│   │   └── index.vue
│   ├── bill/                         # 账单管理
│   │   ├── list.vue                  # 账单列表
│   │   ├── add.vue                   # 新增账单
│   │   └── edit.vue                  # 编辑账单
│   ├── accountBook/                  # 账本管理
│   │   ├── list.vue                  # 账本列表
│   │   └── config.vue                # 账本配置
│   ├── classify/                     # 分类管理
│   │   └── index.vue
│   ├── report/                       # 报表统计
│   │   ├── trend.vue                 # 趋势分析
│   │   ├── category.vue              # 分类统计
│   │   └── compare.vue               # 对比分析
│   └── system/                       # 系统管理
│       ├── user.vue                  # 用户管理
│       └── config.vue                # 系统配置
├── App.vue                           # 根组件
└── main.ts                           # 入口文件
```

---

## 三、总体设计

### 3.1 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                      视图层 (Views)                      │
│  Dashboard | Bill | AccountBook | Classify | Report     │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                    状态�� (Store - Pinia)                │
│     UserStore | BillStore | AccountBookStore | ReportStore │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                      API 层 (API)                        │
│  auth | incomeExpense | accountBook | classify | report │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                    HTTP 层 (Axios)                       │
│    请求拦截 | 响应拦截 | Token管理 | 错误处理            │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                   后端服务 (bk-server)                   │
│            Spring Boot + MyBatis-Plus + JWT             │
└─────────────────────────────────────────────────────────┘
```

### 3.2 核心模块

| 模块 | 功能描述 |
|------|---------|
| 首页 | 系统首页，展示收支概览和数据统计 |
| 收支记录 | 收支记录的增删改查、筛选、批量操作 |
| 统计报表 | 趋势分析、分类统计、对比分析 |
| 设置 | 账本设置、支出限额设置、分类设置、备注设置、标签设置 |

---

## 四、页面设计

### 4.0 整体页面布局

采用 Pure-Admin-Thin 单页面模式布局，结构如下：

```
┌─────────────────────────────────────────────────────────────────┐
│  顶部导航栏 (lay-navbar)                                        │
│  ┌─────────┬────────────────────────────────────┬────────────┐ │
│  │ Logo    │ 面包屑                             │ 用户信息   │ │
│  └─────────┴────────────────────────────────────┴────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│         │                                                    │   │
│         │  ┌──────────────────────────────────────────────┐  │   │
│  侧边栏 │  │ 标签页 (lay-tag)                              │  │   │
│  (sidebar)  ├──────────────────────────────────────────────┤  │   │
│         │  │                                              │  │   │
│  菜单   │  │  内容区域 (lay-content)                      │  │   │
│  列表   │  │                                              │  │   │
│         │  │  页面内容                                     │  │   │
│         │  │                                              │  │   │
│         │  └──────────────────────────────────────────────┘  │   │
│         │                                                    │   │
└─────────┴────────────────────────────────────────────────────┴───┘
```

**布局说明**:
- **顶部导航栏 (Navbar)**:
  - 左侧: Logo + 系统名称
  - 中间: 面包屑导航
  - 右侧: 用户头像、退出按钮、设置按钮
- **侧边栏 (Sidebar)**:
  - 宽度: 200px (展开) / 64px (折叠)
  - 菜单图标 + 菜单文字
  - 支持折叠/展开
- **标签页 (Tags)**:
  - 显示已打开的页面标签
  - 支持关闭当前、关闭其他、关闭全部
- **内容区域 (Content)**:
  - 路由页面渲染区域
  - 支持 keep-alive 缓存

### 4.1 菜单结构

```
├── 首页 (/dashboard)
├── 收支记录 (/bill)
├── 统计报表 (/report)
│   ├── 趋势分析 (/report/trend)
│   ├── 分类统计 (/report/category)
│   └── 对比分析 (/report/compare)
└── 设置 (/settings)
    ├── 账本设置 (/settings/account-book)
    ├── 支出限额设置 (/settings/expense-limit)
    ├── 分类设置 (/settings/classify)
    ├── 备注设置 (/settings/remark)
    └── 标签设置 (/settings/tag)
```

### 4.2 登录页

**路径**: `/login`

**功能**:
- 用户名/密码登录
- 验证码输入
- 记住密码
- 注册入口

**布局**:
- 左侧: 背景图 + 欢迎语
- 右侧: 登录表单

### 4.2 仪表盘 (首页)

**路径**: `/dashboard`

**功能**:
- 本月收支概览卡片
- 收支趋势折线图
- 最近记账记录列表
- 快捷操作按钮

### 4.3 收支记录

**路径**: `/bill`

**子页面**:
| 页面 | 路径 | 功能 |
|------|------|------|
| 账单列表 | `/bill` | 列表展示、筛选、导出 |
| 新增账单 | 弹窗 | 快速记账表单 |
| 编辑账单 | 弹窗 | 编辑记账记录 |

**账单列表功能**:
- 分页查询
- 多条件筛选 (日期、分类、账本、金额范围、备注、标签)
- 批量删除
- 导出 Excel

### 4.4 统计报表

**路径**: `/report`

**子页面**:
| 页面 | 路径 | 功能 |
|------|------|------|
| 趋势分析 | `/report/trend` | 日/周/月收支趋势图 |
| 分类统计 | `/report/category` | 分类占比饼图、柱状图 |
| 对比分析 | `/report/compare` | 月度/年度对比 |

### 4.5 设置

**路径**: `/settings`

**子页面**:
| 页面 | 路径 | 功能 |
|------|------|------|
| 账本设置 | `/settings/account-book` | 账本列表、新增/编辑/删除账本、设置默认账本 |
| 支出限额设置 | `/settings/expense-limit` | 月度/年度支出预算设置 |
| 分类设置 | `/settings/classify` | 支出/收入分类维护、图标配置 |
| 备注设置 | `/settings/remark` | 常用备注管理 |
| 标签设置 | `/settings/tag` | 标签管理、颜色配置 |

---

## 五、接口设计

### 5.1 认证接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 登录 | POST | `/auth/login` | 获取 Token |
| 登出 | POST | `/auth/logout` | 清除 Token |
| 验证码 | GET | `/auth/captcha` | 获取验证码图片 |
| 用户信息 | POST | `/auth/user-info` | 获取当前用户信息 |
| 注册 | POST | `/auth/register` | 用户注册 |

### 5.2 收支接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 查询收支 | GET | `/incomeExpense` | 列表查询 |
| 创建收支 | POST | `/incomeExpense` | 新增记录 |
| 更新收支 | PUT | `/incomeExpense` | 编辑记录 |
| 删除收支 | DELETE | `/incomeExpense` | 批量删除 |
| 收支摘要 | GET | `/incomeExpense/summary` | 首页摘要数据 |
| 账单报表 | GET | `/incomeExpense/sumPeriod` | 报表统计数据 |

### 5.3 账本接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 查询账本 | GET | `/accountBook` | 账本列表 |
| 创建账本 | POST | `/accountBook` | 新增账本 |
| 更新账本 | PUT | `/accountBook` | 编辑账本 |
| 删除账本 | DELETE | `/accountBook` | 删除账本 |

### 5.4 分类接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 查询分类 | GET | `/classify` | 分类列表 |
| 创建分类 | POST | `/classify` | 新增分类 |
| 更新分类 | PUT | `/classify` | 编辑分类 |
| 删除分类 | DELETE | `/classify` | 删除分类 |

### 5.5 用户配置接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 查询配置 | GET | `/userConfig` | 用户配置 |
| 更新配置 | PUT | `/userConfig` | 保存配置 |

---

## 六、操作逻辑

### 6.1 登录流程

```
用户输入账号密码 + 验证码
    ↓
调用 auth/login 接口
    ↓
后端验证并返回 token + userInfo
    ↓
存储 token 到 localStorage
    ↓
存储用户信息到 UserStore
    ↓
跳转首页 /dashboard
```

### 6.2 记账流程

```
用户点击"记一笔"或进入账单页面
    ↓
填写金额、类型(收/支)、分类、日期、备注
    ↓
选择账本 (默认当前选中账本)
    ↓
调用 incomeExpense POST 接口
    ↓
成功后刷新列表和首页数据
    ↓
提示"记账成功"
```

### 6.3 查询筛选流程

```
用户设置筛选条件
    ↓
点击查询 / 自动查询 (防抖)
    ↓
调用 incomeExpense GET 接口
    ↓
返回分页数据
    ↓
渲染表格
```

### 6.4 报表统计流程

```
用户选择统计维度 (日期范围、账本)
    ↓
调用 incomeExpense/sumPeriod 接口
    ↓
返回统计数据
    ↓
ECharts 渲染图表
```

---

## 七、数据字段设计

### 7.1 收支记录 (IncomeExpense)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| userId | Long | 用户 ID |
| accountBookId | Long | 账本 ID |
| amount | BigDecimal | 金额 |
| type | Enum (0/1) | 类型: 0-支出, 1-收入 |
| date | LocalDate | 产生日期 |
| remark | String | 备注 |
| mainClassifyId | Long | 主分类 ID |
| subClassifyId | Long | 子分类 ID |
| isCreditCard | Boolean | 是否信用卡消费 |
| isAddRemark | Boolean | 是否加入常用备注 |
| tagCodes | String | 标签 ID 列表 (逗号分隔) |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

**扩展字段 (查询返回)**:
| 字段 | 类型 | 说明 |
|------|------|------|
| mainClassifyName | String | 主分类名称 |
| subClassifyName | String | 子分类名称 |
| mainClassifyImage | String | 主分类图标 |
| subClassifyImage | String | 子分类图标 |

### 7.2 账本 (AccountBook)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| name | String | 账本名称 |
| description | String | 账本描述 |
| userId | Long | 用户 ID |
| image | String | 图标名称 |
| sort | Integer | 排序 |
| isDefault | Boolean | 是否默认账本 |

### 7.3 分类 (Classify)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| name | String | 分类名称 |
| pid | Long | 父分类 ID |
| userId | Long | 用户 ID |
| image | String | 图标名称 |
| sort | Integer | 排序 |
| type | Enum (0/1) | 类型: 0-支出, 1-收入 |
| enable | Boolean | 是否启用 |

### 7.4 首页摘要 (Summary)

| 字段 | 类型 | 说明 |
|------|------|------|
| expenseAmount | BigDecimal | 本月支出总额 |
| incomeAmount | BigDecimal | 本月收入总额 |
| incomeExpenseList | List | 最近记账记录列表 |
| expenseLimit | BigDecimal | 支出预算 |
| expenseSurplus | BigDecimal | 预算剩余 |

### 7.5 账单报表 (BillResult)

| 字段 | 类型 | 说明 |
|------|------|------|
| date | String | 日期 (按维度格式化) |
| expenseAmount | BigDecimal | 支出金额 |
| incomeAmount | BigDecimal | 收入金额 |
| details | List | 明细数据 |

---

## 八、HTTP 请求封装

### 8.1 请求拦截器

- 自动注入 Token 到请求头
- Token 过期自动刷新
- 请求 loading 状态管理

### 8.2 响应拦截器

- 统一处理 success/failure
- 401 自动跳转登录
- 统一错误提示

### 8.3 请求示例

```typescript
// GET 请求
http.get('/incomeExpense', { params: { page: 1, size: 10 } })

// POST 请求
http.post('/incomeExpense', { ...data })

// 带 Token 的请求 (自动携带)
```

---

## 九、权限控制

### 9.1 页面权限

通过路由 meta.roles 控制:

```typescript
{
  path: '/bill',
  meta: { roles: ['admin', 'user'] }
}
```

### 9.2 按钮权限

通过 v-auth 指令控制:

```vue
<el-button v-auth="'bill:add'">新增</el-button>
<el-button v-auth="'bill:delete'">删除</el-button>
```

---

## 十、主题与配置

### 10.1 主题系统

- 支持亮色/暗色主题切换
- 主题色 (Primary Color) 可配置
- 侧边栏折叠/展开状态持久化

### 10.2 多标签页

- 支持多页面同时打开
- 标签页拖拽排序
- 关闭其他/关闭全部功能

---

## 十一、环境配置

| 环境 | 变量 | 默认值 |
|------|------|--------|
| 开发 | VITE_APP_API | http://127.0.0.1:8080 |
| 测试 | VITE_APP_API | http://192.168.2.88:8080 |
| 生产 | VITE_APP_API | https://www.zhiizh.com/bookkeeping/api |

---

## 十二、构建与部署

### 12.1 开发命令

```bash
pnpm dev          # 启动开发服务器
pnpm build        # 生产构建
pnpm build:staging # 测试构建
pnpm preview      # 预览构建结果
```

### 12.2 环境变量文件

| 文件 | 用途 |
|------|------|
| .env | 默认环境 |
| .env.development | 开发环境 |
| .env.staging | 测试环境 |
| .env.production | 生产环境 |