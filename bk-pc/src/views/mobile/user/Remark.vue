<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { showConfirmDialog } from "vant";
import { storeToRefs } from "pinia";
import { useUserStoreHook } from "@/store/modules/user";
import { useRemarkStore } from "@/store/modules/remark";
import { useClassifyStore } from "@/store/modules/classify";
import {
  getUserRemarkPage,
  createUserRemark,
  updateUserRemark,
  deleteUserRemark
} from "@/api/remark";
import type { UserRemark, UserRemarkForm } from "@/types/remark";
import type { Classify } from "@/types/classify";
import {
  showLoading,
  hideLoading,
  showSuccess,
  showError
} from "@/utils/mobile/message";
import ClassifyPicker from "@/components/mobile/ClassifyPicker.vue";

defineOptions({
  name: "MobileRemark"
});

const { t } = useI18n();
const userStore = useUserStoreHook();
const remarkStore = useRemarkStore();
const classifyStore = useClassifyStore();

// 分类列表
const { list: classifyList } = storeToRefs(classifyStore);

// 备注列表
const remarkList = ref<UserRemark[]>([]);

// 搜索关键词
const searchKeyword = ref("");

// 过滤后的列表
const filteredList = computed(() => {
  if (!searchKeyword.value.trim()) {
    return remarkList.value;
  }
  const keyword = searchKeyword.value.toLowerCase();
  return remarkList.value.filter(item =>
    item.remark.toLowerCase().includes(keyword)
  );
});

// 弹窗状态
const showEditor = ref(false);
const showClassifyPicker = ref(false);
const isEdit = ref(false);
const saving = ref(false);

// 多选模式
const selectMode = ref(false);
const selectedIds = ref<number[]>([]);

// 长按计时器
let longPressTimer: ReturnType<typeof setTimeout> | null = null;

// 表单数据
const formData = ref<UserRemarkForm>({
  userId: 0,
  remark: "",
  classifyId: 0
});

// 选中的顶级分类ID和子分类ID
const mainClassifyId = ref<number | null>(null);
const subClassifyId = ref<number | null>(null);

// 根据分类ID获取顶级分类和子分类
const getClassifyInfo = (classifyId: number) => {
  const classify = classifyList.value.find(item => item.id === classifyId);
  if (!classify) return { mainId: null, subId: null };

  // 如果是顶级分类
  if (!classify.pid || classify.pid === 0 || classify.pid === -1) {
    return { mainId: classify.id, subId: null };
  }
  // 如果是子分类
  return { mainId: classify.pid, subId: classify.id };
};

// 选中的分类名称（显示为"顶级分类-子分类"格式）
const selectedClassifyName = computed(() => {
  if (!mainClassifyId.value) return "";

  const mainClassify = classifyList.value.find(
    item => item.id === mainClassifyId.value
  );
  if (!mainClassify) return "";

  if (subClassifyId.value) {
    const subClassify = classifyList.value.find(
      item => item.id === subClassifyId.value
    );
    if (subClassify) {
      return `${mainClassify.name}-${subClassify.name}`;
    }
  }

  return mainClassify.name;
});

// 加载备注列表
const loadData = async () => {
  const userId = userStore.id;
  if (!userId) return;

  try {
    const result = await getUserRemarkPage(userId);
    remarkList.value = result?.list || result?.record || [];
  } catch (error: any) {
    showError(error?.message || t("mobile.common.failed"));
  }
};

// 打开新增弹窗
const handleAdd = () => {
  isEdit.value = false;
  formData.value = {
    userId: userStore.id,
    remark: "",
    classifyId: 0
  };
  mainClassifyId.value = null;
  subClassifyId.value = null;
  showEditor.value = true;
};

// 打开编辑弹窗
const handleEdit = (remark: UserRemark) => {
  if (selectMode.value) return;
  isEdit.value = true;
  formData.value = {
    id: remark.id,
    userId: remark.userId,
    remark: remark.remark,
    classifyId: remark.classifyId
  };
  // 根据分类ID获取顶级和子分类
  const { mainId, subId } = getClassifyInfo(remark.classifyId);
  mainClassifyId.value = mainId;
  subClassifyId.value = subId;
  showEditor.value = true;
};

// 选择分类
const handleClassifySelect = (
  selectedMainClassify: Classify,
  selectedSubClassify?: Classify
) => {
  mainClassifyId.value = selectedMainClassify.id;
  subClassifyId.value = selectedSubClassify?.id || null;
  // 设置实际的分类ID
  formData.value.classifyId =
    selectedSubClassify?.id || selectedMainClassify.id;
  showClassifyPicker.value = false;
};

