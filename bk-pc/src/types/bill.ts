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

export interface IncomeExpenseRecord {
  id: number;
  accountBookId: number;
  amount: number;
  type: string;
  date: string;
  remark: string;
  mainClassify: number;
  subClassify: number | null;
  mainClassifyName: string;
  subClassifyName: string;
  mainClassifyImage: string;
  tagCodes: string | null;
  isCreditCard: string;
}

export interface DaySum {
  income: number;
  expense: number;
}

export interface ClassifySummary {
  percent: number;
  expense: number;
  income: number;
  num: number;
  classifyName: string;
  classifyImage: string;
  classify: string;
  type?: string;
}

export interface TrendData {
  expenseTotal: number;
  incomeTotal: number;
  expenseLimit: number;
  expenseSurplus: number;
  incomeExpenseSum: Record<string, DaySum>;
  incomeExpenseList: IncomeExpenseRecord[];
}

export interface ClassifyReportData {
  expenseTotal: number;
  incomeTotal: number;
  expenseLimit: number;
  expenseSurplus: number;
  incomeExpenseSum: Record<string, ClassifySummary>;
  incomeExpenseList: IncomeExpenseRecord[];
}

export interface TrendParams {
  userId: number;
  accountBookId?: number;
  mode: string;
  queryMode?: string;
  beginDate: string;
  endDate: string;
  classifyList?: ClassifyQueryItem[];
  remark?: string;
  tagCodes?: number[];
}

export interface ClassifyQueryItem {
  mainClassifyId: number;
  subClassifyId?: number | null;
}

export interface Summary {
  expenseAmount: number;
  incomeAmount: number;
  expenseLimit: number;
  expenseSurplus: number;
  incomeExpenseList: IncomeExpenseRecord[];
}

export interface SummaryParams {
  userId: number;
  accountBookId?: number;
  days?: number;
}
