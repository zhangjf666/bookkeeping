# 用户个人资料模块 PRD

## 一、功能描述

### 1.1 功能概述

用户个人资料模块用于管理用户的个人信息和账户设置。用户可以通过右上角头像进入个人资料页面，查看和编辑个人信息，包括昵称、性别、邮箱、手机号、头像等，同时可以修改密码。

### 1.2 功能需求

- 在右上角用户头像下拉菜单中添加"个人资料"入口
- 进入个人资料页面，显示用户信息
- 编辑用户基本信息（昵称、性别、邮箱、手机号、头像）
- 修改密码功能
- 保存修改后的用户信息
- 调用后端接口：GET /user、PUT /user、POST /user/changePwd

---

## 二、用户流程

```
用户点击右上角头像
         ↓
下拉菜单显示
         ↓
点击"个人资料"
         ↓
进入个人资料编辑页面（调用 GET /user）
         ↓
查看用户信息（用户名、密码、昵称、性别、邮箱、手机号、头像）
         ↓
[可选] 修改密码 → 弹出修改密码弹窗 → 输入当前密码、新密码、确认新密码 → 提交 POST /user/changePwd
         ↓
[可选] 编辑个人信息 → 修改昵称/性别/邮箱/手机号/头像
         ↓
点击保存 → 调用 PUT /user 更新用户信息
         ↓
保存成功 → 显示成功提示 → 刷新页面数据
```

---

## 三、接口定义

### 3.1 获取用户信息

**请求**

```
GET /user
```

**响应**

```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "id": 1,
    "username": "admin",
    "nickName": "管理员",
    "gender": "MALE",
    "email": "admin@example.com",
    "mobilePhone": "+86 13800138000",
    "avatar": "https://example.com/avatar.jpg"
  }
}
```

**字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户ID |
| username | String | 用户名 |
| nickName | String | 昵称 |
| gender | String | 性别（MALE-男，FEMALE-女） |
| email | String | 电子邮件 |
| mobilePhone | String | 手机号（包含国际代码） |
| avatar | String | 用户头像URL |

### 3.2 更新用户信息

**请求**

```
PUT /user
```

**请求体**

```json
{
  "nickName": "管理员",
  "gender": "MALE",
  "email": "admin@example.com",
  "mobilePhone": "+86 13800138000",
  "avatar": "https://example.com/avatar.jpg"
}
```

**字段说明**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickName | String | 是 | 昵称，20字以内 |
| gender | String | 否 | 性别（MALE-男，FEMALE-女） |
| email | String | 否 | 电子邮件地址 |
| mobilePhone | String | 否 | 手机号（包含国际代码和手机号） |
| avatar | String | 否 | 用户头像URL |

### 3.3 修改密码

**请求**

```
POST /user/changePwd
```

**请求体**

```json
{
  "oldPassword": "123456",
  "newPassword": "abcdef",
  "confirmNewPassword": "abcdef"
}
```

**字段说明**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | String | 是 | 当前密码 |
| newPassword | String | 是 | 新密码 |
| confirmNewPassword | String | 是 | 确认新密码 |

**响应（成功）**

```json
{
  "code": 0,
  "msg": "修改密码成功",
  "data": null
}
```

**响应（失败）**

```json
{
  "code": 400,
  "msg": "当前密码错误",
  "data": null
}
```

### 3.4 上传用户头像

**请求**

```
POST /user/avatar
Content-Type: multipart/form-data
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 头像文件（支持 JPG、PNG） |

**响应（成功）**

```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "url": "https://example.com/avatars/avatar_123.jpg",
    "fileName": "avatar_123.jpg"
  }
}
```

**字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| url | String | 头像文件的URL，用于头像图片显示 |
| fileName | String | 头像文件名称，保存用户资料时传入avatar字段 |

---

## 四、数据结构

### 4.1 用户信息 (UserProfile)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户ID |
| username | String | 用户名（只读） |
| nickName | String | 昵称 |
| gender | String | 性别（MALE-男，FEMALE-女） |
| email | String | 电子邮件 |
| mobilePhone | String | 手机号 |
| avatar | String | 头像文件名（保存时使用 fileName，上传后返回 url 用于预览） |

### 4.2 修改密码表单 (ChangePasswordForm)

| 字段 | 类型 | 说明 |
|------|------|------|
| oldPassword | String | 当前密码 |
| newPassword | String | 新密码 |
| confirmNewPassword | String | 确认新密码 |

---

## 五、页面设计

### 5.1 入口位置

在右上角用户头像下拉菜单中添加"个人资料"菜单项：

```
┌─────────────────────────────────────────────────────────────┐
│  [头像] 用户名                                             │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ 👤 个人资料                                           │  │
│  │ 🚪 退出登录                                           │  │
│  └─────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 个人资料页面布局

