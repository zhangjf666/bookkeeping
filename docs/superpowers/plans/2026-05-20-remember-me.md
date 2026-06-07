# 登录页"记住我"功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 PC 端登录页增加"记住我"勾选框，H5 端默认开启，前端 Cookie 与后端 token 续期同步。

**Architecture:** 复用现有 `isRemembered` 状态，PC 端登录页新增 UI 勾选框控制该状态，H5 端登录时硬编码开启。在 HTTP 响应拦截器中，当 `isRemembered=true` 时刷新 `multiple-tabs` Cookie 过期时间，使其与后端 Redis token 续期保持同步。

**Tech Stack:** Vue 3, Element Plus, Vant, Pinia, Axios, js-cookie

---

## 文件结构

| 文件 | 操作 | 说明 |
|------|------|------|
| `bk-pc/src/views/login/index.vue` | 修改 | PC 登录页：增加"记住我"勾选框及状态绑定 |
| `bk-pc/src/views/mobile/login/index.vue` | 修改 | H5 登录页：登录时默认设置 `isRemembered=true` |
| `bk-pc/src/utils/http/index.ts` | 修改 | HTTP 响应拦截器：增加 `multiple-tabs` Cookie 续期逻辑 |
| `bk-pc/src/types/auth.ts` | 修改 | `LoginParams` 增加可选 `rememberMe` 字段 |

---

### Task 1: PC 登录页增加"记住我"勾选框

**Files:**
- Modify: `bk-pc/src/views/login/index.vue`

- [ ] **Step 1: 在 ruleForm 中增加 rememberMe 字段**

在 `ruleForm` 响应式对象中增加 `rememberMe` 字段，默认值 `false`：

```ts
const ruleForm = reactive({
  username: "",
  password: "",
  captcha: "",
  uuid: "",
  rememberMe: false
});
```

- [ ] **Step 2: 在模板中插入勾选框**

在验证码 `el-form-item` 和登录按钮 `el-button` 之间插入"记住我"勾选框：

```vue
<Motion :delay="225">
  <el-form-item>
    <el-checkbox v-model="ruleForm.rememberMe">
      {{ t("login.pureRememberMe") }}
    </el-checkbox>
  </el-form-item>
</Motion>
```

注意：`delay="225"` 位于验证码（delay="200"）和登录按钮（delay="250"）之间，保持动画顺序。

- [ ] **Step 3: 在 onLogin 中设置 isRemembered 状态**

在调用 `useUserStoreHook().loginByUsername` 之前，设置记住我状态：

```ts
const onLogin = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  await formEl.validate(valid => {
    if (valid) {
      loading.value = true;
      // 设置记住我状态
      useUserStoreHook().SET_ISREMEMBERED(ruleForm.rememberMe);
      useUserStoreHook()
        .loginByUsername({
          username: ruleForm.username,
          password: ruleForm.password,
          captcha: ruleForm.captcha,
          uuid: uuid.value,
          rememberMe: ruleForm.rememberMe
        })
```

- [ ] **Step 4: Commit**

```bash
git add bk-pc/src/views/login/index.vue
git commit -m "feat: PC登录页增加记住我勾选框"
```

---

### Task 2: H5 登录页默认开启记住我

**Files:**
- Modify: `bk-pc/src/views/mobile/login/index.vue`

- [ ] **Step 1: 在 handleLogin 中设置 isRemembered=true**

在调用 `userStore.loginByUsername` 之前，增加设置记住我状态的代码：

```ts
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
```

- [ ] **Step 2: Commit**

```bash
git add bk-pc/src/views/mobile/login/index.vue
git commit -m "feat: H5登录页默认开启记住我"
```

---

### Task 3: HTTP 响应拦截器增加 Cookie 续期

**Files:**
- Modify: `bk-pc/src/utils/http/index.ts`

- [ ] **Step 1: 引入所需依赖**

在文件顶部引入 `Cookies`、`multipleTabsKey` 和 `useUserStoreHook`：

```ts
import Axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type CustomParamsSerializer
} from "axios";
import type {
  PureHttpError,
  RequestMethods,
  PureHttpResponse,
  PureHttpRequestConfig
} from "./types.d";
import { stringify } from "qs";
import { getToken, formatToken, removeToken, multipleTabsKey } from "@/utils/auth";
import { useUserStoreHook } from "@/store/modules/user";
import Cookies from "js-cookie";
import router from "@/router";
```

- [ ] **Step 2: 在响应拦截器中增加 Cookie 续期逻辑**

在响应拦截器的成功分支中，返回数据之前增加续期逻辑：

```ts
/** 响应拦截 */
private httpInterceptorsResponse(): void {
  const instance = PureHttp.axiosInstance;
  instance.interceptors.response.use(
    (response: PureHttpResponse) => {
      const $config = response.config;

      // 记住我模式下，刷新 multiple-tabs cookie 过期时间
      const userStore = useUserStoreHook();
      if (Cookies.get(multipleTabsKey) && userStore.isRemembered) {
        Cookies.set(multipleTabsKey, "true", {
          expires: userStore.loginDay
        });
      }

      if ($config.responseType === "blob") {
        return response.data;
      }
```

- [ ] **Step 3: Commit**

```bash
git add bk-pc/src/utils/http/index.ts
git commit -m "feat: HTTP响应拦截器增加multiple-tabs Cookie续期"
```

---

### Task 4: 扩展登录参数类型

**Files:**
- Modify: `bk-pc/src/types/auth.ts`

- [ ] **Step 1: LoginParams 增加 rememberMe 字段**

```ts
export interface LoginParams {
  username: string;
  password: string;
  captcha: string;
  uuid: string;
  rememberMe?: boolean;
}
```

- [ ] **Step 2: Commit**

```bash
git add bk-pc/src/types/auth.ts
git commit -m "types: LoginParams增加rememberMe可选字段"
```

---

## 自我审查

### Spec 覆盖检查

| 设计文档要求 | 对应任务 |
|-------------|---------|
| PC 端登录页增加"记住我"勾选框 | Task 1 |
| H5 端默认开启 | Task 2 |
| 前端 Cookie 续期机制 | Task 3 |
| 登录参数扩展 | Task 4 |

### Placeholder 检查

- 无 TBD/TODO
- 无模糊描述
- 所有代码块包含完整代码

### 类型一致性检查

- `ruleForm.rememberMe` 为 `boolean`，与 `SET_ISREMEMBERED(bool: boolean)` 签名匹配
- `LoginParams.rememberMe` 为可选字段 `boolean`，不影响现有调用
- `userStore.loginDay` 为 `number`，与 `Cookies.set` 的 `expires` 参数类型匹配
