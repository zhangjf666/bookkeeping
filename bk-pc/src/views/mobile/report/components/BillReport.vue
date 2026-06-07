<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import dayjs from "dayjs";
import * as echarts from "echarts";
import type { TrendData, IncomeExpenseRecord, DaySum } from "@/types/bill";
import { getTrendData } from "@/api/incomeExpense";
import { useUserStoreHook } from "@/store/modules/user";
import { useClassifyStore } from "@/store/modules/classify";
import { formatNumber } from "@/utils/format";
import { getClassifyIcon } from "@/utils/classifyIcons";
import {
  showLoading,
  hideLoading,
  showError
} from "@/utils/mobile/message";
import type { ReportFilterParams } from "./ReportFilter.vue";

defineOptions({
  name: "BillReport"
});

const props = defineProps<{
  filterParams: ReportFilterParams;
}>();

const { t } = useI18n();
const router = useRouter();
const userStore = useUserStoreHook();
const classifyStore = useClassifyStore();

// 图表容器
const chartRef = ref<HTMLElement | null>(null);
let chartInstance: echarts.ECharts | null = null;

// 数据
const trendData = ref<TrendData | null>(null);
const loading = ref(false);

// 上一次的筛选参数（用于判断是否需要重新加载）
const lastFilterParams = ref<string>("");

// 排序方式
const sortBy = ref<"time" | "amount">("time");

// 展开的月份（年账单模式）
const expandedMonths = ref<Set<string>>(new Set());

// 总收入
const totalIncome = computed(() => trendData.value?.incomeTotal || 0);

// 总支出
const totalExpense = computed(() => trendData.value?.expenseTotal || 0);

// 结余
const balance = computed(() => totalIncome.value - totalExpense.value);

// 是否是年账单模式
const isYearMode = computed(() => props.filterParams.billType === "year");

// 图表数据
const chartData = computed(() => {
  if (!trendData.value?.incomeExpenseSum) return { dates: [], incomes: [], expenses: [] };

  const sum = trendData.value.incomeExpenseSum;
  const dates: string[] = [];
  const incomes: number[] = [];
  const expenses: number[] = [];

  // 按日期排序
  const sortedKeys = Object.keys(sum).sort((a, b) => a.localeCompare(b));

  sortedKeys.forEach(key => {
    const data = sum[key];
    if (isYearMode.value) {
      // 年账单显示月份
      dates.push(key.slice(0, 7));
    } else {
      // 月账单/自定义显示日期
      dates.push(key.slice(5));
    }
    incomes.push(data.income || 0);
    expenses.push(data.expense || 0);
  });

  return { dates, incomes, expenses };
});

// 明细列表（排序后）
const sortedList = computed(() => {
  if (!trendData.value?.incomeExpenseList) return [];

  const list = [...trendData.value.incomeExpenseList];

  if (sortBy.value === "time") {
    return list.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
  } else {
    return list.sort((a, b) => b.amount - a.amount);
  }
});

// 按月分组的明细（年账单模式）
const groupedByMonth = computed(() => {
  if (!isYearMode.value || !sortedList.value.length) return [];

  const groups: Map<string, { month: string; income: number; expense: number; records: IncomeExpenseRecord[] }> = new Map();

  sortedList.value.forEach(record => {
    const month = record.date.slice(0, 7);
    if (!groups.has(month)) {
      groups.set(month, {
        month,
        income: 0,
        expense: 0,
        records: []
      });
    }
    const group = groups.get(month)!;
    group.records.push(record);
    if (record.type === "EXPENSE") {
      group.expense += record.amount;
    } else {
      group.income += record.amount;
    }
  });

  return Array.from(groups.values()).sort((a, b) => b.month.localeCompare(a.month));
});

