<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { showNotify } from "vant";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import { getSummary } from "@/api/incomeExpense";
import { getUserConfigList } from "@/api/userConfig";
import type { Summary, IncomeExpense } from "@/types/bill";
import { formatNumber } from "@/utils/format";
import { getAccountBookIcon } from "@/utils/accountBook";
import { getClassifyIcon } from "@/utils/classifyIcons";

defineOptions({
  name: "MobileHome"
});

const router = useRouter();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

const loading = ref(false);
const refreshing = ref(false);
const summaryData = ref<Summary | null>(null);
const showExpenseLimit = ref<"1" | "2" | "3">("1");
const showAccountBookPicker = ref(false);

// 下拉刷新相关
const pullDistance = ref(0);
const touchStartY = ref(0);
const scrollTop = ref(0);
const recordScrollRef = ref<HTMLElement | null>(null);
const pullThreshold = 60;

const userId = computed(() => userStore.id);

// 当前账本名称显示
const currentAccountBookName = computed(() => {
  const book = billStore.currentAccountBook;
  if (!book) return "请选择账本";
  return `${getAccountBookIcon(book.image)} ${book.name}`;
});

// 切换账本
const onSelectAccountBook = (book: any) => {
  billStore.setCurrentAccountBook(book);
  showAccountBookPicker.value = false;
  loadData();
};

// 加载数据
const loadData = async () => {
  if (!userId.value) {
    router.replace("/login");
    return;
  }

  loading.value = true;
  try {
    await Promise.all([
      billStore.loadAccountBooks(userId.value),
      billStore.loadClassifyAndTag(userId.value)
    ]);

    const accountBookId = billStore.currentAccountBook?.id;
    const summary = await getSummary({
      userId: userId.value,
      accountBookId,
      days: 3
    });
    summaryData.value = summary;

    await loadUserConfig();
  } catch (error: any) {
    showNotify({ type: "danger", message: error?.message || "加载失败" });
  } finally {
    loading.value = false;
  }
};

// 下拉刷新
const onRefresh = async () => {
  refreshing.value = true;
  pullDistance.value = pullThreshold;
  await loadData();
  refreshing.value = false;
  pullDistance.value = 0;
};

// 摘要区域触摸事件 - 只处理下拉刷新
const onSummaryTouchStart = (e: TouchEvent) => {
  if (refreshing.value) return;
  touchStartY.value = e.touches[0].clientY;
};

const onSummaryTouchMove = (e: TouchEvent) => {
  if (refreshing.value) return;

  const currentY = e.touches[0].clientY;
  const deltaY = currentY - touchStartY.value;

  // 摘要区域：下拉时触发刷新
  if (deltaY > 0) {
    e.preventDefault();
    pullDistance.value = Math.min(deltaY * 0.4, 80);
  } else {
    // 上滑时不处理
    if (pullDistance.value > 0) {
      pullDistance.value = 0;
    }
  }
};

const onSummaryTouchEnd = () => {
  if (pullDistance.value >= pullThreshold && !refreshing.value) {
    onRefresh();
  } else {
    pullDistance.value = 0;
  }
};

// 记录区域触摸事件 - 处理滚动和下拉刷新
const onRecordTouchStart = (e: TouchEvent) => {
  if (refreshing.value) return;
  touchStartY.value = e.touches[0].clientY;
};

const onRecordTouchMove = (e: TouchEvent) => {
  if (refreshing.value) return;

  const currentY = e.touches[0].clientY;
  const deltaY = currentY - touchStartY.value;

  // 实时获取滚动位置
  const currentScrollTop = recordScrollRef.value?.scrollTop ?? 0;

  // 只有下拉且在顶部时才处理刷新
  if (deltaY > 0 && currentScrollTop <= 0) {
    e.preventDefault();
    pullDistance.value = Math.min(deltaY * 0.4, 80);
  } else {
    // 其他情况让滚动正常进行
    if (pullDistance.value > 0) {
      pullDistance.value = 0;
    }
  }
};

const onRecordTouchEnd = () => {
  if (pullDistance.value >= pullThreshold && !refreshing.value) {
    onRefresh();
  } else {
    pullDistance.value = 0;
  }
};

// 记录区域滚动
const handleScroll = (e: Event) => {
  const target = e.target as HTMLElement;
  scrollTop.value = target.scrollTop;
};

// 加载用户配置
const loadUserConfig = async () => {
  if (!userId.value) return;

  try {
    const result = await getUserConfigList(userId.value);
    const configList = result || [];
    billStore.setUserConfigList(configList);

    const showLimitConfig = configList.find(
      c => c.name === "show_expense_limit"
    );
    if (showLimitConfig) {
      showExpenseLimit.value = showLimitConfig.value as "1" | "2" | "3";
    }
  } catch {
    showExpenseLimit.value = "1";
  }
};

// 跳转记账页
const goRecord = () => {
  router.push("/record");
};

