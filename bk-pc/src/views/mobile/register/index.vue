<script setup lang="ts">
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { showNotify, showLoadingToast, closeToast } from "vant";
import { register } from "@/api/auth";

defineOptions({
  name: "MobileRegister"
});

const router = useRouter();

const form = ref({
  username: "",
  password: "",
  confirmPassword: "",
  nickName: ""
});

const loading = ref(false);

const canSubmit = computed(() => {
  return (
    form.value.username.trim() !== "" &&
    form.value.password.trim() !== "" &&
    form.value.confirmPassword.trim() !== "" &&
    form.value.password === form.value.confirmPassword
  );
});

const handleSubmit = async () => {
  if (!form.value.username.trim()) {
    showNotify({ type: "warning", message: "请输入用户名" });
    return;
  }

  if (!form.value.password.trim()) {
    showNotify({ type: "warning", message: "请输入密码" });
    return;
  }

  if (form.value.password !== form.value.confirmPassword) {
    showNotify({ type: "warning", message: "两次密码输入不一致" });
    return;
  }

  loading.value = true;
  showLoadingToast({
    message: "注册中...",
    forbidClick: true
  });

  try {
    await register({
      username: form.value.username,
      password: form.value.password,
      repeatPassword: form.value.confirmPassword,
      captcha: "skip",
      uuid: "skip"
    });

    closeToast();
    showNotify({ type: "success", message: "注册成功" });

    // 跳转到登录页
    router.replace("/login");
  } catch (error: any) {
    closeToast();
    showNotify({ type: "danger", message: error?.message || "注册失败" });
  } finally {
    loading.value = false;
  }
};

const goLogin = () => {
  router.push("/login");
};
</script>

<template>
  <div class="mobile-register">
    <div class="register-header">
      <h1 class="title">注册账号</h1>
      <p class="subtitle">创建您的记账账号</p>
    </div>

    <van-cell-group inset class="register-form">
      <van-field
        v-model="form.username"
        label="用户名"
        placeholder="请输入用户名"
        clearable
      />
      <van-field
        v-model="form.nickName"
        label="昵称"
        placeholder="请输入昵称（可选）"
        clearable
      />
      <van-field
        v-model="form.password"
        type="password"
        label="密码"
        placeholder="请输入密码"
        clearable
      />
      <van-field
        v-model="form.confirmPassword"
        type="password"
        label="确认密码"
        placeholder="请再次输入密码"
        clearable
      />
    </van-cell-group>

    <div class="register-actions">
      <van-button
        type="primary"
        block
        :loading="loading"
        :disabled="!canSubmit"
        @click="handleSubmit"
      >
        注册
      </van-button>

      <van-button type="default" block @click="goLogin">
        已有账号，去登录
      </van-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.mobile-register {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;

  .title {
    font-size: 28px;
    font-weight: bold;
    color: #fff;
    margin-bottom: 10px;
  }

  .subtitle {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
  }
}

.register-form {
  width: 100%;
  max-width: 350px;
  margin-bottom: 20px;
}

.register-actions {
  width: 100%;
  max-width: 350px;

  .van-button {
    margin-bottom: 12px;
  }
}
</style>