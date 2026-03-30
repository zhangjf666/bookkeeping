import { http } from "@/utils/http";
import type { Summary, SummaryParams } from "@/types/bill";

export const getSummary = (params: SummaryParams) => {
  return http.request<Summary>("get", "/incomeExpense/summary", { params });
};
