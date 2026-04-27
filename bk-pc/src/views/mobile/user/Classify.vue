<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { showConfirmDialog } from "vant";
import { useUserStoreHook } from "@/store/modules/user";
import { useClassifyStore } from "@/store/modules/classify";
import {
  getClassifyPage,
  createClassify,
  updateClassify,
  deleteClassify
} from "@/api/classify";
import type { Classify, ClassifyForm, ClassifyType } from "@/types/classify";
import {
  showLoading,
  hideLoading,
  showSuccess,
  showError
} from "@/utils/mobile/message";
import { getClassifyIcon, getClassifyIconOptions } from "@/utils/classifyIcons";

defineOptions({
  name: "MobileClassify"
});

const { t } = useI18n();
const userStore = useUserStoreHook();
const classifyStore = useClassifyStore();

// 当前类型
const currentType = ref<ClassifyType>("EXPENSE");

// 分类列表
const classifyList = ref<Classify[]>([]);

// 树形数据
const treeData = computed(() => {
  const list = classifyList.value;
  const parentList = list.filter(item => item.pid === 0);
  return parentList.map(parent => ({
    ...parent,
    children: list.filter(item => item.pid === parent.id)
  }));
});

// 弹窗状态
const showEditor = ref(false);
const showParentPicker = ref(false);
const isEdit = ref(false);
const saving = ref(false);

// 表单数据
const formData = ref<ClassifyForm>({
  userId: 0,
  name: "",
  pid: 0,
  image: "food",
  sort: 1,
  type: "EXPENSE",
  enable: "YES"
});

// 图标选项
const iconOptions = getClassifyIconOptions();

// 父分类选项
const parentOptions = computed(() => {
  const options = [{ id: 0, name: t("mobile.classify.topLevel") }];
  const parents = classifyList.value.filter(
    item => item.pid === 0 && item.type === formData.value.type
  );
  return [...options, ...parents];
});

// 父分类名称
const parentClassifyName = computed(() => {
  if (formData.value.pid === 0) {
    return t("mobile.classify.topLevel");
  }
  const parent = classifyList.value.find(
    item => item.id === formData.value.pid
  );
  return parent?.name || "";
});

// 加载分类列表
const loadData = async () => {
  const userId = userStore.id;
  if (!userId) return;

  try {
    const result = await getClassifyPage(userId, { type: currentType.value });
    classifyList.value = result?.list || [];
  } catch (error: any) {
    showError(error?.message || t("mobile.common.failed"));
  }
};

// 类型切换
const handleTypeChange = () => {
  loadData();
};

// 打开新增弹窗
const handleAdd = () => {
  isEdit.value = false;
  formData.value = {
    userId: userStore.id,
    name: "",
    pid: 0,
    image: currentType.value === "EXPENSE" ? "food" : "salary",
    sort: 1,
    type: currentType.value,
    enable: "YES"
  };
  showEditor.value = true;
};

// 打开编辑弹窗
const handleEdit = (classify: Classify) => {
  isEdit.value = true;
  formData.value = {
    id: classify.id,
    userId: classify.userId,
    name: classify.name,
    pid: classify.pid,
    image: classify.image,
    sort: classify.sort,
    type: classify.type,
    enable: classify.enable
  };
  showEditor.value = true;
};

// 选择父分类
const handleSelectParent = (id: number) => {
  formData.value.pid = id;
  showParentPicker.value = false;
};

// 提交表单
const handleSubmit = async () => {
  if (!formData.value.name.trim()) {
    showError(t("mobile.classify.namePlaceholder"));
    return;
  }

  saving.value = true;
  showLoading(t("mobile.common.loading"));

  try {
    if (isEdit.value) {
      await updateClassify(formData.value);
      showSuccess(t("mobile.classify.updateSuccess"));
    } else {
      await createClassify(formData.value);
      showSuccess(t("mobile.classify.createSuccess"));
    }
    hideLoading();
    showEditor.value = false;

    // 刷新列表
    await loadData();

    // 同步更新 store
    await classifyStore.fetchList(userStore.id);
  } catch (error: any) {
    hideLoading();
    showError(error?.message || t("mobile.common.failed"));
  } finally {
    saving.value = false;
  }
};

