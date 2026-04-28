<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { Classify } from "@/types/classify";
import { getClassifyIcon } from "@/utils/classifyIcons";

defineOptions({
  name: "ClassifyPicker"
});

const props = defineProps<{
  modelValue: boolean;
  classifyList: Classify[];
  type?: "EXPENSE" | "INCOME";
  mainClassifyId: number | null;
  subClassifyId: number | null;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: boolean];
  select: [mainClassify: Classify, subClassify?: Classify];
}>();

const { t } = useI18n();

const show = computed({
  get: () => props.modelValue,
  set: val => emit("update:modelValue", val)
});

// 当前展开的顶级分类ID
const expandedMainId = ref<number | null>(null);

// 当前选中的类型（仅当未指定 type 时使用）
const currentType = ref<"EXPENSE" | "INCOME">("EXPENSE");

// 是否显示类型切换（当未指定 type 时显示）
const showTypeSwitch = computed(() => !props.type);

// 实际过滤类型
const filterType = computed(() => props.type || currentType.value);

// 顶级分类列表（根据类型过滤）
const mainClassifyList = computed(() => {
  return props.classifyList.filter(
    item =>
      item.type === filterType.value &&
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

// 点击顶级分类
const handleMainClick = (mainClassify: Classify) => {
  const childrenExist = hasChildren(mainClassify.id);

  if (childrenExist) {
    // 如果点击的是当前展开的，收起并选中该顶级分类
    if (expandedMainId.value === mainClassify.id) {
      expandedMainId.value = null;
      emit("select", mainClassify);
      show.value = false;
    } else {
      // 展开新的顶级分类
      expandedMainId.value = mainClassify.id;
    }
  } else {
    // 没有子分类，直接选中并关闭
    emit("select", mainClassify);
    show.value = false;
  }
};

// 点击子分类
const handleSubClick = (mainClassify: Classify, subClassify: Classify) => {
  emit("select", mainClassify, subClassify);
  show.value = false;
};

// 点击遮罩层关闭
const handleClose = () => {
  // 只有当用户展开了子分类，且当前没有已选中的子分类属于这个展开的顶级分类时
  // 才选中当前展开的顶级分类
  if (expandedMainId.value) {
    // 检查是否已有属于该顶级分类的子分类被选中
    const hasSelectedSub =
      props.subClassifyId &&
      props.classifyList.some(
        item =>
          item.id === props.subClassifyId && item.pid === expandedMainId.value
      );

    // 如果没有已选中的子分类属于这个顶级分类，才选中顶级分类
    if (!hasSelectedSub) {
      const mainClassify = mainClassifyList.value.find(
        item => item.id === expandedMainId.value
      );
      if (mainClassify) {
        emit("select", mainClassify);
      }
    }
  }
};

// 类型切换
const handleTypeChange = (type: "EXPENSE" | "INCOME") => {
  currentType.value = type;
  expandedMainId.value = null;
};

// 弹窗打开时，根据已选中的分类初始化展开状态
watch(show, val => {
  if (val) {
    // 如果有选中的分类，根据分类类型初始化 currentType
    if (props.mainClassifyId) {
      const mainClassify = props.classifyList.find(
        item => item.id === props.mainClassifyId
      );
      if (mainClassify && showTypeSwitch.value) {
        currentType.value = mainClassify.type as "EXPENSE" | "INCOME";
      }
    }

    // 如果有选中的子分类，展开对应的顶级分类
    if (props.subClassifyId) {
      const subClassify = props.classifyList.find(
        item => item.id === props.subClassifyId
      );
      if (subClassify && subClassify.pid) {
        expandedMainId.value = subClassify.pid;
      }
    } else if (props.mainClassifyId) {
      // 只有顶级分类选中，检查是否有子分类，如果有则展开
      const childrenExist = hasChildren(props.mainClassifyId);
      if (childrenExist) {
        expandedMainId.value = props.mainClassifyId;
      } else {
        expandedMainId.value = null;
      }
    } else {
      expandedMainId.value = null;
    }
  } else {
    expandedMainId.value = null;
  }
});
</script>

<template>
  <van-popup
    v-model:show="show"
    position="bottom"
    round
    style="height: 60%"
    @close="handleClose"
  >
    <div class="classify-picker">
      <!-- 头部 -->
      <div class="picker-header">
        <span class="title">{{ t("mobile.record.classify") }}</span>
        <van-icon name="cross" @click="show = false" />
      </div>

      <!-- 类型切换（仅当未指定 type 时显示） -->
      <div v-if="showTypeSwitch" class="type-switch">
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

      <!-- 分类内容 -->
      <div class="classify-content">
        <!-- 按行渲染顶级分类 -->
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
                active: mainClassifyId === mainClassify.id && !subClassifyId,
                'has-sub-active':
                  mainClassifyId === mainClassify.id && subClassifyId,
                expanded: expandedMainId === mainClassify.id
              }"
              @click="handleMainClick(mainClassify)"
            >
              <span class="main-icon">{{
                getClassifyIcon(mainClassify.image)
              }}</span>
              <span class="main-name">{{ mainClassify.name }}</span>
            </div>
          </div>

          <!-- 子分类展开区域（在该行下方弹出） -->
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
                :class="{ active: subClassifyId === subClassify.id }"
                @click="
                  handleSubClick(
                    mainClassifyList.find(c => c.id === expandedMainId)!,
                    subClassify
                  )
                "
              >
                <span class="sub-icon">{{
                  getClassifyIcon(subClassify.image)
                }}</span>
                <span class="sub-name">{{ subClassify.name }}</span>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </div>
  </van-popup>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.classify-picker {
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
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: center;
  justify-content: center;
  padding: 12px 8px;
  cursor: pointer;
  background-color: $color-background;
  border: 2px solid transparent;
  border-radius: 12px;
  transition: all 0.2s;

  &.active {
    background-color: rgba($color-primary, 0.15);
    border-color: $color-primary;

    .main-icon {
      transform: scale(1.1);
    }
  }

  &.has-sub-active {
    background-color: rgba($color-primary, 0.1);
    border-color: rgba($color-primary, 0.5);
  }

  &.expanded {
    background-color: rgba($color-primary, 0.15);
    border-color: $color-primary;
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

    .sub-icon {
      transform: scale(1.1);
    }
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
