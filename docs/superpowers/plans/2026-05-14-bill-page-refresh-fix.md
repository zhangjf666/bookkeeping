# 账单页面刷新与交互优化 - 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复移动端账单页面从编辑/添加返回时全量刷新的问题，实现本地更新、下拉刷新、筛选栏固定。

**Architecture:** 通过路由 keepAlive 缓存账单页面组件，Store 新增本地增删改方法替代全量刷新，记录页面保存后直接调用本地更新并返回，账单页面使用 van-pull-refresh 做下拉刷新。

**Tech Stack:** Vue 3 + TypeScript + Pinia + Vant 4

---

## 文件变更清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `bk-pc/src/router/modules/bill.ts` | 修改 | 添加 `keepAlive: true` |
| `bk-pc/src/store/modules/bill.ts` | 修改 | 新增 `localAddRecord`, `localUpdateRecord`, `localRemoveRecord`；删除 `needRefresh` 相关 |
| `bk-pc/src/views/mobile/record/index.vue` | 修改 | 保存后调用本地更新方法，不再设置 `needRefresh` |
| `bk-pc/src/views/mobile/bill/index.vue` | 修改 | 删除 `needRefresh` 逻辑，添加下拉刷新，固定筛选栏，调整删除逻辑 |

---

### Task 1: 路由添加 keepAlive

**Files:**
- Modify: `bk-pc/src/router/modules/bill.ts`

- [ ] **Step 1: 给 `/bill-list` 路由添加 `keepAlive: true`**

在 `/bill-list` 路由的 `meta` 中添加 `keepAlive: true`：

```typescript
{
  path: "/bill-list",
  name: "BillList",
  component: () => import("@/components/DeviceAwareView.vue"),
  meta: {
    title: $t("menus.pureBill"),
    pcComponent: () => import("@/views/bill/index.vue"),
    mobileComponent: () => import("@/views/mobile/bill/index.vue"),
    showTabBar: true,
    keepAlive: true
  }
}
```

- [ ] **Step 2: Commit**

```bash
git add bk-pc/src/router/modules/bill.ts
git commit -m "feat: 账单页面路由添加 keepAlive"
```

---

### Task 2: Store 新增本地操作方法

**Files:**
- Modify: `bk-pc/src/store/modules/bill.ts`

- [ ] **Step 1: 新增三个本地操作 action**

在 `actions` 对象中，在 `buildClassifyTree` 方法之后新增以下三个方法：

```typescript
// 本地新增记录（按日期降序插入到正确位置）
localAddRecord(record: IncomeExpense) {
  const insertIndex = this.list.findIndex(
    item => item.date < record.date
  );
  if (insertIndex === -1) {
    this.list.push(record);
  } else {
    this.list.splice(insertIndex, 0, record);
  }
  this.total++;
},
// 本地更新记录
localUpdateRecord(record: IncomeExpense) {
  const index = this.list.findIndex(item => item.id === record.id);
  if (index !== -1) {
    this.list.splice(index, 1, record);
  }
},
// 本地删除记录
localRemoveRecord(id: number) {
  const index = this.list.findIndex(item => item.id === id);
  if (index !== -1) {
    this.list.splice(index, 1);
    this.total--;
  }
}
```

- [ ] **Step 2: 删除不再使用的 needRefresh 相关代码**

1. 在 `state` 中删除 `needRefresh: false`
2. 删除 `setNeedRefresh` action

- [ ] **Step 3: Commit**

```bash
git add bk-pc/src/store/modules/bill.ts
git commit -m "feat: Store 新增本地增删改方法，删除 needRefresh"
```

---

### Task 3: 记录页面保存后本地更新

**Files:**
- Modify: `bk-pc/src/views/mobile/record/index.vue`

- [ ] **Step 1: 新增构造记录数据的方法**

在 `script setup` 中，在 `getTagIdsByCodes` 方法之后新增一个方法：

```typescript
// 构造用于本地更新的 IncomeExpense 对象
const buildLocalRecord = (): IncomeExpense => {
  const mainClassify = classifyList.value.find(
    item => item.id === formData.value.mainClassifyId
  );
  const subClassify = classifyList.value.find(
    item => item.id === formData.value.subClassifyId
  );
  const accountBook = accountBookList.value.find(
    item => item.id === formData.value.accountBookId
  );
  const tagCodes = getTagCodesByIds(formData.value.tagIds);
  const tagNames = tagList.value
    .filter(tag => formData.value.tagIds.includes(tag.id))
    .map(tag => tag.name)
    .join(",");

  return {
    id: isEdit.value ? parseInt(recordId.value) : Date.now(), // 新增时临时 id
    userId: userId.value || 0,
    accountBookId: formData.value.accountBookId!,
    accountBookName: accountBook?.name || "",
    amount: formData.value.amount,
    type: formData.value.type,
    date: formData.value.date,
    remark: formData.value.remark || "",
    mainClassify: formData.value.mainClassifyId!,
    mainClassifyName: mainClassify?.name || "",
    mainClassifyImage: mainClassify?.image || "",
    subClassify: formData.value.subClassifyId || null,
    subClassifyName: subClassify?.name || "",
    subClassifyImage: subClassify?.image || "",
    isCreditCard: formData.value.isCreditCard ? "YES" : "NO",
    tagCodes,
    tagNames,
    createTime: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    updateTime: dayjs().format("YYYY-MM-DD HH:mm:ss")
  } as IncomeExpense;
};
```

