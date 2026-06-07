<script setup lang="ts">
import { useI18n } from "vue-i18n";
import Motion from "@/views/login/utils/motion";
import { useRouter } from "vue-router";
import { message } from "@/utils/message";
import { createRegisterRules } from "@/views/login/utils/rule";
import { ref, reactive, onMounted, computed } from "vue";
import { debounce } from "@pureadmin/utils";
import { useEventListener } from "@vueuse/core";
import type { FormInstance } from "element-plus";
import { useLayout } from "@/layout/hooks/useLayout";
import { useUserStoreHook } from "@/store/modules/user";
import { getCaptcha, register } from "@/api/auth";
import { addPathMatch, getTopMenu } from "@/router/utils";
import { usePermissionStoreHook } from "@/store/modules/permission";
import { bg, illustration } from "@/views/login/utils/static";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { useDataThemeChange } from "@/layout/hooks/useDataThemeChange";
import { useUserConfig } from "@/composables/useUserConfig";
import Lock from "~icons/ri/lock-fill";
import User from "~icons/ri/user-3-fill";
import Refresh from "~icons/ep/refresh-right";

defineOptions({
  name: "Register"
});

const router = useRouter();
const loading = ref(false);
const disabled = ref(false);
const ruleFormRefRegister = ref<FormInstance>();

const { initStorage } = useLayout();
initStorage();

const { t } = useI18n();
const registerRules = computed(() =>
  createRegisterRules(t, ruleFormRefRegister, ruleForm)
);
const { dataTheme, overallStyle, dataThemeChange } = useDataThemeChange();
dataThemeChange(overallStyle.value);

const { loadUserConfig } = useUserConfig();

const captchaImg = ref("");
const uuid = ref("");
const captchaLoading = ref(false);

const ruleForm = reactive({
  username: "",
  password: "",
  repeatPassword: "",
  captcha: "",
  uuid: ""
});

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

const onRegister = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;

  const fields = ["username", "password", "repeatPassword", "captcha"];
  for (const field of fields) {
    await formEl.validateField(field);
  }

  loading.value = true;
  try {
    await register({
      username: ruleForm.username,
      password: ruleForm.password,
      repeatPassword: ruleForm.repeatPassword,
      captcha: ruleForm.captcha,
      uuid: uuid.value
    });

    message(t("login.pureRegisterSuccess"), { type: "success" });

    disabled.value = true;
    await useUserStoreHook().loginByUsername({
      username: ruleForm.username,
      password: ruleForm.password,
      captcha: ruleForm.captcha,
      uuid: uuid.value
    });

    usePermissionStoreHook().handleWholeMenus([]);
    addPathMatch();
    router.push(getTopMenu(true).path);
    await loadUserConfig(useUserStoreHook().id);
  } catch (error: any) {
    const errMsg = error?.message || t("login.pureRegisterFail");
    message(errMsg, { type: "error" });
    loadCaptcha();
    ruleForm.captcha = "";
  } finally {
    loading.value = false;
    disabled.value = false;
  }
};

const immediateDebounce: any = debounce(
  formRef => onRegister(formRef),
  1000,
  true
);

useEventListener(document, "keydown", ({ code }) => {
  if (
    ["Enter", "NumpadEnter"].includes(code) &&
    !disabled.value &&
    !loading.value
  )
    immediateDebounce(ruleFormRefRegister.value);
});

const toLogin = () => {
  router.push("/login");
};
</script>

<template>
  <div class="select-none">
    <img :src="bg" class="wave" />
    <div class="login-container">
      <div class="img">
        <component :is="illustration" />
      </div>
      <div class="login-box">
        <div class="login-form">
          <Motion>
            <h2 class="outline-hidden">{{ t("register.pureTitle") }}</h2>
          </Motion>

          <el-form
            ref="ruleFormRefRegister"
            :model="ruleForm"
            :rules="registerRules"
            size="large"
          >
            <Motion :delay="100">
              <el-form-item prop="username">
                <el-input
                  v-model="ruleForm.username"
                  clearable
                  :placeholder="t('register.pureUsername')"
                  :prefix-icon="useRenderIcon(User)"
                />
              </el-form-item>
            </Motion>

            <Motion :delay="150">
              <el-form-item prop="password">
                <el-input
                  v-model="ruleForm.password"
                  clearable
                  show-password
                  :placeholder="t('register.purePassword')"
                  :prefix-icon="useRenderIcon(Lock)"
                />
              </el-form-item>
            </Motion>

            <Motion :delay="200">
              <el-form-item prop="repeatPassword">
                <el-input
                  v-model="ruleForm.repeatPassword"
                  clearable
                  show-password
                  :placeholder="t('register.pureRepeatPassword')"
                  :prefix-icon="useRenderIcon(Lock)"
                />
              </el-form-item>
            </Motion>

            <Motion :delay="250">
              <el-form-item prop="captcha">
                <el-input
                  v-model="ruleForm.captcha"
                  clearable
                  maxlength="4"
                  :placeholder="t('register.pureCaptcha')"
                  style="width: 60%"
                >
                  <template #prefix>
                    <el-icon><Refresh /></el-icon>
                  </template>
                </el-input>
                <el-image
                  v-if="captchaImg"
                  :src="captchaImg"
                  fit="contain"
                  class="captcha-img"
                  @click="loadCaptcha"
                >
                  <template #error>
                    <div class="captcha-placeholder">
                      <el-icon><Refresh /></el-icon>
                    </div>
                  </template>
                </el-image>
                <el-button
                  v-else
                  :loading="captchaLoading"
                  class="captcha-btn"
                  @click="loadCaptcha"
                >
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </el-form-item>
            </Motion>

            <Motion :delay="300">
              <el-button
                class="w-full mt-4!"
                size="default"
                type="primary"
                :loading="loading"
                :disabled="disabled"
                @click="onRegister(ruleFormRefRegister)"
              >
                {{ t("register.pureRegister") }}
              </el-button>
            </Motion>

            <Motion :delay="350">
              <div class="login-link">
                <span>{{ t("register.pureHasAccount") }}</span>
                <el-link type="primary" @click="toLogin">
                  {{ t("register.pureGoLogin") }}
                </el-link>
              </div>
            </Motion>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("@/style/login.css");
</style>

<style lang="scss" scoped>
:deep(.el-input-group__append, .el-input-group__prepend) {
  padding: 0;
}

.captcha-img {
  width: 35%;
  height: 40px;
  margin-left: 5%;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.captcha-btn {
  width: 35%;
  height: 40px;
  margin-left: 5%;
}

.captcha-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
}

.login-link {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 16px;
  font-size: 14px;
  color: #999;

  .el-link {
    margin-left: 4px;
  }
}
</style>
