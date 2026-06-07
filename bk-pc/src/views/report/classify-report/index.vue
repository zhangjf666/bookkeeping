<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import * as echarts from "echarts";
import dayjs from "dayjs";
import { ElMessage } from "element-plus";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import { getClassifyReportData } from "@/api/incomeExpense";
import { getClassifyIcon } from "@/utils/classifyIcons";
import { formatAmount } from "@/utils/format";
import type {
  ClassifyReportData,
  ClassifySummary,
  IncomeExpenseRecord
} from "@/types/bill";
import ReportFilter from "../components/ReportFilter.vue";

defineOptions({
  name: "ClassifyReport"
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

const reportData = ref<ClassifyReportData | null>(null);

const detailSortType = ref<"time" | "amount">("time");
const selectedClassify = ref<string | null>(null);

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

const isIncomeClassify = () => {
  if (!selectedClassifyData.value) return false;
  return (
    selectedClassifyData.value.type === "INCOME" ||
    selectedClassifyData.value.income > 0
  );
};

const getQueryParams = () => {
  const userId = userStore.id;
  if (!userId) return null;

  let mode = "0";
  if (formData.value.billType === "year") {
    mode = "1";
  } else if (formData.value.billType === "custom") {
    mode = "0";
  }

  let beginDate = "";
  let endDate = "";

  if (formData.value.billType === "month") {
    beginDate = dayjs(formData.value.month + "-01")
      .startOf("month")
      .format("YYYY-MM-DD");
    endDate = dayjs(formData.value.month + "-01")
      .endOf("month")
      .format("YYYY-MM-DD");
  } else if (formData.value.billType === "year") {
    beginDate = dayjs(formData.value.year + "-01-01")
      .startOf("year")
      .format("YYYY-MM-DD");
    endDate = dayjs(formData.value.year + "-01-01")
      .endOf("year")
      .format("YYYY-MM-DD");
  } else if (formData.value.dateRange) {
    beginDate = formData.value.dateRange[0];
    endDate = formData.value.dateRange[1];
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
    queryMode: "1",
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
    const result = await getClassifyReportData(params);
    reportData.value = result;

    if (
      result.incomeExpenseSum &&
      Object.keys(result.incomeExpenseSum).length > 0
    ) {
      const firstClassifyId = Object.keys(result.incomeExpenseSum)[0];
      selectedClassify.value = firstClassifyId;
    } else {
      selectedClassify.value = null;
    }

    initChart();
  } catch (error: any) {
    ElMessage.error(error.message || "获取数据失败");
  } finally {
    loading.value = false;
    chartLoading.value = false;
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
  fetchData();
};

const initChart = () => {
  if (!chartRef.value || !reportData.value?.incomeExpenseSum) return;

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value);
  }

  const classifyData = Object.values(reportData.value.incomeExpenseSum).map(
    (item: ClassifySummary) => ({
      name: item.classifyName,
      value: item.expense || item.income || 0,
      percent: item.percent,
      num: item.num,
      classify: item.classify,
      classifyImage: item.classifyImage,
      type: item.type
    })
  );

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: "item",
      formatter: (params: any) => {
        const data = params.data;
        return `${data.classifyImage ? "" : ""}${data.name}<br/>${t("bill.purePercent")}: ${data.percent}%<br/>${t("bill.pureRecordCount")}: ${data.num}<br/>${t("bill.pureAmount")}: ¥${formatAmount(data.value)}`;
      }
    },
    legend: {
      orient: "vertical",
      left: 10,
      top: "center",
      formatter: (name: string) => {
        const item = classifyData.find((d: any) => d.name === name);
        if (item) {
          return `{name|${name}} {percent|${item.percent}%}`;
        }
        return name;
      },
      textStyle: {
        rich: {
          name: {
            fontSize: 14,
            padding: [0, 10, 0, 0]
          },
          percent: {
            fontSize: 12,
            color: "#999"
          }
        }
      }
    },
    series: [
      {
        type: "pie",
        radius: ["40%", "70%"],
        center: ["45%", "50%"],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 10,
          borderColor: "#fff",
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: (params: any) => {
            return `${params.name}\n${params.percent.toFixed(1)}%`;
          },
          fontSize: 11,
          color: "#333",
          lineHeight: 16
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 13,
            fontWeight: "bold"
          }
        },
        labelLine: {
          show: true,
          lineStyle: {
            color: "#999"
          }
        },
        data: classifyData
      }
    ]
  };

  chartInstance.setOption(option);

  chartInstance.off("click");
  chartInstance.on("click", (params: any) => {
    if (params.data && params.data.classify) {
      selectedClassify.value = String(params.data.classify);
    }
  });
};

