import { $t } from "@/plugins/i18n";
const Layout = () => import("@/layout/index.vue");

export default {
  path: "/bill",
  name: "Bill",
  component: Layout,
  redirect: "/bill-list",
  meta: {
    icon: "ep:ticket",
    title: $t("bill.pureTitle"),
    rank: 1
  },
  children: [
    {
      path: "/bill-list",
      name: "BillList",
      component: () => import("@/views/bill/index.vue"),
      meta: {
        title: $t("bill.pureTitle")
      }
    }
  ]
} satisfies RouteConfigsTable;
