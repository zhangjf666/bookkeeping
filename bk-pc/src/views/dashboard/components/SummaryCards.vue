<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { Summary } from "@/types/dashboard";
import Edit from "~icons/ep/edit";
import ShoppingCart from "~icons/ep/shopping-cart";
import Money from "~icons/ep/money";
import Odometer from "~icons/ep/odometer";
import Coin from "~icons/ep/coin";

defineOptions({
  name: "SummaryCards"
});

interface Props {
  data: Summary | null;
  loading: boolean;
  showExpenseLimit?: "1" | "2" | "3";
}

const props = withDefaults(defineProps<Props>(), {
  showExpenseLimit: "1"
});

const emit = defineEmits<{
  editLimit: [];
}>();

const { t } = useI18n();

const formatAmount = (amount: number | undefined) => {
  if (amount === undefined || amount === null) return "0.00";
  return amount.toFixed(2);
};

const shouldShowLimit = computed(() => props.showExpenseLimit !== "1");

const limitTitle = computed(() => {
  if (props.showExpenseLimit === "2") {
    return t("dashboard.pureMonthlyExpenseLimit");
  }
  return t("dashboard.pureYearlyExpenseLimit");
});

const surplusTitle = computed(() => {
  if (props.showExpenseLimit === "2") {
    return t("dashboard.pureMonthlyExpenseSurplus");
  }
  return t("dashboard.pureYearlyExpenseSurplus");
});

const cards = computed(() => {
  const cardList = [
    {
      title: t("dashboard.pureExpense"),
      value: formatAmount(props.data?.expenseAmount),
      color: "#f56c6c",
      icon: ShoppingCart,
      showEdit: shouldShowLimit.value
    },
    {
      title: t("dashboard.pureIncome"),
      value: formatAmount(props.data?.incomeAmount),
      color: "#67c23a",
      icon: Money,
      showEdit: false
    }
  ];

  if (shouldShowLimit.value) {
    cardList.push({
      title: limitTitle.value,
      value: formatAmount(props.data?.expenseLimit),
      color: "#e6a23c",
      icon: Odometer,
      showEdit: true
    });
    cardList.push({
      title: surplusTitle.value,
      value: formatAmount(props.data?.expenseSurplus),
      color: "#409eff",
      icon: Coin,
      showEdit: false
    });
  }

  return cardList;
});
</script>

<template>
  <div class="summary-cards">
    <el-row :gutter="16">
      <el-col
        v-for="(card, index) in cards"
        :key="index"
        :xs="24"
        :sm="shouldShowLimit ? 12 : 12"
        :md="shouldShowLimit ? 6 : 12"
      >
        <el-card class="summary-card" shadow="hover">
          <div class="card-content">
            <div class="card-icon" :style="{ backgroundColor: card.color }">
              <IconifyIconOffline :icon="card.icon" />
            </div>
            <div class="card-info">
              <div class="card-title">{{ card.title }}</div>
              <div class="card-value-row">
                <div class="card-value" :style="{ color: card.color }">
                  ¥{{ card.value }}
                </div>
                <el-button
                  v-if="card.showEdit"
                  type="primary"
                  link
                  :icon="Edit"
                  @click="emit('editLimit')"
                />
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
    gap: 16px;
    align-items: center;
  }

  .card-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 56px;
    height: 56px;
    font-size: 28px;
    color: #fff;
    border-radius: 12px;
  }

  .card-info {
    display: flex;
    flex: 1;
    flex-direction: column;
    justify-content: center;
  }

  .card-title {
    font-size: 14px;
    color: #909399;
  }

  .card-value-row {
    display: flex;
    gap: 4px;
    align-items: center;
  }

  .card-value {
    font-size: 24px;
    font-weight: 600;
  }
}
</style>
