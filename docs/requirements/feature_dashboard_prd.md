# 首页功能 PRD

## 一、功能描述

### 1.1 功能概述

首页（仪表盘）是 BK-PC 记账系统的入口页面，展示用户本月的收支概况、趋势图表和最近记账记录，提供快捷记账入口。

### 1.2 功能需求

- 本月收支概览卡片（支出、收入、支出限额、剩余限额）
- 收支趋势折线图（最近7天/30天）
- 最近三天记账记录列表
- 快捷操作按钮（记一笔）
- 账本切换功能
- 左侧菜单导航

### 1.3 左侧菜单结构

```
- 首页 (/dashboard)
- 收入支出记录 (/bill)
- 统计报表 (/report)
- 设置
  - 支出限额设置 (/settings/limit)
  - 账本设置 (/settings/account-book)
  - 分类设置 (/settings/classify)
  - 备注设置 (/settings/remark)
  - 标签设置 (/settings/tag)
```

---

## 二、用户流程

```
用户登录成功
        ↓
跳转首页 /dashboard
        ↓
调用 /incomeExpense/summary 获取本月摘要数据
        ↓
调用 /incomeExpense/sumPeriod 获取趋势数据
        ↓
渲染收支概览卡片
        ↓
渲染收支趋势图
        ↓
渲染最近记账记录列表
```

---

## 三、接口定义

### 3.1 获取收支摘要

**请求**

```
GET /incomeExpense/summary
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户ID |
| accountBookId | Long | 否 | 账本ID，不传则查询所有账本 |
| days | Integer | 否 | 查询最近几天的记录，默认2 |

**响应**

```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "expenseAmount": 5000.00,
    "incomeAmount": 8000.00,
    "expenseLimit": 10000.00,
    "expenseSurplus": 5000.00,
    "incomeExpenseList": [
      {
        "id": 1,
        "amount": 50.00,
        "type": 0,
        "date": "2026-03-18",
        "remark": "午餐",
        "mainClassifyName": "餐饮",
        "subClassifyName": "午餐",
        "mainClassifyImage": "icon-food"
      }
    ]
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| expenseAmount | BigDecimal | 本月支出总额 |
| incomeAmount | BigDecimal | 本月收入总额 |
| expenseLimit | BigDecimal | 支出预算 |
| expenseSurplus | BigDecimal | 预算剩余 |
| incomeExpenseList | Array | 最近记账记录列表 |

### 3.2 获取收支趋势

**请求**

```
GET /incomeExpense/sumPeriod
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户ID |
| accountBookId | Long | 否 | 账本ID，不传则查询所有账本 |
| mode | String | 是 | 查询模式：0-月，1-年，2-自定义 |
| queryMode | String | 否 | 查询方式：0-账单，1-报表，默认0 |
| beginDate | String | 是 | 开始日期 (yyyy-MM-dd) |
| endDate | String | 是 | 结束日期 (yyyy-MM-dd) |
| classifyList | Array | 否 | 分类ID列表 |

**响应**

```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "expenseTotal": 5000.00,
    "incomeTotal": 8000.00,
    "expenseLimit": 10000.00,
    "expenseSurplus": 5000.00,
    "incomeExpenseSum": {
      "2026-03-12": {
        "income": 0.00,
        "expense": 150.00
      },
      "2026-03-13": {
        "income": 5000.00,
        "expense": 80.00
      }
    },
    "incomeExpenseList": [
      {
        "id": 1,
        "amount": 50.00,
        "type": 0,
        "date": "2026-03-18",
        "remark": "午餐",
        "mainClassifyName": "餐饮",
        "subClassifyName": "午餐"
      }
    ]
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| expenseTotal | BigDecimal | 总支出 |
| incomeTotal | BigDecimal | 总收入 |
| expenseLimit | BigDecimal | 当前支出限额 |
| expenseSurplus | BigDecimal | 当���支出剩余 |
| incomeExpenseSum | Dict | 收入支出统计列表，key为日期(yyyy-MM-dd)，value包含income和expense |
| incomeExpenseList | List | 收支详细列表 |

### 3.3 获取账本列表

**请求**

```
GET /accountBook
```

**响应**

```json
{
  "code": 0,
  "msg": "ok",
  "data": [
    {
      "id": 1,
      "name": "日常账本",
      "image": "book-default",
      "isDefault": true
    }
  ]
}
```

---

## 四、数据结构

### 4.1 收支摘要 (Summary)

| 字段 | 类型 | 说明 |
|------|------|------|
| expenseAmount | BigDecimal | 本月支出总额 |
| incomeAmount | BigDecimal | 本月收入总额 |
| expenseLimit | BigDecimal | 支出预算 |
| expenseSurplus | BigDecimal | 预算剩余 |
| incomeExpenseList | List | 最近记账记录列表 |

### 4.2 趋势数据 (BillResult)

| 字段 | 类型 | 说明 |
|------|------|------|
| expenseTotal | BigDecimal | 总支出 |
| incomeTotal | BigDecimal | 总收入 |
| expenseLimit | BigDecimal | 当前支出限额 |
| expenseSurplus | BigDecimal | 当前支出剩余 |
| incomeExpenseSum | Dict | 收入支出统计列表，key为日期(yyyy-MM-dd)，value包含income和expense |
| incomeExpenseList | List | 收支详细列表 |

