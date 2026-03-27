<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { ElMessageBox, ElMessage } from "element-plus";
import { Edit, Delete, Plus } from "@element-plus/icons-vue";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import {
  getUserTagList,
  createUserTag,
  updateUserTag,
  deleteUserTag
} from "@/api/userTag";
import type { UserTag, UserTagForm } from "@/types/userTag";

defineOptions({
  name: "TagSetting"
});

const { t } = useI18n();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

const loading = ref(false);
const tableData = ref<UserTag[]>([]);
const dialogVisible = ref(false);
const dialogTitle = ref("");
const formRef = ref();
const saving = ref(false);
const selectedRows = ref<UserTag[]>([]);

const searchForm = ref({
  name: ""
});

const pagination = ref({
  pageNo: 1,
  pageSize: 10,
  total: 0
});

const formData = ref<UserTagForm>({
  userId: 0,
  name: "",
  color: "#409EFF80",
  sort: 0
});

const rules = {
  name: [
    { required: true, message: t("tag.pureTagNameRequired"), trigger: "blur" },
    { max: 20, message: t("tag.pureTagNameLength"), trigger: "blur" }
  ],
  color: [
    { required: true, message: t("tag.pureColorRequired"), trigger: "change" }
  ]
};

const colorPresets = [
  "#FF6B6B80",
  "#FF8C6980",
  "#FFA07A80",
  "#FFB34780",
  "#FFD70080",
  "#FFEAA780",
  "#F7DC6F80",
  "#98D8C880",
  "#96CEB480",
  "#4ECDC480",
  "#45B7D180",
  "#87CEEB80",
  "#6BB3F080",
  "#DDA0DD80",
  "#DA70D680",
  "#FF69B480",
  "#FF149380",
  "#DC143C80",
  "#B2222280",
  "#2F4F4F80",
  "#70809080",
  "#77889980",
  "#A9A9A980",
  "#00000080",
  "#FF000080",
  "#00FF0080",
  "#0000FF80",
  "#FFFF0080",
  "#00FFFF80",
  "#FF00FF80"
];

const rgbToHex = (rgb: string): string => {
  if (!rgb || !rgb.startsWith("rgb")) return rgb;
  const rgba = rgb.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*([\d.]+))?\)/);
  if (!rgba) return rgb;

  const r = parseInt(rgba[1]).toString(16).padStart(2, "0");
  const g = parseInt(rgba[2]).toString(16).padStart(2, "0");
  const b = parseInt(rgba[3]).toString(16).padStart(2, "0");
  const a = rgba[4]
    ? Math.round(parseFloat(rgba[4]) * 255)
        .toString(16)
        .padStart(2, "0")
    : "";

  return `#${r}${g}${b}${a}`;
};

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
  dialogTitle.value = t("tag.pureAdd");
  formData.value = {
    userId: userStore.id,
    name: "",
    color: "#409EFF80",
    sort: pagination.value.total + 1
  };
  dialogVisible.value = true;
};

const handleEdit = (row: UserTag) => {
  dialogTitle.value = t("tag.pureEdit");
  formData.value = {
    id: row.id,
    userId: row.userId,
    name: row.name,
    color: row.color,
    sort: row.sort
  };
  dialogVisible.value = true;
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

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  const submitData = {
    ...formData.value,
    color: rgbToHex(formData.value.color)
  };

  saving.value = true;
  try {
    if (submitData.id) {
      await updateUserTag(submitData);
      ElMessage.success(t("tag.pureUpdateSuccess"));
    } else {
      await createUserTag(submitData);
      ElMessage.success(t("tag.pureCreateSuccess"));
    }
    dialogVisible.value = false;
    await refreshTagCache();
    loadData();
  } catch (error: any) {
    ElMessage.error(error?.message || t("tag.pureOperationFail"));
  } finally {
    saving.value = false;
  }
};

const handleColorChange = (color: string) => {
  formData.value.color = color;
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
        <el-form-item :label="t('tag.pureTagName')" prop="name">
          <el-input
            v-model="formData.name"
            :placeholder="t('tag.pureTagNamePlaceholder')"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="t('tag.pureColor')" prop="color">
          <el-color-picker
            v-model="formData.color"
            show-alpha
            :predefine="colorPresets"
            @change="handleColorChange"
          />
          <span class="color-input-hint">{{ t("tag.pureColorHint") }}</span>
        </el-form-item>
        <el-form-item :label="t('tag.purePreview')">
          <el-tag :color="formData.color" :style="{ color: '#fff' }">
            {{ formData.name || t("tag.pureTagNamePlaceholder") }}
          </el-tag>
        </el-form-item>
        <el-form-item :label="t('tag.pureSort')" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{
          t("tag.pureCancel")
        }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          {{ t("tag.pureTagConfirm") }}
        </el-button>
      </template>
    </el-dialog>
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
