<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { ElMessageBox, ElMessage } from "element-plus";
import { Edit, Delete, Plus } from "@element-plus/icons-vue";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import {
  getAccountBookList,
  createAccountBook,
  updateAccountBook,
  deleteAccountBook
} from "@/api/accountBook";
import type { AccountBook, AccountBookForm } from "@/types/accountBook";
import { ACCOUNT_BOOK_ICON_MAP, getAccountBookIcon } from "@/utils/accountBook";

defineOptions({
  name: "AccountBookSetting"
});

const { t } = useI18n();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

const loading = ref(false);
const tableData = ref<AccountBook[]>([]);
const dialogVisible = ref(false);
const dialogTitle = ref("");
const formRef = ref();
const saving = ref(false);
const selectedRows = ref<AccountBook[]>([]);

const searchForm = ref({
  name: ""
});

const pagination = ref({
  pageNo: 1,
  pageSize: 10,
  total: 0
});

const formData = ref<AccountBookForm>({
  userId: 0,
  name: "",
  image: "book",
  isDefault: "NO"
});

const rules = {
  name: [
    {
      required: true,
      message: t("accountBook.pureAccountBookNameRequired"),
      trigger: "blur"
    },
    {
      max: 20,
      message: t("accountBook.pureAccountBookNameLength"),
      trigger: "blur"
    }
  ]
};

const iconOptions = Object.entries(ACCOUNT_BOOK_ICON_MAP).map(
  ([key, value]) => ({
    value: key,
    label: value
  })
);

const loadData = async () => {
  if (!userStore.id) return;
  loading.value = true;
  try {
    const result = await getAccountBookList(userStore.id, {
      name: searchForm.value.name || undefined,
      pageNo: pagination.value.pageNo,
      pageSize: pagination.value.pageSize
    });
    if (Array.isArray(result)) {
      tableData.value = result;
      pagination.value.total = result.length;
    } else {
      tableData.value = result.list || result.record || [];
      pagination.value.total = result.totalCount || result.total || 0;
    }
  } catch {
    tableData.value = [];
    pagination.value.total = 0;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  pagination.value.pageNo = 1;
  loadData();
};

const handleReset = () => {
  searchForm.value.name = "";
  pagination.value.pageNo = 1;
  loadData();
};

const handlePageChange = (page: number) => {
  pagination.value.pageNo = page;
  loadData();
};

const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size;
  pagination.value.pageNo = 1;
  loadData();
};

const handleAdd = () => {
  dialogTitle.value = t("accountBook.pureAdd");
  formData.value = {
    userId: userStore.id,
    name: "",
    image: "book",
    isDefault: "NO"
  };
  dialogVisible.value = true;
};

const handleEdit = (row: AccountBook) => {
  dialogTitle.value = t("accountBook.pureEdit");
  formData.value = {
    id: row.id,
    userId: row.userId,
    name: row.name,
    image: row.image,
    isDefault: row.isDefault
  };
  dialogVisible.value = true;
};

const handleDelete = async (row: AccountBook) => {
  if (row.isDefault === "YES") {
    ElMessage.warning(t("accountBook.pureCannotDeleteDefault"));
    return;
  }
  try {
    await ElMessageBox.confirm(
      t("accountBook.pureDeleteAccountBookConfirm"),
      t("accountBook.pureAccountBookWarning"),
      {
        confirmButtonText: t("accountBook.pureAccountBookConfirm"),
        cancelButtonText: t("accountBook.pureCancel"),
        type: "warning"
      }
    );
    await deleteAccountBook([row.id]);
    ElMessage.success(t("accountBook.pureDeleteSuccess"));
    await refreshAccountBookCache();
    loadData();
  } catch {
    // cancelled
  }
};

const handleSelectionChange = (selection: AccountBook[]) => {
  selectedRows.value = selection;
};

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning(t("accountBook.pureAccountBookSelectFirst"));
    return;
  }
  const hasDefault = selectedRows.value.some(row => row.isDefault === "YES");
  if (hasDefault) {
    ElMessage.warning(t("accountBook.pureCannotDeleteDefault"));
    return;
  }
  try {
    await ElMessageBox.confirm(
      t("accountBook.pureAccountBookBatchDeleteConfirm", {
        count: selectedRows.value.length
      }),
      t("accountBook.pureAccountBookWarning"),
      {
        confirmButtonText: t("accountBook.pureAccountBookConfirm"),
        cancelButtonText: t("accountBook.pureCancel"),
        type: "warning"
      }
    );
    const ids = selectedRows.value.map(row => row.id);
    await deleteAccountBook(ids);
    ElMessage.success(t("accountBook.pureDeleteSuccess"));
    selectedRows.value = [];
    await refreshAccountBookCache();
    loadData();
  } catch {
    // cancelled
  }
};

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  saving.value = true;
  try {
    if (formData.value.id) {
      await updateAccountBook(formData.value);
      ElMessage.success(t("accountBook.pureUpdateSuccess"));
    } else {
      await createAccountBook(formData.value);
      ElMessage.success(t("accountBook.pureCreateSuccess"));
    }
    dialogVisible.value = false;
    await refreshAccountBookCache();
    loadData();
  } catch (error: any) {
    ElMessage.error(error?.message || t("accountBook.pureOperationFail"));
  } finally {
    saving.value = false;
  }
};

