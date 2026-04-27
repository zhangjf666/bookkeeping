import { defineStore } from "pinia";
import { getClassifyList } from "@/api/classify";
import type { Classify, ClassifyType } from "@/types/classify";
import { store } from "@/store";

interface ClassifyState {
  list: Classify[];
  loading: boolean;
}

export const useClassifyStore = defineStore("classify", {
  state: (): ClassifyState => ({
    list: [],
    loading: false
  }),

  getters: {
    // 根据类型获取分类列表
    getClassifyByType(): (type: ClassifyType) => Classify[] {
      return (type: ClassifyType) =>
        this.list.filter(item => item.type === type && item.enable === "YES");
    },
    // 获取支出分类
    expenseClassify(): Classify[] {
      return this.getClassifyByType("EXPENSE");
    },
    // 获取收入分类
    incomeClassify(): Classify[] {
      return this.getClassifyByType("INCOME");
    },
    // 根据ID获取分类
    getClassifyById(): (id: number) => Classify | undefined {
      return (id: number) => this.list.find(item => item.id === id);
    },
    // 获取顶级分类
    getParentClassify(): (type: ClassifyType) => Classify[] {
      return (type: ClassifyType) =>
        this.list.filter(
          item => item.type === type && item.pid === 0 && item.enable === "YES"
        );
    }
  },

  actions: {
    async fetchList(userId: number, type?: ClassifyType) {
      this.loading = true;
      try {
        const result = await getClassifyList(
          userId,
          type ? { type } : undefined
        );
        this.list = result || [];
      } finally {
        this.loading = false;
      }
    },

    setList(list: Classify[]) {
      this.list = list;
    }
  }
});

export function useClassifyStoreHook() {
  return useClassifyStore(store);
}
