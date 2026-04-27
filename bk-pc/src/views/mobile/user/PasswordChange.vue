<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { changePassword } from "@/api/user";
import type { ChangePasswordForm } from "@/api/user";
import {
  showLoading,
  hideLoading,
  showSuccess,
  showError
} from "@/utils/mobile/message";

defineOptions({
  name: "MobileUserPassword"
});

const router = useRouter();
const { t } = useI18n();

// 表单数据
const formData = ref<ChangePasswordForm>({
  oldPassword: "",
  newPassword: "",
  confirmNewPassword: ""
});

// 密码可见性
const showOldPassword = ref(false);
const showNewPassword = ref(false);
const showConfirmPassword = ref(false);

// 保存状态
const saving = ref(false);

// 表单验证
const validate = () => {
  if (!formData.value.oldPassword) {
    showError(t("mobile.password.oldPasswordRequired"));
    return false;
  }

  if (!formData.value.newPassword) {
    showError(t("mobile.password.newPasswordRequired"));
    return false;
  }

  if (formData.value.newPassword.length < 6) {
    showError(t("mobile.password.passwordMinLength"));
    return false;
  }

  if (formData.value.newPassword !== formData.value.confirmNewPassword) {
    showError(t("mobile.password.passwordNotMatch"));
    return false;
  }

  return true;
};

// 提交修改
const handleSubmit = async () => {
  if (!validate()) return;

  saving.value = true;
  showLoading(t("mobile.common.loading"));

  try {
    await changePassword(formData.value);
    hideLoading();
    showSuccess(t("mobile.password.changeSuccess"));
    router.back();
  } catch (error: any) {
    hideLoading();
    showError(error?.message || t("mobile.common.failed"));
  } finally {
    saving.value = false;
  }
};
</script>

<template>
  <div class="password-page">
    <!-- 表单区域 -->
    <van-cell-group inset class="form-group">
      <!-- 原密码 -->
      <van-field
        v-model="formData.oldPassword"
        :type="showOldPassword ? 'text' : 'password'"
        :label="t('mobile.password.oldPassword')"
        :placeholder="t('mobile.password.oldPasswordPlaceholder')"
        :right-icon="showOldPassword ? 'eye-o' : 'closed-eye'"
        @click-right-icon="showOldPassword = !showOldPassword"
      />

      <!-- 新密码 -->
      <van-field
        v-model="formData.newPassword"
        :type="showNewPassword ? 'text' : 'password'"
        :label="t('mobile.password.newPassword')"
        :placeholder="t('mobile.password.newPasswordPlaceholder')"
        :right-icon="showNewPassword ? 'eye-o' : 'closed-eye'"
        @click-right-icon="showNewPassword = !showNewPassword"
      />

      <!-- 确认密码 -->
      <van-field
        v-model="formData.confirmNewPassword"
        :type="showConfirmPassword ? 'text' : 'password'"
        :label="t('mobile.password.confirmPassword')"
        :placeholder="t('mobile.password.confirmPasswordPlaceholder')"
        :right-icon="showConfirmPassword ? 'eye-o' : 'closed-eye'"
        @click-right-icon="showConfirmPassword = !showConfirmPassword"
      />
    </van-cell-group>

    <!-- 提交按钮 -->
    <div class="submit-btn">
      <van-button
        type="danger"
        block
        round
        :loading="saving"
        @click="handleSubmit"
      >
        {{ t("mobile.password.confirmChange") }}
      </van-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.password-page {
  min-height: 100vh;
  background-color: $color-background;
}

.form-group {
  margin: 12px 16px;
}

.submit-btn {
  padding: 24px 16px;
}
</style>
