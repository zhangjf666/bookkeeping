export interface IncomeExpenseRecord {
  id: number;
  amount: number;
  type: string;
  date: string;
  remark: string;
  mainClassifyName: string;
  subClassifyName: string;
  mainClassifyImage: string;
}

export interface Summary {
  expenseAmount: number;
  incomeAmount: number;
  expenseLimit: number;
  expenseSurplus: number;
  incomeExpenseList: IncomeExpenseRecord[];
}

export interface DaySum {
  income: number;
  expense: number;
}

export interface TrendData {
  expenseTotal: number;
  incomeTotal: number;
  expenseLimit: number;
  expenseSurplus: number;
  incomeExpenseSum: Record<string, DaySum>;
  incomeExpenseList: IncomeExpenseRecord[];
}

export interface AccountBook {
  id: number;
  name: string;
  image: string;
  isDefault: boolean;
}

export interface SummaryParams {
  userId: number;
  accountBookId?: number;
  days?: number;
}

export interface TrendParams {
  userId: number;
  accountBookId?: number;
  mode: string;
  queryMode?: string;
  beginDate: string;
  endDate: string;
  classifyList?: number[];
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