```
┌─────────────────────────────────────────────────────────────┐
│  个人资料                                                    │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 头像                                                   │   │
│  │  ┌──────────┐                                        │   │
│  │  │  [头像]  │  点击上传头像                           │   │
│  │  └──────────┘                                        │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 用户名                    [admin]          （不可编辑）│   │
│  │                                                   │   │
│  │ 密码                      [********]  [修改密码]   │   │
│  │                                                   │   │
│  │ 昵称                      [__________]           │   │
│  │                                                   │   │
│  │ 性别                       (●) 男  ( ) 女         │   │
│  │                                                   │   │
│  │ 电子邮件                  [__________]           │   │
│  │                                                   │   │
│  │ 手机                      [__________]            │   │
│  │                                                   │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                               │
│                                          [ 保 存 ]           │
└─────────────────────────────────────────────────────────────┘
```

### 5.3 修改密码弹窗

```
┌─────────────────────────────────────────────────────────────┐
│  修改密码                                          [X]     │
├─────────────────────────────────────────────────────────────┤
│  当前密码:                      [__________]              │
│                                                             │
│  新密码:                           [__________]              │
│                                                             │
│  确认新密码:                       [__________]              │
│                                                             │
│                                  [取消]  [确定]             │
└─────────────────────────────────────────────────────────────┘
```

### 5.4 字段说明

| 字段 | 组件类型 | 校验规则 | 说明 |
|------|----------|----------|------|
| username | 文本展示 | 不可编辑 | 显示用户登录名 |
| password | 按钮 | 点击弹出修改密码弹窗 | 显示为掩码，点击"修改密码"按钮 |
| nickName | 输入框 | 最大20字符 | 用户昵称 |
| gender | 单选框 | 必选 | MALE-男，FEMALE-女 |
| email | 输入框 | 邮箱格式校验 | 需符合电子邮件格式 |
| mobilePhone | 输入框 | 手机号格式校验 | 需包含国际代码和手机号 |
| avatar | 图片上传 | 文件类型/大小限制 | 支持 JPG、PNG 格式，< 2MB |

---

## 六、操作逻辑

### 6.1 进入页面

1. 用户点击右上角头像 → 下拉菜单显示
2. 点击"个人资料" → 跳转到 /user-profile 页面
3. 页面加载时调用 GET /user 接口获取用户信息
4. 获取成功后填充表单数据

### 6.2 修改密码

1. 点击"修改密码"按钮 → 弹出修改密码弹窗
2. 用户输入：当前密码、新密码、确认新密码
3. 点击"确定"前进行前端校验：
   - 当前密码不能为空
   - 新密码不能为空
   - 确认新密码不能为空
   - 新密码与确认新密码必须一致
   - 新密码长度校验（后端校验）
4. 校验通过后调用 POST /user/changePwd 接口
5. 成功后：
   - 关闭弹窗
   - 显示成功提示"修改密码成功"
6. 失败后：
   - 显示错误提示（当前密码错误等）

### 6.3 保存个人信息

1. 用户编辑昵称、性别、邮箱、手机号
2. 点击"保存"按钮
3. 前端校验：
   - 昵称：最大20字符
   - 邮箱：符合邮箱格式
   - 手机号：符合手机号格式
4. 校验通过后调用 PUT /user 接口
5. 成功后：
   - 显示成功提示"保存成功"
   - 更新本地存储的用户信息
6. 失败后：
   - 显示错误提示

### 6.4 上传头像

1. 用户点击头像区域
2. 打开文件选择器
3. 选择图片文件（支持 JPG、PNG）
4. 校验文件大小（< 2MB）
5. 调用上传接口（需后端提供头像上传接口）
6. 获取返回的头像URL
7. 预览新头像
8. 保存时将头像URL一并提交

---

## 七、数据校验规则

### 7.1 昵称

| 规则 | 说明 |
|------|------|
| 必填 | 是 |
| 最大长度 | 20个字符 |
| 提示 | 昵称不能超过20个字符 |

