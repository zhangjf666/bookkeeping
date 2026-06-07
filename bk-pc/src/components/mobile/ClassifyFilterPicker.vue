<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { Classify } from "@/types/classify";
import { getClassifyIcon } from "@/utils/classifyIcons";

defineOptions({
  name: "ClassifyFilterPicker"
});

const props = defineProps<{
  modelValue: boolean;
  classifyList: Classify[];
  selectedClassifies?: Map<number, Set<number | null>>;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: boolean];
  "update:selectedClassifies": [value: Map<number, Set<number | null>>];
}>();

const { t } = useI18n();

const show = computed({
  get: () => props.modelValue,
  set: val => emit("update:modelValue", val)
});

// 当前展开的顶级分类ID
const expandedMainId = ref<number | null>(null);

// 当前选中的类型
const currentType = ref<"EXPENSE" | "INCOME">("EXPENSE");

// 本地选中的分类
const localSelectedClassifies = ref<Map<number, Set<number | null>>>(new Map());

// 顶级分类列表（根据类型过滤）
const mainClassifyList = computed(() => {
  return props.classifyList.filter(
    item =>
      item.type === currentType.value &&
      item.enable === "YES" &&
      (item.pid === 0 || item.pid === -1 || item.pid === null || !item.pid)
  );
});

// 将顶级分类按每行4个分组
const mainClassifyRows = computed(() => {
  const rows: Classify[][] = [];
  const items = mainClassifyList.value;
  for (let i = 0; i < items.length; i += 4) {
    rows.push(items.slice(i, i + 4));
  }
  return rows;
});

// 获取某个顶级分类的子分类列表
const getSubList = (mainId: number) => {
  return props.classifyList.filter(
    item => item.pid === mainId && item.enable === "YES"
  );
};

// 检查顶级分类是否有子分类
const hasChildren = (mainId: number) => {
  return props.classifyList.some(
    item => item.pid === mainId && item.enable === "YES"
  );
};

// 全选状态
const selectAllCurrentType = computed(() => {
  const parents = mainClassifyList.value;
  if (parents.length === 0) return false;

  return parents.every(parent => {
    const subSet = localSelectedClassifies.value.get(parent.id);
    if (!subSet) return false;
    // 检查是否选中了顶级分类本身
    if (!subSet.has(null)) return false;
    // 检查是否选中了所有子分类
    const children = getSubList(parent.id);
    return children.every(child => subSet.has(child.id));
  });
});

// 切换类型
const handleTypeChange = (type: "EXPENSE" | "INCOME") => {
  currentType.value = type;
  expandedMainId.value = null;
};

// 切换全选当前类型
const toggleSelectAll = () => {
  const parents = mainClassifyList.value;

  if (selectAllCurrentType.value) {
    // 取消全选
    parents.forEach(parent => {
      localSelectedClassifies.value.delete(parent.id);
    });
  } else {
    // 全选
    parents.forEach(parent => {
      const subSet = new Set<number | null>();
      subSet.add(null); // 选中顶级分类本身
      getSubList(parent.id).forEach(child => {
        subSet.add(child.id);
      });
      localSelectedClassifies.value.set(parent.id, subSet);
    });
  }
};

// 点击顶级分类
const handleMainClick = (mainClassify: Classify) => {
  const childrenExist = hasChildren(mainClassify.id);

  if (childrenExist) {
    // 如果点击的是当前展开的，收起
    if (expandedMainId.value === mainClassify.id) {
      expandedMainId.value = null;
    } else {
      // 展开新的顶级分类
      expandedMainId.value = mainClassify.id;
    }
  }
};

// 切换顶级分类选中状态
const toggleMainSelect = (mainClassify: Classify, event: Event) => {
  event.stopPropagation();

  let subSet = localSelectedClassifies.value.get(mainClassify.id);
  const hasSub = subSet && subSet.has(null);

  if (hasSub) {
    // 取消选中顶级分类及其所有子分类
    localSelectedClassifies.value.delete(mainClassify.id);
  } else {
    // 选中顶级分类及其所有子分类
    if (!subSet) {
      subSet = new Set();
      localSelectedClassifies.value.set(mainClassify.id, subSet);
    } else {
      subSet.clear();
    }
    subSet.add(null);
    getSubList(mainClassify.id).forEach(child => {
      subSet!.add(child.id);
    });
  }
};

