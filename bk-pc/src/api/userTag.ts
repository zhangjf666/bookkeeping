import { http } from "@/utils/http";
import type { UserTagForm, UserTagPageResult } from "@/types/userTag";

export const getUserTagList = (
  userId: number,
  params?: { name?: string; pageNo?: number; pageSize?: number }
) => {
  return http.request<UserTagPageResult>("get", "/userTag/page", {
    params: { userId, ...params }
  });
};

export const createUserTag = (data: UserTagForm) => {
  return http.request<number>("post", "/userTag", { data });
};

export const updateUserTag = (data: UserTagForm) => {
  return http.request<void>("put", "/userTag", { data });
};

export const deleteUserTag = (ids: number[]) => {
  return http.request<void>("delete", "/userTag", { data: ids });
};
