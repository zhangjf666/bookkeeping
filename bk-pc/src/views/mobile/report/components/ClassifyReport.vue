<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import dayjs from "dayjs";
import * as echarts from "echarts";
import type { ClassifyReportData, ClassifySummary, IncomeExpenseRecord } from "@/types/bill";
import { getClassifyReportData } from "@/api/incomeExpense";
import { useUserStoreHook } from "@/store/modules/user";
import { formatNumber } from "@/utils/format";
import { getClassifyIcon } from "@/utils/classifyIcons";
import {
  showLoading,
  hideLoading,
  showError
} from "@/utils/mobile/message";
import type { ReportFilterParams } from "./ReportFilter.vue";

defineOptions({
  name: "ClassifyReport"
});

const props = defineProps<{
  filterParams: ReportFilterParams;
}>();

const { t } = useI18n();
const router = useRouter();
const userStore = useUserStoreHook();

// 图表容器
const chartRef = ref<HTMLElement | null>(null);
let chartInstance: echarts.ECharts | null = null;

// 数据
const reportData = ref<ClassifyReportData | null>(null);
const loading = ref(false);

// 上一次的筛选参数（用于判断是否需要重新加载）
const lastFilterParams = ref<string>("");

// 当前选中的分类
const selectedClassify = ref<ClassifySummary | null>(null);

// 排序方式
const sortBy = ref<"time" | "amount">("time");

// 总收入
const totalIncome = computed(() => reportData.value?.incomeTotal || 0);

// 总支出
const totalExpense = computed(() => reportData.value?.expenseTotal || 0);

// 结余
const balance = computed(() => totalIncome.value - totalExpense.value);

// 分类列表
const classifyList = computed(() => {
  if (!reportData.value?.incomeExpenseSum) return [];

  const sum = reportData.value.incomeExpenseSum;
  const list: ClassifySummary[] = [];

  Object.values(sum).forEach(item => {
    list.push(item);
  });

  // 按金额排序
  return list.sort((a, b) => {
    const aAmount = a.expense || a.income || 0;
    const bAmount = b.expense || b.income || 0;
    return bAmount - aAmount;
  });
});

// 支出分类列表
const expenseClassifies = computed(() => {
  return classifyList.value.filter(item => item.expense > 0);
});

// 收入分类列表
const incomeClassifies = computed(() => {
  return classifyList.value.filter(item => item.income > 0);
});

// 图表数据 - 包含所有分类（支出和收入）
const chartData = computed(() => {
  return classifyList.value.map(item => ({
    name: item.classifyName,
    value: item.expense || item.income || 0,
    percent: item.percent,
    classify: item,
    isExpense: item.expense > 0
  }));
});

// 选中分类的明细列表
const selectedDetailList = computed(() => {
  if (!selectedClassify.value || !reportData.value?.incomeExpenseList) return [];

  // 从 classify 字段中提取分类ID（classify 格式可能是 "123" 或 "123-456"）
  const classifyStr = selectedClassify.value.classify;
  const classifyParts = classifyStr.split('-');
  const mainClassifyId = Number(classifyParts[0]);

  // 匹配主分类ID
  const list = reportData.value.incomeExpenseList.filter(record => {
    return record.mainClassify === mainClassifyId;
  });

  if (sortBy.value === "time") {
    return list.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
  } else {
    return list.sort((a, b) => b.amount - a.amount);
  }
});

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

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return;

  chartInstance = echarts.init(chartRef.value);

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: "item",
      formatter: (params: any) => {
        return `${params.name}<br/>¥${formatNumber(params.value)} (${params.data.percent.toFixed(1)}%)`;
      }
    },
    series: [
      {
        type: "pie",
        radius: ["40%", "65%"],
        center: ["50%", "50%"],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 4,
          borderColor: "#fff",
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: (params: any) => {
            return `${params.name}\n${params.data.percent.toFixed(1)}%`;
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
          },
          length: 10,
          length2: 10
        },
        data: chartData.value
      }
    ]
  };

  chartInstance.setOption(option);

  // 点击事件
  chartInstance.on("click", (params: any) => {
    if (params.data?.classify) {
      selectedClassify.value = params.data.classify;
    }
  });
};

