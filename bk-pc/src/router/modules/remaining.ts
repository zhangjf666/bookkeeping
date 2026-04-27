import { $t } from "@/plugins/i18n";
const Layout = () => import("@/layout/index.vue");

export default [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/components/DeviceAwareView.vue"),
    meta: {
      title: $t("menus.pureLogin"),
      showLink: false,
      pcComponent: () => import("@/views/login/index.vue"),
      mobileComponent: () => import("@/views/mobile/login/index.vue")
    }
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("@/components/DeviceAwareView.vue"),
    meta: {
      title: $t("menus.pureRegister"),
      showLink: false,
      pcComponent: () => import("@/views/register/index.vue"),
      mobileComponent: () => import("@/views/mobile/register/index.vue")
    }
  },
  // 全屏403（无权访问）页面
  {
    path: "/access-denied",
    name: "AccessDenied",
    component: () => import("@/views/error/403.vue"),
    meta: {
      title: $t("menus.pureAccessDenied"),
      showLink: false
    }
  },
  // 全屏500（服务器出错）页面
  {
    path: "/server-error",
    name: "ServerError",
    component: () => import("@/views/error/500.vue"),
    meta: {
      title: $t("menus.pureServerError"),
      showLink: false
    }
  },
  {
    path: "/redirect",
    component: Layout,
    meta: {
      title: $t("status.pureLoad"),
      showLink: false
    },
    children: [
      {
        path: "/redirect/:path(.*)",
        name: "Redirect",
        component: () => import("@/layout/redirect.vue")
      }
    ]
  },
  // 404 页面 - 必须放在最后
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("@/views/error/404.vue"),
    meta: {
      title: $t("menus.purePageNotFound"),
      showLink: false
    }
  }
] satisfies Array<RouteConfigsTable>;
