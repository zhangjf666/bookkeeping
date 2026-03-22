# 收入支出记录菜单 PRD

## 一、功能描述

### 1.1 功能概述

收入支出记录模块是 BK-PC 记账管理系统的核心功能模块，用于管理用户的记账数据，支持收支记录的增删改查、批量操作、多条件筛选等功能。

### 1.2 功能需求

- 收支记录列表展示（分页）
- 新增收支记录
- 编辑收支记录
- 删除收支记录（支持批量删除）
- 多条件筛选查询
- 分类名称/图标展示

---

## 二、用户流程

### 2.1 收支记录管理流程

```
┌─────────────────────────────────────────────────────────────┐
│                   收支记录管理流程                           │
├─────────────────────────────────────────────────────────────┤
│  1. 用户进入账单列表页 (/bill)                               │
│           ↓                                                 │
│  2. 默认加载当前账本/当月收支记录                            │
│           ↓                                                 │
│  3. 用户可设置筛选条件：                                     │
│     - 日期范围                                               │
│     - 收支类型 (支出/收入)                                   │
│     - 账本                                                   │
│     - 金额范围                                               │
│     - 分类                                                   │
│     - 备注 (模糊查询)                                        │
│     - 标签 (多选下拉勾选)                                    │
│           ↓                                                 │
│  4. 点击查询/自动查询                                        │
│           ↓                                                 │
│  5. 表格展示收支记录列表                                     │
│           ↓                                                 │
│  6. 用户可执行操作：                                         │
│     - 新增记录 → 弹窗表单 → 提交 → 刷新列表                  │
│     - 编辑记录 → 弹窗表单 → 提交 → 刷新列表                  │
│     - 删除记录 → 确认 → 删除 → 刷新列表                      │
│     - 批量删除 → 确认 → 删除 → 刷新列表                      │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 新增/编辑流程

```
点击"新增"或"编辑"按钮
        ↓
弹出记账表单弹窗
        ↓
填写/回显数据：
  - 金额 (必填)
  - 类型 (支出/收入)
  - 主分类 (必填)
  - 子分类
  - 日期 (必填，默认今天)
  - 备注
  - 是否信用卡消费
  - 是否加入常用备注
  - 标签
        ↓
点击"确定"提交
        ↓
调用后端接口创建/更新
        ↓
成功：关闭弹窗、提示成功、刷新列表
        ↓