// 更新图表数据
const updateChart = () => {
  if (!chartInstance) return;

  chartInstance.setOption({
    series: [
      {
        data: chartData.value
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
      queryMode: "1",
      beginDate: props.filterParams.beginDate,
      endDate: props.filterParams.endDate,
      accountBookId: props.filterParams.accountBookId,
      classifyList: props.filterParams.classifyList,
      remark: props.filterParams.remark,
      tagCodes: props.filterParams.tagCodes?.map(Number)
    };

    const data = await getClassifyReportData(params);
    reportData.value = data;
    selectedClassify.value = null;

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

// 选择分类
const handleClassifyClick = (item: ClassifySummary) => {
  selectedClassify.value = item;
};

// 返回分类列表
const handleBackToList = () => {
  selectedClassify.value = null;
};

// 获取分类图标
const getIcon = (record: IncomeExpenseRecord) => {
  return getClassifyIcon(
    record.subClassifyImage || record.mainClassifyImage || "other"
  );
};

// 获取分类名称
const getClassifyName = (record: IncomeExpenseRecord) => {
  const main = record.mainClassifyName || "";
  const sub = record.subClassifyName;
  return sub ? `${main}-${sub}` : main;
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
  <div class="classify-report">
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

    <!-- 分类列表 / 明细列表 -->
    <div class="list-container">
      <!-- 分类列表 -->
      <template v-if="!selectedClassify">
        <div class="list-header">
          <span class="list-title">{{ t("mobile.report.classifyList") }}</span>
        </div>

        <!-- 支出分类 -->
        <div v-if="expenseClassifies.length > 0" class="classify-section">
          <div class="section-title">{{ t("mobile.bill.expense") }}</div>
          <div
            v-for="item in expenseClassifies"
            :key="item.classify"
            class="classify-item"
            @click="handleClassifyClick(item)"
          >
            <div class="item-left">
              <span class="classify-icon">{{ getClassifyIcon(item.classifyImage) }}</span>
              <span class="classify-name">{{ item.classifyName }}</span>
            </div>
            <div class="item-right">
              <span class="classify-percent">{{ item.percent.toFixed(1) }}%</span>
              <span class="classify-amount expense">¥{{ formatNumber(item.expense) }}</span>
              <van-icon name="arrow" />
            </div>
          </div>
        </div>

        <!-- 收入分类 -->
        <div v-if="incomeClassifies.length > 0" class="classify-section">
          <div class="section-title">{{ t("mobile.bill.income") }}</div>
          <div
            v-for="item in incomeClassifies"
            :key="item.classify"
            class="classify-item"
            @click="handleClassifyClick(item)"
          >
            <div class="item-left">
              <span class="classify-icon">{{ getClassifyIcon(item.classifyImage) }}</span>
              <span class="classify-name">{{ item.classifyName }}</span>
            </div>
            <div class="item-right">
              <span class="classify-percent">{{ item.percent.toFixed(1) }}%</span>
              <span class="classify-amount income">¥{{ formatNumber(item.income) }}</span>
              <van-icon name="arrow" />
            </div>
          </div>
        </div>

        <van-empty
          v-if="classifyList.length === 0 && !loading"
          :description="t('mobile.bill.noData')"
        />
      </template>

      <!-- 明细列表 -->
      <template v-else>
        <div class="detail-header">
          <div class="header-left" @click="handleBackToList">
            <van-icon name="arrow-left" />
            <span>{{ t("mobile.report.backToList") }}</span>
          </div>
          <div class="header-row">
            <div class="header-info">
              <span class="classify-name">{{ selectedClassify.classifyName }}</span>
              <span class="classify-percent">{{ selectedClassify.percent.toFixed(1) }}%</span>
              <span class="record-count">{{ selectedClassify.num }}笔</span>
              <span
                class="classify-amount"
                :class="selectedClassify.expense > 0 ? 'expense' : 'income'"
              >
                ¥{{ formatNumber(selectedClassify.expense || selectedClassify.income) }}
              </span>
            </div>
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
        </div>

        <!-- 明细列表 -->
        <div class="detail-list">
          <div
            v-for="record in selectedDetailList"
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
            v-if="selectedDetailList.length === 0 && !loading"
            :description="t('mobile.bill.noData')"
          />
        </div>
      </template>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.classify-report {
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
  height: 280px;
}

.list-container {
  background-color: $color-card;
  border-top: 1px solid $color-border;
}

.list-header {
  padding: 12px 16px;
  border-bottom: 1px solid $color-border;
}

.list-title {
  font-size: 14px;
  font-weight: 500;
  color: $color-text-primary;
}

.classify-section {
  padding: 0 16px;
}

.section-title {
  padding: 12px 0 8px;
  font-size: 12px;
  color: $color-text-secondary;
}

.classify-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  cursor: pointer;
  border-bottom: 1px solid $color-border;

  &:last-child {
    border-bottom: none;
  }
}

.item-left {
  display: flex;
  gap: 12px;
  align-items: center;

  .classify-icon {
    font-size: 24px;
  }

  .classify-name {
    font-size: 14px;
    color: $color-text-primary;
  }
}

.item-right {
  display: flex;
  gap: 8px;
  align-items: center;

  .classify-percent {
    font-size: 12px;
    color: $color-text-secondary;
  }

  .classify-amount {
    font-size: 14px;
    font-weight: 500;

    &.expense {
      color: #f56c6c;
    }

    &.income {
      color: #67c23a;
    }
  }
}

.detail-header {
  padding: 12px 16px;
  background-color: rgba(#f56c6c, 0.05);
  border-bottom: 1px solid $color-border;
}

.header-left {
  display: flex;
  gap: 4px;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  color: $color-text-secondary;
  cursor: pointer;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-info {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;

  .classify-name {
    font-size: 16px;
    font-weight: 500;
    color: $color-text-primary;
  }

  .classify-percent {
    font-size: 14px;
    color: $color-text-secondary;
  }

  .record-count {
    font-size: 12px;
    color: $color-text-secondary;
  }

  .classify-amount {
    font-size: 16px;
    font-weight: 600;

    &.expense {
      color: #f56c6c;
    }

    &.income {
      color: #67c23a;
    }
  }
}

.sort-options {
  display: flex;
  gap: 8px;
}

.detail-list {
  padding: 0 16px;
}

.record-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
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
