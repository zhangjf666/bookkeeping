<script setup lang="ts">
import { ref, onMounted, shallowRef, provide } from "vue";
import { isMobile } from "@/utils/device";

defineOptions({
  name: "DeviceAwareLayout"
});

const currentLayout = shallowRef<any>(null);
const loading = ref(true);
const isMobileDevice = ref(false);

const loadLayout = async () => {
  loading.value = true;
  isMobileDevice.value = isMobile();
  const loader = isMobileDevice.value
    ? () => import("@/mobile-layout/MobileLayout.vue")
    : () => import("@/layout/index.vue");
  try {
    const module = await loader();
    currentLayout.value = module.default;
  } catch (error) {
    console.error("Failed to load layout:", error);
  } finally {
    loading.value = false;
  }
};

provide("isMobileDevice", isMobileDevice);

onMounted(() => {
  loadLayout();
});
</script>

<template>
  <component :is="currentLayout" v-if="!loading && currentLayout">
    <router-view />
  </component>
</template>
