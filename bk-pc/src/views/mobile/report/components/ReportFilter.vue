<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useI18n } from "vue-i18n";
import dayjs from "dayjs";
import type { Classify } from "@/types/classify";
import type { UserTag } from "@/types/userTag";
import type { UserRemark } from "@/types/remark";
import RemarkPicker from "@/components/mobile/RemarkPicker.vue";
import TagPicker from "@/components/mobile/TagPicker.vue";
import ClassifyFilterPicker from "@/components/mobile/ClassifyFilterPicker.vue";

defineOptions({
  name: "ReportFilter"
});

export interface ReportFilterParams {
  billType: "month" | "year" | "custom";
  beginDate: string;
  endDate: string;
  accountBookId?: number;
  classifyList?: { mainClassifyId: number; subClassifyId: number | null }[];
  remark?: string;
  tagCodes?: string[];
}

const props = defineProps<{
  classifyList: Classify[];
  tagList: UserTag[];
  remarkList: UserRemark[];
  initialParams?: ReportFilterParams;
}>();

const emit = defineEmits<{
  confirm: [params: ReportFilterParams];
  reset: [];
}>();

const { t } = useI18n();

// 账单类型
const billType = ref<"month" | "year" | "custom">("month");

// 年月选择器
const showMonthPicker = ref(false);
const selectedYear = ref(new Date().getFullYear());
const selectedMonth = ref(new Date().getMonth() + 1);

// 年份选择器
const showYearPicker = ref(false);
const selectedYearOnly = ref(new Date().getFullYear());

// 自定义日期选择器
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
const selectedClassifies = ref<Map<number, Set<number | null>>>(new Map());

// 备注选择器
const showRemarkPicker = ref(false);
const remark = ref("");

// 标签选择器
const showTagPicker = ref(false);
const selectedTagIds = ref<number[]>([]);

// 年月选择器数据
const currentYear = new Date().getFullYear();
const yearOptions = computed(() => {
  const years = [];
  for (let i = currentYear; i >= currentYear - 10; i--) {
    years.push({ text: `${i}`, value: i });
  }
  return years;
});

const monthOptions = computed(() => {
  const months = [];
  for (let i = 1; i <= 12; i++) {
    months.push({ text: `${i}`, value: i });
  }
  return months;
});

// 年份选择器数据（用于年账单）
const yearOnlyOptions = computed(() => {
  const years = [];
  for (let i = currentYear; i >= currentYear - 10; i--) {
    years.push({ text: `${i}`, value: i });
  }
  return years;
});

// 选中的标签列表
const selectedTags = computed(() => {
  return props.tagList.filter(tag => selectedTagIds.value.includes(tag.id));
});

// 分类选中数量
const classifyCount = computed(() => {
  let count = 0;
  selectedClassifies.value.forEach(subSet => {
    count += subSet.size;
  });
  return count;
});

// 时间显示文本
const dateDisplay = computed(() => {
  if (billType.value === "month") {
    return `${selectedYear.value}年${selectedMonth.value}月`;
  } else if (billType.value === "year") {
    return `${selectedYearOnly.value}年`;
  } else {
    const start = formatDateString(startDate.value);
    const end = formatDateString(endDate.value);
    return `${start} ~ ${end}`;
  }
});

// 监听外部传入的初始参数
watch(
  () => props.initialParams,
  (newParams) => {
    if (newParams) {
      billType.value = newParams.billType || "month";
      remark.value = newParams.remark || "";

      // 标签：将 tagCodes（标签code）转换为标签ID
      if (newParams.tagCodes && newParams.tagCodes.length > 0) {
        const codes = newParams.tagCodes.map(c => Number(c));
        selectedTagIds.value = props.tagList
          .filter(tag => codes.includes((tag as any).code))
          .map(tag => tag.id);
      } else {
        selectedTagIds.value = [];
      }

      if (newParams.beginDate && newParams.endDate) {
        if (billType.value === "month") {
          const date = dayjs(newParams.beginDate);
          selectedYear.value = date.year();
          selectedMonth.value = date.month() + 1;
        } else if (billType.value === "year") {
          selectedYearOnly.value = dayjs(newParams.beginDate).year();
        } else {
          startDate.value = newParams.beginDate.split("-");
          endDate.value = newParams.endDate.split("-");
        }
      }

      if (newParams.classifyList) {
        const map = new Map<number, Set<number | null>>();
        newParams.classifyList.forEach(item => {
          if (!map.has(item.mainClassifyId)) {
            map.set(item.mainClassifyId, new Set());
          }
          map.get(item.mainClassifyId)!.add(item.subClassifyId);
        });
        selectedClassifies.value = map;
      }
    }
  },
  { immediate: true }
);

