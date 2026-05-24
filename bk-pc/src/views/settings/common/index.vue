<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
import { useUserStoreHook } from "@/store/modules/user";
import { getUserConfigList, updateUserConfig } from "@/api/userConfig";
import { useUserConfig } from "@/composables/useUserConfig";
import type { UserConfig, UserConfigForm } from "@/types/userConfig";

defineOptions({
  name: "CommonConfigSetting"
});

const { t } = useI18n();
const userStore = useUserStoreHook();

const loading = ref(false);
const configList = ref<UserConfig[]>([]);

const isCreditCard = ref<"YES" | "NO">("NO");
const showExpenseLimit = ref<"1" | "2" | "3">("1");
const defaultMonthlyExpenseLimit = ref<string>("");
const defaultYearlyExpenseLimit = ref<string>("");

const limitModeOptions = [
  { value: "1", label: t("commonConfig.pureNotDisplay") },
  { value: "2", label: t("commonConfig.pureMonthlyLimit") },
  { value: "3", label: t("commonConfig.pureYearlyLimit") }
];

const { saveConfig } = useUserConfig();

const theme = ref("system");
const language = ref("system");

const themeOptions = [
  { value: "system", label: t("commonConfig.pureThemeSystem") },
  { value: "light", label: t("commonConfig.pureThemeLight") },
  { value: "dark", label: t("commonConfig.pureThemeDark") }
];

const languageOptions = [
  { value: "system", label: t("commonConfig.pureLanguageSystem") },
  { value: "zh", label: t("commonConfig.pureLanguageZh") },
  { value: "en", label: t("commonConfig.pureLanguageEn") }
];

const loadData = async () => {
  if (!userStore.id) return;
  loading.value = true;
  try {
    const result = await getUserConfigList(userStore.id);
    configList.value = result || [];

    const creditCardConfig = configList.value.find(
      c => c.name === "is_credit_card"
    );
    if (creditCardConfig) {
      isCreditCard.value = creditCardConfig.value === "1" ? "YES" : "NO";
    }

    const showLimitConfig = configList.value.find(
      c => c.name === "show_expense_limit"
    );
    if (showLimitConfig) {
      showExpenseLimit.value = showLimitConfig.value as "1" | "2" | "3";
    }

    const monthlyLimitConfig = configList.value.find(
      c => c.name === "default_monthly_expense_limit"
    );
    if (monthlyLimitConfig) {
      defaultMonthlyExpenseLimit.value = monthlyLimitConfig.value;
    }

    const yearlyLimitConfig = configList.value.find(
      c => c.name === "default_yearly_expense_limit"
    );
    if (yearlyLimitConfig) {
      defaultYearlyExpenseLimit.value = yearlyLimitConfig.value;
    }

    const themeConfig = configList.value.find(c => c.name === "theme");
    if (themeConfig) {
      theme.value = themeConfig.value;
    }
    const languageConfig = configList.value.find(c => c.name === "language");
    if (languageConfig) {
      language.value = languageConfig.value;
    }
  } catch {
    configList.value = [];
  } finally {
    loading.value = false;
  }
};

const getConfigByName = (name: string) => {
  return configList.value.find(c => c.name === name);
};

const handleCreditCardChange = async (value: "YES" | "NO") => {
  const config = getConfigByName("is_credit_card");
  if (!config) return;

  const valueToSave = value === "YES" ? "1" : "0";
  await updateConfigValue(
    config.id,
    config.userId,
    "is_credit_card",
    valueToSave
  );
};

const handleShowExpenseLimitChange = async (value: "1" | "2" | "3") => {
  const config = getConfigByName("show_expense_limit");
  if (!config) return;

  await updateConfigValue(
    config.id,
    config.userId,
    "show_expense_limit",
    value
  );
};

const handleMonthlyLimitBlur = async () => {
  const value = defaultMonthlyExpenseLimit.value;
  const numValue = parseFloat(value);

  if (value && (isNaN(numValue) || numValue < 0)) {
    ElMessage.error(t("commonConfig.pureInvalidAmount"));
    return;
  }

  const config = getConfigByName("default_monthly_expense_limit");
  if (!config) return;

  await updateConfigValue(
    config.id,
    config.userId,
    "default_monthly_expense_limit",
    value || ""
  );
};

