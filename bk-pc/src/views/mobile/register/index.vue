<script setup lang="ts">
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { register } from "@/api/auth";
import { storageLocal } from "@pureadmin/utils";
import { responsiveStorageNameSpace } from "@/config";
import {
  showError,
  showSuccess,
  showLoading,
  hideLoading
} from "@/utils/mobile/message";

defineOptions({
  name: "MobileRegister"
});

const router = useRouter();
const { t, locale } = useI18n();

// 语言选项
const languageOptions = [
  { value: "zh", label: "中文" },
  { value: "en", label: "English" }
];

// 当前语言显示文本
const currentLanguage = computed(() => {
  return (
    languageOptions.find(opt => opt.value === locale.value)?.label || "中文"
  );
});

// 语言切换弹出框显示状态
const showLanguagePopover = ref(false);

// 切换语言
const switchLanguage = (lang: string) => {
  locale.value = lang;
  storageLocal().setItem(`${responsiveStorageNameSpace()}locale`, {
    locale: lang
  });
  showLanguagePopover.value = false;
};

// 表单数据
const formData = ref({
  username: "",
  password: "",
  repeatPassword: ""
});

// 加载状态
const loading = ref(false);

// 表单验证
const validate = () => {
  if (!formData.value.username.trim()) {
    showError(t("mobile.register.usernameRequired"));
    return false;
  }
  if (
    formData.value.username.length < 3 ||
    formData.value.username.length > 20
  ) {
    showError(t("mobile.register.usernameLength"));
    return false;
  }
  if (!/^[a-zA-Z0-9_]+$/.test(formData.value.username)) {
    showError(t("mobile.register.usernameFormat"));
    return false;
  }
  if (!formData.value.password) {
    showError(t("mobile.register.passwordRequired"));
    return false;
  }
  if (
    formData.value.password.length < 6 ||
    formData.value.password.length > 20
  ) {
    showError(t("mobile.register.passwordLength"));
    return false;
  }
  if (!formData.value.repeatPassword) {
    showError(t("mobile.register.confirmPasswordRequired"));
    return false;
  }
  if (formData.value.password !== formData.value.repeatPassword) {
    showError(t("mobile.register.passwordNotMatch"));
    return false;
  }
  return true;
};

// 注册
const handleRegister = async () => {
  if (!validate()) return;

  loading.value = true;
  showLoading(t("mobile.common.loading"));

  try {
    await register({
      username: formData.value.username,
      password: formData.value.password,
      repeatPassword: formData.value.repeatPassword
    });

    hideLoading();
    showSuccess(t("mobile.register.success"));

    // 注册成功后跳转登录页
    setTimeout(() => {
      router.replace("/login");
    }, 1500);
  } catch (error: any) {
    hideLoading();
    const errMsg = error?.message || t("mobile.register.failed");
    showError(errMsg);
  } finally {
    loading.value = false;
  }
};

// 跳转登录
const goLogin = () => {
  router.push("/login");
};
</script>

<template>
  <div class="register-page">
    <!-- 语言切换 -->
    <div class="language-switch">
      <van-popover
        v-model:show="showLanguagePopover"
        placement="bottom-end"
        :actions="
          languageOptions.map(opt => ({ text: opt.label, value: opt.value }))
        "
        @select="(action: any) => switchLanguage(action.value)"
      >
        <template #reference>
          <van-button size="small" type="default">
            {{ currentLanguage }}
            <van-icon name="arrow-down" />
          </van-button>
        </template>
      </van-popover>
    </div>

    <!-- 页面标题 -->
    <div class="page-title">
      <h2>{{ t("mobile.register.title") }}</h2>
    </div>

    <!-- 表单区域 -->
    <div class="form-container">
      <van-cell-group inset>
        <van-field
          v-model="formData.username"
          :placeholder="t('mobile.register.usernamePlaceholder')"
          left-icon="user-o"
          clearable
        />
        <van-field
          v-model="formData.password"
          type="password"
          :placeholder="t('mobile.register.passwordPlaceholder')"
          left-icon="lock"
          show-password-on="click"
        />
        <van-field
          v-model="formData.repeatPassword"
          type="password"
          :placeholder="t('mobile.register.confirmPasswordPlaceholder')"
          left-icon="lock"
          show-password-on="click"
        />
      </van-cell-group>

      <!-- 注册按钮 -->
      <van-button
        type="primary"
        block
        round
        :loading="loading"
        class="register-btn"
        @click="handleRegister"
      >
        {{ t("mobile.register.registerBtn") }}
      </van-button>

      <!-- 登录提示 -->
      <div class="login-tip">
        <span>{{ t("mobile.register.hasAccount") }}</span>
        <span class="link" @click="goLogin">{{
          t("mobile.register.goLogin")
        }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.register-page {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 16px;
  background-color: $color-background;
}

.language-switch {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;

  :deep(.van-button) {
    height: 28px;
    padding: 0 8px;
    font-size: 12px;
  }
}

.page-title {
  margin-bottom: 40px;

  h2 {
    margin: 0;
    font-size: 22px;
    font-weight: 600;
    color: #323233;
  }
}

.form-container {
  width: 100%;
  max-width: 325px;

  :deep(.van-cell-group--inset) {
    margin: 0;
    overflow: hidden;
    border-radius: 8px;
  }

  :deep(.van-field) {
    padding: 12px 16px;

    .van-field__left-icon {
      margin-right: 8px;
      color: #969799;
    }
  }
}

.register-btn {
  height: 44px;
  margin-top: 24px;
  font-size: 16px;
  background-color: $color-primary;
  border-color: $color-primary;
}

.login-tip {
  margin-top: 16px;
  font-size: 13px;
  color: #969799;
  text-align: center;

  .link {
    margin-left: 4px;
    color: $color-primary;
    cursor: pointer;
  }
}
</style>
