<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import dayjs from "dayjs";
import "dayjs/locale/zh-cn";
import type { IncomeExpenseRecord } from "@/types/bill";
import { formatNumber } from "@/utils/format";
import { getClassifyIcon } from "@/utils/classifyIcons";

defineOptions({
  name: "RecordItem"
});

const props = defineProps<{
  record: IncomeExpenseRecord;
  showDate?: boolean;
}>();

const emit = defineEmits<{
  click: [record: IncomeExpenseRecord];
  delete: [id: number];
}>();

const { locale } = useI18n();

// 中文星期映射
const weekDayMap: Record<string, string> = {
  Sun: "周日",
  Mon: "周一",
  Tue: "周二",
  Wed: "周三",
  Thu: "周四",
  Fri: "周五",
  Sat: "周六"
};

// 格式化日期
const formattedDate = computed(() => {
  const date = dayjs(props.record.date);
  const weekEn = date.format("ddd");
  // 根据语言环境显示周几
  const week = locale.value === "en" ? weekEn : weekDayMap[weekEn] || weekEn;
  return {
    day: date.format("MM-DD"),
    week
  };
});

// 分类图标 - 优先显示子分类图标，没有子分类则显示顶级分类图标
const classifyIcon = computed(() => {
  // 如果有子分类图标，优先使用子分类图标
  if (props.record.subClassifyImage) {
    return getClassifyIcon(props.record.subClassifyImage);
  }
  // 否则使用主分类图标
  return getClassifyIcon(props.record.mainClassifyImage);
});

// 分类名称（父分类-子分类）
const classifyName = computed(() => {
  const mainName = props.record.mainClassifyName || "未分类";
  const subName = props.record.subClassifyName;
  return subName ? `${mainName}-${subName}` : mainName;
});

// 金额显示（不带正负号，通过颜色区分）
const amountDisplay = computed(() => {
  return `¥ ${formatNumber(props.record.amount)}`;
});

// 金额颜色
const amountColor = computed(() => {
  return props.record.type === "INCOME" ? "#00a151" : "#d83d34";
});

// 点击事件
const handleClick = () => {
  emit("click", props.record);
};

// 删除事件
const handleDelete = () => {
  emit("delete", props.record.id);
};
</script>

<template>
  <van-swipe-cell>
    <div class="record-item" @click="handleClick">
      <!-- 日期 -->
      <div v-if="showDate" class="date-section">
        <div class="day">{{ formattedDate.day }}</div>
        <div class="week">{{ formattedDate.week }}</div>
      </div>

      <!-- 分类图标 -->
      <div class="icon-section">
        <span class="classify-icon">{{ classifyIcon }}</span>
      </div>

      <!-- 分类名称和备注 -->
      <div class="info-section">
        <div class="classify-name">{{ classifyName }}</div>
        <div v-if="record.remark" class="remark">{{ record.remark }}</div>
      </div>

      <!-- 金额 -->
      <div class="amount-section" :style="{ color: amountColor }">
        {{ amountDisplay }}
      </div>
    </div>

    <template #right>
      <van-button
        square
        type="danger"
        text="删除"
        class="delete-btn"
        @click="handleDelete"
      />
    </template>
  </van-swipe-cell>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.record-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background-color: $color-card;
  border-bottom: 1px solid $color-border;

  &:active {
    background-color: #f5f5f5;
  }
}

.date-section {
  width: 48px;
  text-align: center;

  .day {
    font-size: 14px;
    font-weight: 500;
    color: $color-text-primary;
  }

  .week {
    font-size: 12px;
    color: $color-text-secondary;
  }
}

.icon-section {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  margin: 0 12px;
  background-color: #f5f5f5;
  border-radius: 8px;

  .classify-icon {
    font-size: 20px;
  }
}

.info-section {
  flex: 1;
  min-width: 0;

  .classify-name {
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: 14px;
    color: $color-text-primary;
    white-space: nowrap;
  }

  .remark {
    margin-top: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: 12px;
    color: $color-text-secondary;
    white-space: nowrap;
  }
}

.amount-section {
  font-size: 14px;
  font-weight: 500;
}
</style>

<style lang="scss">
/* 全局样式，修复左滑删除按钮高度问题 */
.van-swipe-cell__right {
  .van-button {
    height: 100% !important;
    margin: 0;
  }
}
</style>
