# H5 移动端整体设计文档

## 1. 项目概述

### 1.1 项目定位
记账本 H5 移动端应用，面向个人用户的日常收支记录与管理工具。支持多账本、分类管理、统计报表等功能。

### 1.2 设计原则
- **简洁高效**：核心功能一步直达，减少操作层级
- **数据可视化**：收支情况一目了然
- **响应迅速**：优化加载体验，支持离线缓存
- **国际化支持**：完整 i18n 多语言支持

---

## 2. 技术选型

### 2.1 核心框架
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5+ | 前端框架，Composition API |
| TypeScript | 5.x | 类型安全 |
| Vite | 7.x | 构建工具 |
| Vue Router | 4.x | 路由管理 |
| Pinia | 3.x | 状态管理 |

### 2.2 UI 组件库
| 技术 | 版本 | 说明 |
|------|------|------|
| Vant | 4.x | 移动端 UI 组件库 |
| @vant/auto-import-resolver | - | 组件自动导入 |

### 2.3 工具库
| 技术 | 说明 |
|------|------|
| axios | HTTP 请求 |
| dayjs | 日期处理 |
| vue-i18n | 国际化 |
| @vueuse/core | 组合式工具函数 |
| echarts / vue-echarts | 图表可视化 |

### 2.4 样式方案
- **CSS 框架**：Tailwind CSS 4.x（原子化 CSS）
- **预处理器**：Sass
- **移动端适配**：viewport + rem/vw 方案

---

## 3. 主题设计

### 3.1 主题色
```scss
// 主色调
$color-primary: #d83d34;      // 红色 - 支出/强调
$color-secondary: #00a151;    // 绿色 - 收入/成功

// 功能色
$color-danger: #ee0a24;       // 危险/删除
$color-warning: #ff976a;      // 警告
$color-success: #00a151;      // 成功
$color-info: #1989fa;         // 信息

// 中性色
$color-text-primary: #323233;    // 主要文字
$color-text-secondary: #646566;  // 次要文字
$color-text-placeholder: #c8c9cc; // 占位文字
$color-border: #ebedf0;          // 边框
$color-background: #f7f8fa;      // 页面背景
$color-card: #ffffff;            // 卡片背景
```

### 3.2 颜色语义
| 颜色 | 用途 | 场景示例 |
|------|------|----------|
| `#d83d34` | 支出相关 | 支出金额、支出分类图标、支出趋势线 |
| `#00a151` | 收入相关 | 收入金额、收入分类图标、收入趋势线 |
| `#d83d34` | 主要操作 | 主按钮、选中状态、TabBar 激活 |

### 3.3 字体规范
```scss
$font-size-xs: 20rpx;    // 10px - 辅助信息
$font-size-sm: 24rpx;    // 12px - 次要内容
$font-size-md: 28rpx;    // 14px - 正文
$font-size-lg: 32rpx;    // 16px - 标题
$font-size-xl: 40rpx;    // 20px - 大标题
$font-size-xxl: 56rpx;   // 28px - 金额数字
```

---

## 4. 布局架构

### 4.1 页面布局模式

采用 **动态 Header + Scrollable Content + Fixed TabBar** 布局，根据页面类型决定是否显示顶部导航栏：

#### 4.1.1 一级页面（无顶部导航栏）

首页、账单、报表、我的等底部有 TabBar 的页面，不需要顶部导航栏：

```
┌─────────────────────────────────────┐
│                                     │
│  Scrollable Content                 │  ← 可滚动内容区域
│  (scroll-view / overflow-y: auto)   │
│                                     │
│                                     │
├─────────────────────────────────────┤
│  Fixed TabBar                       │  ← position: fixed, z-index: 9998
│  高度: 50px + 安全区域               │
└─────────────────────────────────────┘
```

**适用页面**：
- 首页 (`/dashboard`)
- 账单 (`/bill-list`)
- 报表 (`/report/bill-report`)
- 我的 (`/user/profile`)

#### 4.1.2 二级页面（有顶部导航栏）

记账、搜索、设置等二级页面，需要顶部导航栏提供返回功能：

```
┌─────────────────────────────────────┐
│  Fixed Header (NavBar)              │  ← position: fixed, z-index: 9999
│  高度: 46px + 状态栏                 │
│  包含返回按钮和页面标题               │
├─────────────────────────────────────┤
│                                     │
│  Scrollable Content                 │  ← 可滚动内容区域
│  (scroll-view / overflow-y: auto)   │
│                                     │
│                                     │
└─────────────────────────────────────┘
```

