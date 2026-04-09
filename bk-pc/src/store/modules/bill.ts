import { defineStore } from "pinia";
import { store } from "../utils";
import type { AccountBook } from "@/types/accountBook";
import type {
  IncomeExpense,
  IncomeExpenseForm,
  IncomeExpenseQuery,
  Tag
} from "@/types/bill";
import type { Classify } from "@/types/classify";
import { getAccountBooks } from "@/api/accountBook";
import { getClassifyList as getClassifyListNew } from "@/api/classify";
import {
  getIncomeExpenseList,
  createIncomeExpense,
  updateIncomeExpense,
  deleteIncomeExpense,
  getTagList,
  getRemarkList
} from "@/api/incomeExpense";

export const useBillStore = defineStore("pure-bill", {
  state: () => ({
    accountBooks: [] as AccountBook[],
    currentAccountBook: null as AccountBook | null,
    list: [] as IncomeExpense[],
    total: 0,
    queryParams: {
      pageNo: 1,
      pageSize: 10
    },
    listLoading: false,
    classifyList: [] as Classify[],
    classifyTree: [] as Classify[],
    tagList: [] as Tag[],
    remarkList: [] as { id: number; remark: string; classifyId: number }[],
    userConfigList: [] as { name: string; value: string }[]
  }),
  getters: {
    isCreditCardEnabled(): boolean {
      const config = this.userConfigList.find(c => c.name === "is_credit_card");
      return config?.value === "1";
    }
  },
  actions: {
    setUserConfigList(configs: { name: string; value: string }[]) {
      this.userConfigList = configs;
    },
    async loadAccountBooks(userId: number) {
      try {
        const result = await getAccountBooks(userId);
        this.accountBooks = result;
        if (result.length > 0 && !this.currentAccountBook) {
          const defaultBook = result.find(
            (item: AccountBook) => item.isDefault === "YES"
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
    async loadList(userId: number) {
      this.listLoading = true;
      try {
        const params: Record<string, any> = {};
        Object.entries(this.queryParams).forEach(([key, value]) => {
          if (value !== undefined && value !== null && value !== "") {
            if (Array.isArray(value) && value.length === 0) {
              return;
            }
            params[key] = value;
          }
        });
        if (params.accountBookId === undefined && this.currentAccountBook) {
          params.accountBookId = this.currentAccountBook.id;
        }
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
    },
    async update(userId: number, data: IncomeExpenseForm) {
      await updateIncomeExpense(userId, data);
    },
    async remove(userId: number, ids: number[]) {
      await deleteIncomeExpense(ids);
      await this.loadList(userId);
    },
    setQueryParams(params: Partial<IncomeExpenseQuery>) {
      const newParams: any = {
        pageNo: 1,
        pageSize: 10
      };
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== "") {
          if (Array.isArray(value) && value.length === 0) {
            return;
          }
          newParams[key] = value;
        }
      });
      this.queryParams = newParams;
    },
    resetQueryParams() {
      this.queryParams = {
        pageNo: 1,
        pageSize: 10,
        accountBookId: this.queryParams.accountBookId,
        date: undefined,
        amount: undefined,
        classifyList: [],
        remark: undefined,
        tagCodes: undefined
      };
    },
    async loadClassifyAndTag(userId: number, force = false) {
      if (!force && this.classifyList.length > 0 && this.tagList.length > 0) {
        return;
      }
      try {
        const [classifyResult, tagResult, remarkResult] = await Promise.all([
          getClassifyListNew(userId),
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
