import type { RouteRecordRaw } from "vue-router";
import MobileLayout from "@/mobile-layout/MobileLayout.vue";

/**
 * 移动端路由配置
 * 所有移动端页面都使用 MobileLayout 布局
 * 路由路径使用 /m 前缀
 */
export const mobileRoutes: Array<RouteRecordRaw> = [
  {
    path: "/m",
    component: MobileLayout,
    redirect: "/m/home",
    children: [
      {
        path: "home",
        name: "MobileHome",
        component: () => import("@/views/mobile/home/index.vue"),
        meta: {
          title: "首页",
          keepAlive: true,
          showTabBar: true
        }
      },
      {
        path: "bill",
        name: "MobileBill",
        component: () => import("@/views/mobile/bill/index.vue"),
        meta: {
          title: "账单",
          keepAlive: true,
          showTabBar: true
        }
      },
      {
        path: "record",
        name: "MobileRecord",
        component: () => import("@/views/mobile/record/index.vue"),
        meta: {
          title: "record.addTitle",
          showTabBar: false
        }
      },
      {
        path: "record/:id",
        name: "MobileRecordEdit",
        component: () => import("@/views/mobile/record/index.vue"),
        meta: {
          title: "record.editTitle",
          showTabBar: false
        }
      },
      {
        path: "report",
        name: "MobileReport",
        component: () => import("@/views/mobile/report/index.vue"),
        meta: {
          title: "报表",
          keepAlive: true,
          showTabBar: true
        }
      },
      {
        path: "report/detail",
        name: "MobileReportDetail",
        component: () => import("@/views/mobile/report/detail.vue"),
        meta: {
          title: "报表详情",
          showTabBar: false
        }
      },
      {
        path: "search",
        name: "MobileSearch",
        component: () => import("@/views/mobile/search/index.vue"),
        meta: {
          title: "搜索",
          showTabBar: false
        }
      },
      {
        path: "user",
        name: "MobileUser",
        component: () => import("@/views/mobile/user/index.vue"),
        meta: {
          title: "我的",
          keepAlive: true,
          showTabBar: true
        }
      }
    ]
  },
  {
    path: "/m/login",
    name: "MobileLogin",
    component: () => import("@/views/mobile/login/index.vue"),
    meta: {
      title: "登录",
      showTabBar: false
    }
  },
  {
    path: "/m/register",
    name: "MobileRegister",
    component: () => import("@/views/mobile/register/index.vue"),
    meta: {
      title: "注册",
      showTabBar: false
    }
  }
];

export default mobileRoutes;
