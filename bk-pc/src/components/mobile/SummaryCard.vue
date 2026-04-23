<script setup lang="ts">
import { computed } from "vue";
import type { Summary } from "@/types/bill";
import { formatNumber } from "@/utils/format";

defineOptions({
  name: "SummaryCard"
});

const props = defineProps<{
  data: Summary | null;
  showExpenseLimit?: "1" | "2" | "3";
  loading?: boolean;
}>();

const emit = defineEmits<{
  editLimit: [];
}>();

// 是否显示限额
const showLimit = computed(() => {
  return props.showExpenseLimit && props.showExpenseLimit !== "1";
});

// 限额类型文字
const limitTypeText = computed(() => {
  if (props.showExpenseLimit === "2") return "本月限额";
  if (props.showExpenseLimit === "3") return "本年限额";
  return "";
});

// 剩余限额样式
const surplusClass = computed(() => {
  if (!props.data) return "";
  const surplus = props.data.expenseSurplus;
  if (surplus < 0) return "danger";
  if (surplus < props.data.expenseLimit * 0.1) return "warning";
  return "success";
});
</script>

<template>
  <div class="summary-card" v-loading="loading">
    <!-- 顶部信息 -->
    <div class="summary-header">
      <span class="month-label">本月</span>
    </div>

    <!-- 支出金额 -->
    <div class="expense-section">
      <span class="label">支出 (元)</span>
      <div class="amount">
        <span class="currency">¥</span>
        <span class="value">{{ formatNumber(data?.expenseAmount || 0) }}</span>
      </div>
    </div>

    <!-- 收入金额 -->
    <div class="income-section">
      <span class="label">收入</span>
      <span class="amount">¥{{ formatNumber(data?.incomeAmount || 0) }}</span>
    </div>

    <!-- 支出限额 -->
    <div v-if="showLimit && data" class="limit-section">
      <span class="limit-label">{{ limitTypeText }}</span>
      <span class="limit-value">¥{{ formatNumber(data.expenseLimit) }}</span>
      <span class="limit-surplus" :class="surplusClass" @click="emit('editLimit')">
        剩余 ¥{{ formatNumber(data.expenseSurplus) }}
        <van-icon name="edit" />
      </span>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.summary-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  color: #fff;

  .summary-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .month-label {
      font-size: 16px;
      font-weight: 500;
    }
  }

  .expense-section {
    margin-bottom: 15px;

    .label {
      font-size: 14px;
      opacity: 0.8;
    }

    .amount {
      margin-top: 5px;

      .currency {
        font-size: 20px;
      }

      .value {
        font-size: 36px;
        font-weight: bold;
      }
    }
  }

  .income-section {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;

    .label {
      opacity: 0.8;
    }

    .amount {
      font-weight: 500;
    }
  }

  .limit-section {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-top: 15px;
    padding-top: 15px;
    border-top: 1px solid rgba(255, 255, 255, 0.2);
    font-size: 12px;

    .limit-surplus {
      margin-left: auto;
      display: flex;
      align-items: center;
      gap: 5px;
      cursor: pointer;

      &.danger {
        color: #ff6b6b;
      }

      &.warning {
        color: #ffd93d;
      }

      &.success {
        color: #6bcb77;
      }
    }
  }
}
</style>