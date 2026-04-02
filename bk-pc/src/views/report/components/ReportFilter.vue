<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useBillStoreHook } from "@/store/modules/bill";
import { getAccountBookIcon } from "@/utils/accountBook";
import { getClassifyIcon } from "@/utils/classifyIcons";
import dayjs from "dayjs";

defineOptions({
  name: "ReportFilter"
});

interface Props {
  accountBookId?: number;
  billType?: "month" | "year" | "custom";
  month?: string;
  year?: string;
  dateRange?: [string, string] | null;
  classifyList?: (number | string)[];
  remark?: string;
  tagCodes?: number[];
}

interface Emits {
  "update:accountBookId": [value: number | undefined];
  "update:billType": [value: "month" | "year" | "custom"];
  "update:month": [value: string];
  "update:year": [value: string];
  "update:dateRange": [value: [string, string] | null | undefined];
  "update:classifyList": [value: (number | string)[]];
  "update:remark": [value: string];
  "update:tagCodes": [value: number[]];
  query: [];
  reset: [];
}

const props = withDefaults(defineProps<Props>(), {
  billType: "month",
  month: () => dayjs().format("YYYY-MM"),
  year: () => dayjs().format("YYYY"),
  dateRange: null,
  classifyList: () => [],
  remark: "",
  tagCodes: () => []
});

const emit = defineEmits<Emits>();

const { t } = useI18n();
const billStore = useBillStoreHook();

const localAccountBookId = computed({
  get: () => props.accountBookId,
  set: (val) => emit("update:accountBookId", val)
});

const localBillType = computed({
  get: () => props.billType,
  set: (val) => emit("update:billType", val)
});

const localMonth = computed({
  get: () => props.month,
  set: (val) => emit("update:month", val)
});

const localYear = computed({
  get: () => props.year,
  set: (val) => emit("update:year", val)
});

const localDateRange = computed({
  get: () => props.dateRange,
  set: (val) => emit("update:dateRange", val ?? undefined)
});

const localClassifyList = computed({
  get: () => props.classifyList,
  set: (val) => emit("update:classifyList", val)
});

const localRemark = computed({
  get: () => props.remark,
  set: (val) => emit("update:remark", val)
});

const localTagCodes = computed({
  get: () => props.tagCodes,
  set: (val) => emit("update:tagCodes", val)
});

const filterRemarkText = ref("");
const filterTagText = ref("");

const filteredRemarkList = computed(() => {
  if (!filterRemarkText.value) return billStore.remarkList;
  return billStore.remarkList.filter(remark =>
    remark.remark.toLowerCase().includes(filterRemarkText.value.toLowerCase())
  );
});

const filteredTagList = computed(() => {
  if (!filterTagText.value) return billStore.tagList;
  return billStore.tagList.filter(tag =>
    tag.name.toLowerCase().includes(filterTagText.value.toLowerCase())
  );
});

const getFilterTagName = (tagId: number) => {
  const tag = billStore.tagList.find(t => t.id === tagId);
  return tag ? tag.name : "";
};

const getFilterTagColor = (tagId: number) => {
  const tag = billStore.tagList.find(t => t.id === tagId);
  return tag ? tag.color : "#409eff";
};

const toggleFilterTag = (tagId: number) => {
  const index = localTagCodes.value.indexOf(tagId);
  if (index === -1) {
    emit("update:tagCodes", [...localTagCodes.value, tagId]);
  } else {
    emit("update:tagCodes", localTagCodes.value.filter(id => id !== tagId));
  }
};

const removeFilterTag = (tagId: number) => {
  emit("update:tagCodes", localTagCodes.value.filter(id => id !== tagId));
};

const selectFilterRemark = (remark: string) => {
  emit("update:remark", remark);
};

const accountBookOptions = computed(() => {
  if (billStore.accountBooks.length > 0) {
    return billStore.accountBooks;
  }
  if (billStore.currentAccountBook) {
    return [billStore.currentAccountBook];
  }
  return [];
});

const expenseClassifyList = computed(() => {
  return billStore.classifyList.filter(
    (c: any) => c.pid === -1 && c.type === "EXPENSE"
  );
});

