<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import * as echarts from "echarts";
import dayjs from "dayjs";
import { ElMessage } from "element-plus";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import { getTrendData } from "@/api/dashboard";
import type { TrendData, IncomeExpenseRecord } from "@/types/dashboard";

defineOptions({
  name: "BillReport"
});

const { t } = useI18n();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

const loading = ref(false);
const chartLoading = ref(false);

interface FormData {
  accountBookId: number | undefined;
  billType: string;
  month: string;
  year: string;
  dateRange: [string, string] | null;
  classifyList: number[];
}

interface MonthGroup {
  month: string;
  records: IncomeExpenseRecord[];
}

interface DateGroup {
  date: string;
  records: IncomeExpenseRecord[];
}

const formData = ref<FormData>({
  accountBookId: undefined,
  billType: "month",
  month: dayjs().format("YYYY-MM"),
  year: dayjs().format("YYYY"),
  dateRange: null,
  classifyList: []
});

const trendData = ref<TrendData | null>(null);

const billTypeOptions = [
  { label: "月账单", value: "month" },
  { label: "年账单", value: "year" },
  { label: "自定义", value: "custom" }
];

const detailSortType = ref<"time" | "amount">("time");
const expandedMonths = ref<Set<string>>(new Set());

const chartRef = ref<HTMLDivElement>();
let chartInstance: echarts.ECharts | null = null;

const mainClassifyList = computed(() => {
  return billStore.classifyList.filter((c: any) => c.pid === -1);
});

const getAccountBookName = (accountBookId: number) => {
  const book = billStore.accountBooks.find((b: any) => b.id === accountBookId);
  return book ? book.name : "";
};

const getTagsByCodes = (tagCodes: string | null | undefined) => {
  if (!tagCodes) return [];
  const tagIdStrs = tagCodes.split(",").map(t => t.trim());
  return billStore.tagList.filter((tag: any) =>
    tagIdStrs.includes(String(tag.code))
  );
};

const getQueryParams = () => {
  const userId = userStore.id;
  if (!userId) return null;

  let mode = "0";
  let beginDate = "";
  let endDate = "";

  switch (formData.value.billType) {
    case "month":
      mode = "0";
      beginDate = dayjs(formData.value.month + "-01")
        .startOf("month")
        .format("YYYY-MM-DD");
      endDate = dayjs(formData.value.month + "-01")
        .endOf("month")
        .format("YYYY-MM-DD");
      break;
    case "year":
      mode = "1";
      beginDate = dayjs(formData.value.year + "-01-01")
        .startOf("year")
        .format("YYYY-MM-DD");
      endDate = dayjs(formData.value.year + "-01-01")
        .endOf("year")
        .format("YYYY-MM-DD");
      break;
    case "custom":
      mode = "0";
      if (
        formData.value.dateRange &&
        formData.value.dateRange[0] &&
        formData.value.dateRange[1]
      ) {
        beginDate = formData.value.dateRange[0];
        endDate = formData.value.dateRange[1];
      } else {
        ElMessage.warning("请选择日期范围");
        return null;
      }
      break;
  }

  return {
    userId,
    accountBookId: formData.value.accountBookId,
    mode,
    queryMode: "0",
    beginDate,
    endDate,
    classifyList:
      formData.value.classifyList.length > 0
        ? formData.value.classifyList
        : undefined
  };
};

const fetchData = async () => {
  const params = getQueryParams();
  if (!params) return;

  loading.value = true;
  chartLoading.value = true;
  try {
    const result = await getTrendData(params);
    trendData.value = result;
    updateChart();
  } catch (error: any) {
    ElMessage.error(error?.message || "获取数据失败");
  } finally {
    loading.value = false;
    chartLoading.value = false;
  }
};

const initChart = () => {
  if (!chartRef.value) return;
  chartInstance = echarts.init(chartRef.value);
  updateChart();
};

