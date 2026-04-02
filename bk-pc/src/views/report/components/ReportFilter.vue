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
}

interface Emits {
  "update:accountBookId": [value: number | undefined];
  "update:billType": [value: "month" | "year" | "custom"];
  "update:month": [value: string];
  "update:year": [value: string];
  "update:dateRange": [value: [string, string] | null | undefined];
  "update:classifyList": [value: (number | string)[]];
  query: [];
  reset: [];
}

const props = withDefaults(defineProps<Props>(), {
  billType: "month",
  month: () => dayjs().format("YYYY-MM"),
  year: () => dayjs().format("YYYY"),
  dateRange: null,
  classifyList: () => []
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
  emit("reset");
};
</script>

<template>
  <el-card class="report-filter" shadow="never">
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
      <el-form-item>
        <el-button type="primary" @click="handleQuery">
          {{ t("bill.pureQuery") }}
        </el-button>
        <el-button @click="handleReset">
          {{ t("bill.pureReset") }}
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style lang="scss" scoped>
.report-filter {
  margin-bottom: 16px;
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
</style>