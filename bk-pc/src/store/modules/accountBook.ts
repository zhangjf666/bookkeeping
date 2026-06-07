import { defineStore } from "pinia";
import { getAccountBooks } from "@/api/accountBook";
import type { AccountBook } from "@/types/accountBook";
import { store } from "@/store";

interface AccountBookState {
  list: AccountBook[];
  loading: boolean;
}

export const useAccountBookStore = defineStore("accountBook", {
  state: (): AccountBookState => ({
    list: [],
    loading: false
  }),

  getters: {
    // 默认账本
    defaultAccountBook(): AccountBook | null {
      return (
        this.list.find(item => item.isDefault === "YES") || this.list[0] || null
      );
    },
    // 默认账本ID
    defaultAccountBookId(): number | null {
      const defaultBook = this.list.find(item => item.isDefault === "YES");
      return defaultBook?.id || this.list[0]?.id || null;
    },
    // 根据ID获取账本
    getAccountBookById(): (id: number) => AccountBook | undefined {
      return (id: number) => this.list.find(item => item.id === id);
    }
  },

  actions: {
    async fetchList(userId: number) {
      this.loading = true;
      try {
        const result = await getAccountBooks(userId);
        this.list = result || [];
      } finally {
        this.loading = false;
      }
    },

    setList(list: AccountBook[]) {
      this.list = list;
    }
  }
});

export function useAccountBookStoreHook() {
  return useAccountBookStore(store);
}
