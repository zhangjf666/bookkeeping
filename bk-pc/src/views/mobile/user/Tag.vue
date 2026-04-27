<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { showConfirmDialog } from "vant";
import { useUserStoreHook } from "@/store/modules/user";
import { useUserTagStore } from "@/store/modules/userTag";
import {
  getUserTagPage,
  createUserTag,
  updateUserTag,
  deleteUserTag
} from "@/api/userTag";
import type { UserTag, UserTagForm } from "@/types/userTag";
import {
  showLoading,
  hideLoading,
  showSuccess,
  showError
} from "@/utils/mobile/message";

defineOptions({
  name: "MobileTag"
});

const { t } = useI18n();
const userStore = useUserStoreHook();
const userTagStore = useUserTagStore();

// 标签列表
const tagList = ref<UserTag[]>([]);

// 搜索关键词
const searchKeyword = ref("");

// 过滤后的列表
const filteredList = computed(() => {
  if (!searchKeyword.value.trim()) {
    return tagList.value;
  }
  const keyword = searchKeyword.value.toLowerCase();
  return tagList.value.filter(item =>
    item.name.toLowerCase().includes(keyword)
  );
});

// 弹窗状态
const showEditor = ref(false);
const isEdit = ref(false);
const saving = ref(false);

// 表单数据
const formData = ref<UserTagForm>({
  userId: 0,
  name: "",
  color: "#d83d34",
  sort: 1
});

// 预设颜色列表
const presetColors = [
  "#d83d34",
  "#e91e63",
  "#9c27b0",
  "#673ab7",
  "#3f51b5",
  "#2196f3",
  "#03a9f4",
  "#00bcd4",
  "#009688",
  "#4caf50",
  "#8bc34a",
  "#cddc39",
  "#ffeb3b",
  "#ffc107",
  "#ff9800",
  "#ff5722",
  "#795548",
  "#9e9e9e",
  "#607d8b",
  "#000000"
];

// 加载标签列表
const loadData = async () => {
  const userId = userStore.id;
  if (!userId) return;

  try {
    const result = await getUserTagPage(userId);
    tagList.value = result?.list || result?.record || [];
  } catch (error: any) {
    showError(error?.message || t("mobile.common.failed"));
  }
};

// 打开新增弹窗
const handleAdd = () => {
  isEdit.value = false;
  formData.value = {
    userId: userStore.id,
    name: "",
    color: "#d83d34",
    sort: 1
  };
  showEditor.value = true;
};

// 打开编辑弹窗
const handleEdit = (tag: UserTag) => {
  isEdit.value = true;
  formData.value = {
    id: tag.id,
    userId: tag.userId,
    name: tag.name,
    color: tag.color,
    sort: tag.sort
  };
  showEditor.value = true;
};

// 选择颜色
const handleSelectColor = (color: string) => {
  formData.value.color = color;
};

// 提交表单
const handleSubmit = async () => {
  if (!formData.value.name.trim()) {
    showError(t("mobile.tag.namePlaceholder"));
    return;
  }

  saving.value = true;
  showLoading(t("mobile.common.loading"));

  try {
    if (isEdit.value) {
      await updateUserTag(formData.value);
      showSuccess(t("mobile.tag.updateSuccess"));
    } else {
      await createUserTag(formData.value);
      showSuccess(t("mobile.tag.createSuccess"));
    }
    hideLoading();
    showEditor.value = false;

    // 刷新列表
    await loadData();

    // 同步更新 store
    await userTagStore.fetchList(userStore.id);
  } catch (error: any) {
    hideLoading();
    showError(error?.message || t("mobile.common.failed"));
  } finally {
    saving.value = false;
  }
};