**适用页面**：
- 记账 (`/record` 或 `/record/:id`) - 返回上一级页面
- 搜索 (`/search`) - 返回上一级页面
- 账本管理、分类管理、标签管理、备注管理、设置等

#### 4.1.3 特殊页面（无导航栏无 TabBar）

登录、注册等独立页面，既无顶部导航栏也无底部 TabBar：

```
┌─────────────────────────────────────┐
│                                     │
│                                     │
│  Full Screen Content                │  ← 全屏内容区域
│                                     │
│                                     │
│                                     │
└─────────────────────────────────────┘
```

**适用页面**：
- 登录 (`/login`)
- 注册 (`/register`)

### 4.2 导航栏返回逻辑

二级页面的顶部导航栏返回按钮，返回到来源页面：

| 来源页面 | 跳转二级页面 | 点击返回 |
|----------|--------------|----------|
| 首页 | 记账页 | 返回首页 |
| 账单 | 记账页 | 返回账单 |
| 首页 | 搜索页 | 返回首页 |
| 账单 | 搜索页 | 返回账单 |
| 我的 | 设置页 | 返回我的 |

实现方式：使用 `router.back()` 或记录来源页面的路由信息。

### 4.2 布局组件结构
```
src/mobile-layout/
├── MobileLayout.vue          # 主布局（含 TabBar）
├── components/
│   ├── MobileHeader.vue      # 顶部导航栏
│   ├── MobileTabBar.vue      # 底部标签栏
│   └── MobilePullRefresh.vue # 下拉刷新容器
└── styles/
    ├── variables.scss        # 主题变量
    ├── mixins.scss           # 混入样式
    └── index.scss            # 入口样式
```

### 4.3 响应式断点
```scss
// 移动端优先，主要适配宽度
$breakpoint-xs: 320px;   // 小屏手机
$breakpoint-sm: 375px;   // 标准手机
$breakpoint-md: 414px;   // 大屏手机
$breakpoint-lg: 768px;   // 平板
```

---

## 5. 页面规划

### 5.1 页面结构图
```
├── 首页 (/dashboard 或 /)
│   ├── 本月概览卡片
│   ├── 快捷记账入口
│   └── 近期账单列表
│
├── 账单 (/bill-list)
│   ├── 日期筛选器
│   ├── 收支统计
│   └── 账单列表（按日分组）
│
├── 记账 (/record 或 /record/:id)
│   ├── 收入/支出切换
│   ├── 金额输入
│   ├── 分类选择
│   ├── 日期选择
│   ├── 备注输入
│   └── 保存/更新
│
├── 报表 (/report/bill-report)
│   ├── 时间维度切换（周/月/年）
│   ├── 收支趋势图
│   ├── 分类占比图
│   └── 明细列表
│
├── 搜索 (/search)
│   ├── 搜索条件（金额/分类/日期/备注）
│   └── 搜索结果列表
│
├── 我的 (/user/profile)
│   ├── 用户信息
│   ├── 账本管理
│   ├── 分类管理
│   ├── 标签管理
│   ├── 备注管理
│   ├── 系统设置
│   └── 关于/帮助
│
├── 登录 (/login)
│   └── 账号密码/验证码登录
│
└── 注册 (/register)
    └── 账号注册
```

> **注意**：以上路由路径与 PC 端保持一致，系统根据访问设备自动加载对应端（PC/Mobile）的页面组件，URL 保持不变。

### 5.2 页面功能详情

#### 5.2.1 首页 (Home)
| 模块 | 功能 | 交互 |
|------|------|------|
| 概览卡片 | 显示本月收入/支出/结余 | 下拉刷新更新数据 |
| 支出限额 | 显示剩余限额（可选） | 点击编辑限额 |
| 记账按钮 | 快速记账入口 | 跳转记账页 |
| 近期账单 | 最近3天账单列表 | 点击查看详情，下拉刷新 |

#### 5.2.2 账单页 (Bill)
| 模块 | 功能 | 交互 |
|------|------|------|
| 日期筛选 | 月/年/自定义切换 | 弹窗选择日期范围 |
| 收支统计 | 显示选中期间收支 | 自动计算 |
| 账单列表 | 按日期分组的账单 | 左滑删除/编辑 |
| 分类筛选 | 按分类过滤账单 | 弹窗多选 |

