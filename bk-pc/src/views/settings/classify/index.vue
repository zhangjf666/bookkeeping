<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { ElMessageBox, ElMessage } from "element-plus";
import { Edit, Delete, Plus, Search } from "@element-plus/icons-vue";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import {
  getClassifyList,
  createClassify,
  updateClassify,
  deleteClassify
} from "@/api/classify";
import type { Classify, ClassifyForm, ClassifyType } from "@/types/classify";
import {
  CLASSIFY_ICON_MAP,
  getClassifyIcon,
  getClassifyIconOptions
} from "@/utils/classifyIcons";

defineOptions({
  name: "ClassifySetting"
});

const { t } = useI18n();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

const loading = ref(false);
const allClassifyList = ref<Classify[]>([]);
const treeRef = ref();
const dialogVisible = ref(false);
const dialogTitle = ref("");
const formRef = ref();
const saving = ref(false);
const currentTreeType = ref<ClassifyType>("EXPENSE");
const treeSearchText = ref("");
const iconPopoverVisible = ref(false);

const formData = ref<ClassifyForm>({
  userId: 0,
  name: "",
  pid: -1,
  image: "food",
  sort: 1,
  type: "EXPENSE",
  enable: "YES"
});

const rules = {
  name: [
    {
      required: true,
      message: t("classify.pureClassifyNameRequired"),
      trigger: "blur"
    },
    {
      max: 20,
      message: t("classify.pureClassifyNameLength"),
      trigger: "blur"
    }
  ],
  image: [
    {
      required: true,
      message: t("classify.pureIconRequired"),
      trigger: "change"
    }
  ]
};

const iconOptions = getClassifyIconOptions();

const filteredClassifyList = computed(() => {
  let list = allClassifyList.value.filter(
    item => item.type === currentTreeType.value
  );

  if (treeSearchText.value) {
    const searchText = treeSearchText.value.toLowerCase();
    const filterIds = new Set<number>();

    list.forEach(item => {
      if (item.name.toLowerCase().includes(searchText)) {
        filterIds.add(item.id);
        if (item.pid !== -1) {
          filterIds.add(item.pid);
        }
      }
    });

    list = list.filter(item => filterIds.has(item.id));
  }

  return list;
});

const treeData = computed(() => {
  return buildTree(filteredClassifyList.value);
});

const mainClassifyOptions = computed(() => {
  const options = [{ value: -1, label: t("classify.pureTopLevel") }];
  filteredClassifyList.value
    .filter(item => item.pid === -1)
    .forEach(item => {
      options.push({ value: item.id, label: item.name });
    });
  return options;
});

const buildTree = (list: Classify[]): Classify[] => {
  const tree: Classify[] = [];
  const map = new Map<number, Classify>();

  list.forEach(item => {
    map.set(item.id, { ...item, children: [] });
  });

  list.forEach(item => {
    const node = map.get(item.id)!;
    if (item.pid === -1) {
      tree.push(node);
    } else {
      const parent = map.get(item.pid);
      if (parent) {
        parent.children = parent.children || [];
        parent.children.push(node);
      } else {
        tree.push(node);
      }
    }
  });

  return tree;
};

const loadData = async () => {
  if (!userStore.id) return;
  loading.value = true;
  try {
    const result = await getClassifyList(userStore.id, {});
    allClassifyList.value = result || [];
  } catch {
    allClassifyList.value = [];
  } finally {
    loading.value = false;
  }
};

const handleTypeChange = (type: ClassifyType) => {
  currentTreeType.value = type;
  treeSearchText.value = "";
};

const handleTreeNodeClick = (data: Classify) => {
  handleEdit(data);
};

const handleAdd = () => {
  dialogTitle.value = t("classify.pureAddClassify");
  formData.value = {
    userId: userStore.id,
    name: "",
    pid: -1,
    image: "food",
    sort: 1,
    type: currentTreeType.value,
    enable: "YES"
  };
  dialogVisible.value = true;
};

const handleEdit = (row: Classify) => {
  dialogTitle.value = t("classify.pureEditClassify");
  formData.value = {
    id: row.id,
    userId: row.userId,
    name: row.name,
    pid: row.pid,
    image: row.image,
    sort: row.sort,
    type: row.type,
    enable: row.enable
  };
  dialogVisible.value = true;
};

