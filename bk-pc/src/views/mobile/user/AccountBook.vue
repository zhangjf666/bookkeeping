<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { showConfirmDialog } from "vant";
import { useUserStoreHook } from "@/store/modules/user";
import { useAccountBookStore } from "@/store/modules/accountBook";
import {
  getAccountBookList,
  createAccountBook,
  updateAccountBook,
  deleteAccountBook
} from "@/api/accountBook";
import type { AccountBook, AccountBookForm } from "@/types/accountBook";
import {
  showLoading,
  hideLoading,
  showSuccess,
  showError
} from "@/utils/mobile/message";

defineOptions({
  name: "MobileAccountBook"
});

const { t } = useI18n();
const userStore = useUserStoreHook();
const accountBookStore = useAccountBookStore();

// 账本列表
const accountBookList = ref<AccountBook[]>([]);

// 搜索关键词
const searchKeyword = ref("");

// 过滤后的列表
const filteredList = computed(() => {
  if (!searchKeyword.value.trim()) {
    return accountBookList.value;
  }
  const keyword = searchKeyword.value.toLowerCase();
  return accountBookList.value.filter(item =>
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
const formData = ref<AccountBookForm>({
  userId: 0,
  name: "",
  image: "book",
  isDefault: "NO"
});

// 账本图标映射
const ACCOUNT_BOOK_ICON_MAP: Record<string, string> = {
  book: "📒",
  travel: "✈️",
  work: "💼",
  home: "🏠",
  car: "🚗",
  money: "💰",
  target: "🎯",
  heart: "❤️",
  star: "⭐",
  gift: "🎁"
};

// 图标选项
const iconOptions = Object.entries(ACCOUNT_BOOK_ICON_MAP).map(
  ([value, label]) => ({
    value,
    label
  })
);

// 获取账本图标
const getAccountBookIcon = (image: string): string => {
  return ACCOUNT_BOOK_ICON_MAP[image] || ACCOUNT_BOOK_ICON_MAP.book;
};

// 加载账本列表
const loadData = async () => {
  const userId = userStore.id;
  if (!userId) return;

  try {
    const result = await getAccountBookList(userId);
    accountBookList.value = result?.list || result?.record || [];
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
    image: "book",
    isDefault: "NO"
  };
  showEditor.value = true;
};

// 打开编辑弹窗
const handleEdit = (book: AccountBook) => {
  if (selectMode.value) return;
  isEdit.value = true;
  formData.value = {
    id: book.id,
    userId: book.userId,
    name: book.name,
    image: book.image,
    isDefault: book.isDefault
  };
  showEditor.value = true;
};

// 提交表单
const handleSubmit = async () => {
  if (!formData.value.name.trim()) {
    showError(t("mobile.accountBook.namePlaceholder"));
    return;
  }

  saving.value = true;
  showLoading(t("mobile.common.loading"));

  try {
    if (isEdit.value) {
      await updateAccountBook(formData.value);
      showSuccess(t("mobile.accountBook.updateSuccess"));
    } else {
      await createAccountBook(formData.value);
      showSuccess(t("mobile.accountBook.createSuccess"));
    }
    hideLoading();
    showEditor.value = false;

    // 刷新列表
    await loadData();

    // 同步更新 store
    await accountBookStore.fetchList(userStore.id);
  } catch (error: any) {
    hideLoading();
    showError(error?.message || t("mobile.common.failed"));
  } finally {
    saving.value = false;
  }
};

// 删除单个账本
const handleDelete = async (book: AccountBook) => {
  if (book.isDefault === "YES") {
    showError(t("mobile.accountBook.cannotDeleteDefault"));
    return;
  }

  try {
    await showConfirmDialog({
      message: t("mobile.accountBook.deleteConfirm"),
      confirmButtonColor: "#d83d34"
    });

    showLoading(t("mobile.common.loading"));
    await deleteAccountBook([book.id]);
    hideLoading();

    showSuccess(t("mobile.accountBook.deleteSuccess"));

    // 刷新列表
    await loadData();

    // 同步更新 store
    await accountBookStore.fetchList(userStore.id);
  } catch {
    hideLoading();
  }
};

// 长按开始
const handleTouchStart = (book: AccountBook) => {
  if (selectMode.value) return;

  longPressTimer = setTimeout(() => {
    // 进入多选模式
    selectMode.value = true;
    selectedIds.value = [book.id];
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
const handleItemClick = (book: AccountBook) => {
  if (selectMode.value) {
    // 多选模式下切换选中状态
    const index = selectedIds.value.indexOf(book.id);
    if (index > -1) {
      selectedIds.value.splice(index, 1);
      // 如果没有选中项，退出多选模式
      if (selectedIds.value.length === 0) {
        selectMode.value = false;
      }
    } else {
      selectedIds.value.push(book.id);
    }
  } else {
    // 非多选模式下编辑
    handleEdit(book);
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

  // 检查是否包含默认账本
  const hasDefault = accountBookList.value.some(
    book => selectedIds.value.includes(book.id) && book.isDefault === "YES"
  );
  if (hasDefault) {
    showError(t("mobile.accountBook.cannotDeleteDefault"));
    return;
  }

  try {
    await showConfirmDialog({
      message: t("mobile.accountBook.deleteConfirm"),
      confirmButtonColor: "#d83d34"
    });

    showLoading(t("mobile.common.loading"));
    await deleteAccountBook(selectedIds.value);
    hideLoading();

    showSuccess(t("mobile.accountBook.deleteSuccess"));

    // 退出多选模式
    exitSelectMode();

    // 刷新列表
    await loadData();

    // 同步更新 store
    await accountBookStore.fetchList(userStore.id);
  } catch {
    hideLoading();
  }
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="account-book-page">
    <!-- 搜索框 -->
    <van-search
      v-model="searchKeyword"
      shape="round"
      :placeholder="t('mobile.accountBook.searchPlaceholder')"
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

    <!-- 账本网格 -->
    <div class="grid-container">
      <div
        v-for="book in filteredList"
        :key="book.id"
        class="grid-item"
        :class="{ selected: selectedIds.includes(book.id) }"
        @touchstart="handleTouchStart(book)"
        @touchend="handleTouchEnd"
        @touchcancel="handleTouchEnd"
        @click="handleItemClick(book)"
      >
        <!-- 选中标记 -->
        <div v-if="selectMode" class="select-checkbox">
          <van-icon v-if="selectedIds.includes(book.id)" name="success" />
        </div>
        <!-- 图标 -->
        <div class="item-icon">{{ getAccountBookIcon(book.image) }}</div>
        <!-- 名称 -->
        <div class="item-name">{{ book.name }}</div>
        <!-- 默认标签 -->
        <van-tag
          v-if="book.isDefault === 'YES'"
          type="success"
          size="small"
          class="default-tag"
        >
          {{ t("mobile.accountBook.default") }}
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
      :style="{ height: '60%' }"
    >
      <div class="account-book-editor">
        <div class="editor-header">
          <span class="title">
            {{
              isEdit
                ? t("mobile.accountBook.edit")
                : t("mobile.accountBook.add")
            }}
          </span>
          <van-icon name="cross" @click="showEditor = false" />
        </div>

        <!-- 账本名称 -->
        <van-field
          v-model="formData.name"
          :label="t('mobile.accountBook.name')"
          :placeholder="t('mobile.accountBook.namePlaceholder')"
          maxlength="20"
          show-word-limit
        />

        <!-- 账本图标 -->
        <div class="icon-selector">
          <div class="selector-label">{{ t("mobile.accountBook.icon") }}</div>
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

        <!-- 设为默认 -->
        <van-cell center :title="t('mobile.accountBook.setAsDefault')">
          <template #right-icon>
            <van-switch
              v-model="formData.isDefault"
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
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.account-book-page {
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
    border-color: $color-primary;
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
    background-color: $color-primary;
    border-radius: 50%;
  }

  .item-icon {
    font-size: 32px;
  }

  .item-name {
    max-width: 100%;
    overflow: hidden;
    font-size: 14px;
    font-weight: 500;
    color: $color-text-primary;
    text-align: center;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .default-tag {
    margin-top: 4px;
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

.account-book-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
  padding-bottom: env(safe-area-inset-bottom);
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
    grid-template-columns: repeat(5, 1fr);
    gap: 12px;
  }

  .icon-item {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 40px;
    font-size: 20px;
    cursor: pointer;
    background-color: #f5f5f5;
    border-radius: 8px;
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
</style>
