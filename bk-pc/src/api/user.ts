import { http } from "@/utils/http";

export interface BookkeepingUser {
  id: number;
  username: string;
  nickName: string;
  gender: "MALE" | "FEMALE" | "";
  avatar: string;
  email: string;
  mobilePhone: string;
  createTime: string;
  updateTime: string;
}

export interface ChangePasswordForm {
  oldPassword: string;
  newPassword: string;
  confirmNewPassword: string;
}

export interface AvatarUploadResult {
  url: string;
  fileName: string;
}

export type UserResult = {
  success: boolean;
  data: {
    /** 头像 */
    avatar: string;
    /** 用户名 */
    username: string;
    /** 昵称 */
    nickname: string;
    /** 当前登录用户的角色 */
    roles: Array<string>;
    /** 按钮级别权限 */
    permissions: Array<string>;
    /** `token` */
    accessToken: string;
    /** 用于调用刷新`accessToken`的接口时所需的`token` */
    refreshToken: string;
    /** `accessToken`的过期时间（格式'xxxx/xx/xx xx:xx:xx'） */
    expires: Date;
  };
};

export type RefreshTokenResult = {
  success: boolean;
  data: {
    /** `token` */
    accessToken: string;
    /** 用于调用刷新`accessToken`的接口时所需的`token` */
    refreshToken: string;
    /** `accessToken`的过期时间（格式'xxxx/xx/xx xx:xx:xx'） */
    expires: Date;
  };
};

/** 登录 */
export const getLogin = (data?: object) => {
  return http.request<UserResult>("post", "/login", { data });
};

/** 刷新`token` */
export const refreshTokenApi = (data?: object) => {
  return http.request<RefreshTokenResult>("post", "/refresh-token", { data });
};

/** 获取当前用户信息 */
export const getUserInfo = () => {
  return http.request<BookkeepingUser>("get", "/bookkeepingUser");
};

/** 更新用户信息 */
export const updateUserInfo = (data: Partial<BookkeepingUser>) => {
  return http.request<BookkeepingUser>("put", "/bookkeepingUser", { data });
};

/** 修改密码 */
export const changePassword = (data: ChangePasswordForm) => {
  return http.request<void>("post", "/bookkeepingUser/changePwd", { data });
};

/** 上传头像 */
export const uploadAvatar = (data: FormData) => {
  return http.request<AvatarUploadResult>("post", "/bookkeepingUser/avatar", {
    data,
    headers: { "Content-Type": "multipart/form-data" }
  });
};
