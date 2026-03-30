<script setup lang="ts">
import { onMounted, ref, watch, computed } from "vue";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import { getUserConfigList, setAdditionalExpenseLimit } from "@/api/userConfig";
import { getSummary } from "@/api/incomeExpense";
import type { Summary } from "@/types/bill";
import dayjs from "dayjs";
import SummaryCards from "./components/SummaryCards.vue";
import TrendChart from "./components/TrendChart.vue";
import RecentRecords from "./components/RecentRecords.vue";
import AccountBookSelect from "./components/AccountBookSelect.vue";
import BillForm from "@/views/bill/components/BillForm.vue";
import Add from "~icons/ep/circle-plus-filled";
import Edit from "~icons/ep/edit";

defineOptions({
  name: "Dashboard"
});

const { t } = useI18n();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

const loading = ref(false);
const isManualSwitch = ref(false);
const showExpenseLimitMode = ref<"1" | "2" | "3">("1");
const showBillForm = ref(false);
const showLimitDialog = ref(false);
const limitForm = ref({
  expenseLimit: ""
});

const summaryData = ref<Summary | null>(null);

const loadData = async () => {
  const userId = userStore.id;
  if (!userId) {
    ElMessage.error("用户信息加载失败");
    return;
  }

  loading.value = true;
  try {
    const accountBookId = billStore.currentAccountBook?.id;
    const summary = await getSummary({ userId, accountBookId, days: 3 });
    summaryData.value = summary;
  } finally {
    loading.value = false;
  }
};

const loadAll = async () => {
  const userId = userStore.id;
  if (!userId) {
    ElMessage.error("用户信息加载失败");
    return;
  }

  loading.value = true;
  try {
    await Promise.all([
      billStore.loadAccountBooks(),
      billStore.loadClassifyAndTag(userId)
    ]);
    const accountBookId = billStore.currentAccountBook?.id;
    const summary = await getSummary({ userId, accountBookId, days: 3 });
    summaryData.value = summary;

    await loadUserConfig();
  } finally {
    loading.value = false;
  }
};

const loadUserConfig = async () => {
  const userId = userStore.id;
  if (!userId) return;

  try {
    const result = await getUserConfigList(userId);
    const configList = result || [];

    const showLimitConfig = configList.find(
      c => c.name === "show_expense_limit"
    );
    if (showLimitConfig) {
      showExpenseLimitMode.value = showLimitConfig.value as "1" | "2" | "3";
    }
  } catch {
    showExpenseLimitMode.value = "1";
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
  showBillForm.value = true;
};

const handleBillFormSuccess = () => {
  showBillForm.value = false;
  loadData();
};

const handleEditLimit = () => {
  const currentLimit = summaryData.value?.expenseLimit;
  limitForm.value.expenseLimit = currentLimit ? String(currentLimit) : "";
  showLimitDialog.value = true;
};

const handleLimitSave = async () => {
  const userId = userStore.id;
  if (!userId) return;

  const type = showExpenseLimitMode.value === "2" ? "MONTHLY" : "YEARLY";
  const expenseLimit = limitForm.value.expenseLimit;

  try {
    await setAdditionalExpenseLimit(userId, type, expenseLimit);
    ElMessage.success(t("commonConfig.pureUpdateSuccess"));
    showLimitDialog.value = false;
    await loadData();
  } catch (error: any) {
    ElMessage.error(error?.message || t("commonConfig.pureOperationFail"));
  }
};

const summaryParams = computed(() => ({
  showExpenseLimit: showExpenseLimitMode.value
}));
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
      <SummaryCards
        :data="summaryData"
        :loading="loading"
        :show-expense-limit="showExpenseLimitMode"
        @edit-limit="handleEditLimit"
      />
      <TrendChart
        :user-id="userStore.id"
        :account-book-id="billStore.currentAccountBook?.id"
      />
      <RecentRecords
        :records="summaryData?.incomeExpenseList || []"
        :loading="loading"
      />
    </el-skeleton>

    <el-dialog
      v-model="showBillForm"
      :title="t('bill.pureAddRecord')"
      width="600px"
      :close-on-click-modal="false"
    >
      <BillForm
        :data="null"
        @success="handleBillFormSuccess"
        @cancel="showBillForm = false"
      />
    </el-dialog>

    <el-dialog
      v-model="showLimitDialog"
      :title="
        showExpenseLimitMode === '2'
          ? t('commonConfig.pureSetMonthlyLimit')
          : t('commonConfig.pureSetYearlyLimit')
      "
      width="400px"
    >
      <el-form>
        <el-form-item
          :label="
            showExpenseLimitMode === '2'
              ? t('commonConfig.pureMonthlyLimit')
              : t('commonConfig.pureYearlyLimit')
          "
        >
          <el-input
            v-model="limitForm.expenseLimit"
            :placeholder="t('commonConfig.pureAmountPlaceholder')"
          >
            <template #prefix>¥</template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLimitDialog = false">{{
          t("bill.pureCancel")
        }}</el-button>
        <el-button type="primary" @click="handleLimitSave">{{
          t("bill.pureConfirm")
        }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
}

.dashboard-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}
</style>
