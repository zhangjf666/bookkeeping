<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { message } from "@/utils/message";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import SummaryCards from "./components/SummaryCards.vue";
import TrendChart from "./components/TrendChart.vue";
import RecentRecords from "./components/RecentRecords.vue";
import AccountBookSelect from "./components/AccountBookSelect.vue";
import Add from "~icons/ep/circle-plus-filled";

defineOptions({
  name: "Dashboard"
});

const router = useRouter();
const { t } = useI18n();

const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

const loading = ref(false);
const isManualSwitch = ref(false);

const loadData = async () => {
  const userId = userStore.id;
  if (!userId) {
    message("用户信息加载失败", { type: "error" });
    return;
  }

  loading.value = true;
  try {
    const accountBookId = billStore.currentAccountBook?.id;
    await Promise.all([
      billStore.loadSummary(userId, accountBookId),
      billStore.loadTrendData(userId, accountBookId, 7)
    ]);
  } finally {
    loading.value = false;
  }
};

const loadAll = async () => {
  const userId = userStore.id;
  if (!userId) {
    message("用户信息加载失败", { type: "error" });
    return;
  }

  loading.value = true;
  try {
    await Promise.all([
      billStore.loadAccountBooks(),
      billStore.loadClassifyAndTag(userId)
    ]);
    const accountBookId = billStore.currentAccountBook?.id;
    await Promise.all([
      billStore.loadSummary(userId, accountBookId),
      billStore.loadTrendData(userId, accountBookId, 7)
    ]);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadAll();
});

watch(
  () => billStore.currentAccountBook,
  (newVal, oldVal) => {
    if (oldVal && isManualSwitch.value) {
      loadData();
    }
    isManualSwitch.value = true;
  }
);

const handleQuickAdd = () => {
  router.push("/bill");
};
</script>

<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <div class="header-left">
        <AccountBookSelect />
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Add" @click="handleQuickAdd">
          {{ t("dashboard.pureAdd") }}
        </el-button>
      </div>
    </div>

    <el-skeleton :loading="loading" animated :rows="8">
      <SummaryCards :data="billStore.summary" :loading="loading" />
      <TrendChart
        :data="billStore.trendData"
        :loading="billStore.trendLoading"
      />
      <RecentRecords
        :records="billStore.summary?.incomeExpenseList || []"
        :loading="loading"
      />
    </el-skeleton>
  </div>
</template>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right {
  display: flex;
  align-items: center;
}
</style>