// 切换月份展开
const toggleMonthExpand = (month: string) => {
  if (expandedMonths.value.has(month)) {
    expandedMonths.value.delete(month);
  } else {
    expandedMonths.value.add(month);
  }
};

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return;

  chartInstance = echarts.init(chartRef.value);

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: "axis",
      className: "report-chart-tooltip",
      axisPointer: {
        type: "shadow"
      },
      formatter: (params: any) => {
        const date = params[0].axisValue;
        let result = `${date}<br/>`;
        params.forEach((item: any) => {
          const color = item.seriesName === t("mobile.bill.income") ? "#67c23a" : "#f56c6c";
          result += `<span style="color:${color}">● ${item.seriesName}: ¥${formatNumber(item.value)}</span><br/>`;
        });
        return result;
      }
    },
    legend: {
      data: [t("mobile.bill.income"), t("mobile.bill.expense")],
      top: 10,
      textStyle: {
        fontSize: 12
      }
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      top: 50,
      containLabel: true
    },
    xAxis: {
      type: "category",
      data: chartData.value.dates,
      axisLabel: {
        fontSize: 10,
        rotate: chartData.value.dates.length > 10 ? 45 : 0
      }
    },
    yAxis: {
      type: "value",
      axisLabel: {
        formatter: (value: number) => {
          if (value >= 10000) {
            return `${(value / 10000).toFixed(1)}万`;
          }
          return value.toString();
        }
      }
    },
    series: [
      {
        name: t("mobile.bill.income"),
        type: "bar",
        data: chartData.value.incomes,
        itemStyle: {
          color: "#67c23a"
        },
        barWidth: "30%"
      },
      {
        name: t("mobile.bill.expense"),
        type: "bar",
        data: chartData.value.expenses,
        itemStyle: {
          color: "#f56c6c"
        },
        barWidth: "30%"
      }
    ]
  };

  chartInstance.setOption(option);
};

// 更新图表数据
const updateChart = () => {
  if (!chartInstance) return;

  chartInstance.setOption({
    xAxis: {
      data: chartData.value.dates
    },
    series: [
      {
        data: chartData.value.incomes
      },
      {
        data: chartData.value.expenses
      }
    ]
  });
};

// 加载数据
const loadData = async () => {
  const userId = userStore.id;
  if (!userId) return;

  loading.value = true;
  showLoading(t("mobile.common.loading"));

  try {
    const params = {
      userId,
      mode: props.filterParams.billType === "year" ? "1" : "0",
      queryMode: "0",
      beginDate: props.filterParams.beginDate,
      endDate: props.filterParams.endDate,
      accountBookId: props.filterParams.accountBookId,
      classifyList: props.filterParams.classifyList,
      remark: props.filterParams.remark,
      tagCodes: props.filterParams.tagCodes?.map(Number)
    };

    const data = await getTrendData(params);
    trendData.value = data;

    await nextTick();
    if (!chartInstance) {
      initChart();
    } else {
      updateChart();
    }
  } catch (error: any) {
    showError(error?.message || t("mobile.common.failed"));
  } finally {
    hideLoading();
    loading.value = false;
  }
};

// 从classifyStore查找分类图标
const getClassifyImageFromStore = (classifyId: number | null): string => {
  if (!classifyId) return "";
  const classify = classifyStore.list.find(c => c.id === classifyId);
  return classify ? classify.image : "";
};

// 获取分类图标
const getIcon = (record: IncomeExpenseRecord) => {
  const subImage = getClassifyImageFromStore(record.subClassify);
  if (subImage) {
    return getClassifyIcon(subImage);
  }
  const mainImage = getClassifyImageFromStore(record.mainClassify);
  return getClassifyIcon(mainImage || "other");
};

// 获取分类名称
const getClassifyName = (record: IncomeExpenseRecord) => {
  const main = record.mainClassifyName || "";
  const sub = record.subClassifyName;
  return sub ? `${main}-${sub}` : main;
};

// 格式化日期为年份
const formatDateYear = (dateStr: string) => {
  const date = dayjs(dateStr);
  return date.format("YYYY");
};

// 格式化日期为月日
const formatDateMonthDay = (dateStr: string) => {
  const date = dayjs(dateStr);
  return date.format("MM-DD");
};

// 格式化月份显示
const formatMonthDisplay = (month: string) => {
  const date = dayjs(month);
  return date.format("YYYY-MM");
};

// 点击账单项 - 查看详情
const handleRecordClick = (record: IncomeExpenseRecord) => {
  // 使用 sessionStorage 传递数据
  sessionStorage.setItem("viewRecordData", JSON.stringify(record));
  router.push(`/record-detail/${record.id}`);
};

// 窗口大小变化时重新调整图表
const handleResize = () => {
  chartInstance?.resize();
};

// 暴露 resize 方法给父组件
defineExpose({
  resize: handleResize
});

