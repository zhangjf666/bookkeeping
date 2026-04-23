<script setup lang="ts">
import { ref, computed, watch } from "vue";
import type { Classify } from "@/types/classify";

defineOptions({
  name: "ClassifyPicker"
});

const props = defineProps<{
  modelValue: boolean;
  type: "EXPENSE" | "INCOME";
  classifyList: Classify[];
  selectedId?: number;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: boolean];
  select: [classify: Classify];
}>();

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val)
});

// 搜索关键词
const keyword = ref("");

// 筛选后的分类列表
const filteredList = computed(() => {
  if (!keyword.value.trim()) {
    return props.classifyList;
  }
  return props.classifyList.filter((item) =>
    item.name.toLowerCase().includes(keyword.value.toLowerCase())
  );
});

// 选择分类
const handleSelect = (classify: Classify) => {
  emit("select", classify);
  show.value = false;
};

// 关闭弹窗
const handleClose = () => {
  show.value = false;
};

// 重置搜索
watch(show, (val) => {
  if (!val) {
    keyword.value = "";
  }
});
</script>

<template>
  <van-popup
    v-model:show="show"
    position="bottom"
    round
    style="height: 60%"
  >
    <div class="classify-picker">
      <!-- 头部 -->
      <div class="picker-header">
        <span class="title">选择分类</span>
        <van-icon name="cross" @click="handleClose" />
      </div>

      <!-- 搜索框 -->
      <div class="search-bar">
        <van-search
          v-model="keyword"
          placeholder="搜索分类"
          shape="round"
        />
      </div>

      <!-- 分类网格 -->
      <div class="classify-grid">
        <div
          v-for="item in filteredList"
          :key="item.id"
          class="classify-item"
          :class="{ selected: item.id === selectedId }"
          @click="handleSelect(item)"
        >
          <span class="icon">{{ item.image || "📝" }}</span>
          <span class="name">{{ item.name }}</span>
        </div>
      </div>

      <!-- 空状态 -->
      <van-empty
        v-if="filteredList.length === 0"
        description="暂无分类"
      />
    </div>
  </van-popup>
</template>

<style lang="scss" scoped>
.classify-picker {
  height: 100%;
  display: flex;
  flex-direction: column;

  .picker-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    border-bottom: 1px solid #eee;

    .title {
      font-size: 16px;
      font-weight: 500;
    }
  }

  .search-bar {
    padding: 10px;
  }

  .classify-grid {
    flex: 1;
    overflow-y: auto;
    padding: 15px;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 15px;

    .classify-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 5px;
      cursor: pointer;

      &.selected {
        .icon {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
      }

      .icon {
        width: 50px;
        height: 50px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f5f5f5;
        border-radius: 10px;
        font-size: 24px;
        transition: background 0.3s;
      }

      .name {
        font-size: 12px;
        color: #333;
        text-align: center;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        max-width: 60px;
      }
    }
  }
}
</style>