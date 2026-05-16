<script setup lang="ts">
import { ref, computed, onMounted, onActivated } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { storeToRefs } from "pinia";
import { useUserStoreHook } from "@/store/modules/user";
import { useAccountBookStore } from "@/store/modules/accountBook";
import { useUserTagStore } from "@/store/modules/userTag";
import { useRemarkStore } from "@/store/modules/remark";
import { useUserConfigStore } from "@/store/modules/userConfig";
import { useClassifyStore } from "@/store/modules/classify";
import { useBillStore } from "@/store/modules/bill";
import { getSummary } from "@/api/incomeExpense";
import type { Summary, IncomeExpenseRecord } from "@/types/bill";
import {
  showLoading,
  hideLoading,
  showError,
  showSuccess
} from "@/utils/mobile/message";
import SummaryCard from "@/components/mobile/SummaryCard.vue";
import RecordItem from "@/components/mobile/RecordItem.vue";

defineOptions({
  name: "MobileHome"
});

const router = useRouter();
const { t } = useI18n();

// 用户信息
const userStore = useUserStoreHook();
const userId = computed(() => userStore.id);

// Stores
const accountBookStore = useAccountBookStore();
const userTagStore = useUserTagStore();
const remarkStore = useRemarkStore();
const userConfigStore = useUserConfigStore();
const classifyStore = useClassifyStore();
const billStore = useBillStore();

// 状态
const refreshing = ref(false);
const loading = ref(false);
const summaryData = ref<Summary | null>(null);
const currentAccountBookId = ref<number | null>(null);

// 账本列表
const { list: accountBookList } = storeToRefs(accountBookStore);

// 用户配置
const { showExpenseLimit, limitType, monthExpenseLimit, yearExpenseLimit } =
  storeToRefs(userConfigStore);

// 头部固定区域
const headerFixedRef = ref<HTMLElement>();

// 加载用户基础数据
const loadUserData = async () => {
  if (!userId.value) return;

  try {
    // 只获取没有数据的store
    const promises = [];
    if (accountBookStore.list.length === 0) {
      promises.push(accountBookStore.fetchList(userId.value));
    }
    if (userTagStore.list.length === 0) {
      promises.push(userTagStore.fetchList(userId.value));
    }
    if (remarkStore.list.length === 0) {
      promises.push(remarkStore.fetchList(userId.value));
    }
    if (!userConfigStore.loaded) {
      promises.push(userConfigStore.fetchConfigs(userId.value));
    }
    if (classifyStore.list.length === 0) {
      promises.push(classifyStore.fetchList(userId.value));
    }
    if (promises.length > 0) {
      await Promise.all(promises);
    }
  } catch (error) {
    console.error("加载用户数据失败:", error);
  }
};

// 加载摘要数据
const loadSummary = async (accountBookId?: number) => {
  const targetId = accountBookId || currentAccountBookId.value;
  if (!targetId || !userId.value) return;

  try {
    const result = await getSummary({
      userId: userId.value,
      accountBookId: targetId,
      days: 3
    });
    summaryData.value = result;
  } catch (error) {
    console.error("加载摘要数据失败:", error);
    throw error;
  }
};

// 初始化页面
const initPage = async () => {
  loading.value = true;
  showLoading(t("mobile.common.loading"));

  try {
    await loadUserData();
    // 设置默认账本
    currentAccountBookId.value = accountBookStore.defaultAccountBookId;
    await loadSummary();
  } catch (error) {
    showError(t("mobile.common.failed"));
  } finally {
    hideLoading();
    loading.value = false;
  }
};

// 切换账本
const handleAccountBookChange = async (bookId: number) => {
  currentAccountBookId.value = bookId;
  loading.value = true;
  try {
    await loadSummary(bookId);
  } catch (error) {
    showError(t("mobile.common.failed"));
  } finally {
    loading.value = false;
  }
};

