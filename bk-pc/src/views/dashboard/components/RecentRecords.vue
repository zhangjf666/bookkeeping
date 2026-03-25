<script setup lang="ts">
import { useI18n } from "vue-i18n";
import type { IncomeExpenseRecord } from "@/types/dashboard";
import dayjs from "dayjs";

defineOptions({
  name: "RecentRecords"
});

interface Props {
  records: IncomeExpenseRecord[];
  loading: boolean;
}

const props = defineProps<Props>();
const { t } = useI18n();

const formatAmount = (amount: number, type: string) => {
  const prefix = type === "0" ? "-" : "+";
  return `${prefix}¥${amount.toFixed(2)}`;
};

const formatDate = (date: string) => {
  return dayjs(date).format("MM-DD");
};

const getTypeLabel = (type: string) => {
  return type === "0" ? t("dashboard.pureExpense") : t("dashboard.pureIncome");
};
</script>

<template>
  <el-card class="recent-records" shadow="never">
    <template #header>
      <div class="card-header">
        <span>{{ t("dashboard.pureRecentRecords") }}</span>
      </div>
    </template>
    <el-table v-loading="loading" :data="records" style="width: 100%">
      <el-table-column prop="date" :label="t('dashboard.pureDate')" width="80">
        <template #default="{ row }">
          {{ formatDate(row.date) }}
        </template>
      </el-table-column>
      <el-table-column
        prop="mainClassifyName"
        :label="t('dashboard.pureClassify')"
        width="100"
      />
      <el-table-column
        prop="remark"
        :label="t('dashboard.pureRemark')"
        min-width="120"
      />
      <el-table-column
        :label="t('dashboard.pureAmount')"
        width="120"
        align="right"
      >
        <template #default="{ row }">
          <span :style="{ color: row.type === '0' ? '#f56c6c' : '#67c23a' }">
            {{ formatAmount(row.amount, row.type) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column
        :label="t('dashboard.pureType')"
        width="80"
        align="center"
      >
        <template #default="{ row }">
          <el-tag :type="row.type === '0' ? 'danger' : 'success'" size="small">
            {{ getTypeLabel(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-empty
      v-if="!loading && records.length === 0"
      :description="t('dashboard.pureNoRecords')"
    />
  </el-card>
</template>

<style lang="scss" scoped>
.recent-records {
  .card-header {
    font-weight: 600;
  }
}
</style>
