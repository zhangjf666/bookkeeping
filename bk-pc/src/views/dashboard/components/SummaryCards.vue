<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { Summary } from "@/types/dashboard";

defineOptions({
  name: "SummaryCards"
});

interface Props {
  data: Summary | null;
  loading: boolean;
}

const props = defineProps<Props>();
const { t } = useI18n();

const formatAmount = (amount: number | undefined) => {
  if (amount === undefined || amount === null) return "0.00";
  return amount.toFixed(2);
};

const cards = computed(() => [
  {
    title: t("dashboard.pureExpense"),
    value: formatAmount(props.data?.expenseAmount),
    color: "#f56c6c",
    icon: "ep:trend-charts"
  },
  {
    title: t("dashboard.pureIncome"),
    value: formatAmount(props.data?.incomeAmount),
    color: "#67c23a",
    icon: "ep:wallet"
  },
  {
    title: t("dashboard.pureExpenseLimit"),
    value: formatAmount(props.data?.expenseLimit),
    color: "#e6a23c",
    icon: "ep:data-line"
  },
  {
    title: t("dashboard.pureExpenseSurplus"),
    value: formatAmount(props.data?.expenseSurplus),
    color: "#409eff",
    icon: "ep:coin"
  }
]);
</script>

<template>
  <div class="summary-cards">
    <el-row :gutter="16">
      <el-col
        v-for="(card, index) in cards"
        :key="index"
        :xs="24"
        :sm="12"
        :md="6"
      >
        <el-card class="summary-card" shadow="hover">
          <div class="card-content">
            <div class="card-icon" :style="{ backgroundColor: card.color }">
              <IconifyIconOffline :icon="card.icon" />
            </div>
            <div class="card-info">
              <div class="card-title">{{ card.title }}</div>
              <div class="card-value" :style="{ color: card.color }">
                ¥{{ card.value }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style lang="scss" scoped>
.summary-cards {
  margin-bottom: 20px;
}

.summary-card {
  margin-bottom: 16px;

  .card-content {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .card-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: #fff;
  }

  .card-info {
    flex: 1;
  }

  .card-title {
    font-size: 14px;
    color: #909399;
    margin-bottom: 8px;
  }

  .card-value {
    font-size: 24px;
    font-weight: 600;
  }
}
</style>
