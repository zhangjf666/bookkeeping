<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useI18n } from "vue-i18n";

defineOptions({
  name: "AmountInput"
});

const { t } = useI18n();

const props = defineProps<{
  modelValue: number;
  type?: "EXPENSE" | "INCOME";
}>();

const emit = defineEmits<{
  "update:modelValue": [value: number];
}>();

// 数字键盘显示
const showKeyboard = ref(false);

// 输入值（字符串形式）
const inputValue = ref("0");

// 键盘绑定值
const keyboardValue = ref("");

// 初始化输入值
watch(
  () => props.modelValue,
  val => {
    if (val === 0) {
      inputValue.value = "0";
      keyboardValue.value = "";
    } else {
      inputValue.value = val.toFixed(2).replace(/\.?0+$/, "");
      keyboardValue.value = inputValue.value;
    }
  },
  { immediate: true }
);

// 金额样式
const amountClass = computed(() => {
  return props.type === "INCOME" ? "income" : "expense";
});

// 格式化显示金额（实时显示输入值）
const displayAmount = computed(() => {
  const value = inputValue.value;
  if (!value || value === "0") return "0.00";

  // 如果以小数点结尾，显示格式化
  if (value.endsWith(".")) {
    return value + "00";
  }

  // 如果有小数点，补齐小数位
  if (value.includes(".")) {
    const parts = value.split(".");
    const decimal = (parts[1] || "").padEnd(2, "0").substring(0, 2);
    return parts[0] + "." + decimal;
  }

  // 整数，补 .00
  return value + ".00";
});

// 点击金额区域
const handleAmountClick = () => {
  // 如果当前值是 0，清空键盘值，让用户重新输入
  keyboardValue.value = inputValue.value === "0" ? "" : inputValue.value;
  showKeyboard.value = true;
};

// 监听键盘输入变化
watch(keyboardValue, val => {
  if (!val) {
    inputValue.value = "0";
    return;
  }

  // 限制小数位数最多2位
  if (val.includes(".")) {
    const parts = val.split(".");
    if (parts[1] && parts[1].length > 2) {
      keyboardValue.value = parts[0] + "." + parts[1].substring(0, 2);
      return;
    }
  }

  // 限制整数位数最多8位
  const intPart = val.split(".")[0];
  if (intPart.length > 8) {
    keyboardValue.value =
      intPart.substring(0, 8) +
      (val.includes(".") ? "." + val.split(".")[1] : "");
    return;
  }

  inputValue.value = val || "0";
});

// 确认金额
const handleConfirm = () => {
  let value = inputValue.value;

  // 格式化为2位小数
  if (value.includes(".")) {
    const parts = value.split(".");
    const decimal = (parts[1] || "").padEnd(2, "0").substring(0, 2);
    value = parts[0] + "." + decimal;
  } else {
    value = value + ".00";
  }

  const numValue = parseFloat(value) || 0;
  emit("update:modelValue", numValue);
  inputValue.value = numValue.toFixed(2).replace(/\.?0+$/, "");
  showKeyboard.value = false;
};
</script>

<template>
  <div class="amount-input">
    <!-- 金额显示区域 -->
    <div class="amount-display" :class="amountClass" @click="handleAmountClick">
      <span class="currency">¥</span>
      <span class="value">{{ displayAmount }}</span>
    </div>

    <!-- 数字键盘 -->
    <van-number-keyboard
      v-model:show="showKeyboard"
      v-model="keyboardValue"
      theme="custom"
      extra-key="."
      :close-button-text="t('mobile.common.confirm')"
      maxlength="11"
      @close="handleConfirm"
      @blur="handleConfirm"
    />
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.amount-input {
  padding: 16px;
  background-color: $color-card;
}

.amount-display {
  display: flex;
  align-items: baseline;
  padding: 12px 0;
  cursor: pointer;

  .currency {
    margin-right: 4px;
    font-size: 20px;
    font-weight: 500;
  }

  .value {
    font-size: 32px;
    font-weight: 600;
  }

  &.expense {
    color: $color-primary;
  }

  &.income {
    color: $color-secondary;
  }

  &:active {
    opacity: 0.8;
  }
}
</style>