// 跳转账单页
const goBill = () => {
  router.push("/bill-list");
};

// 点击账单项，将数据存入store后跳转
const handleRecordClick = (item: any) => {
  const existIndex = billStore.list.findIndex(r => r.id === item.id);
  const record = {
    ...item,
    userId: userId.value,
    isAddRemark: null,
    createTime: item.createTime || new Date().toISOString(),
    updateTime: item.updateTime || new Date().toISOString(),
    subClassifyImage: item.subClassifyImage || null
  } as IncomeExpense;

  if (existIndex === -1) {
    billStore.list = [record, ...billStore.list];
  } else {
    billStore.list[existIndex] = record;
  }
  router.push(`/record/${item.id}`);
};

// 是否显示支出限额
const expenseLimitShow = computed(() => {
  return showExpenseLimit.value !== "1" && summaryData.value;
});

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="mobile-home">
    <!-- 下拉刷新指示器 -->
    <div
      class="refresh-indicator"
      :class="{ refreshing }"
      :style="{ height: pullDistance + 'px' }"
    >
      <van-loading v-if="refreshing" size="18" color="#d83d34" />
      <span v-else :class="{ ready: pullDistance >= pullThreshold }">
        {{ pullDistance >= pullThreshold ? '释放刷新' : '下拉刷新' }}
      </span>
    </div>

    <!-- 可移动的内容区域 -->
    <div
      class="content-wrapper"
      :style="{ transform: `translateY(${pullDistance}px)` }"
    >
      <!-- 顶部摘要卡片 - 固定高度，处理下拉刷新 -->
      <div
        class="summary-card"
        @touchstart="onSummaryTouchStart"
        @touchmove="onSummaryTouchMove"
        @touchend="onSummaryTouchEnd"
      >
        <div class="summary-header">
          <span class="month-label">本月</span>
          <span class="account-book-picker" @click="showAccountBookPicker = true">
            {{ currentAccountBookName }}
            <van-icon name="arrow-down" />
          </span>
        </div>

        <div class="expense-section">
          <span class="label">支出 (元)</span>
          <div class="amount">
            <span class="currency">¥</span>
            <span class="value">{{ formatNumber(summaryData?.expenseAmount || 0) }}</span>
          </div>
        </div>

        <div class="income-section">
          <span class="label">收入</span>
          <span class="amount">¥{{ formatNumber(summaryData?.incomeAmount || 0) }}</span>
        </div>

        <div v-if="expenseLimitShow" class="limit-section">
          <span class="limit-label">
            {{ showExpenseLimit === '2' ? '本月限额' : '本年限额' }}
          </span>
          <span class="limit-value">¥{{ formatNumber(summaryData?.expenseLimit || 0) }}</span>
          <span class="limit-surplus" :class="{ warning: (summaryData?.expenseSurplus || 0) < 0 }">
            剩余 ¥{{ formatNumber(summaryData?.expenseSurplus || 0) }}
          </span>
        </div>
      </div>

      <!-- 近期收支记录区域 -->
      <div class="recent-section">
        <!-- 标题栏 -->
        <div class="section-header">
          <span class="title">近三日收支记录</span>
          <span class="more" @click="goBill">查看全部 ></span>
        </div>

        <!-- 记录列表 - 独立滚动容器 -->
        <div
          ref="recordScrollRef"
          class="record-scroll"
        >
          <van-loading v-if="loading" class="loading" />

          <van-empty
            v-else-if="!summaryData?.incomeExpenseList?.length"
            description="暂无收支记录"
          />

          <div v-else class="record-list">
            <div
              v-for="item in summaryData?.incomeExpenseList"
              :key="item.id"
              class="record-item"
              @click="handleRecordClick(item)"
            >
              <span class="date">{{ item.date.slice(5) }}</span>
              <div class="record-middle">
                <span class="classify-icon">{{ getClassifyIcon(item.mainClassifyImage) }}</span>
                <div class="record-info">
                  <span class="classify-name">
                    {{ item.subClassifyName ? `${item.mainClassifyName} - ${item.subClassifyName}` : item.mainClassifyName }}
                  </span>
                  <span class="remark" v-if="item.remark">{{ item.remark }}</span>
                </div>
              </div>
              <span class="amount" :class="{ income: item.type === 'INCOME' }">
                ¥{{ formatNumber(item.amount) }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 悬浮记账按钮 -->
    <div class="fab-button" @click="goRecord">
      <van-icon name="plus" />
    </div>

    <!-- 账本选择弹窗 -->
    <van-popup
      v-model:show="showAccountBookPicker"
      position="bottom"
      round
      style="height: 40%"
    >
      <div class="account-book-picker-popup">
        <div class="picker-header">
          <span class="title">选择账本</span>
          <van-icon name="cross" @click="showAccountBookPicker = false" />
        </div>
        <div class="picker-content">
          <div
            v-for="book in billStore.accountBooks"
            :key="book.id"
            class="book-item"
            :class="{ active: book.id === billStore.currentAccountBook?.id }"
            @click="onSelectAccountBook(book)"
          >
            <span class="book-icon">{{ getAccountBookIcon(book.image) }}</span>
            <span class="book-name">{{ book.name }}</span>
            <van-tag v-if="book.isDefault === 'YES'" color="#00a151">默认</van-tag>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style lang="scss" scoped>
.mobile-home {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f7f8fa;
  overflow: hidden;
  position: relative;
}

// 下拉刷新指示器
.refresh-indicator {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #969799;
  background: #f7f8fa;
  overflow: hidden;
  z-index: 1;

  span {
    &.ready {
      color: #d83d34;
    }
  }
}

// 内容包装器 - 整体移动
.content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: transform 0.15s ease-out;
}