const refreshAccountBookCache = async () => {
  if (userStore.id) {
    const { getAccountBooks } = await import("@/api/accountBook");
    try {
      const result = await getAccountBooks(userStore.id);
      billStore.accountBooks = result || [];
    } catch {
      billStore.accountBooks = [];
    }
  }
};

const beforeIsDefaultChange = async (): Promise<boolean> => {
  if (formData.value.id && formData.value.isDefault === "YES") {
    const hasOtherDefault = tableData.value.some(
      book => book.id !== formData.value.id && book.isDefault === "YES"
    );
    if (!hasOtherDefault) {
      ElMessage.warning(t("accountBook.pureCannotCancelDefault"));
      return false;
    }
  }
  return true;
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="account-book-setting-container">
    <el-card class="account-book-filter" shadow="never">
      <el-form :model="searchForm" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item :label="t('accountBook.pureAccountBookNameQuery')">
              <el-input
                v-model="searchForm.name"
                :placeholder="t('accountBook.pureAccountBookNameRequired')"
                clearable
                @keyup.enter="handleQuery"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item>
              <el-button type="primary" @click="handleQuery">
                {{ t("bill.pureQuery") }}
              </el-button>
              <el-button @click="handleReset">{{
                t("bill.pureReset")
              }}</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="handleAdd">
        {{ t("accountBook.pureAddAccountBook") }}
      </el-button>
      <el-button
        type="danger"
        :icon="Delete"
        :disabled="selectedRows.length === 0"
        @click="handleBatchDelete"
      >
        {{ t("tag.pureBatchDelete") }} ({{ selectedRows.length }})
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column
        prop="image"
        :label="t('accountBook.pureAccountBookImage')"
        width="120"
        align="center"
      >
        <template #default="{ row }">
          <span style="font-size: 20px">{{
            getAccountBookIcon(row.image)
          }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="name"
        :label="t('accountBook.pureAccountBookName')"
        min-width="200"
      >
        <template #default="{ row }">
          <span>{{ row.name }}</span>
          <el-tag
            v-if="row.isDefault === 'YES'"
            type="warning"
            size="small"
            style="margin-left: 8px"
          >
            {{ t("accountBook.pureDefault") }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        :label="t('accountBook.pureAccountBookOperation')"
        width="150"
        align="center"
      >
        <template #default="{ row }">
          <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">
            {{ t("accountBook.pureEdit") }}
          </el-button>
          <el-button
            type="danger"
            link
            :icon="Delete"
            @click="handleDelete(row)"
          >
            {{ t("accountBook.pureDelete") }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.pageNo"
      v-model:page-size="pagination.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      style="justify-content: flex-end; margin-top: 16px"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item :label="t('accountBook.pureAccountBookName')" prop="name">
          <el-input
            v-model="formData.name"
            :placeholder="t('accountBook.pureAccountBookNameRequired')"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="t('accountBook.pureAccountBookImage')">
          <el-select
            v-model="formData.image"
            :placeholder="t('accountBook.pureAccountBookImagePlaceholder')"
            style="width: 100%"
          >
            <el-option
              v-for="item in iconOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
              <span style="margin-right: 8px; font-size: 20px">{{
                item.label
              }}</span>
              <span>{{ item.value }}</span>
            </el-option>
          </el-select>
          <div style="margin-top: 8px">
            <span style="font-size: 32px">{{
              getAccountBookIcon(formData.image)
            }}</span>
          </div>
        </el-form-item>
        <el-form-item :label="t('accountBook.pureIsDefault')">
          <el-switch
            v-model="formData.isDefault"
            active-value="YES"
            inactive-value="NO"
            :before-change="beforeIsDefaultChange"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{
          t("accountBook.pureCancel")
        }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          {{ t("accountBook.pureAccountBookConfirm") }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.account-book-setting-container {
  padding: 16px;
}

.account-book-filter {
  margin-bottom: 16px;
}

.toolbar {
  margin-bottom: 16px;
}
</style>