### 4.3 最近记录

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 记录ID |
| amount | BigDecimal | 金额 |
| type | String | 类型：0-支出，1-收入 |
| date | LocalDate | 日期 |
| remark | String | 备注 |
| mainClassifyName | String | 主分类名称 |
| subClassifyName | String | 子分类名称 |
| mainClassifyImage | String | 主分类图标 |

### 4.4 账本 (AccountBook)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 账本ID |
| name | String | 账本名称 |
| image | String | 图标 |
| isDefault | Boolean | 是否默认 |

---

## 五、页面设计

### 5.1 布局结构

```
┌─────────────────────────────────────────────────────────────┐
│  顶部导航栏 (lay-navbar)                                     │
├──────────┬──────────────────────────────────────────────────┤
│          │  当前账本：日常账本  [▼]  [+ 记一笔]              │
│  左侧菜单 │──────────────────────────────────────────────────│
│          │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐    │
│  · 首页   │  │ 支出   │ │ 收入   │ │支出限额│ │剩余限额│    │
│  · 账单   │  │ ¥5000  │ │ ¥8000  │ │¥10000 │ │ ¥5000  │    │
│  · 统计   │  └────────┘ └────────┘ └────────┘ └────────┘    │
│  · 设置   │                                                 │
│    - 限额 │  收支趋势图 (ECharts 折线图)                     │
│    - 账本 │  ┌───────────────────────────────────────────┐  │
│    - 分类 │  │          ╱╲    ╱                           │  │
│    - 备注 │  │         ╱  ╲__╱                             │  │
│    - 标签 │  └───────────────────────────────────────────┘  │
│          │                                                 │
│          │  最近三天记账记录                                 │
│          │  ┌───────────────────────────────────────────┐   │
│          │  │ 03-18  午餐    餐饮    -50.00   支出      │   │
│          │  │ 03-17  工资    工资   +5000.00  收入      │   │
│          │  │ 03-16  购物    日用   -120.00   支出      │   │
│          │  └───────────────────────────────────────────┘   │
└──────────┴──────────────────────────────────────────────────┘
```

### 5.2 组件设计

| 组件 | 说明 |
|------|------|
| SummaryCard | 收支概览卡片（收入/支出/预算） |
| TrendChart | 收支趋势折线图 |
| RecentRecords | 最近记账记录列表 |
| AccountBookSelect | 账本选择器 |
| QuickAddButton | 快捷记账按钮 |

---

## 六、异常处理

| 错误场景 | 处理方式 |
|----------|----------|
| 获取摘要失败 | 显示空状态，提示"数据加载失败" |
| 获取趋势图失败 | 隐藏趋势图区域，显示空状态 |
| 账本列表为空 | 显示"请先创建账本"提示 |
| 网络错误 | 显示重试按钮 |

---

## 七、文件清单

### 7.1 需要新建的文件

| 文件路径 | 职责描述 |
|----------|----------|
| `src/api/dashboard.ts` | 定义首页相关API方法（获取摘要、趋势数据、账本列表） |
| `src/types/dashboard.ts` | 首页相关TypeScript类型定义 |
| `src/store/modules/bill.ts` | 账单状态管理（存储摘要数据、当前账本） |
| `src/views/dashboard/components/SummaryCards.vue` | 收支概览卡片组件 |
| `src/views/dashboard/components/TrendChart.vue` | 趋势图表组件 |
| `src/views/dashboard/components/RecentRecords.vue` | 最近记录列表组件 |
| `src/views/dashboard/components/AccountBookSelect.vue` | 账本选择组件 |

### 7.2 需要修改的文件

| 文件路径 | 修改内容 |
|----------|----------|
| `src/views/dashboard/index.vue` | 重写首页，组合各个子组件 |
| `src/router/modules/home.ts` | 配置首页路由元信息 |
| `src/api/auth.ts` | （已有，保持不变） |

---

## 八、验收标准

### 8.1 功能验收

- [ ] 页面加载时自动获取本月收支摘要
- [ ] 四个卡片一行显示：支出、收入、支出限额、剩余限额
- [ ] 收支趋势折线图正确渲染
- [ ] 最近三天记账记录列表显示正确
- [ ] 顶部左侧显示当前账本名称
- [ ] 账本下拉选择功能正常
- [ ] 记一笔快捷按钮跳转正常
- [ ] 左侧菜单导航正确显示

### 8.2 交互验收

- [ ] 页面loading状态显示
- [ ] 账本切换时数据刷新
- [ ] 点击记账记录跳转到编辑页面
- [ ] 点击"记一笔"跳转到记账页面

### 8.3 性能验收

- [ ] 页面首次加载 < 2s
- [ ] 切换账本数据刷新 < 1s

---

## 九、技术实现要点

### 9.1 数据流

```
页面加载
    ↓
DashboardStore.loadSummary() → API调用 → 更新store.state.summary
    ↓
DashboardStore.loadTrend() → API调用 → 更新store.state.trendData
    ↓
DashboardStore.loadAccountBooks() → API调用 → 更新store.state.accountBooks
    ↓
组件从store获取数据渲染
```

### 9.2 缓存策略

- 账本列表缓存到本地，切换账本时刷新
- 摘要数据每次进入页面刷新

### 9.3 图表配置

- 使用 ECharts 折线图
- 支持日/周/月切换
- 双Y轴：收入和支出
- 支持触摸操作