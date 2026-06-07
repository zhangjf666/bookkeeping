# 分类筛选选择器实现计划

## 概述

修改账单页面筛选弹窗中的分类选择功能：
1. 将分类选择改为弹窗模式，点击后弹出分类选择器
2. 支持多选顶级分类和子分类
3. 提供"全部支出"和"全部收入"全选按钮

## 文件修改清单

### 1. 新建 ClassifyFilterPicker.vue

**路径**: `D:\WorkSpace\bookkeeping\bk-pc\src\components\mobile\ClassifyFilterPicker.vue`

**功能**:
- 分类多选弹窗组件
- 顶部"全部支出"和"全部收入"全选按钮
- 支持选择顶级分类和子分类
- 点击"确定"按钮确认选择

**Props**:
```typescript
{
  modelValue: boolean;           // 弹窗显示状态
  classifyList: Classify[];      // 分类列表
  selectedClassifies?: Map<number, Set<number | null>>;  // 已选分类
}
```

**Emit**:
```typescript
{
  "update:modelValue": [value: boolean];
  "update:selectedClassifies": [value: Map<number, Set<number | null>>];
}
```

**核心逻辑**:
1. 支出/收入类型切换（类似 ClassifyPicker.vue）
2. 顶级分类网格展示，每行4个
3. 点击顶级分类展开/收起子分类区域
4. 点击顶级分类选中/取消（多选）
5. 点击子分类选中/取消（多选）
6. 全选按钮逻辑
7. 确定按钮返回选中数据

**模板结构**:
```vue
<van-popup>
  <div class="classify-filter-picker">
    <!-- 头部：标题 + 关闭按钮 -->
    <div class="picker-header">...</div>

    <!-- 类型切换：支出/收入 -->
    <div class="type-switch">...</div>

    <!-- 全选按钮：全部支出/全部收入 -->
    <div class="select-all-row">...</div>

    <!-- 分类内容 -->
    <div class="classify-content">
      <!-- 顶级分类网格 -->
      <div class="main-classify-grid">...</div>

      <!-- 子分类展开区域 -->
      <transition>
        <div class="sub-classify-drawer">...</div>
      </transition>
    </div>

    <!-- 底部确定按钮 -->
    <div class="picker-footer">
      <van-button type="danger" block @click="handleConfirm">确定</van-button>
    </div>
  </div>
</van-popup>
```

### 2. 修改 BillFilter.vue

**路径**: `D:\WorkSpace\bookkeeping\bk-pc\src\components\mobile\BillFilter.vue`

**修改内容**:

#### 2.1 导入新组件
```typescript
import ClassifyFilterPicker from "@/components/mobile/ClassifyFilterPicker.vue";
```

#### 2.2 添加分类选择器弹窗状态
```typescript
const showClassifyPicker = ref(false);
```

#### 2.3 添加分类选中数量显示
```typescript
const classifyCount = computed(() => {
  let count = 0;
  selectedClassifies.value.forEach(subSet => {
    count += subSet.size;
  });
  return count;
});
```

#### 2.4 修改模板 - 分类筛选部分
```vue
<!-- 分类筛选 -->
<div class="filter-section">
  <div class="section-label">{{ t("mobile.bill.classifyFilter") }}</div>
  <van-cell is-link @click="showClassifyPicker = true">
    <template #title>
      <span v-if="classifyCount > 0">已选择 {{ classifyCount }} 个分类</span>
      <span v-else class="placeholder">请选择分类</span>
    </template>
  </van-cell>
</div>

<!-- 分类选择器弹窗 -->
<ClassifyFilterPicker
  v-model="showClassifyPicker"
  :classify-list="classifyList"
  :selected-classifies="selectedClassifies"
  @update:selected-classifies="selectedClassifies = $event"
/>
```

#### 2.5 移除以下内容
- 移除 `expenseParents`、`incomeParents` 计算属性
- 移除 `getChildren` 方法
- 移除 `selectAllExpense`、`selectAllIncome` ref
- 移除 `toggleAllExpense`、`toggleAllIncome` 方法
- 移除 `toggleClassify`、`isClassifySelected`、`updateSelectAllState` 方法
- 移除模板中的分类展示区域（全选按钮、支出分类网格、收入分类网格）
- 移除相关样式代码

#### 2.6 保留以下内容
- `selectedClassifies` ref（数据结构不变）
- `handleConfirm` 中分类参数的组装逻辑

### 3. 国际化文件修改

**路径**: `D:\WorkSpace\bookkeeping\bk-pc\locales\mobile\zh-CN.yaml`

添加：
```yaml
classifyFilter:
  selectClassify: 请选择分类
  selectedCount: 已选择 {count} 个分类
```

**路径**: `D:\WorkSpace\bookkeeping\bk-pc\locales\mobile\en.yaml`

添加：
```yaml
classifyFilter:
  selectClassify: Select category
  selectedCount: {count} categories selected
```

## 实现步骤

### 步骤 1: 创建 ClassifyFilterPicker.vue 组件
- 复制 ClassifyPicker.vue 作为基础
- 修改为多选模式
- 添加全选按钮
- 添加确定按钮
- 修改数据结构和事件

### 步骤 2: 修改 BillFilter.vue
- 导入新组件
- 添加弹窗状态
- 修改模板结构
- 移除旧代码

### 步骤 3: 添加国际化文案

### 步骤 4: 测试验证
- 测试分类选择弹窗打开/关闭
- 测试顶级分类选择/取消
- 测试子分类选择/取消
- 测试全选按钮
- 测试确定后数据返回
- 测试筛选查询参数正确性

## 数据结构说明

### 组件返回数据结构
```typescript
// Map<顶级分类ID, Set<子分类ID | null>>
// null 表示选中顶级分类本身
Map<number, Set<number | null>>

// 示例：
// 选择顶级分类 ID=1：Map { 1 => Set { null } }
// 选择子分类 ID=5（父ID=1）：Map { 1 => Set { 5 } }
// 同时选择顶级分类 ID=1 和子分类 ID=5：Map { 1 => Set { null, 5 } }
```

### 接口查询参数转换
```typescript
// BillFilter.vue 中的转换逻辑
const convertClassifyFilter = (selected: Map<number, Set<number | null>>) => {
  const classifyList: { mainClassifyId: number; subClassifyId: number | null }[] = [];
  selected.forEach((subSet, parentId) => {
    subSet.forEach(childId => {
      classifyList.push({
        mainClassifyId: parentId,
        subClassifyId: childId
      });
    });
  });
  return classifyList;
};
```

## 注意事项

1. 保持与 ClassifyPicker.vue 相似的交互体验和视觉风格
2. 选中状态要清晰区分（顶级分类选中 vs 子分类选中）
3. 全选按钮状态要正确反映当前选择情况
4. 弹窗打开时要正确初始化已选状态
