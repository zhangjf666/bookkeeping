<script setup lang="ts">
import { ref, watch, computed } from "vue";
import { useI18n } from "vue-i18n";

defineOptions({
  name: "AmountKeyboardInput"
});

const { t } = useI18n();

const props = defineProps<{
  modelValue: number | null;
  placeholder?: string;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: number | null];
}>();

// 数字键盘显示
const showKeyboard = ref(false);

// 键盘绑定值
const keyboardValue = ref("");

// 当前是否正在输入
const isEditing = ref(false);

// 显示文本
const displayText = computed(() => {
  if (isEditing.value) {
    return keyboardValue.value || "";
  }
  if (props.modelValue === null || props.modelValue === undefined) {
    return "";
  }
  return props.modelValue.toFixed(2).replace(/\.?0+$/, "");
});

// 初始化键盘值
watch(
  () => props.modelValue,
  val => {
    if (!isEditing.value) {
      keyboardValue.value =
        val === null || val === undefined || val === 0
          ? ""
          : val.toFixed(2).replace(/\.?0+$/, "");
    }
  },
  { immediate: true }
);

// 点击输入框
const handleFocus = () => {
  isEditing.value = true;
  keyboardValue.value =
    props.modelValue === null || props.modelValue === undefined || props.modelValue === 0
      ? ""
      : props.modelValue.toFixed(2).replace(/\.?0+$/, "");
  showKeyboard.value = true;
};

// 监听键盘输入变化
watch(keyboardValue, val => {
  if (!isEditing.value) return;

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
});

// 确认金额
const handleConfirm = () => {
  isEditing.value = false;
  showKeyboard.value = false;

  if (!keyboardValue.value || keyboardValue.value === "0") {
    emit("update:modelValue", null);
    return;
  }

  let value = keyboardValue.value;

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
};

// 清空输入
const handleClear = () => {
  keyboardValue.value = "";
  emit("update:modelValue", null);
};
</script>

<template>
  <div class="amount-keyboard-input">
    <div class="input-wrapper" @click="handleFocus">
      <span v-if="displayText" class="input-value">{{ displayText }}</span>
      <span v-else class="input-placeholder">{{ placeholder }}</span>
      <van-icon v-if="displayText" name="clear" class="clear-icon" @click.stop="handleClear" />
    </div>

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

.amount-keyboard-input {
  width: 100%;
}

.input-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  cursor: pointer;
  background-color: $color-background;
  border-radius: 8px;

  .input-value {
    flex: 1;
    font-size: 14px;
    color: $color-text-primary;
  }

  .input-placeholder {
    flex: 1;
    font-size: 14px;
    color: $color-text-placeholder;
  }

  .clear-icon {
    font-size: 16px;
    color: $color-text-placeholder;
  }
}
</style>
