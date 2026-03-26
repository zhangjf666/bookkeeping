<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { ElMessageBox, ElMessage } from "element-plus";
import { Edit, Delete, Plus } from "@element-plus/icons-vue";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import {
  getUserRemarkList,
  createUserRemark,
  updateUserRemark,
  deleteUserRemark
} from "@/api/remark";
import type { UserRemark, UserRemarkForm } from "@/types/remark";
import { getClassifyIcon } from "@/utils/classifyIcons";

defineOptions({
  name: "RemarkSetting"
});

const { t } = useI18n();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

const loading = ref(false);
const tableData = ref<UserRemark[]>([]);
const dialogVisible = ref(false);
const dialogTitle = ref("");
const formRef = ref();
const saving = ref(false);
const selectedRows = ref<UserRemark[]>([]);

const searchForm = ref({
  remark: "",
  classifyId: undefined as number | undefined
});

const pagination = ref({
  pageNo: 1,
  pageSize: 10,
  total: 0
});

const formData = ref<UserRemarkForm>({
  userId: 0,
  remark: "",
  classifyId: undefined as number | undefined
});

const rules = {
  remark: [
    {
      required: true,
      message: t("remark.pureRemarkNameRequired"),
      trigger: "blur"
    },
    { max: 20, message: t("remark.pureRemarkNameLength"), trigger: "blur" }
  ],
  classifyId: [
    {
      required: true,
      message: t("remark.pureClassifyRequired"),
      trigger: "change"
    }
  ]
};

const loadClassifyData = async () => {
  if (userStore.id && billStore.classifyList.length === 0) {
    await billStore.loadClassifyAndTag(userStore.id);
  }
};

