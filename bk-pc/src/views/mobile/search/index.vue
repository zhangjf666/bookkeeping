<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useRouter } from "vue-router";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import { getIncomeExpenseList } from "@/api/incomeExpense";
import type { IncomeExpense } from "@/types/bill";
import { formatNumber } from "@/utils/format";

defineOptions({
  name: "MobileSearch"
});

const router = useRouter();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

const keyword = ref("");
const loading = ref(false);
const results = ref<IncomeExpense[]>([]);

// 搜索
const doSearch = async () => {
  if (!keyword.value.trim() || !userStore.id) return;

  loading.value = true;
  try {
    const result = await getIncomeExpenseList(userStore.id, {
      pageNo: 1,
      pageSize: 50,
      remark: keyword.value.trim()
    });
    results.value = Array.isArray(result) ? result : (result as any).record || [];
  } catch {
    results.value = [];
  } finally {
    loading.value = false;
  }
};

// 监听关键词变化，防抖搜索
let timer: ReturnType<typeof setTimeout> | null = null;
watch(keyword, () => {
  if (timer) clearTimeout(timer);
  timer = setTimeout(() => {
    doSearch();
  }, 300);
});

// 编辑
const handleEdit = (id: number) => {
  router.push(`/m/record/${id}`);
};

// 常用备注
const recentRemarks = computed(() => {
  return billStore.remarkList.slice(0, 10);
});

// 选择常用备注
const selectRemark = (remark: string) => {
  keyword.value = remark;
};
</script>

<template>
  <div class="mobile-search">
    <!-- 搜索框 -->
    <div class="search-bar">
      <van-search
        v-model="keyword"
        placeholder="搜索备注"
        show-action
        @search="doSearch"
        @cancel="router.back()"
      />
    </div>

    <!-- 常用备注 -->
    <div v-if="!keyword && recentRemarks.length" class="recent-section">
      <div class="section-title">常用备注</div>
      <div class="remark-tags">
        <van-tag
          v-for="item in recentRemarks"
          :key="item.id"
          size="large"
          @click="selectRemark(item.remark)"
        >
          {{ item.remark }}
        </van-tag>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div v-if="keyword" class="results-section">
      <van-loading v-if="loading" class="loading" />

      <van-empty v-else-if="results.length === 0" description="未找到相关记录" />

      <div v-else class="result-list">
        <div
          v-for="item in results"
          :key="item.id"
          class="result-item"
          @click="handleEdit(item.id)"
        >
          <div class="result-left">
            <span class="classify-icon">{{ item.mainClassifyImage || '📝' }}</span>
            <div class="result-info">
              <span class="classify-name">{{ item.mainClassifyName }}</span>
              <span class="remark" v-if="item.remark">{{ item.remark }}</span>
              <span class="date">{{ item.date }}</span>
            </div>
          </div>
          <div class="result-right">
            <span class="amount" :class="{ income: item.type === 'INCOME' }">
              {{ item.type === 'INCOME' ? '+' : '-' }}¥{{ formatNumber(item.amount) }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.mobile-search {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.search-bar {
  background: #fff;
}

.recent-section {
  background: #fff;
  padding: 15px;
  margin-top: 10px;

  .section-title {
    font-size: 14px;
    color: #666;
    margin-bottom: 10px;
  }

  .remark-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .van-tag {
      cursor: pointer;
    }
  }
}

.loading {
  display: flex;
  justify-content: center;
  padding: 40px;
}

.results-section {
  margin-top: 10px;
}

.result-list {
  .result-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 15px;
    background: #fff;
    border-bottom: 1px solid #f5f5f5;

    .result-left {
      display: flex;
      align-items: center;
      gap: 10px;

      .classify-icon {
        width: 36px;
        height: 36px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f5f5f5;
        border-radius: 8px;
        font-size: 18px;
      }

      .result-info {
        display: flex;
        flex-direction: column;
        gap: 2px;

        .classify-name {
          font-size: 14px;
          color: #333;
        }

        .remark {
          font-size: 12px;
          color: #666;
        }

        .date {
          font-size: 12px;
          color: #999;
        }
      }
    }

    .result-right {
      .amount {
        font-size: 16px;
        font-weight: 500;
        color: #333;

        &.income {
          color: #07c160;
        }
      }
    }
  }
}
</style>