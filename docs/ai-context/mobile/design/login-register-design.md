# 登录与注册页面详细设计

## 1. 页面概述

### 1.1 页面信息
| 项目 | 登录页 | 注册页 |
|------|--------|--------|
| 路由路径 | `/login` | `/register` |
| 页面名称 | Login | Register |
| 是否显示 TabBar | 否 | 否 |
| 是否显示 NavBar | 否 | 否 |

### 1.2 页面关系
```
登录页 (/login)  ←─────→  注册页 (/register)
     │                         │
     │ 点击"立即注册"           │ 点击"登录"
     └─────────────────────────┘
```

---

## 2. 登录页面设计

### 2.1 页面布局
```
┌─────────────────────────────────────┐
│                              [中文 ▼]│  ← 语言切换（右上角）
│                                     │
│          bookkeeping                │  ← 应用名称（居中）
│          (Logo/图标 可选)            │
│                                     │
│    ┌───────────────────────────┐    │
│    │ 用户名                    │    │  ← 输入框
│    │ placeholder: 请输入用户名  │    │
│    └───────────────────────────┘    │
│                                     │
│    ┌───────────────────────────┐    │
│    │ 密码                      │    │  ← 输入框（密码类型）
│    │ placeholder: 请输入密码    │    │
│    └───────────────────────────┘    │
│                                     │
│    ┌───────────────────────────┐    │
│    │         登  录            │    │  ← 主按钮（主题色）
│    └───────────────────────────┘    │
│                                     │
│         没有账号？立即注册           │  ← 提示文字（可点击）
│                                     │
│                                     │
└─────────────────────────────────────┘
```

### 2.2 组件清单
| 组件 | 类型 | 说明 |
|------|------|------|
| 语言切换 | van-popover + van-button | 右上角，点击弹出语言列表 |
| 应用名称 | 文本 | "bookkeeping"，居中显示 |
| 用户名输入框 | van-field | 用户名输入，左侧图标 |
| 密码输入框 | van-field | 密码输入，支持显示/隐藏切换 |
| 登录按钮 | van-button | 主题色按钮，全宽 |
| 注册提示 | 文本 | 包含可点击链接 |

### 2.3 语言切换组件
```
┌─────────────────┐
│    [中文 ▼]     │  ← 点击展开
└─────────────────┘
        │
        ▼
┌─────────────────┐
│ ✓ 中文          │  ← 当前选中
│   English       │
└─────────────────┘
```

**语言选项**：
| 语言代码 | 显示名称 |
|----------|----------|
| zh | 中文 |
| en | English |

### 2.4 表单字段
| 字段 | 类型 | 必填 | 验证规则 | placeholder |
|------|------|------|----------|-------------|
| username | text | 是 | 非空，长度 3-20 | 请输入用户名 |
| password | password | 是 | 非空，长度 6-20 | 请输入密码 |

### 2.5 交互流程

#### 2.5.1 语言切换流程
```
点击语言按钮
       │
       ▼
显示语言列表 Popover
       │
       ▼
选择目标语言
       │
       ▼
┌─────────────────┐
│ 更新 i18n locale │
│ 存储到 localStorage │
└─────────────────┘
       │
       ▼
页面文案自动切换
```

#### 2.5.2 登录流程
```
用户点击登录按钮
       │
       ▼
┌─────────────────┐
│  表单验证通过？  │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
   否         是
    │         │
    ▼         ▼
显示错误   显示 Loading
提示       │
           ▼
    ┌─────────────────┐
    │   调用登录 API   │
    └────────┬────────┘
             │
        ┌────┴────┐
        │         │
      失败       成功
        │         │
        ▼         ▼
   显示错误    存储 Token
   提示        更新用户状态
              │
              ▼
         跳转首页
```

#### 2.5.3 跳转注册
```
点击"立即注册"
       │
       ▼
路由跳转 /register
```

### 2.5 API 调用
```typescript
// api/auth.ts
interface LoginParams {
  username: string;
  password: string;
}

interface LoginResult {
  token: string;
  user: UserInfo;
}

// 登录接口
export function login(params: LoginParams): Promise<LoginResult>
```

