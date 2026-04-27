<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";

defineOptions({
  name: "MobileLayout"
});

const route = useRoute();
const router = useRouter();
const { t } = useI18n();

// 当前激活的 Tab
const activeTab = computed(() => {
  const path = route.path;
  if (path === "/dashboard" || path === "/") return "home";
  if (path === "/bill-list") return "bill";
  if (path.startsWith("/report")) return "report";
  if (path.startsWith("/user")) return "user";
  return "";
});

// 页面标题（使用移动端 i18n 命名空间）
const pageTitle = computed(() => {
  const titleKey = route.meta?.title as string;
  if (!titleKey) return "";

  // 移动端使用 mobile 命名空间翻译
  // 根据 route.name 确定对应的 mobile i18n key
  const routeName = route.name as string;

  // 映射路由名称到 mobile i18n key
  const titleMap: Record<string, string> = {
    Record: "mobile.record.addTitle",
    RecordEdit: "mobile.record.editTitle",
    Search: "mobile.search.title",
    ReportDetail: "mobile.report.title",
    UserProfileSettings: "mobile.user.profile",
    UserPasswordChange: "mobile.user.changePassword",
    UserAbout: "mobile.user.about",
    CommonConfigSetting: "mobile.user.commonSettings",
    AccountBookSetting: "mobile.user.accountBook",
    ClassifySetting: "mobile.user.classifyManage",
    TagSetting: "mobile.user.tagManage",
    RemarkSetting: "mobile.user.remarkManage"
  };

  const mobileKey = titleMap[routeName];
  if (mobileKey) {
    return t(mobileKey);
  }

  // 默认返回原始标题
  return titleKey;
});

// 是否显示返回按钮
const showBack = computed(() => {
  const noBackPaths = [
    "/dashboard",
    "/bill-list",
    "/report/bill-report",
    "/user/profile",
    "/login",
    "/register"
  ];
  return !noBackPaths.includes(route.path);
});

// 是否显示 TabBar
const showTabBar = computed(() => {
  return route.meta?.showTabBar !== false;
});

// 是否显示导航栏（有 TabBar 的页面不显示导航栏）
const showNavBar = computed(() => {
  // 登录、注册页面不显示导航栏
  if (["/login", "/register"].includes(route.path)) {
    return false;
  }
  // 有 TabBar 的页面不显示导航栏
  if (route.meta?.showTabBar === true) {
    return false;
  }
  return true;
});

// 返回上一页
const onBack = () => {
  router.back();
};

// TabBar 配置（使用与PC端一致的路径）
const tabs = [
  { name: "home", path: "/dashboard", icon: "wap-home-o", text: "首页" },
  { name: "bill", path: "/bill-list", icon: "notes-o", text: "账单" },
  {
    name: "report",
    path: "/report/bill-report",
    icon: "bar-chart-o",
    text: "报表"
  },
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
          <component
            :is="Component"
            v-if="route.meta?.keepAlive"
            :key="route.path"
          />
        </keep-alive>
        <component
          :is="Component"
          v-if="!route.meta?.keepAlive"
          :key="route.path"
        />
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
  background-color: #fff;
  border-top: 1px solid #ebedf0;

  .van-tabbar-item--active {
    color: #ee0a24;
  }

  .van-tabbar-item__text {
    font-size: 12px;
  }
}
</style>