- [ ] **Step 2: 修改 handleSave 方法**

将 `handleSave` 方法中保存成功后的逻辑从：

```typescript
// 设置刷新标记，通知账单页面刷新数据
billStore.setNeedRefresh(true);

showSuccess(t("mobile.record.saveSuccess"));
router.back();
```

改为：

```typescript
const record = buildLocalRecord();
if (isEdit.value) {
  billStore.localUpdateRecord(record);
} else {
  billStore.localAddRecord(record);
}

showSuccess(t("mobile.record.saveSuccess"));
router.back();
```

注意：需要确保 `buildLocalRecord` 方法中的类型强制 `as IncomeExpense` 是有效的，因为 `IncomeExpense` 类型可能还有其他字段。如果编译报错，可以补充缺失字段或改用 `Partial<IncomeExpense>` 类型。

- [ ] **Step 3: Commit**

```bash
git add bk-pc/src/views/mobile/record/index.vue
git commit -m "feat: 记录页面保存后本地更新列表，不再全量刷新"
```

---

### Task 4: 账单页面删除逻辑调整

**Files:**
- Modify: `bk-pc/src/views/mobile/bill/index.vue`

- [ ] **Step 1: 修改 handleDelete 方法**

将 `handleDelete` 方法从：

```typescript
const handleDelete = async (id: number) => {
  try {
    await showConfirmDialog({
      message: t("mobile.bill.deleteConfirm"),
      confirmButtonColor: "#d83d34"
    });

    showLoading(t("mobile.common.loading"));
    await deleteIncomeExpense([id]);
    hideLoading();

    showSuccess(t("mobile.bill.deleteSuccess"));
    billStore.list = [];
    await resetAndLoad(fetchBills);
  } catch {
    hideLoading();
  }
};
```

改为：

```typescript
const handleDelete = async (id: number) => {
  try {
    await showConfirmDialog({
      message: t("mobile.bill.deleteConfirm"),
      confirmButtonColor: "#d83d34"
    });

    showLoading(t("mobile.common.loading"));
    await deleteIncomeExpense([id]);
    hideLoading();

    showSuccess(t("mobile.bill.deleteSuccess"));
    // 本地移除，不再全量刷新
    billStore.localRemoveRecord(id);
    // 如果删除后列表为空，重置加载状态（可能筛条件下已没有数据）
    if (billStore.list.length === 0) {
      reset();
    }
  } catch {
    hideLoading();
  }
};
```

- [ ] **Step 2: Commit**

```bash
git add bk-pc/src/views/mobile/bill/index.vue
git commit -m "feat: 账单页面删除改为本地移除，不再全量刷新"
```

---

### Task 5: 账单页面清理 needRefresh 逻辑

**Files:**
- Modify: `bk-pc/src/views/mobile/bill/index.vue`

- [ ] **Step 1: 删除不再需要的导入和变量**

1. 删除导入中的 `onActivated`（如果其他逻辑不需要的话）
2. 删除 `isInitialized` ref（不再需要）
3. 删除 `refreshData` 方法

- [ ] **Step 2: 简化 initData 方法**

将 `initData` 方法简化为只在列表为空时加载：

```typescript
const initData = async () => {
  const userId = userStore.id;
  if (!userId) return;

  // 如果列表已有数据，说明是 keep-alive 缓存返回，不重新加载
  if (billStore.list.length > 0) {
    return;
  }

  showLoading(t("mobile.common.loading"));
  try {
    // 加载分类数据
    if (classifyStore.list.length === 0) {
      await classifyStore.fetchList(userId);
    }
    // 加载标签数据
    if (userTagStore.list.length === 0) {
      await userTagStore.fetchList(userId);
    }
    // 加载备注数据
    if (remarkStore.list.length === 0) {
      await remarkStore.fetchList(userId);
    }
    // 加载账本数据
    if (accountBookStore.list.length === 0) {
      await accountBookStore.fetchList(userId);
    }
    // 设置当前账本
    if (accountBookStore.defaultAccountBook) {
      billStore.setCurrentAccountBook(accountBookStore.defaultAccountBook);
    }
    // 加载账单列表
    await resetAndLoad(fetchBills);
  } catch (error: any) {
    showError(error?.message || t("mobile.common.failed"));
  } finally {
    hideLoading();
  }
};
```

