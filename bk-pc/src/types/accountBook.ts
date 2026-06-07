export interface AccountBook {
  id: number;
  userId: number;
  name: string;
  image: string;
  isDefault: "YES" | "NO";
  createTime: string;
  updateTime: string;
}

export interface AccountBookForm {
  id?: number;
  userId: number;
  name: string;
  image: string;
  isDefault: "YES" | "NO";
}

export interface AccountBookQuery {
  userId: number;
  name?: string;
  pageNo?: number;
  pageSize?: number;
}

export interface AccountBookPageResult {
  total?: number;
  totalCount?: number;
  list?: AccountBook[];
  record?: AccountBook[];
}
