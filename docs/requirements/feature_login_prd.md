# 用户登录模块 PRD

## 一、功能描述

### 1.1 功能概述

用户登录模块是 BK-PC 记账管理系统的入口模块，负责用户身份认证、Token 管理和会话维护。

### 1.2 功能需求

- 用户名密码登录
- 验证码校验
- Token 令牌管理
- 自动登录状态保持
- 退出登录

---

## 二、用户流程

### 2.1 登录流程

```
┌─────────────────────────────────────────────────────────────┐
│                       用户登录流程                           │
├─────────────────────────────────────────────────────────────┤
│  1. 用户访问 /login 页面                                    │
│           ↓                                                 │
│  2. 调用 /auth/captcha 获取验证码图片 + uuid                │
│           ↓                                                 │
│  3. 用户输入用户名 + 密码 + 验证码                           │
│           ↓                                                 │
│  4. 点击登录，调用 /auth/login 接口                          │
│           ↓                                                 │
│  5. 后端验证：                                               │
│     - 验证码有效性                                           │
│     - 用户名密码正确性                                       │
│           ↓                                                 │
│  6. 验证成功：                                               │
│     - 返回 token + userInfo                                 │
│     - 存储 token 到 localStorage                            │
│     - 存储用户信息到 UserStore                              │
│     - 跳转首页 /dashboard                                   │
│           ↓                                                 │
│  7. 验证失败：                                               │
│     - 显示错误提示                                          │
│     - 刷新验证码                                            │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 退出流程

```
用户点击退出按钮
        ↓
调用 /auth/logout 接口
        ↓
清除 localStorage 中的 token
        ↓
清除 UserStore 中的用户信息
        ↓