#### 5.2.3 记账页 (Record)
| 模块 | 功能 | 交互 |
|------|------|------|
| 类型切换 | 收入/支出切换 | Tab 切换 |
| 金额输入 | 数字键盘输入 | 自定义键盘 |
| 分类选择 | 图标网格选择 | 弹窗选择 |
| 日期选择 | 选择账单日期 | 日期选择器 |
| 备注输入 | 添加备注信息 | 文本输入 |
| 标签选择 | 添加标签（可选） | 多选标签 |
| 保存 | 保存账单记录 | 提交并返回 |

#### 5.2.4 报表页 (Report)
| 模块 | 功能 | 交互 |
|------|------|------|
| 时间维度 | 周/月/年切换 | Tab 切换 |
| 趋势图 | 折线/柱状图展示 | 点击查看详情 |
| 分类占比 | 饼图/环形图 | 点击分类查看明细 |
| 排行榜 | 收支分类排行 | 列表展示 |

#### 5.2.5 搜索页 (Search)
| 模块 | 功能 | 交互 |
|------|------|------|
| 关键词搜索 | 按备注关键词搜索 | 输入框 |
| 金额范围 | 按金额区间筛选 | 输入范围 |
| 分类筛选 | 按分类筛选 | 多选 |
| 日期范围 | 按日期筛选 | 日期选择 |
| 结果列表 | 显示匹配账单 | 点击查看详情 |

#### 5.2.6 我的页 (User)
| 模块 | 功能 | 交互 |
|------|------|------|
| 用户信息 | 头像、昵称、账号 | 点击编辑 |
| 账本管理 | 创建/编辑/切换账本 | 列表管理 |
| 分类管理 | 自定义收支分类 | 增删改 |
| 标签管理 | 管理账单标签 | 增删改 |
| 备注管理 | 常用备注模板 | 增删改 |
| 系统设置 | 主题、语言、通知等 | 开关/选择 |
| 语言切换 | 中/英文切换 | i18n |
| 关于 | 版本信息、帮助文档 | 静态页 |

---

## 6. 项目结构

### 6.1 目录结构
```
bk-pc/src/
├── api/                          # API 接口（共享）
│   ├── auth.ts                   # 认证
│   ├── user.ts                   # 用户
│   ├── incomeExpense.ts          # 收支
│   ├── classify.ts               # 分类
│   ├── accountBook.ts            # 账本
│   ├── userConfig.ts             # 用户配置
│   ├── userTag.ts                # 标签
│   └── remark.ts                 # 备注
│
├── types/                        # 类型定义（共享）
│   ├── auth.ts
│   ├── bill.ts
│   ├── classify.ts
│   ├── accountBook.ts
│   ├── userConfig.ts
│   ├── userTag.ts
│   └── remark.ts
│
├── store/                        # 状态管理（共享）
│   ├── index.ts
│   └── modules/
│       ├── user.ts
│       ├── bill.ts
│       ├── classify.ts
│       └── app.ts
│
├── utils/                        # 工具函数（共享）
│   ├── http/                     # HTTP 封装
│   ├── auth.ts                   # Token 管理
│   ├── format.ts                 # 格式化
│   └── device.ts                 # 设备检测
│
├── mobile-layout/                # 移动端布局
│   ├── MobileLayout.vue
│   ├── components/
│   └── styles/
│
├── components/                   # 共享组件
│   ├── mobile/                   # 移动端组件
│   │   ├── RecordItem.vue        # 账单项
│   │   ├── ClassifyPicker.vue    # 分类选择器
│   │   ├── AmountInput.vue       # 金额输入
│   │   ├── DatePicker.vue        # 日期选择
│   │   ├── SummaryCard.vue       # 摘要卡片
│   │   ├── ChartCard.vue         # 图表卡片
│   │   ├── FilterBar.vue         # 筛选栏
│   │   └── EmptyState.vue        # 空状态
│   └── ReDialog/                 # 弹窗组件
│
├── views/                        # 页面
│   └── mobile/                   # 移动端页面
│       ├── home/                 # 首页
│       │   └── index.vue
│       ├── bill/                 # 账单
│       │   └── index.vue
│       ├── record/               # 记账
│       │   └── index.vue
│       ├── report/               # 报表
│       │   ├── index.vue
│       │   └── detail.vue
│       ├── search/               # 搜索
│       │   └── index.vue
│       ├── user/                 # 我的
│       │   ├── index.vue
│       │   ├── AccountBook.vue   # 账本管理
│       │   ├── Classify.vue      # 分类管理
│       │   ├── Tag.vue           # 标签管理
│       │   ├── Remark.vue        # 备注管理
│       │   └── Settings.vue      # 设置
│       ├── login/                # 登录
│       │   └── index.vue
│       └── register/             # 注册
│           └── index.vue
│
├── router/                       # 路由
│   ├── index.ts                  # 主路由（设备感知，统一路径）
│   ├── utils.ts                  # 路由工具
│   └── modules/                  # 路由模块
│       ├── home.ts
│       ├── bill.ts
│       ├── record.ts
│       ├── report.ts
│       ├── user.ts
│       └── auth.ts
│
├── locales/                      # 国际化（项目根目录）
│   ├── zh-CN.yaml                # PC 端中文（现有）
│   ├── en.yaml                   # PC 端英文（现有）
│   └── mobile/                   # 移动端文案（新增）
│       ├── zh-CN.yaml            # 移动端中文
│       └── en.yaml               # 移动端英文
│
└── styles/                       # 样式
    ├── mobile/                   # 移动端样式
    │   ├── variables.scss        # 变量
    │   ├── mixins.scss           # 混入
    │   ├── reset.scss            # 重置
    │   └── index.scss            # 入口
    └── ...
```

