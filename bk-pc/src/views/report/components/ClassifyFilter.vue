<script setup lang="ts">
import { computed, ref } from "vue";
import { useBillStoreHook } from "@/store/modules/bill";

interface Props {
  modelValue: (number | string)[];
}

const props = defineProps<Props>();
const emit = defineEmits<{
  "update:modelValue": [value: (number | string)[]];
}>();

const billStore = useBillStoreHook();

const prevHasAllExpense = ref(false);
const prevHasAllIncome = ref(false);

const expenseClassifyList = computed(() => {
  return billStore.classifyList.filter(
    (c: any) => c.pid === -1 && c.type === "EXPENSE"
  );
});

const incomeClassifyList = computed(() => {
  return billStore.classifyList.filter(
    (c: any) => c.pid === -1 && c.type === "INCOME"
  );
});

const handleClassifyChange = (value: (string | number)[]) => {
  const numValue = value.map(v => Number(v));
  const expenseIds = expenseClassifyList.value.map((c: any) => c.id);
  const incomeIds = incomeClassifyList.value.map((c: any) => c.id);

  const hasAllExpense = numValue.includes(-1);
  const hasAllIncome = numValue.includes(-2);

  let result: (number | string)[] = [];

  if (hasAllExpense && !prevHasAllExpense.value) {
    result = [...expenseIds, "-1"];
  } else if (!hasAllExpense && prevHasAllExpense.value) {
    result = numValue.filter(v => !expenseIds.includes(v));
  } else {
    result = [...value];
  }

  if (hasAllIncome && !prevHasAllIncome.value) {
    result = [...result.filter(v => !incomeIds.includes(Number(v))), ...incomeIds, "-2"];
  } else if (!hasAllIncome && prevHasAllIncome.value) {
    result = result.filter(v => !incomeIds.includes(Number(v)));
  }

  prevHasAllExpense.value = hasAllExpense;
  prevHasAllIncome.value = hasAllIncome;

  emit("update:modelValue", [...new Set(result)]);
};
</script>

<template>
  <el-select
    :model-value="modelValue"
    multiple
    collapse-tags
    collapse-tags-tooltip
    placeholder="请选择"
    style="width: 200px"
    @update:model-value="handleClassifyChange"
  >
    <el-option
      label="全部支出"
      value="-1"
      class="classify-all-option expense"
    />
    <el-option
      v-for="item in expenseClassifyList"
      :key="item.id"
      :label="item.name"
      :value="item.id"
    />
    <el-option
      label="全部收入"
      value="-2"
      class="classify-all-option income"
    />
    <el-option
      v-for="item in incomeClassifyList"
      :key="item.id"
      :label="item.name"
      :value="item.id"
    />
  </el-select>
</template>

<style lang="scss">
.classify-all-option {
  &.expense {
    color: #f56c6c !important;
    font-weight: 600;
  }
  &.income {
    color: #67c23a !important;
    font-weight: 600;
  }
}
</style>
