export interface Tag {
  id: number;
  code: number;
  name: string;
  color: string;
}

export interface IncomeExpense {
  id: number;
  userId: number;
  accountBookId: number;
  amount: number;
  type: string;
  date: string;
  remark: string;
  mainClassify: number;
  subClassify: number | null;
  isCreditCard: string;
  isAddRemark: string | null;
  tagCodes: string;
  createTime: string;
  updateTime: string;
  mainClassifyName: string | null;
  subClassifyName: string | null;
  mainClassifyImage: string | null;
  subClassifyImage: string | null;
  tags?: Tag[];
}

export interface IncomeExpenseQuery {
  pageNo?: number;
  pageSize?: number;
  accountBookId?: number;
  type?: string;
  date?: string[];
  amount?: number[];
  mainClassify?: number;
  subClassify?: number;
  remark?: string;
  tagCodes?: number[];
}

export interface IncomeExpenseForm {
  id?: number;
  accountBookId: number;
  amount: number;
  type: string;
  date: string;
  remark: string;
  mainClassify: number;
  subClassify?: number;
  isCreditCard: string;
  isAddRemark: string;
  tagCodes: string;
}

export interface PageResult<T> {
  total?: number;
  totalCount?: number;
  list?: T[];
  record?: T[];
}

export interface Classify {
  id: number;
  pid: number;
  name: string;
  userId: number;
  image: string;
  sort: number;
  type: 0 | 1;
  enable: boolean;
  children?: Classify[];
}