---

## 7. 接口调用规范

### 7.1 HTTP 封装
复用现有 `src/utils/http/` 封装，移动端无需额外处理。

### 7.2 API 调用示例
```typescript
// views/mobile/home/index.vue
import { querySummary } from '@/api/incomeExpense';
import { useUserStore } from '@/store/modules/user';

const userStore = useUserStore();

const fetchSummary = async () => {
  const data = await querySummary({ userId: userStore.userId });
  // ...
};
```

### 7.3 错误处理
```typescript
// 统一错误处理（已在 http 封装中处理）
// 页面只需处理业务逻辑
try {
  await someApi(params);
} catch (error) {
  // 错误已由全局拦截器处理并提示
  // 此处可做额外处理
}
```

### 7.4 Loading 状态
```typescript
// 使用 Vant 的 Toast
import { showLoadingToast, closeToast } from 'vant';

const fetchData = async () => {
  showLoadingToast({ message: '加载中...', forbidClick: true });
  try {
    const data = await someApi(params);
    // 处理数据
  } finally {
    closeToast();
  }
};
```

---

## 8. 国际化 (i18n)

### 8.1 现有结构分析
PC 端已有国际化配置，使用 YAML 格式：
```
bk-pc/locales/
├── zh-CN.yaml      # 中文文案
└── en.yaml         # 英文文案
```

现有配置通过 `import.meta.glob` 动态加载，语言代码为 `zh` 和 `en`。

### 8.2 设计原则
**移动端国际化独立于 PC 端，不影响现有 PC 端代码**：
- PC 端现有的 YAML 文件保持不变，继续使用
- 移动端新增独立的 YAML 文件，使用 `mobile` 命名空间
- 最终合并到同一个 i18n 实例中，互不干扰

### 8.3 新增文件结构
```
bk-pc/locales/
├── zh-CN.yaml              # PC 端中文（现有，保持不变）
├── en.yaml                 # PC 端英文（现有，保持不变）
├── mobile/                 # 移动端文案（新增目录）
│   ├── zh-CN.yaml          # 移动端中文
│   └── en.yaml             # 移动端英文
```

### 8.4 配置修改
```typescript
// plugins/i18n.ts
import { type I18n, createI18n } from "vue-i18n";
import { storageLocal } from "@pureadmin/utils";
import { responsiveStorageNameNamespace } from "@/config";

// element-plus 国际化
import enLocale from "element-plus/es/locale/lang/en";
import zhLocale from "element-plus/es/locale/lang/zh-cn";

// 加载 PC 端 YAML 文件（现有逻辑）
const loadPCLocales = (prefix = "zh-CN") => {
  const cache = Object.fromEntries(
    Object.entries(
      import.meta.glob("../locales/*.y(a)?ml", { eager: true })
    ).map(([key, value]: any) => {
      const matched = key.match(/([A-Za-z0-9-_]+)\./i)[1];
      return [matched, value.default];
    })
  );
  return cache[prefix];
};

// 加载移动端 YAML 文件（新增）
const loadMobileLocales = (prefix = "zh-CN") => {
  const cache = Object.fromEntries(
    Object.entries(
      import.meta.glob("../locales/mobile/*.y(a)?ml", { eager: true })
    ).map(([key, value]: any) => {
      const matched = key.match(/([A-Za-z0-9-_]+)\./i)[1];
      return [matched, value.default];
    })
  );
  return cache[prefix] || {};
};

// 合并 PC 和 Mobile 文案
export const localesConfigs = {
  zh: {
    ...loadPCLocales("zh-CN"),      // PC 端文案
    mobile: loadMobileLocales("zh-CN"), // 移动端文案（独立命名空间）
    ...zhLocale
  },
  en: {
    ...loadPCLocales("en"),         // PC 端文案
    mobile: loadMobileLocales("en"),    // 移动端文案（独立命名空间）
    ...enLocale
  }
};

export const i18n: I18n = createI18n({
  legacy: false,
  locale:
    storageLocal().getItem<StorageConfigs>(
      `${responsiveStorageNameNamespace()}locale`
    )?.locale ?? "zh",
  fallbackLocale: "en",
  messages: localesConfigs
});

export function useI18n(app: App) {
  app.use(i18n);
}
```

