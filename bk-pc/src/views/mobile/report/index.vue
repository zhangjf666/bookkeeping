<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { showNotify } from "vant";
import dayjs from "dayjs";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import { getTrendData, getClassifyReportData } from "@/api/incomeExpense";
import type { TrendData, ClassifyReportData } from "@/types/bill";
import { formatNumber } from "@/utils/format";

defineOptions({
  name: "MobileReport"
});

const router = useRouter();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

const loading = ref(false);
const activeTab = ref(0);
const currentDate = dayjs();

// 查询模式：month-月度，year-年度
const queryMode = ref<"month" | "year">("month");

// 查询参数
const queryParams = computed(() => {
  if (queryMode.value === "month") {
    return {
      mode: "MONTH",
      beginDate: currentDate.startOf("month").format("YYYY-MM-DD"),
      endDate: currentDate.endOf("month").format("YYYY-MM-DD")
    };
  } else {
    return {
      mode: "YEAR",
      beginDate: currentDate.startOf("year").format("YYYY-MM-DD"),
      endDate: currentDate.endOf("year").format("YYYY-MM-DD")
    };
  }
});

// 趋势数据
const trendData = ref<TrendData | null>(null);

// 分类数据
const classifyData = ref<ClassifyReportData | null>(null);

const userId = computed(() => userStore.id);

// 加载数据
const loadData = async () => {
  if (!userId.value) return;

  loading.value = true;
  try {
    const accountBookId = billStore.currentAccountBook?.id;

    if (activeTab.value === 0) {
      // 趋势数据
      const result = await getTrendData({
        userId: userId.value,
        accountBookId,
        ...queryParams.value
      });
      trendData.value = result;
    } else {
      // 分类数据
      const result = await getClassifyReportData({
        userId: userId.value,
        accountBookId,
        ...queryParams.value
      });
      classifyData.value = result;
    }
  } catch (error: any) {
    showNotify({ type: "danger", message: error?.message || "加载失败" });
  } finally {
    loading.value = false;
  }
};

// 切换查询模式
const toggleQueryMode = (mode: "month" | "year") => {
  queryMode.value = mode;
  loadData();
};

// 切换 Tab
const onTabChange = () => {
  loadData();
};

// 查看详情
const goDetail = () => {
  router.push("/m/report/detail");
};

onMounted(() => {
  if (!userStore.id) {
    router.replace("/m/login");
    return;
  }
  loadData();
});
</script>

<template>
  <div class="mobile-report">
    <!-- 时间选择 -->
    <div class="time-selector">
      <div
        class="tab"
        :class="{ active: queryMode === 'month' }"
        @click="toggleQueryMode('month')"
      >
        本月
      </div>
      <div
        class="tab"
        :class="{ active: queryMode === 'year' }"
        @click="toggleQueryMode('year')"
      >
        本年
      </div>
    </div>

    <!-- 汇总卡片 -->
    <div class="summary-card">
      <div class="summary-item">
        <span class="label">支出</span>
        <span class="value expense">¥{{ formatNumber(trendData?.expenseTotal || 0) }}</span>
      </div>
      <div class="summary-item">
        <span class="label">收入</span>
        <span class="value income">¥{{ formatNumber(trendData?.incomeTotal || 0) }}</span>
      </div>
      <div class="summary-item">
        <span class="label">结余</span>
        <span class="value">¥{{ formatNumber((trendData?.incomeTotal || 0) - (trendData?.expenseTotal || 0)) }}</span>
      </div>
    </div>

    <!-- Tab 切换 -->
    <van-tabs v-model:active="activeTab" @change="onTabChange">
      <van-tab title="收支趋势">
        <van-loading v-if="loading" class="loading" />
        <div v-else class="chart-section">
          <div class="chart-placeholder">
            <van-icon name="bar-chart-o" size="48" color="#ccc" />
            <p>图表展示区域</p>
            <p class="hint">可集成 ECharts 展示趋势图</p>
          </div>
        </div>
      </van-tab>

      <van-tab title="分类统计">
        <van-loading v-if="loading" class="loading" />
        <div v-else class="classify-section">
          <div
            v-for="(item, key) in classifyData?.incomeExpenseSum"
            :key="key"
            class="classify-item"
          >
            <div class="classify-info">
              <span class="name">{{ item.classifyName }}</span>
              <span class="percent">{{ item.percent.toFixed(1) }}%</span>
            </div>
            <div class="classify-bar">
              <div class="bar" :style="{ width: `${item.percent}%` }"></div>
            </div>
            <div class="classify-amount">
              ¥{{ formatNumber(item.expense || item.income) }}
            </div>
          </div>

          <van-empty v-if="!classifyData?.incomeExpenseSum || Object.keys(classifyData.incomeExpenseSum).length === 0" description="暂无数据" />
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<style lang="scss" scoped>
.mobile-report {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.time-selector {
  display: flex;
  background: #fff;
  padding: 10px 20px;
  gap: 10px;

  .tab {
    padding: 8px 20px;
    border-radius: 20px;
    background: #f5f5f5;
    font-size: 14px;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }
  }
}

.summary-card {
  display: flex;
  background: #fff;
  padding: 15px;
  margin-bottom: 10px;

  .summary-item {
    flex: 1;
    text-align: center;

    .label {
      font-size: 12px;
      color: #999;
      display: block;
      margin-bottom: 5px;
    }

    .value {
      font-size: 16px;
      font-weight: bold;

      &.expense {
        color: #333;
      }

      &.income {
        color: #07c160;
      }
    }
  }
}

.loading {
  display: flex;
  justify-content: center;
  padding: 40px;
}

.chart-section {
  background: #fff;
  padding: 20px;
  min-height: 200px;

  .chart-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 200px;
    color: #999;

    p {
      margin-top: 10px;
      font-size: 14px;
    }

    .hint {
      font-size: 12px;
      color: #ccc;
    }
  }
}

.classify-section {
  background: #fff;
  padding: 15px;

  .classify-item {
    margin-bottom: 15px;

    .classify-info {
      display: flex;
      justify-content: space-between;
      margin-bottom: 5px;

      .name {
        font-size: 14px;
        color: #333;
      }

      .percent {
        font-size: 12px;
        color: #999;
      }
    }

    .classify-bar {
      height: 8px;
      background: #f5f5f5;
      border-radius: 4px;
      overflow: hidden;

      .bar {
        height: 100%;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 4px;
        transition: width 0.3s;
      }
    }

    .classify-amount {
      font-size: 12px;
      color: #666;
      margin-top: 5px;
      text-align: right;
    }
  }
}
</style>