// 监听筛选参数变化
watch(
  () => props.filterParams,
  (newParams) => {
    // 只有当 accountBookId 有值时才加载数据
    if (newParams.accountBookId === undefined) {
      return;
    }
    const paramsStr = JSON.stringify(newParams);
    if (paramsStr !== lastFilterParams.value) {
      lastFilterParams.value = paramsStr;
      loadData();
    }
  },
  { deep: true }
);

onMounted(() => {
  // 只有当 accountBookId 有值时才加载数据
  if (props.filterParams.accountBookId !== undefined) {
    const paramsStr = JSON.stringify(props.filterParams);
    if (paramsStr !== lastFilterParams.value) {
      lastFilterParams.value = paramsStr;
      loadData();
    }
  }
  window.addEventListener("resize", handleResize);
});

onUnmounted(() => {
  chartInstance?.dispose();
  window.removeEventListener("resize", handleResize);
});
</script>

<template>
  <div class="bill-report">
    <!-- 统计卡片 -->
    <div class="stats-card">
      <div class="stat-item">
        <div class="stat-label">{{ t("mobile.bill.totalIncome") }}</div>
        <div class="stat-value income">¥{{ formatNumber(totalIncome) }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">{{ t("mobile.bill.totalExpense") }}</div>
        <div class="stat-value expense">¥{{ formatNumber(totalExpense) }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">{{ t("mobile.report.balance") }}</div>
        <div class="stat-value" :class="balance >= 0 ? 'income' : 'expense'">
          {{ balance >= 0 ? "+" : "-" }}¥{{ formatNumber(Math.abs(balance)) }}
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-container">
      <div ref="chartRef" class="chart" />
    </div>

    <!-- 排序选项 -->
    <div class="sort-bar">
      <div class="sort-label">{{ t("mobile.report.detail") }}</div>
      <div class="sort-options">
        <van-button
          size="small"
          :type="sortBy === 'time' ? 'danger' : 'default'"
          @click="sortBy = 'time'"
        >
          {{ t("mobile.bill.sortByTime") }}
        </van-button>
        <van-button
          size="small"
          :type="sortBy === 'amount' ? 'danger' : 'default'"
          @click="sortBy = 'amount'"
        >
          {{ t("mobile.bill.sortByAmount") }}
        </van-button>
      </div>
    </div>

    <!-- 明细列表 -->
    <div class="detail-list">
      <!-- 年账单模式：按月分组 -->
      <template v-if="isYearMode">
        <div
          v-for="group in groupedByMonth"
          :key="group.month"
          class="month-group"
        >
          <div class="group-header" @click="toggleMonthExpand(group.month)">
            <div class="month-info">
              <span class="month-name">{{ formatMonthDisplay(group.month) }}</span>
              <van-icon
                :name="expandedMonths.has(group.month) ? 'arrow-up' : 'arrow-down'"
              />
            </div>
            <div class="month-summary">
              <span class="summary-item">
                <span class="summary-label">{{ t("mobile.bill.totalIncome") }}：</span>
                <span class="income">¥{{ formatNumber(group.income) }}</span>
              </span>
              <span class="summary-item">
                <span class="summary-label">{{ t("mobile.bill.totalExpense") }}：</span>
                <span class="expense">¥{{ formatNumber(group.expense) }}</span>
              </span>
              <span class="summary-item">
                <span class="summary-label">{{ t("mobile.report.balance") }}：</span>
                <span :class="group.income - group.expense >= 0 ? 'income' : 'expense'">
                  {{ group.income - group.expense >= 0 ? "+" : "-" }}¥{{ formatNumber(Math.abs(group.income - group.expense)) }}
                </span>
              </span>
            </div>
          </div>

          <!-- 月度明细 -->
          <div v-if="expandedMonths.has(group.month)" class="group-records">
            <div
              v-for="record in group.records"
              :key="record.id"
              class="record-item"
              @click="handleRecordClick(record)"
            >
              <!-- 日期 -->
              <div class="date-section">
                <div class="year">{{ formatDateYear(record.date) }}</div>
                <div class="day">{{ formatDateMonthDay(record.date) }}</div>
              </div>
              <!-- 分类图标 -->
              <div class="item-icon">{{ getIcon(record) }}</div>
              <!-- 分类和备注 -->
              <div class="item-info">
                <div class="classify-name">{{ getClassifyName(record) }}</div>
                <div v-if="record.remark" class="remark">
                  {{ record.remark }}
                </div>
              </div>
              <!-- 金额 -->
              <div
                class="item-amount"
                :class="record.type === 'EXPENSE' ? 'expense' : 'income'"
              >
                ¥{{ formatNumber(Math.abs(record.amount)) }}
              </div>
            </div>
          </div>
        </div>

        <van-empty
          v-if="groupedByMonth.length === 0 && !loading"
          :description="t('mobile.bill.noData')"
        />
      </template>

      <!-- 月账单/自定义模式：直接显示明细 -->
      <template v-else>
        <div
          v-for="record in sortedList"
          :key="record.id"
          class="record-item"
          @click="handleRecordClick(record)"
        >
          <!-- 日期 -->
          <div class="date-section">
            <div class="year">{{ formatDateYear(record.date) }}</div>
            <div class="day">{{ formatDateMonthDay(record.date) }}</div>
          </div>
          <!-- 分类图标 -->
          <div class="item-icon">{{ getIcon(record) }}</div>
          <!-- 分类和备注 -->
          <div class="item-info">
            <div class="classify-name">{{ getClassifyName(record) }}</div>
            <div v-if="record.remark" class="remark">
              {{ record.remark }}
            </div>
          </div>
          <!-- 金额 -->
          <div
            class="item-amount"
            :class="record.type === 'EXPENSE' ? 'expense' : 'income'"
          >
            ¥{{ formatNumber(Math.abs(record.amount)) }}
          </div>
        </div>

        <van-empty
          v-if="sortedList.length === 0 && !loading"
          :description="t('mobile.bill.noData')"
        />
      </template>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.bill-report {
  min-height: 100%;
  background-color: $color-background;
}

.stats-card {
  display: flex;
  padding: 16px;
  background-color: $color-card;
  border-bottom: 1px solid $color-border;
}

.stat-item {
  flex: 1;
  text-align: center;

  .stat-label {
    margin-bottom: 4px;
    font-size: 12px;
    color: $color-text-secondary;
  }

  .stat-value {
    font-size: 16px;
    font-weight: 600;

    &.income {
      color: #67c23a;
    }

    &.expense {
      color: #f56c6c;
    }
  }
}

.chart-container {
  padding: 16px;
  background-color: $color-card;
}

.chart {
  width: 100%;
  height: 250px;
}

.sort-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: $color-card;
  border-top: 1px solid $color-border;
  border-bottom: 1px solid $color-border;
}

.sort-label {
  font-size: 14px;
  font-weight: 500;
  color: $color-text-primary;
}

.sort-options {
  display: flex;
  gap: 8px;
}

.detail-list {
  padding: 12px;
}

.month-group {
  margin-bottom: 12px;
  background-color: $color-card;
  border-radius: 12px;
  overflow: hidden;
}

.group-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid $color-border;
}

