<script setup lang="ts">
import { ref, computed } from "vue";
import { useI18n } from "vue-i18n";
import dayjs from "dayjs";
import type { Classify } from "@/types/classify";
import type { UserTag } from "@/types/userTag";
import type { UserRemark } from "@/types/remark";
import { getClassifyIcon } from "@/utils/classifyIcons";

defineOptions({
  name: "BillFilter"
});

const props = defineProps<{
  classifyList: Classify[];
  tagList: UserTag[];
  remarkList: UserRemark[];
}>();

const emit = defineEmits<{
  confirm: [filters: FilterParams];
  reset: [];
}>();

const { t } = useI18n();

interface FilterParams {
  date?: string[];
  amount?: number[];
  classifyList?: { mainClassifyId: number; subClassifyId: number | null }[];
  remark?: string;
  tagCodes?: string[];
}

// 筛选条件
const dateRange = ref<[string, string] | null>(null);
const minAmount = ref<number | null>(null);
const maxAmount = ref<number | null>(null);
const selectedClassifies = ref<Map<number, Set<number | null>>>(new Map());
const remark = ref("");
const selectedTagCodes = ref<Set<string>>(new Set());

// 日期选择器
const showDatePicker = ref(false);

// 分类选择相关
const selectAllExpense = ref(false);
const selectAllIncome = ref(false);

// 备注搜索
const remarkSearch = ref("");

// 标签搜索
const tagSearch = ref("");

// 支出顶级分类
const expenseParents = computed(() => {
  return props.classifyList.filter(
    item =>
      (item.pid === -1 || item.pid === 0 || item.pid === null) &&
      item.type === "EXPENSE"
  );
});

// 收入顶级分类
const incomeParents = computed(() => {
  return props.classifyList.filter(
    item =>
      (item.pid === -1 || item.pid === 0 || item.pid === null) &&
      item.type === "INCOME"
  );
});

// 获取子分类
const getChildren = (parentId: number) => {
  return props.classifyList.filter(item => item.pid === parentId);
};

// 过滤后的备注列表
const filteredRemarks = computed(() => {
  if (!remarkSearch.value) return props.remarkList;
  const keyword = remarkSearch.value.toLowerCase();
  return props.remarkList.filter(item =>
    item.remark.toLowerCase().includes(keyword)
  );
});

// 过滤后的标签列表
const filteredTags = computed(() => {
  if (!tagSearch.value) return props.tagList;
  const keyword = tagSearch.value.toLowerCase();
  return props.tagList.filter(item =>
    item.name.toLowerCase().includes(keyword)
  );
});

// 日期显示文本
const dateDisplay = computed(() => {
  if (!dateRange.value) return "";
  return `${dateRange.value[0]} 至 ${dateRange.value[1]}`;
});

// 金额显示文本
const amountDisplay = computed(() => {
  if (minAmount.value === null && maxAmount.value === null) return "";
  const min = minAmount.value !== null ? minAmount.value.toFixed(2) : "0";
  const max = maxAmount.value !== null ? maxAmount.value.toFixed(2) : "∞";
  return `${min} - ${max}`;
});

// 分类选中数量
const classifyCount = computed(() => {
  let count = 0;
  selectedClassifies.value.forEach(subSet => {
    count += subSet.size;
  });
  return count;
});

// 是否有筛选条件
const hasFilter = computed(() => {
  return (
    dateRange.value !== null ||
    minAmount.value !== null ||
    maxAmount.value !== null ||
    classifyCount.value > 0 ||
    remark.value !== "" ||
    selectedTagCodes.value.size > 0
  );
});

// 日期确认
const handleDateConfirm = (values: Date[]) => {
  dateRange.value = [
    dayjs(values[0]).format("YYYY-MM-DD"),
    dayjs(values[1]).format("YYYY-MM-DD")
  ];
  showDatePicker.value = false;
};