// 提交表单
const handleSubmit = async () => {
  if (!formData.value.remark.trim()) {
    showError(t("mobile.remark.namePlaceholder"));
    return;
  }

  saving.value = true;
  showLoading(t("mobile.common.loading"));

  try {
    if (isEdit.value) {
      await updateUserRemark(formData.value);
      showSuccess(t("mobile.remark.updateSuccess"));
    } else {
      await createUserRemark(formData.value);
      showSuccess(t("mobile.remark.createSuccess"));
    }
    hideLoading();
    showEditor.value = false;

    // 刷新列表
    await loadData();

    // 同步更新 store
    await remarkStore.fetchList(userStore.id);
  } catch (error: any) {
    hideLoading();
    showError(error?.message || t("mobile.common.failed"));
  } finally {
    saving.value = false;
  }
};

// 长按开始
const handleTouchStart = (remark: UserRemark) => {
  if (selectMode.value) return;

  longPressTimer = setTimeout(() => {
    // 进入多选模式
    selectMode.value = true;
    selectedIds.value = [remark.id];
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
const handleItemClick = (remark: UserRemark) => {
  if (selectMode.value) {
    // 多选模式下切换选中状态
    const index = selectedIds.value.indexOf(remark.id);
    if (index > -1) {
      selectedIds.value.splice(index, 1);
      // 如果没有选中项，退出多选模式
      if (selectedIds.value.length === 0) {
        selectMode.value = false;
      }
    } else {
      selectedIds.value.push(remark.id);
    }
  } else {
    // 非多选模式下编辑
    handleEdit(remark);
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
      message: t("mobile.remark.deleteConfirm"),
      confirmButtonColor: "#d83d34"
    });

    showLoading(t("mobile.common.loading"));
    await deleteUserRemark(selectedIds.value);
    hideLoading();

    showSuccess(t("mobile.remark.deleteSuccess"));

    // 退出多选模式
    exitSelectMode();

    // 刷新列表
    await loadData();

    // 同步更新 store
    await remarkStore.fetchList(userStore.id);
  } catch {
    hideLoading();
  }
};

// 初始化
const init = async () => {
  showLoading(t("mobile.common.loading"));
  try {
    // 确保分类列表已加载
    if (classifyStore.list.length === 0) {
      await classifyStore.fetchList(userStore.id);
    }
    await loadData();
  } catch (error: any) {
    showError(error?.message || t("mobile.common.failed"));
  } finally {
    hideLoading();
  }
};

onMounted(() => {
  init();
});
</script>

<template>
  <div class="remark-page">
    <!-- 搜索框 -->
    <van-search
      v-model="searchKeyword"
      shape="round"
      :placeholder="t('mobile.remark.searchPlaceholder')"
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

    <!-- 备注网格 -->
    <div class="grid-container">
      <div
        v-for="remark in filteredList"
        :key="remark.id"
        class="grid-item"
        :class="{ selected: selectedIds.includes(remark.id) }"
        @touchstart="handleTouchStart(remark)"
        @touchend="handleTouchEnd"
        @touchcancel="handleTouchEnd"
        @click="handleItemClick(remark)"
      >
        <!-- 选中标记 -->
        <div v-if="selectMode" class="select-checkbox">
          <van-icon v-if="selectedIds.includes(remark.id)" name="success" />
        </div>
        <!-- 备注名称 -->
        <div class="item-name">{{ remark.remark }}</div>
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
      :style="{ height: '40%' }"
    >
      <div class="remark-editor">
        <div class="editor-header">
          <span class="title">
            {{ isEdit ? t("mobile.remark.edit") : t("mobile.remark.add") }}
          </span>
          <van-icon name="cross" @click="showEditor = false" />
        </div>

        <!-- 备注名称 -->
        <van-field
          v-model="formData.remark"
          :label="t('mobile.remark.name')"
          :placeholder="t('mobile.remark.namePlaceholder')"
          maxlength="20"
          show-word-limit
        />

        <!-- 关联分类 -->
        <van-cell
          :title="t('mobile.remark.classify')"
          is-link
          @click="showClassifyPicker = true"
        >
          <template #value>
            {{ selectedClassifyName || t("mobile.remark.selectClassify") }}
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

    <!-- 分类选择器 -->
    <ClassifyPicker
      v-model="showClassifyPicker"
      :classify-list="classifyList"
      :main-classify-id="mainClassifyId"
      :sub-classify-id="subClassifyId"
      @select="handleClassifySelect"
    />
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.remark-page {
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
  min-height: 60px;
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

.remark-editor {
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

.editor-footer {
  padding-top: 16px;
  margin-top: auto;
}
</style>