跳转登录页 /login
```

---

## 三、接口定义

### 3.1 获取验证码

**请求**

```
GET /auth/captcha
```

**响应**

```json
{
  "success": true,
  "data": {
    "img": "data:image/png;base64,...",
    "uuid": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

**字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| img | String | 验证码图片 Base64 |
| uuid | String | 验证码唯一标识 |

### 3.2 用户登录

**请求**

```
POST /auth/login
Content-Type: application/json
```

**请求体**

```json
{
  "username": "admin",
  "password": "123456",
  "captcha": "abcd",
  "uuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

**字段说明**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |
| captcha | String | 是 | 验证码 |
| uuid | String | 是 | 验证码 UUID |

**响应（成功）**

```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "nickName": "管理员",
      "avatar": "",
      "email": "admin@example.com",
      "phone": "",
      "type": "1",
      "enabled": "1"
    }
  }
}
```

**响应（失败）**

```json
{
  "success": false,
  "message": "用户名或密码错误"
}
```

### 3.3 获取用户信息

**请求**

```
POST /auth/user-info
```

**请求头**

```
Authorization: Bearer <token>
```

**响应**

```json
{
  "success": true,
  "data": {
    "user": { ... },
    "permission": ["bill:add", "bill:edit", "bill:delete"]
  }
}
```

### 3.4 退出登录

**请求**

```
POST /auth/logout
```

**请求头**

```
Authorization: Bearer <token>
```

**响应**

```json
{
  "success": true
}
```

---

## 四、前端页面设计

### 4.1 登录页布局

```
┌────────────────────────────────────────────────────────┐
│  左侧区域 (40%)          │  右侧登录表单 (60%)         │
│  ┌──────────────────┐   │  ┌────────────────────────┐ │
│  │                  │   │  │     BK-PC 记账系统      │ │
│  │    背景图        │   │  ├────────────────────────┤ │
│  │    + 欢迎语      │   │  │ 用户名: [________]      │ │
│  │                  │   │  ├────────────────────────┤ │
│  │                  │   │  │ 密码:   [________]      │ │
│  │                  │   │  ├────────────────────────┤ │
│  │                  │   │  │ 验证码: [____] [图片]   │ │
│  │                  │   │  ├────────────────────────┤ ��
│  │                  │   │  │ [ ] 记住密码            │ │
│  │                  │   │  ├────────────────────────┤ │
│  │                  │   │  │     [ 登 录 ]          │ │
│  └──────────────────┘   │  └────────────────────────┘ │
└────────────────────────────────────────────────────────┘
```

### 4.2 组件设计

| 组件 | 说明 |
|------|------|
| LoginForm | 登录表单组件 |
| CaptchaImage | 验证码图片组件 |
| RememberMe | 记住密码复选框 |

---

## 五、数据结构

### 5.1 登录请求参数 (LoginParams)

```typescript
interface LoginParams {
  username: string
  password: string
  captcha: string
  uuid: string
}
```

### 5.2 登录响应 (LoginResult)

```typescript
interface LoginResult {
  token: string
  user: UserInfo
}
```

### 5.3 用户信息 (UserInfo)

```typescript
interface UserInfo {
  id: number
  username: string
  nickName: string
  avatar: string
  email: string
  phone: string
  type: '0' | '1'
  enabled: '0' | '1'
  roles?: Role[]
}
```

### 5.4 验证码响应 (CaptchaResult)

```typescript
interface CaptchaResult {
  img: string
  uuid: string
}
```

---

## 六、异常处理

### 6.1 错误码与提示

| 错误场景 | 提示信息 |
|----------|----------|
| 用户名为空 | 请输入用户名 |
| 密码为空 | 请输入密码 |
| 验证码为空 | 请输入验证码 |
| 验证码错误 | 验证码错误 |
| 验证码已过期 | 验证码已过期，请重新获取 |
| 用户名或密码错误 | 用户名或密码错误 |
| 账号被禁用 | 账号已被禁用，请联系管理员 |
| 网络错误 | 网络错误，请稍后重试 |

### 6.2 前端处理逻辑

```typescript
// 错误处理示例
switch (error.code) {
  case 'CAPTCHA_EXPIRED':
    refreshCaptcha()
    break
  case 'INVALID_CREDENTIALS':
    ElMessage.error('用户名或密码错误')
    break
  case 'USER_DISABLED':
    ElMessage.error('账号已被禁用')
    break
  default:
    ElMessage.error(error.message || '登录失败')
}
```

---

## 七、文件清单

### 7.1 需要新建的文件

| 文件路径 | 职责描述 |
|----------|----------|
| `bk-pc/src/api/auth.ts` | 定义登录、登出、验证码、用户信息等 API 方法 |
| `bk-pc/src/store/modules/user.ts` | 用户状态管理：token、用户信息、权限 |
| `bk-pc/src/views/login/index.vue` | 登录页面主组件 |
| `bk-pc/src/views/login/components/LoginForm.vue` | 登录表单组件 |
| `bk-pc/src/views/login/components/CaptchaImage.vue` | 验证码图片组件 |
| `bk-pc/src/router/modules/login.ts` | 登录路由配置 |
| `bk-pc/src/types/auth.ts` | 登录相关 TypeScript 类型定义 |

### 7.2 需要修改的文件

| 文件路径 | 修改内容 |
|----------|----------|
| `bk-pc/src/router/index.ts` | 添加登录路由白名单、路由守卫逻辑 |
| `bk-pc/src/utils/http/index.ts` | 配置 Token 自动注入、401 处理 |
| `bk-pc/src/utils/auth.ts` | 添加 Token 存取方法 |
| `bk-pc/src/layout/components/lay-navbar/index.vue` | 添加用户信息显示、退出按钮 |

---

## 八、验收标准

### 8.1 功能验收

- [ ] 页面打开时自动获取验证码
- [ ] 点击验证码图片可刷新
- [ ] 用户名密码验证码必填校验
- [ ] 登录成功跳转首页
- [ ] 登录失败显示错误提示
- [ ] 刷新页面保持登录状态
- [ ] 退出登录清除状态并跳转登录页

### 8.2 交互验收

- [ ] 登录按钮加载中状态
- [ ] 回车键触发登录
- [ ] 错误输入框抖动提示
- [ ] 验证码输入框自动聚焦

### 8.3 性能验收

- [ ] 首次加载 < 2s
- [ ] 登录请求响应 < 1s

---

## 九、技术实现要点

### 9.1 Token 存储

- 使用 localStorage 存储 token 和用户信息
- 字段名：`user-info`
- 存储内容：
  ```typescript
  {
    accessToken: string,      // 访问令牌
    refreshToken: string,     // 刷新令牌
    expires: number,          // 过期时间戳
    id: number,               // 用户ID（重要：用于后续接口调用）
    avatar: string,           // 头像
    username: string,         // 用户名
    nickname: string,         // 昵称
    roles: string[],          // 角色列表
    permissions: string[]     // 权限列表
  }
  ```

### 9.2 用户信息持久化

登录成功后，用户信息（包含 userId）需要持久化到本地存储，确保：
- 刷新页面后可以获取用户ID
- 后续接口调用需要使用 userId 参数

### 9.2 路由守卫

```typescript
// 白名单路由
const whiteList = ['/login', '/register']

router.beforeEach((to, from, next) => {
  const hasToken = getToken()
  if (hasToken) {
    if (to.path === '/login') {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})
```

### 9.3 记住密码

- 使用 localStorage 存储用户名和密码（加密）
- 字段名：`bk_remember`