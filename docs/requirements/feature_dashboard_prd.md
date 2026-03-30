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
- 支出限额显示配置（通过用户配置 show_expense_limit 控制）

### 1.3 用户配置项

| 配置项 | 说明 |
|--------|------|
| show_expense_limit | 支出限额显示模式：1-不显示，2-月度限额，3-年度限额 |
| is_credit_card | 新增记录时信用卡checkbox默认状态：0-不勾选，1-勾选 |

### 1.3 左侧菜单结构

```
- 首页 (/dashboard)
- 收入支出记录 (/bill)
- 统计报表 (/report)
  - 账单统计 (/report/bill-report)
  - 分类统计 (/report/classify-report)
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
调用 /accountBook 获取账本列表（存储到store，供账本切换使用）
        ↓
调用 /classify 获取用户分类列表（存储到store，供收支记录页面使用）
        ↓
调用 /userTag 获取用户标签列表（存储到store，供收支记录页面使用）
        ↓
调用 /userRemark 获取用户备注列表（存储到store，供收支记录页面使用）
        ↓
直接调用 API 获取本月摘要数据（不存储到store）
        ↓
直接调用 API 获取趋势数据（不存储到store）
        ↓
渲染收支概览卡片
        ↓
渲染收支趋势图
        ↓
渲染最近记账记录列表
```

**说明**：
- 分类、标签和备注数据在首页加载后存储到状态管理中，供收支记录页面直接使用，避免重复请求。
- 收支摘要和趋势数据每次进入页面时直接查询，不存储到 store，属于即时数据查询。

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

### 3.4 获取用户分类列表

**请求**

```
GET /classify
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | number | 是 | 用户ID |
| type | number | 否 | 类型: 0-支出, 1-收入 |

**响应**

```json
{
  "code": 0,
  "msg": "ok",
  "data": [
    {
      "id": 1,
      "pid": 0,
      "name": "餐饮",
      "userId": 62,
      "image": "food",
      "sort": 1,
      "type": 0,
      "enable": true
    }
  ],
  "timestamp": 1706512345678
}
```

### 3.5 获取用户标签列表

**请求**

```
GET /userTag
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | number | 是 | 用户ID |

**响应**

```json
{
  "code": 0,
  "msg": "ok",
  "data": [
    {
      "id": 10001,
      "name": "重要",
      "color": "#ff4d4f"
    },
    {
      "id": 10002,
      "name": "日常",
      "color": "#1890ff"
    }
  ],
  "timestamp": 1706512345678
}
```

### 3.5 获取用户备注列表

**请求**

