<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from "vue";
import { useI18n } from "vue-i18n";
import * as echarts from "echarts";
import type { TrendData } from "@/types/bill";
import { getTrendData } from "@/api/incomeExpense";
import dayjs from "dayjs";
import quarterOfYear from "dayjs/plugin/quarterOfYear";

dayjs.extend(quarterOfYear);

defineOptions({
  name: "TrendChart"
});

interface Props {
  userId: number;
  accountBookId?: number;
}

const props = defineProps<Props>();
const { t } = useI18n();

const loading = ref(false);
const trendData = ref<TrendData | null>(null);
const activeMode = ref("month");

const chartRef = ref<HTMLDivElement>();
let chartInstance: echarts.ECharts | null = null;

const getDateRange = (mode: string) => {
  const now = dayjs();
  let beginDate: string;
  let endDate: string;
  let modeParam: string;

  switch (mode) {
    case "month":
      modeParam = "0";
      beginDate = now.startOf("month").format("YYYY-MM-DD");
      endDate = now.endOf("month").format("YYYY-MM-DD");
      break;
    case "quarter":
      modeParam = "0";
      beginDate = now.startOf("quarter").format("YYYY-MM-DD");
      endDate = now.endOf("quarter").format("YYYY-MM-DD");
      break;
    case "halfYear":
      modeParam = "0";
      beginDate = now.subtract(180, "day").format("YYYY-MM-DD");
      endDate = now.format("YYYY-MM-DD");
      break;
    case "year":
      modeParam = "1";
      beginDate = now.startOf("year").format("YYYY-MM-DD");
      endDate = now.endOf("year").format("YYYY-MM-DD");
      break;
    default:
      modeParam = "0";
      beginDate = now.startOf("month").format("YYYY-MM-DD");
      endDate = now.endOf("month").format("YYYY-MM-DD");
  }

  return { mode: modeParam, beginDate, endDate };
};

const fetchData = async (mode: string) => {
  if (!props.userId) return;

  loading.value = true;
  try {
    const { mode: modeParam, beginDate, endDate } = getDateRange(mode);
    const result = await getTrendData({
      userId: props.userId,
      accountBookId: props.accountBookId,
      mode: modeParam,
      queryMode: "0",
      beginDate,
      endDate
    });
    trendData.value = result;
    updateChart();
  } finally {
    loading.value = false;
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
  const dateLabels: string[] = [];
  const incomeData: number[] = [];
  const expenseData: number[] = [];

  if (trendData.value?.incomeExpenseSum) {
    const sortedKeys = Object.keys(trendData.value.incomeExpenseSum).sort();
    sortedKeys.forEach(date => {
      if (activeMode.value === "year") {
        dates.push(dayjs(date).format("YYYY-MM"));
        dateLabels.push(dayjs(date).format("YYYY-MM"));
      } else {
        dates.push(dayjs(date).format("MM-DD"));
        dateLabels.push(dayjs(date).format("YYYY-MM-DD"));
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
        const dateIndex = params[0].dataIndex;
        const fullDate = dateLabels[dateIndex];
        let result = fullDate + "<br/>";
        params.forEach((item: any) => {
          result += `${item.marker} ${item.seriesName}: ¥${Number(item.value).toFixed(2)}<br/>`;
        });
        return result;
      }
    },
    legend: {
      data: [t("dashboard.pureIncome"), t("dashboard.pureExpense")],
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
        name: t("dashboard.pureIncome"),
        type: "bar",
        data: incomeData,
        itemStyle: { color: "#67c23a" }
      },
      {
        name: t("dashboard.pureExpense"),
        type: "bar",
        data: expenseData,
        itemStyle: { color: "#f56c6c" }
      }
    ]
  };

  chartInstance.setOption(option);
};

const handleModeChange = (mode: string) => {
  activeMode.value = mode;
  fetchData(mode);
};

watch(
  () => props.userId,
  val => {
    if (val) {
      fetchData(activeMode.value);
    }
  }
);

watch(
  () => props.accountBookId,
  () => {
    if (props.userId) {
      fetchData(activeMode.value);
    }
  }
);

onMounted(() => {
  if (props.userId && props.accountBookId) {
    fetchData(activeMode.value);
  }
  initChart();
  window.addEventListener("resize", () => chartInstance?.resize());
});

onUnmounted(() => {
  window.removeEventListener("resize", () => chartInstance?.resize());
  chartInstance?.dispose();
});
</script>

<template>
  <el-card v-loading="loading" class="trend-chart" shadow="never">
    <template #header>
      <div class="card-header">
        <div class="header-left">
          <span class="chart-title">{{ t("dashboard.pureTrend") }}</span>
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
        <div class="header-right">
          <el-radio-group
            v-model="activeMode"
            size="small"
            @change="handleModeChange"
          >
            <el-radio-button value="month">{{ t('dashboard.pureMonth') }}</el-radio-button>
            <el-radio-button value="quarter">{{ t('dashboard.pureQuarter') }}</el-radio-button>
            <el-radio-button value="halfYear">{{ t('dashboard.pureHalfYear') }}</el-radio-button>
            <el-radio-button value="year">{{ t('dashboard.pureYear') }}</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </template>
    <div ref="chartRef" class="chart-container" />
  </el-card>
</template>

<style lang="scss" scoped>
.trend-chart {
  margin-bottom: 20px;

  .card-header {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    align-items: center;
    justify-content: space-between;
  }

  .header-left {
    display: flex;
    gap: 16px;
    align-items: center;
  }

  .chart-title {
    font-weight: 600;
  }

  .total-info {
    display: flex;
    gap: 16px;
    font-size: 14px;
    font-weight: normal;

    .total-item {
      &.income {
        color: #67c23a;
      }

      &.expense {
        color: #f56c6c;
      }
    }
  }

  .header-right {
    display: flex;
    gap: 12px;
    align-items: center;
  }

  .chart-container {
    width: 100%;
    height: 300px;
  }
}
</style>
