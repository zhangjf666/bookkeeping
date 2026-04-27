import { defineStore } from "pinia";
import { getUserConfigList } from "@/api/userConfig";
import type { UserConfig } from "@/types/userConfig";
import { store } from "@/store";

interface UserConfigState {
  configs: UserConfig[];
  loading: boolean;
  loaded: boolean;
}

// 限额显示类型：1=不显示，2=显示月限额，3=显示年限额
export type ExpenseLimitDisplayType = 1 | 2 | 3;

export const useUserConfigStore = defineStore("userConfig", {
  state: (): UserConfigState => ({
    configs: [],
    loading: false,
    loaded: false
  }),

  getters: {
    // 获取指定名称的配置
    getConfigByName(): (name: string) => UserConfig | undefined {
      return (name: string) => this.configs.find(item => item.name === name);
    },
    // 限额显示类型：1=不显示，2=显示月限额，3=显示年限额
    expenseLimitDisplayType(): ExpenseLimitDisplayType {
      const config = this.getConfigByName("show_expense_limit");
      const value = config?.value || "1";
      return parseInt(value) as ExpenseLimitDisplayType;
    },
    // 是否显示支出限额
    showExpenseLimit(): boolean {
      return this.expenseLimitDisplayType !== 1;
    },
    // 限额类型
    limitType(): "MONTHLY" | "YEARLY" {
      return this.expenseLimitDisplayType === 3 ? "YEARLY" : "MONTHLY";
    },
    // 月支出限额
    monthExpenseLimit(): number {
      const config = this.getConfigByName("monthlyExpenseLimit");
      return config ? parseFloat(config.value) : 0;
    },
    // 年支出限额
    yearExpenseLimit(): number {
      const config = this.getConfigByName("yearlyExpenseLimit");
      return config ? parseFloat(config.value) : 0;
    }
  },

  actions: {
    async fetchConfigs(userId: number) {
      this.loading = true;
      try {
        const result = await getUserConfigList(userId);
        this.configs = result || [];
        this.loaded = true;
      } finally {
        this.loading = false;
      }
    },

    setConfigs(configs: UserConfig[]) {
      this.configs = configs;
    }
  }
});

export function useUserConfigStoreHook() {
  return useUserConfigStore(store);
}
