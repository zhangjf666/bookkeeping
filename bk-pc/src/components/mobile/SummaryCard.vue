<script setup lang="ts">
import { ref, computed } from "vue";
import { useI18n } from "vue-i18n";
import type { Summary, IncomeExpenseRecord } from "@/types/bill";
import type { AccountBook } from "@/types/accountBook";
import { formatNumber } from "@/utils/format";

defineOptions({
  name: "SummaryCard"
});

const props = defineProps<{
  data: Summary | null;
  accountBookList: AccountBook[];
  currentAccountBookId: number | null;
  showExpenseLimit?: boolean;
  limitType?: "MONTHLY" | "YEARLY";
  monthExpenseLimit?: number;
  yearExpenseLimit?: number;
  loading?: boolean;
}>();

const emit = defineEmits<{
  "update:currentAccountBookId": [id: number];
  refresh: [];
}>();

const { t } = useI18n();

// 账本选择弹窗
const showAccountBookPicker = ref(false);

// 当前账本名称
const currentAccountBookName = computed(() => {
  const book = props.accountBookList.find(
    item => item.id === props.currentAccountBookId
  );
  return book?.name || "";
});

// 限额类型文字
const limitTypeText = computed(() => {
  return props.limitType === "YEARLY"
    ? t("mobile.home.yearLimit")
    : t("mobile.home.monthLimit");
});

// 当前限额值（根据限额类型）
const currentLimit = computed(() => {
  if (props.limitType === "YEARLY") {
    return props.yearExpenseLimit || props.data?.expenseLimit || 0;
  }
  return props.monthExpenseLimit || props.data?.expenseLimit || 0;
});

// 剩余限额样式
const surplusClass = computed(() => {
  if (!props.data) return "";
  const surplus = props.data.expenseSurplus;
  if (surplus < 0) return "danger";
  if (surplus < props.data.expenseLimit * 0.1) return "warning";
  return "";
});

// 选择账本
const handleSelectAccountBook = (book: AccountBook) => {
  emit("update:currentAccountBookId", book.id);
  showAccountBookPicker.value = false;
};
</script>

<template>
  <div v-loading="loading" class="summary-card">
    <!-- 账本选择 -->
    <div class="account-book-selector" @click="showAccountBookPicker = true">
      <span class="book-name">{{ currentAccountBookName }}</span>
      <van-icon name="arrow-down" size="12" />
    </div>

    <!-- 本月支出 -->
    <div class="expense-section">
      <span class="label">{{ t("mobile.home.monthExpense") }}</span>
      <div class="amount expense">
        <span class="currency">¥</span>
        <span class="value">{{ formatNumber(data?.expenseAmount || 0) }}</span>
      </div>
    </div>

    <!-- 本月收入 -->
    <div class="income-section">
      <span class="label">{{ t("mobile.home.monthIncome") }}</span>
      <span class="amount income"
        >¥{{ formatNumber(data?.incomeAmount || 0) }}</span
      >
    </div>

    <!-- 支出限额 -->
    <template v-if="showExpenseLimit && data">
      <van-divider />
      <div class="limit-section">
        <div class="limit-item">
          <span class="label">{{ limitTypeText }}</span>
          <span class="value">¥{{ formatNumber(currentLimit) }}</span>
        </div>
        <div class="limit-item">
          <span class="label">{{ t("mobile.home.remainingLimit") }}</span>
          <span class="value" :class="surplusClass">
            ¥{{ formatNumber(data.expenseSurplus) }}
          </span>
        </div>
      </div>
    </template>

    <!-- 账本选择弹窗 -->
    <van-action-sheet
      v-model:show="showAccountBookPicker"
      :title="t('mobile.home.selectAccountBook')"
    >
      <div class="account-book-list">
        <van-cell
          v-for="book in accountBookList"
          :key="book.id"
          clickable
          @click="handleSelectAccountBook(book)"
        >
          <template #title>
            <div class="book-item">
              <span class="book-name">{{ book.name }}</span>
              <span v-if="book.isDefault === 'YES'" class="default-tag"
                >默认</span
              >
            </div>
          </template>
        </van-cell>
        <van-empty
          v-if="accountBookList.length === 0"
          :description="t('mobile.common.noData')"
        />
      </div>
    </van-action-sheet>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.summary-card {
  position: relative;
  padding: 16px;
  margin: 16px;
  background-color: $color-card;
  border-radius: 12px;
}

.account-book-selector {
  position: absolute;
  top: 12px;
  right: 16px;
  display: flex;
  gap: 4px;
  align-items: center;
  padding: 6px 12px;
  cursor: pointer;
  border: 1px solid $color-border;
  border-radius: 16px;

  .book-name {
    font-size: 13px;
    color: $color-text-secondary;
  }

  &:active {
    opacity: 0.7;
  }
}

.expense-section {
  margin-bottom: 8px;

  .label {
    display: block;
    margin-bottom: 4px;
    font-size: 12px;
    color: $color-text-secondary;
  }

  .amount {
    display: flex;
    align-items: baseline;

    &.expense {
      color: $color-primary;

      .currency {
        margin-right: 2px;
        font-size: 18px;
      }

      .value {
        font-size: 28px;
        font-weight: 600;
      }
    }
  }
}

.income-section {
  .label {
    margin-right: 8px;
    font-size: 12px;
    color: $color-text-secondary;
  }

  .amount {
    &.income {
      font-size: 20px;
      font-weight: 500;
      color: $color-secondary;
    }
  }
}

.limit-section {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
}

.limit-item {
  .label {
    display: block;
    margin-bottom: 4px;
    font-size: 12px;
    color: $color-text-secondary;
  }

  .value {
    font-size: 16px;
    font-weight: 500;
    color: $color-text-primary;

    &.danger {
      color: $color-danger;
    }

    &.warning {
      color: $color-warning;
    }
  }
}

.account-book-list {
  max-height: 300px;
  padding-bottom: env(safe-area-inset-bottom);
  overflow-y: auto;
}

.book-item {
  display: flex;
  gap: 8px;
  align-items: center;
}

.default-tag {
  font-size: 12px;
  color: #00a151;
}
</style>
