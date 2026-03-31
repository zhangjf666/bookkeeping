<script setup lang="ts">
import { ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
import { changePassword } from "@/api/user";
import type { ChangePasswordForm } from "@/api/user";

defineOptions({
  name: "ChangePasswordDialog"
});

const { t } = useI18n();

const props = defineProps<{
  visible: boolean;
}>();

const emit = defineEmits<{
  (e: "update:visible", value: boolean): void;
  (e: "success"): void;
}>();

const loading = ref(false);
const form = ref<ChangePasswordForm>({
  oldPassword: "",
  newPassword: "",
  confirmNewPassword: ""
});

const rules = {
  oldPassword: [
    {
      required: true,
      message: t("profile.pureOldPasswordRequired"),
      trigger: "blur"
    }
  ],
  newPassword: [
    {
      required: true,
      message: t("profile.pureNewPasswordRequired"),
      trigger: "blur"
    },
    {
      min: 6,
      max: 20,
      message: t("profile.purePasswordLength"),
      trigger: "blur"
    }
  ],
  confirmNewPassword: [
    {
      required: true,
      message: t("profile.pureConfirmPasswordRequired"),
      trigger: "blur"
    },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== form.value.newPassword) {
          callback(new Error(t("profile.purePasswordNotMatch")));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }
  ]
};

const formRef = ref();

const dialogVisible = ref(props.visible);

watch(
  () => props.visible,
  (val) => {
    dialogVisible.value = val;
  }
);

watch(dialogVisible, (val) => {
  emit("update:visible", val);
  if (!val) {
    form.value = {
      oldPassword: "",
      newPassword: "",
      confirmNewPassword: ""
    };
    formRef.value?.clearValidate();
  }
});

const handleClose = () => {
  dialogVisible.value = false;
};

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  loading.value = true;
  try {
    await changePassword({
      oldPassword: form.value.oldPassword,
      newPassword: form.value.newPassword,
      confirmNewPassword: form.value.confirmNewPassword
    });
    ElMessage.success(t("profile.purePasswordChangeSuccess"));
    emit("success");
  } catch (error: any) {
    ElMessage.error(error?.message || t("profile.purePasswordChangeFailed"));
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    :title="t('profile.pureChangePassword')"
    width="450px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
    >
      <el-form-item
        :label="t('profile.pureOldPassword')"
        prop="oldPassword"
      >
        <el-input
          v-model="form.oldPassword"
          type="password"
          show-password
          :placeholder="t('profile.pureOldPasswordPlaceholder')"
        />
      </el-form-item>
      <el-form-item
        :label="t('profile.pureNewPassword')"
        prop="newPassword"
      >
        <el-input
          v-model="form.newPassword"
          type="password"
          show-password
          :placeholder="t('profile.pureNewPasswordPlaceholder')"
        />
      </el-form-item>
      <el-form-item
        :label="t('profile.pureConfirmPassword')"
        prop="confirmNewPassword"
      >
        <el-input
          v-model="form.confirmNewPassword"
          type="password"
          show-password
          :placeholder="t('profile.pureConfirmPasswordPlaceholder')"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">
        {{ t("buttons.pureClose") }}
      </el-button>
      <el-button
        type="primary"
        :loading="loading"
        @click="handleSubmit"
      >
        {{ t("buttons.pureConfirm") }}
      </el-button>
    </template>
  </el-dialog>
</template>
