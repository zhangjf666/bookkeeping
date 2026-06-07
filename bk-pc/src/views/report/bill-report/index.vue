<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import * as echarts from "echarts";
import dayjs from "dayjs";
import { ElMessage } from "element-plus";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import { getTrendData } from "@/api/incomeExpense";
import { formatAmount } from "@/utils/format";
import { getClassifyIcon } from "@/utils/classifyIcons";
import type { TrendData, IncomeExpenseRecord } from "@/types/bill";
import ReportFilter from "../components/ReportFilter.vue";

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
  billType: "month" | "year" | "custom";
  month: string;
  year: string;
  dateRange: [string, string] | null;
  classifyList: { mainClassifyId: number; subClassifyId: number | null }[];
  remark: string;
  tagCodes: number[];
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
  classifyList: [],
  remark: "",
  tagCodes: []
});

const displayBillType = ref<"month" | "year" | "custom">("month");

const trendData = ref<TrendData | null>(null);

const detailSortType = ref<"time" | "amount">("time");
const expandedMonths = ref<Set<string>>(new Set());

const chartRef = ref<HTMLDivElement>();
let chartInstance: echarts.ECharts | null = null;

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
        ElMessage.warning(t("bill.pureDateRangeRequired"));
        return null;
      }
      break;
  }

  const getClassifyList = () => {
    const list = formData.value.classifyList;
    if (!list || list.length === 0) return undefined;
    return list;
  };

  const getRemark = () => {
    if (!formData.value.remark) return undefined;
    return formData.value.remark;
  };

  const getTagCodes = () => {
    if (formData.value.tagCodes.length === 0) return undefined;
    return formData.value.tagCodes;
  };

  return {
    userId,
    accountBookId: formData.value.accountBookId,
    mode,
    queryMode: "0",
    beginDate,
    endDate,
    classifyList: getClassifyList(),
    remark: getRemark(),
    tagCodes: getTagCodes()
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
    displayBillType.value = formData.value.billType;
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
          result += `${item.marker} ${item.seriesName}: ¥${formatAmount(Number(item.value))}<br/>`;
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
    classifyList: [],
    remark: "",
    tagCodes: []
  };
  trendData.value = null;
  expandedMonths.value.clear();
  fetchData();
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
  () => displayBillType.value,
  () => {
    expandedMonths.value.clear();
  }
);

onMounted(async () => {
  formData.value.accountBookId = billStore.currentAccountBook?.id;
  if (userStore.id) {
    if (billStore.accountBooks.length === 0) {
      await billStore.loadAccountBooks(userStore.id);
    }
    await billStore.loadClassifyAndTag(userStore.id);
  }
  initChart();
  await fetchData();
});
</script>