// 切换子分类选中状态
const toggleSubSelect = (mainClassify: Classify, subClassify: Classify) => {
  let subSet = localSelectedClassifies.value.get(mainClassify.id);
  if (!subSet) {
    subSet = new Set();
    localSelectedClassifies.value.set(mainClassify.id, subSet);
  }

  if (subSet.has(subClassify.id)) {
    subSet.delete(subClassify.id);
    if (subSet.size === 0) {
      localSelectedClassifies.value.delete(mainClassify.id);
    }
  } else {
    subSet.add(subClassify.id);
  }
};

// 检查顶级分类是否选中
const isMainSelected = (mainId: number) => {
  const subSet = localSelectedClassifies.value.get(mainId);
  return subSet?.has(null) ?? false;
};

// 检查顶级分类是否有子分类被选中
const hasSubSelected = (mainId: number) => {
  const subSet = localSelectedClassifies.value.get(mainId);
  if (!subSet) return false;
  // 有子分类被选中，但顶级分类本身没被选中
  let hasChild = false;
  subSet.forEach(id => {
    if (id !== null) hasChild = true;
  });
  return hasChild && !subSet.has(null);
};

// 检查子分类是否选中
const isSubSelected = (parentId: number, subId: number) => {
  const subSet = localSelectedClassifies.value.get(parentId);
  return subSet?.has(subId) ?? false;
};

// 确认选择
const handleConfirm = () => {
  emit("update:selectedClassifies", new Map(localSelectedClassifies.value));
  show.value = false;
};

// 弹窗打开时初始化选中状态
watch(show, val => {
  if (val) {
    // 复制外部传入的选中状态
    if (props.selectedClassifies) {
      localSelectedClassifies.value = new Map();
      props.selectedClassifies.forEach((subSet, parentId) => {
        localSelectedClassifies.value.set(parentId, new Set(subSet));
      });
    } else {
      localSelectedClassifies.value = new Map();
    }
    expandedMainId.value = null;
  }
});
</script>

<template>
  <van-popup
    v-model:show="show"
    position="bottom"
    round
    style="height: 70%"
  >
    <div class="classify-filter-picker">
      <!-- 头部 -->
      <div class="picker-header">
        <span class="title">{{ t("mobile.bill.classifyFilter") }}</span>
        <van-icon name="cross" @click="show = false" />
      </div>

      <!-- 类型切换 -->
      <div class="type-switch">
        <div
          class="type-btn expense"
          :class="{ active: currentType === 'EXPENSE' }"
          @click="handleTypeChange('EXPENSE')"
        >
          {{ t("mobile.record.expense") }}
        </div>
        <div
          class="type-btn income"
          :class="{ active: currentType === 'INCOME' }"
          @click="handleTypeChange('INCOME')"
        >
          {{ t("mobile.record.income") }}
        </div>
      </div>

      <!-- 全选按钮 -->
      <div class="select-all-row">
        <div
          class="select-all-btn"
          :class="{ active: selectAllCurrentType }"
          @click="toggleSelectAll"
        >
          <span class="icon">{{ getClassifyIcon("other") }}</span>
          <span class="text">{{
            currentType === "EXPENSE"
              ? t("mobile.bill.allExpense")
              : t("mobile.bill.allIncome")
          }}</span>
          <van-icon v-if="selectAllCurrentType" name="success" class="check" />
        </div>
      </div>

      <!-- 分类内容 -->
      <div class="classify-content">
        <div
          v-for="(row, rowIndex) in mainClassifyRows"
          :key="rowIndex"
          class="classify-row"
        >
          <!-- 顶级分类网格 -->
          <div class="main-classify-grid">
            <div
              v-for="mainClassify in row"
              :key="mainClassify.id"
              class="main-classify-item"
              :class="{
                active: isMainSelected(mainClassify.id),
                'has-sub-active': hasSubSelected(mainClassify.id),
                expanded: expandedMainId === mainClassify.id
              }"
              @click="handleMainClick(mainClassify)"
            >
              <div class="item-content">
                <span class="main-icon">{{
                  getClassifyIcon(mainClassify.image)
                }}</span>
                <span class="main-name">{{ mainClassify.name }}</span>
              </div>
              <div class="select-icon" @click="toggleMainSelect(mainClassify, $event)">
                <van-icon
                  :name="isMainSelected(mainClassify.id) ? 'success' : 'circle'"
                />
              </div>
            </div>
          </div>

          <!-- 子分类展开区域 -->
          <transition name="drawer">
            <div
              v-if="
                row.some(item => item.id === expandedMainId) &&
                getSubList(expandedMainId!).length > 0
              "
              class="sub-classify-drawer"
            >
              <div
                v-for="subClassify in getSubList(expandedMainId!)"
                :key="subClassify.id"
                class="sub-classify-item"
                :class="{ active: isSubSelected(expandedMainId!, subClassify.id) }"
                @click="toggleSubSelect(
                  mainClassifyList.find(c => c.id === expandedMainId)!,
                  subClassify
                )"
              >
                <span class="sub-icon">{{
                  getClassifyIcon(subClassify.image)
                }}</span>
                <span class="sub-name">{{ subClassify.name }}</span>
                <van-icon
                  v-if="isSubSelected(expandedMainId!, subClassify.id)"
                  name="success"
                  class="check"
                />
              </div>
            </div>
          </transition>
        </div>
      </div>

      <!-- 底部确定按钮 -->
      <div class="picker-footer">
        <van-button type="danger" block @click="handleConfirm">
          {{ t("mobile.common.confirm") }}
        </van-button>
      </div>
    </div>
  </van-popup>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.classify-filter-picker {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding-bottom: env(safe-area-inset-bottom);
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  border-bottom: 1px solid $color-border;

  .title {
    font-size: 16px;
    font-weight: 500;
    color: $color-text-primary;
  }
}

