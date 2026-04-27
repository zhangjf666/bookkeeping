<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { useUserStoreHook } from "@/store/modules/user";
import { getUserConfigList, updateUserConfig } from "@/api/userConfig";
import type { UserConfig, UserConfigForm } from "@/types/userConfig";
import { showLoading, hideLoading, showError } from "@/utils/mobile/message";

defineOptions({
  name: "MobileCommonSettings"
});

const { t } = useI18n();
const userStore = useUserStoreHook();

// 配置列表
const configList = ref<UserConfig[]>([]);

// 信用卡默认开关
const isCreditCard = ref<"YES" | "NO">("NO");

// 支出限额显示类型：1=不显示，2=月限额，3=年限额
const showExpenseLimitType = ref<"1" | "2" | "3">("1");

// 月支出限额
const monthlyExpenseLimit = ref("");

// 年支出限额
const yearlyExpenseLimit = ref("");

// 弹窗状态
const showLimitTypePicker = ref(false);
const showMonthlyLimitEditor = ref(false);
const showYearlyLimitEditor = ref(false);

// 编辑临时数据
const editMonthlyLimit = ref("");
const editYearlyLimit = ref("");

// 输入框引用
const monthlyLimitInput = ref<{ $el: HTMLElement } | null>(null);
const yearlyLimitInput = ref<{ $el: HTMLElement } | null>(null);

// 加载状态
const loading = ref(false);

// 聚焦输入框并将光标移到末尾
const focusInput = (inputRef: typeof monthlyLimitInput, value: string) => {
  setTimeout(() => {
    const input = inputRef.value?.$el?.querySelector(
      "input"
    ) as HTMLInputElement | null;
    if (input) {
      input.focus();
      const len = value.length;
      input.setSelectionRange(len, len);
    }
  }, 100);
};

// 支出限额显示类型选项
const limitTypeOptions = computed(() => [
  { value: "1", label: t("mobile.commonSettings.notDisplay") },
  { value: "2", label: t("mobile.commonSettings.monthlyLimit") },
  { value: "3", label: t("mobile.commonSettings.yearlyLimit") }
]);

// 支出限额显示类型文本
const limitTypeText = computed(() => {
  const option = limitTypeOptions.value.find(
    item => item.value === showExpenseLimitType.value
  );
  return option?.label || "";
});

// 根据名称获取配置
const getConfigByName = (name: string) => {
  return configList.value.find(c => c.name === name);
};

// 加载配置
const loadConfigs = async () => {
  if (!userStore.id) return;

  loading.value = true;
  try {
    const result = await getUserConfigList(userStore.id);
    configList.value = result || [];

    // 信用卡默认配置
    const creditCardConfig = getConfigByName("is_credit_card");
    if (creditCardConfig) {
      isCreditCard.value = creditCardConfig.value === "1" ? "YES" : "NO";
    }

    // 支出限额显示类型
    const showLimitConfig = getConfigByName("show_expense_limit");
    if (showLimitConfig) {
      showExpenseLimitType.value = showLimitConfig.value as "1" | "2" | "3";
    }

    // 月支出限额
    const monthlyLimitConfig = getConfigByName("default_monthly_expense_limit");
    if (monthlyLimitConfig) {
      monthlyExpenseLimit.value = monthlyLimitConfig.value;
    }

    // 年支出限额
    const yearlyLimitConfig = getConfigByName("default_yearly_expense_limit");
    if (yearlyLimitConfig) {
      yearlyExpenseLimit.value = yearlyLimitConfig.value;
    }
  } catch {
    configList.value = [];
  } finally {
    loading.value = false;
  }
};

// 更新配置值
const updateConfigValue = async (name: string, value: string) => {
  const config = getConfigByName(name);
  if (!config) return;

  try {
    const data: UserConfigForm = {
      id: config.id,
      userId: config.userId,
      name: config.name,
      value: value,
      description: config.description,
      enable: config.enable
    };

    await updateUserConfig(data);
    // 重新加载配置
    await loadConfigs();
  } catch (error: any) {
    showError(error?.message || t("mobile.common.failed"));
  }
};

// 信用卡默认开关变化
const handleCreditCardChange = async (value: "YES" | "NO") => {
  const valueToSave = value === "YES" ? "1" : "0";
  await updateConfigValue("is_credit_card", valueToSave);
};

// 选择支出限额显示类型
const handleLimitTypeSelect = async (value: "1" | "2" | "3") => {
  showLimitTypePicker.value = false;
  if (value === showExpenseLimitType.value) return;
  await updateConfigValue("show_expense_limit", value);
};

// 打开月限额编辑器
const openMonthlyLimitEditor = () => {
  editMonthlyLimit.value = monthlyExpenseLimit.value;
  showMonthlyLimitEditor.value = true;
  focusInput(monthlyLimitInput, editMonthlyLimit.value);
};

// 确认月限额修改
const handleMonthlyLimitConfirm = async () => {
  const value = editMonthlyLimit.value.trim();
  await updateConfigValue("default_monthly_expense_limit", value || "");
  showMonthlyLimitEditor.value = false;
};

// 打开年限额编辑器
const openYearlyLimitEditor = () => {
  editYearlyLimit.value = yearlyExpenseLimit.value;
  showYearlyLimitEditor.value = true;
  focusInput(yearlyLimitInput, editYearlyLimit.value);
};

// 确认年限额修改
const handleYearlyLimitConfirm = async () => {
  const value = editYearlyLimit.value.trim();
  await updateConfigValue("default_yearly_expense_limit", value || "");
  showYearlyLimitEditor.value = false;
};