失败：提示错误信息
```

---

## 三、接口定义

### 3.1 查询收支记录列表

**请求**

```
GET /incomeExpense
```

**请求参数** (Query Params)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | number | 否 | 页码 (从1开始) |
| size | number | 否 | 每页条数 |
| accountBookId | number | 否 | 账本ID |
| type | number | 否 | 类型: 0-支出, 1-收入 |
| date | string[] | 否 | 日期范围 [start, end] |
| amount | number[] | 否 | 金额范围 [min, max] |
| mainClassify | number | 否 | 主分类ID |
| subClassify | number | 否 | 子分类ID |
| remark | string | 否 | 备注 (模糊查询 %like%) |
| tagCodes | number[] | 否 | 标签ID列表 (多选查询) |

**响应**

```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "accountBookId": 1,
      "amount": 100.00,
      "type": 0,
      "date": "2024-01-15",
      "remark": "午餐",
      "mainClassify": 1,
      "subClassify": 2,
      "isCreditCard": false,
      "isAddRemark": false,
      "tagCodes": "1,2,3",
      "createTime": "2024-01-15T12:00:00",
      "updateTime": "2024-01-15T12:00:00",
      "mainClassifyName": "餐饮",
      "subClassifyName": "午餐",
      "mainClassifyImage": "food",
      "subClassifyImage": "lunch",
      "tags": [
        { "id": 1, "name": "重要", "color": "#ff4d4f" },
        { "id": 2, "name": "日常", "color": "#1890ff" },
        { "id": 3, "name": "月度", "color": "#52c41a" }
      ]
    }
  ],
  "msg": "success"
}
```

### 3.2 创建收支记录

**请求**

```
POST /incomeExpense
Content-Type: application/json
```

**请求体**

```json
{
  "accountBookId": 1,
  "amount": 100.00,
  "type": 0,
  "date": "2024-01-15",
  "remark": "午餐",
  "mainClassify": 1,
  "subClassify": 2,
  "isCreditCard": false,
  "isAddRemark": false,
  "tagCodes": "1,2,3"
}
```

**字段说明**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| accountBookId | number | 是 | 账本ID |
| amount | number | 是 | 金额 |
| type | number | 是 | 类型: 0-支出, 1-收入 |
| date | string | 是 | 日期 (yyyy-MM-dd) |
| remark | string | 否 | 备注 |
| mainClassify | number | 是 | 主分类ID |
| subClassify | number | 否 | 子分类ID |
| isCreditCard | boolean | 否 | 是否信用卡消费 |
| isAddRemark | boolean | 否 | 是否加入常用备注 |
| tagCodes | string | 否 | 标签ID列表 (逗号分隔) |

**响应**

```json
{
  "code": 0,
  "data": null,
  "msg": "success"
}
```

### 3.3 更新收支记录

**请求**

```
PUT /incomeExpense
Content-Type: application/json
```

**请求体**

```json
{
  "id": 1,
  "accountBookId": 1,
  "amount": 150.00,
  "type": 0,
  "date": "2024-01-15",
  "remark": "午餐",
  "mainClassify": 1,
  "subClassify": 2,
  "isCreditCard": true,
  "isAddRemark": false,
  "tagCodes": "1,2"
}
```

**响应**

```json
{
  "code": 0,
  "data": null,
  "msg": "success"
}
```

### 3.4 删除收支记录

**请求**

```
DELETE /incomeExpense
Content-Type: application/json
```

**请求体**

```json
[1, 2, 3]
```

**响应**

```json
{
  "code": 0,
  "data": null,
  "msg": "success"
}
```

---

## 四、前端页面设计

### 4.1 账单列表页

**路径**: `/bill`

**布局结构**:

```
┌────────────────────────────────────────────────────────┐
│  [新增] [批量删除]                    [更多筛选 ▼]     │
├────────────────────────────────────────────────────────┤
│  账本: [全部 ▼]  类型: [全部 ▼]  日期: [日期范围]      │
│  金额: [Min]-[Max]   分类: [选择]                       │
│  备注: [___________]  标签: [选择标签 ▼]               │
│  [重置] [查询]                                        │
├────────────────────────────────────────────────────────┤
│  ┌─────┬────────┬────────┬───────┬────────┬─────────┐  │
│  │ ☐   │ 金额   │ 类型   │ 分类  │ 标签   │ 操作    │  │
│  ├─────┼────────┼────────┼───────┼────────┼─────────┤  │
│  │ ☐   │ 100.00 │ 支出   │ 餐饮  │ 重要   │ [编辑] │  │
│  │     │        │        │       │ 日常   │ [删除] │  │
│  └─────┴────────┴────────┴───────┴────────┴─────────┘  │
├────────────────────────────────────────────────────────┤
│                              共 100 条  [首页] [上页]  │
│                                    [下页] [末页]       │
└────────────────────────────────────────────────────────┘
```

### 4.2 新增/编辑弹窗

**弹窗内容**:

```
┌────────────────────────────────────────┐
│  新增/编辑 收支记录                    │
├────────────────────────────────────────┤
│  金额: * [_______________]             │
│  类型: * [● 支出 ○ 收入]               │
│  账本: * [请选择账本 ▼]                │
│  主分类: * [请选择分类 ▼]              │
│  子分类:   [请选择子分类 ▼]            │
│  日期: * [____年__月__日]              │
│  备注:   [_______________] [☐ 加入常用备注] │
│  标签:   [选择标签 ▼] (非必填)         │
│  ☑ 信用卡消费                          │
├────────────────────────────────────────┤
│                          [取消] [确定] │
└────────────────────────────────────────┘
```

### 4.3 组件设计

| 组件 | 说明 |
|------|------|
| BillList | 账单列表主组件 |
| BillFilter | 筛选表单组件 |
| BillForm | 新增/编辑表单组件 |
| BillTable | 表格组件 |
| BillPagination | 分页组件 |

---

## 五、数据结构

### 5.1 收支记录 (IncomeExpense)

```typescript
interface IncomeExpense {
  id: number
  userId: number
  accountBookId: number
  amount: number
  type: 0 | 1          // 0-支出, 1-收入
  date: string         // yyyy-MM-dd
  remark: string
  mainClassify: number
  subClassify: number
  isCreditCard: boolean
  isAddRemark: boolean
  tagCodes: string     // 标签ID列表 (逗号分隔)
  createTime: string   // yyyy-MM-ddTHH:mm:ss
  updateTime: string
  // 扩展字段
  mainClassifyName: string
  subClassifyName: string
  mainClassifyImage: string
  subClassifyImage: string
  // 标签字段
  tags: Tag[]
}