// 删除标签
const handleDelete = async (tag: UserTag) => {
  try {
    await showConfirmDialog({
      message: t("mobile.tag.deleteConfirm"),
      confirmButtonColor: "#d83d34"
    });

    showLoading(t("mobile.common.loading"));
    await deleteUserTag([tag.id]);
    hideLoading();

    showSuccess(t("mobile.tag.deleteSuccess"));

    // 刷新列表
    await loadData();

    // 同步更新 store
    await userTagStore.fetchList(userStore.id);
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
  <div class="tag-page">
    <!-- 搜索框 -->
    <van-search
      v-model="searchKeyword"
      shape="round"
      :placeholder="t('mobile.tag.searchPlaceholder')"
    />

    <!-- 新增按钮 -->
    <div class="add-btn-wrapper">
      <van-button type="danger" size="small" icon="plus" @click="handleAdd">
        {{ t("mobile.common.add") }}
      </van-button>
    </div>

    <!-- 标签列表 -->
    <van-cell-group inset>
      <van-swipe-cell v-for="tag in filteredList" :key="tag.id">
        <van-cell is-link @click="handleEdit(tag)">
          <template #title>
            <van-tag :color="tag.color" text-color="#fff">
              {{ tag.name }}
            </van-tag>
          </template>
        </van-cell>

        <template #right>
          <van-button
            square
            type="danger"
            :text="t('mobile.common.delete')"
            @click="handleDelete(tag)"
          />
        </template>
      </van-swipe-cell>
    </van-cell-group>

    <van-empty
      v-if="filteredList.length === 0"
      :description="t('mobile.common.noData')"
    />

    <!-- 新增/编辑弹窗 -->
    <van-popup
      v-model:show="showEditor"
      position="bottom"
      round
      :style="{ height: '60%' }"
    >
      <div class="tag-editor">
        <div class="editor-header">
          <span class="title">
            {{ isEdit ? t("mobile.tag.edit") : t("mobile.tag.add") }}
          </span>
          <van-icon name="cross" @click="showEditor = false" />
        </div>

        <!-- 标签名称 -->
        <van-field
          v-model="formData.name"
          :label="t('mobile.tag.name')"
          :placeholder="t('mobile.tag.namePlaceholder')"
          maxlength="20"
          show-word-limit
        />

        <!-- 标签颜色 -->
        <div class="color-selector">
          <div class="selector-label">{{ t("mobile.tag.color") }}</div>
          <div class="color-grid">
            <div
              v-for="color in presetColors"
              :key="color"
              class="color-item"
              :style="{ backgroundColor: color }"
              :class="{ active: formData.color === color }"
              @click="handleSelectColor(color)"
            >
              <van-icon
                v-if="formData.color === color"
                name="success"
                color="#fff"
              />
            </div>
          </div>
        </div>

        <!-- 排序 -->
        <van-field
          v-model="formData.sort"
          type="digit"
          :label="t('mobile.tag.sort')"
        >
          <template #button>
            <van-stepper v-model="formData.sort" min="1" max="999" />
          </template>
        </van-field>

        <!-- 预览 -->
        <div class="preview-section">
          <span class="preview-label">{{ t("mobile.tag.preview") }}：</span>
          <van-tag :color="formData.color" text-color="#fff">
            {{ formData.name || t("mobile.tag.name") }}
          </van-tag>
        </div>

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
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.tag-page {
  min-height: 100vh;
  background-color: $color-background;
}

.add-btn-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 0 16px 12px;
}

.tag-editor {
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

.color-selector {
  margin: 16px 0;

  .selector-label {
    margin-bottom: 8px;
    font-size: 14px;
    color: $color-text-secondary;
  }

  .color-grid {
    display: grid;
    grid-template-columns: repeat(10, 1fr);
    gap: 8px;
  }

  .color-item {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    cursor: pointer;
    border-radius: 4px;
    transition: transform 0.2s;

    &.active {
      box-shadow: 0 2px 8px rgb(0 0 0 / 20%);
      transform: scale(1.1);
    }
  }
}

.preview-section {
  display: flex;
  gap: 8px;
  align-items: center;
  margin: 16px 0;

  .preview-label {
    font-size: 14px;
    color: $color-text-secondary;
  }
}

.editor-footer {
  padding-top: 16px;
  margin-top: auto;
}
</style>
