<script setup lang="ts">
import { ref, onMounted, shallowRef, watch } from "vue";
import { useRoute } from "vue-router";
import { isMobile } from "@/utils/device";

defineOptions({
  name: "DeviceAwareView"
});

const route = useRoute();
const currentComponent = shallowRef<any>(null);
const loading = ref(true);

const loadComponent = async () => {
  loading.value = true;
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
    loadComponent();
  }
);

onMounted(() => {
  loadComponent();
});
</script>

<template>
  <component :is="currentComponent" v-if="!loading && currentComponent" />
</template>
