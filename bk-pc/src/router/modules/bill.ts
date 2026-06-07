import { $t } from "@/plugins/i18n";

export default {
  path: "/bill",
  name: "Bill",
  component: () => import("@/components/DeviceAwareLayout.vue"),
  redirect: "/bill-list",
  meta: {
    icon: "ep:ticket",
    title: $t("menus.pureBill"),
    rank: 1
  },
  children: [
    {
      path: "/bill-list",
      name: "BillList",
      component: () => import("@/components/DeviceAwareView.vue"),
      meta: {
        title: $t("menus.pureBill"),
        pcComponent: () => import("@/views/bill/index.vue"),
        mobileComponent: () => import("@/views/mobile/bill/index.vue"),
        showTabBar: true,
        keepAlive: true
      }
    }
  ]
} satisfies RouteConfigsTable;