// 切换全选支出
const toggleAllExpense = () => {
  if (selectAllExpense.value) {
    // 取消全选
    expenseParents.value.forEach(parent => {
      selectedClassifies.value.delete(parent.id);
    });
    selectAllExpense.value = false;
  } else {
    // 全选
    expenseParents.value.forEach(parent => {
      const subSet = new Set<number | null>();
      subSet.add(null); // null 表示选中顶级分类本身
      getChildren(parent.id).forEach(child => {
        subSet.add(child.id);
      });
      selectedClassifies.value.set(parent.id, subSet);
    });
    selectAllExpense.value = true;
  }
};

// 切换全选收入
const toggleAllIncome = () => {
  if (selectAllIncome.value) {
    incomeParents.value.forEach(parent => {
      selectedClassifies.value.delete(parent.id);
    });
    selectAllIncome.value = false;
  } else {
    incomeParents.value.forEach(parent => {
      const subSet = new Set<number | null>();
      subSet.add(null);
      getChildren(parent.id).forEach(child => {
        subSet.add(child.id);
      });
      selectedClassifies.value.set(parent.id, subSet);
    });
    selectAllIncome.value = true;
  }
};

// 切换分类选中
const toggleClassify = (parent: Classify, childId: number | null = null) => {
  let subSet = selectedClassifies.value.get(parent.id);
  if (!subSet) {
    subSet = new Set();
    selectedClassifies.value.set(parent.id, subSet);
  }

  if (subSet.has(childId)) {
    subSet.delete(childId);
    if (subSet.size === 0) {
      selectedClassifies.value.delete(parent.id);
    }
  } else {
    subSet.add(childId);
  }

  // 更新全选状态
  updateSelectAllState();
};

// 检查分类是否选中
const isClassifySelected = (
  parentId: number,
  childId: number | null = null
) => {
  const subSet = selectedClassifies.value.get(parentId);
  return subSet?.has(childId) ?? false;
};

// 更新全选状态
const updateSelectAllState = () => {
  // 检查支出是否全选
  const expenseSelected = expenseParents.value.every(parent => {
    const subSet = selectedClassifies.value.get(parent.id);
    if (!subSet) return false;
    const children = getChildren(parent.id);
    return subSet.has(null) && children.every(child => subSet.has(child.id));
  });
  selectAllExpense.value = expenseSelected;

  // 检查收入是否全选
  const incomeSelected = incomeParents.value.every(parent => {
    const subSet = selectedClassifies.value.get(parent.id);
    if (!subSet) return false;
    const children = getChildren(parent.id);
    return subSet.has(null) && children.every(child => subSet.has(child.id));
  });
  selectAllIncome.value = incomeSelected;
};

// 切换标签选中
const toggleTag = (code: string) => {
  if (selectedTagCodes.value.has(code)) {
    selectedTagCodes.value.delete(code);
  } else {
    selectedTagCodes.value.add(code);
  }
};

// 选择备注
const selectRemark = (text: string) => {
  remark.value = text;
};

// 重置
const handleReset = () => {
  dateRange.value = null;
  minAmount.value = null;
  maxAmount.value = null;
  selectedClassifies.value = new Map();
  remark.value = "";
  selectedTagCodes.value = new Set();
  selectAllExpense.value = false;
  selectAllIncome.value = false;
  remarkSearch.value = "";
  tagSearch.value = "";
  emit("reset");
};

// 确认
const handleConfirm = () => {
  const params: FilterParams = {};

  // 日期范围
  if (dateRange.value) {
    params.date = [
      dayjs(dateRange.value[0]).startOf("day").format("YYYY-MM-DD HH:mm:ss"),
      dayjs(dateRange.value[1]).endOf("day").format("YYYY-MM-DD HH:mm:ss")
    ];
  }

  // 金额范围
  if (minAmount.value !== null || maxAmount.value !== null) {
    params.amount = [minAmount.value ?? 0, maxAmount.value ?? 999999999];
  }

  // 分类
  if (selectedClassifies.value.size > 0) {
    const classifyArr: {
      mainClassifyId: number;
      subClassifyId: number | null;
    }[] = [];
    selectedClassifies.value.forEach((subSet, parentId) => {
      subSet.forEach(childId => {
        classifyArr.push({
          mainClassifyId: parentId,
          subClassifyId: childId
        });
      });
    });
    params.classifyList = classifyArr;
  }

  // 备注
  if (remark.value) {
    params.remark = remark.value;
  }

  // 标签
  if (selectedTagCodes.value.size > 0) {
    params.tagCodes = Array.from(selectedTagCodes.value);
  }

  emit("confirm", params);
};
</script>

