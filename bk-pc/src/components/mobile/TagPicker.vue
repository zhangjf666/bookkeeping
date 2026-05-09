<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { UserTag } from "@/types/userTag";

defineOptions({
  name: "TagPicker"
});

const props = defineProps<{
  modelValue: boolean;
  tagList: UserTag[];
  selectedIds: number[];
}>();

const emit = defineEmits<{
  "update:modelValue": [value: boolean];
  "update:selectedIds": [ids: number[]];
}>();

const { t } = useI18n();

const show = computed({
  get: () => props.modelValue,
  set: val => emit("update:modelValue", val)
});

// 搜索关键词
const keyword = ref("");

// 本地选中的标签ID列表
const localSelectedIds = ref<number[]>([]);

// 筛选后的标签列表
const filteredList = computed(() => {
  if (!keyword.value.trim()) {
    return props.tagList;
  }
  return props.tagList.filter(item =>
    item.name.toLowerCase().includes(keyword.value.toLowerCase())
  );
});

// 切换标签选择
const toggleTag = (tag: UserTag) => {
  const index = localSelectedIds.value.indexOf(tag.id);
  if (index > -1) {
    localSelectedIds.value.splice(index, 1);
  } else {
    localSelectedIds.value.push(tag.id);
  }
};

// 检查是否选中
const isSelected = (tagId: number) => {
  return localSelectedIds.value.includes(tagId);
};

// 弹窗打开时初始化选中状态，关闭后重置搜索
watch(show, val => {
  if (val) {
    localSelectedIds.value = [...props.selectedIds];
  } else {
    // 关闭时同步数据到父组件
    emit("update:selectedIds", [...localSelectedIds.value]);
    keyword.value = "";
  }
});
</script>

<template>
  <van-popup
    v-model:show="show"
    position="bottom"
    round
    style="height: 50%"
  >
    <div class="tag-picker">
      <!-- 头部 -->
      <div class="picker-header">
        <span class="title">{{ t("mobile.record.tag") }}</span>
        <van-icon name="cross" @click="show = false" />
      </div>

      <!-- 搜索框 -->
      <div class="search-bar">
        <van-search
          v-model="keyword"
          :placeholder="t('mobile.record.tagSearch')"
          shape="round"
        />
      </div>

      <!-- 标签列表 -->
      <div class="tag-list">
        <div
          v-for="tag in filteredList"
          :key="tag.id"
          class="tag-item"
          :class="{ selected: isSelected(tag.id) }"
          :style="{
            '--tag-color': tag.color
          }"
          @click="toggleTag(tag)"
        >
          <span class="tag-name">{{ tag.name }}</span>
        </div>
      </div>

      <!-- 空状态 -->
      <van-empty v-if="filteredList.length === 0" description="暂无标签" />
    </div>
  </van-popup>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.tag-picker {
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

.search-bar {
  padding: 10px;
}

.tag-list {
  display: flex;
  flex: 1;
  flex-wrap: wrap;
  gap: 10px;
  align-content: flex-start;
  padding: 15px;
  overflow-y: auto;

  .tag-item {
    padding: 6px 12px;
    color: var(--tag-color);
    cursor: pointer;

    // 未选中状态
    background: transparent;
    border: 1px solid var(--tag-color);
    border-radius: 4px;
    transition: all 0.2s;

    &.selected {
      color: #fff;
      // 选中状态
      background: var(--tag-color);
    }

    .tag-name {
      font-size: 13px;
    }
  }
}
</style>
