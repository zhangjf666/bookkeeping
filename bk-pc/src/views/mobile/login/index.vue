<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { getCaptcha } from "@/api/auth";
import { useUserStoreHook } from "@/store/modules/user";
import { usePermissionStoreHook } from "@/store/modules/permission";
import { addPathMatch, getTopMenu } from "@/router/utils";
import { useUserConfig } from "@/composables/useUserConfig";
import { storageLocal } from "@pureadmin/utils";
import { responsiveStorageNameSpace } from "@/config";
import {
  showError,
  showSuccess,
  showLoading,
  hideLoading
} from "@/utils/mobile/message";

defineOptions({
  name: "MobileLogin"
});

const router = useRouter();
const { t, locale } = useI18n();
const userStore = useUserStoreHook();
const { loadUserConfig } = useUserConfig();

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
  captcha: ""
});

// 验证码相关
const captchaImg = ref("");
const uuid = ref("");
const captchaLoading = ref(false);

// 加载状态
const loading = ref(false);

// 加载验证码
const loadCaptcha = async () => {
  captchaLoading.value = true;
  try {
    const result = await getCaptcha();
    captchaImg.value = result.img;
    uuid.value = result.uuid;
  } catch (error) {
    console.error("获取验证码失败:", error);
  } finally {
    captchaLoading.value = false;
  }
};

onMounted(() => {
  loadCaptcha();
});

// 表单验证
const validate = () => {
  if (!formData.value.username.trim()) {
    showError(t("mobile.login.usernameRequired"));
    return false;
  }
  if (
    formData.value.username.length < 3 ||
    formData.value.username.length > 20
  ) {
    showError(t("mobile.login.usernameLength"));
    return false;
  }
  if (!formData.value.password) {
    showError(t("mobile.login.passwordRequired"));
    return false;
  }
  if (
    formData.value.password.length < 6 ||
    formData.value.password.length > 20
  ) {
    showError(t("mobile.login.passwordLength"));
    return false;
  }
  if (!formData.value.captcha.trim()) {
    showError(t("mobile.login.captchaRequired"));
    return false;
  }
  return true;
};

// 登录
const handleLogin = async () => {
  if (!validate()) return;

  loading.value = true;
  showLoading(t("mobile.common.loading"));

  try {
    // H5 端默认开启记住我
    userStore.SET_ISREMEMBERED(true);

    await userStore.loginByUsername({
      username: formData.value.username,
      password: formData.value.password,
      captcha: formData.value.captcha,
      uuid: uuid.value,
      rememberMe: true
    });

    usePermissionStoreHook().handleWholeMenus([]);
    addPathMatch();

    hideLoading();
    showSuccess(t("mobile.login.success"));

    // 加载用户配置
    await loadUserConfig(userStore.id);

    // 跳转首页
    router.replace(getTopMenu(true).path);
  } catch (error: any) {
    hideLoading();
    const errMsg = error?.message || t("mobile.login.failed");
    showError(errMsg);
    // 刷新验证码
    loadCaptcha();
    formData.value.captcha = "";
  } finally {
    loading.value = false;
  }
};

// 跳转注册
const goRegister = () => {
  router.push("/register");
};
</script>

<template>
  <div class="login-page">
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

    <!-- 应用名称 -->
    <div class="app-title">
      <h1>bookkeeping</h1>
    </div>

    <!-- 表单区域 -->
    <div class="form-container">
      <van-cell-group inset>
        <van-field
          v-model="formData.username"
          :placeholder="t('mobile.login.usernamePlaceholder')"
          left-icon="user-o"
          clearable
        />
        <van-field
          v-model="formData.password"
          type="password"
          :placeholder="t('mobile.login.passwordPlaceholder')"
          left-icon="lock"
          show-password-on="click"
        />
        <van-field
          v-model="formData.captcha"
          :placeholder="t('mobile.login.captchaPlaceholder')"
          left-icon="shield-o"
          clearable
          maxlength="4"
        >
          <template #button>
            <van-image
              v-if="captchaImg"
              :src="captchaImg"
              fit="contain"
              class="captcha-img"
              @click="loadCaptcha"
            >
              <template #error>
                <van-icon name="refresh" class="captcha-icon" />
              </template>
            </van-image>
            <van-button
              v-else
              size="small"
              :loading="captchaLoading"
              @click="loadCaptcha"
            >
              <van-icon name="refresh" />
            </van-button>
          </template>
        </van-field>
      </van-cell-group>

      <!-- 登录按钮 -->
      <van-button
        type="primary"
        block
        round
        :loading="loading"
        class="login-btn"
        @click="handleLogin"
      >
        {{ t("mobile.login.loginBtn") }}
      </van-button>

      <!-- 注册提示 -->
      <div class="register-tip">
        <span>{{ t("mobile.login.noAccount") }}</span>
        <span class="link" @click="goRegister">{{
          t("mobile.login.goRegister")
        }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.login-page {
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

.app-title {
  margin-bottom: 40px;

  h1 {
    margin: 0;
    font-size: 28px;
    font-weight: 600;
    color: $color-primary;
    letter-spacing: 1px;
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

.captcha-img {
  width: 80px;
  height: 28px;
  cursor: pointer;
}

.captcha-icon {
  font-size: 20px;
  color: #969799;
}

.login-btn {
  height: 44px;
  margin-top: 24px;
  font-size: 16px;
  background-color: $color-primary;
  border-color: $color-primary;
}

.register-tip {
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
