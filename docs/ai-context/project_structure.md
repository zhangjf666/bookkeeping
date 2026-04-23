# 项目结构说明

## 1. 目录结构

```
bk-pc/
├── build/                          # 构建配置
│   ├── plugins.ts                  # Vite 插件配置
│   └── utils.ts                    # 构建工具函数
├── public/                         # 静态资源
├── src/
│   ├── api/                        # API 接口（PC/移动端共享）
│   │   ├── auth.ts                 # 认证接口
│   │   ├── user.ts                 # 用户接口
│   │   ├── incomeExpense.ts        # 收支接口
│   │   ├── classify.ts             # 分类接口
│   │   ├── accountBook.ts          # 账本接口
│   │   ├── userConfig.ts           # 用户配置接口
│   │   ├── userTag.ts              # 标签接口
│   │   ├── remark.ts               # 备注接口
│   │   ├── dashboard.ts            # 仪表盘接口
│   │   └── routes.ts               # 路由接口
│   ├── components/                 # 组件
│   │   ├── pc/                     # PC 端专用组件
│   │   │   └── ...
│   │   ├── mobile/                 # 移动端专用组件
│   │   │   ├── RecordItem.vue      # 账单项
│   │   │   ├── ClassifyPicker.vue  # 分类选择器
│   │   │   ├── AmountInput.vue     # 金额输入
│   │   │   ├── DatePicker.vue      # 日期选择
│   │   │   └── SummaryCard.vue     # 摘要卡片
│   │   └── ReDialog/               # 共享组件
│   ├── layouts/                    # 布局组件
│   │   ├── index.vue               # PC 布局（原 layout）
│   │   └── MobileLayout.vue        # 移动端布局
│   ├── router/                     # 路由配置
│   │   ├── index.ts                # 主路由入口（设备检测）
│   │   ├── pc.ts                   # PC 端路由
│   │   ├── mobile.ts               # 移动端路由
│   │   ├── utils.ts                # 路由工具
│   │   └── modules/                # 路由模块
│   │       ├── home.ts
│   │       ├── bill.ts
│   │       ├── report.ts
│   │       ├── settings.ts
│   │       ├── user.ts
│   │       └── error.ts
│   ├── store/                      # 状态管理（PC/移动端共享）
│   │   ├── index.ts
│   │   ├── types.ts
│   │   ├── utils.ts
│   │   └── modules/
│   │       ├── user.ts             # 用户状态
│   │       ├── bill.ts             # 账单状态
│   │       ├── app.ts
│   │       └── ...
│   ├── styles/                     # 样式
│   │   ├── dark.scss
│   │   ├── element-plus.scss
│   │   ├── reset.scss
│   │   ├── sidebar.scss
│   │   └── mobile.scss             # 移动端样式（新增）
│   ├── types/                      # 类型定义（PC/移动端共享）
│   │   ├── auth.ts
│   │   ├── bill.ts
│   │   ├── classify.ts
│   │   ├── accountBook.ts
│   │   ├── userConfig.ts
│   │   ├── userTag.ts
│   │   ├── remark.ts
│   │   └── dashboard.ts
│   ├── utils/                      # 工具函数（PC/移动端共享）
│   │   ├── http/                   # HTTP 请求封装
│   │   ├── auth.ts                 # Token 管理
│   │   ├── device.ts               # 设备检测（新增）
│   │   ├── format.ts               # 格式化工具
│   │   ├── message.ts              # 消息提示
│   │   └── ...
│   ├── views/                      # 页面
│   │   ├── pc/                     # PC 端页面
│   │   │   ├── dashboard/          # 首页
│   │   │   ├── bill/               # 账单管理
│   │   │   ├── report/             # 报表
│   │   │   ├── settings/           # 设置
│   │   │   │   ├── classify/       # 分类管理
│   │   │   │   ├── tag/            # 标签管理
│   │   │   │   ├── remark/         # 备注管理
│   │   │   │   ├── account-book/   # 账本管理
│   │   │   │   └── common/         # 通用配置
│   │   │   ├── login/              # 登录
│   │   │   ├── register/           # 注册
│   │   │   ├── user/               # 用户中心
│   │   │   └── error/              # 错误页面
│   │   └── mobile/                 # 移动端页面
│   │       ├── home/               # 首页
│   │       │   └── index.vue
│   │       ├── bill/               # 账单列表
│   │       │   └── index.vue
│   │       ├── record/             # 记账
│   │       │   └── index.vue
│   │       ├── report/             # 报表
│   │       │   ├── index.vue
│   │       │   └── detail.vue
│   │       ├── search/             # 搜索
│   │       │   └── index.vue
│   │       ├── user/               # 个人中心
│   │       │   └── index.vue
│   │       ├── login/              # 登录
│   │       │   └── index.vue
│   │       └── register/           # 注册
│   │           └── index.vue
│   ├── App.vue
│   ├── main.ts
│   └── env.d.ts
├── types/                          # 全局类型
├── vite.config.ts                  # Vite 配置
├── tsconfig.json                   # TypeScript 配置
├── package.json
└── pnpm-lock.yaml
```

## 2. 共享代码说明

以下代码 PC 端和移动端共享，无需重复开发：

| 目录 | 说明 |
|------|------|
| `src/api/` | 所有 API 接口定义 |
| `src/types/` | 所有 TypeScript 类型定义 |
| `src/store/` | Pinia 状态管理 |
| `src/utils/http/` | Axios 请求封装 |
| `src/utils/auth.ts` | Token 管理工具 |
| `src/utils/format.ts` | 格式化工具 |

## 3. 独立代码说明

| 目录 | PC 端 | 移动端 |
|------|-------|--------|
| 页面 | `views/pc/` | `views/mobile/` |
| 组件 | `components/pc/` | `components/mobile/` |
| 布局 | `layouts/index.vue` | `layouts/MobileLayout.vue` |
| 路由 | `router/pc.ts` | `router/mobile.ts` |
| 样式 | Element Plus | Vant 4 |