// 账单类型切换
const handleBillTypeChange = (type: "month" | "year" | "custom") => {
  billType.value = type;
  if (type === "month") {
    showMonthPicker.value = true;
  } else if (type === "year") {
    showYearPicker.value = true;
  } else {
    showDatePicker.value = true;
  }
};

// 格式化日期数组为字符串
const formatDateString = (dateArr: string[]) => {
  if (!dateArr || dateArr.length < 3) return "";
  const year = String(dateArr[0]);
  const month = String(dateArr[1]).padStart(2, "0");
  const day = String(dateArr[2]).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

// 获取日期范围
const getDateRange = (): { beginDate: string; endDate: string } => {
  if (billType.value === "month") {
    const beginDate = dayjs(`${selectedYear.value}-${selectedMonth.value}-01`);
    const endDate = beginDate.endOf("month");
    return {
      beginDate: beginDate.format("YYYY-MM-DD"),
      endDate: endDate.format("YYYY-MM-DD")
    };
  } else if (billType.value === "year") {
    const beginDate = dayjs(`${selectedYearOnly.value}-01-01`);
    const endDate = beginDate.endOf("year");
    return {
      beginDate: beginDate.format("YYYY-MM-DD"),
      endDate: endDate.format("YYYY-MM-DD")
    };
  } else {
    return {
      beginDate: formatDateString(startDate.value),
      endDate: formatDateString(endDate.value)
    };
  }
};

// 切换当前选择的日期
const switchPicker = (type: "start" | "end") => {
  activePicker.value = type;
};

// 确认日期选择
const confirmDatePicker = () => {
  showDatePicker.value = false;
};

// 备注选择
const handleRemarkSelect = (selectedRemark: UserRemark) => {
  remark.value = selectedRemark.remark || "";
};

// 重置
const handleReset = () => {
  billType.value = "month";
  selectedYear.value = new Date().getFullYear();
  selectedMonth.value = new Date().getMonth() + 1;
  selectedYearOnly.value = new Date().getFullYear();
  startDate.value = [
    String(new Date().getFullYear()),
    String(new Date().getMonth() + 1),
    String(new Date().getDate())
  ];
  endDate.value = [
    String(new Date().getFullYear()),
    String(new Date().getMonth() + 1),
    String(new Date().getDate())
  ];
  selectedClassifies.value = new Map();
  remark.value = "";
  selectedTagIds.value = [];
  emit("reset");
};

// 确认
const handleConfirm = () => {
  const dateRange = getDateRange();

  const params: ReportFilterParams = {
    billType: billType.value,
    beginDate: dateRange.beginDate,
    endDate: dateRange.endDate,
    accountBookId: props.initialParams?.accountBookId
  };

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

  // 标签 - 需要将标签ID转换为标签code
  if (selectedTagIds.value.length > 0) {
    const tagCodes = selectedTagIds.value
      .map(id => {
        const tag = props.tagList.find(t => t.id === id);
        return tag ? String((tag as any).code) : null;
      })
      .filter(code => code !== null);
    params.tagCodes = tagCodes;
  }

  emit("confirm", params);
};
</script>

<template>
  <div class="report-filter">
    <!-- 账单类型 -->
    <div class="filter-section">
      <div class="section-label">{{ t("mobile.report.billType") }}</div>
      <div class="bill-type-tabs">
        <div
          class="type-tab"
          :class="{ active: billType === 'month' }"
          @click="handleBillTypeChange('month')"
        >
          {{ t("mobile.report.monthBill") }}
        </div>
        <div
          class="type-tab"
          :class="{ active: billType === 'year' }"
          @click="handleBillTypeChange('year')"
        >
          {{ t("mobile.report.yearBill") }}
        </div>
        <div
          class="type-tab"
          :class="{ active: billType === 'custom' }"
          @click="handleBillTypeChange('custom')"
        >
          {{ t("mobile.report.customBill") }}
        </div>
      </div>
    </div>

    <!-- 时间显示 -->
    <div class="filter-section">
      <div class="section-label">{{ t("mobile.report.timeRange") }}</div>
      <div class="date-display" @click="handleBillTypeChange(billType)">
        <span class="date-text">{{ dateDisplay }}</span>
        <van-icon name="arrow" />
      </div>
    </div>

    <!-- 分类筛选 -->
    <div class="filter-section">
      <div class="section-label">{{ t("mobile.bill.classifyFilter") }}</div>
      <van-cell is-link @click="showClassifyPicker = true">
        <template #title>
          <span v-if="classifyCount > 0">{{
            t("mobile.bill.selectedClassify", { count: classifyCount })
          }}</span>
          <span v-else class="placeholder">{{
            t("mobile.bill.selectClassify")
          }}</span>
        </template>
      </van-cell>
    </div>

    <!-- 备注筛选 -->
    <div class="filter-section">
      <div class="section-label">{{ t("mobile.bill.remarkFilter") }}</div>
      <van-cell is-link @click="showRemarkPicker = true">
        <template #title>
          <span v-if="remark">{{ remark }}</span>
          <span v-else class="placeholder">{{
            t("mobile.bill.remarkPlaceholder")
          }}</span>
        </template>
      </van-cell>
    </div>

    <!-- 标签筛选 -->
    <div class="filter-section">
      <div class="section-label">{{ t("mobile.bill.tagFilter") }}</div>
      <van-cell is-link @click="showTagPicker = true">
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
          <span v-else class="placeholder">{{
            t("mobile.record.tagPlaceholder")
          }}</span>
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

    <!-- 年月选择器 -->
    <van-popup
      v-model:show="showMonthPicker"
      position="bottom"
      round
      :style="{ height: '40%' }"
    >
      <van-picker
        :columns="[yearOptions, monthOptions]"
        :title="t('mobile.report.selectMonth')"
        :confirm-button-text="t('mobile.common.confirm')"
        :cancel-button-text="t('mobile.common.cancel')"
        @confirm="showMonthPicker = false"
        @cancel="showMonthPicker = false"
        @change="({ selectedValues }) => {
          selectedYear = selectedValues[0] as number;
          selectedMonth = selectedValues[1] as number;
        }"
      />
    </van-popup>

    <!-- 年份选择器 -->
    <van-popup
      v-model:show="showYearPicker"
      position="bottom"
      round
      :style="{ height: '40%' }"
    >
      <van-picker
        :columns="yearOnlyOptions"
        :title="t('mobile.report.selectYear')"
        :confirm-button-text="t('mobile.common.confirm')"
        :cancel-button-text="t('mobile.common.cancel')"
        @confirm="showYearPicker = false"
        @cancel="showYearPicker = false"
        @change="({ selectedValues }) => {
          selectedYearOnly = selectedValues[0] as number;
        }"
      />
    </van-popup>

    <!-- 自定义日期选择器 -->
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

.report-filter {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  min-height: 0;
  padding: 16px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
  overflow-y: auto;
}

.filter-section {
  flex-shrink: 0;
}

.filter-section {
  .section-label {
    margin-bottom: 8px;
    font-size: 14px;
    font-weight: 500;
    color: $color-text-primary;
  }
}

.bill-type-tabs {
  display: flex;
  gap: 8px;

  .type-tab {
    flex: 1;
    padding: 10px 0;
    font-size: 14px;
    text-align: center;
    cursor: pointer;
    border: 1px solid $color-border;
    border-radius: 8px;
    transition: all 0.2s;

    &.active {
      color: #fff;
      background-color: $color-primary;
      border-color: $color-primary;
    }
  }
}

.date-display {
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
  flex-shrink: 0;
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
