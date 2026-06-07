export type ClassifyType = "EXPENSE" | "INCOME";

export interface Classify {
  id: number;
  pid: number;
  name: string;
  userId: number;
  image: string;
  sort: number;
  type: ClassifyType;
  enable: "YES" | "NO";
  createTime?: string;
  updateTime?: string;
  children?: Classify[];
}

export interface ClassifyForm {
  id?: number;
  userId: number;
  name: string;
  pid: number;
  image: string;
  sort?: number;
  type: ClassifyType;
  enable: "YES" | "NO";
}

export interface ClassifyQuery {
  userId: number;
  name?: string;
  type?: ClassifyType;
  pageNo?: number;
  pageSize?: number;
}

export interface ClassifyPageResult {
  total?: number;
  totalCount?: number;
  list?: Classify[];
  record?: Classify[];
}