.month-info {
  display: flex;
  gap: 8px;
  align-items: center;

  .month-name {
    font-size: 14px;
    font-weight: 500;
    color: $color-text-primary;
  }
}

.month-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;

  .summary-item {
    display: flex;
    align-items: center;
    font-size: 12px;

    .summary-label {
      color: $color-text-secondary;
    }
  }

  .income {
    color: #67c23a;
  }

  .expense {
    color: #f56c6c;
  }
}

.group-records {
  padding: 0;
}

.record-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background-color: $color-card;
  border-bottom: 1px solid $color-border;

  &:last-child {
    border-bottom: none;
  }
}

.date-section {
  width: 48px;
  text-align: center;

  .year {
    font-size: 12px;
    color: $color-text-secondary;
  }

  .day {
    font-size: 14px;
    font-weight: 500;
    color: $color-text-primary;
  }
}

.item-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  margin: 0 12px;
  font-size: 22px;
  background-color: $color-background;
  border-radius: 8px;
}

.item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;

  .classify-name {
    overflow: hidden;
    font-size: 14px;
    color: $color-text-primary;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .remark {
    margin-top: 4px;
    overflow: hidden;
    font-size: 12px;
    color: $color-text-secondary;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.item-amount {
  font-size: 14px;
  font-weight: 500;

  &.expense {
    color: #f56c6c;
  }

  &.income {
    color: #67c23a;
  }
}
</style>
