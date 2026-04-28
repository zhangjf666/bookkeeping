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

// 多选模式
const selectMode = ref(false);
const selectedIds = ref<number[]>([]);

// 长按计时器
let longPressTimer: ReturnType<typeof setTimeout> | null = null;

// 表单数据
const formData = ref<UserTagForm>({
  userId: 0,
  name: "",
  color: "#d83d34",
  sort: 1
});

// 预设颜色列表 (7列 * 4行 = 28种颜色)
const presetColors = [
  // 第一行 - 红色系
  "#d83d34",
  "#e91e63",
  "#f44336",
  "#ff5722",
  "#ff9800",
  "#ffc107",
  "#ffeb3b",
  // 第二行 - 绿色系
  "#4caf50",
  "#8bc34a",
  "#cddc39",
  "#009688",
  "#00bcd4",
  "#03a9f4",
  "#2196f3",
  // 第三行 - 蓝紫色系
  "#3f51b5",
  "#673ab7",
  "#9c27b0",
  "#673ab7",
  "#795548",
  "#9e9e9e",
  "#607d8b",
  // 第四行 - 深色系
  "#000000",
  "#37474f",
  "#455a64",
  "#546e7a",
  "#607d8b",
  "#78909c",
  "#90a4ae"
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
  const maxSort =
    tagList.value.length > 0
      ? Math.max(...tagList.value.map(tg => tg.sort || 0))
      : 0;
  formData.value = {
    userId: userStore.id,
    name: "",
    color: "#d83d34",
    sort: maxSort + 1
  };
  showEditor.value = true;
};

// 打开编辑弹窗
const handleEdit = (tag: UserTag) => {
  if (selectMode.value) return;
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

// 长按开始
const handleTouchStart = (tag: UserTag) => {
  if (selectMode.value) return;

  longPressTimer = setTimeout(() => {
    // 进入多选模式
    selectMode.value = true;
    selectedIds.value = [tag.id];
  }, 500);
};

// 长按结束
const handleTouchEnd = () => {
  if (longPressTimer) {
    clearTimeout(longPressTimer);
    longPressTimer = null;
  }
};

// 点击网格项
const handleItemClick = (tag: UserTag) => {
  if (selectMode.value) {
    // 多选模式下切换选中状态
    const index = selectedIds.value.indexOf(tag.id);
    if (index > -1) {
      selectedIds.value.splice(index, 1);
      // 如果没有选中项，退出多选模式
      if (selectedIds.value.length === 0) {
        selectMode.value = false;
      }
    } else {
      selectedIds.value.push(tag.id);
    }
  } else {
    // 非多选模式下编辑
    handleEdit(tag);
  }
};

// 退出多选模式
const exitSelectMode = () => {
  selectMode.value = false;
  selectedIds.value = [];
};

// 批量删除
const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) return;

  try {
    await showConfirmDialog({
      message: t("mobile.tag.deleteConfirm"),
      confirmButtonColor: "#d83d34"
    });

    showLoading(t("mobile.common.loading"));
    await deleteUserTag(selectedIds.value);
    hideLoading();

    showSuccess(t("mobile.tag.deleteSuccess"));

    // 退出多选模式
    exitSelectMode();

    // 刷新列表
    await loadData();

    // 同步更新 store
    await userTagStore.fetchList(userStore.id);
  } catch {
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

    <!-- 多选模式工具栏 -->
    <div v-if="selectMode" class="select-toolbar">
      <span class="select-info">
        {{ t("mobile.common.selected") }}: {{ selectedIds.length }}
      </span>
      <van-button size="small" @click="exitSelectMode">
        {{ t("mobile.common.cancel") }}
      </van-button>
      <van-button
        type="danger"
        size="small"
        :disabled="selectedIds.length === 0"
        @click="handleBatchDelete"
      >
        {{ t("mobile.common.delete") }}
      </van-button>
    </div>

    <!-- 标签网格 -->
    <div class="grid-container">
      <div
        v-for="tag in filteredList"
        :key="tag.id"
        class="grid-item"
        :class="{ selected: selectedIds.includes(tag.id) }"
        :style="{
          borderColor: selectedIds.includes(tag.id) ? tag.color : undefined
        }"
        @touchstart="handleTouchStart(tag)"
        @touchend="handleTouchEnd"
        @touchcancel="handleTouchEnd"
        @click="handleItemClick(tag)"
      >
        <!-- 选中标记 -->
        <div
          v-if="selectMode"
          class="select-checkbox"
          :style="{ backgroundColor: tag.color }"
        >
          <van-icon v-if="selectedIds.includes(tag.id)" name="success" />
        </div>
        <!-- 标签名称 -->
        <van-tag :color="tag.color" text-color="#fff" size="large">
          {{ tag.name }}
        </van-tag>
      </div>
    </div>

    <van-empty
      v-if="filteredList.length === 0"
      :description="t('mobile.common.noData')"
    />

    <!-- 悬浮添加按钮 -->
    <div v-if="!selectMode" class="floating-btn" @click="handleAdd">
      <van-icon name="plus" size="24" />
    </div>

    <!-- 新增/编辑弹窗 -->
    <van-popup
      v-model:show="showEditor"
      position="bottom"
      round
      :style="{ height: '55%' }"
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
  padding-bottom: calc(60px + env(safe-area-inset-bottom));
  background-color: $color-background;
}

.select-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: $color-card;
  border-bottom: 1px solid $color-border;

  .select-info {
    font-size: 14px;
    color: $color-text-primary;
  }
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 12px;
}

.grid-item {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  justify-content: center;
  min-height: 80px;
  padding: 16px 8px;
  cursor: pointer;
  background-color: $color-card;
  border: 2px solid transparent;
  border-radius: 12px;
  transition: all 0.2s;

  &:active {
    transform: scale(0.98);
  }

  &.selected {
    background-color: rgba($color-primary, 0.1);
  }

  .select-checkbox {
    position: absolute;
    top: 8px;
    right: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    font-size: 12px;
    color: #fff;
    border-radius: 50%;
  }
}

.floating-btn {
  position: fixed;
  right: 16px;
  bottom: calc(16px + env(safe-area-inset-bottom));
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  color: #fff;
  background-color: $color-primary;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgb(0 0 0 / 15%);

  &:active {
    transform: scale(0.95);
  }
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
    grid-template-columns: repeat(7, 1fr);
    gap: 10px;
  }

  .color-item {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    cursor: pointer;
    border-radius: 6px;
    transition: transform 0.2s;

    &.active {
      box-shadow: 0 2px 8px rgb(0 0 0 / 30%);
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
