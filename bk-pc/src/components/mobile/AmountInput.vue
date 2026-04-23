<script setup lang="ts">
import { ref, computed, watch } from "vue";

defineOptions({
  name: "AmountInput"
});

const props = defineProps<{
  modelValue: number;
  type?: "EXPENSE" | "INCOME";
  placeholder?: string;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: number];
}>();

// 显示值
const displayValue = ref("");

// 初始化显示值
watch(
  () => props.modelValue,
  (val) => {
    if (val === 0) {
      displayValue.value = "";
    } else {
      displayValue.value = val.toString();
    }
  },
  { immediate: true }
);

// 金额样式
const amountClass = computed(() => {
  return props.type === "INCOME" ? "income" : "expense";
});

// 标签文字
const labelText = computed(() => {
  return props.type === "INCOME" ? "收入金额" : "支出金额";
});

// 输入处理
const handleInput = (value: string) => {
  // 只允许数字和小数点
  let formatted = value.replace(/[^\d.]/g, "");

  // 只允许一个小数点
  const parts = formatted.split(".");
  if (parts.length > 2) {
    formatted = parts[0] + "." + parts.slice(1).join("");
  }

  // 限制小数位数为2位
  if (parts.length === 2 && parts[1].length > 2) {
    formatted = parts[0] + "." + parts[1].slice(0, 2);
  }

  displayValue.value = formatted;

  // 更新值
  const numValue = formatted ? parseFloat(formatted) : 0;
  emit("update:modelValue", numValue);
};

// 快捷金额按钮
const quickAmounts = [100, 500, 1000, 5000];

const setQuickAmount = (amount: number) => {
  displayValue.value = amount.toString();
  emit("update:modelValue", amount);
};

// 清空
const clearAmount = () => {
  displayValue.value = "";
  emit("update:modelValue", 0);
};
</script>

<template>
  <div class="amount-input">
    <div class="amount-label">{{ labelText }}</div>

    <div class="amount-field" :class="amountClass">
      <span class="currency">¥</span>
      <input
        :value="displayValue"
        type="text"
        inputmode="decimal"
        :placeholder="placeholder || '0.00'"
        class="input"
        @input="handleInput(($event.target as HTMLInputElement).value)"
      />
      <van-icon
        v-if="displayValue"
        name="clear"
        class="clear-icon"
        @click="clearAmount"
      />
    </div>

    <!-- 快捷金额 -->
    <div class="quick-amounts">
      <van-button
        v-for="amount in quickAmounts"
        :key="amount"
        size="small"
        type="default"
        @click="setQuickAmount(amount)"
      >
        {{ amount }}
      </van-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.amount-input {
  background: #fff;
  padding: 20px;

  .amount-label {
    font-size: 14px;
    color: #666;
    margin-bottom: 10px;
  }

  .amount-field {
    display: flex;
    align-items: center;
    padding: 10px 0;
    border-bottom: 2px solid #eee;
    transition: border-color 0.3s;

    &:focus-within {
      border-color: #667eea;
    }

    &.income:focus-within {
      border-color: #07c160;
    }

    .currency {
      font-size: 24px;
      font-weight: bold;
      color: #333;
    }

    .input {
      flex: 1;
      font-size: 36px;
      font-weight: bold;
      border: none;
      outline: none;
      background: transparent;
      padding-left: 5px;

      &::placeholder {
        color: #ccc;
        font-weight: normal;
      }
    }

    .clear-icon {
      color: #999;
      font-size: 20px;
    }
  }

  .quick-amounts {
    display: flex;
    gap: 10px;
    margin-top: 15px;

    .van-button {
      flex: 1;
    }
  }
}
</style>