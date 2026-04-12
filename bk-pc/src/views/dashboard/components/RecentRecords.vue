<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { IncomeExpenseRecord } from "@/types/bill";
import { useBillStoreHook } from "@/store/modules/bill";
import { formatAmount } from "@/utils/format";

defineOptions({
  name: "RecentRecords"
});

interface Props {
  records: IncomeExpenseRecord[];
}

const props = defineProps<Props>();
const { t } = useI18n();
const billStore = useBillStoreHook();

const formatAmountWithPrefix = (amount: number, type: string) => {
  return `¥${formatAmount(amount)}`;
};

const isExpense = (type: string) => {
  return type === "EXPENSE";
};

const getTypeLabel = (type: string) => {
  return type === "EXPENSE" ? t("bill.pureExpense") : t("bill.pureIncome");
};

const getAccountBookName = (accountBookId: number) => {
  const book = billStore.accountBooks.find(b => b.id === accountBookId);
  return book ? book.name : "";
};

const findClassifyName = (
  mainClassifyId: number,
  subClassifyId: number | null
): { mainName: string; subName: string } => {
  const classifyList = billStore.classifyList;

  const mainClassify = classifyList.find(
    c => c.id === mainClassifyId && c.pid === -1
  );
  if (!mainClassify) {
    return { mainName: String(mainClassifyId), subName: "" };
  }

  if (subClassifyId) {
    const subClassify = classifyList.find(
      c => c.id === subClassifyId && c.pid === mainClassifyId
    );
    if (subClassify) {
      return { mainName: mainClassify.name, subName: subClassify.name };
    }
  }

  return { mainName: mainClassify.name, subName: "" };
};

const getClassifyDisplay = (row: IncomeExpenseRecord) => {
  return findClassifyName(row.mainClassify, row.subClassify);
};

const getTagsByCodes = (tagCodes: string | null | undefined) => {
  if (!tagCodes) return [];
  const tagIdStrs = tagCodes.split(",").map(t => t.trim());
  return billStore.tagList.filter(tag =>
    tagIdStrs.includes(String((tag as any).code))
  );
};
</script>

<template>
  <el-card class="recent-records" shadow="never">
    <template #header>
      <div class="card-header">
        <span>{{ t("dashboard.pureRecentRecords") }}</span>
      </div>
    </template>
    <el-table :data="records" border stripe style="width: 100%">
      <el-table-column prop="date" :label="t('bill.pureDate')" width="100" />
      <el-table-column :label="t('bill.pureAmount')" width="130" align="right">
        <template #default="{ row }">
          <span :style="{ color: isExpense(row.type) ? '#f56c6c' : '#67c23a' }">
            {{ formatAmountWithPrefix(row.amount, row.type) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column :label="t('bill.pureType')" width="80" align="center">
        <template #default="{ row }">
          <el-tag
            :type="isExpense(row.type) ? 'danger' : 'success'"
            size="small"
          >
            {{ getTypeLabel(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('bill.pureClassify')" width="300">
        <template #default="{ row }">
          <div class="classify-cell">
            <span>{{ getClassifyDisplay(row).mainName }}</span>
            <span v-if="getClassifyDisplay(row).subName" class="sub-classify">
              / {{ getClassifyDisplay(row).subName }}
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        :label="t('bill.pureCreditCard')"
        width="100"
        align="center"
      >
        <template #default="{ row }">
          <el-tag
            :type="row.isCreditCard === 'YES' ? 'warning' : 'info'"
            size="small"
          >
            {{
              row.isCreditCard === "YES" ? t("bill.pureYes") : t("bill.pureNo")
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="remark"
        :label="t('bill.pureRemark')"
        width="300"
      />
      <el-table-column :label="t('bill.pureTag')" min-width="300">
        <template #default="{ row }">
          <el-tag
            v-for="tag in getTagsByCodes(row.tagCodes)"
            :key="tag.id"
            :color="tag.color"
            size="small"
            :style="{ color: '#fff', marginRight: '4px' }"
          >
            {{ tag.name }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-empty
      v-if="records.length === 0"
      :description="t('dashboard.pureNoRecords')"
    />
  </el-card>
</template>

<style lang="scss" scoped>
.recent-records {
  .card-header {
    font-weight: 600;
  }

  .classify-cell {
    .sub-classify {
      font-size: 12px;
      color: #909399;
    }
  }
}
</style>