const incomeClassifyList = computed(() => {
  return billStore.classifyList.filter(
    (c: any) => c.pid === -1 && c.type === "INCOME"
  );
});

const prevHasAllExpense = ref(false);
const prevHasAllIncome = ref(false);

const handleClassifyChange = (value: (string | number)[]) => {
  const numValue = value.map(v => Number(v));
  const expenseIds = expenseClassifyList.value.map((c: any) => c.id);
  const incomeIds = incomeClassifyList.value.map((c: any) => c.id);

  const hasAllExpense = numValue.includes(-1);
  const hasAllIncome = numValue.includes(-2);

  let result: (number | string)[] = [];

  if (hasAllExpense && !prevHasAllExpense.value) {
    result = [...expenseIds, "-1"];
  } else if (!hasAllExpense && prevHasAllExpense.value) {
    result = numValue.filter(v => !expenseIds.includes(v));
  } else {
    result = [...value];
  }

  if (hasAllIncome && !prevHasAllIncome.value) {
    result = [
      ...result.filter(v => !incomeIds.includes(Number(v))),
      ...incomeIds,
      "-2"
    ];
  } else if (!hasAllIncome && prevHasAllIncome.value) {
    result = result.filter(v => !incomeIds.includes(Number(v)));
  }

  prevHasAllExpense.value = hasAllExpense;
  prevHasAllIncome.value = hasAllIncome;

  emit("update:classifyList", [...new Set(result)]);
};

const billTypeOptions = [
  { label: "月账单", value: "month" },
  { label: "年账单", value: "year" },
  { label: "自定义", value: "custom" }
];

const handleQuery = () => {
  emit("query");
};

const handleReset = () => {
  localBillType.value = "month";
  localMonth.value = dayjs().format("YYYY-MM");
  localYear.value = dayjs().format("YYYY");
  localDateRange.value = null;
  localClassifyList.value = [];
  localRemark.value = "";
  localTagCodes.value = [];
  emit("reset");
};
</script>