### 8.5 移动端 YAML 文件示例
```yaml
# locales/mobile/zh-CN.yaml

# 通用文案
common:
  confirm: 确认
  cancel: 取消
  save: 保存
  delete: 删除
  edit: 编辑
  add: 添加
  search: 搜索
  loading: 加载中...
  noData: 暂无数据
  success: 操作成功
  failed: 操作失败
  pullDownRefresh: 下拉刷新
  releaseRefresh: 释放刷新
  refreshing: 刷新中...

# 底部导航栏
tabBar:
  home: 首页
  bill: 账单
  report: 报表
  user: 我的

# 首页
home:
  title: 首页
  monthExpense: 本月支出
  monthIncome: 本月收入
  monthBalance: 本月结余
  recentBills: 近三日账单
  quickRecord: 记一笔
  expenseLimit: 支出限额
  surplus: 剩余限额
  records: "{count} 笔"

# 账单页
bill:
  title: 账单
  monthBill: 月账单
  yearBill: 年账单
  custom: 自定义
  filter: 筛选
  allExpense: 全部支出
  allIncome: 全部收入
  sortByTime: 按时间
  sortByAmount: 按金额

# 记账页
record:
  title: 记账
  editTitle: 编辑记录
  expense: 支出
  income: 收入
  amount: 金额
  classify: 分类
  date: 日期
  remark: 备注
  tag: 标签
  saveSuccess: 保存成功

# 报表页
report:
  title: 报表
  week: 周
  month: 月
  year: 年
  trend: 趋势
  classify: 分类占比
  ranking: 排行榜

# 搜索页
search:
  title: 搜索
  placeholder: 搜索备注...
  amountRange: 金额范围
  dateRange: 日期范围
  classifyFilter: 分类筛选
  noResult: 未找到相关记录

# 我的页
user:
  title: 我的
  profile: 个人信息
  accountBook: 账本管理
  classifyManage: 分类管理
  tagManage: 标签管理
  remarkManage: 备注管理
  settings: 设置
  about: 关于
  language: 语言
  logout: 退出登录
```

```yaml
# locales/mobile/en.yaml

common:
  confirm: Confirm
  cancel: Cancel
  save: Save
  delete: Delete
  edit: Edit
  add: Add
  search: Search
  loading: Loading...
  noData: No Data
  success: Success
  failed: Failed
  pullDownRefresh: Pull to refresh
  releaseRefresh: Release to refresh
  refreshing: Refreshing...

tabBar:
  home: Home
  bill: Bills
  report: Report
  user: Me

home:
  title: Home
  monthExpense: Monthly Expense
  monthIncome: Monthly Income
  monthBalance: Balance
  recentBills: Recent Bills
  quickRecord: Add Record
  expenseLimit: Expense Limit
  surplus: Remaining
  records: "{count} records"

# ... 其他页面类似
```

### 8.6 使用方式

**PC 端（保持不变）**：
```vue
<template>
  <!-- PC 端继续使用现有方式 -->
  <div>{{ $t('menus.pureHome') }}</div>
  <el-button>{{ $t('buttons.pureConfirm') }}</el-button>
</template>

<script setup lang="ts">
import { transformI18n } from '@/plugins/i18n';
// 现有代码无需修改
</script>
```

**移动端（使用 mobile 命名空间）**：
```vue
<template>
  <!-- 移动端使用 mobile 命名空间 -->
  <div>{{ $t('mobile.home.title') }}</div>
  <van-button>{{ $t('mobile.common.confirm') }}</van-button>
  <van-tabbar-item>{{ $t('mobile.tabBar.home') }}</van-tabbar-item>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
// 移动端文案
const title = t('mobile.home.title');
const confirmText = t('mobile.common.confirm');
</script>
```

