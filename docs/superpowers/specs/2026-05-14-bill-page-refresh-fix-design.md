# 账单页面刷新与交互优化 - 设计文档

## 背景

移动端账单页面（`/bill-list`）当前存在以下问题：
1. 从编辑/添加页面返回时，页面会重新调用接口全量刷新，导致用户滚动位置丢失，无法继续浏览
2. 缺少下拉刷新能力
3. 顶部筛选栏随列表滚动，用户滚动到下方后无法直接修改筛选条件

## 目标

1. 从添加/编辑页面返回账单页面时，不重新调用接口，保持原有滚动位置
2. 编辑保存后，仅更新列表中对应记录，不刷新整个列表
3. 新增保存后，将新记录插入列表正确位置
4. 为账单页面添加顶部下拉刷新功能
5. 固定顶部筛选栏，使其不随列表滚动

## 技术约束

- Vue 3 + TypeScript + Vite
- Vant 4 移动端 UI 组件库
- Pinia 状态管理
- `van-list` 做无限滚动加载
- `useInfiniteScroll` composable 管理加载状态

---

## 方案设计

### 1. 路由添加 keepAlive

**问题根因：** 当前 `/bill-list` 路由未设置 `keepAlive: true`，导致每次进入都会重新挂载组件，触发 `onMounted` → `initData()` 全量刷新。

**修改：** 在 `router/modules/bill.ts` 中给 `/bill-list` 路由添加 `keepAlive: true`。

### 2. Store 新增本地更新方法

在 `store/modules/bill.ts` 中新增三个 action：

- **`localAddRecord(record: IncomeExpense)`**
  - 将新记录按日期降序插入到列表正确位置
  - 使用二分查找或遍历找到第一个 `date <= record.date` 的位置插入

- **`localUpdateRecord(record: IncomeExpense)`**
  - 在列表中找到 `id === record.id` 的项并替换
  - 同时更新 `total`（如有需要）

- **`localRemoveRecord(id: number)`**
  - 从列表中过滤掉对应 id 的记录
  - 同时减少 `total`

### 3. 记录页面保存逻辑调整

**修改 `views/mobile/record/index.vue`：**

保存成功后（`handleSave` 方法中）：
1. 不再调用 `billStore.setNeedRefresh(true)`
2. 构造完整的 `IncomeExpense` 对象，包含所有展示字段：
   - `mainClassifyName` / `subClassifyName`：从 classifyStore 查找
   - `mainClassifyImage` / `subClassifyImage`：从 classifyStore 查找并使用 `getClassifyIcon`
   - `tagNames`：从 tagStore 查找
   - 其他字段直接使用表单数据
3. 如果是编辑模式，调用 `billStore.localUpdateRecord(record)`
4. 如果是新增模式，调用 `billStore.localAddRecord(record)`
5. 然后 `router.back()` 返回

### 4. 账单页面逻辑调整

**修改 `views/mobile/bill/index.vue`：**

- **删除** `needRefresh` 相关逻辑
- **删除** `refreshData` 方法
- `onMounted`：只在列表为空时初始化加载（首次进入）
- `onActivated`：空实现或完全删除，不做任何操作
- 删除操作 `handleDelete`：调用 API 删除成功后，调用 `billStore.localRemoveRecord(id)`，不再全量刷新
- 引入 `van-pull-refresh` 组件做下拉刷新

### 5. 下拉刷新实现

使用 `van-pull-refresh` 包裹 `van-list`：

```vue
<van-pull-refresh v-model="refreshing" @refresh="onRefresh">
  <van-list ...>
    <!-- 列表内容 -->
  </van-list>
</van-pull-refresh>
```

`onRefresh` 方法调用 `resetAndLoad(fetchBills)`，完成后设置 `refreshing = false`。

### 6. 筛选栏固定

给 `.filter-bar` 添加 CSS：

```scss
.filter-bar {
  position: sticky;
  top: 0;
  z-index: 10;
}
```

当前布局滚动在 body 上，sticky 定位可以直接生效。

---

## 滚动位置保持机制

1. 路由添加 `keepAlive: true` 后，Vue 的 `keep-alive` 会缓存组件实例
2. `useInfiniteScroll` 的状态（`pageNo`, `finished` 等）也得以保持
3. 浏览器自身的滚动位置在 `router.back()` 时由 Vue Router 的 `scrollBehavior` 恢复
4. 由于返回时不做全量刷新，DOM 不会发生大幅变化，滚动位置自然保持

---

## 数据一致性边界

- **编辑后本地更新**：前端直接用表单数据更新列表项，分类名称、图标等展示字段从 store 中获取，与保存时一致
- **新增后插入**：前端构造的记录可能没有服务端生成的某些字段（如创建时间），但核心展示字段齐全
- **删除后移除**：列表中移除对应项，UI 即时响应
- **如需强一致性**：用户可主动下拉刷新获取服务端最新数据

---

## 涉及文件

| 文件 | 操作 | 说明 |
|------|------|------|
| `router/modules/bill.ts` | 修改 | 添加 `keepAlive: true` |
| `store/modules/bill.ts` | 修改 | 新增 `localAddRecord`, `localUpdateRecord`, `localRemoveRecord` |
| `views/mobile/record/index.vue` | 修改 | 保存后调用本地更新，不再设置 needRefresh |
| `views/mobile/bill/index.vue` | 修改 | 删除 needRefresh 逻辑，添加 PullRefresh，调整删除逻辑，固定筛选栏 |
