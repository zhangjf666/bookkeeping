<script setup lang="ts">
import { ref } from "vue";
import { useI18n } from "vue-i18n";

defineOptions({
  name: "MobileAbout"
});

const { t } = useI18n();

// 应用信息
const appInfo = ref({
  name: "记账本",
  version: "1.0.2",
  author: "Bookkeeping Team"
});
</script>

<template>
  <div class="about-page">
    <!-- 应用信息 -->
    <div class="app-info">
      <div class="app-icon">📒</div>
      <div class="app-name">{{ appInfo.name }}</div>
      <div class="app-version">v{{ appInfo.version }}</div>
    </div>

    <!-- 信息列表 -->
    <van-cell-group inset class="info-group">
      <van-cell :title="t('mobile.user.about') + ' ' + appInfo.name" />
      <van-cell title="版本" :value="'v' + appInfo.version" />
      <van-cell title="作者" :value="appInfo.author" />
    </van-cell-group>

    <!-- 版权信息 -->
    <div class="copyright">
      <p>© 2024 {{ appInfo.name }}</p>
      <p>All rights reserved.</p>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.about-page {
  min-height: 100vh;
  background-color: $color-background;
}

.app-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  padding: 40px 16px;
  background-color: $color-card;
}

.app-icon {
  font-size: 64px;
}

.app-name {
  font-size: 20px;
  font-weight: 600;
  color: $color-text-primary;
}

.app-version {
  font-size: 14px;
  color: $color-text-secondary;
}

.info-group {
  margin: 12px 0;
}

.copyright {
  margin-top: 40px;
  text-align: center;

  p {
    margin: 4px 0;
    font-size: 12px;
    color: $color-text-secondary;
  }
}
</style>
