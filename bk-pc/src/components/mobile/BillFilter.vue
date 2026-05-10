<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useI18n } from "vue-i18n";
import { showToast } from "vant";
import dayjs from "dayjs";
import type { Classify } from "@/types/classify";
import type { UserTag } from "@/types/userTag";
import type { UserRemark } from "@/types/remark";
import AmountKeyboardInput from "@/components/mobile/AmountKeyboardInput.vue";
import RemarkPicker from "@/components/mobile/RemarkPicker.vue";
import TagPicker from "@/components/mobile/TagPicker.vue";
import ClassifyFilterPicker from "@/components/mobile/ClassifyFilterPicker.vue";

defineOptions({
  name: "BillFilter"
});

const props = defineProps<{
  classifyList: Classify[];
  tagList: UserTag[];
  remarkList: UserRemark[];
  initialDate?: string[]; // 外部传入的初始日期
  initialRemark?: string; // 外部传入的初始备注
  initialTagIds?: number[]; // 外部传入的初始标签ID
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
const selectedTagIds = ref<number[]>([]);

// 日期选择器
const showDatePicker = ref(false);
const startDate = ref<string[]>([
  String(new Date().getFullYear()),
  String(new Date().getMonth() + 1),
  String(new Date().getDate())
]);
const endDate = ref<string[]>([
  String(new Date().getFullYear()),
  String(new Date().getMonth() + 1),
  String(new Date().getDate())
]);
const activePicker = ref<"start" | "end">("start");

// 分类选择器
const showClassifyPicker = ref(false);

// 备注选择器
const showRemarkPicker = ref(false);

// 标签选择器
const showTagPicker = ref(false);

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

// 选中的标签列表
const selectedTags = computed(() => {
  return props.tagList.filter(tag => selectedTagIds.value.includes(tag.id));
});

// 是否有筛选条件
const hasFilter = computed(() => {
  return (
    dateRange.value !== null ||
    minAmount.value !== null ||
    maxAmount.value !== null ||
    classifyCount.value > 0 ||
    remark.value !== "" ||
    selectedTagIds.value.length > 0
  );
});

// 监听外部传入的初始日期
watch(
  () => props.initialDate,
  (newDate) => {
    if (newDate && newDate.length === 2) {
      dateRange.value = [newDate[0], newDate[1]];
      startDate.value = newDate[0].split("-");
      endDate.value = newDate[1].split("-");
    }
  },
  { immediate: true }
);

// 监听外部传入的初始备注
watch(
  () => props.initialRemark,
  (newRemark) => {
    remark.value = newRemark || "";
  },
  { immediate: true }
);

// 监听外部传入的初始标签ID
watch(
  () => props.initialTagIds,
  (newTagIds) => {
    selectedTagIds.value = newTagIds || [];
  },
  { immediate: true }
);

// 打开日期选择器
const openDatePicker = () => {
  if (dateRange.value) {
    const startDateParts = dateRange.value[0].split("-");
    startDate.value = startDateParts;
    const endDateParts = dateRange.value[1].split("-");
    endDate.value = endDateParts;
  } else {
    const now = new Date();
    startDate.value = [
      String(now.getFullYear()),
      String(now.getMonth() + 1),
      String(now.getDate())
    ];
    endDate.value = [
      String(now.getFullYear()),
      String(now.getMonth() + 1),
      String(now.getDate())
    ];
  }
  activePicker.value = "start";
  showDatePicker.value = true;
};

// 切换当前选择的日期
const switchPicker = (type: "start" | "end") => {
  activePicker.value = type;
};

// 格式化日期数组为字符串
const formatDateString = (dateArr: string[]) => {
  if (!dateArr || dateArr.length < 3) return "";
  const year = String(dateArr[0]);
  const month = String(dateArr[1]).padStart(2, "0");
  const day = String(dateArr[2]).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

// 确认日期选择
const confirmDatePicker = () => {
  const start = formatDateString(startDate.value);
  const end = formatDateString(endDate.value);

  // 校验结束日期不能小于开始日期
  if (dayjs(end).isBefore(dayjs(start))) {
    showToast(t("mobile.bill.endDateBeforeStart"));
    return;
  }

  dateRange.value = [start, end];
  showDatePicker.value = false;
};

// 备注选择
const handleRemarkSelect = (selectedRemark: UserRemark) => {
  remark.value = selectedRemark.remark || "";
};

// 重置
const handleReset = () => {
  dateRange.value = null;
  minAmount.value = null;
  maxAmount.value = null;
  selectedClassifies.value = new Map();
  remark.value = "";
  selectedTagIds.value = [];
  emit("reset");
};

// 确认
const handleConfirm = () => {
  const params: FilterParams = {};

  // 日期范围 - 直接使用 dateRange
  if (dateRange.value) {
    params.date = [dateRange.value[0], dateRange.value[1]];
  } else {
    params.date = undefined;
  }

  // 金额范围
  if (minAmount.value !== null || maxAmount.value !== null) {
    params.amount = [minAmount.value ?? 0, maxAmount.value ?? 999999999];
  } else {
    params.amount = undefined;
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
  } else {
    params.classifyList = undefined;
  }

  // 备注
  if (remark.value) {
    params.remark = remark.value;
  } else {
    params.remark = undefined;
  }

  // 标签 - 需要将标签ID转换为标签code
  if (selectedTagIds.value.length > 0) {
    const tagCodes = selectedTagIds.value
      .map(id => {
        const tag = props.tagList.find(t => t.id === id);
        return tag ? String((tag as any).code) : null;
      })
      .filter(code => code !== null);
    params.tagCodes = tagCodes;
  } else {
    params.tagCodes = undefined;
  }

  emit("confirm", params);
};
</script>

<template>
  <div class="bill-filter">
    <!-- 日期范围 -->
    <div class="filter-section">
      <div class="section-label">{{ t("mobile.bill.dateRange") }}</div>
      <div class="date-picker" @click="openDatePicker">
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
        <AmountKeyboardInput
          v-model="minAmount"
          :placeholder="t('mobile.bill.minAmount')"
        />
        <span class="separator">-</span>
        <AmountKeyboardInput
          v-model="maxAmount"
          :placeholder="t('mobile.bill.maxAmount')"
        />
      </div>
    </div>

    <!-- 分类筛选 -->
    <div class="filter-section">
      <div class="section-label">{{ t("mobile.bill.classifyFilter") }}</div>
      <van-cell is-link @click="showClassifyPicker = true">
        <template #title>
          <span v-if="classifyCount > 0">{{ t("mobile.bill.selectedClassify", { count: classifyCount }) }}</span>
          <span v-else class="placeholder">{{ t("mobile.bill.selectClassify") }}</span>
        </template>
      </van-cell>
    </div>

    <!-- 备注筛选 -->
    <div class="filter-section">
      <div class="section-label">{{ t("mobile.bill.remarkFilter") }}</div>
      <van-cell
        is-link
        @click="showRemarkPicker = true"
      >
        <template #title>
          <span v-if="remark">{{ remark }}</span>
          <span v-else class="placeholder">{{ t("mobile.bill.remarkPlaceholder") }}</span>
        </template>
      </van-cell>
    </div>

    <!-- 标签筛选 -->
    <div class="filter-section">
      <div class="section-label">{{ t("mobile.bill.tagFilter") }}</div>
      <van-cell
        is-link
        @click="showTagPicker = true"
      >
        <template #title>
          <div v-if="selectedTags.length > 0" class="selected-tags-cell">
            <van-tag
              v-for="tag in selectedTags"
              :key="tag.id"
              :color="tag.color"
              text-color="#fff"
            >
              {{ tag.name }}
            </van-tag>
          </div>
          <span v-else class="placeholder">{{ t("mobile.record.tagPlaceholder") }}</span>
        </template>
      </van-cell>
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

    <!-- 日期选择器弹窗 -->
    <van-popup
      v-model:show="showDatePicker"
      position="bottom"
      round
      :style="{ height: '50%' }"
    >
      <div class="date-picker-popup">
        <div class="picker-header">
          <div
            class="picker-tab"
            :class="{ active: activePicker === 'start' }"
            @click="switchPicker('start')"
          >
            {{ t("mobile.bill.startDate") }}
          </div>
          <div
            class="picker-tab"
            :class="{ active: activePicker === 'end' }"
            @click="switchPicker('end')"
          >
            {{ t("mobile.bill.endDate") }}
          </div>
        </div>
        <div class="picker-content">
          <van-date-picker
            v-if="activePicker === 'start'"
            v-model="startDate"
            title=""
            :min-date="new Date(2020, 0, 1)"
            :max-date="new Date()"
            :show-toolbar="false"
          />
          <van-date-picker
            v-if="activePicker === 'end'"
            v-model="endDate"
            title=""
            :min-date="new Date(2020, 0, 1)"
            :max-date="new Date()"
            :show-toolbar="false"
          />
        </div>
        <div class="picker-footer">
          <van-button block type="danger" @click="confirmDatePicker">
            {{ t("mobile.common.confirm") }}
          </van-button>
        </div>
      </div>
    </van-popup>

    <!-- 分类选择器 -->
    <ClassifyFilterPicker
      v-model="showClassifyPicker"
      :classify-list="classifyList"
      :selected-classifies="selectedClassifies"
      @update:selected-classifies="selectedClassifies = $event"
    />

    <!-- 备注选择器 -->
    <RemarkPicker
      v-model="showRemarkPicker"
      :remark-list="remarkList"
      :current-remark="remark"
      @select="handleRemarkSelect"
    />

    <!-- 标签选择器 -->
    <TagPicker
      v-model="showTagPicker"
      :tag-list="tagList"
      :selected-ids="selectedTagIds"
      @update:selected-ids="selectedTagIds = $event"
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

  .separator {
    font-size: 14px;
    color: $color-text-secondary;
  }
}

.selected-tags-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.placeholder {
  color: $color-text-placeholder;
}

.filter-footer {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  margin-top: auto;
}

.date-picker-popup {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.picker-header {
  display: flex;
  border-bottom: 1px solid $color-border;
}

.picker-tab {
  flex: 1;
  padding: 12px;
  font-size: 14px;
  text-align: center;
  color: $color-text-secondary;
  cursor: pointer;

  &.active {
    font-weight: 500;
    color: $color-primary;
    border-bottom: 2px solid $color-primary;
  }
}

.picker-content {
  flex: 1;
  overflow: hidden;
}

.picker-footer {
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
}
</style>