### 2.6 代码示例
```vue
<!-- views/mobile/login/index.vue -->
<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showLoadingToast, closeToast } from 'vant';
import { useI18n } from 'vue-i18n';
import { login } from '@/api/auth';
import { useUserStore } from '@/store/modules/user';
import { setToken } from '@/utils/auth';
import { storageLocal } from '@pureadmin/utils';
import { responsiveStorageNameSpace } from '@/config';

const router = useRouter();
const { t, locale } = useI18n();
const userStore = useUserStore();

// 语言选项
const languageOptions = [
  { value: 'zh', label: '中文' },
  { value: 'en', label: 'English' }
];

// 当前语言显示文本
const currentLanguage = computed(() => {
  return languageOptions.find(opt => opt.value === locale.value)?.label || '中文';
});

// 切换语言
const switchLanguage = (lang: string) => {
  locale.value = lang;
  storageLocal().setItem(`${responsiveStorageNameSpace()}locale`, { locale: lang });
};

// 表单数据
const formData = ref({
  username: '',
  password: ''
});

// 加载状态
const loading = ref(false);

// 表单验证
const validate = () => {
  if (!formData.value.username.trim()) {
    showToast(t('mobile.login.usernameRequired'));
    return false;
  }
  if (formData.value.username.length < 3 || formData.value.username.length > 20) {
    showToast(t('mobile.login.usernameLength'));
    return false;
  }
  if (!formData.value.password) {
    showToast(t('mobile.login.passwordRequired'));
    return false;
  }
  if (formData.value.password.length < 6 || formData.value.password.length > 20) {
    showToast(t('mobile.login.passwordLength'));
    return false;
  }
  return true;
};

// 登录
const handleLogin = async () => {
  if (!validate()) return;

  loading.value = true;
  showLoadingToast({ message: t('mobile.common.loading'), forbidClick: true });

  try {
    const res = await login({
      username: formData.value.username,
      password: formData.value.password
    });

    // 存储 Token
    setToken(res.token);

    // 更新用户状态
    userStore.setUserInfo(res.user);

    showToast(t('mobile.login.success'));

    // 跳转首页
    router.replace('/dashboard');
  } catch (error: any) {
    showToast(error.message || t('mobile.login.failed'));
  } finally {
    loading.value = false;
    closeToast();
  }
};

// 跳转注册
const goRegister = () => {
  router.push('/register');
};
</script>

<template>
  <div class="login-page">
    <!-- 语言切换 -->
    <div class="language-switch">
      <van-popover
        placement="bottom-end"
        :actions="languageOptions.map(opt => ({ text: opt.label, value: opt.value }))"
        @select="(action) => switchLanguage(action.value)"
      >
        <template #reference>
          <van-button size="small" type="default" icon="arrow-down">
            {{ currentLanguage }}
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
        {{ t('mobile.login.loginBtn') }}
      </van-button>

      <!-- 注册提示 -->
      <div class="register-tip">
        <span>{{ t('mobile.login.noAccount') }}</span>
        <span class="link" @click="goRegister">{{ t('mobile.login.goRegister') }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
  background-color: #f7f8fa;
  position: relative;
}

.language-switch {
  position: absolute;
  top: 32rpx;
  right: 32rpx;
  z-index: 10;

  :deep(.van-button) {
    font-size: 24rpx;
    padding: 0 16rpx;
    height: 56rpx;
  }
}

.app-title {
  margin-bottom: 80rpx;

  h1 {
    font-size: 56rpx;
    font-weight: 600;
    color: $color-primary;
    letter-spacing: 2rpx;
  }
}

.form-container {
  width: 100%;
  max-width: 650rpx;

  :deep(.van-cell-group--inset) {
    margin: 0;
    border-radius: 16rpx;
    overflow: hidden;
  }

  :deep(.van-field) {
    padding: 24rpx 32rpx;

    .van-field__left-icon {
      margin-right: 16rpx;
      color: #969799;
    }
  }
}

.login-btn {
  margin-top: 48rpx;
  height: 88rpx;
  font-size: 32rpx;
  background-color: $color-primary;
  border-color: $color-primary;
}

.register-tip {
  margin-top: 32rpx;
  text-align: center;
  font-size: 26rpx;
  color: #969799;

  .link {
    color: $color-primary;
    margin-left: 8rpx;
  }
}
</style>
```

