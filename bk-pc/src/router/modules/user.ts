import { $t } from "@/plugins/i18n";

export default {
  path: "/user",
  name: "User",
  component: () => import("@/components/DeviceAwareLayout.vue"),
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
      component: () => import("@/components/DeviceAwareView.vue"),
      meta: {
        icon: "ep:user",
        title: $t("menus.pureUserProfile"),
        showLink: false,
        pcComponent: () => import("@/views/user/Profile.vue"),
        mobileComponent: () => import("@/views/mobile/user/index.vue"),
        showTabBar: true
      }
    }
  ]
} satisfies RouteConfigsTable;
