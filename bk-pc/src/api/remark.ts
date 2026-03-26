import { http } from "@/utils/http";
import type { UserRemarkForm, UserRemarkPageResult } from "@/types/remark";

export const getUserRemarkList = (
  userId: number,
  params?: {
    remark?: string;
    classifyId?: number;
    pageNo?: number;
    pageSize?: number;
  }
) => {
  return http.request<UserRemarkPageResult>("get", "/userRemark/page", {
    params: { userId, ...params }
  });
};

export const createUserRemark = (data: UserRemarkForm) => {
  return http.request<number>("post", "/userRemark", { data });
};

export const updateUserRemark = (data: UserRemarkForm) => {
  return http.request<void>("put", "/userRemark", { data });
};

export const deleteUserRemark = (ids: number[]) => {
  return http.request<void>("delete", "/userRemark", { data: ids });
};