---

## 3. 注册页面设计

### 3.1 页面布局
```
┌─────────────────────────────────────┐
│                              [中文 ▼]│  ← 语言切换（右上角）
│                                     │
│          注册账号                    │  ← 页面标题
│                                     │
│    ┌───────────────────────────┐    │
│    │ 用户名                    │    │  ← 用户名输入框
│    │ placeholder: 请输入用户名  │    │
│    └───────────────────────────┘    │
│                                     │
│    ┌───────────────────────────┐    │
│    │ 昵称                      │    │  ← 昵称输入框
│    │ placeholder: 请输入昵称    │    │
│    └───────────────────────────┘    │
│                                     │
│    ┌───────────────────────────┐    │
│    │ 密码                      │    │  ← 密码输入框
│    │ placeholder: 请输入密码    │    │
│    └───────────────────────────┘    │
│                                     │
│    ┌───────────────────────────┐    │
│    │ 确认密码                  │    │  ← 确认密码输入框
│    │ placeholder: 请再次输入密码│    │
│    └───────────────────────────┘    │
│                                     │
│    ┌───────────────────────────┐    │
│    │         注  册            │    │  ← 主按钮（主题色）
│    └───────────────────────────┘    │
│                                     │
│         已有账号？去登录             │  ← 提示文字（可点击）
│                                     │
└─────────────────────────────────────┘
```

### 3.2 组件清单
| 组件 | 类型 | 说明 |
|------|------|------|
| 语言切换 | van-popover + van-button | 右上角，点击弹出语言列表 |
| 页面标题 | 文本 | "注册账号" |
| 用户名输入框 | van-field | 用户名输入，用于登录 |
| 昵称输入框 | van-field | 用户昵称，显示用 |
| 密码输入框 | van-field | 密码输入，支持显示/隐藏 |
| 确认密码输入框 | van-field | 再次输入密码 |
| 注册按钮 | van-button | 主题色按钮，全宽 |
| 登录提示 | 文本 | 包含可点击链接 |

### 3.3 表单字段
| 字段 | 类型 | 必填 | 验证规则 | placeholder |
|------|------|------|----------|-------------|
| username | text | 是 | 非空，长度 3-20，仅字母数字下划线 | 请输入用户名 |
| nickname | text | 是 | 非空，长度 2-20 | 请输入昵称 |
| password | password | 是 | 非空，长度 6-20 | 请输入密码 |
| confirmPassword | password | 是 | 与 password 一致 | 请再次输入密码 |

### 3.4 交互流程

#### 3.4.1 注册流程
```
用户点击注册按钮
       │
       ▼
┌─────────────────┐
│  表单验证通过？  │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
   否         是
    │         │
    ▼         ▼
显示错误   显示 Loading
提示       │
           ▼
    ┌─────────────────┐
    │   调用注册 API   │
    └────────┬────────┘
             │
        ┌────┴────┐
        │         │
      失败       成功
        │         │
        ▼         ▼
   显示错误    显示成功提示
   提示        │
              ▼
         跳转登录页
```

#### 3.4.2 跳转登录
```
点击"登录"
       │
       ▼
路由跳转 /login
```

### 3.5 API 调用
```typescript
// api/auth.ts
interface RegisterParams {
  username: string;
  nickname: string;
  password: string;
}

interface RegisterResult {
  success: boolean;
  message: string;
}

// 注册接口
export function register(params: RegisterParams): Promise<RegisterResult>
```