onMounted(async () => {
  showLoading(t("mobile.common.loading"));
  try {
    await loadConfigs();
  } catch (error: any) {
    showError(error?.message || t("mobile.common.failed"));
  } finally {
    hideLoading();
  }
});
</script>

<template>
  <div class="common-settings-page">
    <!-- 常用设置 -->
    <van-cell-group inset class="settings-group">
      <van-cell
        :title="t('mobile.commonSettings.commonGroup')"
        class="group-title"
      />
      <van-cell center :title="t('mobile.commonSettings.defaultCreditCard')">
        <template #right-icon>
          <van-switch
            v-model="isCreditCard"
            active-value="YES"
            inactive-value="NO"
            size="20"
            active-color="#d83d34"
            @change="handleCreditCardChange"
          />
        </template>
      </van-cell>
    </van-cell-group>

    <!-- 限额配置 -->
    <van-cell-group inset class="settings-group">
      <van-cell
        :title="t('mobile.commonSettings.limitGroup')"
        class="group-title"
      />
      <van-cell
        :title="t('mobile.commonSettings.showExpenseLimit')"
        :value="limitTypeText"
        is-link
        @click="showLimitTypePicker = true"
      />
      <van-cell
        :title="t('mobile.commonSettings.monthlyExpenseLimit')"
        is-link
        @click="openMonthlyLimitEditor"
      >
        <template #value>
          <span v-if="monthlyExpenseLimit" class="amount-value">
            ¥{{ monthlyExpenseLimit }}
          </span>
          <span v-else class="placeholder">{{
            t("mobile.commonSettings.amountPlaceholder")
          }}</span>
        </template>
      </van-cell>
      <van-cell
        :title="t('mobile.commonSettings.yearlyExpenseLimit')"
        is-link
        @click="openYearlyLimitEditor"
      >
        <template #value>
          <span v-if="yearlyExpenseLimit" class="amount-value">
            ¥{{ yearlyExpenseLimit }}
          </span>
          <span v-else class="placeholder">{{
            t("mobile.commonSettings.amountPlaceholder")
          }}</span>
        </template>
      </van-cell>
    </van-cell-group>

    <!-- 支出限额显示类型选择 -->
    <van-action-sheet
      v-model:show="showLimitTypePicker"
      :title="t('mobile.commonSettings.showExpenseLimit')"
    >
      <div class="picker-list">
        <van-cell
          v-for="item in limitTypeOptions"
          :key="item.value"
          :title="item.label"
          clickable
          @click="handleLimitTypeSelect(item.value as '1' | '2' | '3')"
        >
          <template #right-icon>
            <van-icon
              v-if="showExpenseLimitType === item.value"
              name="success"
              color="#d83d34"
            />
          </template>
        </van-cell>
      </div>
    </van-action-sheet>

    <!-- 月限额编辑弹窗 -->
    <van-dialog
      v-model:show="showMonthlyLimitEditor"
      show-confirm-button
      close-on-click-overlay
      @confirm="handleMonthlyLimitConfirm"
    >
      <template #title>
        <div class="dialog-header">
          <span>{{ t("mobile.commonSettings.monthlyExpenseLimit") }}</span>
          <van-icon
            name="cross"
            class="close-icon"
            @click="showMonthlyLimitEditor = false"
          />
        </div>
      </template>
      <div class="dialog-content">
        <van-field
          ref="monthlyLimitInput"
          v-model="editMonthlyLimit"
          type="number"
          :placeholder="t('mobile.commonSettings.amountPlaceholder')"
        >
          <template #left-icon>
            <span class="currency">¥</span>
          </template>
        </van-field>
      </div>
    </van-dialog>

    <!-- 年限额编辑弹窗 -->
    <van-dialog
      v-model:show="showYearlyLimitEditor"
      show-confirm-button
      close-on-click-overlay
      @confirm="handleYearlyLimitConfirm"
    >
      <template #title>
        <div class="dialog-header">
          <span>{{ t("mobile.commonSettings.yearlyExpenseLimit") }}</span>
          <van-icon
            name="cross"
            class="close-icon"
            @click="showYearlyLimitEditor = false"
          />
        </div>
      </template>
      <div class="dialog-content">
        <van-field
          ref="yearlyLimitInput"
          v-model="editYearlyLimit"
          type="number"
          :placeholder="t('mobile.commonSettings.amountPlaceholder')"
        >
          <template #left-icon>
            <span class="currency">¥</span>
          </template>
        </van-field>
      </div>
    </van-dialog>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.common-settings-page {
  min-height: 100vh;
  background-color: $color-background;
}

.settings-group {
  margin: 12px 0;

  &:first-child {
    margin-top: 0;
  }
}

.group-title {
  font-weight: 500;
  color: $color-text-primary;
  background-color: $color-card;
}

.amount-value {
  color: $color-text-primary;
}

.placeholder {
  color: $color-text-secondary;
}

.picker-list {
  padding-bottom: env(safe-area-inset-bottom);
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 0 16px;

  span {
    flex: 1;
    text-align: left;
  }

  .close-icon {
    font-size: 18px;
    color: #969799;
    cursor: pointer;
  }
}

.dialog-content {
  padding: 16px;

  :deep(.van-field) {
    padding: 10px 12px;
    border: 1px solid #ebedf0;
    border-radius: 4px;

    &:focus-within {
      border-color: #d83d34;
    }
  }
}

.currency {
  font-size: 14px;
  color: $color-text-primary;
}
</style>