```
GET /userRemark
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | number | 是 | 用户ID |

**响应**

```json
{
  "code": 0,
  "msg": "ok",
  "data": [
    "午饭",
    "晚饭",
    "工资",
    "购物"
  ],
  "timestamp": 1706512345678
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

### 4.5 分类 (Classify)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 分类ID |
| pid | Long | 父分类ID |
| name | String | 分类名称 |
| userId | Long | 用户ID |
| image | String | 图标 |
| sort | Integer | 排序 |
| type | Integer | 类型: 0-支出, 1-收入 |
| enable | Boolean | 是否启用 |
| children | Array | 子分类列表 |

### 4.6 标签 (Tag)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 标签ID |
| name | String | 标签名称 |
| color | String | 标签颜色 |

### 4.7 备注 (Remark)

| 字段 | 类型 | 说明 |
|------|------|------|
| - | string[] | 用户备注列表 |

---

## 五、页面设计

### 5.1 布局结构

```
┌─────────────────────────────────────────────────────────────┐
│  顶部导航栏 (lay-navbar)                                     │
├──────────┬──────────────────────────────────────────────────┤
│          │  当前账本：日常账本  [▼]  [+ 记一笔]              │
│  左侧菜单 │──────────────────────────────────────────────────│
│          │  支出限额显示模式=1 (不显示限额):                  │
│  · 首页   │  ┌──────────────┐ ┌──────────────┐               │
│  · 账单   │  │ 支出 ¥5000   │ │ 收入 ¥8000   │               │
│  · 统计   │  └──────────────┘ └──────────────┘               │
│  · 设置   │                                                 │
│    - 限额 │  支出限额显示模式=2 (月度限额) 或 3 (年度限额):   │
│    - 账本 │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐     │
│    - 分类 │  │ 支出   │ │ 收入   │ │月支出  │ │月剩余  │     │
│    - 备注 │  │ ¥5000  │ │ ¥8000  │ │¥10000  │ │ ¥5000  │     │
│    - 标签 │  └────────┘ └────────┘ └────────┘ └────────┘     │
│          │                                                 │
│          │  收支趋势图 (ECharts 柱状图)                      │
│          │  ┌───────────────────────────────────────────┐   │
│          │  │ 总收入: ¥8000  总支出: ¥5000    [月][季] │   │
│          │  │ ████████  ██████                          │   │
│          │  │ ██████    ████████                        │   │
│          │  │ 03-01    03-02    03-03                  │   │
│          │  └───────────────────────────────────────────┘   │
│          │                                                 │
│          │  最近三天记账记录                                 │
│          │  ┌───────────────────────────────────────────┐   │
│          │  │ 03-18  午餐    餐饮    -50.00   支出      │   │
│          │  │ 03-17  工资    工资   +5000.00  收入      │   │
│          │  │ 03-16  购物    日用   -120.00   支出      │   │
│          │  └───────────────────────────────────────────┘   │
└──────────┴──────────────────────────────────────────────────┘
```

### 5.2 收支趋势图

**图表特性：**
- 图表类型：柱状图
- 数据来源：直接调用 `/incomeExpense/sumPeriod` 接口（不存储到 store）
- 固定参数：`queryMode=0`（账单模式）

**顶部左侧：**
- 显示总收入（incomeTotal）和总支出（expenseTotal）

**顶部右侧周期切换按钮：**
| 按钮名称 | mode参数 | beginDate | endDate |
|---------|----------|-----------|---------|
| 月 | 0 | 当前月第一天 | 当前月最后一天 |
| 季度 | 0 | 当前季度第一天 | 当前季度最后一天 |
| 近半年 | 0 | 当前日期向前180天 | 当前日期 |
| 年 | 1 | 当年第一天 | 当年最后一天 |

**X轴标签格式：**
- 月/季度/近半年模式：显示 MM-DD（如 03-01）
- 年模式：显示 YYYY-MM（如 2026-01），代表月总计

**Tooltip：**
- 鼠标指向柱状图时显示完整日期（如 2026-03-01）

### 5.3 支出限额卡片显示逻辑

| show_expense_limit值 | 显示内容 | 布局 |
|---------------------|----------|------|
| 1 | 只显示支出、收入两个卡片 | 两个卡片各占50%宽度 |
| 2 | 显示支出、收入、月度限额、月度剩余 | 四个卡片各占25%宽度，限额标题显示"月支出限额"/"月剩余限额" |
| 3 | 显示支出、收入、年度限额、年度剩余 | 四个卡片各占25%宽度，限额标题显示"年度支出限额"/"年度剩余限额" |

**注意**：支出/收入卡片上的编辑按钮用于修改限额，仅在show_expense_limit!=1时显示在支出卡片数值后面。

### 5.4 组件设计

| 组件 | 说明 |
|------|------|
| SummaryCards | 收支概览卡片（收入/支出/预算），根据show_expense_limit配置显示2或4个卡片 |
| TrendChart | 收支趋势柱状图，直接调用API获取数据，支持月/季度/近半年/年份切换 |
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
| `src/store/modules/bill.ts` | 账单状态管理（存储账本列表、分类、标签、备注） |
| `src/views/dashboard/components/SummaryCards.vue` | 收支概览卡片组件 |
| `src/views/dashboard/components/TrendChart.vue` | 趋势图表组件，直接调用API获取数据 |
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
- [ ] 支出限额显示根据用户配置 show_expense_limit 动态调整
- [ ] show_expense_limit=1 时只显示支出、收入两个卡片，卡片各占50%宽度
- [ ] show_expense_limit=2 时显示四个卡片，限额标题为"月支出限额"/"月剩余限额"
- [ ] show_expense_limit=3 时显示四个卡片，限额标题为"年度支出限额"/"年度剩余限额"
- [ ] 支出卡片数值后面显示编辑按钮，点击弹出限额设置对话框
- [ ] 收支趋势柱状图正确渲染
- [ ] 趋势图左上角显示总收入和总支出
- [ ] 趋势图支持月/季度/近半年/年份周期切换
- [ ] 趋势图切换周期时正确调用对应日期范围的API
- [ ] 年模式时X轴显示完整月份（YYYY-MM格式）
- [ ] 鼠标指向柱状图时显示完整日期（如2026-03-01）
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
BillStore.loadAccountBooks() → API调用 → 更新store.state.accountBooks
    ↓
BillStore.loadClassifyAndTag() → API调用 → 获取分类和标签列表存储
    ↓
SummaryCards组件：直接调用 getSummary API → 返回数据供组件渲染（不存储到store）
    ↓
TrendChart组件：根据用户选择的周期（月/季度/近半年/年），直接调用 getTrendData API(queryMode=0)
    ↓
RecentRecords组件：使用SummaryCards返回的incomeExpenseList数据渲染
```

**注意**：
- 分类(classify)和标签(userTag)数据在首页加载时获取，存储在 BillStore 中供收支记录页面使用，无需在收支记录页面重复请求。
- 收支摘要数据属于即时数据，每次页面加载时直接查询 API，不存储到 store，避免数据冗余。
- 趋势图表数据由 TrendChart 组件自行管理，根据用户选择的周期动态请求不同日期范围的数据。

### 9.2 缓存策略

- 账本列表缓存到本地，切换账本时刷新
- 分类、标签、备注数据存储到 store，供其他页面复用
- 收支摘要和趋势数据每次进入页面刷新（不缓存）

### 9.3 图表配置

- 使用 ECharts 折线图
- 支持日/周/月切换
- 双Y轴：收入和支出
- 支持触摸操作