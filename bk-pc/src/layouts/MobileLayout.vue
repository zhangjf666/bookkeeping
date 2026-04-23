<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";

defineOptions({
  name: "MobileLayout"
});

const route = useRoute();
const router = useRouter();

// 当前激活的 Tab
const activeTab = computed(() => {
  const path = route.path;
  if (path === "/" || path === "/dashboard") return "home";
  if (path === "/bill" || path === "/bill-list") return "bill";
  if (path.startsWith("/report")) return "report";
  if (path.startsWith("/user")) return "user";
  return "";
});

// 页面标题
const pageTitle = computed(() => {
  return (route.meta?.title as string) || "";
});

// 是否显示返回按钮
const showBack = computed(() => {
  const noBackPaths = ["/", "/dashboard", "/bill-list", "/report/bill-report", "/user/profile", "/login", "/register"];
  return !noBackPaths.includes(route.path) && !route.path.startsWith("/report/bill-report");
});

// 是否显示 TabBar
const showTabBar = computed(() => {
  return route.meta?.showTabBar !== false;
});

// 是否显示导航栏
const showNavBar = computed(() => {
  return !["/login", "/register"].includes(route.path);
});

// 返回上一页
const onBack = () => {
  router.back();
};

// TabBar 配置
const tabs = [
  { name: "home", path: "/dashboard", icon: "wap-home-o", text: "首页" },
  { name: "bill", path: "/bill-list", icon: "notes-o", text: "账单" },
  { name: "report", path: "/report/bill-report", icon: "bar-chart-o", text: "报表" },
  { name: "user", path: "/user/profile", icon: "user-o", text: "我的" }
];
</script>

<template>
  <div class="mobile-layout">
    <!-- 顶部导航栏 -->
    <van-nav-bar
      v-if="showNavBar"
      :title="pageTitle"
      :left-arrow="showBack"
      fixed
      placeholder
      @click-left="onBack"
    >
      <template #right>
        <slot name="nav-right" />
      </template>
    </van-nav-bar>

    <!-- 页面内容 -->
    <div class="mobile-content" :class="{ 'has-tabbar': showTabBar }">
      <router-view v-slot="{ Component }">
        <keep-alive>
          <component :is="Component" v-if="route.meta?.keepAlive" :key="route.path" />
        </keep-alive>
        <component :is="Component" v-if="!route.meta?.keepAlive" :key="route.path" />
      </router-view>
    </div>

    <!-- 底部 TabBar -->
    <van-tabbar v-if="showTabBar" v-model="activeTab" fixed placeholder route>
      <van-tabbar-item
        v-for="tab in tabs"
        :key="tab.name"
        :name="tab.name"
        :to="tab.path"
        :icon="tab.icon"
      >
        {{ tab.text }}
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style lang="scss" scoped>
.mobile-layout {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.mobile-content {
  padding-bottom: env(safe-area-inset-bottom);

  &.has-tabbar {
    padding-bottom: calc(50px + env(safe-area-inset-bottom));
  }
}

// Vant 导航栏红色主题
:deep(.van-nav-bar) {
  background: linear-gradient(135deg, #ee0a24 0%, #ff6b6b 100%);

  .van-nav-bar__title {
    font-weight: 600;
    color: #fff;
  }

  .van-nav-bar__arrow {
    color: #fff;
  }

  .van-nav-bar__text {
    color: #fff;
  }
}

// Vant TabBar 红色主题
:deep(.van-tabbar) {
  border-top: 1px solid #ebedf0;
  background-color: #fff;

  .van-tabbar-item--active {
    color: #ee0a24;
  }

  .van-tabbar-item__text {
    font-size: 12px;
  }
}
</style>