- [ ] **Step 3: 删除 onActivated 钩子**

将原来的 `onActivated` 钩子：

```typescript
// 从子页面返回时激活
onActivated(() => {
  // 如果需要刷新（保存账单后返回）
  if (billStore.needRefresh) {
    billStore.setNeedRefresh(false);
    refreshData();
    return;
  }

  // 不需要重新加载数据，状态已保存在 store 中
});
```

删除或注释掉。保留 `onMounted`：

```typescript
onMounted(() => {
  initData();
});
```

- [ ] **Step 4: Commit**

```bash
git add bk-pc/src/views/mobile/bill/index.vue
git commit -m "feat: 清理账单页面 needRefresh 和 isInitialized 逻辑"
```

---

### Task 6: 账单页面添加下拉刷新

**Files:**
- Modify: `bk-pc/src/views/mobile/bill/index.vue`

- [ ] **Step 1: 添加 refreshing 状态**

在 `script setup` 顶部，在现有 ref 声明区域添加：

```typescript
const refreshing = ref(false);
```

- [ ] **Step 2: 添加 onRefresh 方法**

在 `handleFilterReset` 方法之后添加：

```typescript
// 下拉刷新
const onRefresh = async () => {
  try {
    billStore.list = [];
    await resetAndLoad(fetchBills);
  } finally {
    refreshing.value = false;
  }
};
```

- [ ] **Step 3: 使用 van-pull-refresh 包裹列表**

将模板中的：

```vue
<!-- 账单列表 -->
<van-list
  v-model:loading="loading"
  :finished="finished"
  :finished-text="groupedBills.length > 0 ? t('mobile.common.noMore') : ''"
  :immediate-check="false"
  @load="onLoadMore(fetchBills)"
>
```

改为：

```vue
<!-- 账单列表 -->
<van-pull-refresh v-model="refreshing" @refresh="onRefresh">
  <van-list
    v-model:loading="loading"
    :finished="finished"
    :finished-text="groupedBills.length > 0 ? t('mobile.common.noMore') : ''"
    :immediate-check="false"
    @load="onLoadMore(fetchBills)"
  >
```

并添加闭合标签：

```vue
  </van-list>
</van-pull-refresh>
```

原来 `</van-list>` 后面紧跟的是悬浮按钮，需要在 `</van-pull-refresh>` 之后再放悬浮按钮。

- [ ] **Step 4: Commit**

```bash
git add bk-pc/src/views/mobile/bill/index.vue
git commit -m "feat: 账单页面添加下拉刷新"
```

---

### Task 7: 账单页面固定筛选栏

**Files:**
- Modify: `bk-pc/src/views/mobile/bill/index.vue`

- [ ] **Step 1: 修改 .filter-bar 样式**

将样式中的 `.filter-bar` 从：

```scss
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: $color-card;
  border-bottom: 1px solid $color-border;
}
```

改为：

```scss
.filter-bar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: $color-card;
  border-bottom: 1px solid $color-border;
}
```

- [ ] **Step 2: Commit**

```bash
git add bk-pc/src/views/mobile/bill/index.vue
git commit -m "feat: 账单页面筛选栏固定"
```

---

## Spec 覆盖检查

| Spec 需求 | 对应 Task |
|-----------|-----------|
| 路由添加 keepAlive | Task 1 |
| Store 本地增删改 | Task 2 |
| 记录页面保存后本地更新 | Task 3 |
| 账单页面删除改为本地移除 | Task 4 |
| 清理 needRefresh 逻辑 | Task 5 |
| 下拉刷新 | Task 6 |
| 筛选栏固定 | Task 7 |

---

## 验证步骤

全部修改完成后，启动开发服务器验证：

```bash
cd bk-pc
pnpm dev
```

1. 进入账单页面，加载数据后滚动到中间位置
2. 点击一条记录进入编辑，修改金额/备注，保存返回
3. 验证：页面保持在原来滚动位置，该条记录显示已更新的数据
4. 点击"记一笔"新增一条记录，保存返回
5. 验证：新记录出现在列表正确位置（按日期），页面不刷新
6. 滑动删除一条记录
7. 验证：该记录消失，页面不刷新
8. 在列表顶部下拉
9. 验证：出现下拉刷新动画，数据重新加载
10. 滚动到列表下方
11. 验证：顶部筛选栏始终可见