<template>
  <el-card class="report-filter" shadow="never">
    <div class="filter-row">
      <el-form inline>
        <el-form-item :label="t('bill.pureAccountBook')">
          <el-select
            v-model="localAccountBookId"
            :placeholder="t('bill.pureSelectPlaceholder')"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="book in accountBookOptions"
              :key="book.id"
              :label="`${getAccountBookIcon(book.image)} ${book.name}`"
              :value="book.id"
            >
              <div class="book-option">
                <span>{{ getAccountBookIcon(book.image) }} {{ book.name }}</span>
                <el-tag
                  v-if="book.isDefault === 'YES'"
                  size="small"
                  type="success"
                >
                  {{ t("dashboard.pureDefault") }}
                </el-tag>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="账单类型">
          <el-select v-model="localBillType" style="width: 100px">
            <el-option
              v-for="item in billTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker
            v-if="localBillType === 'month'"
            v-model="localMonth"
            type="month"
            value-format="YYYY-MM"
            placeholder="选择月份"
            style="width: 140px"
          />
          <el-date-picker
            v-else-if="localBillType === 'year'"
            v-model="localYear"
            type="year"
            value-format="YYYY"
            placeholder="选择年份"
            style="width: 100px"
          />
          <el-date-picker
            v-else
            v-model="localDateRange"
            type="daterange"
            range-separator="-"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item :label="t('bill.pureClassify')">
          <el-select
            :model-value="localClassifyList"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择"
            style="width: 200px"
            @update:model-value="handleClassifyChange"
          >
            <el-option
              label="全部支出"
              value="-1"
              class="classify-all-option expense"
            >
              <span class="option-icon">📌</span>
              <span>全部支出</span>
            </el-option>
            <el-option
              v-for="item in expenseClassifyList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
              <span class="option-icon">{{ getClassifyIcon(item.image) }}</span>
              <span>{{ item.name }}</span>
            </el-option>
            <el-option
              label="全部收入"
              value="-2"
              class="classify-all-option income"
            >
              <span class="option-icon">📌</span>
              <span>全部收入</span>
            </el-option>
            <el-option
              v-for="item in incomeClassifyList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
              <span class="option-icon">{{ getClassifyIcon(item.image) }}</span>
              <span>{{ item.name }}</span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </div>
    <div class="filter-row">
      <el-form inline>
      <el-form-item :label="t('bill.pureRemark')">
        <el-popover placement="bottom-start" :width="400" trigger="click">
          <template #reference>
            <el-input
              v-model="localRemark"
              :placeholder="t('bill.pureRemarkPlaceholder')"
              class="filter-remark-trigger"
              clearable
            />
          </template>
          <div class="filter-remark-content">
            <el-input
              v-model="filterRemarkText"
              placeholder="搜索备注"
              clearable
              class="filter-remark-search"
            />
            <div class="filter-remark-grid">
              <div
                v-for="remark in filteredRemarkList"
                :key="remark.id"
                class="filter-remark-item"
                @click="selectFilterRemark(remark.remark)"
              >
                {{ remark.remark }}
              </div>
            </div>
          </div>
        </el-popover>
      </el-form-item>
        <el-form-item :label="t('bill.pureTag')">
          <el-popover placement="bottom-start" :width="400" trigger="click">
            <template #reference>
              <div class="filter-tag-trigger">
                <span
                  v-if="localTagCodes.length === 0"
                  class="placeholder"
                >
                  {{ t("bill.pureSelectPlaceholder") }}
                </span>
                <span v-else class="selected-tags">
                  <el-tag
                    v-for="tagId in localTagCodes"
                    :key="tagId"
                    :color="getFilterTagColor(tagId)"
                    :style="{ color: '#fff' }"
                    size="small"
                    closable
                    @close="removeFilterTag(tagId)"
                  >
                    {{ getFilterTagName(tagId) }}
                  </el-tag>
                </span>
              </div>
            </template>
            <div class="filter-tag-content">
              <el-input
                v-model="filterTagText"
                :placeholder="t('bill.pureQuery')"
                clearable
                class="filter-tag-search"
              />
              <div class="filter-tag-grid">
                <div
                  v-for="tag in filteredTagList"
                  :key="tag.id"
                  class="filter-tag-item"
                  :class="{ active: localTagCodes.includes(tag.id) }"
                  @click="toggleFilterTag(tag.id)"
                >
                  <el-tag
                    :color="tag.color"
                    :style="{ color: '#fff' }"
                    size="small"
                  >
                    {{ tag.name }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-popover>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            {{ t("bill.pureQuery") }}
          </el-button>
          <el-button @click="handleReset">
            {{ t("bill.pureReset") }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </el-card>
</template>

<style lang="scss" scoped>
.report-filter {
  margin-bottom: 16px;
}

.filter-row {
  &:not(:last-child) {
    margin-bottom: 12px;
  }
}

.book-option {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: space-between;
}

.classify-all-option {
  &.expense {
    font-weight: 600;
    color: #f56c6c !important;
  }

  &.income {
    font-weight: 600;
    color: #67c23a !important;
  }
}

:deep(.el-select-dropdown__item) {
  display: flex;
  align-items: center;

  .option-icon {
    margin-right: 8px;
    font-size: 16px;
  }
}

.filter-tag-trigger {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  width: 360px;
  min-height: 32px;
  padding: 0 8px;
  cursor: pointer;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;

  .placeholder {
    color: #999;
  }

  &:hover {
    border-color: #409eff;
  }

  .selected-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    align-items: center;
  }

  :deep(.el-tag) {
    margin: 0;

    .el-tag__close {
      color: #fff;
      background-color: rgb(0 0 0 / 30%);

      &:hover {
        background-color: rgb(0 0 0 / 60%);
      }
    }
  }
}

.filter-remark-search,
.filter-tag-search {
  margin-bottom: 12px;
}

.filter-remark-grid,
.filter-tag-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.filter-remark-item,
.filter-tag-item {
  padding: 4px 8px;
  text-align: center;
  border-radius: 4px;
  cursor: pointer;
  background-color: #f5f7fa;

  &:hover {
    background-color: #ecf5ff;
  }

  &.active {
    background-color: #ecf5ff;
  }
}

.filter-remark-trigger {
  width: 240px;
}
</style>