### 3.6 代码示例
```vue
<!-- views/mobile/register/index.vue -->
<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showLoadingToast, closeToast } from 'vant';
import { useI18n } from 'vue-i18n';
import { register } from '@/api/auth';
import { storageLocal } from '@pureadmin/utils';
import { responsiveStorageNameSpace } from '@/config';

const router = useRouter();
const { t, locale } = useI18n();

// 语言选项
const languageOptions = [
  { value: 'zh', label: '中文' },
  { value: 'en', label: 'English' }
];

// 当前语言显示文本
const currentLanguage = computed(() => {
  return languageOptions.find(opt => opt.value === locale.value)?.label || '中文';
});

// 切换语言
const switchLanguage = (lang: string) => {
  locale.value = lang;
  storageLocal().setItem(`${responsiveStorageNameSpace()}locale`, { locale: lang });
};

// 表单数据
const formData = ref({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
});

// 加载状态
const loading = ref(false);

// 表单验证
const validate = () => {
  if (!formData.value.username.trim()) {
    showToast(t('mobile.register.usernameRequired'));
    return false;
  }
  if (formData.value.username.length < 3 || formData.value.username.length > 20) {
    showToast(t('mobile.register.usernameLength'));
    return false;
  }
  if (!/^[a-zA-Z0-9_]+$/.test(formData.value.username)) {
    showToast(t('mobile.register.usernameFormat'));
    return false;
  }
  if (!formData.value.nickname.trim()) {
    showToast(t('mobile.register.nicknameRequired'));
    return false;
  }
  if (formData.value.nickname.length < 2 || formData.value.nickname.length > 20) {
    showToast(t('mobile.register.nicknameLength'));
    return false;
  }
  if (!formData.value.password) {
    showToast(t('mobile.register.passwordRequired'));
    return false;
  }
  if (formData.value.password.length < 6 || formData.value.password.length > 20) {
    showToast(t('mobile.register.passwordLength'));
    return false;
  }
  if (!formData.value.confirmPassword) {
    showToast(t('mobile.register.confirmPasswordRequired'));
    return false;
  }
  if (formData.value.password !== formData.value.confirmPassword) {
    showToast(t('mobile.register.passwordNotMatch'));
    return false;
  }
  return true;
};

// 注册
const handleRegister = async () => {
  if (!validate()) return;

  loading.value = true;
  showLoadingToast({ message: t('mobile.common.loading'), forbidClick: true });

  try {
    await register({
      username: formData.value.username,
      nickname: formData.value.nickname,
      password: formData.value.password
    });

    showToast(t('mobile.register.success'));

    // 跳转登录页
    setTimeout(() => {
      router.replace('/login');
    }, 1500);
  } catch (error: any) {
    showToast(error.message || t('mobile.register.failed'));
  } finally {
    loading.value = false;
    closeToast();
  }
};

// 跳转登录
const goLogin = () => {
  router.push('/login');
};
</script>

<template>
  <div class="register-page">
    <!-- 语言切换 -->
    <div class="language-switch">
      <van-popover
        placement="bottom-end"
        :actions="languageOptions.map(opt => ({ text: opt.label, value: opt.value }))"
        @select="(action) => switchLanguage(action.value)"
      >
        <template #reference>
          <van-button size="small" type="default" icon="arrow-down">
            {{ currentLanguage }}
          </van-button>
        </template>
      </van-popover>
    </div>

    <!-- 页面标题 -->
    <div class="page-title">
      <h2>{{ t('mobile.register.title') }}</h2>
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
          v-model="formData.nickname"
          :placeholder="t('mobile.register.nicknamePlaceholder')"
          left-icon="contact"
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
          v-model="formData.confirmPassword"
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
        {{ t('mobile.register.registerBtn') }}
      </van-button>

      <!-- 登录提示 -->
      <div class="login-tip">
        <span>{{ t('mobile.register.hasAccount') }}</span>
        <span class="link" @click="goLogin">{{ t('mobile.register.goLogin') }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 80rpx 32rpx 32rpx;
  background-color: #f7f8fa;
  position: relative;
}

.language-switch {
  position: absolute;
  top: 32rpx;
  right: 32rpx;
  z-index: 10;

  :deep(.van-button) {
    font-size: 24rpx;
    padding: 0 16rpx;
    height: 56rpx;
  }
}

.page-title {
  margin-bottom: 48rpx;

  h2 {
    font-size: 44rpx;
    font-weight: 600;
    color: #323233;
  }
}

.form-container {
  width: 100%;
  max-width: 650rpx;

  :deep(.van-cell-group--inset) {
    margin: 0;
    border-radius: 16rpx;
    overflow: hidden;
  }

  :deep(.van-field) {
    padding: 24rpx 32rpx;

    .van-field__left-icon {
      margin-right: 16rpx;
      color: #969799;
    }
  }
}

.register-btn {
  margin-top: 48rpx;
  height: 88rpx;
  font-size: 32rpx;
  background-color: $color-primary;
  border-color: $color-primary;
}

.login-tip {
  margin-top: 32rpx;
  text-align: center;
  font-size: 26rpx;
  color: #969799;

  .link {
    color: $color-primary;
    margin-left: 8rpx;
  }
}
</style>
```

