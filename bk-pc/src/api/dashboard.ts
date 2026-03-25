import { http } from "@/utils/http";
import type {
  Summary,
  TrendData,
  AccountBook,
  SummaryParams,
  TrendParams
} from "@/types/dashboard";

export const getSummary = (params: SummaryParams) => {
  return http.request<Summary>("get", "/incomeExpense/summary", { params });
};

export const getTrendData = (params: TrendParams) => {
  return http.request<TrendData>("get", "/incomeExpense/sumPeriod", { params });
};

export const getAccountBooks = () => {
  return http.request<AccountBook[]>("get", "/accountBook");
};
