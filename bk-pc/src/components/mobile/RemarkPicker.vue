<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { UserRemark } from "@/types/remark";
import type { Classify } from "@/types/classify";

defineOptions({
  name: "RemarkPicker"
});

const props = defineProps<{
  modelValue: boolean;
  remarkList: UserRemark[];
  classifyList: Classify[];
  currentRemark?: string;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: boolean];
  select: [remark: UserRemark, mainClassify?: Classify, subClassify?: Classify];
}>();

const { t } = useI18n();

const show = computed({
  get: () => props.modelValue,
  set: val => emit("update:modelValue", val)
});

// 自定义备注输入
const customRemark = ref("");

// 选择常用备注
const handleSelectRemark = (remark: UserRemark) => {
  if (!remark.classifyId) {
    emit("select", remark);
    show.value = false;
    return;
  }

  // 先查找是否是主分类（pid 为 0、-1、null 或 undefined）
  const mainClassify = props.classifyList.find(
    item =>
      item.id === remark.classifyId &&
      (item.pid === 0 ||
        item.pid === -1 ||
        item.pid === null ||
        item.pid === undefined)
  );

  if (mainClassify) {
    emit("select", remark, mainClassify);
  } else {
    // 查找是否是子分类，通过 pid 找到父分类
    const subClassify = props.classifyList.find(
      item => item.id === remark.classifyId
    );
    if (subClassify && subClassify.pid) {
      // 找到父分类
      const parentClassify = props.classifyList.find(
        item => item.id === subClassify.pid
      );
      if (parentClassify) {
        emit("select", remark, parentClassify, subClassify);
        show.value = false;
        return;
      }
    }
    emit("select", remark);
  }
  show.value = false;
};

// 关闭弹窗时保存自定义备注
const handleClose = () => {
  // 如果有自定义备注内容，自动填入
  if (customRemark.value.trim()) {
    emit("select", {
      id: 0,
      userId: 0,
      remark: customRemark.value.trim(),
      classifyId: 0,
      createTime: "",
      updateTime: ""
    } as UserRemark);
    customRemark.value = "";
  }
};

// 弹窗打开时显示当前备注，关闭后重置状态
watch(show, val => {
  if (val) {
    // 打开时，显示当前已填写的备注
    customRemark.value = props.currentRemark || "";
  } else {
    // 关闭后重置
    customRemark.value = "";
  }
});
</script>

<template>
  <van-popup
    v-model:show="show"
    position="bottom"
    round
    style="height: 50%"
    @close="handleClose"
  >
    <div class="remark-picker">
      <!-- 头部 -->
      <div class="picker-header">
        <span class="title">{{ t("mobile.record.remark") }}</span>
        <van-icon name="cross" @click="show = false" />
      </div>

      <!-- 自定义备注输入 -->
      <div class="custom-input">
        <van-field
          v-model="customRemark"
          :placeholder="t('mobile.record.remarkPlaceholder')"
          clearable
        />
      </div>

      <!-- 常用备注 -->
      <div v-if="remarkList.length > 0" class="remark-section">
        <div class="section-label">常用备注：</div>
        <div class="remark-list">
          <div
            v-for="remark in remarkList"
            :key="remark.id"
            class="remark-item"
            @click="handleSelectRemark(remark)"
          >
            {{ remark.remark }}
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <van-empty v-if="remarkList.length === 0" description="暂无常用备注" />
    </div>
  </van-popup>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.remark-picker {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding-bottom: env(safe-area-inset-bottom);
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  border-bottom: 1px solid $color-border;

  .title {
    font-size: 16px;
    font-weight: 500;
    color: $color-text-primary;
  }
}

.custom-input {
  padding: 10px 15px;
  border-bottom: 1px solid $color-border;
}

.remark-section {
  flex: 1;
  padding: 15px;
  overflow-y: auto;

  .section-label {
    margin-bottom: 10px;
    font-size: 13px;
    color: $color-text-secondary;
  }
}

.remark-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;

  .remark-item {
    padding: 8px 16px;
    font-size: 14px;
    color: $color-text-primary;
    cursor: pointer;
    background: #f5f5f5;
    border-radius: 4px;

    &:active {
      background: #eee;
    }
  }
}
</style>
