import { $t } from "@/plugins/i18n";
const { VITE_HIDE_HOME } = import.meta.env;

export default {
  path: "/",
  name: "Home",
  component: () => import("@/components/DeviceAwareLayout.vue"),
  redirect: "/dashboard",
  meta: {
    icon: "ep:home-filled",
    title: $t("menus.pureHome"),
    rank: 0
  },
  children: [
    {
      path: "/dashboard",
      name: "Dashboard",
      component: () => import("@/components/DeviceAwareView.vue"),
      meta: {
        title: $t("menus.pureHome"),
        showLink: VITE_HIDE_HOME === "true" ? false : true,
        pcComponent: () => import("@/views/dashboard/index.vue"),
        mobileComponent: () => import("@/views/mobile/home/index.vue"),
        showTabBar: true,
        keepAlive: true
      }
    },
    {
      path: "/record",
      name: "Record",
      component: () => import("@/components/DeviceAwareView.vue"),
      meta: {
        title: $t("bill.pureRecord"),
        showLink: false,
        pcComponent: () => import("@/views/bill/index.vue"),
        mobileComponent: () => import("@/views/mobile/record/index.vue"),
        showTabBar: false
      }
    },
    {
      path: "/record/:id",
      name: "RecordEdit",
      component: () => import("@/components/DeviceAwareView.vue"),
      meta: {
        title: $t("bill.pureEditRecord"),
        showLink: false,
        pcComponent: () => import("@/views/bill/index.vue"),
        mobileComponent: () => import("@/views/mobile/record/index.vue"),
        showTabBar: false
      }
    },
    {
      path: "/record-detail/:id",
      name: "RecordDetail",
      component: () => import("@/components/DeviceAwareView.vue"),
      meta: {
        title: $t("bill.pureRecordDetail"),
        showLink: false,
        pcComponent: () => import("@/views/bill/index.vue"),
        mobileComponent: () => import("@/views/mobile/record/detail.vue"),
        showTabBar: false
      }
    },
    {
      path: "/search",
      name: "Search",
      component: () => import("@/components/DeviceAwareView.vue"),
      meta: {
        title: $t("bill.pureSearch"),
        showLink: false,
        pcComponent: () => import("@/views/bill/index.vue"),
        mobileComponent: () => import("@/views/mobile/search/index.vue"),
        showTabBar: false
      }
    }
  ]
} satisfies RouteConfigsTable;
