<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { storeToRefs } from "pinia";
import dayjs from "dayjs";
import { useUserStoreHook } from "@/store/modules/user";
import { useAccountBookStore } from "@/store/modules/accountBook";
import { useUserTagStore } from "@/store/modules/userTag";
import { useRemarkStore } from "@/store/modules/remark";
import { useClassifyStore } from "@/store/modules/classify";
import { getIncomeExpenseById } from "@/api/incomeExpense";
import type { IncomeExpenseRecord } from "@/types/bill";
import {
  showLoading,
  hideLoading,
  showError
} from "@/utils/mobile/message";
import { formatNumber } from "@/utils/format";

defineOptions({
  name: "MobileRecordDetail"
});

const route = useRoute();
const router = useRouter();
const { t } = useI18n();

// 用户信息
const userStore = useUserStoreHook();
const userId = computed(() => userStore.id);

// Stores
const accountBookStore = useAccountBookStore();
const userTagStore = useUserTagStore();
const remarkStore = useRemarkStore();
const classifyStore = useClassifyStore();

// 路由参数
const recordId = computed(() => route.params.id as string);

// 加载状态
const loading = ref(false);

// 账单数据
const recordData = ref<IncomeExpenseRecord | null>(null);

// 账本列表
const { list: accountBookList } = storeToRefs(accountBookStore);
const { list: tagList } = storeToRefs(userTagStore);

// 账本名称
const accountBookName = computed(() => {
  if (!recordData.value) return "";
  const book = accountBookList.value.find(
    item => item.id === recordData.value!.accountBookId
  );
  return book?.name || "";
});

// 分类名称
const classifyName = computed(() => {
  if (!recordData.value) return "";
  const main = recordData.value.mainClassifyName || "";
  const sub = recordData.value.subClassifyName;
  return sub ? `${main}-${sub}` : main;
});

// 标签列表
const selectedTags = computed(() => {
  if (!recordData.value?.tagCodes) return [];
  const codes = recordData.value.tagCodes.split(",").map(c => Number(c.trim()));
  return tagList.value.filter(tag => codes.includes((tag as any).code));
});

// 加载账单数据
const loadRecordData = async () => {
  if (!recordId.value) return;

  loading.value = true;
  showLoading(t("mobile.common.loading"));

  try {
    // 优先使用 sessionStorage 传递的数据
    const storedData = sessionStorage.getItem("viewRecordData");
    if (storedData) {
      try {
        recordData.value = JSON.parse(storedData) as IncomeExpenseRecord;
        sessionStorage.removeItem("viewRecordData");
        hideLoading();
        loading.value = false;
        return;
      } catch (e) {
        console.error("解析存储数据失败:", e);
      }
    }

    // 如果没有存储数据，则调用接口获取
    const data = await getIncomeExpenseById(Number(recordId.value));
    recordData.value = data;
    hideLoading();
  } catch (error: any) {
    hideLoading();
    showError(error?.message || t("mobile.common.failed"));
    router.back();
  } finally {
    loading.value = false;
  }
};

// 返回
const handleBack = () => {
  router.back();
};

// 初始化
const init = async () => {
  if (!userId.value) return;

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
  if (classifyStore.list.length === 0) {
    promises.push(classifyStore.fetchList(userId.value));
  }
  if (promises.length > 0) {
    await Promise.all(promises);
  }

  // 加载账单数据
  await loadRecordData();
};

onMounted(() => {
  init();
});
</script>

<template>
  <div class="record-detail-page">
    <!-- 头部导航 -->
    <div class="header-nav">
      <van-icon name="arrow-left" size="20" @click="handleBack" />
      <span class="nav-title">{{ t("mobile.record.detailTitle") }}</span>
      <span class="nav-placeholder" />
    </div>

    <!-- 类型显示 -->
    <div class="type-display">
      <div
        class="type-tag"
        :class="recordData?.type === 'EXPENSE' ? 'expense' : 'income'"
      >
        {{ recordData?.type === "EXPENSE" ? t("mobile.record.expense") : t("mobile.record.income") }}
      </div>
    </div>

    <!-- 金额显示 -->
    <div class="amount-display">
      <div
        class="amount-value"
        :class="recordData?.type === 'EXPENSE' ? 'expense' : 'income'"
      >
        ¥{{ formatNumber(recordData?.amount || 0) }}
      </div>
    </div>

    <!-- 详情区域 -->
    <van-cell-group inset class="detail-group">
      <!-- 账本 -->
      <van-cell :title="t('mobile.record.accountBook')">
        <template #value>
          <span class="detail-value">{{ accountBookName || "-" }}</span>
        </template>
      </van-cell>

      <!-- 分类 -->
      <van-cell :title="t('mobile.record.classify')">
        <template #value>
          <span class="detail-value">{{ classifyName || "-" }}</span>
        </template>
      </van-cell>

      <!-- 日期 -->
      <van-cell :title="t('mobile.record.date')">
        <template #value>
          <span class="detail-value">{{ recordData?.date || "-" }}</span>
        </template>
      </van-cell>

      <!-- 备注 -->
      <van-cell :title="t('mobile.record.remark')">
        <template #value>
          <span class="detail-value">{{ recordData?.remark || "-" }}</span>
        </template>
      </van-cell>

      <!-- 标签 -->
      <van-cell :title="t('mobile.record.tag')">
        <template #value>
          <div v-if="selectedTags.length > 0" class="tags-display">
            <van-tag
              v-for="tag in selectedTags"
              :key="tag.id"
              :color="tag.color"
              text-color="#fff"
            >
              {{ tag.name }}
            </van-tag>
          </div>
          <span v-else class="detail-value">-</span>
        </template>
      </van-cell>

      <!-- 信用卡消费 -->
      <van-cell :title="t('mobile.record.isCreditCard')">
        <template #value>
          <span class="detail-value">
            {{ recordData?.isCreditCard === "YES" ? t("mobile.common.yes") : t("mobile.common.no") }}
          </span>
        </template>
      </van-cell>
    </van-cell-group>

    <!-- 加载中 -->
    <van-loading v-if="loading" class="page-loading" />
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.record-detail-page {
  min-height: 100vh;
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
  background-color: $color-background;
}

.header-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background-color: $color-card;
  border-bottom: 1px solid $color-border;

  .nav-title {
    font-size: 16px;
    font-weight: 500;
    color: $color-text-primary;
  }

  .nav-placeholder {
    width: 20px;
  }
}

.type-display {
  display: flex;
  justify-content: center;
  padding: 16px;
  background-color: $color-card;
}

.type-tag {
  padding: 6px 16px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 16px;

  &.expense {
    color: #f56c6c;
    background-color: rgba(#f56c6c, 0.1);
  }

  &.income {
    color: #67c23a;
    background-color: rgba(#67c23a, 0.1);
  }
}

.amount-display {
  display: flex;
  justify-content: center;
  padding: 16px;
  background-color: $color-card;
  border-bottom: 1px solid $color-border;
}

.amount-value {
  font-size: 32px;
  font-weight: 600;

  &.expense {
    color: #f56c6c;
  }

  &.income {
    color: #67c23a;
  }
}

.detail-group {
  margin: 16px;
}

.detail-value {
  font-size: 14px;
  color: $color-text-primary;
}

.tags-display {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: flex-end;
}

.page-loading {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
</style>