const handleDelete = async (row: Classify) => {
  try {
    await ElMessageBox.confirm(
      t("classify.pureDeleteClassifyConfirm"),
      t("classify.pureClassifyWarning"),
      {
        confirmButtonText: t("classify.pureClassifyConfirm"),
        cancelButtonText: t("classify.pureCancel"),
        type: "warning"
      }
    );
    await deleteClassify([row.id]);
    ElMessage.success(t("classify.pureDeleteSuccess"));
    await refreshClassifyCache();
    await loadData();
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
      await updateClassify(formData.value);
      ElMessage.success(t("classify.pureUpdateSuccess"));
    } else {
      await createClassify(formData.value);
      ElMessage.success(t("classify.pureCreateSuccess"));
    }
    dialogVisible.value = false;
    await refreshClassifyCache();
    await loadData();
  } catch (error: any) {
    ElMessage.error(error?.message || t("classify.pureOperationFail"));
  } finally {
    saving.value = false;
  }
};

const refreshClassifyCache = async () => {
  if (userStore.id) {
    try {
      const result = await getClassifyList(userStore.id);
      const flatList = result || [];
      billStore.classifyList = flatList;
      billStore.classifyTree = buildTree(flatList);
    } catch {
      billStore.classifyList = [];
      billStore.classifyTree = [];
    }
  }
};

const getTypeText = (type: ClassifyType) => {
  return type === "EXPENSE"
    ? t("dashboard.pureExpense")
    : t("dashboard.pureIncome");
};

const selectIcon = (icon: string) => {
  formData.value.image = icon;
  iconPopoverVisible.value = false;
};