.type-switch {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background-color: $color-card;

  .type-btn {
    flex: 1;
    padding: 10px 0;
    font-size: 14px;
    font-weight: 500;
    text-align: center;
    cursor: pointer;
    border: 1px solid $color-border;
    border-radius: 8px;
    transition: all 0.2s;

    &.expense {
      color: $color-text-secondary;

      &.active {
        color: #fff;
        background-color: $color-primary;
        border-color: $color-primary;
      }
    }

    &.income {
      color: $color-text-secondary;

      &.active {
        color: #fff;
        background-color: $color-secondary;
        border-color: $color-secondary;
      }
    }
  }
}

.select-all-row {
  padding: 12px 16px;
}

.select-all-btn {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
  padding: 12px 8px;
  background-color: $color-background;
  border: 2px solid transparent;
  border-radius: 8px;

  &.active {
    background-color: rgba($color-primary, 0.1);
    border-color: $color-primary;
  }

  .icon {
    font-size: 20px;
  }

  .text {
    font-size: 12px;
    color: $color-text-primary;
  }

  .check {
    position: absolute;
    top: 4px;
    right: 4px;
    font-size: 14px;
    color: $color-primary;
  }
}

.classify-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.classify-row {
  margin-bottom: 12px;
}

.main-classify-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.main-classify-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  cursor: pointer;
  background-color: $color-background;
  border: 2px solid transparent;
  border-radius: 12px;
  transition: all 0.2s;

  &.active {
    background-color: rgba($color-primary, 0.15);
    border-color: $color-primary;
  }

  &.has-sub-active {
    background-color: rgba($color-primary, 0.1);
    border-color: rgba($color-primary, 0.5);
  }

  &.expanded {
    background-color: rgba($color-primary, 0.15);
    border-color: $color-primary;
  }

  .item-content {
    display: flex;
    flex-direction: column;
    gap: 6px;
    align-items: center;
  }

  .main-icon {
    font-size: 28px;
    transition: transform 0.2s;
  }

  .main-name {
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: 13px;
    color: $color-text-primary;
    text-align: center;
    white-space: nowrap;
  }

  .select-icon {
    position: absolute;
    top: 4px;
    right: 4px;
    font-size: 16px;
    color: $color-primary;
  }
}

.sub-classify-drawer {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px;
  margin-top: 8px;
  background-color: rgba($color-primary, 0.05);
  border-radius: 10px;
}

.sub-classify-item {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
  justify-content: center;
  width: 65px;
  padding: 8px 4px;
  cursor: pointer;
  background-color: $color-card;
  border: 2px solid transparent;
  border-radius: 8px;
  transition: all 0.2s;

  &.active {
    background-color: rgba($color-primary, 0.15);
    border-color: $color-primary;
  }

  &:active {
    opacity: 0.8;
    transform: scale(0.95);
  }

  .sub-icon {
    font-size: 20px;
    color: $color-text-primary;
    transition: all 0.2s;
  }

  .sub-name {
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: 11px;
    color: $color-text-primary;
    text-align: center;
    white-space: nowrap;
    transition: color 0.2s;
  }

  .check {
    position: absolute;
    top: 2px;
    right: 2px;
    font-size: 12px;
    color: $color-primary;
  }
}

.picker-footer {
  padding: 12px 16px;
  border-top: 1px solid $color-border;
}

// 抽屉展开动画
.drawer-enter-active,
.drawer-leave-active {
  transition: all 0.3s ease;
}

.drawer-enter-from,
.drawer-leave-to {
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
  margin-top: 0;
  opacity: 0;
}

.drawer-enter-to,
.drawer-leave-from {
  max-height: 150px;
  opacity: 1;
}
</style>
