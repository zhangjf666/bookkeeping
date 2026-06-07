import { http } from "@/utils/http";
import type { UserConfig, UserConfigForm } from "@/types/userConfig";

export const getUserConfigList = (userId: number, name?: string) => {
  return http.request<UserConfig[]>("get", "/userConfig", {
    params: {
      userId,
      ...(name && { name })
    }
  });
};

export const getUserConfig = (userId: number, name: string) => {
  return http
    .request<UserConfig[]>("get", "/userConfig", {
      params: { userId, name }
    })
    .then(result => {
      if (Array.isArray(result) && result.length > 0) {
        return result[0];
      }
      return null;
    });
};

export const updateUserConfig = (data: UserConfigForm) => {
  return http.request<void>("put", "/userConfig", { data });
};

export const setAdditionalExpenseLimit = (
  userId: number,
  type: "MONTHLY" | "YEARLY",
  expenseLimit: string
) => {
  return http.request<void>("post", "/userConfig/additionalExpenseLimit", {
    data: {
      userId,
      type,
      expenseLimit
    }
  });
};
