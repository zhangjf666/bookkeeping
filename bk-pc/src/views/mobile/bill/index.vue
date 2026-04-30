<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import { showConfirmDialog } from "vant";
import dayjs from "dayjs";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import { useClassifyStoreHook } from "@/store/modules/classify";
import { useAccountBookStoreHook } from "@/store/modules/accountBook";
import { useUserTagStoreHook } from "@/store/modules/userTag";
import { useRemarkStoreHook } from "@/store/modules/remark";
import { deleteIncomeExpense } from "@/api/incomeExpense";
import type { IncomeExpense } from "@/types/bill";
import {
  showLoading,
  hideLoading,
  showSuccess,
  showError
} from "@/utils/mobile/message";
import { formatNumber } from "@/utils/format";
import { getClassifyIcon } from "@/utils/classifyIcons";
import BillFilter from "@/components/mobile/BillFilter.vue";

defineOptions({
  name: "MobileBill"
});

const { t } = useI18n();
const router = useRouter();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();
const classifyStore = useClassifyStoreHook();
const accountBookStore = useAccountBookStoreHook();
const userTagStore = useUserTagStoreHook();
const remarkStore = useRemarkStoreHook();

// 日期快捷选择
const dateMode = ref<"month" | "year" | "custom">("month");
const showDateAction = ref(false);
const showCalendar = ref(false);

// 筛选弹窗
const showFilter = ref(false);

// 加载更多状态
const loading = ref(true); // 初始为 true，防止 van-list 自动触发
const finished = ref(false);
const pageSize = 20;
const isFirstLoad = ref(true); // 是否首次加载

// 当前日期范围
const currentDateRange = computed(() => {
  const now = dayjs();
  if (dateMode.value === "month") {
    return [
      now.startOf("month").format("YYYY-MM-DD"),
      now.endOf("month").format("YYYY-MM-DD")
    ];
  } else if (dateMode.value === "year") {
    return [
      now.startOf("year").format("YYYY-MM-DD"),
      now.endOf("year").format("YYYY-MM-DD")
    ];
  }
  return null;
});

// 日期显示文本
const dateDisplayText = computed(() => {
  if (dateMode.value === "month") {
    return t("mobile.bill.thisMonth");
  } else if (dateMode.value === "year") {
    return t("mobile.bill.thisYear");
  }
  // 自定义模式下显示日期范围
  if (dateMode.value === "custom") {
    // 从 billStore 的查询参数中获取日期
    const dateParam = billStore.queryParams.date;
    if (dateParam && dateParam.length === 2) {
      return `${dateParam[0].slice(0, 10)} ~ ${dateParam[1].slice(0, 10)}`;
    }
  }
  return t("mobile.bill.custom");
});

// 日期快捷选择选项
const dateActions = computed(() => [
  { name: t("mobile.bill.thisMonth") },
  { name: t("mobile.bill.thisYear") },
  { name: t("mobile.bill.custom") }
]);

// 按日期分组的账单数据
interface DayGroup {
  date: string;
  weekday: string;
  dayExpense: number;
  dayIncome: number;
  records: IncomeExpense[];
}

const weekDayMap: Record<string, string> = {
  Sun: "周日",
  Mon: "周一",
  Tue: "周二",
  Wed: "周三",
  Thu: "周四",
  Fri: "周五",
  Sat: "周六"
};

