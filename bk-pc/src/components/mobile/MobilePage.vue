<script setup lang="ts">
import { computed } from "vue";

defineOptions({
  name: "MobilePage"
});

const props = defineProps<{
  title?: string;
  loading?: boolean;
  empty?: boolean;
  emptyText?: string;
}>();

// 是否显示加载状态
const showLoading = computed(() => props.loading);

// 是否显示空状态
const showEmpty = computed(() => props.empty && !props.loading);
</script>

<template>
  <div class="mobile-page">
    <!-- 加载状态 -->
    <van-loading v-if="showLoading" class="page-loading" />

    <!-- 空状态 -->
    <van-empty
      v-else-if="showEmpty"
      :description="emptyText || '暂无数据'"
      class="page-empty"
    />

    <!-- 页面内容 -->
    <template v-else>
      <slot />
    </template>
  </div>
</template>

<style lang="scss" scoped>
.mobile-page {
  min-height: 100vh;
  background-color: var(--mobile-bg-page, #f5f5f5);
  padding-top: var(--mobile-safe-area-top, 0);
  padding-bottom: var(--mobile-safe-area-bottom, 0);
}

.page-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
}

.page-empty {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
}
</style>