<script setup lang="ts">
import { ref, computed, watch } from "vue";
import dayjs from "dayjs";

defineOptions({
  name: "DatePicker"
});

const props = defineProps<{
  modelValue: string;
  label?: string;
  placeholder?: string;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: string];
  change: [value: string];
}>();

const show = ref(false);

// 当前选择的日期（年月日字符串数组）
const selectedDate = ref<string[]>([
  String(new Date().getFullYear()),
  String(new Date().getMonth() + 1),
  String(new Date().getDate())
]);

// 监听 props 变化更新选中日期
watch(
  () => props.modelValue,
  val => {
    if (val) {
      const date = new Date(val);
      selectedDate.value = [
        String(date.getFullYear()),
        String(date.getMonth() + 1),
        String(date.getDate())
      ];
    }
  },
  { immediate: true }
);

// 显示值
const displayValue = computed(() => {
  if (!props.modelValue) return "";
  return dayjs(props.modelValue).format("YYYY-MM-DD");
});

// 是否今天
const isToday = computed(() => {
  return props.modelValue === dayjs().format("YYYY-MM-DD");
});

// 是否昨天
const isYesterday = computed(() => {
  return props.modelValue === dayjs().subtract(1, "day").format("YYYY-MM-DD");
});

// 日期标签
const dateLabel = computed(() => {
  if (isToday.value) return "今天";
  if (isYesterday.value) return "昨天";
  return "";
});

// 打开选择器
const openPicker = () => {
  show.value = true;
};

// 确认选择
const onConfirm = ({ selectedValues }: { selectedValues: number[] }) => {
  const date = `${selectedValues[0]}-${String(selectedValues[1]).padStart(2, "0")}-${String(selectedValues[2]).padStart(2, "0")}`;
  emit("update:modelValue", date);
  emit("change", date);
  show.value = false;
};

// 快捷选择
const selectToday = () => {
  const date = dayjs().format("YYYY-MM-DD");
  emit("update:modelValue", date);
  emit("change", date);
};

const selectYesterday = () => {
  const date = dayjs().subtract(1, "day").format("YYYY-MM-DD");
  emit("update:modelValue", date);
  emit("change", date);
};
</script>

<template>
  <div class="date-picker">
    <van-cell :title="label || '日期'" is-link @click="openPicker">
      <template #value>
        <span class="date-value">
          <span v-if="dateLabel" class="date-label">{{ dateLabel }}</span>
          {{ displayValue || placeholder || "请选择日期" }}
        </span>
      </template>
    </van-cell>

    <!-- 快捷选择 -->
    <div class="quick-select">
      <van-button
        size="small"
        :type="isToday ? 'primary' : 'default'"
        @click="selectToday"
      >
        今天
      </van-button>
      <van-button
        size="small"
        :type="isYesterday ? 'primary' : 'default'"
        @click="selectYesterday"
      >
        昨天
      </van-button>
    </div>

    <!-- 日期选择弹窗 -->
    <van-popup v-model:show="show" position="bottom" round>
      <van-date-picker
        v-model="selectedDate"
        title="选择日期"
        @confirm="onConfirm"
        @cancel="show = false"
      />
    </van-popup>
  </div>
</template>

<style lang="scss" scoped>
.date-picker {
  .date-value {
    display: flex;
    gap: 5px;
    align-items: center;

    .date-label {
      padding: 2px 6px;
      font-size: 12px;
      color: #666;
      background: #f5f5f5;
      border-radius: 4px;
    }
  }

  .quick-select {
    display: flex;
    gap: 10px;
    padding: 0 15px 15px;

    .van-button {
      flex: 1;
    }
  }
}
</style>