const getIconLabel = (value: string): string => {
  return CLASSIFY_ICON_MAP[value] || "📌";
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="classify-setting-container">
    <div class="classify-main">
      <el-card class="classify-tree-card" shadow="never">
        <div class="tree-header">
          <el-radio-group v-model="currentTreeType" @change="handleTypeChange">
            <el-radio-button value="EXPENSE">{{
              t("dashboard.pureExpense")
            }}</el-radio-button>
            <el-radio-button value="INCOME">{{
              t("dashboard.pureIncome")
            }}</el-radio-button>
          </el-radio-group>
        </div>

        <el-input
          v-model="treeSearchText"
          :placeholder="t('classify.pureClassifyNameQuery')"
          clearable
          class="tree-search"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <div class="tree-toolbar">
          <el-button
            type="primary"
            :icon="Plus"
            size="small"
            @click="handleAdd"
          >
            {{ t("classify.pureAddClassify") }}
          </el-button>
        </div>

        <el-tree
          ref="treeRef"
          v-loading="loading"
          :data="treeData"
          :props="{ label: 'name', children: 'children' }"
          node-key="id"
          highlight-current
          :expand-on-click-node="false"
          :default-expand-all="false"
          @node-click="handleTreeNodeClick"
        >
          <template #default="{ node, data }">
            <span class="tree-node">
              <span class="tree-node-icon">{{
                getClassifyIcon(data.image)
              }}</span>
              <span class="tree-node-label">{{ node.label }}</span>
              <span class="tree-node-actions">
                <el-button
                  type="primary"
                  link
                  :icon="Edit"
                  size="small"
                  @click.stop="handleEdit(data)"
                >
                  {{ t("classify.pureEdit") }}
                </el-button>
                <el-button
                  type="danger"
                  link
                  :icon="Delete"
                  size="small"
                  @click.stop="handleDelete(data)"
                >
                  {{ t("classify.pureDelete") }}
                </el-button>
              </span>
            </span>
          </template>
        </el-tree>
      </el-card>

      <el-card class="classify-form-card" shadow="never">
        <template v-if="dialogVisible">
          <div class="form-title">{{ dialogTitle }}</div>
          <el-form
            ref="formRef"
            :model="formData"
            :rules="rules"
            label-width="100px"
          >
            <el-form-item :label="t('classify.pureType')">
              <el-tag
                :type="formData.type === 'EXPENSE' ? 'danger' : 'success'"
              >
                {{ getTypeText(formData.type) }}
              </el-tag>
            </el-form-item>
            <el-form-item :label="t('classify.pureParentClassify')">
              <el-select
                v-model="formData.pid"
                :placeholder="t('classify.pureParentClassifyPlaceholder')"
                style="width: 100%"
                filterable
              >
                <el-option
                  v-for="item in mainClassifyOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('classify.pureClassifyName')" prop="name">
              <el-input
                v-model="formData.name"
                :placeholder="t('classify.pureClassifyNameRequired')"
                maxlength="20"
                show-word-limit
              />
            </el-form-item>
            <el-form-item :label="t('classify.pureClassifyIcon')" prop="image">
              <el-popover
                v-model:visible="iconPopoverVisible"
                placement="bottom"
                :width="400"
                trigger="click"
              >
                <template #reference>
                  <div class="icon-selector">
                    <span class="selected-icon">{{
                      getClassifyIcon(formData.image)
                    }}</span>
                  </div>
                </template>
                <div class="icon-grid">
                  <div
                    v-for="item in iconOptions"
                    :key="item.value"
                    class="icon-item"
                    :class="{ active: formData.image === item.value }"
                    @click="selectIcon(item.value)"
                  >
                    {{ item.label }}
                  </div>
                </div>
              </el-popover>
            </el-form-item>
            <el-form-item :label="t('classify.pureSort')">
              <el-input-number
                v-model="formData.sort"
                :min="1"
                :max="999"
                controls-position="right"
              />
            </el-form-item>
            <el-form-item :label="t('classify.pureEnable')">
              <el-switch
                v-model="formData.enable"
                active-value="YES"
                inactive-value="NO"
              />
            </el-form-item>
          </el-form>
          <div class="form-footer">
            <el-button @click="dialogVisible = false">
              {{ t("classify.pureCancel") }}
            </el-button>
            <el-button type="primary" :loading="saving" @click="handleSubmit">
              {{ t("classify.pureClassifyConfirm") }}
            </el-button>
          </div>
        </template>
        <div v-else class="empty-form">
          <el-empty :description="t('classify.pureSelectClassifyToEdit')" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.classify-setting-container {
  box-sizing: border-box;
  height: 100%;
  padding: 16px;
}

.classify-main {
  display: flex;
  gap: 16px;
  height: calc(100vh - 180px);
}

.classify-tree-card {
  display: flex;
  flex-shrink: 0;
  flex-direction: column;
  width: 400px;

  :deep(.el-card__body) {
    display: flex;
    flex: 1;
    flex-direction: column;
    overflow: hidden;
  }
}

.tree-header {
  margin-bottom: 12px;
}

.tree-search {
  margin-bottom: 12px;

  :deep(.el-input__wrapper) {
    padding-left: 8px;
  }

  :deep(.el-input__inner) {
    padding-left: 4px;
  }
}

.tree-toolbar {
  margin-bottom: 12px;
}

:deep(.el-tree) {
  flex: 1;
  overflow: auto;
}

.tree-node {
  display: flex;
  flex: 1;
  align-items: center;
  padding-right: 8px;
}

.tree-node-icon {
  margin-right: 6px;
  font-size: 16px;
}

.tree-node-label {
  flex: 1;
}

.tree-node-actions {
  display: none;
  margin-left: 8px;
}

:deep(.el-tree-node__content:hover) .tree-node-actions {
  display: inline-flex;
}

.classify-form-card {
  flex: 1;
  overflow: auto;
}

.form-title {
  padding-bottom: 12px;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 500;
  border-bottom: 1px solid #eee;
}

.form-footer {
  margin-top: 24px;
  text-align: right;
}

.empty-form {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 300px;
}

.icon-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  padding: 8px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;

  &:hover {
    border-color: #409eff;
  }
}

.selected-icon {
  font-size: 24px;
}

.arrow-icon {
  color: #909399;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.icon-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  font-size: 24px;
  cursor: pointer;
  border-radius: 4px;

  &:hover {
    background-color: #f5f7fa;
  }

  &.active {
    background-color: #ecf5ff;
    border: 1px solid #409eff;
  }
}
</style>
