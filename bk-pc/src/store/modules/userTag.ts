import { defineStore } from "pinia";
import { getUserTagList } from "@/api/userTag";
import type { UserTag } from "@/types/userTag";
import { store } from "@/store";

interface UserTagState {
  list: UserTag[];
  loading: boolean;
}

export const useUserTagStore = defineStore("userTag", {
  state: (): UserTagState => ({
    list: [],
    loading: false
  }),

  getters: {
    // 根据ID获取标签
    getTagById(): (id: number) => UserTag | undefined {
      return (id: number) => this.list.find(item => item.id === id);
    },
    // 根据名称搜索标签
    searchByName(): (name: string) => UserTag[] {
      return (name: string) =>
        this.list.filter(item =>
          item.name.toLowerCase().includes(name.toLowerCase())
        );
    }
  },

  actions: {
    async fetchList(userId: number) {
      this.loading = true;
      try {
        const result = await getUserTagList(userId);
        // 直接返回数组
        this.list = Array.isArray(result) ? result : [];
      } finally {
        this.loading = false;
      }
    },

    setList(list: UserTag[]) {
      this.list = list;
    }
  }
});

export function useUserTagStoreHook() {
  return useUserTagStore(store);
}