### 8.7 文案对比示例
| 场景 | PC 端文案 (`$t('xxx')`) | 移动端文案 (`$t('mobile.xxx')`) |
|------|-------------------------|--------------------------------|
| 首页标题 | `menus.pureHome` → "首页" | `mobile.home.title` → "首页" |
| 确认按钮 | `buttons.pureConfirm` → "确认" | `mobile.common.confirm` → "确认" |
| 账单操作 | "新增账单" | `mobile.home.quickRecord` → "记一笔" |
| 空状态 | "暂无数据" | `mobile.common.noData` → "暂无账单" |

### 8.8 语言切换
语言切换逻辑保持不变，PC 和 Mobile 共用同一语言设置：
```typescript
// 切换语言
const switchLocale = (locale: 'zh' | 'en') => {
  i18n.global.locale.value = locale;
  storageLocal().setItem(`${responsiveStorageNameNamespace()}locale`, { locale });
};
```

### 8.9 兼容性总结
| 项目 | PC 端 | 移动端 |
|------|-------|--------|
| 文件位置 | `locales/zh-CN.yaml` | `locales/mobile/zh-CN.yaml` |
| 命名空间 | 根级别 | `mobile.xxx` |
| 使用方式 | `$t('xxx')` | `$t('mobile.xxx')` |
| 是否需要修改 | **否** | 新增文件 |

**结论**：此方案完全兼容现有 PC 端代码，无需修改任何 PC 端文件，只需新增移动端 YAML 文件并修改 i18n 配置即可。

---

## 9. 状态管理

### 9.1 Store 模块
复用现有 Pinia Store，按需扩展移动端特有状态：

```typescript
// store/modules/app.ts
export const useAppStore = defineStore('app', {
  state: () => ({
    // 移动端特有
    activeTab: 'home',           // 当前激活 Tab
    pullRefreshEnabled: true,    // 下拉刷新开关
    keepAlivePages: ['home', 'bill', 'report']  // 缓存页面
  })
});
```

---

## 10. 路由配置

### 10.1 路由设计原则
- **统一路由路径**：PC 端和移动端使用相同的 URL 路径
- **设备感知加载**：根据访问设备自动加载对应页面组件
- **URL 保持不变**：用户访问同一 URL，看到对应端的页面

### 10.2 路由结构
```typescript
// router/index.ts
import { createRouter, createWebHistory } from 'vue-router';
import { isMobile } from '@/utils/device';

// 设备检测函数
const getDeviceType = () => isMobile() ? 'mobile' : 'pc';

// 动态导入页面组件
const loadPage = (name: string) => {
  const device = getDeviceType();
  return () => import(`@/views/${device}/${name}/index.vue`);
};

// 动态导入布局
const loadLayout = () => {
  const device = getDeviceType();
  return device === 'mobile'
    ? () => import('@/mobile-layout/MobileLayout.vue')
    : () => import('@/layout/index.vue');
};

export const routes = [
  {
    path: '/',
    component: loadLayout(),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: loadPage('home'),
        meta: { title: '首页', keepAlive: true, showNavBar: false, showTabBar: true }
      },
      {
        path: 'bill-list',
        name: 'BillList',
        component: loadPage('bill'),
        meta: { title: '账单', keepAlive: true, showNavBar: false, showTabBar: true }
      },
      {
        path: 'record',
        name: 'Record',
        component: loadPage('record'),
        meta: { title: '记账', showNavBar: true, showTabBar: false }
      },
      {
        path: 'record/:id',
        name: 'RecordEdit',
        component: loadPage('record'),
        meta: { title: '编辑记录', showNavBar: true, showTabBar: false }
      },
      {
        path: 'report/bill-report',
        name: 'Report',
        component: loadPage('report'),
        meta: { title: '报表', keepAlive: true, showNavBar: false, showTabBar: true }
      },
      {
        path: 'search',
        name: 'Search',
        component: loadPage('search'),
        meta: { title: '搜索', showNavBar: true, showTabBar: false }
      },
      {
        path: 'user/profile',
        name: 'User',
        component: loadPage('user'),
        meta: { title: '我的', keepAlive: true, showNavBar: false, showTabBar: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: loadPage('login'),
    meta: { title: '登录', showNavBar: false, showTabBar: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: loadPage('register'),
    meta: { title: '注册', showNavBar: false, showTabBar: false }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
```

