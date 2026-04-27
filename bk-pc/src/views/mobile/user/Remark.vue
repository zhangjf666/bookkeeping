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
import { getClassifyIcon } from "@/utils/classifyIcons";

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

// 表单数据
const formData = ref<UserRemarkForm>({
  userId: 0,
  remark: "",
  classifyId: 0
});

// 选中的分类
const selectedClassify = ref<Classify | null>(null);

// 选中的分类名称
const selectedClassifyName = computed(() => {
  if (!formData.value.classifyId) return "";
  const classify = classifyList.value.find(
    item => item.id === formData.value.classifyId
  );
  return classify?.name || "";
});

// 获取分类名称
const getClassifyName = (classifyId: number): string => {
  const classify = classifyList.value.find(item => item.id === classifyId);
  return classify?.name || "";
};

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
  selectedClassify.value = null;
  showEditor.value = true;
};

// 打开编辑弹窗
const handleEdit = (remark: UserRemark) => {
  isEdit.value = true;
  formData.value = {
    id: remark.id,
    userId: remark.userId,
    remark: remark.remark,
    classifyId: remark.classifyId
  };
  selectedClassify.value =
    classifyList.value.find(item => item.id === remark.classifyId) || null;
  showEditor.value = true;
};

// 选择分类
const handleSelectClassify = (classify: Classify) => {
  formData.value.classifyId = classify.id;
  selectedClassify.value = classify;
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

// 删除备注
const handleDelete = async (remark: UserRemark) => {
  try {
    await showConfirmDialog({
      message: t("mobile.remark.deleteConfirm"),
      confirmButtonColor: "#d83d34"
    });

    showLoading(t("mobile.common.loading"));
    await deleteUserRemark([remark.id]);
    hideLoading();

    showSuccess(t("mobile.remark.deleteSuccess"));

    // 刷新列表
    await loadData();

    // 同步更新 store
    await remarkStore.fetchList(userStore.id);
  } catch {
    // 取消或失败
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

    <!-- 新增按钮 -->
    <div class="add-btn-wrapper">
      <van-button type="danger" size="small" icon="plus" @click="handleAdd">
        {{ t("mobile.common.add") }}
      </van-button>
    </div>

    <!-- 备注列表 -->
    <van-cell-group inset>
      <van-swipe-cell v-for="remark in filteredList" :key="remark.id">
        <van-cell :title="remark.remark" is-link @click="handleEdit(remark)">
          <template #value>
            <span class="classify-name">{{
              getClassifyName(remark.classifyId)
            }}</span>
          </template>
        </van-cell>

        <template #right>
          <van-button
            square
            type="danger"
            :text="t('mobile.common.delete')"
            @click="handleDelete(remark)"
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
      :style="{ height: '50%' }"
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

    <!-- 分类选择弹窗 -->
    <van-action-sheet
      v-model:show="showClassifyPicker"
      :title="t('mobile.remark.classify')"
    >
      <div class="classify-list">
        <van-cell
          v-for="classify in classifyList"
          :key="classify.id"
          :title="classify.name"
          clickable
          @click="handleSelectClassify(classify)"
        >
          <template #icon>
            <span class="classify-icon">{{
              getClassifyIcon(classify.image)
            }}</span>
          </template>
          <template #right-icon>
            <van-icon
              v-if="formData.classifyId === classify.id"
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

.remark-page {
  min-height: 100vh;
  background-color: $color-background;
}

.add-btn-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 0 16px 12px;
}

.classify-name {
  font-size: 12px;
  color: $color-text-secondary;
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

.classify-list {
  max-height: 300px;
  padding-bottom: env(safe-area-inset-bottom);
  overflow-y: auto;
}

.classify-icon {
  margin-right: 8px;
  font-size: 20px;
}
</style>
