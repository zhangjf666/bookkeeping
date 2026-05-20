export interface LoginParams {
  username: string;
  password: string;
  captcha: string;
  uuid: string;
  rememberMe?: boolean;
}

export interface RegisterParams {
  username: string;
  password: string;
  repeatPassword: string;
  captcha?: string;
  uuid?: string;
}

export interface CaptchaResult {
  img: string;
  uuid: string;
}

export interface UserInfo {
  id: number;
  username: string;
  nickName: string;
  avatar: string;
  email: string;
  phone: string;
  type: "0" | "1";
  enabled: "0" | "1";
}

export interface LoginResult {
  token: string;
  user: UserInfo;
}

export interface UserInfoResult {
  user: UserInfo;
  permission: string[];
}
