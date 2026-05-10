# 移动端报表页面设计文档

## 概述

开发移动端报表页面功能，包括账单报表和分类统计报表两种模式，支持筛选条件，使用 ECharts 展示图表。

## 页面结构

### 1. 主页面 (`/views/mobile/report/index.vue`)

**布局结构**：
- 顶部 Tab 切换：账单报表 / 分类报表
- 筛选按钮：点击弹出筛选弹窗
- 内容区域：根据 Tab 显示对应的图表和列表

### 2. 组件结构

```
src/views/mobile/report/
├── index.vue                    # 报表主页面（Tab切换）
├── components/
│   ├── ReportFilter.vue         # 筛选弹窗组件
│   ├── BillReport.vue           # 账单报表组件
│   └── ClassifyReport.vue       # 分类报表组件
```

## 筛选功能

### 筛选条件

| 条件 | 说明 |
|------|------|
| 账单类型 | 月账单 / 年账单 / 自定义 |
| 时间选择 | 月账单：年月选择器；年账单：年份选择器；自定义：开始+结束日期 |
| 分类 | 复用 ClassifyFilterPicker.vue，支持多选 |
| 备注 | 复用 RemarkPicker.vue |
| 标签 | 复用 TagPicker.vue，支持多选 |

### 筛选弹窗组件 (ReportFilter.vue)

**交互方式**：
- 点击"筛选"按钮弹出弹窗
- 选择筛选条件后点击"确定"按钮
- 支持"重置"功能

**时间选择器**：
- 月账单模式：使用 `van-picker` 实现年月选择
- 年账单模式：使用 `van-picker` 实现年份选择
- 自定义模式：使用两个日期选择器选择开始和结束日期

## 账单报表 (BillReport.vue)

### 图表区域

**展示内容**：
- 柱状图展示收入/支出数据
- 顶部统计卡片：
  - 总收入：绿色显示
  - 总支出：红色显示
  - 结余：正数绿色显示，负数红色显示，带正负号

**X轴数据**：
- 月账单：显示每日日期（MM-DD格式）
- 年账单：显示每月月份（YYYY-MM格式）
- 自定义：显示每日日期（MM-DD格式）

**图表配置**：
- 收入柱状图：绿色 (#67c23a)
- 支出柱状图：红色 (#f56c6c)
- 支持触摸交互，显示 tooltip

### 明细列表

**月账单/自定义模式**：
- 直接显示收支明细列表
- 列表项展示方式与首页近三日账单一致
- 支持按时间/按金额排序

**年账单模式**：
- 按月分组显示
- 月分组头部显示：月份、收入（绿色）、支出（红色）、结余（正绿负红）
- 点击月份展开显示该月明细列表
- 排序方式只影响各月份内的记录顺序，月份间互不影响

**排序逻辑**：
- 按时间：按记录时间倒序排列
- 按金额：按记录金额由大到小排列

## 分类报表 (ClassifyReport.vue)

### 图表区域

**展示内容**：
- 饼状图展示分类占比
- 顶部统计卡片：
  - 总收入：绿色显示
  - 总支出：红色显示
  - 结余：正数绿色显示，负数红色显示，带正负号

**饼状图配置**：
- 显示分类名称和占比
- 支持点击分类查看明细
- 支出分类使用红色系配色
- 收入分类使用绿色系配色

### 明细列表

**触发方式**：
- 点击饼状图中的分类
- 或点击分类列表项

**列表头部信息**：
```
分类：餐饮    占比：16.5%    记录笔数：120    支出：¥3,898.95
```
- 分类名称
- 占比百分比
- 记录笔数
- 收入/支出标识（收入绿色，支出红色）
- 总金额（收入绿色，支出红色）

**列表内容**：
- 显示该分类的收支明细列表
- 列表项展示方式与首页近三日账单一致
- 支持按时间/按金额排序

## 颜色规范

| 数据类型 | 颜色 | 说明 |
|---------|------|------|
| 收入 | #67c23a (绿色) | 图表、统计数字、文字 |
| 支出 | #f56c6c (红色) | 图表、统计数字、文字 |
| 结余(正) | #67c23a (绿色) | 带正号显示 |
| 结余(负) | #f56c6c (红色) | 带负号显示 |

## API 接口

### 获取账单报表数据

```typescript
// 接口：POST /incomeExpense/sumPeriod
interface TrendParams {
  userId: number;
  accountBookId?: number;
  mode: string;           // "0": 月/自定义, "1": 年
  queryMode?: string;     // "0": 账单报表
  beginDate: string;
  endDate: string;
  classifyList?: { mainClassifyId: number; subClassifyId: number | null }[];
  remark?: string;
  tagCodes?: number[];
}

interface TrendData {
  expenseTotal: number;
  incomeTotal: number;
  expenseLimit: number;
  expenseSurplus: number;
  incomeExpenseSum: Record<string, DaySum>;
  incomeExpenseList: IncomeExpenseRecord[];
}
```

### 获取分类报表数据

```typescript
// 接口：POST /incomeExpense/sumPeriod
// queryMode: "1" 表示分类报表

interface ClassifyReportData {
  expenseTotal: number;
  incomeTotal: number;
  expenseLimit: number;
  expenseSurplus: number;
  incomeExpenseSum: Record<string, ClassifySummary>;
  incomeExpenseList: IncomeExpenseRecord[];
}

interface ClassifySummary {
  percent: number;
  expense: number;
  income: number;
  num: number;
  classifyName: string;
  classifyImage: string;
  classify: string;
  type?: string;
}
```

## 实现步骤

### 步骤 1：创建筛选弹窗组件
- 创建 `ReportFilter.vue`
- 实现账单类型切换
- 实现时间选择器（年月/年/日期范围）
- 集成分类、备注、标签选择器

### 步骤 2：创建账单报表组件
- 创建 `BillReport.vue`
- 集成 ECharts 柱状图
- 实现统计卡片显示
- 实现明细列表（支持年账单分组展开）
- 实现排序功能

### 步骤 3：创建分类报表组件
- 创建 `ClassifyReport.vue`
- 集成 ECharts 饼状图
- 实现统计卡片显示
- 实现分类点击查看明细
- 实现排序功能

### 步骤 4：创建主页面
- 创建 `index.vue`
- 实现 Tab 切换
- 集成筛选弹窗
- 集成两个报表组件

### 步骤 5：添加国际化文案
- 添加报表相关文案

## 注意事项

1. **图表响应式**：ECharts 需要监听窗口大小变化，自动调整图表尺寸
2. **触摸交互**：移动端需要支持触摸手势，如缩放、滑动等
3. **性能优化**：大量数据时需要考虑列表虚拟滚动
4. **状态保持**：切换 Tab 时保持各自的筛选状态和滚动位置