<template>
  <div class="bill-filter">
    <!-- 日期范围 -->
    <div class="filter-section">
      <div class="section-label">{{ t("mobile.bill.dateRange") }}</div>
      <div class="date-picker" @click="showDatePicker = true">
        <span v-if="dateDisplay" class="date-text">{{ dateDisplay }}</span>
        <span v-else class="placeholder">{{
          t("mobile.bill.selectDate")
        }}</span>
        <van-icon name="calendar-o" />
      </div>
    </div>

    <!-- 金额范围 -->
    <div class="filter-section">
      <div class="section-label">{{ t("mobile.bill.amountRange") }}</div>
      <div class="amount-range">
        <van-field
          v-model="minAmount"
          type="number"
          :placeholder="t('mobile.bill.minAmount')"
          class="amount-input"
        />
        <span class="separator">-</span>
        <van-field
          v-model="maxAmount"
          type="number"
          :placeholder="t('mobile.bill.maxAmount')"
          class="amount-input"
        />
      </div>
    </div>

    <!-- 分类筛选 -->
    <div class="filter-section">
      <div class="section-label">{{ t("mobile.bill.classifyFilter") }}</div>

      <!-- 全选按钮 -->
      <div class="select-all-row">
        <div
          class="select-all-btn"
          :class="{ active: selectAllExpense }"
          @click="toggleAllExpense"
        >
          <span class="icon">{{ getClassifyIcon("other") }}</span>
          <span class="text">{{ t("mobile.bill.allExpense") }}</span>
          <van-icon v-if="selectAllExpense" name="success" class="check" />
        </div>
        <div
          class="select-all-btn"
          :class="{ active: selectAllIncome }"
          @click="toggleAllIncome"
        >
          <span class="icon">{{ getClassifyIcon("other") }}</span>
          <span class="text">{{ t("mobile.bill.allIncome") }}</span>
          <van-icon v-if="selectAllIncome" name="success" class="check" />
        </div>
      </div>

      <!-- 支出分类 -->
      <div v-if="expenseParents.length > 0" class="classify-group">
        <div class="group-title">{{ t("mobile.bill.expense") }}</div>
        <div class="classify-grid">
          <div
            v-for="parent in expenseParents"
            :key="parent.id"
            class="classify-item"
            :class="{ active: isClassifySelected(parent.id, null) }"
            @click="toggleClassify(parent, null)"
          >
            <span class="icon">{{ getClassifyIcon(parent.image) }}</span>
            <span class="name">{{ parent.name }}</span>
            <van-icon
              v-if="isClassifySelected(parent.id, null)"
              name="success"
              class="check"
            />
          </div>
        </div>
      </div>

      <!-- 收入分类 -->
      <div v-if="incomeParents.length > 0" class="classify-group">
        <div class="group-title">{{ t("mobile.bill.income") }}</div>
        <div class="classify-grid">
          <div
            v-for="parent in incomeParents"
            :key="parent.id"
            class="classify-item"
            :class="{ active: isClassifySelected(parent.id, null) }"
            @click="toggleClassify(parent, null)"
          >
            <span class="icon">{{ getClassifyIcon(parent.image) }}</span>
            <span class="name">{{ parent.name }}</span>
            <van-icon
              v-if="isClassifySelected(parent.id, null)"
              name="success"
              class="check"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 备注搜索 -->
    <div class="filter-section">
      <div class="section-label">{{ t("mobile.bill.remarkFilter") }}</div>
      <van-search
        v-model="remarkSearch"
        shape="round"
        :placeholder="t('mobile.bill.remarkPlaceholder')"
      />
      <div v-if="filteredRemarks.length > 0" class="remark-grid">
        <div
          v-for="item in filteredRemarks"
          :key="item.id"
          class="remark-item"
          :class="{ active: remark === item.remark }"
          @click="selectRemark(item.remark)"
        >
          {{ item.remark }}
        </div>
      </div>
      <van-empty
        v-else-if="remarkSearch"
        :description="t('mobile.common.noData')"
        image-size="40"
      />
    </div>

    <!-- 标签筛选 -->
    <div class="filter-section">
      <div class="section-label">{{ t("mobile.bill.tagFilter") }}</div>
      <van-search
        v-model="tagSearch"
        shape="round"
        :placeholder="t('mobile.record.tagSearch')"
      />
      <div v-if="filteredTags.length > 0" class="tag-grid">
        <div
          v-for="tag in filteredTags"
          :key="tag.id"
          class="tag-item"
          :class="{ active: selectedTagCodes.has(String(tag.id)) }"
          @click="toggleTag(String(tag.id))"
        >
          <van-tag :color="tag.color" text-color="#fff">
            {{ tag.name }}
          </van-tag>
          <van-icon
            v-if="selectedTagCodes.has(String(tag.id))"
            name="success"
            class="check"
          />
        </div>
      </div>
      <van-empty
        v-else-if="tagSearch"
        :description="t('mobile.common.noData')"
        image-size="40"
      />
    </div>

    <!-- 底部按钮 -->
    <div class="filter-footer">
      <van-button block @click="handleReset">
        {{ t("mobile.bill.reset") }}
      </van-button>
      <van-button type="danger" block @click="handleConfirm">
        {{ t("mobile.bill.confirm") }}
      </van-button>
    </div>

    <!-- 日期选择器 -->
    <van-calendar
      v-model:show="showDatePicker"
      type="range"
      :min-date="new Date(2020, 0, 1)"
      :max-date="new Date()"
      show-confirm
      position="bottom"
      round
      @confirm="handleDateConfirm"
    />
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.bill-filter {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  padding: 16px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
  overflow-y: auto;
}