### 10.3 设备检测工具
```typescript
// utils/device.ts
export const isMobile = (): boolean => {
  if (typeof window === 'undefined') return false;

  const userAgent = navigator.userAgent.toLowerCase();
  const mobileKeywords = [
    'android', 'iphone', 'ipad', 'ipod',
    'windows phone', 'mobile', 'blackberry'
  ];

  // User Agent 检测
  if (mobileKeywords.some(keyword => userAgent.includes(keyword))) {
    return true;
  }

  // 屏幕宽度检测（可选，作为辅助判断）
  if (window.innerWidth < 768) {
    return true;
  }

  return false;
};

// 响应式设备变化监听
export const useDevice = () => {
  const mobile = ref(isMobile());

  const updateDevice = () => {
    mobile.value = isMobile();
  };

  onMounted(() => {
    window.addEventListener('resize', updateDevice);
  });

  onUnmounted(() => {
    window.removeEventListener('resize', updateDevice);
  });

  return { isMobile: mobile };
};
```

### 10.4 页面目录对应关系
| 路由路径 | PC 端页面 | 移动端页面 |
|----------|-----------|------------|
| `/dashboard` | `views/pc/home/index.vue` | `views/mobile/home/index.vue` |
| `/bill-list` | `views/pc/bill/index.vue` | `views/mobile/bill/index.vue` |
| `/record` | `views/pc/record/index.vue` | `views/mobile/record/index.vue` |
| `/report/bill-report` | `views/pc/report/index.vue` | `views/mobile/report/index.vue` |
| `/search` | `views/pc/search/index.vue` | `views/mobile/search/index.vue` |
| `/user/profile` | `views/pc/user/index.vue` | `views/mobile/user/index.vue` |
| `/login` | `views/pc/login/index.vue` | `views/mobile/login/index.vue` |

### 10.5 路由守卫
```typescript
// 路由守卫复用现有逻辑
// 检查登录状态、设置页面标题等
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title as string || '记账本';

  // 登录检查
  const token = getToken();
  const publicPages = ['/login', '/register'];
  if (!token && !publicPages.includes(to.path)) {
    return next('/login');
  }

  next();
});
```

---

## 11. 提示信息规范

### 11.1 组件选择

使用 Vant 4 的两个组件实现不同场景的提示：

| 组件 | 场景 | 特点 |
|------|------|------|
| **Notify** | 操作结果提示（成功/失败/警告/信息） | 顶部通知栏，有颜色背景，自动消失 |
| **Toast** | 加载状态提示 | 居中显示，可阻止点击，需手动关闭 |

### 11.2 提示类型与样式

使用 Vant Notify 组件实现统一提示，分为以下类型：

| 类型 | 方法 | 用途 | type 值 | 背景色 |
|------|------|------|---------|--------|
| 成功 | `showSuccess()` | 操作成功、保存成功等 | `success` | 绿色 |
| 错误 | `showError()` | 操作失败、网络错误、表单验证失败等 | `danger` | 红色 |
| 警告 | `showWarning()` | 提醒用户注意、数据异常等 | `warning` | 橙色 |
| 信息 | `showInfo()` | 一般提示信息 | `primary` | 蓝色 |
| 加载 | `showLoading()` | 异步操作进行中 | Toast | 白色背景 |

### 11.3 统一封装

```typescript
// utils/mobile/message.ts
import { showNotify, closeNotify } from "vant";
import { showLoadingToast, closeToast } from "vant";

/** 提示配置 */
const NOTIFY_DURATION = 3000; // 默认展示时间 3 秒

/** 成功提示（绿色） */
export const showSuccess = (message: string) => {
  showNotify({
    type: "success",
    message,
    duration: NOTIFY_DURATION
  });
};

/** 错误提示（红色） */
export const showError = (message: string) => {
  showNotify({
    type: "danger",
    message,
    duration: NOTIFY_DURATION
  });
};

/** 警告提示（橙色） */
export const showWarning = (message: string) => {
  showNotify({
    type: "warning",
    message,
    duration: NOTIFY_DURATION
  });
};

/** 信息提示（蓝色） */
export const showInfo = (message: string) => {
  showNotify({
    type: "primary",
    message,
    duration: NOTIFY_DURATION
  });
};

/** 加载提示 */
export const showLoading = (message: string = "加载中...") => {
  showLoadingToast({
    message,
    forbidClick: true,
    duration: 0 // 不自动关闭
  });
};

/** 关闭加载提示 */
export const hideLoading = () => {
  closeToast();
};

/** 关闭通知提示 */
export const hideNotify = () => {
  closeNotify();
};
```

### 11.4 使用示例

