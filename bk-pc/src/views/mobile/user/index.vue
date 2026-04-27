<script setup lang="ts">
defineOptions({
  name: "MobileUser"
});
</script>

<template>
  <div class="mobile-user">
    <div class="placeholder">
      <p>我的页开发中...</p>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.mobile-user {
  padding: 16px;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #969799;
  background-color: #f7f8fa;
  border-radius: 8px;
}
</style>
