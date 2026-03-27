<script setup lang="ts">
import { useI18n } from "vue-i18n";
import Motion from "./utils/motion";
import { useRouter } from "vue-router";
import { message } from "@/utils/message";
import { createLoginRules } from "./utils/rule";
import { ref, reactive, toRaw, onMounted, computed } from "vue";
import { debounce } from "@pureadmin/utils";
import { useNav } from "@/layout/hooks/useNav";
import { useEventListener } from "@vueuse/core";
import type { FormInstance } from "element-plus";
import { $t, transformI18n } from "@/plugins/i18n";
import { useLayout } from "@/layout/hooks/useLayout";
import { useUserStoreHook } from "@/store/modules/user";
import { addPathMatch, getTopMenu } from "@/router/utils";
import { usePermissionStoreHook } from "@/store/modules/permission";
import { bg, avatar, illustration } from "./utils/static";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { useTranslationLang } from "@/layout/hooks/useTranslationLang";
import { useDataThemeChange } from "@/layout/hooks/useDataThemeChange";
import { getCaptcha } from "@/api/auth";

import dayIcon from "@/assets/svg/day.svg?component";
import darkIcon from "@/assets/svg/dark.svg?component";
import globalization from "@/assets/svg/globalization.svg?component";
import Lock from "~icons/ri/lock-fill";
import Check from "~icons/ep/check";
import User from "~icons/ri/user-3-fill";
import Refresh from "~icons/ep/refresh-right";

defineOptions({
  name: "Login"
});

const router = useRouter();
const loading = ref(false);
const disabled = ref(false);
const ruleFormRef = ref<FormInstance>();
const captchaRef = ref<FormInstance>();

const { initStorage } = useLayout();
initStorage();

const { t } = useI18n();
const loginRules = computed(() => createLoginRules(t));
const { dataTheme, overallStyle, dataThemeChange } = useDataThemeChange();
dataThemeChange(overallStyle.value);
const { title, getDropdownItemStyle, getDropdownItemClass } = useNav();
const { locale, translationCh, translationEn } = useTranslationLang();

const captchaImg = ref("");
const uuid = ref("");
const captchaLoading = ref(false);

const ruleForm = reactive({
  username: "",
  password: "",
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

const onLogin = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  await formEl.validate(valid => {
    if (valid) {
      loading.value = true;
      useUserStoreHook()
        .loginByUsername({
          username: ruleForm.username,
          password: ruleForm.password,
          captcha: ruleForm.captcha,
          uuid: uuid.value
        })
        .then(() => {
          // 全部采取静态路由模式
          disabled.value = true;
          usePermissionStoreHook().handleWholeMenus([]);
          addPathMatch();
          router.push(getTopMenu(true).path);
          message(t("login.pureLoginSuccess"), { type: "success" });
          disabled.value = false;
          // return initRouter().then(() => {
          //   disabled.value = true;
          //   router
          //     .push(getTopMenu(true).path)
          //     .then(() => {
          //       message(t("login.pureLoginSuccess"), { type: "success" });
          //     })
          //     .finally(() => (disabled.value = false));
          // });
        })
        .catch(error => {
          const errMsg = error?.message || t("login.pureLoginFail");
          message(errMsg, { type: "error" });
          loadCaptcha();
          ruleForm.captcha = "";
        })
        .finally(() => (loading.value = false));
    }
  });
};

const immediateDebounce: any = debounce(
  formRef => onLogin(formRef),
  1000,
  true
);

useEventListener(document, "keydown", ({ code }) => {
  if (
    ["Enter", "NumpadEnter"].includes(code) &&
    !disabled.value &&
    !loading.value
  )
    immediateDebounce(ruleFormRef.value);
});

const toRegister = () => {
  router.push("/register");
};
</script>

<template>
  <div class="select-none">
    <img :src="bg" class="wave" />
    <div class="flex-c absolute right-5 top-3">
      <el-switch
        v-model="dataTheme"
        inline-prompt
        :active-icon="dayIcon"
        :inactive-icon="darkIcon"
        @change="dataThemeChange"
      />
      <el-dropdown trigger="click">
        <globalization
          class="hover:text-primary hover:bg-[transparent]! w-[20px] h-[20px] ml-1.5 cursor-pointer outline-hidden duration-300"
        />
        <template #dropdown>
          <el-dropdown-menu class="translation">
            <el-dropdown-item
              :style="getDropdownItemStyle(locale, 'zh')"
              :class="['dark:text-white!', getDropdownItemClass(locale, 'zh')]"
              @click="translationCh"
            >
              <IconifyIconOffline
                v-show="locale === 'zh'"
                class="check-zh"
                :icon="Check"
              />
              简体中文
            </el-dropdown-item>
            <el-dropdown-item
              :style="getDropdownItemStyle(locale, 'en')"
              :class="['dark:text-white!', getDropdownItemClass(locale, 'en')]"
              @click="translationEn"
            >
              <span v-show="locale === 'en'" class="check-en">
                <IconifyIconOffline :icon="Check" />
              </span>
              English
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <div class="login-container">
      <div class="img">
        <component :is="toRaw(illustration)" />
      </div>
      <div class="login-box">
        <div class="login-form">
          <avatar class="avatar" />
          <Motion>
            <h2 class="outline-hidden">{{ title }}</h2>
          </Motion>

          <el-form
            ref="ruleFormRef"
            :model="ruleForm"
            :rules="loginRules"
            size="large"
          >
            <Motion :delay="100">
              <el-form-item
                :rules="[
                  {
                    required: true,
                    message: transformI18n($t('login.pureUsernameReg')),
                    trigger: 'blur'
                  }
                ]"
                prop="username"
              >
                <el-input
                  v-model="ruleForm.username"
                  clearable
                  :placeholder="t('login.pureUsername')"
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
                  :placeholder="t('login.purePassword')"
                  :prefix-icon="useRenderIcon(Lock)"
                />
              </el-form-item>
            </Motion>

            <Motion :delay="200">
              <el-form-item prop="captcha">
                <el-input
                  v-model="ruleForm.captcha"
                  clearable
                  maxlength="4"
                  :placeholder="t('login.pureCaptcha')"
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

            <Motion :delay="250">
              <el-button
                class="w-full mt-4!"
                size="default"
                type="primary"
                :loading="loading"
                :disabled="disabled"
                @click="onLogin(ruleFormRef)"
              >
                {{ t("login.pureLogin") }}
              </el-button>
            </Motion>

            <Motion :delay="300">
              <div class="register-link">
                <span>{{ t("login.pureNoAccount") }}</span>
                <el-link type="primary" @click="toRegister">
                  {{ t("login.pureGoRegister") }}
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

.translation {
  ::v-deep(.el-dropdown-menu__item) {
    padding: 5px 40px;
  }

  .check-zh {
    position: absolute;
    left: 20px;
  }

  .check-en {
    position: absolute;
    left: 20px;
  }
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

.register-link {
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
