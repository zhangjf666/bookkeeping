import { http } from "@/utils/http";
import type {
  UserRemark,
  UserRemarkForm,
  UserRemarkPageResult
} from "@/types/remark";

// 获取用户备注列表（不分页）
export const getUserRemarkList = (userId: number) => {
  return http.request<UserRemark[]>("get", "/userRemark", {
    params: { userId }
  });
};

// 获取用户备注分页列表
export const getUserRemarkPage = (
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
