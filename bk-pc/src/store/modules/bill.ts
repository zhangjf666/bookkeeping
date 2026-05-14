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
      pageSize: 20,
      accountBookId: undefined as number | undefined,
      date: undefined as string[] | undefined,
      amount: undefined as number[] | undefined,
      classifyList: undefined as
        | { mainClassifyId: number; subClassifyId: number | null }[]
        | undefined,
      remark: undefined as string | undefined,
      tagCodes: undefined as string[] | undefined
    } as IncomeExpenseQuery,
    listLoading: false,
    classifyList: [] as Classify[],
    classifyTree: [] as Classify[],
    tagList: [] as Tag[],
    remarkList: [] as { id: number; remark: string; classifyId: number }[],
    userConfigList: [] as { name: string; value: string }[],
    // 编辑记录数据（用于页面间传递）
    editRecordData: null as IncomeExpense | null,
    // 列表滚动位置
    scrollTop: 0,
    // 日期模式（用于保持页面状态）
    dateMode: "month" as "month" | "year" | "custom",
    // 日期选择器状态
    startDate: [] as string[],
    endDate: [] as string[]
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
    setEditRecordData(record: IncomeExpense | null) {
      this.editRecordData = record;
    },
    setScrollTop(top: number) {
      this.scrollTop = top;
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
    async loadList(userId: number, append = false) {
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
          if (append) {
            // 追加数据，去重
            const existingIds = new Set(this.list.map(item => item.id));
            const newItems = result.filter(item => !existingIds.has(item.id));
            this.list = [...this.list, ...newItems];
          } else {
            this.list = result;
          }
          this.total = result.length;
        } else {
          const records = (result as any).record || result.list || [];
          if (append) {
            // 追加数据，去重
            const existingIds = new Set(this.list.map(item => item.id));
            const newItems = records.filter(
              (item: IncomeExpense) => !existingIds.has(item.id)
            );
            this.list = [...this.list, ...newItems];
          } else {
            this.list = records;
          }
          this.total = (result as any).totalCount || result.total || 0;
        }
      } catch {
        if (!append) {
          this.list = [];
          this.total = 0;
        }
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
      const newParams: any = { ...this.queryParams };
      Object.entries(params).forEach(([key, value]) => {
        // 如果值为空（undefined、null、空字符串、空数组），则删除该参数
        if (
          value === undefined ||
          value === null ||
          value === "" ||
          (Array.isArray(value) && value.length === 0)
        ) {
          delete newParams[key];
        } else {
          newParams[key] = value;
        }
      });
      this.queryParams = newParams;
    },
    resetQueryParams() {
      this.queryParams = {
        pageNo: 1,
        pageSize: 20,
        accountBookId: this.queryParams.accountBookId,
        date: undefined,
        amount: undefined,
        classifyList: undefined,
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
    },
    localAddRecord(record: IncomeExpense) {
      const insertIndex = this.list.findIndex(
        item => item.date < record.date
      );
      if (insertIndex === -1) {
        this.list.push(record);
      } else {
        this.list.splice(insertIndex, 0, record);
      }
      this.total++;
    },
    localUpdateRecord(record: IncomeExpense) {
      const index = this.list.findIndex(item => item.id === record.id);
      if (index !== -1) {
        this.list.splice(index, 1, record);
      }
    },
    localRemoveRecord(id: number) {
      const index = this.list.findIndex(item => item.id === id);
      if (index !== -1) {
        this.list.splice(index, 1);
        this.total--;
      }
    }
  }
});

export function useBillStoreHook() {
  return useBillStore(store);
}
