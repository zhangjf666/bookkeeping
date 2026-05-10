# 账单页面刷新问题修复设计文档

## 问题描述

### 现象
1. 从账单页面进入编辑/新增页面，保存后返回，账单页面会重新调用接口刷新数据，导致滚动位置回到顶部
2. 缺少下拉刷新功能，无法主动触发数据刷新
3. 筛选栏随列表滚动，无法在滚动后快速修改筛选条件

### 期望效果
1. 编辑/新增保存后返回账单页面，保持滚动位置，精准更新对应的数据项
2. 支持下拉刷新功能
3. 筛选栏固定在顶部，不随列表滚动

## 解决方案

### 一、路由配置

为账单列表页面添加 `keepAlive: true`，启用组件缓存：

```typescript
// src/router/modules/home.ts
{
  path: "/bill-list",
  name: "BillList",
  meta: {
    keepAlive: true,
    // 其他配置保持不变
  }
}
```

**说明**：keep-alive 只影响移动端，PC端使用独立的缓存机制，不会受到影响。

### 二、Store 修改

在 `src/store/modules/bill.ts` 中添加保存记录的状态和方法：

```typescript
state: {
  // 现有状态保持不变...

  // 新增：最后保存的记录（用于账单页面精准更新）
  lastSavedRecord: null as IncomeExpense | null,
}

actions: {
  // 新增：设置最后保存的记录
  setLastSavedRecord(record: IncomeExpense) {
    this.lastSavedRecord = record;
  },

  // 新增：清除最后保存的记录
  clearLastSavedRecord() {
    this.lastSavedRecord = null;
  },

  // 新增：更新列表中的单条记录
  updateRecordInList(updatedRecord: IncomeExpense) {
    const index = this.list.findIndex(item => item.id === updatedRecord.id);
    if (index !== -1) {
      this.list[index] = { ...this.list[index], ...updatedRecord };
    }
  },

  // 新增：在列表顶部插入新记录
  insertRecordAtTop(newRecord: IncomeExpense) {
    this.list.unshift(newRecord);
  },

  // 新增：从列表中移除记录
  removeRecordFromList(recordId: number) {
    const index = this.list.findIndex(item => item.id === recordId);
    if (index !== -1) {
      this.list.splice(index, 1);
    }
  },
}
```

### 三、账单页面修改

#### 3.1 布局结构调整

将筛选栏固定在顶部，列表区域单独滚动：

```vue
<template>
  <div class="bill-page">
    <!-- 筛选栏：固定在顶部 -->
    <div class="filter-bar">
      <div class="date-selector">...</div>
      <van-button>筛选</van-button>
    </div>

    <!-- 下拉刷新 + 列表区域 -->
    <div class="list-wrapper">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          @load="onLoadMore(fetchBills)"
        >
          <div class="bill-list">...</div>
        </van-list>
      </van-pull-refresh>
    </div>

    <!-- 其他组件保持不变 -->
  </div>
</template>
```

#### 3.2 样式调整

```scss
.bill-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.filter-bar {
  position: sticky;
  top: 0;
  z-index: 10;
  flex-shrink: 0;
  // 其他样式保持不变
}

.list-wrapper {
  flex: 1;
  overflow-y: auto;
  padding-bottom: calc(60px + env(safe-area-inset-bottom));
}
```

#### 3.3 下拉刷新逻辑

```typescript
const refreshing = ref(false);

const onRefresh = async () => {
  billStore.list = [];
  billStore.queryParams.pageNo = 1;
  await resetAndLoad(fetchBills);
  refreshing.value = false;
};
```

#### 3.4 数据更新逻辑

```typescript
// onActivated 中处理保存的数据
onActivated(() => {
  if (billStore.lastSavedRecord) {
    const record = billStore.lastSavedRecord;
    billStore.clearLastSavedRecord();

    // 判断是新增还是编辑（根据记录是否已存在于列表）
    const existingIndex = billStore.list.findIndex(item => item.id === record.id);

    if (existingIndex !== -1) {
      // 编辑：更新列表中的对应记录
      billStore.updateRecordInList(record);
    } else {
      // 新增：插入到列表顶部
      billStore.insertRecordAtTop(record);
    }
  }
});
```

#### 3.5 删除逻辑优化

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
    // 改为精准移除，不刷新整个列表
    billStore.removeRecordFromList(id);
  } catch {
    hideLoading();
  }
};
```

### 四、编辑/新增页面修改

保存成功后，将数据存到 store：

```typescript
const handleSave = async () => {
  // ...保存逻辑

  // 构建保存后的记录数据
  const savedRecord: IncomeExpense = {
    id: isEdit.value ? parseInt(recordId.value) : result.id,
    accountBookId: formData.value.accountBookId!,
    amount: formData.value.amount,
    type: formData.value.type,
    date: formData.value.date,
    remark: formData.value.remark || "",
    mainClassify: formData.value.mainClassifyId!,
    subClassify: formData.value.subClassifyId || null,
    mainClassifyName: selectedClassifyName.value.split('-')[0],
    subClassifyName: formData.value.subClassifyId
      ? selectedClassifyName.value.split('-')[1]
      : null,
    mainClassifyImage: formData.value.mainClassifyId
      ? classifyList.value.find(c => c.id === formData.value.mainClassifyId)?.image
      : null,
    subClassifyImage: formData.value.subClassifyId
      ? classifyList.value.find(c => c.id === formData.value.subClassifyId)?.image
      : null,
    tagCodes: getTagCodesByIds(formData.value.tagIds),
    isCreditCard: formData.value.isCreditCard ? "YES" : "NO",
  };

  // 设置保存的记录（不再设置 needRefresh）
  billStore.setLastSavedRecord(savedRecord);

  showSuccess(t("mobile.record.saveSuccess"));
  router.back();
};
```

### 五、首页处理

首页每次激活时刷新数据（统计数据必须从接口获取）：

```typescript
onActivated(() => {
  // 每次激活都刷新数据
  loadData();
});
```

**说明**：首页包含统计卡片（本月支出、本月收入、结余等），这些数据需要从接口计算获取，不能通过列表操作来更新，所以每次返回都需要刷新。

### 六、各页面处理逻辑总结

| 页面 | 操作 | 处理方式 |
|------|------|----------|
| 账单页面 | 编辑保存返回 | 精准更新列表中对应记录 |
| 账单页面 | 新增保存返回 | 插入到列表顶部 |
| 账单页面 | 删除 | 从列表中移除对应记录 |
| 账单页面 | 下拉刷新 | 重新加载第一页数据 |
| 首页 | 编辑/新增/删除返回 | 刷新接口获取最新统计数据 |

## 修改文件清单

1. `src/router/modules/home.ts` - 添加 keepAlive 配置
2. `src/store/modules/bill.ts` - 添加保存记录状态和方法
3. `src/views/mobile/bill/index.vue` - 固定筛选栏、下拉刷新、精准更新逻辑
4. `src/views/mobile/record/index.vue` - 保存后设置数据实体而非刷新标记
5. `src/views/mobile/home/index.vue` - onActivated 刷新数据

## 注意事项

1. 新增记录的 ID 需要从接口返回获取
2. 编辑记录如果日期改变，分组显示会自动重新计算（因为 groupedBills 是 computed）
3. 删除记录后，如果某日期分组没有记录了，分组会自动消失（computed 特性）