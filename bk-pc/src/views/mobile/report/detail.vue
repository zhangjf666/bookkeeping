<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { showNotify } from "vant";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import { getTrendData } from "@/api/incomeExpense";
import type { TrendData, DaySum } from "@/types/bill";
import { formatNumber } from "@/utils/format";

defineOptions({
  name: "MobileReportDetail"
});

const router = useRouter();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

const loading = ref(false);
const trendData = ref<TrendData | null>(null);

// 加载数据
const loadData = async () => {
  if (!userStore.id) return;

  loading.value = true;
  try {
    const result = await getTrendData({
      userId: userStore.id,
      accountBookId: billStore.currentAccountBook?.id,
      mode: "MONTH",
      beginDate: new Date().toISOString().slice(0, 10),
      endDate: new Date().toISOString().slice(0, 10)
    });
    trendData.value = result;
  } catch (error: any) {
    showNotify({ type: "danger", message: error?.message || "加载失败" });
  } finally {
    loading.value = false;
  }
};

// 格式化日期数据
const daySumList = () => {
  if (!trendData.value?.incomeExpenseSum) return [];
  return Object.entries(trendData.value.incomeExpenseSum).map(([date, sum]) => ({
    date,
    ...sum
  }));
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="mobile-report-detail">
    <van-loading v-if="loading" class="loading" />

    <div v-else class="detail-list">
      <div class="total-section">
        <div class="total-item">
          <span class="label">总支出</span>
          <span class="value expense">¥{{ formatNumber(trendData?.expenseTotal || 0) }}</span>
        </div>
        <div class="total-item">
          <span class="label">总收入</span>
          <span class="value income">¥{{ formatNumber(trendData?.incomeTotal || 0) }}</span>
        </div>
      </div>

      <div class="day-list">
        <div v-for="item in daySumList()" :key="item.date" class="day-item">
          <div class="day-header">{{ item.date }}</div>
          <div class="day-summary">
            <span class="expense">支出: ¥{{ formatNumber(item.expense) }}</span>
            <span class="income">收入: ¥{{ formatNumber(item.income) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.mobile-report-detail {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.loading {
  display: flex;
  justify-content: center;
  padding: 40px;
}

.total-section {
  display: flex;
  background: #fff;
  padding: 20px;
  margin-bottom: 10px;

  .total-item {
    flex: 1;
    text-align: center;

    .label {
      font-size: 14px;
      color: #999;
      display: block;
      margin-bottom: 8px;
    }

    .value {
      font-size: 20px;
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

.day-list {
  .day-item {
    background: #fff;
    margin-bottom: 10px;
    padding: 15px;

    .day-header {
      font-size: 14px;
      font-weight: 500;
      margin-bottom: 10px;
    }

    .day-summary {
      display: flex;
      gap: 20px;
      font-size: 14px;

      .expense {
        color: #333;
      }

      .income {
        color: #07c160;
      }
    }
  }
}
</style>