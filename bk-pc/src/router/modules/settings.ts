import { $t } from "@/plugins/i18n";
const Layout = () => import("@/layout/index.vue");

export default {
  path: "/settings",
  name: "Settings",
  component: Layout,
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
      component: () => import("@/views/settings/common/index.vue"),
      meta: {
        icon: "ep:setting",
        title: $t("menus.pureCommonSetting")
      }
    },
    {
      path: "/settings/account-book",
      name: "AccountBookSetting",
      component: () => import("@/views/settings/account-book/index.vue"),
      meta: {
        icon: "ep:wallet",
        title: $t("menus.pureAccountBookSetting")
      }
    },
    {
      path: "/settings/classify",
      name: "ClassifySetting",
      component: () => import("@/views/settings/classify/index.vue"),
      meta: {
        icon: "ep:folder",
        title: $t("menus.pureClassifySetting")
      }
    },
    {
      path: "/settings/tag",
      name: "TagSetting",
      component: () => import("@/views/settings/tag/index.vue"),
      meta: {
        icon: "ep:price-tag",
        title: $t("menus.pureTagSetting")
      }
    },
    {
      path: "/settings/remark",
      name: "RemarkSetting",
      component: () => import("@/views/settings/remark/index.vue"),
      meta: {
        icon: "ep:document",
        title: $t("menus.pureRemarkSetting")
      }
    }
  ]
} satisfies RouteConfigsTable;
