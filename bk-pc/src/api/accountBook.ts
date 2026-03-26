import { http } from "@/utils/http";
import type {
  AccountBookForm,
  AccountBookPageResult
} from "@/types/accountBook";

export const getAccountBookList = (
  userId: number,
  params?: { name?: string; pageNo?: number; pageSize?: number }
) => {
  return http.request<AccountBookPageResult>("get", "/accountBook/page", {
    params: { userId, ...params }
  });
};

export const createAccountBook = (data: AccountBookForm) => {
  return http.request<number>("post", "/accountBook", { data });
};

export const updateAccountBook = (data: AccountBookForm) => {
  return http.request<void>("put", "/accountBook", { data });
};

export const deleteAccountBook = (ids: number[]) => {
  return http.request<void>("delete", "/accountBook", { data: ids });
};