const groupedBills = computed<DayGroup[]>(() => {
  const groups: Map<string, DayGroup> = new Map();

  billStore.list.forEach(record => {
    const dateStr = record.date?.split(" ")[0] || record.date;
    if (!dateStr) return;

    if (!groups.has(dateStr)) {
      const date = dayjs(dateStr);
      const weekEn = date.format("ddd");
      const weekday =
        t("mobile.language.zh") === "中文" ? weekDayMap[weekEn] : weekEn;
      groups.set(dateStr, {
        date: dateStr,
        weekday,
        dayExpense: 0,
        dayIncome: 0,
        records: []
      });
    }

    const group = groups.get(dateStr)!;
    group.records.push(record);

    if (record.type === "EXPENSE") {
      group.dayExpense += record.amount;
    } else {
      group.dayIncome += record.amount;
    }
  });

  // 按日期降序排列
  return Array.from(groups.values()).sort(
    (a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()
  );
});

// 加载账单数据
const loadBills = async (append = false) => {
  const userId = userStore.id;
  if (!userId) return;

  if (!append) {
    showLoading(t("mobile.common.loading"));
    // 设置日期范围（仅在非追加模式下设置）
    if (currentDateRange.value) {
      billStore.setQueryParams({
        date: currentDateRange.value
      });
    }
  }
  try {
    const prevLength = billStore.list.length;
    await billStore.loadList(userId, append);
    const newLength = billStore.list.length;

    // 本次加载的数据量
    const loadedCount = newLength - prevLength;
    // 如果本次加载的数据量小于页大小，说明没有更多数据了
    if (loadedCount < pageSize) {
      finished.value = true;
    }
  } catch (error: any) {
    showError(error?.message || t("mobile.bill.loadFailed"));
    finished.value = true;
  } finally {
    if (!append) {
      hideLoading();
    }
    loading.value = false;
  }
};

// 加载更多
const onLoadMore = async () => {
  // 首次加载由 initData 触发，这里跳过
  if (isFirstLoad.value || loading.value || finished.value) return;

  loading.value = true;
  billStore.queryParams.pageNo += 1;
  await loadBills(true);
};

// 重置并加载
const resetAndLoad = async () => {
  billStore.queryParams.pageNo = 1;
  billStore.list = [];
  finished.value = false;
  loading.value = true;
  isFirstLoad.value = true;
  await loadBills(false);
};

// 初始化数据
const initData = async () => {
  const userId = userStore.id;
  if (!userId) return;

  showLoading(t("mobile.common.loading"));
  try {
    // 加载分类数据
    if (classifyStore.list.length === 0) {
      await classifyStore.fetchList(userId);
    }
    // 加载标签数据
    if (userTagStore.list.length === 0) {
      await userTagStore.fetchList(userId);
    }
    // 加载备注数据
    if (remarkStore.list.length === 0) {
      await remarkStore.fetchList(userId);
    }
    // 加载账本数据
    if (accountBookStore.list.length === 0) {
      await accountBookStore.fetchList(userId);
    }
    // 设置当前账本
    if (accountBookStore.defaultAccountBook) {
      billStore.setCurrentAccountBook(accountBookStore.defaultAccountBook);
    }
    // 加载账单列表
    await loadBills();
    isFirstLoad.value = false;
  } catch (error: any) {
    showError(error?.message || t("mobile.common.failed"));
  } finally {
    hideLoading();
  }
};

// 日期模式切换
const handleDateModeChange = (mode: "month" | "year" | "custom") => {
  dateMode.value = mode;
  showDateAction.value = false;

  if (mode === "custom") {
    showCalendar.value = true;
  } else {
    billStore.resetQueryParams();
    resetAndLoad();
  }
};

// 日期快捷选择
const handleDateActionSelect = (action: { name: string }) => {
  const modeMap: Record<string, "month" | "year" | "custom"> = {
    [t("mobile.bill.thisMonth")]: "month",
    [t("mobile.bill.thisYear")]: "year",
    [t("mobile.bill.custom")]: "custom"
  };
  const mode = modeMap[action.name];
  if (mode) {
    handleDateModeChange(mode);
  }
};

// 日历确认
const handleCalendarConfirm = (values: Date[]) => {
  const startDate = dayjs(values[0]).format("YYYY-MM-DD");
  const endDate = dayjs(values[1]).format("YYYY-MM-DD");
  billStore.setQueryParams({
    date: [startDate, endDate]
  });
  showCalendar.value = false;
  resetAndLoad();
};

// 打开筛选弹窗
const handleOpenFilter = () => {
  showFilter.value = true;
};

// 筛选确认
const handleFilterConfirm = (filters: any) => {
  showFilter.value = false;
  billStore.setQueryParams(filters);
  resetAndLoad();
};

// 筛选重置
const handleFilterReset = () => {
  billStore.resetQueryParams();
  resetAndLoad();
};

// 点击账单项 - 编辑
const handleBillClick = (record: IncomeExpense) => {
  // 直接传递数据，不需要重新获取详情
  sessionStorage.setItem("editRecordData", JSON.stringify(record));
  router.push(`/record/${record.id}`);
};

// 删除账单
const handleDelete = async (id: number) => {
  try {
    await showConfirmDialog({
      message: t("mobile.bill.deleteConfirm"),
      confirmButtonColor: "#d83d34"
    });

    showLoading(t("mobile.common.loading"));
    await deleteIncomeExpense([id]);
    hideLoading();

    showSuccess(t("mobile.bill.deleteSuccess"));
    await resetAndLoad();
  } catch {
    hideLoading();
  }
};

// 新增账单
const handleAdd = () => {
  router.push("/record");
};

// 获取分类图标
const getIcon = (record: IncomeExpense) => {
  return getClassifyIcon(
    record.subClassifyImage || record.mainClassifyImage || "other"
  );
};

// 获取分类名称
const getClassifyName = (record: IncomeExpense) => {
  const main = record.mainClassifyName || "";
  const sub = record.subClassifyName;
  return sub ? `${main}-${sub}` : main;
};

// 格式化日期显示
const formatDateDisplay = (dateStr: string) => {
  const date = dayjs(dateStr);
  return `${date.month() + 1}月${date.date()}日`;
};

// 监听账本切换
watch(
  () => billStore.currentAccountBook,
  () => {
    if (userStore.id) {
      resetAndLoad();
    }
  }
);

onMounted(() => {
  initData();
});
</script>

<template>
  <div class="bill-page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="date-selector" @click="showDateAction = true">
        <span class="date-text">{{ dateDisplayText }}</span>
        <van-icon name="arrow-down" />
      </div>
      <van-button size="small" icon="filter-o" @click="handleOpenFilter">
        {{ t("mobile.bill.filter") }}
      </van-button>
    </div>

    <!-- 账单列表 -->
    <van-list
      v-model:loading="loading"
      :finished="finished"
      :finished-text="groupedBills.length > 0 ? t('mobile.common.noData') : ''"
      @load="onLoadMore"
    >
      <div class="bill-list">
        <div v-for="group in groupedBills" :key="group.date" class="day-group">
          <!-- 日期标题 -->
          <div class="group-header">
            <div class="date-info">
              <span class="date">{{ formatDateDisplay(group.date) }}</span>
              <span class="weekday">{{ group.weekday }}</span>
            </div>
            <div class="day-summary">
              <span v-if="group.dayExpense > 0" class="expense">
                -¥{{ formatNumber(group.dayExpense) }}
              </span>
              <span v-if="group.dayIncome > 0" class="income">
                +¥{{ formatNumber(group.dayIncome) }}
              </span>
            </div>
          </div>

          <!-- 当日账单项 -->
          <div class="group-records">
            <van-swipe-cell v-for="record in group.records" :key="record.id">
              <div class="bill-item" @click="handleBillClick(record)">
                <div class="item-icon">{{ getIcon(record) }}</div>
                <div class="item-info">
                  <div class="classify-name">{{ getClassifyName(record) }}</div>
                  <div v-if="record.remark" class="remark">
                    {{ record.remark }}
                  </div>
                  <div v-else class="remark placeholder">
                    {{ t("mobile.bill.noRemark") }}
                  </div>
                </div>
                <div
                  class="item-amount"
                  :class="record.type === 'EXPENSE' ? 'expense' : 'income'"
                >
                  ¥{{ formatNumber(Math.abs(record.amount)) }}
                </div>
              </div>
              <template #right>
                <van-button
                  square
                  type="danger"
                  text="删除"
                  class="delete-btn"
                  @click="handleDelete(record.id)"
                />
              </template>
            </van-swipe-cell>
          </div>
        </div>

        <!-- 空状态 -->
        <van-empty
          v-if="groupedBills.length === 0 && !billStore.listLoading"
          :description="t('mobile.bill.noData')"
        />
      </div>
    </van-list>

    <!-- 悬浮记账按钮 -->
    <div class="floating-btn" @click="handleAdd">
      <van-icon name="plus" size="24" />
    </div>

    <!-- 日期快捷选择 -->
    <van-action-sheet
      v-model:show="showDateAction"
      :actions="dateActions"
      @select="handleDateActionSelect"
    />

    <!-- 日期范围选择器 -->
    <van-calendar
      v-model:show="showCalendar"
      type="range"
      :min-date="new Date(2020, 0, 1)"
      :max-date="new Date()"
      show-confirm
      :confirm-text="t('mobile.common.confirm')"
      position="bottom"
      round
      @confirm="handleCalendarConfirm"
    />

    <!-- 筛选弹窗 -->
    <van-popup
      v-model:show="showFilter"
      position="bottom"
      round
      :style="{ height: '80%' }"
    >
      <BillFilter
        :classify-list="classifyStore.list"
        :tag-list="userTagStore.list"
        :remark-list="remarkStore.list"
        @confirm="handleFilterConfirm"
        @reset="handleFilterReset"
      />
    </van-popup>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.bill-page {
  min-height: 100vh;
  padding-bottom: calc(60px + env(safe-area-inset-bottom));
  background-color: $color-background;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: $color-card;
  border-bottom: 1px solid $color-border;
}

.date-selector {
  display: flex;
  gap: 4px;
  align-items: center;
  cursor: pointer;

  .date-text {
    font-size: 14px;
    font-weight: 500;
    color: $color-text-primary;
  }
}

.bill-list {
  padding: 12px;
}

.day-group {
  margin-bottom: 12px;
  background-color: $color-card;
  border-radius: 12px;
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid $color-border;
}

.date-info {
  display: flex;
  gap: 8px;
  align-items: center;

  .date {
    font-size: 14px;
    font-weight: 500;
    color: $color-text-primary;
  }

  .weekday {
    font-size: 12px;
    color: $color-text-secondary;
  }
}

.day-summary {
  display: flex;
  gap: 8px;

  .expense {
    font-size: 12px;
    color: $color-primary;
  }

  .income {
    font-size: 12px;
    color: $color-secondary;
  }
}

.group-records {
  padding: 0;
}

.bill-item {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid $color-border;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background-color: rgba(0, 0, 0, 0.05);
  }
}

.item-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  font-size: 22px;
  background-color: $color-background;
  border-radius: 8px;
}

.item-info {
  flex: 1;
  min-width: 0;

  .classify-name {
    overflow: hidden;
    font-size: 14px;
    font-weight: 500;
    color: $color-text-primary;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .remark {
    margin-top: 4px;
    overflow: hidden;
    font-size: 12px;
    color: $color-text-secondary;
    text-overflow: ellipsis;
    white-space: nowrap;

    &.placeholder {
      color: $color-text-placeholder;
    }
  }
}

.item-amount {
  font-size: 14px;
  font-weight: 500;

  &.expense {
    color: $color-primary;
  }

  &.income {
    color: $color-secondary;
  }
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

.delete-btn {
  height: 100% !important;
}
</style>
