<script setup lang="ts">
defineOptions({
  name: "UserAbout"
});

const appInfo = {
  name: "记账本",
  version: "1.0.2",
  author: "Bookkeeping Team"
};
</script>

<template>
  <div class="about-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>关于</span>
        </div>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="应用名称">{{
          appInfo.name
        }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{
          appInfo.version
        }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{
          appInfo.author
        }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.about-container {
  padding: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: 500;
}
</style>
