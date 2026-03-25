import { $t } from "@/plugins/i18n";
const Layout = () => import("@/layout/index.vue");

export default {
  path: "/settings",
  name: "Settings",
  component: Layout,
  redirect: "/settings/tag",
  meta: {
    icon: "ep:setting",
    title: $t("menus.pureSettings"),
    rank: 10
  },
  children: [
    {
      path: "/settings/tag",
      name: "TagSetting",
      component: () => import("@/views/settings/tag/index.vue"),
      meta: {
        icon: "ep:price-tag",
        title: $t("menus.pureTagSetting"),
        showParent: true
      }
    }
  ]
} satisfies RouteConfigsTable;
