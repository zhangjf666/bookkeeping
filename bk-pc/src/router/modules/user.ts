import { $t } from "@/plugins/i18n";
const Layout = () => import("@/layout/index.vue");

export default {
  path: "/user",
  name: "User",
  component: Layout,
  redirect: "/user/profile",
  meta: {
    icon: "ep:user",
    title: $t("menus.pureUser"),
    rank: 10
  },
  children: [
    {
      path: "/user/profile",
      name: "Profile",
      component: () => import("@/views/user/Profile.vue"),
      meta: {
        icon: "ep:user",
        title: $t("menus.pureUserProfile"),
        showLink: false
      }
    }
  ]
} satisfies RouteConfigsTable;