const updateChart = () => {
  if (!chartInstance) return;

  const dates: string[] = [];
  const incomeData: number[] = [];
  const expenseData: number[] = [];

  if (trendData.value?.incomeExpenseSum) {
    const sortedKeys = Object.keys(trendData.value.incomeExpenseSum).sort();
    sortedKeys.forEach(date => {
      if (formData.value.billType === "year") {
        dates.push(dayjs(date).format("YYYY-MM"));
      } else {
        dates.push(dayjs(date).format("MM-DD"));
      }
      const item = trendData.value!.incomeExpenseSum[date];
      incomeData.push(Number(item.income) || 0);
      expenseData.push(Number(item.expense) || 0);
    });
  }

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: "axis",
      formatter: (params: any) => {
        let dateLabel = params[0].axisValueLabel;
        if (formData.value.billType === "year") {
          const idx = params[0].dataIndex;
          const sortedKeys = Object.keys(
            trendData.value!.incomeExpenseSum
          ).sort();
          dateLabel = sortedKeys[idx] || dateLabel;
        }
        let result = dateLabel + "<br/>";
        params.forEach((item: any) => {
          result += `${item.marker} ${item.seriesName}: ¥${Number(item.value).toFixed(2)}<br/>`;
        });
        return result;
      }
    },
    legend: {
      data: [t("bill.pureIncome"), t("bill.pureExpense")],
      top: 0
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      containLabel: true
    },
    xAxis: {
      type: "category",
      data: dates
    },
    yAxis: {
      type: "value"
    },
    series: [
      {
        name: t("bill.pureIncome"),
        type: "bar",
        data: incomeData,
        itemStyle: { color: "#67c23a" }
      },
      {
        name: t("bill.pureExpense"),
        type: "bar",
        data: expenseData,
        itemStyle: { color: "#f56c6c" }
      }
    ]
  };

  chartInstance.setOption(option);
};

const yearGroupedRecords = computed<MonthGroup[]>(() => {
  if (!trendData.value?.incomeExpenseList) return [];

  const records = [...trendData.value.incomeExpenseList];

  if (detailSortType.value === "amount") {
    records.sort((a, b) => b.amount - a.amount);
    const grouped: Record<string, IncomeExpenseRecord[]> = {};
    records.forEach(record => {
      const monthKey = dayjs(record.date).format("YYYY-MM");
      if (!grouped[monthKey]) {
        grouped[monthKey] = [];
      }
      grouped[monthKey].push(record);
    });
    return Object.entries(grouped)
      .sort((a, b) => b[0].localeCompare(a[0]))
      .map(([month, list]) => ({
        month,
        records: list
      }));
  }

  const grouped: Record<string, IncomeExpenseRecord[]> = {};
  records.forEach(record => {
    const monthKey = dayjs(record.date).format("YYYY-MM");
    if (!grouped[monthKey]) {
      grouped[monthKey] = [];
    }
    grouped[monthKey].push(record);
  });
  return Object.entries(grouped)
    .sort((a, b) => b[0].localeCompare(a[0]))
    .map(([month, list]) => ({
      month,
      records: list
    }));
});

const dateGroupedRecords = computed<DateGroup[]>(() => {
  if (!trendData.value?.incomeExpenseList) return [];

  const records = [...trendData.value.incomeExpenseList];

  if (detailSortType.value === "amount") {
    records.sort((a, b) => b.amount - a.amount);
    return [{ date: "all", records }];
  }

  const grouped: Record<string, IncomeExpenseRecord[]> = {};
  records.forEach(record => {
    const dateKey = record.date;
    if (!grouped[dateKey]) {
      grouped[dateKey] = [];
    }
    grouped[dateKey].push(record);
  });
  return Object.entries(grouped)
    .sort((a, b) => b[0].localeCompare(a[0]))
    .map(([date, list]) => ({
      date,
      records: list
    }));
});

const flatRecordsByTime = computed(() => {
  if (!trendData.value?.incomeExpenseList) return [];
  return [...trendData.value.incomeExpenseList].sort((a, b) =>
    b.date.localeCompare(a.date)
  );
});

