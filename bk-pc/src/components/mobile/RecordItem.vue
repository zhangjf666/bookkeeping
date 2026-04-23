<script setup lang="ts">
import { computed } from "vue";
import type { IncomeExpenseRecord } from "@/types/bill";
import { formatNumber } from "@/utils/format";

defineOptions({
  name: "RecordItem"
});

const props = defineProps<{
  record: IncomeExpenseRecord;
  showDate?: boolean;
}>();

const emit = defineEmits<{
  click: [id: number];
  delete: [id: number];
}>();

// 分类图标
const classifyIcon = computed(() => {
  return props.record.mainClassifyImage || "📝";
});

// 分类名称
const classifyName = computed(() => {
  return props.record.mainClassifyName || "未分类";
});

// 金额显示
const amountDisplay = computed(() => {
  const prefix = props.record.type === "INCOME" ? "+" : "-";
  return `${prefix}¥${formatNumber(props.record.amount)}`;
});

// 金额样式
const amountClass = computed(() => {
  return props.record.type === "INCOME" ? "income" : "expense";
});

// 点击事件
const handleClick = () => {
  emit("click", props.record.id);
};

// 删除事件
const handleDelete = () => {
  emit("delete", props.record.id);
};
</script>

<template>
  <van-swipe-cell>
    <div class="record-item" @click="handleClick">
      <div class="record-left">
        <span class="classify-icon">{{ classifyIcon }}</span>
        <div class="record-info">
          <span class="classify-name">{{ classifyName }}</span>
          <span class="remark" v-if="record.remark">{{ record.remark }}</span>
          <span class="date" v-if="showDate">{{ record.date }}</span>
        </div>
      </div>
      <div class="record-right">
        <span class="amount" :class="amountClass">{{ amountDisplay }}</span>
      </div>
    </div>

    <template #right>
      <van-button square type="danger" text="删除" @click="handleDelete" />
    </template>
  </van-swipe-cell>
</template>

<style lang="scss" scoped>
.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  background: #fff;

  .record-left {
    display: flex;
    align-items: center;
    gap: 10px;

    .classify-icon {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f5f5;
      border-radius: 8px;
      font-size: 18px;
    }

    .record-info {
      display: flex;
      flex-direction: column;
      gap: 2px;

      .classify-name {
        font-size: 14px;
        color: #333;
      }

      .remark {
        font-size: 12px;
        color: #666;
      }

      .date {
        font-size: 12px;
        color: #999;
      }
    }
  }

  .record-right {
    .amount {
      font-size: 16px;
      font-weight: 500;

      &.expense {
        color: #333;
      }

      &.income {
        color: #07c160;
      }
    }
  }
}
</style>