const loadData = async () => {
  if (!userStore.id) return;
  loading.value = true;
  try {
    const result = await getUserRemarkList(userStore.id, {
      remark: searchForm.value.remark || undefined,
      classifyId: searchForm.value.classifyId,
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

const refreshRemarkCache = async () => {
  if (userStore.id) {
    const { getRemarkList } = await import("@/api/incomeExpense");
    try {
      const result = await getRemarkList(userStore.id);
      billStore.remarkList = result || [];
    } catch {
      billStore.remarkList = [];
    }
  }
};

const handleQuery = () => {
  pagination.value.pageNo = 1;
  loadData();
};

const handleReset = () => {
  searchForm.value.remark = "";
  searchForm.value.classifyId = undefined;
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
  dialogTitle.value = t("remark.pureAdd");
  formData.value = {
    userId: userStore.id,
    remark: "",
    classifyId: undefined
  };
  dialogVisible.value = true;
};

const handleEdit = (row: UserRemark) => {
  dialogTitle.value = t("remark.pureEdit");
  formData.value = {
    id: row.id,
    userId: row.userId,
    remark: row.remark,
    classifyId: row.classifyId
  };
  dialogVisible.value = true;
};

const handleDelete = async (row: UserRemark) => {
  try {
    await ElMessageBox.confirm(
      t("remark.pureDeleteRemarkConfirm"),
      t("remark.pureRemarkWarning"),
      {
        confirmButtonText: t("remark.pureRemarkConfirm"),
        cancelButtonText: t("remark.pureCancel"),
        type: "warning"
      }
    );
    await deleteUserRemark([row.id]);
    ElMessage.success(t("remark.pureDeleteSuccess"));
    await refreshRemarkCache();
    loadData();
  } catch {
    // cancelled
  }
};

const handleSelectionChange = (selection: UserRemark[]) => {
  selectedRows.value = selection;
};

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning(t("remark.pureSelectFirst"));
    return;
  }
  try {
    await ElMessageBox.confirm(
      t("remark.pureBatchDeleteConfirm", { count: selectedRows.value.length }),
      t("remark.pureRemarkWarning"),
      {
        confirmButtonText: t("remark.pureRemarkConfirm"),
        cancelButtonText: t("remark.pureCancel"),
        type: "warning"
      }
    );
    const ids = selectedRows.value.map(row => row.id);
    await deleteUserRemark(ids);
    ElMessage.success(t("remark.pureDeleteSuccess"));
    selectedRows.value = [];
    await refreshRemarkCache();
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
      await updateUserRemark(formData.value);
      ElMessage.success(t("remark.pureUpdateSuccess"));
    } else {
      await createUserRemark(formData.value);
      ElMessage.success(t("remark.pureCreateSuccess"));
    }
    dialogVisible.value = false;
    await refreshRemarkCache();
    loadData();
  } catch (error: any) {
    ElMessage.error(error?.message || t("remark.pureOperationFail"));
  } finally {
    saving.value = false;
  }
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

const getClassifyDisplay = (classifyId: number) => {
  const classifyList = billStore.classifyList;
  const classify = classifyList.find(c => c.id === classifyId);
  if (!classify) {
    return { mainName: String(classifyId), subName: "" };
  }
  if (classify.pid === -1) {
    return { mainName: classify.name, subName: "" };
  }
  const parentClassify = classifyList.find(c => c.id === classify.pid);
  if (parentClassify) {
    return { mainName: parentClassify.name, subName: classify.name };
  }
  return { mainName: classify.name, subName: "" };
};

const classifyTreeData = computed(() => billStore.classifyTree);

const classifyTreeDataWithIcon = computed(() => {
  const transformNode = (node: any): any => {
    return {
      ...node,
      name: `${getClassifyIcon(node.image)} ${node.name}`,
      children: node.children?.map(transformNode)
    };
  };
  return classifyTreeData.value.map(transformNode);
});

onMounted(async () => {
  await loadClassifyData();
  loadData();
});
</script>

<template>
  <div class="remark-setting-container">
    <el-card class="remark-filter" shadow="never">
      <el-form :model="searchForm" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item :label="t('remark.pureRemarkNameQuery')">
              <el-input
                v-model="searchForm.remark"
                :placeholder="t('remark.pureRemarkNamePlaceholder')"
                clearable
                @keyup.enter="handleQuery"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item :label="t('remark.pureRemarkClassifyQuery')">
              <el-tree-select
                v-model="searchForm.classifyId"
                :data="classifyTreeDataWithIcon"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                :placeholder="t('bill.pureSelectPlaceholder')"
                check-strictly
                clearable
                filterable
                :render-after-expand="false"
                style="width: 100%"
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
        {{ t("remark.pureAddRemark") }}
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
        prop="remark"
        :label="t('remark.pureRemarkName')"
        min-width="200"
      />
      <el-table-column :label="t('remark.pureRemarkClassify')" min-width="150">
        <template #default="{ row }">
          <div class="classify-cell">
            <span>{{ getClassifyDisplay(row.classifyId).mainName }}</span>
            <span
              v-if="getClassifyDisplay(row.classifyId).subName"
              class="sub-classify"
            >
              / {{ getClassifyDisplay(row.classifyId).subName }}
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        :label="t('remark.pureRemarkOperation')"
        width="150"
        align="center"
      >
        <template #default="{ row }">
          <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">
            {{ t("remark.pureEdit") }}
          </el-button>
          <el-button
            type="danger"
            link
            :icon="Delete"
            @click="handleDelete(row)"
          >
            {{ t("remark.pureDelete") }}
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
      style="margin-top: 16px; justify-content: flex-end"
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
        <el-form-item :label="t('remark.pureRemarkName')" prop="remark">
          <el-input
            v-model="formData.remark"
            :placeholder="t('remark.pureRemarkNamePlaceholder')"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="t('remark.pureRemarkClassify')" prop="classifyId">
          <el-tree-select
            v-model="formData.classifyId"
            :data="classifyTreeDataWithIcon"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            :placeholder="t('remark.pureClassifyRequired')"
            check-strictly
            clearable
            filterable
            :render-after-expand="false"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{
          t("remark.pureCancel")
        }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          {{ t("remark.pureRemarkConfirm") }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.remark-setting-container {
  padding: 16px;
}

.remark-filter {
  margin-bottom: 16px;
}

.toolbar {
  margin-bottom: 16px;
}

.classify-cell {
  .sub-classify {
    font-size: 12px;
    color: #909399;
    margin-left: 4px;
  }
}
</style>