const selectedClassifyData = computed(() => {
  if (!selectedClassify.value || !reportData.value?.incomeExpenseSum) {
    return null;
  }
  return reportData.value.incomeExpenseSum[selectedClassify.value];
});

const filteredRecords = computed(() => {
  if (!reportData.value?.incomeExpenseList || !selectedClassify.value) {
    return [];
  }

  return reportData.value.incomeExpenseList.filter(
    (record: IncomeExpenseRecord) =>
      record.mainClassify === Number(selectedClassify.value)
  );
});

const sortedRecords = computed(() => {
  const records = [...filteredRecords.value];

  if (detailSortType.value === "amount") {
    records.sort((a, b) => b.amount - a.amount);
    return records;
  }

  records.sort((a, b) => b.date.localeCompare(a.date));
  return records;
});

const flatRecordsByTime = computed(() => {
  return [...sortedRecords.value].sort((a, b) => b.date.localeCompare(a.date));
});

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

watch(
  () => billStore.currentAccountBook,
  val => {
    if (val && !formData.value.accountBookId) {
      formData.value.accountBookId = val.id;
    }
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
  <div class="classify-report">
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
                formatAmount(reportData?.incomeTotal ?? 0)
              }}
            </span>
            <span class="total-item expense">
              {{ t("dashboard.pureTotalExpense") }}: ¥{{
                formatAmount(reportData?.expenseTotal ?? 0)
              }}
            </span>
            <span
              :class="
                (reportData?.incomeTotal ?? 0) -
                  (reportData?.expenseTotal ?? 0) >=
                0
                  ? 'balance-positive'
                  : 'balance-negative'
              "
              class="total-item"
            >
              {{ t("dashboard.pureBalance") }}: ¥{{
                formatAmount(
                  (reportData?.incomeTotal ?? 0) -
                    (reportData?.expenseTotal ?? 0)
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
          <span v-if="selectedClassifyData" class="classify-info">
            <span class="classify-icon">{{
              getClassifyIcon(selectedClassifyData.classifyImage)
            }}</span>
            <span class="classify-name">{{
              selectedClassifyData.classifyName
            }}</span>
            <span class="classify-percent"
              >{{ t("bill.purePercent") }}：{{
                selectedClassifyData.percent
              }}%</span
            >
            <span class="classify-num"
              >{{ t("bill.pureRecordCount") }}：{{
                selectedClassifyData.num
              }}</span
            >
            <span
              :class="
                isIncomeClassify()
                  ? 'classify-amount-income'
                  : 'classify-amount-expense'
              "
              class="classify-amount"
            >
              {{
                isIncomeClassify()
                  ? t("bill.pureIncome")
                  : t("bill.pureExpense")
              }}：¥{{
                formatAmount(
                  selectedClassifyData.expense ||
                    selectedClassifyData.income ||
                    0
                )
              }}
            </span>
          </span>
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
        <template v-if="selectedClassify">
          <el-table
            v-show="detailSortType === 'amount'"
            :data="sortedRecords"
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
            <el-table-column :label="t('bill.pureClassify')" width="200">
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
              min-width="200"
            />
            <el-table-column :label="t('bill.pureTag')" min-width="200">
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
            <el-table-column :label="t('bill.pureClassify')" width="200">
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
              min-width="200"
            />
            <el-table-column :label="t('bill.pureTag')" min-width="200">
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
        <el-empty
          v-if="
            !loading &&
            (!reportData?.incomeExpenseSum ||
              Object.keys(reportData.incomeExpenseSum).length === 0)
          "
          :description="t('dashboard.pureNoRecords')"
        />
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.classify-report {
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
      height: 400px;
    }
  }

  .detail-card {
    .detail-header {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .classify-info {
        display: flex;
        gap: 12px;
        align-items: center;

        .classify-icon {
          font-size: 20px;
        }

        .classify-name {
          font-size: 14px;
          font-weight: 600;
        }

        .classify-percent {
          color: #409eff;
        }

        .classify-num {
          color: #909399;
        }

        .classify-amount-expense {
          color: #f56c6c;
        }

        .classify-amount-income {
          color: #67c23a;
        }
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
