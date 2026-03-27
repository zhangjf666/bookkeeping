export interface UserConfig {
  id: number;
  userId: number;
  name: string;
  value: string;
  description: string;
  enable: "YES" | "NO";
}

export interface UserConfigForm {
  id: number;
  userId: number;
  name: string;
  value: string;
  description?: string;
  enable: "YES" | "NO";
}

export interface UserConfigQuery {
  userId: number;
  name?: string;
}
