export interface UserRemark {
  id: number;
  userId: number;
  remark: string;
  classifyId: number;
  createTime: string;
  updateTime: string;
}

export interface UserRemarkForm {
  id?: number;
  userId: number;
  remark: string;
  classifyId: number;
}

export interface UserRemarkQuery {
  userId: number;
  remark?: string;
  classifyId?: number;
  pageNo?: number;
  pageSize?: number;
}

export interface UserRemarkPageResult {
  total?: number;
  totalCount?: number;
  list?: UserRemark[];
  record?: UserRemark[];
}