---

## 4. 国际化文案

### 4.0 语言切换文案
```yaml
# locales/mobile/zh-CN.yaml

language:
  zh: 中文
  en: English
  switch: 切换语言
```

```yaml
# locales/mobile/en.yaml

language:
  zh: 中文
  en: English
  switch: Switch Language
```

### 4.1 登录页文案
```yaml
# locales/mobile/zh-CN.yaml

login:
  title: 登录
  usernamePlaceholder: 请输入用户名
  passwordPlaceholder: 请输入密码
  loginBtn: 登录
  noAccount: 没有账号？
  goRegister: 立即注册
  usernameRequired: 请输入用户名
  usernameLength: 用户名长度为3-20个字符
  passwordRequired: 请输入密码
  passwordLength: 密码长度为6-20个字符
  success: 登录成功
  failed: 登录失败，请重试
```

```yaml
# locales/mobile/en.yaml

login:
  title: Login
  usernamePlaceholder: Please enter username
  passwordPlaceholder: Please enter password
  loginBtn: Login
  noAccount: No account?
  goRegister: Register now
  usernameRequired: Please enter username
  usernameLength: Username must be 3-20 characters
  passwordRequired: Please enter password
  passwordLength: Password must be 6-20 characters
  success: Login successful
  failed: Login failed, please try again
```

### 4.2 注册页文案
```yaml
# locales/mobile/zh-CN.yaml

register:
  title: 注册账号
  usernamePlaceholder: 请输入用户名
  nicknamePlaceholder: 请输入昵称
  passwordPlaceholder: 请输入密码
  confirmPasswordPlaceholder: 请再次输入密码
  registerBtn: 注册
  hasAccount: 已有账号？
  goLogin: 去登录
  usernameRequired: 请输入用户名
  usernameLength: 用户名长度为3-20个字符
  usernameFormat: 用户名只能包含字母、数字和下划线
  nicknameRequired: 请输入昵称
  nicknameLength: 昵称长度为2-20个字符
  passwordRequired: 请输入密码
  passwordLength: 密码长度为6-20个字符
  confirmPasswordRequired: 请再次输入密码
  passwordNotMatch: 两次输入的密码不一致
  success: 注册成功
  failed: 注册失败，请重试
```

```yaml
# locales/mobile/en.yaml

register:
  title: Create Account
  usernamePlaceholder: Please enter username
  nicknamePlaceholder: Please enter nickname
  passwordPlaceholder: Please enter password
  confirmPasswordPlaceholder: Please confirm password
  registerBtn: Register
  hasAccount: Already have an account?
  goLogin: Login
  usernameRequired: Please enter username
  usernameLength: Username must be 3-20 characters
  usernameFormat: Username can only contain letters, numbers and underscores
  nicknameRequired: Please enter nickname
  nicknameLength: Nickname must be 2-20 characters
  passwordRequired: Please enter password
  passwordLength: Password must be 6-20 characters
  confirmPasswordRequired: Please confirm password
  passwordNotMatch: Passwords do not match
  success: Registration successful
  failed: Registration failed, please try again
```

---

## 5. 样式规范

### 5.1 颜色使用
| 元素 | 颜色值 | 说明 |
|------|--------|------|
| 应用名称 | `#d83d34` | 主题色 |
| 登录/注册按钮 | `#d83d34` | 主题色背景 |
| 可点击链接 | `#d83d34` | 主题色文字 |
| 输入框边框 | `#ebedf0` | 默认边框色 |
| 输入框聚焦边框 | `#d83d34` | 主题色 |
| 提示文字 | `#969799` | 次要文字色 |

### 5.2 间距规范
| 元素 | 间距 |
|------|------|
| 页面内边距 | 32rpx |
| 应用名称底部间距 | 80rpx |
| 表单组圆角 | 16rpx |
| 输入框内边距 | 24rpx 32rpx |
| 按钮上边距 | 48rpx |
| 提示文字上边距 | 32rpx |

