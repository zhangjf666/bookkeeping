import { http } from "@/utils/http";
import type {
  LoginParams,
  RegisterParams,
  CaptchaResult,
  LoginResult,
  UserInfoResult
} from "@/types/auth";

export const getCaptcha = () => {
  return http.request<CaptchaResult>("get", "/auth/captcha");
};

export const login = (data: LoginParams) => {
  return http.request<LoginResult>("post", "/auth/login", { data });
};

export const register = (data: RegisterParams) => {
  return http.request<void>("post", "/auth/register", { data });
};

export const getUserInfo = () => {
  return http.request<UserInfoResult>("post", "/auth/user-info");
};

export const logout = () => {
  return http.request<void>("post", "/auth/logout");
};