// 下拉刷新
const onRefresh = async () => {
  try {
    await loadSummary();
    showSuccess(t("mobile.common.success"));
  } catch (error) {
    showError(t("mobile.common.failed"));
  } finally {
    refreshing.value = false;
  }
};

// 点击记录项
const handleRecordClick = (record: IncomeExpenseRecord) => {
  // 先保存滚动位置，再传递数据
  billStore.setScrollTop(window.scrollY);
  // 使用 billStore 传递数据，比 sessionStorage 更可靠
  billStore.setEditRecordData(record as any);
  router.push(`/record/${record.id}`);
};

// 点击记一笔
const handleAddRecord = () => {
  router.push("/record");
};

onMounted(() => {
  initPage();
});

// 从记录页返回时，如果保存成功则刷新摘要
onActivated(() => {
  const needRefresh = sessionStorage.getItem("homeNeedRefresh");
  if (needRefresh) {
    sessionStorage.removeItem("homeNeedRefresh");
    loadSummary();
  }
});
</script>

<template>
  <div class="home-page">
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div class="page-content">
        <!-- 粘性顶部区域 -->
        <div ref="headerFixedRef" class="header-sticky">
          <!-- 摘要卡片 -->
          <SummaryCard
            v-if="summaryData"
            :data="summaryData"
            :account-book-list="accountBookList"
            :current-account-book-id="currentAccountBookId"
            :show-expense-limit="showExpenseLimit"
            :limit-type="limitType"
            :month-expense-limit="monthExpenseLimit"
            :year-expense-limit="yearExpenseLimit"
            :loading="loading"
            @update:currentAccountBookId="handleAccountBookChange"
          />

          <!-- 近三日账单标题 -->
          <div class="list-header">
            {{ t("mobile.home.recentBills") }}
          </div>
        </div>

        <!-- 列表 -->
        <div v-if="summaryData?.incomeExpenseList?.length" class="list-content">
          <RecordItem
            v-for="record in summaryData.incomeExpenseList"
            :key="record.id"
            :record="record"
            :show-date="true"
            @click="handleRecordClick"
          />
        </div>

        <van-empty v-else :description="t('mobile.common.noData')" />
      </div>
    </van-pull-refresh>

    <!-- 悬浮按钮 -->
    <div class="floating-btn" @click="handleAddRecord">
      <van-icon name="plus" size="24" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.home-page {
  min-height: 100vh;
  background-color: $color-background;
  overscroll-behavior-y: none;
}

// 解除 van-pull-refresh 的 overflow: hidden，使其不成为滚动容器
// 这样 position: sticky 才能以 viewport 为参考系正常粘顶
:deep(.van-pull-refresh) {
  overflow: visible;
}

.page-content {
  min-height: 100vh;
  padding-bottom: calc(60px + env(safe-area-inset-bottom));
  background-color: $color-background;
}

.list-content {
  margin: 0 16px;
  background-color: $color-card;
  border-radius: 12px;
  overflow: hidden;
}

// 粘性头部：向上滚动时粘顶，下拉时跟随 van-pull-refresh 同步下滑
.header-sticky {
  position: sticky;
  position: -webkit-sticky;
  top: 0;
  z-index: 10;
  width: 100%;
  padding-top: calc(env(safe-area-inset-top) + 15px);
  background-color: $color-background;
}

// 消除 SummaryCard 默认 margin-top，避免 margin collapsing 导致 sticky 偏移
.header-sticky :deep(.summary-card) {
  margin-top: 0;
}

.list-header {
  padding: 12px 16px;
  font-size: 14px;
  font-weight: 500;
  color: $color-text-primary;
}

.list-content {
  margin: 0 16px;
  background-color: $color-card;
  border-radius: 12px;
  overflow: hidden;
}

.floating-btn {
  position: fixed;
  right: 16px;
  bottom: calc(66px + env(safe-area-inset-bottom));
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  color: #fff;
  background-color: $color-primary;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgb(0 0 0 / 15%);

  &:active {
    transform: scale(0.95);
  }
}
</style>
