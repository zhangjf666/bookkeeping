<script setup lang="ts">
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { showNotify, showLoadingToast, closeToast } from "vant";
import { useUserStoreHook } from "@/store/modules/user";

defineOptions({
  name: "MobileLogin"
});

const router = useRouter();
const userStore = useUserStoreHook();

const form = ref({
  username: "",
  password: "",
  captcha: "",
  uuid: ""
});

const loading = ref(false);
const captchaImg = ref("");

const canSubmit = computed(() => {
  return form.value.username.trim() !== "" && form.value.password.trim() !== "";
});

const handleSubmit = async () => {
  if (!canSubmit.value) {
    showNotify({ type: "warning", message: "请输入用户名和密码" });
    return;
  }

  loading.value = true;
  showLoadingToast({
    message: "登录中...",
    forbidClick: true
  });

  try {
    await userStore.loginByUsername({
      username: form.value.username,
      password: form.value.password,
      captcha: form.value.captcha || "skip",
      uuid: form.value.uuid || "skip"
    });

    closeToast();
    showNotify({ type: "success", message: "登录成功" });

    // 跳转到首页
    router.replace("/dashboard");
  } catch (error: any) {
    closeToast();
    showNotify({ type: "danger", message: error?.message || "登录失败" });
  } finally {
    loading.value = false;
  }
};

const goRegister = () => {
  router.push("/register");
};
</script>

<template>
  <div class="mobile-login">
    <div class="login-header">
      <h1 class="title">记账本</h1>
      <p class="subtitle">轻松管理您的收支</p>
    </div>

    <van-cell-group inset class="login-form">
      <van-field
        v-model="form.username"
        label="用户名"
        placeholder="请输入用户名"
        clearable
      />
      <van-field
        v-model="form.password"
        type="password"
        label="密码"
        placeholder="请输入密码"
        clearable
      />
    </van-cell-group>

    <div class="login-actions">
      <van-button
        type="primary"
        block
        :loading="loading"
        :disabled="!canSubmit"
        @click="handleSubmit"
      >
        登录
      </van-button>

      <van-button type="default" block @click="goRegister">
        注册账号
      </van-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.mobile-login {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  .title {
    font-size: 32px;
    font-weight: bold;
    color: #fff;
    margin-bottom: 10px;
  }

  .subtitle {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
  }
}

.login-form {
  width: 100%;
  max-width: 350px;
  margin-bottom: 20px;
}

.login-actions {
  width: 100%;
  max-width: 350px;

  .van-button {
    margin-bottom: 12px;
  }
}
</style>