### 5.3 字体规范
| 元素 | 字号 | 字重 |
|------|------|------|
| 应用名称 | 56rpx | 600 |
| 页面标题 | 44rpx | 600 |
| 按钮文字 | 32rpx | 500 |
| 输入框文字 | 28rpx | 400 |
| 提示文字 | 26rpx | 400 |

---

## 6. 状态管理

### 6.1 用户状态
```typescript
// store/modules/user.ts
export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userInfo: null as UserInfo | null
  }),

  actions: {
    // 设置用户信息
    setUserInfo(info: UserInfo) {
      this.userInfo = info;
    },

    // 清除用户信息（退出登录）
    clearUserInfo() {
      this.token = '';
      this.userInfo = null;
    }
  }
});
```

---

## 7. 路由配置

### 7.1 路由定义
```typescript
// router/index.ts
{
  path: '/login',
  name: 'Login',
  component: () => import('@/views/mobile/login/index.vue'),
  meta: {
    title: '登录',
    showTabBar: false,
    showNavBar: false
  }
},
{
  path: '/register',
  name: 'Register',
  component: () => import('@/views/mobile/register/index.vue'),
  meta: {
    title: '注册',
    showTabBar: false,
    showNavBar: false
  }
}
```

### 7.2 路由守卫
```typescript
// 登录页和注册页不需要登录验证
const publicPages = ['/login', '/register'];

router.beforeEach((to, from, next) => {
  const token = getToken();

  // 已登录状态访问登录/注册页，重定向到首页
  if (token && publicPages.includes(to.path)) {
    return next('/dashboard');
  }

  // 未登录状态访问需要登录的页面，重定向到登录页
  if (!token && !publicPages.includes(to.path)) {
    return next('/login');
  }

  next();
});
```

---

## 8. 错误处理

### 8.1 错误类型
| 错误码 | 说明 | 提示文案 |
|--------|------|----------|
| 401 | 用户名或密码错误 | 用户名或密码错误 |
| 409 | 用户名已存在 | 用户名已被注册 |
| 500 | 服务器错误 | 系统繁忙，请稍后重试 |
| -1 | 网络错误 | 网络连接失败，请检查网络 |

### 8.2 错误处理示例
```typescript
try {
  const res = await login(params);
  // 处理成功
} catch (error: any) {
  const errorCode = error.code;

  switch (errorCode) {
    case 401:
      showToast(t('mobile.login.invalidCredentials'));
      break;
    case 409:
      showToast(t('mobile.register.usernameExists'));
      break;
    case 500:
      showToast(t('mobile.common.serverError'));
      break;
    default:
      showToast(t('mobile.common.networkError'));
  }
}
```

---

## 9. 测试要点

### 9.1 登录页测试
- [ ] 空用户名提交，显示错误提示
- [ ] 用户名长度不符合要求，显示错误提示
- [ ] 空密码提交，显示错误提示
- [ ] 密码长度不符合要求，显示错误提示
- [ ] 正确的用户名密码，登录成功
- [ ] 错误的用户名密码，显示错误提示
- [ ] 点击"立即注册"，跳转注册页
- [ ] 已登录状态访问登录页，重定向首页
- [ ] 点击语言切换按钮，显示语言列表
- [ ] 选择不同语言，页面文案切换
- [ ] 切换语言后刷新页面，语言设置保持

### 9.2 注册页测试
- [ ] 空用户名提交，显示错误提示
- [ ] 用户名格式不正确，显示错误提示
- [ ] 空昵称提交，显示错误提示
- [ ] 昵称长度不符合要求，显示错误提示
- [ ] 空密码提交，显示错误提示
- [ ] 密码长度不符合要求，显示错误提示
- [ ] 两次密码不一致，显示错误提示
- [ ] 正确信息注册，注册成功
- [ ] 用户名已存在，显示错误提示
- [ ] 点击"登录"，跳转登录页
- [ ] 注册成功后自动跳转登录页
- [ ] 点击语言切换按钮，显示语言列表
- [ ] 选择不同语言，页面文案切换
- [ ] 切换语言后刷新页面，语言设置保持