<template>
  <div class="bill-report">
    <ReportFilter
      v-model:accountBookId="formData.accountBookId"
      v-model:billType="formData.billType"
      v-model:month="formData.month"
      v-model:year="formData.year"
      v-model:dateRange="formData.dateRange"
      v-model:classifyList="formData.classifyList"
      v-model:remark="formData.remark"
      v-model:tagCodes="formData.tagCodes"
      @query="fetchData"
      @reset="handleReset"
    />

    <el-card v-loading="chartLoading" class="chart-card" shadow="never">
      <template #header>
        <div class="chart-header">
          <span class="total-info">
            <span class="total-item income">
              {{ t("dashboard.pureTotalIncome") }}: ¥{{
                formatAmount(trendData?.incomeTotal ?? 0)
              }}
            </span>
            <span class="total-item expense">
              {{ t("dashboard.pureTotalExpense") }}: ¥{{
                formatAmount(trendData?.expenseTotal ?? 0)
              }}
            </span>
            <span
              :class="
                (trendData?.incomeTotal ?? 0) -
                  (trendData?.expenseTotal ?? 0) >=
                0
                  ? 'balance-positive'
                  : 'balance-negative'
              "
              class="total-item"
            >
              {{ t("dashboard.pureBalance") }}: ¥{{
                formatAmount(
                  (trendData?.incomeTotal ?? 0) - (trendData?.expenseTotal ?? 0)
                )
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
            <el-radio-button value="time">{{
              t("bill.pureByTime")
            }}</el-radio-button>
            <el-radio-button value="amount">{{
              t("bill.pureByAmount")
            }}</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div v-loading="loading" class="detail-content">
        <template v-if="displayBillType === 'year'">
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
                  >{{ t("bill.pureIncome") }}: ¥{{
                    formatAmount(getMonthSummary(group.month).income)
                  }}</span
                >
                <span class="expense-text"
                  >{{ t("bill.pureExpense") }}: ¥{{
                    formatAmount(getMonthSummary(group.month).expense)
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
                  >{{ t("dashboard.pureBalance") }}: ¥{{
                    formatAmount(
                      getMonthSummary(group.month).income -
                        getMonthSummary(group.month).expense
                    )
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
              style="width: 100%"
              :span-method="
                (args: any) =>
                  groupDateSpanMethod({ ...args, data: group.records })
              "
            >
              <el-table-column
                :label="t('bill.pureDate')"
                width="100"
                align="center"
              >
                <template #default="{ row }">
                  {{ row.date }}
                </template>
              </el-table-column>
              <el-table-column :label="t('bill.pureAccountBook')" width="100">
                <template #default="{ row }">
                  {{ getAccountBookName(row.accountBookId) }}
                </template>
              </el-table-column>
              <el-table-column
                :label="t('bill.pureAmount')"
                width="130"
                align="right"
              >
                <template #default="{ row }">
                  <span
                    :style="{
                      color:
                        row.type === 'EXPENSE' || row.type === '0'
                          ? '#f56c6c'
                          : '#67c23a'
                    }"
                  >
                    ¥{{ formatAmount(row.amount) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column
                :label="t('bill.pureType')"
                width="80"
                align="center"
              >
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
                  <div class="classify-cell">
                    <span
                      v-if="row.subClassifyName"
                      class="classify-icon"
                    >
                      {{ getClassifyIcon(row.subClassifyImage) }}
                    </span>
                    <span v-else class="classify-icon">
                      {{ getClassifyIcon(row.mainClassifyImage) }}
                    </span>
                    <span>{{ row.mainClassifyName }}</span>
                    <span v-if="row.subClassifyName" class="sub-classify">
                      / {{ row.subClassifyName }}</span
                    >
                  </div>
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
        <div v-show="displayBillType !== 'year'">
          <el-table
            v-show="detailSortType === 'amount'"
            :data="
              (dateGroupedRecords[0]?.records || []).sort(
                (a, b) => b.amount - a.amount
              )
            "
            border
            stripe
            style="width: 100%"
          >
            <el-table-column
              :label="t('bill.pureDate')"
              width="100"
              align="center"
            >
              <template #default="{ row }">
                {{ row.date }}
              </template>
            </el-table-column>
            <el-table-column :label="t('bill.pureAccountBook')" width="100">
              <template #default="{ row }">
                {{ getAccountBookName(row.accountBookId) }}
              </template>
            </el-table-column>
            <el-table-column
              :label="t('bill.pureAmount')"
              width="130"
              align="right"
            >
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
                  }}¥{{ formatAmount(row.amount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column
              :label="t('bill.pureType')"
              width="80"
              align="center"
            >
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
                <div class="classify-cell">
                  <span
                    v-if="row.subClassifyName"
                    class="classify-icon"
                  >
                    {{ getClassifyIcon(row.subClassifyImage) }}
                  </span>
                  <span v-else class="classify-icon">
                    {{ getClassifyIcon(row.mainClassifyImage) }}
                  </span>
                  <span>{{ row.mainClassifyName }}</span>
                  <span v-if="row.subClassifyName" class="sub-classify">
                    / {{ row.subClassifyName }}</span
                  >
                </div>
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
          <el-table
            v-show="detailSortType === 'time'"
            :data="flatRecordsByTime"
            border
            stripe
            style="width: 100%"
            :span-method="dateSpanMethod"
          >
            <el-table-column
              :label="t('bill.pureDate')"
              width="100"
              align="center"
            >
              <template #default="{ row }">
                {{ row.date }}
              </template>
            </el-table-column>
            <el-table-column :label="t('bill.pureAccountBook')" width="100">
              <template #default="{ row }">
                {{ getAccountBookName(row.accountBookId) }}
              </template>
            </el-table-column>
            <el-table-column
              :label="t('bill.pureAmount')"
              width="130"
              align="right"
            >
              <template #default="{ row }">
                <span
                  :style="{
                    color:
                      row.type === 'EXPENSE' || row.type === '0'
                        ? '#f56c6c'
                        : '#67c23a'
                  }"
                >
                  ¥{{ formatAmount(row.amount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column
              :label="t('bill.pureType')"
              width="80"
              align="center"
            >
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
                <div class="classify-cell">
                  <span
                    v-if="row.subClassifyName"
                    class="classify-icon"
                  >
                    {{ getClassifyIcon(row.subClassifyImage) }}
                  </span>
                  <span v-else class="classify-icon">
                    {{ getClassifyIcon(row.mainClassifyImage) }}
                  </span>
                  <span>{{ row.mainClassifyName }}</span>
                  <span v-if="row.subClassifyName" class="sub-classify">
                    / {{ row.subClassifyName }}</span
                  >
                </div>
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
        <el-empty
          v-if="!loading && !trendData?.incomeExpenseList?.length"
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

        &.balance-positive {
          color: #67c23a;
        }

        &.balance-negative {
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
      align-items: center;
      justify-content: space-between;
    }

    .month-group {
      margin-bottom: 16px;

      .month-header {
        display: flex;
        align-items: center;
        padding: 8px 12px;
        margin-bottom: 8px;
        cursor: pointer;
        background: #f5f7fa;
        border-radius: 4px;

        .expand-icon {
          margin-right: 8px;
          color: #409eff;
        }

        .month-label {
          font-size: 14px;
          font-weight: 600;
        }

        .month-summary {
          margin-left: 30px;

          .income-text {
            margin-right: 16px;
            color: #67c23a;
          }

          .expense-text {
            margin-right: 16px;
            color: #f56c6c;
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
        padding: 8px 12px;
        margin-bottom: 8px;
        font-weight: 600;
        background: #f5f7fa;
        border-radius: 4px;
      }
    }
  }
}

.classify-cell {
  display: flex;
  align-items: center;
  gap: 4px;

  .classify-icon {
    font-size: 16px;
    margin-right: 2px;
  }

  .sub-classify {
    font-size: 12px;
    color: #909399;
  }
}
</style>