// 删除分类
const handleDelete = async (classify: Classify) => {
  try {
    await showConfirmDialog({
      message: t("mobile.classify.deleteConfirm"),
      confirmButtonColor: "#d83d34"
    });

    showLoading(t("mobile.common.loading"));
    await deleteClassify([classify.id]);
    hideLoading();

    showSuccess(t("mobile.classify.deleteSuccess"));

    // 刷新列表
    await loadData();

    // 同步更新 store
    await classifyStore.fetchList(userStore.id);
  } catch {
    // 取消或失败
    hideLoading();
  }
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="classify-page">
    <!-- 类型筛选 -->
    <van-tabs v-model:active="currentType" shrink @change="handleTypeChange">
      <van-tab name="EXPENSE">{{ t("mobile.record.expense") }}</van-tab>
      <van-tab name="INCOME">{{ t("mobile.record.income") }}</van-tab>
    </van-tabs>

    <!-- 新增按钮 -->
    <div class="add-btn-wrapper">
      <van-button type="danger" size="small" icon="plus" @click="handleAdd">
        {{ t("mobile.common.add") }}
      </van-button>
    </div>

    <!-- 分类列表 -->
    <van-cell-group inset>
      <template v-for="mainClassify in treeData" :key="mainClassify.id">
        <!-- 父分类 -->
        <van-swipe-cell>
          <van-cell
            :title="mainClassify.name"
            is-link
            @click="handleEdit(mainClassify)"
          >
            <template #icon>
              <span class="classify-icon">{{
                getClassifyIcon(mainClassify.image)
              }}</span>
            </template>
            <template #value>
              <van-tag
                v-if="mainClassify.enable === 'YES'"
                type="success"
                size="small"
              >
                {{ t("mobile.classify.enabled") }}
              </van-tag>
            </template>
          </van-cell>
          <template #right>
            <van-button
              square
              type="danger"
              :text="t('mobile.common.delete')"
              @click="handleDelete(mainClassify)"
            />
          </template>
        </van-swipe-cell>

        <!-- 子分类 -->
        <template v-if="mainClassify.children?.length">
          <van-swipe-cell
            v-for="subClassify in mainClassify.children"
            :key="subClassify.id"
          >
            <van-cell
              :title="subClassify.name"
              is-link
              class="sub-classify-cell"
              @click="handleEdit(subClassify)"
            >
              <template #icon>
                <span class="classify-icon sub-icon">{{
                  getClassifyIcon(subClassify.image)
                }}</span>
              </template>
            </van-cell>
            <template #right>
              <van-button
                square
                type="danger"
                :text="t('mobile.common.delete')"
                @click="handleDelete(subClassify)"
              />
            </template>
          </van-swipe-cell>
        </template>
      </template>
    </van-cell-group>

    <van-empty
      v-if="treeData.length === 0"
      :description="t('mobile.common.noData')"
    />

    <!-- 新增/编辑弹窗 -->
    <van-popup
      v-model:show="showEditor"
      position="bottom"
      round
      :style="{ height: '80%' }"
    >
      <div class="classify-editor">
        <div class="editor-header">
          <span class="title">
            {{ isEdit ? t("mobile.classify.edit") : t("mobile.classify.add") }}
          </span>
          <van-icon name="cross" @click="showEditor = false" />
        </div>

        <!-- 类型（新增时显示，编辑时只读） -->
        <van-cell v-if="!isEdit" :title="t('mobile.classify.type')">
          <template #value>
            <van-radio-group v-model="formData.type" direction="horizontal">
              <van-radio name="EXPENSE">{{
                t("mobile.record.expense")
              }}</van-radio>
              <van-radio name="INCOME">{{
                t("mobile.record.income")
              }}</van-radio>
            </van-radio-group>
          </template>
        </van-cell>

        <!-- 父分类 -->
        <van-cell
          :title="t('mobile.classify.parentClassify')"
          is-link
          @click="showParentPicker = true"
        >
          <template #value>
            {{ parentClassifyName }}
          </template>
        </van-cell>

        <!-- 分类名称 -->
        <van-field
          v-model="formData.name"
          :label="t('mobile.classify.name')"
          :placeholder="t('mobile.classify.namePlaceholder')"
          maxlength="20"
          show-word-limit
        />

        <!-- 分类图标 -->
        <div class="icon-selector">
          <div class="selector-label">{{ t("mobile.classify.icon") }}</div>
          <div class="icon-grid">
            <div
              v-for="item in iconOptions"
              :key="item.value"
              class="icon-item"
              :class="{ active: formData.image === item.value }"
              @click="formData.image = item.value"
            >
              {{ item.label }}
            </div>
          </div>
        </div>

        <!-- 排序 -->
        <van-field
          v-model="formData.sort"
          type="digit"
          :label="t('mobile.classify.sort')"
        >
          <template #button>
            <van-stepper v-model="formData.sort" min="1" max="999" />
          </template>
        </van-field>

        <!-- 启用 -->
        <van-cell center :title="t('mobile.classify.enable')">
          <template #right-icon>
            <van-switch
              v-model="formData.enable"
              size="20"
              active-value="YES"
              inactive-value="NO"
              active-color="#d83d34"
            />
          </template>
        </van-cell>

        <!-- 确认按钮 -->
        <div class="editor-footer">
          <van-button
            type="danger"
            block
            round
            :loading="saving"
            @click="handleSubmit"
          >
            {{ t("mobile.common.confirm") }}
          </van-button>
        </div>
      </div>
    </van-popup>

    <!-- 父分类选择弹窗 -->
    <van-action-sheet
      v-model:show="showParentPicker"
      :title="t('mobile.classify.parentClassify')"
    >
      <div class="parent-list">
        <van-cell
          v-for="option in parentOptions"
          :key="option.id"
          :title="option.name"
          clickable
          @click="handleSelectParent(option.id)"
        >
          <template #right-icon>
            <van-icon
              v-if="formData.pid === option.id"
              name="success"
              color="#d83d34"
            />
          </template>
        </van-cell>
      </div>
    </van-action-sheet>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.classify-page {
  min-height: 100vh;
  background-color: $color-background;
}

.add-btn-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 12px 16px;
}

.classify-icon {
  margin-right: 8px;
  font-size: 20px;

  &.sub-icon {
    margin-left: 20px;
  }
}

.sub-classify-cell {
  background-color: #fafafa;
}

.classify-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
  padding-bottom: env(safe-area-inset-bottom);
  overflow-y: auto;
}

.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  .title {
    font-size: 16px;
    font-weight: 500;
    color: $color-text-primary;
  }
}

.icon-selector {
  margin: 16px 0;

  .selector-label {
    margin-bottom: 8px;
    font-size: 14px;
    color: $color-text-secondary;
  }

  .icon-grid {
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: 8px;
  }

  .icon-item {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 32px;
    font-size: 18px;
    cursor: pointer;
    background-color: #f5f5f5;
    border-radius: 4px;
    transition: all 0.2s;

    &.active {
      color: #fff;
      background-color: $color-primary;
    }
  }
}

.editor-footer {
  padding-top: 16px;
  margin-top: auto;
}

.parent-list {
  max-height: 300px;
  padding-bottom: env(safe-area-inset-bottom);
  overflow-y: auto;
}
</style>
