import { http } from "@/utils/http";
import type { UserTag, UserTagForm, UserTagPageResult } from "@/types/userTag";

// 获取用户标签列表（不分页）
export const getUserTagList = (userId: number) => {
  return http.request<UserTag[]>("get", "/userTag", {
    params: { userId }
  });
};

// 获取用户标签分页列表
export const getUserTagPage = (
  userId: number,
  params?: { name?: string; pageNo?: number; pageSize?: number }
) => {
  return http.request<UserTagPageResult>("get", "/userTag/page", {
    params: { userId, ...params }
  });
};

export const createUserTag = (data: UserTagForm) => {
  return http.request<UserTag>("post", "/userTag", { data });
};

export const updateUserTag = (data: UserTagForm) => {
  return http.request<void>("put", "/userTag", { data });
};

export const deleteUserTag = (ids: number[]) => {
  return http.request<void>("delete", "/userTag", { data: ids });
};
