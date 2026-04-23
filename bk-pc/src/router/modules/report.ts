import { $t } from "@/plugins/i18n";

export default {
  path: "/report",
  name: "Report",
  component: () => import("@/components/DeviceAwareLayout.vue"),
  redirect: "/report/bill-report",
  meta: {
    icon: "ep:data-line",
    title: $t("menus.pureReport"),
    rank: 2
  },
  children: [
    {
      path: "/report/bill-report",
      name: "BillReport",
      component: () => import("@/components/DeviceAwareView.vue"),
      meta: {
        title: $t("menus.pureBillReport"),
        pcComponent: () => import("@/views/report/bill-report/index.vue"),
        mobileComponent: () => import("@/views/mobile/report/index.vue"),
        showTabBar: true
      }
    },
    {
      path: "/report/classify-report",
      name: "ClassifyReport",
      component: () => import("@/components/DeviceAwareView.vue"),
      meta: {
        title: $t("menus.pureClassifyReport"),
        pcComponent: () => import("@/views/report/classify-report/index.vue"),
        mobileComponent: () => import("@/views/mobile/report/detail.vue"),
        showTabBar: false
      }
    }
  ]
} satisfies RouteConfigsTable;
