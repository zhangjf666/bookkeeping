import { defineStore } from "pinia";
import { store } from "../utils";
import type {
  Summary,
  TrendData,
  AccountBook,
  IncomeExpenseQuery
} from "@/types/dashboard";
import type {
  IncomeExpense,
  IncomeExpenseForm,
  Classify,
  Tag
} from "@/types/bill";
import { getSummary, getTrendData, getAccountBooks } from "@/api/dashboard";
import {
  getIncomeExpenseList,
  createIncomeExpense,
  updateIncomeExpense,
  deleteIncomeExpense,
  getClassifyList,
  getTagList,
  getRemarkList
} from "@/api/incomeExpense";
import dayjs from "dayjs";

export const useBillStore = defineStore("pure-bill", {
  state: () => ({
    summary: null as Summary | null,
    trendData: null as TrendData | null,
    accountBooks: [] as AccountBook[],
    currentAccountBook: null as AccountBook | null,
    loading: false,
    trendLoading: false,
    list: [] as IncomeExpense[],
    total: 0,
    queryParams: {
      pageNo: 1,
      pageSize: 10,
      type: undefined as string | undefined
    },
    listLoading: false,
    classifyList: [] as Classify[],
    classifyTree: [] as Classify[],
    tagList: [] as Tag[],
    remarkList: [] as { id: number; remark: string; classifyId: number }[]
  }),
  actions: {
    async loadSummary(userId: number, accountBookId?: number) {
      this.loading = true;
      try {
        const result = await getSummary({
          userId,
          accountBookId,
          days: 3
        });
        this.summary = result;
      } catch {
        this.summary = null;
      } finally {
        this.loading = false;
      }
    },
    async loadTrendData(
      userId: number,
      accountBookId?: number,
      days: number = 7
    ) {
      this.trendLoading = true;
      const endDate = dayjs().format("YYYY-MM-DD");
      const beginDate = dayjs()
        .subtract(days - 1, "day")
        .format("YYYY-MM-DD");
      try {
        const result = await getTrendData({
          userId,
          accountBookId,
          mode: "2",
          beginDate,
          endDate
        });
        this.trendData = result;
      } catch {
        this.trendData = null;
      } finally {
        this.trendLoading = false;
      }
    },
    async loadAccountBooks() {
      try {
        const result = await getAccountBooks();
        this.accountBooks = result;
        if (result.length > 0 && !this.currentAccountBook) {
          const defaultBook = result.find(
            (item: AccountBook) => item.isDefault
          );
          this.currentAccountBook = defaultBook || result[0];
        }
      } catch {
        this.accountBooks = [];
      }
    },
    setCurrentAccountBook(accountBook: AccountBook) {
      this.currentAccountBook = accountBook;
    },
    async loadList(userId: number, accountBookId?: number) {
      this.listLoading = true;
      try {
        const { type, ...restParams } = this.queryParams;
        const params = {
          ...restParams,
          ...(accountBookId && { accountBookId }),
          ...(type !== undefined && { type })
        };
        const result = await getIncomeExpenseList(userId, params);
        if (Array.isArray(result)) {
          this.list = result;
          this.total = result.length;
        } else {
          this.list = (result as any).record || result.list || [];
          this.total = (result as any).totalCount || result.total || 0;
        }
      } catch {
        this.list = [];
        this.total = 0;
      } finally {
        this.listLoading = false;
      }
    },
    async create(userId: number, data: IncomeExpenseForm) {
      await createIncomeExpense(userId, data);
      await this.loadList(userId, data.accountBookId);
    },
    async update(userId: number, data: IncomeExpenseForm) {
      await updateIncomeExpense(userId, data);
      await this.loadList(userId, data.accountBookId);
    },
    async remove(userId: number, ids: number[]) {
      await deleteIncomeExpense(ids);
      await this.loadList(userId, this.currentAccountBook?.id);
    },
    setQueryParams(params: Partial<IncomeExpenseQuery>) {
      const { type, ...rest } = params;
      const newParams: any = {
        ...this.queryParams,
        ...rest,
        page: 1
      };
      if (type !== undefined) {
        newParams.type = type;
      } else {
        delete newParams.type;
      }
      this.queryParams = newParams;
    },
    resetQueryParams() {
      this.queryParams = { pageNo: 1, pageSize: 10, type: undefined };
    },
    async loadClassifyAndTag(userId: number) {
      try {
        const [classifyResult, tagResult, remarkResult] = await Promise.all([
          getClassifyList(userId),
          getTagList(userId),
          getRemarkList(userId)
        ]);
        const flatList = classifyResult || [];
        this.classifyList = flatList;
        this.classifyTree = this.buildClassifyTree(flatList);
        this.tagList = tagResult || [];
        this.remarkList = remarkResult || [];
      } catch {
        this.classifyList = [];
        this.classifyTree = [];
        this.tagList = [];
        this.remarkList = [];
      }
    },
    buildClassifyTree(list: Classify[]): Classify[] {
      const tree: Classify[] = [];
      const map = new Map<number, Classify>();

      list.forEach(item => {
        map.set(item.id, { ...item, children: [] });
      });

      list.forEach(item => {
        const node = map.get(item.id)!;
        if (item.pid === -1) {
          tree.push(node);
        } else {
          const parent = map.get(item.pid);
          if (parent) {
            parent.children = parent.children || [];
            parent.children.push(node);
          }
        }
      });

      return tree;
    }
  }
});

export function useBillStoreHook() {
  return useBillStore(store);
}