const getMonthSummary = (month: string) => {
  if (!trendData.value?.incomeExpenseList) return { income: 0, expense: 0 };

  const monthRecords = trendData.value.incomeExpenseList.filter(
    record => dayjs(record.date).format("YYYY-MM") === month
  );

  const income = monthRecords
    .filter(r => r.type === "INCOME" || r.type === "1")
    .reduce((sum, r) => sum + r.amount, 0);

  const expense = monthRecords
    .filter(r => r.type === "EXPENSE" || r.type === "0")
    .reduce((sum, r) => sum + r.amount, 0);

  return { income, expense };
};

const formatDate = (date: string) => {
  const weekDays = ["日", "一", "二", "三", "四", "五", "六"];
  const dateObj = dayjs(date);
  return `${dateObj.format("YYYY年MM月DD日")} 星期${weekDays[dateObj.day()]}`;
};

const dateSpanMethod = ({
  row,
  column,
  rowIndex,
  columnIndex
}: {
  row: IncomeExpenseRecord;
  column: any;
  rowIndex: number;
  columnIndex: number;
}) => {
  if (columnIndex === 0) {
    const data = flatRecordsByTime.value;
    const currentDate = row.date;
    let rowspan = 1;

    if (rowIndex === 0 || data[rowIndex - 1].date !== currentDate) {
      for (let i = rowIndex + 1; i < data.length; i++) {
        if (data[i].date === currentDate) {
          rowspan++;
        } else {
          break;
        }
      }
    } else {
      rowspan = 0;
    }

    return { rowspan, colspan: rowspan ? 1 : 0 };
  }
  return { rowspan: 1, colspan: 1 };
};

const groupDateSpanMethod = ({
  row,
  column,
  rowIndex,
  columnIndex,
  data
}: {
  row: IncomeExpenseRecord;
  column: any;
  rowIndex: number;
  columnIndex: number;
  data: IncomeExpenseRecord[];
}) => {
  if (columnIndex === 0) {
    const currentDate = row.date;
    let rowspan = 1;

    if (rowIndex === 0 || data[rowIndex - 1].date !== currentDate) {
      for (let i = rowIndex + 1; i < data.length; i++) {
        if (data[i].date === currentDate) {
          rowspan++;
        } else {
          break;
        }
      }
    } else {
      rowspan = 0;
    }

    return { rowspan, colspan: rowspan ? 1 : 0 };
  }
  return { rowspan: 1, colspan: 1 };
};

const toggleMonth = (month: string) => {
  if (expandedMonths.value.has(month)) {
    expandedMonths.value.delete(month);
  } else {
    expandedMonths.value.add(month);
  }
};

const handleReset = () => {
  formData.value = {
    accountBookId: billStore.currentAccountBook?.id,
    billType: "month",
    month: dayjs().format("YYYY-MM"),
    year: dayjs().format("YYYY"),
    dateRange: null,
    classifyList: []
  };
  trendData.value = null;
  expandedMonths.value.clear();
};

watch(
  () => billStore.currentAccountBook,
  val => {
    if (val && !formData.value.accountBookId) {
      formData.value.accountBookId = val.id;
    }
  }
);

watch(
  () => formData.value.billType,
  () => {
    expandedMonths.value.clear();
  }
);

onMounted(async () => {
  formData.value.accountBookId = billStore.currentAccountBook?.id;
  initChart();
  await fetchData();
});
</script>

