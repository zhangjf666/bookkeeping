<script setup lang="ts">
import { ref, watch, computed } from "vue";
import { useI18n } from "vue-i18n";
import * as echarts from "echarts";
import type { TrendData } from "@/types/dashboard";
import dayjs from "dayjs";

defineOptions({
  name: "TrendChart"
});

interface Props {
  data: TrendData | null;
  loading: boolean;
}

const props = defineProps<Props>();
const { t } = useI18n();

const chartRef = ref<HTMLDivElement>();
let chartInstance: echarts.ECharts | null = null;

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

  if (props.data?.incomeExpenseSum) {
    const sortedKeys = Object.keys(props.data.incomeExpenseSum).sort();
    sortedKeys.forEach(date => {
      dates.push(dayjs(date).format("MM-DD"));
      const item = props.data.incomeExpenseSum[date];
      incomeData.push(Number(item.income) || 0);
      expenseData.push(Number(item.expense) || 0);
    });
  }

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: "axis"
    },
    legend: {
      data: [t("dashboard.pureIncome"), t("dashboard.pureExpense")]
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      containLabel: true
    },
    xAxis: {
      type: "category",
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: "value"
    },
    series: [
      {
        name: t("dashboard.pureIncome"),
        type: "line",
        smooth: true,
        data: incomeData,
        itemStyle: { color: "#67c23a" },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(103, 194, 58, 0.3)" },
            { offset: 1, color: "rgba(103, 194, 58, 0.05)" }
          ])
        }
      },
      {
        name: t("dashboard.pureExpense"),
        type: "line",
        smooth: true,
        data: expenseData,
        itemStyle: { color: "#f56c6c" },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(245, 108, 108, 0.3)" },
            { offset: 1, color: "rgba(245, 108, 108, 0.05)" }
          ])
        }
      }
    ]
  };

  chartInstance.setOption(option);
};

watch(
  () => props.data,
  () => {
    updateChart();
  },
  { deep: true }
);

watch(
  () => props.loading,
  val => {
    if (!val && chartInstance) {
      updateChart();
    }
  }
);

const init = () => {
  if (!props.loading && props.data) {
    initChart();
  }
};

watch(
  () => props.loading,
  val => {
    if (!val) {
      setTimeout(initChart, 100);
    }
  }
);

import { onMounted, onUnmounted } from "vue";

onMounted(() => {
  initChart();
  window.addEventListener("resize", () => chartInstance?.resize());
});

onUnmounted(() => {
  window.removeEventListener("resize", () => chartInstance?.resize());
  chartInstance?.dispose();
});
</script>

<template>
  <el-card class="trend-chart" shadow="never">
    <template #header>
      <div class="card-header">
        <span>{{ t("dashboard.pureTrend") }}</span>
      </div>
    </template>
    <div ref="chartRef" class="chart-container" />
  </el-card>
</template>

<style lang="scss" scoped>
.trend-chart {
  margin-bottom: 20px;

  .card-header {
    font-weight: 600;
  }

  .chart-container {
    width: 100%;
    height: 300px;
  }
}
</style>
