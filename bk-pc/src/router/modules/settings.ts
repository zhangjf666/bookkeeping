import { $t } from "@/plugins/i18n";

export default {
  path: "/settings",
  name: "Settings",
  component: () => import("@/components/DeviceAwareLayout.vue"),
  redirect: "/settings/account-book",
  meta: {
    icon: "ep:setting",
    title: $t("menus.pureSettings"),
    rank: 10
  },
  children: [
    {
      path: "/settings/common",
      name: "CommonConfigSetting",
      component: () => import("@/components/DeviceAwareView.vue"),
      meta: {
        icon: "ep:setting",
        title: $t("menus.pureCommonSetting"),
        pcComponent: () => import("@/views/settings/common/index.vue"),
        mobileComponent: () => import("@/views/settings/common/index.vue"),
        showTabBar: false
      }
    },
    {
      path: "/settings/account-book",
      name: "AccountBookSetting",
      component: () => import("@/components/DeviceAwareView.vue"),
      meta: {
        icon: "ep:wallet",
        title: $t("menus.pureAccountBookSetting"),
        pcComponent: () => import("@/views/settings/account-book/index.vue"),
        mobileComponent: () => import("@/views/settings/account-book/index.vue"),
        showTabBar: false
      }
    },
    {
      path: "/settings/classify",
      name: "ClassifySetting",
      component: () => import("@/components/DeviceAwareView.vue"),
      meta: {
        icon: "ep:folder",
        title: $t("menus.pureClassifySetting"),
        pcComponent: () => import("@/views/settings/classify/index.vue"),
        mobileComponent: () => import("@/views/settings/classify/index.vue"),
        showTabBar: false
      }
    },
    {
      path: "/settings/tag",
      name: "TagSetting",
      component: () => import("@/components/DeviceAwareView.vue"),
      meta: {
        icon: "ep:price-tag",
        title: $t("menus.pureTagSetting"),
        pcComponent: () => import("@/views/settings/tag/index.vue"),
        mobileComponent: () => import("@/views/settings/tag/index.vue"),
        showTabBar: false
      }
    },
    {
      path: "/settings/remark",
      name: "RemarkSetting",
      component: () => import("@/components/DeviceAwareView.vue"),
      meta: {
        icon: "ep:document",
        title: $t("menus.pureRemarkSetting"),
        pcComponent: () => import("@/views/settings/remark/index.vue"),
        mobileComponent: () => import("@/views/settings/remark/index.vue"),
        showTabBar: false
      }
    }
  ]
} satisfies RouteConfigsTable;
