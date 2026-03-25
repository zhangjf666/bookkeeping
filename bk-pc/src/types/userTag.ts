export interface UserTag {
  id: number;
  userId: number;
  name: string;
  color: string;
  sort: number;
  createTime: string;
  updateTime: string;
}

export interface UserTagForm {
  id?: number;
  userId: number;
  name: string;
  color: string;
  sort?: number;
}

export interface UserTagQuery {
  userId: number;
  name?: string;
  pageNo?: number;
  pageSize?: number;
}

export interface UserTagPageResult {
  total?: number;
  totalCount?: number;
  list?: UserTag[];
  record?: UserTag[];
}
