<script setup lang="ts">
import { ref, computed, watch } from "vue";

defineOptions({
  name: "PullRefreshList"
});

const props = defineProps<{
  loading?: boolean;
  finished?: boolean;
  hasData?: boolean;
}>();

const emit = defineEmits<{
  refresh: [];
  load: [];
}>();

// 内部加载状态
const refreshing = ref(false);
const loadingMore = ref(false);

// 监听外部 loading 状态
watch(
  () => props.loading,
  val => {
    if (!val) {
      refreshing.value = false;
      loadingMore.value = false;
    }
  }
);

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true;
  emit("refresh");
};

// 上拉加载
const onLoad = () => {
  if (props.finished || props.loading) return;
  loadingMore.value = true;
  emit("load");
};
</script>

<template>
  <div class="pull-refresh-list">
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <!-- 列表内容 -->
      <slot />

      <!-- 空状态 -->
      <van-empty v-if="!hasData && !loading" description="暂无数据" />

      <!-- 加载更多 -->
      <van-list
        v-if="hasData"
        :loading="loadingMore"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <slot name="item" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<style lang="scss" scoped>
.pull-refresh-list {
  min-height: calc(100vh - 100px);
}

:deep(.van-pull-refresh) {
  min-height: inherit;
}

:deep(.van-list) {
  background: var(--mobile-bg-card, #fff);
}
</style>
