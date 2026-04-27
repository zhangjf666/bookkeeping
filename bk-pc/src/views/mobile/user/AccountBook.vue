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

// 删除账本
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
    // 取消或失败
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

    <!-- 新增按钮 -->
    <div class="add-btn-wrapper">
      <van-button type="danger" size="small" icon="plus" @click="handleAdd">
        {{ t("mobile.common.add") }}
      </van-button>
    </div>

    <!-- 账本列表 -->
    <van-cell-group inset>
      <van-swipe-cell v-for="book in filteredList" :key="book.id">
        <van-cell :title="book.name" is-link @click="handleEdit(book)">
          <template #icon>
            <span class="book-icon">{{ getAccountBookIcon(book.image) }}</span>
          </template>
          <template #value>
            <van-tag
              v-if="book.isDefault === 'YES'"
              type="success"
              size="small"
            >
              {{ t("mobile.accountBook.default") }}
            </van-tag>
          </template>
        </van-cell>

        <template #right>
          <van-button
            square
            type="danger"
            :text="t('mobile.common.delete')"
            @click="handleDelete(book)"
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
  background-color: $color-background;
}

.add-btn-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 0 16px 12px;
}

.book-icon {
  margin-right: 8px;
  font-size: 20px;
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
