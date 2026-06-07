<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { showConfirmDialog } from "vant";
import { useUserStoreHook } from "@/store/modules/user";
import { useClassifyStore } from "@/store/modules/classify";
import {
  getClassifyList,
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

// 顶级分类列表
const mainClassifyList = computed(() => {
  return classifyList.value.filter(
    item =>
      (item.pid === -1 ||
        item.pid === null ||
        item.pid === undefined) &&
      item.type === currentType.value
  );
});

// 获取子分类列表
const getSubClassifyList = (parentId: number) => {
  return classifyList.value.filter(item => item.pid === parentId);
};

// 弹窗状态
const showEditor = ref(false);
const showParentPicker = ref(false);
const isEdit = ref(false);
const saving = ref(false);

// 是否为添加子分类（子分类时类型不可更改）
const isAddingSub = ref(false);

// 多选模式
const selectMode = ref(false);
const selectedIds = ref<number[]>([]);

// 长按计时器
let longPressTimer: ReturnType<typeof setTimeout> | null = null;

// 表单数据
const formData = ref<ClassifyForm>({
  userId: 0,
  name: "",
  pid: -1,
  image: "food",
  sort: 1,
  type: "EXPENSE",
  enable: "YES"
});

// 图标选项
const iconOptions = getClassifyIconOptions();

// 父分类选项（根据当前选择的类型过滤）
const parentOptions = computed(() => {
  const options = [{ id: -1, name: t("mobile.classify.none"), image: "other" }];
  const parents = classifyList.value
    .filter(
      item =>
        (item.pid === -1 ||
          item.pid === null ||
          item.pid === undefined) &&
        item.type === formData.value.type
    )
    .map(item => ({ id: item.id, name: item.name, image: item.image }));
  return [...options, ...parents];
});

// 父分类名称
const parentClassifyName = computed(() => {
  if (formData.value.pid === -1 || formData.value.pid === null) {
    return t("mobile.classify.none");
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

  showLoading(t("mobile.common.loading"));
  try {
    // 加载所有分类，不按类型过滤，以便在编辑器中切换类型时能正确显示父分类选项
    const result = await getClassifyList(userId);
    classifyList.value = result || [];
  } catch (error: any) {
    showError(error?.message || t("mobile.common.failed"));
  } finally {
    hideLoading();
  }
};

// 类型切换
const handleTypeChange = () => {
  exitSelectMode();
  loadData();
};

// 打开新增弹窗
const handleAdd = () => {
  isEdit.value = false;
  isAddingSub.value = false;
  formData.value = {
    userId: userStore.id,
    name: "",
    pid: -1,
    image: currentType.value === "EXPENSE" ? "food" : "salary",
    sort: 1,
    type: currentType.value,
    enable: "YES"
  };
  showEditor.value = true;
};

// 新增子分类
const handleAddSub = (parentId: number) => {
  const parent = classifyList.value.find(item => item.id === parentId);
  if (!parent) return;

  isEdit.value = false;
  isAddingSub.value = true; // 标记为添加子分类，类型不可更改
  formData.value = {
    userId: userStore.id,
    name: "",
    pid: parentId,
    image: parent.image,
    sort: 1,
    type: parent.type as ClassifyType,
    enable: "YES"
  };
  showEditor.value = true;
};

// 打开编辑弹窗
const handleEdit = (classify: Classify) => {
  if (selectMode.value) return;

  // 判断是否为子分类（有父分类）
  const isSubClassify =
    classify.pid !== 0 &&
    classify.pid !== -1 &&
    classify.pid !== null &&
    classify.pid !== undefined;

  isEdit.value = true;
  isAddingSub.value = false; // 编辑时类型可以更改
  formData.value = {
    id: classify.id,
    userId: classify.userId,
    name: classify.name,
    pid: classify.pid, // 保持原有pid，顶级分类应为-1
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

    await loadData();
    await classifyStore.fetchList(userStore.id);
  } catch (error: any) {
    hideLoading();
    showError(error?.message || t("mobile.common.failed"));
  } finally {
    saving.value = false;
  }
};

// 长按开始
const handleTouchStart = (classify: Classify) => {
  if (selectMode.value) return;

  longPressTimer = setTimeout(() => {
    selectMode.value = true;
    selectedIds.value = [classify.id];
  }, 500);
};

// 长按结束
const handleTouchEnd = () => {
  if (longPressTimer) {
    clearTimeout(longPressTimer);
    longPressTimer = null;
  }
};

// 点击分类项
const handleItemClick = (classify: Classify) => {
  if (selectMode.value) {
    const index = selectedIds.value.indexOf(classify.id);
    if (index > -1) {
      selectedIds.value.splice(index, 1);
      if (selectedIds.value.length === 0) {
        selectMode.value = false;
      }
    } else {
      selectedIds.value.push(classify.id);
    }
  } else {
    handleEdit(classify);
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
      message: t("mobile.classify.deleteConfirm"),
      confirmButtonColor: "#d83d34"
    });

    showLoading(t("mobile.common.loading"));
    await deleteClassify(selectedIds.value);
    hideLoading();

    showSuccess(t("mobile.classify.deleteSuccess"));
    exitSelectMode();
    await loadData();
    await classifyStore.fetchList(userStore.id);
  } catch {
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
    <div class="type-switch">
      <div
        class="type-btn expense"
        :class="{ active: currentType === 'EXPENSE' }"
        @click="
          currentType = 'EXPENSE';
          handleTypeChange();
        "
      >
        {{ t("mobile.record.expense") }}
      </div>
      <div
        class="type-btn income"
        :class="{ active: currentType === 'INCOME' }"
        @click="
          currentType = 'INCOME';
          handleTypeChange();
        "
      >
        {{ t("mobile.record.income") }}
      </div>
    </div>

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

    <!-- 分类列表 -->
    <div class="classify-list">
      <div
        v-for="mainClassify in mainClassifyList"
        :key="mainClassify.id"
        class="classify-group"
      >
        <!-- 父分类标题 -->
        <div
          class="group-header"
          :class="{
            selected: selectedIds.includes(mainClassify.id),
            disabled: mainClassify.enable === 'NO'
          }"
          @touchstart="handleTouchStart(mainClassify)"
          @touchend="handleTouchEnd"
          @touchcancel="handleTouchEnd"
          @click="handleItemClick(mainClassify)"
        >
          <!-- 选中标记 -->
          <div v-if="selectMode" class="select-checkbox">
            <van-icon
              v-if="selectedIds.includes(mainClassify.id)"
              name="success"
            />
          </div>
          <!-- 图标 -->
          <span class="header-icon">
            {{ getClassifyIcon(mainClassify.image) }}
          </span>
          <!-- 名称 -->
          <span class="header-name">{{ mainClassify.name }}</span>
          <!-- 添加子分类按钮 -->
          <van-icon
            v-if="!selectMode"
            name="plus"
            class="add-sub-btn"
            @click.stop="handleAddSub(mainClassify.id)"
          />
        </div>

        <!-- 子分类网格 -->
        <div
          v-if="getSubClassifyList(mainClassify.id).length > 0"
          class="sub-grid"
        >
          <div
            v-for="subClassify in getSubClassifyList(mainClassify.id)"
            :key="subClassify.id"
            class="sub-item"
            :class="{
              selected: selectedIds.includes(subClassify.id),
              disabled: subClassify.enable === 'NO'
            }"
            @touchstart="handleTouchStart(subClassify)"
            @touchend="handleTouchEnd"
            @touchcancel="handleTouchEnd"
            @click="handleItemClick(subClassify)"
          >
            <!-- 选中标记 -->
            <div v-if="selectMode" class="select-checkbox">
              <van-icon
                v-if="selectedIds.includes(subClassify.id)"
                name="success"
              />
            </div>
            <!-- 图标 -->
            <span class="sub-icon">
              {{ getClassifyIcon(subClassify.image) }}
            </span>
            <!-- 名称 -->
            <span class="sub-name">{{ subClassify.name }}</span>
          </div>
        </div>
      </div>
    </div>

    <van-empty
      v-if="mainClassifyList.length === 0"
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
      :style="{ height: '85%' }"
    >
      <div class="classify-editor">
        <div class="editor-header">
          <span class="title">
            {{ isEdit ? t("mobile.classify.edit") : t("mobile.classify.add") }}
          </span>
          <van-icon name="cross" @click="showEditor = false" />
        </div>

        <div class="editor-content">
          <!-- 类型（新增时显示，添加子分类时不可更改） -->
          <van-cell-group v-if="!isEdit" inset class="form-group">
            <van-cell :title="t('mobile.classify.type')">
              <template #value>
                <van-radio-group
                  v-model="formData.type"
                  direction="horizontal"
                  :disabled="isAddingSub"
                >
                  <van-radio name="EXPENSE" checked-color="#d83d34">{{
                    t("mobile.record.expense")
                  }}</van-radio>
                  <van-radio name="INCOME" checked-color="#d83d34">{{
                    t("mobile.record.income")
                  }}</van-radio>
                </van-radio-group>
              </template>
            </van-cell>
          </van-cell-group>

          <!-- 父分类 -->
          <van-cell-group inset class="form-group">
            <van-cell
              :title="t('mobile.classify.parentClassify')"
              :is-link="!isAddingSub"
              :class="{ 'cell-disabled': isAddingSub }"
              @click="!isAddingSub && (showParentPicker = true)"
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
          </van-cell-group>

          <!-- 分类图标 -->
          <div class="icon-section">
            <div class="section-title">{{ t("mobile.classify.icon") }}</div>
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

    <!-- 父分类选择弹窗 -->
    <van-popup
      v-model:show="showParentPicker"
      position="bottom"
      round
      :style="{ height: '55%' }"
    >
      <div class="parent-picker">
        <div class="picker-header">
          <span class="title">{{ t("mobile.classify.parentClassify") }}</span>
          <van-icon name="cross" @click="showParentPicker = false" />
        </div>
        <div class="parent-grid">
          <!-- 无选项 -->
          <div
            class="parent-item"
            :class="{ active: formData.pid === -1 || formData.pid === null }"
            @click="handleSelectParent(-1)"
          >
            <div class="item-icon">{{ getClassifyIcon("other") }}</div>
            <div class="item-name">{{ t("mobile.classify.none") }}</div>
            <van-icon
              v-if="formData.pid === -1 || formData.pid === null"
              name="success"
              class="check-icon"
            />
          </div>
          <!-- 父分类选项 -->
          <div
            v-for="option in parentOptions.slice(1)"
            :key="option.id"
            class="parent-item"
            :class="{ active: formData.pid === option.id }"
            @click="handleSelectParent(option.id)"
          >
            <div class="item-icon">{{ getClassifyIcon(option.image) }}</div>
            <div class="item-name">{{ option.name }}</div>
            <van-icon
              v-if="formData.pid === option.id"
              name="success"
              class="check-icon"
            />
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.classify-page {
  padding-bottom: calc(60px + env(safe-area-inset-bottom));
  background-color: $color-background;
}

.type-switch {
  position: sticky;
  top: 46px;
  z-index: 10;
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background-color: $color-card;

  .type-btn {
    flex: 1;
    padding: 10px 0;
    font-size: 14px;
    font-weight: 500;
    text-align: center;
    cursor: pointer;
    border: 1px solid $color-border;
    border-radius: 8px;
    transition: all 0.2s;

    &.expense {
      color: $color-text-secondary;

      &.active {
        color: #fff;
        background-color: $color-primary;
        border-color: $color-primary;
      }
    }

    &.income {
      color: $color-text-secondary;

      &.active {
        color: #fff;
        background-color: $color-secondary;
        border-color: $color-secondary;
      }
    }
  }
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

.classify-list {
  padding: 12px;
}

.classify-group {
  margin-bottom: 12px;
  overflow: hidden;
  background-color: $color-card;
  border-radius: 12px;
}

.group-header {
  position: relative;
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 14px 16px;
  cursor: pointer;
  transition: background-color 0.2s;

  &:active {
    background-color: rgba(0, 0, 0, 0.05);
  }

  &.selected {
    background-color: rgba($color-primary, 0.1);
  }

  &.disabled {
    opacity: 0.5;
  }

  .select-checkbox {
    position: absolute;
    top: 8px;
    right: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 18px;
    height: 18px;
    font-size: 12px;
    color: #fff;
    background-color: $color-primary;
    border-radius: 50%;
  }

  .header-icon {
    font-size: 24px;
  }

  .header-name {
    flex: 1;
    font-size: 15px;
    font-weight: 500;
    color: $color-text-primary;
  }

  .add-sub-btn {
    font-size: 18px;
    color: $color-primary;
  }
}

.sub-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  padding: 8px 12px 12px;
  background-color: rgba($color-primary, 0.02);
}

.sub-item {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
  justify-content: center;
  padding: 10px 4px;
  cursor: pointer;
  background-color: $color-card;
  border: 2px solid transparent;
  border-radius: 8px;
  transition: all 0.2s;

  &:active {
    transform: scale(0.95);
  }

  &.selected {
    background-color: rgba($color-primary, 0.1);
    border-color: $color-primary;
  }

  &.disabled {
    opacity: 0.5;
  }

  .select-checkbox {
    position: absolute;
    top: 4px;
    right: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 14px;
    height: 14px;
    font-size: 10px;
    color: #fff;
    background-color: $color-primary;
    border-radius: 50%;
  }

  .sub-icon {
    font-size: 22px;
  }

  .sub-name {
    max-width: 100%;
    overflow: hidden;
    font-size: 11px;
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

.classify-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding-bottom: env(safe-area-inset-bottom);
  overflow: hidden;
}

.editor-header {
  display: flex;
  flex: none;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid $color-border;

  .title {
    font-size: 16px;
    font-weight: 500;
    color: $color-text-primary;
  }
}

.editor-content {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
}

.form-group {
  margin-bottom: 12px;
}

.icon-section {
  padding: 12px;
  margin-top: 12px;
  background-color: $color-card;
  border-radius: 8px;

  .section-title {
    margin-bottom: 12px;
    font-size: 14px;
    font-weight: 500;
    color: $color-text-primary;
  }

  .icon-grid {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 10px;
  }

  .icon-item {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 44px;
    font-size: 22px;
    cursor: pointer;
    background-color: $color-background;
    border: 2px solid transparent;
    border-radius: 8px;
    transition: all 0.2s;

    &.active {
      color: #fff;
      background-color: $color-primary;
      border-color: $color-primary;
    }

    &:active {
      transform: scale(0.95);
    }
  }
}

.editor-footer {
  flex: none;
  padding: 12px 16px;
  border-top: 1px solid $color-border;
}

.cell-disabled {
  pointer-events: none;
  opacity: 0.6;
}

.parent-picker {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding-bottom: env(safe-area-inset-bottom);
  overflow: hidden;
}

.picker-header {
  display: flex;
  flex: none;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid $color-border;

  .title {
    font-size: 16px;
    font-weight: 500;
    color: $color-text-primary;
  }
}

.parent-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  align-content: start;
  padding: 12px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  overflow-y: auto;
}

.parent-item {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
  justify-content: center;
  padding: 10px 4px;
  cursor: pointer;
  background-color: $color-card;
  border: 2px solid transparent;
  border-radius: 8px;
  transition: all 0.2s;

  &:active {
    transform: scale(0.95);
  }

  &.active {
    background-color: rgba($color-primary, 0.1);
    border-color: $color-primary;
  }

  .item-icon {
    font-size: 22px;
  }

  .item-name {
    max-width: 100%;
    overflow: hidden;
    font-size: 11px;
    color: $color-text-primary;
    text-align: center;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .check-icon {
    position: absolute;
    top: 4px;
    right: 4px;
    font-size: 14px;
    color: $color-primary;
  }
}
</style>