const handleYearlyLimitBlur = async () => {
  const value = defaultYearlyExpenseLimit.value;
  const numValue = parseFloat(value);

  if (value && (isNaN(numValue) || numValue < 0)) {
    ElMessage.error(t("commonConfig.pureInvalidAmount"));
    return;
  }

  const config = getConfigByName("default_yearly_expense_limit");
  if (!config) return;

  await updateConfigValue(
    config.id,
    config.userId,
    "default_yearly_expense_limit",
    value || ""
  );
};

const handleThemeChange = async (value: string) => {
  await saveConfig("theme", value);
};

const handleLanguageChange = async (value: string) => {
  await saveConfig("language", value);
};

const updateConfigValue = async (
  id: number,
  userId: number,
  name: string,
  value: string
) => {
  try {
    const config = getConfigByName(name);
    if (!config) return;

    const data: UserConfigForm = {
      id: config.id,
      userId: config.userId,
      name: config.name,
      value: value,
      description: config.description,
      enable: config.enable
    };

    await updateUserConfig(data);
    ElMessage.success(t("commonConfig.pureUpdateSuccess"));
    await loadData();
  } catch (error: any) {
    ElMessage.error(error?.message || t("commonConfig.pureOperationFail"));
  }
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="common-config-container">
    <el-card v-loading="loading" shadow="never">
      <div class="config-section">
        <div class="section-title">
          {{ t("commonConfig.pureGeneralConfig") }}
        </div>
        <div class="config-item">
          <span class="config-label">{{
            t("commonConfig.pureDefaultCreditCard")
          }}</span>
          <el-switch
            v-model="isCreditCard"
            active-value="YES"
            inactive-value="NO"
            @change="handleCreditCardChange"
          />
        </div>

        <div class="config-item">
          <span class="config-label">{{ t("commonConfig.pureTheme") }}</span>
          <el-radio-group v-model="theme" @change="handleThemeChange">
            <el-radio-button
              v-for="opt in themeOptions"
              :key="opt.value"
              :value="opt.value"
            >
              {{ opt.label }}
            </el-radio-button>
          </el-radio-group>
        </div>

        <div class="config-item">
          <span class="config-label">{{ t("commonConfig.pureLanguage") }}</span>
          <el-radio-group v-model="language" @change="handleLanguageChange">
            <el-radio-button
              v-for="opt in languageOptions"
              :key="opt.value"
              :value="opt.value"
            >
              {{ opt.label }}
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <el-divider />

      <div class="config-section">
        <div class="section-title">{{ t("commonConfig.pureLimitConfig") }}</div>

        <div class="config-item">
          <span class="config-label">{{
            t("commonConfig.pureShowExpenseLimit")
          }}</span>
          <el-radio-group
            v-model="showExpenseLimit"
            @change="handleShowExpenseLimitChange"
          >
            <el-radio-button value="1">{{
              t("commonConfig.pureNotDisplay")
            }}</el-radio-button>
            <el-radio-button value="2">{{
              t("commonConfig.pureMonthlyLimit")
            }}</el-radio-button>
            <el-radio-button value="3">{{
              t("commonConfig.pureYearlyLimit")
            }}</el-radio-button>
          </el-radio-group>
        </div>

        <div class="config-item">
          <span class="config-label">{{
            t("commonConfig.pureDefaultMonthlyLimit")
          }}</span>
          <div class="amount-input">
            <el-input
              v-model="defaultMonthlyExpenseLimit"
              :placeholder="t('commonConfig.pureAmountPlaceholder')"
              @blur="handleMonthlyLimitBlur"
            >
              <template #prefix>¥</template>
            </el-input>
          </div>
        </div>

        <div class="config-item">
          <span class="config-label">{{
            t("commonConfig.pureDefaultYearlyLimit")
          }}</span>
          <div class="amount-input">
            <el-input
              v-model="defaultYearlyExpenseLimit"
              :placeholder="t('commonConfig.pureAmountPlaceholder')"
              @blur="handleYearlyLimitBlur"
            >
              <template #prefix>¥</template>
            </el-input>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.common-config-container {
  padding: 16px;
}

.config-section {
  padding: 0 16px;
}

.section-title {
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.config-item {
  display: flex;
  align-items: center;
  margin-bottom: 24px;

  .config-label {
    width: 250px;
    color: #606266;
  }
}

.amount-input {
  width: 200px;

  :deep(.el-input__inner) {
    text-align: right;
  }
}
</style>
