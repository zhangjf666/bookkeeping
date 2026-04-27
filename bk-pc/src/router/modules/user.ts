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
    },
    {
      path: "/user/settings/profile",
      name: "UserProfileSettings",
      component: () => import("@/components/DeviceAwareView.vue"),
      meta: {
        icon: "ep:user",
        title: $t("menus.pureUserProfile"),
        showLink: false,
        pcComponent: () => import("@/views/user/Profile.vue"),
        mobileComponent: () =>
          import("@/views/mobile/user/ProfileSettings.vue"),
        showTabBar: false
      }
    },
    {
      path: "/user/settings/password",
      name: "UserPasswordChange",
      component: () => import("@/components/DeviceAwareView.vue"),
      meta: {
        icon: "ep:lock",
        title: $t("menus.pureChangePassword"),
        showLink: false,
        pcComponent: () => import("@/views/user/ChangePasswordDialog.vue"),
        mobileComponent: () => import("@/views/mobile/user/PasswordChange.vue"),
        showTabBar: false
      }
    },
    {
      path: "/user/settings/about",
      name: "UserAbout",
      component: () => import("@/components/DeviceAwareView.vue"),
      meta: {
        icon: "ep:info-filled",
        title: $t("menus.pureAbout"),
        showLink: false,
        pcComponent: () => import("@/views/user/About.vue"),
        mobileComponent: () => import("@/views/mobile/user/About.vue"),
        showTabBar: false
      }
    }
  ]
} satisfies RouteConfigsTable;
