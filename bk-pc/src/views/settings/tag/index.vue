<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { ElMessageBox, ElMessage } from "element-plus";
import { Edit, Delete, Plus } from "@element-plus/icons-vue";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import { getUserTagList, deleteUserTag } from "@/api/userTag";
import type { UserTag } from "@/types/userTag";
import TagForm from "./TagForm.vue";

defineOptions({
  name: "TagSetting"
});

const { t } = useI18n();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

const loading = ref(false);
const tableData = ref<UserTag[]>([]);
const showTagDialog = ref(false);
const selectedRows = ref<UserTag[]>([]);
const editingTag = ref<UserTag | null>(null);

const searchForm = ref({
  name: ""
});

const pagination = ref({
  pageNo: 1,
  pageSize: 10,
  total: 0
});

const refreshTagCache = async () => {
  if (userStore.id) {
    const { getTagList } = await import("@/api/incomeExpense");
    try {
      const result = await getTagList(userStore.id);
      billStore.tagList = result || [];
    } catch {
      billStore.tagList = [];
    }
  }
};

const loadData = async () => {
  if (!userStore.id) return;
  loading.value = true;
  try {
    const result = await getUserTagList(userStore.id, {
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
  editingTag.value = null;
  showTagDialog.value = true;
};

const handleEdit = (row: UserTag) => {
  editingTag.value = row;
  showTagDialog.value = true;
};

const handleDelete = async (row: UserTag) => {
  try {
    await ElMessageBox.confirm(
      t("tag.pureDeleteTagConfirm"),
      t("tag.pureTagWarning"),
      {
        confirmButtonText: t("tag.pureTagConfirm"),
        cancelButtonText: t("tag.pureCancel"),
        type: "warning"
      }
    );
    await deleteUserTag([row.id]);
    ElMessage.success(t("tag.pureDeleteSuccess"));
    await refreshTagCache();
    loadData();
  } catch {
    // cancelled
  }
};

const handleSelectionChange = (selection: UserTag[]) => {
  selectedRows.value = selection;
};

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning(t("tag.pureSelectFirst"));
    return;
  }
  try {
    await ElMessageBox.confirm(
      t("tag.pureBatchDeleteConfirm", { count: selectedRows.value.length }),
      t("tag.pureTagWarning"),
      {
        confirmButtonText: t("tag.pureTagConfirm"),
        cancelButtonText: t("tag.pureCancel"),
        type: "warning"
      }
    );
    const ids = selectedRows.value.map(row => row.id);
    await deleteUserTag(ids);
    ElMessage.success(t("tag.pureDeleteSuccess"));
    selectedRows.value = [];
    await refreshTagCache();
    loadData();
  } catch {
    // cancelled
  }
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="tag-setting-container">
    <el-card class="tag-filter" shadow="never">
      <el-form :model="searchForm" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item :label="t('tag.pureTagNameQuery')">
              <el-input
                v-model="searchForm.name"
                :placeholder="t('tag.pureTagNamePlaceholder')"
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
        {{ t("tag.pureAddTag") }}
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
        prop="name"
        :label="t('tag.pureTagName')"
        min-width="150"
      >
        <template #default="{ row }">
          <el-tag :color="row.color" :style="{ color: '#fff' }">
            {{ row.name }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="color" :label="t('tag.pureColor')" width="150">
        <template #default="{ row }">
          <div class="color-preview">
            <span class="color-box" :style="{ backgroundColor: row.color }" />
            {{ row.color }}
          </div>
        </template>
      </el-table-column>
      <el-table-column
        prop="sort"
        :label="t('tag.pureSort')"
        width="80"
        align="center"
      />
      <el-table-column
        :label="t('tag.pureTagOperation')"
        width="150"
        align="center"
      >
        <template #default="{ row }">
          <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">
            {{ t("tag.pureEdit") }}
          </el-button>
          <el-button
            type="danger"
            link
            :icon="Delete"
            @click="handleDelete(row)"
          >
            {{ t("tag.pureDelete") }}
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

    <TagForm
      v-model:visible="showTagDialog"
      :data="editingTag"
      @success="loadData"
    />
  </div>
</template>

<style lang="scss" scoped>
.tag-setting-container {
  padding: 16px;
}

.tag-filter {
  margin-bottom: 16px;
}

.toolbar {
  margin-bottom: 16px;
}

.color-preview {
  display: flex;
  gap: 8px;
  align-items: center;
}

.color-box {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.color-input-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}
</style>