```typescript
import {
  showSuccess,
  showError,
  showWarning,
  showInfo,
  showLoading,
  hideLoading
} from "@/utils/mobile/message";

// 成功提示
const handleSave = async () => {
  showLoading("保存中...");
  try {
    await saveData();
    hideLoading();
    showSuccess("保存成功");
  } catch (error) {
    hideLoading();
    showError(error.message || "保存失败");
  }
};

// 表单验证错误
const validate = () => {
  if (!username.value) {
    showError("请输入用户名");
    return false;
  }
  return true;
};

// 警告提示
const handleDelete = () => {
  showWarning("删除后无法恢复，请谨慎操作");
};

// 信息提示
const handleTip = () => {
  showInfo("新功能已上线，快来体验吧");
};
```

### 11.5 提示信息文案规范

| 场景 | 文案格式 | 示例 |
|------|----------|------|
| 操作成功 | 动词 + 成功 | "保存成功"、"提交成功" |
| 操作失败 | 动词 + 失败 或 具体原因 | "保存失败"、"网络连接失败" |
| 表单验证 | 请 + 动词 + 名词 | "请输入用户名"、"请选择日期" |
| 数据为空 | 暂无 + 名词 | "暂无数据"、"暂无账单" |
| 确认操作 | 确认 + 动词 + 名词 + ？ | "确认删除该记录？" |

### 11.6 注意事项

1. **Notify 与 Toast 的区别**：
   - Notify：顶部通知栏，适合操作结果反馈
   - Toast：居中弹窗，适合加载状态和简短提示

2. **加载提示必须手动关闭**：
   ```typescript
   showLoading("处理中...");
   // 异步操作完成后
   hideLoading();
   ```

3. **避免提示重叠**：
   - 在显示新提示前，先关闭之前的加载提示
   - Notify 会自动替换之前的通知

---

## 12. 性能优化

### 12.1 路由懒加载
```typescript
component: () => import('@/views/mobile/home/index.vue')
```

### 12.2 组件按需加载
```typescript
// vite.config.ts - Vant 自动导入
Components({
  resolvers: [VantResolver()]
})
```

### 12.3 图片懒加载
```vue
<van-image lazy-src="..." />
```

### 12.4 列表虚拟滚动
```vue
<!-- 大数据量列表使用虚拟滚动 -->
<van-list v-model:loading="loading" :finished="finished" @load="onLoad">
  <van-cell v-for="item in list" :key="item.id" :title="item.title" />
</van-list>
```

### 12.5 页面缓存
```typescript
// 路由 meta.keepAlive 控制缓存
meta: { keepAlive: true }
```

---

## 13. 开发规范

### 13.1 命名规范
| 类型 | 规范 | 示例 |
|------|------|------|
| 组件 | PascalCase | `RecordItem.vue` |
| 页面 | kebab-case 目录 | `views/mobile/home/` |
| 变量 | camelCase | `billList` |
| 常量 | UPPER_SNAKE_CASE | `API_BASE_URL` |
| CSS 类 | kebab-case | `.bill-item` |

### 13.2 组件开发
```vue
<script setup lang="ts">
// 1. 导入
import { ref, computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';

// 2. Props/Emits
const props = defineProps<{...}>();
const emit = defineEmits<{...}>();

// 3. 响应式状态
const loading = ref(false);

// 4. 计算属性
const displayText = computed(() => ...);

// 5. 方法
const handleClick = () => {...};

// 6. 生命周期
onMounted(() => {...});
</script>

<template>
  <!-- 模板内容 -->
</template>

<style lang="scss" scoped>
/* 样式 */
</style>
```

### 13.3 Git 提交规范
```
feat(mobile): 添加首页功能
fix(mobile): 修复账单列表滚动问题
style(mobile): 调整主题色
refactor(mobile): 重构记账页面
```

---

## 14. 后续规划

### 14.1 第一阶段：基础功能
- [ ] 首页概览
- [ ] 账单列表
- [ ] 记账功能
- [ ] 基础报表

### 14.2 第二阶段：完善功能
- [ ] 搜索功能
- [ ] 分类管理
- [ ] 账本管理
- [ ] 标签管理

### 14.3 第三阶段：优化体验
- [ ] 国际化完善
- [ ] 离线缓存
- [ ] 数据导出
- [ ] 主题切换

---

## 15. 附录

### 15.1 参考资源
- [Vant 4 文档](https://vant-ui.github.io/vant/#/zh-CN)
- [Vue 3 文档](https://vuejs.org/)
- [vue-i18n 文档](https://vue-i18n.intlify.dev/)
- [ECharts 文档](https://echarts.apache.org/)

### 15.2 设计稿参考
- 待补充 Figma/Sketch 设计稿链接
