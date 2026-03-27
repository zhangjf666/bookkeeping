<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import { useI18n } from "vue-i18n";
import { message } from "@/utils/message";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import BillFilter from "./components/BillFilter.vue";
import BillTable from "./components/BillTable.vue";
import BillForm from "./components/BillForm.vue";
import type { IncomeExpense } from "@/types/bill";
import { Plus, Delete } from "@element-plus/icons-vue";
import { ElMessageBox } from "element-plus";

defineOptions({
  name: "Bill"
});

const { t } = useI18n();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

const showForm = ref(false);
const formData = ref<IncomeExpense | null>(null);
const selectedIds = ref<number[]>([]);

const userId = computed(() => userStore.id);
const accountBookId = computed(() => billStore.currentAccountBook?.id);

const handleAdd = () => {
  formData.value = null;
  showForm.value = true;
};

const handleEdit = (row: IncomeExpense) => {
  formData.value = row;
  showForm.value = true;
};

const handleDelete = async (ids: number[]) => {
  if (!userId.value) return;
  try {
    await ElMessageBox.confirm(
      ids.length === 1
        ? t("bill.pureDeleteConfirm")
        : t("bill.pureBatchDeleteConfirm", { count: ids.length }),
      t("bill.pureWarning"),
      {
        confirmButtonText: t("bill.pureConfirm"),
        cancelButtonText: t("bill.pureCancel"),
        type: "warning"
      }
    );
    await billStore.remove(userId.value, ids);
    message(t("bill.pureDeleteSuccess"), { type: "success" });
  } catch (error: any) {
    if (error !== "cancel") {
      message(error?.message || t("bill.pureDeleteFail"), { type: "error" });
    }
  }
};

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    message(t("bill.pureSelectFirst"), { type: "warning" });
    return;
  }
  await handleDelete(selectedIds.value);
  selectedIds.value = [];
};

const handleQuery = (accountBookId?: number) => {
  billStore.loadList(userId.value, accountBookId);
};

const handleReset = () => {
  billStore.resetQueryParams();
  billStore.loadList(userId.value, undefined);
};

const handleSelectionChange = (selection: IncomeExpense[]) => {
  selectedIds.value = selection.map((item: IncomeExpense) => item.id);
};

const handleFormSuccess = () => {
  showForm.value = false;
  billStore.loadList(userId.value, accountBookId.value);
};

const currentPage = computed({
  get: () => billStore.queryParams.pageNo || 1,
  set: (val: number) => {
    billStore.setQueryParams({ pageNo: val });
    billStore.loadList(userId.value, accountBookId.value);
  }
});

const pageSize = computed({
  get: () => billStore.queryParams.pageSize || 10,
  set: (val: number) => {
    billStore.setQueryParams({ pageSize: val, pageNo: 1 });
    billStore.loadList(userId.value, accountBookId.value);
  }
});

onMounted(async () => {
  if (billStore.accountBooks.length === 0) {
    await billStore.loadAccountBooks();
  }
  if (billStore.classifyList.length === 0 && userId.value) {
    await billStore.loadClassifyAndTag(userId.value);
  }
  if (userId.value) {
    billStore.loadList(userId.value, accountBookId.value);
  }
});
</script>

<template>
  <div class="bill-container">
    <BillFilter @query="handleQuery" @reset="handleReset" />

    <div class="bill-toolbar">
      <div class="toolbar-left">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          {{ t("bill.pureAdd") }}
        </el-button>
        <el-button
          type="danger"
          :icon="Delete"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >
          {{ t("bill.pureBatchDelete") }} ({{ selectedIds.length }})
        </el-button>
      </div>
    </div>

    <BillTable
      :data="billStore.list"
      :loading="billStore.listLoading"
      @edit="handleEdit"
      @delete="handleDelete"
      @selection-change="handleSelectionChange"
    />

    <div class="bill-pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="billStore.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="(val: number) => (pageSize = val)"
        @current-change="(val: number) => (currentPage = val)"
      />
    </div>

    <el-dialog
      v-model="showForm"
      :title="formData ? t('bill.pureEdit') : t('bill.pureAdd')"
      width="600px"
      destroy-on-close
    >
      <BillForm
        :data="formData"
        @success="handleFormSuccess"
        @cancel="showForm = false"
      />
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.bill-container {
  padding: 20px;
}

.bill-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  .toolbar-left {
    display: flex;
    gap: 12px;
  }
}

.bill-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
