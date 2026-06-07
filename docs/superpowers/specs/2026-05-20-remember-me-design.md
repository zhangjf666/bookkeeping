# 登录页"记住我"功能设计文档

## 背景

`bk-h5` 项目已废弃，H5 页面整合到了 `bk-pc` 中，根据设备自适应展示 PC 端或 H5 端页面。现需在 PC 端登录页增加"记住我"勾选项，H5 端默认开启该功能。

## 现状分析

### 已有基础

代码中已存在"记住我"的基础结构：

- `bk-pc/src/store/modules/user.ts`：已有 `isRemembered`（默认 `false`）和 `loginDay`（默认 7 天）状态
- `bk-pc/src/utils/auth.ts`：`setToken()` 已根据 `isRemembered` 控制 `multiple-tabs` cookie 的过期策略
- 路由守卫 `router.beforeEach`：通过检查 `multiple-tabs` cookie 判断登录状态

### 后端 Token 续期机制

后端 `JwtTokenFilter` 每次请求都会调用 `jwtService.checkRenewal(token)`：

- 初始 token 有效期：7 天（`token-validity: 604800`）
- 续期检查阈值：7 天（`detect: 604800`）
- 续期增加时间：7 天（`renew: 604800`）
- **实际效果：只要用户 7 天内有至少一次访问，后端 token 永远不会过期**

### 当前问题

后端 token 支持活跃续期，但前端 Cookie 不会续期：

- `authorized-token` Cookie：固定 7 天过期，7 天后即使后端 token 仍有效，前端也发不出请求
- `multiple-tabs` Cookie：7 天后过期，路由守卫判定未登录，跳转到登录页

## 设计方案

### 核心思路

复用现有 `isRemembered` 和 `loginDay` 逻辑，仅增加：
1. PC 端登录页 UI 勾选框
2. H5 端默认开启
3. 前端 Cookie 续期机制，与后端 token 续期保持同步

### 详细设计

#### 1. PC 端登录页增加"记住我"勾选框

**文件**：`bk-pc/src/views/login/index.vue`

在登录表单中，密码和验证码之间（或验证码下方、登录按钮上方）增加一个勾选框：

```vue
<el-checkbox v-model="rememberMe">记住我</el-checkbox>
```

- 默认不勾选（`rememberMe = false`）
- 勾选后登录，调用 `useUserStoreHook().SET_ISREMEMBERED(true)`
- 不勾选登录，调用 `useUserStoreHook().SET_ISREMEMBERED(false)`

#### 2. H5 端默认开启

**文件**：`bk-pc/src/views/mobile/login/index.vue`

登录时自动设置：

```js
useUserStoreHook().SET_ISREMEMBERED(true);
```

H5 端不显示"记住我"勾选框，默认保持登录状态。

#### 3. 前端 Cookie 续期机制

**文件**：`bk-pc/src/utils/http/index.ts`

在响应拦截器中，每次成功响应后：

```js
if (Cookies.get(multipleTabsKey) && useUserStoreHook().isRemembered) {
  // 重新设置 multiple-tabs cookie，延长 7 天
  Cookies.set(multipleTabsKey, "true", { expires: loginDay });
}
```

这样前端 `multiple-tabs` Cookie 就与后端 Redis token 保持同步——只要用户在用，两边都不会过期。

#### 4. 登录参数扩展（可选）

**文件**：`bk-pc/src/types/auth.ts`

`LoginParams` 可增加 `rememberMe` 字段，便于后端记录用户偏好（当前后端未使用该字段，可选实现）。

```ts
export interface LoginParams {
  username: string;
  password: string;
  captcha: string;
  uuid: string;
  rememberMe?: boolean;
}
```

### 交互流程

#### PC 端 - 勾选"记住我"

```
用户勾选"记住我" → 点击登录 → 后端返回 token → 
setToken() 设置 multiple-tabs 为 7 天过期 → 
后续每次请求：后端续期 token + 前端续期 multiple-tabs cookie → 
用户持续保持登录状态
```

#### PC 端 - 不勾选"记住我"

```
用户不勾选"记住我" → 点击登录 → 后端返回 token → 
setToken() 设置 multiple-tabs 为会话 cookie → 
浏览器完全关闭后 multiple-tabs 自动清除 → 
下次打开需重新登录
```

#### H5 端

```
用户点击登录 → 自动设置 isRemembered=true → 
后续逻辑同 PC 端勾选"记住我"
```

## 改动文件清单

| 文件 | 改动内容 |
|------|----------|
| `bk-pc/src/views/login/index.vue` | 增加"记住我"勾选框及绑定逻辑 |
| `bk-pc/src/views/mobile/login/index.vue` | 登录时默认设置 `isRemembered=true` |
| `bk-pc/src/utils/http/index.ts` | 响应拦截器中增加 Cookie 续期逻辑 |
| `bk-pc/src/types/auth.ts` | `LoginParams` 可选增加 `rememberMe` 字段 |

## 边界情况

1. **用户手动清除 Cookie**：下次打开需重新登录，符合预期
2. **后端 Redis token 被踢出**（如管理员强制下线）：前端 401 响应，清除 token 跳转到登录页
3. **多端登录**：后端配置 `single-login: true`，同一账号后登录的会踢掉先登录的
4. **记住我有效期内修改密码**：不影响当前登录状态，下次登录需用新密码

## 安全性说明

- `multiple-tabs` 仅标记浏览器会话状态，不存储敏感信息
- 实际的认证 token 存储在 `authorized-token` Cookie 中
- 后端每次请求都校验 token 有效性，前端 Cookie 过期策略不影响安全性
