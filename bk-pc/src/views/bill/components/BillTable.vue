<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { IncomeExpense } from "@/types/bill";
import { useBillStoreHook } from "@/store/modules/bill";
import { formatAmount } from "@/utils/format";
import { getClassifyIcon } from "@/utils/classifyIcons";
import { Edit, Delete } from "@element-plus/icons-vue";

defineOptions({
  name: "BillTable"
});

interface Props {
  data: IncomeExpense[];
  loading: boolean;
}

defineProps<Props>();

const emit = defineEmits<{
  edit: [row: IncomeExpense];
  delete: [ids: number[]];
  "selection-change": [selection: IncomeExpense[]];
}>();

const { t } = useI18n();
const billStore = useBillStoreHook();

const formatAmountWithPrefix = (amount: number, type: string) => {
  return `¥${formatAmount(amount)}`;
};

const getTypeLabel = (type: string) => {
  return type === "EXPENSE" ? t("bill.pureExpense") : t("bill.pureIncome");
};

const isExpense = (type: string) => {
  return type === "EXPENSE";
};

const getAccountBookName = (accountBookId: number) => {
  const book = billStore.accountBooks.find(b => b.id === accountBookId);
  return book ? book.name : "";
};

const findClassifyInfo = (
  mainClassifyId: number,
  subClassifyId: number | null
): { mainName: string; subName: string; mainIcon: string; subIcon: string } => {
  const classifyList = billStore.classifyList;

  const mainClassify = classifyList.find(
    c => c.id === mainClassifyId && c.pid === -1
  );
  if (!mainClassify) {
    return { mainName: String(mainClassifyId), subName: "", mainIcon: "", subIcon: "" };
  }

  if (subClassifyId) {
    const subClassify = classifyList.find(
      c => c.id === subClassifyId && c.pid === mainClassifyId
    );
    if (subClassify) {
      return {
        mainName: mainClassify.name,
        subName: subClassify.name,
        mainIcon: getClassifyIcon(mainClassify.image),
        subIcon: getClassifyIcon(subClassify.image)
      };
    }
  }

  return {
    mainName: mainClassify.name,
    subName: "",
    mainIcon: getClassifyIcon(mainClassify.image),
    subIcon: ""
  };
};

const getClassifyDisplay = (row: IncomeExpense) => {
  return findClassifyInfo(row.mainClassify, row.subClassify ?? null);
};

const getTagsByCodes = (tagCodes: string | null | undefined) => {
  if (!tagCodes) return [];
  const tagIdStrs = tagCodes.split(",").map(t => t.trim());
  return billStore.tagList.filter(tag =>
    tagIdStrs.includes(String((tag as any).code))
  );
};

const handleDelete = (row: IncomeExpense) => {
  emit("delete", [row.id]);
};
</script>

<template>
  <el-table
    v-loading="loading"
    :data="data"
    border
    stripe
    style="width: 100%"
    @selection-change="emit('selection-change', $event)"
  >
    <el-table-column type="selection" width="50" />
    <el-table-column prop="date" :label="t('bill.pureDate')" width="100" />
    <el-table-column :label="t('bill.pureAccountBook')" width="100">
      <template #default="{ row }">
        {{ getAccountBookName(row.accountBookId) }}
      </template>
    </el-table-column>
    <el-table-column :label="t('bill.pureAmount')" width="130" align="right">
      <template #default="{ row }">
        <span :style="{ color: isExpense(row.type) ? '#f56c6c' : '#67c23a' }">
          {{ formatAmountWithPrefix(row.amount, row.type) }}
        </span>
      </template>
    </el-table-column>
    <el-table-column :label="t('bill.pureType')" width="80" align="center">
      <template #default="{ row }">
        <el-tag :type="isExpense(row.type) ? 'danger' : 'success'" size="small">
          {{ getTypeLabel(row.type) }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column :label="t('bill.pureClassify')" min-width="120">
      <template #default="{ row }">
        <div class="classify-cell">
          <span
            v-if="getClassifyDisplay(row).subName"
            class="classify-icon"
          >
            {{ getClassifyDisplay(row).subIcon }}
          </span>
          <span v-else class="classify-icon">
            {{ getClassifyDisplay(row).mainIcon }}
          </span>
          <span>{{ getClassifyDisplay(row).mainName }}</span>
          <span v-if="getClassifyDisplay(row).subName" class="sub-classify">
            / {{ getClassifyDisplay(row).subName }}
          </span>
        </div>
      </template>
    </el-table-column>
    <el-table-column
      :label="t('bill.pureCreditCard')"
      width="95"
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
      min-width="120"
    />
    <el-table-column :label="t('bill.pureTag')" min-width="250">
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
    <el-table-column
      :label="t('bill.pureOperation')"
      width="150"
      align="center"
    >
      <template #default="{ row }">
        <el-button type="primary" link :icon="Edit" @click="emit('edit', row)">
          {{ t("bill.pureEdit") }}
        </el-button>
        <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">
          {{ t("bill.pureDelete") }}
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<style lang="scss" scoped>
.classify-cell {
  display: flex;
  align-items: center;
  gap: 4px;

  .classify-icon {
    font-size: 16px;
    margin-right: 2px;
  }

  .sub-classify {
    font-size: 12px;
    color: #909399;
  }
}
</style>