// 摘要卡片 - 固定高度
.summary-card {
  flex-shrink: 0;
  background: linear-gradient(135deg, #d83d34 0%, #e8645c 100%);
  padding: 20px;
  color: #fff;

  .summary-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .month-label {
      font-size: 16px;
      font-weight: 500;
    }

    .account-book-picker {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      padding: 4px 10px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 12px;
      cursor: pointer;
    }
  }

  .expense-section {
    margin-bottom: 15px;

    .label {
      font-size: 14px;
      opacity: 0.8;
    }

    .amount {
      margin-top: 5px;

      .currency {
        font-size: 20px;
      }

      .value {
        font-size: 36px;
        font-weight: bold;
      }
    }
  }

  .income-section {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;

    .label {
      opacity: 0.8;
    }

    .amount {
      font-weight: 500;
      color: #a8e6cf;
    }
  }

  .limit-section {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-top: 15px;
    padding-top: 15px;
    border-top: 1px solid rgba(255, 255, 255, 0.2);
    font-size: 12px;

    .limit-surplus {
      margin-left: auto;

      &.warning {
        color: #ffeb3b;
      }
    }
  }
}

// 近期收支记录区域
.recent-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  margin-top: 10px;
  overflow: hidden;

  // 标题栏 - 固定
  .section-header {
    flex-shrink: 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    border-bottom: 1px solid #ebedf0;

    .title {
      font-size: 16px;
      font-weight: 500;
      color: #323233;
    }

    .more {
      font-size: 12px;
      color: #969799;
    }
  }

  // 记录列表 - 独立滚动
  .record-scroll {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    -webkit-overflow-scrolling: touch;
  }

  .loading {
    display: flex;
    justify-content: center;
    padding: 20px;
  }
}

.record-list {
  padding-bottom: calc(60px + env(safe-area-inset-bottom));

  .record-item {
    display: flex;
    align-items: center;
    padding: 12px 20px;
    border-bottom: 1px solid #ebedf0;
    gap: 12px;

    &:last-child {
      border-bottom: none;
    }

    .date {
      font-size: 13px;
      color: #969799;
      min-width: 40px;
    }

    .record-middle {
      display: flex;
      align-items: center;
      gap: 10px;
      flex: 1;
      min-width: 0;

      .classify-icon {
        width: 36px;
        height: 36px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f7f8fa;
        border-radius: 8px;
        font-size: 18px;
        flex-shrink: 0;
      }

      .record-info {
        display: flex;
        flex-direction: column;
        gap: 2px;
        min-width: 0;

        .classify-name {
          font-size: 14px;
          color: #323233;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .remark {
          font-size: 12px;
          color: #969799;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }

    .amount {
      font-size: 15px;
      font-weight: 600;
      color: #d83d34;
      flex-shrink: 0;

      &.income {
        color: #00a151;
      }
    }
  }
}

// 悬浮按钮
.fab-button {
  position: fixed;
  right: 20px;
  bottom: calc(70px + env(safe-area-inset-bottom));
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #d83d34;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  box-shadow: 0 4px 12px rgba(216, 61, 52, 0.4);
  cursor: pointer;
  z-index: 99;
  transition: transform 0.2s, box-shadow 0.2s;

  &:active {
    transform: scale(0.95);
    box-shadow: 0 2px 8px rgba(216, 61, 52, 0.3);
  }
}

.account-book-picker-popup {
  height: 100%;
  display: flex;
  flex-direction: column;

  .picker-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #ebedf0;

    .title {
      font-size: 16px;
      font-weight: 600;
      color: #323233;
    }
  }

  .picker-content {
    flex: 1;
    overflow-y: auto;
    padding: 10px;

    .book-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 12px 15px;
      border-radius: 8px;
      cursor: pointer;

      &:active {
        background: #f7f8fa;
      }

      &.active {
        background: #fff5f5;
      }

      .book-icon {
        font-size: 20px;
      }

      .book-name {
        flex: 1;
        font-size: 14px;
        color: #323233;
      }
    }
  }
}
</style>