### 7.2 邮箱

| 规则 | 说明 |
|------|------|
| 必填 | 否 |
| 格式 | 符合 RFC 5322 标准 |
| 提示 | 请输入正确的邮箱地址 |

### 7.3 手机号

| 规则 | 说明 |
|------|------|
| 必填 | 否 |
| 格式 | 国际代码 + 手机号（如 +86 13800138000） |
| 提示 | 请输入正确的手机号 |

### 7.4 密码

| 规则 | 说明 |
|------|------|
| 当前密码 | 必填 |
| 新密码 | 必填，最小6位 |
| 确认新密码 | 必填，需与新密码一致（字段名：confirmNewPassword） |
| 提示 | 两次输入的密码不一致 |

---

## 八、文件清单

### 8.1 需要新建的文件

| 文件路径 | 职责描述 |
|----------|----------|
| `bk-pc/src/api/user.ts` | 添加用户资料相关 API（已在用，需补充） |
| `bk-pc/src/views/user/Profile.vue` | 个人资料页面主组件 |
| `bk-pc/src/views/user/ChangePasswordDialog.vue` | 修改密码弹窗组件 |

### 8.2 需要修改的文件

| 文件路径 | 修改内容 |
|----------|----------|
| `bk-pc/src/router/modules/user.ts` | 添加路由：/user/profile |
| `bk-pc/src/layout/components/lay-navbar/index.vue` | 添加"个人资料"菜单项 |
| `bk-pc/locales/zh-CN.yaml` | 添加个人资料相关国际化文案 |
| `bk-pc/locales/en.yaml` | 添加个人资料相关国际化文案 |

---

## 九、验收标准

### 9.1 功能验收

- [ ] 右上角头像下拉菜单显示"个人资料"入口
- [ ] 点击"个人资料"进入个人资料页面
- [ ] 页面正确显示用户名（只读）
- [ ] 密码显示为掩码，有"修改密码"按钮
- [ ] 点击"修改密码"弹出修改密码弹窗
- [ ] 修改密码表单校验逻辑正确
- [ ] 修改密码成功后显示成功提示
- [ ] 昵称可编辑，最大20字符
- [ ] 性别单选框可选择男/女
- [ ] 邮箱可编辑，格式校验正确
- [ ] 手机可编辑，格式校验正确
- [ ] 头像可上传，显示预览
- [ ] 点击"保存"成功更新用户信息
- [ ] 保存成功后显示成功提示

### 9.2 交互验收

- [ ] 页面加载时显示加载状态
- [ ] 保存按钮加载中状态
- [ ] 修改密码弹窗加载中状态
- [ ] 输入框失焦时触发校验
- [ ] 错误信息正确显示
- [ ] 成功信息正确显示

### 9.3 数据校验

- [ ] 昵称超过20字符提示错误
- [ ] 邮箱格式错误提示错误
- [ ] 手机号格式错误提示错误
- [ ] 新密码与确认密码不一致提示错误

---

## 十、技术实现要点

### 10.1 用户信息获取

```typescript
// 调用 GET /bookkeepingUser 接口获取用户信息
const fetchUserInfo = async () => {
  const result = await getUserInfo()
  userInfo.value = result
}
```

### 10.2 用户信息更新

```typescript
// 调用 PUT /bookkeepingUser 接口更新用户信息
// 注意：avatar 字段传入上传头像返回的 fileName
const handleSave = async () => {
  await updateUserInfo({
    nickName: formData.nickName,
    gender: formData.gender,
    email: formData.email,
    mobilePhone: formData.mobilePhone,
    avatar: avatarFileName.value // 上传头像返回的 fileName
  })
}
```

### 10.3 修改密码

```typescript
// 调用 POST /bookkeepingUser/changePwd 接口修改密码
const handleChangePassword = async () => {
  await changePassword({
    oldPassword: passwordForm.oldPassword,
    newPassword: passwordForm.newPassword,
    confirmNewPassword: passwordForm.confirmNewPassword
  })
}
```

### 10.4 上传头像

```typescript
// 调用 POST /bookkeepingUser/avatar 接口上传头像
const handleAvatarUpload = async (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  const result = await uploadAvatar(formData)
  // result = { url: 'https://...', fileName: 'avatar.jpg' }
  avatarUrl.value = result.url
  avatarFileName.value = result.fileName
}
```