<template>
  <div class="bill-report">
    <el-card class="query-form-card" shadow="never">
      <el-form :model="formData" inline>
        <el-form-item :label="t('bill.pureAccountBook')">
          <el-select
            v-model="formData.accountBookId"
            :placeholder="t('bill.pureSelectPlaceholder')"
            style="width: 140px"
          >
            <el-option
              v-for="book in billStore.accountBooks"
              :key="book.id"
              :label="book.name"
              :value="book.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="账单类型">
          <el-select v-model="formData.billType" style="width: 100px">
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
            v-if="formData.billType === 'month'"
            v-model="formData.month"
            type="month"
            value-format="YYYY-MM"
            placeholder="选择月份"
            style="width: 140px"
          />
          <el-date-picker
            v-else-if="formData.billType === 'year'"
            v-model="formData.year"
            type="year"
            value-format="YYYY"
            placeholder="选择年份"
            style="width: 100px"
          />
          <el-date-picker
            v-else
            v-model="formData.dateRange"
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
            v-model="formData.classifyList"
            multiple
            collapse-tags
            collapse-tags-tooltip
            :placeholder="t('bill.pureSelectPlaceholder')"
            style="width: 180px"
          >
            <el-option
              v-for="item in mainClassifyList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">{{
            t("bill.pureQuery")
          }}</el-button>
          <el-button @click="handleReset">{{ t("bill.pureReset") }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-loading="chartLoading" class="chart-card" shadow="never">
      <template #header>
        <div class="chart-header">
          <span class="total-info">
            <span class="total-item income">
              {{ t("dashboard.pureTotalIncome") }}: ¥{{
                (trendData?.incomeTotal ?? 0).toFixed(2)
              }}
            </span>
            <span class="total-item expense">
              {{ t("dashboard.pureTotalExpense") }}: ¥{{
                (trendData?.expenseTotal ?? 0).toFixed(2)
              }}
            </span>
          </span>
        </div>
      </template>
      <div ref="chartRef" class="chart-container" />
    </el-card>

    <el-card class="detail-card" shadow="never">
      <template #header>
        <div class="detail-header">
          <span>{{ t("bill.pureTitle") }}</span>
          <el-radio-group v-model="detailSortType" size="small">
            <el-radio-button value="time">按时间</el-radio-button>
            <el-radio-button value="amount">按金额</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div v-loading="loading" class="detail-content">
        <template v-if="formData.billType === 'year'">
          <div
            v-for="group in yearGroupedRecords"
            :key="group.month"
            class="month-group"
          >
            <div class="month-header" @click="toggleMonth(group.month)">
              <span class="expand-icon">{{
                expandedMonths.has(group.month) ? "▼" : "▶"
              }}</span>
              <span class="month-label">{{ group.month }}</span>
              <span class="month-summary">
                <span class="income-text"
                  >收入: ¥{{
                    getMonthSummary(group.month).income.toFixed(2)
                  }}</span
                >
                <span class="expense-text"
                  >支出: ¥{{
                    getMonthSummary(group.month).expense.toFixed(2)
                  }}</span
                >
                <span
                  :class="
                    getMonthSummary(group.month).income -
                      getMonthSummary(group.month).expense >=
                    0
                      ? 'balance-positive'
                      : 'balance-negative'
                  "
                  class="balance-text"
                  >结余: ¥{{
                    (
                      getMonthSummary(group.month).income -
                      getMonthSummary(group.month).expense
                    ).toFixed(2)
                  }}</span
                >
              </span>
            </div>
            <el-table
              v-if="expandedMonths.has(group.month)"
              :data="group.records"
              border
              stripe
              size="small"
              :span-method="(args: any) => groupDateSpanMethod({ ...args, data: group.records })"
            >
              <el-table-column :label="t('bill.pureDate')" width="100" align="center">
                <template #default="{ row }">
                  {{ row.date }}
                </template>
              </el-table-column>
              <el-table-column :label="t('bill.pureAccountBook')" width="100">
                <template #default="{ row }">
                  {{ getAccountBookName(row.accountBookId) }}
                </template>
              </el-table-column>
              <el-table-column :label="t('bill.pureAmount')" width="100" align="right">
                <template #default="{ row }">
                  <span
                    :style="{
                      color:
                        row.type === 'EXPENSE' || row.type === '0'
                          ? '#f56c6c'
                          : '#67c23a'
                    }"
                  >
                    {{
                      row.type === "EXPENSE" || row.type === "0" ? "-" : "+"
                    }}¥{{ row.amount.toFixed(2) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column :label="t('bill.pureType')" width="80" align="center">
                <template #default="{ row }">
                  <el-tag
                    :type="
                      row.type === 'EXPENSE' || row.type === '0'
                        ? 'danger'
                        : 'success'
                    "
                    size="small"
                  >
                    {{
                      row.type === "EXPENSE" || row.type === "0"
                        ? t("bill.pureExpense")
                        : t("bill.pureIncome")
                    }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="t('bill.pureClassify')" min-width="200">
                <template #default="{ row }">
                  <span>{{ row.mainClassifyName }}</span>
                  <span v-if="row.subClassifyName" style="color: #909399">
                    / {{ row.subClassifyName }}</span
                  >
                </template>
              </el-table-column>
              <el-table-column
                :label="t('bill.pureCreditCard')"
                width="100"
                align="center"
              >
                <template #default="{ row }">
                  <el-tag
                    :type="row.isCreditCard === 'YES' ? 'warning' : 'info'"
                    size="small"
                  >
                    {{
                        row.isCreditCard === "YES"
                          ? t("bill.pureYes")
                          : t("bill.pureNo")
                      }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column
                prop="remark"
                :label="t('bill.pureRemark')"
                min-width="300"
              />
              <el-table-column :label="t('bill.pureTag')" min-width="300">
                <template #default="{ row }">
                  <el-tag
                    v-for="tag in getTagsByCodes(row.tagCodes)"
                    :key="tag.id"
                    :color="tag.color"
                    size="small"
                    :style="{ color: '#fff', marginRight: '4px' }"
                  >
                    {{ tag.name }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
        <template v-else>
          <el-table
            v-if="detailSortType === 'amount'"
            :data="
              (dateGroupedRecords[0]?.records || []).sort((a, b) => b.amount - a.amount)
            "
            border
            stripe
          >
            <el-table-column :label="t('bill.pureDate')" width="100" align="center">
              <template #default="{ row }">
                {{ row.date }}
              </template>
            </el-table-column>
            <el-table-column :label="t('bill.pureAccountBook')" width="100">
              <template #default="{ row }">
                {{ getAccountBookName(row.accountBookId) }}
              </template>
            </el-table-column>
            <el-table-column :label="t('bill.pureAmount')" width="100" align="right">
              <template #default="{ row }">
                <span
                  :style="{
                    color:
                      row.type === 'EXPENSE' || row.type === '0' ? '#f56c6c' : '#67c23a'
                  }"
                >
                  {{ row.type === "EXPENSE" || row.type === "0" ? "-" : "+" }}¥{{
                    row.amount.toFixed(2)
                  }}
                </span>
              </template>
            </el-table-column>
            <el-table-column :label="t('bill.pureType')" width="80" align="center">
              <template #default="{ row }">
                <el-tag
                  :type="
                    row.type === 'EXPENSE' || row.type === '0' ? 'danger' : 'success'
                  "
                  size="small"
                >
                  {{
                    row.type === "EXPENSE" || row.type === "0"
                      ? t("bill.pureExpense")
                      : t("bill.pureIncome")
                  }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="t('bill.pureClassify')" min-width="200">
              <template #default="{ row }">
                <span>{{ row.mainClassifyName }}</span>
                <span v-if="row.subClassifyName" style="color: #909399">
                  / {{ row.subClassifyName }}</span
                >
              </template>
            </el-table-column>
            <el-table-column :label="t('bill.pureCreditCard')" width="100" align="center">
              <template #default="{ row }">
                <el-tag
                  :type="row.isCreditCard === 'YES' ? 'warning' : 'info'"
                  size="small">
                  {{ row.isCreditCard === "YES" ? t("bill.pureYes") : t("bill.pureNo") }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
              prop="remark"
              :label="t('bill.pureRemark')"
              min-width="300"
            />
            <el-table-column :label="t('bill.pureTag')" min-width="300">
              <template #default="{ row }">
                <el-tag
                  v-for="tag in getTagsByCodes(row.tagCodes)"
                  :key="tag.id"
                  :color="tag.color"
                  size="small"
                  :style="{ color: '#fff', marginRight: '4px' }"
                >
                  {{ tag.name }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <template v-else>
            <el-table
              :data="flatRecordsByTime"
              border
              stripe
              :span-method="dateSpanMethod"
            >
              <el-table-column :label="t('bill.pureDate')" width="100" align="center">
                <template #default="{ row }">
                  {{ row.date }}
                </template>
              </el-table-column>
              <el-table-column :label="t('bill.pureAccountBook')" width="100">
                <template #default="{ row }">
                  {{ getAccountBookName(row.accountBookId) }}
                </template>
              </el-table-column>
              <el-table-column :label="t('bill.pureAmount')" width="100" align="right">
                <template #default="{ row }">
                  <span
                    :style="{
                      color:
                        row.type === 'EXPENSE' || row.type === '0'
                          ? '#f56c6c'
                          : '#67c23a'
                    }"
                  >
                    {{
                      row.type === "EXPENSE" || row.type === "0" ? "-" : "+"
                    }}¥{{ row.amount.toFixed(2) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column :label="t('bill.pureType')" width="80" align="center">
                <template #default="{ row }">
                  <el-tag
                    :type="
                      row.type === 'EXPENSE' || row.type === '0'
                        ? 'danger'
                        : 'success'
                    "
                    size="small"
                  >
                    {{
                      row.type === "EXPENSE" || row.type === "0"
                        ? t("bill.pureExpense")
                        : t("bill.pureIncome")
                    }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="t('bill.pureClassify')" min-width="200">
                <template #default="{ row }">
                  <span>{{ row.mainClassifyName }}</span>
                  <span v-if="row.subClassifyName" style="color: #909399">
                    / {{ row.subClassifyName }}</span
                  >
                </template>
              </el-table-column>
              <el-table-column
                :label="t('bill.pureCreditCard')"
                width="100"
                align="center"
              >
                <template #default="{ row }">
                  <el-tag
                    :type="row.isCreditCard === 'YES' ? 'warning' : 'info'"
                    size="small"
                  >
                    {{
                      row.isCreditCard === "YES"
                        ? t("bill.pureYes")
                        : t("bill.pureNo")
                    }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column
                prop="remark"
                :label="t('bill.pureRemark')"
                min-width="300"
              />
              <el-table-column :label="t('bill.pureTag')" min-width="300">
                <template #default="{ row }">
                  <el-tag
                    v-for="tag in getTagsByCodes(row.tagCodes)"
                    :key="tag.id"
                    :color="tag.color"
                    size="small"
                    :style="{ color: '#fff', marginRight: '4px' }"
                  >
                    {{ tag.name }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </template>
        </template>
        <el-empty
          v-if="!loading && (!trendData?.incomeExpenseList?.length)"
          :description="t('dashboard.pureNoRecords')"
        />
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.bill-report {
  .query-form-card {
    margin-bottom: 20px;
  }

  .chart-card {
    margin-bottom: 20px;

    .chart-header {
      display: flex;
      align-items: center;
    }

    .total-info {
      display: flex;
      gap: 20px;
      font-size: 14px;

      .total-item {
        &.income {
          color: #67c23a;
        }

        &.expense {
          color: #f56c6c;
        }
      }
    }

    .chart-container {
      width: 100%;
      height: 300px;
    }
  }

  .detail-card {
    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .month-group {
      margin-bottom: 16px;

      .month-header {
        display: flex;
        align-items: center;
        padding: 8px 12px;
        background: #f5f7fa;
        border-radius: 4px;
        cursor: pointer;
        margin-bottom: 8px;

        .expand-icon {
          margin-right: 8px;
          color: #409eff;
        }

        .month-label {
          font-weight: 600;
          font-size: 14px;
        }

        .month-summary {
          margin-left: 30px;

          .income-text {
            color: #67c23a;
            margin-right: 16px;
          }

          .expense-text {
            color: #f56c6c;
            margin-right: 16px;
          }

          .balance-positive {
            color: #67c23a;
          }

          .balance-negative {
            color: #f56c6c;
          }
        }
      }
    }

    .date-group {
      margin-bottom: 16px;

      .date-header {
        font-weight: 600;
        padding: 8px 12px;
        background: #f5f7fa;
        border-radius: 4px;
        margin-bottom: 8px;
      }
    }
  }
}
</style>