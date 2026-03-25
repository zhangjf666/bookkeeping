<script setup lang="ts">
import { message } from "@/utils/message";
import { type CSSProperties, ref, computed } from "vue";

defineOptions({
  name: "PermissionPage"
});

const elStyle = computed((): CSSProperties => {
  return {
    width: "85vw",
    justifyContent: "start"
  };
});

const username = ref("admin");

const options = [
  {
    value: "admin",
    label: "管理员角色"
  },
  {
    value: "common",
    label: "普通角色"
  }
];

function onChange() {
  message("请先完成登录功能开发", { type: "warning" });
}
</script>

<template>
  <div>
    <p class="mb-2!">
      模拟后台根据不同角色返回对应路由，观察左侧菜单变化（管理员角色可查看系统管理菜单、普通角色不可查看系统管理菜单）
    </p>
    <el-card shadow="never" :style="elStyle">
      <template #header>
        <div class="card-header">
          <span>当前角色：{{ username }}</span>
        </div>
      </template>
      <el-select v-model="username" class="w-[160px]!" @change="onChange">
        <el-option
          v-for="item in options"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-card>
  </div>
</template>