.filter-section {
  .section-label {
    margin-bottom: 8px;
    font-size: 14px;
    font-weight: 500;
    color: $color-text-primary;
  }
}

.date-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: $color-background;
  border-radius: 8px;

  .date-text {
    font-size: 14px;
    color: $color-text-primary;
  }

  .placeholder {
    font-size: 14px;
    color: $color-text-placeholder;
  }
}

.amount-range {
  display: flex;
  gap: 8px;
  align-items: center;

  .amount-input {
    flex: 1;
  }

  .separator {
    font-size: 14px;
    color: $color-text-secondary;
  }
}

.select-all-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.select-all-btn {
  position: relative;
  display: flex;
  flex: 1;
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

.classify-group {
  margin-bottom: 12px;

  .group-title {
    margin-bottom: 8px;
    font-size: 12px;
    color: $color-text-secondary;
  }
}

.classify-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.classify-item {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
  padding: 8px 4px;
  background-color: $color-background;
  border: 2px solid transparent;
  border-radius: 8px;

  &.active {
    background-color: rgba($color-primary, 0.1);
    border-color: $color-primary;
  }

  .icon {
    font-size: 18px;
  }

  .name {
    max-width: 100%;
    overflow: hidden;
    font-size: 11px;
    color: $color-text-primary;
    text-align: center;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .check {
    position: absolute;
    top: 2px;
    right: 2px;
    font-size: 12px;
    color: $color-primary;
  }
}

.remark-grid,
.tag-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-top: 8px;
}

.remark-item {
  padding: 8px;
  overflow: hidden;
  font-size: 12px;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
  background-color: $color-background;
  border: 2px solid transparent;
  border-radius: 8px;

  &.active {
    background-color: rgba($color-primary, 0.1);
    border-color: $color-primary;
  }
}

.tag-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  background-color: $color-background;
  border: 2px solid transparent;
  border-radius: 8px;

  &.active {
    background-color: rgba($color-primary, 0.1);
    border-color: $color-primary;
  }

  .check {
    position: absolute;
    top: 2px;
    right: 2px;
    font-size: 12px;
    color: $color-primary;
  }
}

.filter-footer {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  margin-top: auto;
}
</style>
