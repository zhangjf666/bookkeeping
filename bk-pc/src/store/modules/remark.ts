import { defineStore } from "pinia";
import { getUserRemarkList } from "@/api/remark";
import type { UserRemark } from "@/types/remark";
import { store } from "@/store";

interface RemarkState {
  list: UserRemark[];
  loading: boolean;
}

export const useRemarkStore = defineStore("remark", {
  state: (): RemarkState => ({
    list: [],
    loading: false
  }),

  getters: {
    // 根据分类ID获取备注列表
    getRemarksByClassifyId(): (classifyId: number) => UserRemark[] {
      return (classifyId: number) =>
        this.list.filter(item => item.classifyId === classifyId);
    },
    // 根据ID获取备注
    getRemarkById(): (id: number) => UserRemark | undefined {
      return (id: number) => this.list.find(item => item.id === id);
    }
  },

  actions: {
    async fetchList(userId: number) {
      this.loading = true;
      try {
        const result = await getUserRemarkList(userId);
        // 直接返回数组
        this.list = Array.isArray(result) ? result : [];
      } finally {
        this.loading = false;
      }
    },

    setList(list: UserRemark[]) {
      this.list = list;
    }
  }
});

export function useRemarkStoreHook() {
  return useRemarkStore(store);
}
