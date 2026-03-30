import { $t } from "@/plugins/i18n";
const Layout = () => import("@/layout/index.vue");

export default {
  path: "/report",
  name: "Report",
  component: Layout,
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
      component: () => import("@/views/report/bill-report/index.vue"),
      meta: {
        title: $t("menus.pureBillReport")
      }
    },
    {
      path: "/report/classify-report",
      name: "ClassifyReport",
      component: () => import("@/views/report/classify-report/index.vue"),
      meta: {
        title: $t("menus.pureClassifyReport")
      }
    }
  ]
} satisfies RouteConfigsTable;
