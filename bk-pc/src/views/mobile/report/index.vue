<script setup lang="ts">
import { ref, computed, onMounted, onActivated, nextTick, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useUserStoreHook } from "@/store/modules/user";
import { useClassifyStoreHook } from "@/store/modules/classify";
import { useUserTagStoreHook } from "@/store/modules/userTag";
import { useRemarkStoreHook } from "@/store/modules/remark";
import { useAccountBookStoreHook } from "@/store/modules/accountBook";
import { useBillStoreHook } from "@/store/modules/bill";
import {
  showLoading,
  hideLoading,
  showError
} from "@/utils/mobile/message";
import ReportFilter, { type ReportFilterParams } from "./components/ReportFilter.vue";
import BillReport from "./components/BillReport.vue";
import ClassifyReport from "./components/ClassifyReport.vue";
import dayjs from "dayjs";

defineOptions({
  name: "MobileReport"
});

const { t } = useI18n();
const userStore = useUserStoreHook();
const classifyStore = useClassifyStoreHook();
const userTagStore = useUserTagStoreHook();
const remarkStore = useRemarkStoreHook();
const accountBookStore = useAccountBookStoreHook();
const billStore = useBillStoreHook();

// 当前 Tab
const activeTab = ref<"bill" | "classify">("bill");

// 组件引用
const billReportRef = ref<InstanceType<typeof BillReport> | null>(null);
const classifyReportRef = ref<InstanceType<typeof ClassifyReport> | null>(null);

// 筛选弹窗
const showFilter = ref(false);

// 筛选参数 - 初始时 accountBookId 为 undefined，等待 initData 设置
const filterParams = ref<ReportFilterParams>({
  billType: "month",
  beginDate: dayjs().startOf("month").format("YYYY-MM-DD"),
  endDate: dayjs().endOf("month").format("YYYY-MM-DD"),
  accountBookId: undefined
});

// 是否已初始化
const isInitialized = ref(false);

// 数据是否已准备好（用于控制子组件是否可以加载）
const dataReady = ref(false);

// 当前账本ID
const currentAccountBookId = computed(() => billStore.currentAccountBook?.id);

// 初始化数据
const initData = async (forceReset = false) => {
  const userId = userStore.id;
  if (!userId) return;

  if (isInitialized.value && !forceReset) {
    return;
  }

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

    // 设置筛选参数中的账本ID，并标记数据已准备好
    filterParams.value.accountBookId = currentAccountBookId.value;
    dataReady.value = true;

    isInitialized.value = true;
  } catch (error: any) {
    showError(error?.message || t("mobile.common.failed"));
  } finally {
    hideLoading();
  }
};

// 打开筛选弹窗
const handleOpenFilter = () => {
  showFilter.value = true;
};

// 筛选确认
const handleFilterConfirm = (params: ReportFilterParams) => {
  filterParams.value = params;
  showFilter.value = false;
};

// 筛选重置
const handleFilterReset = () => {
  filterParams.value = {
    billType: "month",
    beginDate: dayjs().startOf("month").format("YYYY-MM-DD"),
    endDate: dayjs().endOf("month").format("YYYY-MM-DD"),
    accountBookId: currentAccountBookId.value
  };
};

// 时间显示文本
const dateDisplayText = computed(() => {
  const params = filterParams.value;
  if (params.billType === "month") {
    const date = dayjs(params.beginDate);
    return `${date.year()}年${date.month() + 1}月`;
  } else if (params.billType === "year") {
    const date = dayjs(params.beginDate);
    return `${date.year()}年`;
  } else {
    return `${params.beginDate} ~ ${params.endDate}`;
  }
});

onMounted(() => {
  initData();
});

onActivated(() => {
  // 从子页面返回时不需要重新加载
});

// 监听 tab 切换，调整图表大小
watch(activeTab, async (newTab) => {
  await nextTick();
  if (newTab === "bill") {
    billReportRef.value?.resize();
  } else {
    classifyReportRef.value?.resize();
  }
});
</script>

<template>
  <div class="report-page">
    <!-- Tab 切换 -->
    <div class="tab-header">
      <div
        class="tab-item"
        :class="{ active: activeTab === 'bill' }"
        @click="activeTab = 'bill'"
      >
        {{ t("mobile.report.trend") }}
      </div>
      <div
        class="tab-item"
        :class="{ active: activeTab === 'classify' }"
        @click="activeTab = 'classify'"
      >
        {{ t("mobile.report.classify") }}
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="date-display" @click="handleOpenFilter">
        <span>{{ dateDisplayText }}</span>
        <van-icon name="arrow-down" />
      </div>
      <van-button size="small" icon="filter-o" @click="handleOpenFilter">
        {{ t("mobile.bill.filter") }}
      </van-button>
    </div>

    <!-- 内容区域 -->
    <div class="content-area">
      <BillReport
        ref="billReportRef"
        v-show="activeTab === 'bill'"
        :filter-params="filterParams"
      />
      <ClassifyReport
        ref="classifyReportRef"
        v-show="activeTab === 'classify'"
        :filter-params="filterParams"
      />
    </div>

    <!-- 筛选弹窗 -->
    <van-popup
      v-model:show="showFilter"
      position="bottom"
      round
      :style="{ height: '80%' }"
    >
      <ReportFilter
        :classify-list="classifyStore.list"
        :tag-list="userTagStore.list"
        :remark-list="remarkStore.list"
        :initial-params="filterParams"
        @confirm="handleFilterConfirm"
        @reset="handleFilterReset"
      />
    </van-popup>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.report-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: $color-background;
}

.tab-header {
  display: flex;
  background-color: $color-card;
  border-bottom: 1px solid $color-border;
}

.tab-item {
  flex: 1;
  padding: 14px 0;
  font-size: 14px;
  font-weight: 500;
  text-align: center;
  color: $color-text-secondary;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;

  &.active {
    color: $color-primary;
    border-bottom-color: $color-primary;
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: $color-card;
  border-bottom: 1px solid $color-border;
}

.date-display {
  display: flex;
  gap: 4px;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
  color: $color-text-primary;
  cursor: pointer;
}

.content-area {
  flex: 1;
  overflow-y: auto;
}
</style>