interface Tag {
  id: number
  name: string
  color: string
}
```

### 5.2 查询参数 (IncomeExpenseQuery)

```typescript
interface IncomeExpenseQuery {
  page?: number
  size?: number
  accountBookId?: number
  type?: 0 | 1
  date?: [string, string]    // [start, end]
  amount?: [number, number]  // [min, max]
  mainClassify?: number
  subClassify?: number
  remark?: string            // 备注模糊查询
  tagCodes?: number[]        // 标签ID列表 (多选)
}
```

### 5.3 表单数据 (IncomeExpenseForm)

```typescript
interface IncomeExpenseForm {
  id?: number           // 编辑时存在
  accountBookId: number
  amount: number
  type: 0 | 1
  date: string
  remark: string
  mainClassify: number
  subClassify?: number
  isCreditCard: boolean
  isAddRemark: boolean
  tagCodes: string      // 标签ID列表 (逗号分隔，非必填)
}
```

### 5.4 标签选择器设计

**筛选条件中的标签选择器**:
- 使用 `el-select` 组件，设置 `multiple` 和 `filterable` 属性
- 支持用户输入文字筛选标签
- 支持多选勾选
- 多选时展示已选标签的 tag

**新增/编辑表单中的标签选择器**:
- 同上，使用下拉框多选模式
- 非必填项
- 回显时将 `tagCodes` 拆分为数组展示

---

## 六、异常处理

### 6.1 错误码与提示

| 错误场景 | 提示信息 |
|----------|----------|
| 金额为空 | 请输入金额 |
| 金额格式错误 | 金额格式不正确 |
| 收支类型未选 | 请选择收支类型 |
| 账本未选择 | 请选择账本 |
| 分类未选择 | 请选择分类 |
| 日期未选择 | 请选择日期 |
| 删除确认 | 确定要删除选中的记录吗？ |
| 批量删除确认 | 确定要删除选中的 {n} 条记录吗？ |
| 删除成功 | 删除成功 |
| 创建成功 | 创建成功 |
| 更新成功 | 更新成功 |
| 网络错误 | 网络错误，请稍后重试 |

### 6.2 前端表单校验

- 金额：必填，正数，最多两位小数
- 类型：必选
- 账本：必选
- 主分类：必选
- 日期：必选，默认当天

---

## 七、文件清单

### 7.1 需要新建的文件

| 文件路径 | 职责描述 |
|----------|----------|
| `bk-pc/src/api/incomeExpense.ts` | 扩展收支 API 接口（新增、编辑、删除） |
| `bk-pc/src/store/modules/bill.ts` | 账单状态管理（列表数据、筛选条件） |
| `bk-pc/src/views/bill/index.vue` | 账单列表页主组件 |
| `bk-pc/src/views/bill/components/BillFilter.vue` | 筛选表单组件 |
| `bk-pc/src/views/bill/components/BillTable.vue` | 表格组件 |
| `bk-pc/src/views/bill/components/BillForm.vue` | 新增/编辑表单组件 |
| `bk-pc/src/views/bill/components/BillPagination.vue` | 分页组件 |
| `bk-pc/src/router/modules/bill.ts` | 账单路由配置 |
| `bk-pc/src/types/bill.ts` | 账单相关 TypeScript 类型定义 |

### 7.2 需要修改的文件

| 文件路径 | 修改内容 |
|----------|----------|
| `bk-pc/src/router/index.ts` | 引入账单路由模块 |
| `bk-pc/src/api/incomeExpense.ts` | 补充 getSummary 接口（如需） |
| `bk-pc/src/types/incomeExpense.ts` | 补充类型定义 |

---

## 八、验收标准

### 8.1 功能验收

- [ ] 页面打开默认加载当月收支记录
- [ ] 分页展示收支记录，每页默认 10 条
- [ ] 点击"新增"弹出记账表单弹窗
- [ ] 填写表单后点击"确定"创建记录成功
- [ ] 点击"编辑"回显数据到表单，修改后保存成功
- [ ] 勾选记录后点击"批量删除"确认删除成功
- [ ] 点击单条记录"删除"确认删除成功
- [ ] 选择筛选条件后点击"查询"过滤数据
- [ ] 支持按备注模糊查询
- [ ] 支持按标签多选筛选
- [ ] 点击"重置"清空筛选条件
- [ ] 表格展示分类名称和图标
- [ ] 表格展示标签
- [ ] 新增/编辑表单支持选择标签（非必填）
- [ ] "是否加入常用备注"checkbox放在备注输入框后面

### 8.2 交互验收

- [ ] 新增/编辑弹窗可正常打开和关闭
- [ ] 表单必填项有校验提示
- [ ] 删除操作有二次确认
- [ ] 操作成功/失败有提示信息
- [ ] 加载中有 loading 状态

### 8.3 性能验收

- [ ] 列表加载 < 1s
- [ ] 表单提交响应 < 1s
- [ ] 页面切换无明显卡顿

---

## 九、技术实现要点

### 9.1 目录结构

参照 `coding_standards.md` 规范：
- API 文件: 小写下划线 `incomeExpense.ts`
- Vue 组件: 大驼峰 `BillList.vue`
- 类型定义: 小写下划线 `bill.ts`

### 9.2 状态管理

使用 Pinia 管理账单数据：

```typescript
// store/modules/bill.ts
export const useBillStore = defineStore('bill', () => {
  const list = ref<IncomeExpense[]>([])
  const total = ref(0)
  const queryParams = ref<IncomeExpenseQuery>({
    page: 1,
    size: 10
  })
  
  async function fetchList() { ... }
  async function create(data: IncomeExpenseForm) { ... }
  async function update(data: IncomeExpenseForm) { ... }
  async function remove(ids: number[]) { ... }
  
  return { list, total, queryParams, fetchList, create, update, remove }
})
```

### 9.3 权限控制

按钮级别权限使用 `v-auth` 指令：

```vue
<el-button v-auth="'bill:add'">新增</el-button>
<el-button v-auth="'bill:edit'">编辑</el-button>
<el-button v-auth="'bill:delete'">删除</el-button>
```