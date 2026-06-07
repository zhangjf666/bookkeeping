<script setup lang="ts">
import { ref, onMounted, shallowRef, watch, onActivated, onDeactivated, nextTick } from "vue";
import { useRoute } from "vue-router";
import { isMobile } from "@/utils/device";

defineOptions({
  name: "DeviceAwareView"
});

const route = useRoute();
const currentComponent = shallowRef<any>(null);
const loading = ref(true);
const lastLoadedPath = ref("");
let isDeactivated = false;

const loadComponent = async () => {
  const currentPath = route.path;

  // 如果当前组件处于 deactivation 状态，跳过加载（避免 keep-alive 缓存期间被 v-if 移除）
  if (isDeactivated) {
    return;
  }

  // 如果已经加载过相同路径的组件，跳过
  if (lastLoadedPath.value === currentPath && currentComponent.value) {
    return;
  }

  loading.value = true;
  lastLoadedPath.value = currentPath;
  const meta = route.meta as any;
  const mobileDevice = isMobile();

  if (meta?.pcComponent && meta?.mobileComponent) {
    const loader = mobileDevice ? meta.mobileComponent : meta.pcComponent;
    try {
      const module = await loader();
      currentComponent.value = module.default;
    } catch (error) {
      console.error("Failed to load component:", error);
    } finally {
      loading.value = false;
    }
  } else {
    loading.value = false;
  }
};

watch(
  () => route.path,
  () => {
    // 使用 nextTick 延迟执行，确保在 keep-alive deactivation 之后再判断
    nextTick(() => {
      if (!isDeactivated) {
        loadComponent();
      }
    });
  }
);

onMounted(() => {
  loadComponent();
});

onActivated(() => {
  isDeactivated = false;
});

onDeactivated(() => {
  isDeactivated = true;
});
</script>

<template>
  <component :is="currentComponent" v-if="!loading && currentComponent" />
</template>
