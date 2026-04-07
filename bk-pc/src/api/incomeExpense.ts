import { http } from "@/utils/http";
import type {
  IncomeExpense,
  IncomeExpenseQuery,
  IncomeExpenseForm,
  PageResult,
  Classify,
  TrendParams,
  TrendData,
  ClassifyReportData,
  Summary,
  SummaryParams
} from "@/types/bill";

export const getIncomeExpenseList = (
  userId: number,
  params: IncomeExpenseQuery
) => {
  return http.request<PageResult<IncomeExpense>>("get", "/incomeExpense/page", {
    params: { userId, ...params }
  });
};

export const createIncomeExpense = (
  userId: number,
  data: IncomeExpenseForm
) => {
  return http.request<void>("post", "/incomeExpense", {
    data: { userId, ...data }
  });
};

export const updateIncomeExpense = (
  userId: number,
  data: IncomeExpenseForm
) => {
  return http.request<void>("put", "/incomeExpense", {
    data: { userId, ...data }
  });
};

export const deleteIncomeExpense = (ids: number[]) => {
  return http.request<void>("delete", "/incomeExpense", { data: ids });
};

export const getClassifyList = (userId: number, type?: 0 | 1) => {
  return http.request<Classify[]>("get", "/classify", {
    params: { userId, ...(type !== undefined ? { type } : {}) }
  });
};

export const getTagList = (userId: number) => {
  return http.request<
    { id: number; code: number; name: string; color: string }[]
  >("get", "/userTag", { params: { userId } });
};

export const getRemarkList = (userId: number) => {
  return http.request<{ id: number; remark: string; classifyId: number }[]>(
    "get",
    "/userRemark",
    {
      params: { userId }
    }
  );
};

export const getTrendData = (params: TrendParams) => {
  return http.request<TrendData>("post", "/incomeExpense/sumPeriod", { data: params });
};

export const getClassifyReportData = (params: TrendParams) => {
  return http.request<ClassifyReportData>("post", "/incomeExpense/sumPeriod", {
    data: { ...params, queryMode: "1" }
  });
};

export const getSummary = (params: SummaryParams) => {
  return http.request<Summary>("get", "/incomeExpense/summary", { params });
};

export const exportData = (data: {
  userId: number;
  date?: string[];
  accountBookId?: number;
  type?: 0 | 1;
  amount?: number[];
  mainClassify?: number;
  subClassify?: number;
  remark?: string[];
  tagCodes?: string[];
  isCreditCard?: number;
}) => {
  return http.request<Blob>("post", "/incomeExpense/exportRecord", {
    data,
    responseType: "blob"
  });
};
