# 用户个人资料模块 PRD

## 一、功能描述

### 1.1 功能概述

用户个人资料模块用于管理用户的个人信息和账户设置。用户可以通过右上角头像进入个人资料页面，查看和编辑个人信息，包括昵称、性别、邮箱、手机号、头像等，同时可以修改密码。

### 1.2 功能需求

- 在右上角用户头像下拉菜单中添加"个人资料"入口
- 首页加载时获取用户信息并存入store，供导航栏显示昵称和头像
- 进入个人资料页面，显示用户信息
- 编辑用户基本信息（昵称、性别、邮箱、手机号、头像）
- 修改密码功能
- 保存修改后的用户信息
- 调用后端接口：GET /bookkeepingUser、PUT /bookkeepingUser、POST /bookkeepingUser/changePwd、POST /bookkeepingUser/avatar
- 移除导航栏的消息通知铃铛图标

---

## 二、用户流程

```
用户点击右上角头像
         ↓
下拉菜单显示
         ↓
点击"个人资料"
         ↓
进入个人资料编辑页面（调用 GET /bookkeepingUser）
         ↓
查看用户信息（用户名、密码、昵称、性别、邮箱、手机号、头像）
         ↓
[可选] 修改密码 → 弹出修改密码弹窗 → 输入当前密码、新密码、确认新密码 → 提交 POST /bookkeepingUser/changePwd
         ↓
[可选] 编辑个人信息 → 修改昵称/性别/邮箱/手机号/头像
         ↓
点击保存 → 调用 PUT /bookkeepingUser 更新用户信息
         ↓
保存成功 → 显示成功提示 → 更新页面数据和store
```

---

## 三、接口定义

### 3.1 获取用户信息

**请求**

```
GET /bookkeepingUser
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
    "avatar": "/uploads/tmp/avatar_123.png"
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
| avatar | String | 用户头像相对路径（如 /uploads/tmp/xxx.png） |

### 3.2 更新用户信息

**请求**

```
PUT /bookkeepingUser
```

**请求体**

```json
{
  "id": 1,
  "nickName": "管理员",
  "gender": "MALE",
  "email": "admin@example.com",
  "mobilePhone": "+86 13800138000",
  "avatar": "tmp_123.png"
}
```

**字段说明**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户ID |
| nickName | String | 是 | 昵称，20字以内 |
| gender | String | 否 | 性别（MALE-男，FEMALE-女） |
| email | String | 否 | 电子邮件地址 |
| mobilePhone | String | 否 | 手机号（包含国际代码和手机号） |
| avatar | String | 否 | 头像文件名称（使用上传接口返回的fileName） |

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
    "avatar": "/uploads/tmp/avatar_123.png"
  }
}
```

### 3.3 修改密码

**请求**

```
POST /bookkeepingUser/changePwd
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
POST /bookkeepingUser/avatar
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
    "url": "/uploads/tmp/tmp_1234567890.png",
    "fileName": "tmp_1234567890.png"
  }
}
```

**字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| url | String | 头像文件的相对路径，用于前端预览显示 |
| fileName | String | 头像文件名称，保存用户资料时传入avatar字段 |

---

## 四、数据结构

### 4.1 用户信息 (BookkeepingUser)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户ID |
| username | String | 用户名（只读） |
| nickName | String | 昵称 |
| gender | String | 性别（MALE-男，FEMALE-女） |
| email | String | 电子邮件 |
| mobilePhone | String | 手机号 |
| avatar | String | 头像相对路径，显示时需拼接BASE_URL |

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
│  │  │  [头像]  │  点击上传头像（编辑状态下显示）        │   │
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
| nickName | 输入框 | 最大20字符，提交时校验不能为空 | 用户昵称 |
| gender | 单选框 | 非必选 | MALE-男，FEMALE-女 |
| email | 输入框 | 邮箱格式校验（正则：`^[^\s@]+@[^\s@]+\.[^\s@]+$`） | 需符合电子邮件格式 |
| mobilePhone | 输入框 | 手机号格式校验（正则：`^\+?\d{1,3}[\s-]?\d{6,14}$`） | 需包含国际代码和手机号 |
| avatar | 图片上传 | 仅编辑状态可见、文件类型/大小限制 | 支持 JPG、PNG 格式，< 2MB |

---

## 六、操作逻辑

### 6.1 首页加载

1. 首页加载时调用 GET /bookkeepingUser 接口获取用户信息
2. 获取成功后，将昵称（nickName）和头像URL（拼接BASE_URL）存入store
3. 导航栏从store中读取昵称和头像进行显示

### 6.2 进入个人资料页面

1. 用户点击右上角头像 → 下拉菜单显示
2. 点击"个人资料" → 跳转到 /user/profile 页面
3. 页面加载时调用 GET /bookkeepingUser 接口获取用户信息
4. 获取成功后填充表单数据
5. 非编辑状态下，头像显示：BASE_URL + avatar相对路径

### 6.3 编辑状态与头像上传

1. 只有在编辑状态下才能上传头像
2. 点击头像区域，打开文件选择器
3. 选择图片文件（支持 JPG、PNG）
4. 校验文件大小（< 2MB）
5. 调用 POST /bookkeepingUser/avatar 接口上传
6. 获取返回的头像URL（用于预览）和fileName（用于提交）
7. 编辑状态下头像显示：BASE_URL + 上传返回的url
8. **注意：上传头像后不更新store中的头像，只有点击保存后才更新**

### 6.4 修改密码

1. 点击"修改密码"按钮 → 弹出修改密码弹窗
2. 用户输入：当前密码、新密码、确认新密码
3. 点击"确定"前进行前端校验：
   - 当前密码不能为空
   - 新密码不能为空
   - 确认新密码不能为空
   - 新密码与确认新密码必须一致
   - 新密码长度校验（后端校验）
4. 校验通过后调用 POST /bookkeepingUser/changePwd 接口
5. 成功后：
   - 关闭弹窗
   - 显示成功提示"修改密码成功"
6. 失败后：
   - 显示错误提示（当前密码错误等）

### 6.5 保存个人信息

1. 用户编辑昵称、性别、邮箱、手机号
2. 如果上传了新头像，avatar字段值为上传返回的fileName
3. 点击"保存"按钮
4. 前端校验：
   - 昵称：不能为空，最大20字符
   - 邮箱：符合邮箱格式（可选填写）
   - 手机号：符合手机号格式（可选填写）
5. 校验通过后调用 PUT /bookkeepingUser 接口
6. 成功后：
   - 显示成功提示"保存成功"
   - 使用接口返回的最新用户数据更新页面
   - 将最新的昵称和头像URL更新到store，导航栏同步更新
7. 失败后：
   - 显示错误提示

---

## 七、数据校验规则

### 7.1 昵称

| 规则 | 说明 |
|------|------|
| 必填 | 是 |
| 最大长度 | 20个字符 |
| 提示 | 昵称不能为空 / 昵称不能超过20个字符 |

### 7.2 邮箱

| 规则 | 说明 |
|------|------|
| 必填 | 否 |
| 格式 | 符合邮箱格式（正则：`^[^\s@]+@[^\s@]+\.[^\s@]+$`） |
| 提示 | 请输入正确的邮箱地址 |

### 7.3 手机号

| 规则 | 说明 |
|------|------|
| 必填 | 否 |
| 格式 | 国际代码 + 手机号（如 +86 13800138000，正则：`^\+?\d{1,3}[\s-]?\d{6,14}$`） |
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
| `bk-pc/src/api/user.ts` | 添加用户资料相关 API（getUserInfo、updateUserInfo、changePassword、uploadAvatar） |
| `bk-pc/src/views/user/Profile.vue` | 个人资料页面主组件 |
| `bk-pc/src/views/user/ChangePasswordDialog.vue` | 修改密码弹窗组件 |

### 8.2 需要修改的文件

| 文件路径 | 修改内容 |
|----------|----------|
| `bk-pc/src/router/modules/user.ts` | 添加路由：/user/profile |
| `bk-pc/src/layout/components/lay-navbar/index.vue` | 添加"个人资料"菜单项，移除消息通知铃铛 |
| `bk-pc/src/layout/hooks/useNav.ts` | 头像显示逻辑：非完整URL时拼接BASE_URL |
| `bk-pc/src/views/dashboard/index.vue` | 首页加载时调用getUserInfo并存入store |
| `bk-pc/locales/zh-CN.yaml` | 添加个人资料相关国际化文案 |
| `bk-pc/locales/en.yaml` | 添加个人资料相关国际化文案 |

### 8.3 后端需要修改的文件

| 文件路径 | 修改内容 |
|----------|----------|
| `bk-server/.../config/SpringSecurityConfig.java` | 添加 /uploads/** 路径放行 |
| `bk-server/.../aspect/LogAspect.java` | 添加 MultipartFile.class 到排除参数列表 |

---

## 九、验收标准

### 9.1 功能验收

- [ ] 首页加载时获取用户信息并存入store
- [ ] 导航栏正确显示用户昵称和头像
- [ ] 导航栏移除了消息通知铃铛图标
- [ ] 右上角头像下拉菜单显示"个人资料"入口
- [ ] 点击"个人资料"进入个人资料页面
- [ ] 页面正确显示用户名（只读）
- [ ] 密码显示为掩码，有"修改密码"按钮
- [ ] 点击"修改密码"弹出修改密码弹窗
- [ ] 修改密码表单校验逻辑正确
- [ ] 修改密码成功后显示成功提示
- [ ] 非编辑状态下不能上传头像
- [ ] 昵称可编辑，最大20字符，必填校验
- [ ] 性别单选框可选择男/女
- [ ] 邮箱可编辑，格式校验正确
- [ ] 手机可编辑，格式校验正确
- [ ] 头像可上传，显示预览
- [ ] 点击"保存"成功更新用户信息
- [ ] 保存成功后显示成功提示
- [ ] 保存成功后导航栏头像和昵称同步更新

### 9.2 交互验收

- [ ] 页面加载时显示加载状态
- [ ] 保存按钮加载中状态
- [ ] 修改密码弹窗加载中状态
- [ ] 错误信息正确显示
- [ ] 成功信息正确显示

### 9.3 数据校验

- [ ] 昵称为空提示错误
- [ ] 昵称超过20字符提示错误
- [ ] 邮箱格式错误提示错误
- [ ] 手机号格式错误提示错误
- [ ] 新密码与确认密码不一致提示错误

### 9.4 头像逻辑验收

- [ ] 非编辑状态下头像显示：BASE_URL + avatar相对路径
- [ ] 编辑状态下上传头像前显示原头像
- [ ] 编辑状态下上传头像后显示预览头像
- [ ] 编辑状态下上传头像后，导航栏头像不变化
- [ ] 保存成功后，导航栏头像更新为最新头像

---

## 十、技术实现要点

### 10.1 首页加载用户信息

```typescript
// 首页加载时获取用户信息并存入store
import { getUserInfo } from "@/api/user";
import { useUserStoreHook } from "@/store/modules/user";

const BASE_URL = import.meta.env.VITE_BASE_URL as string;
const userStore = useUserStoreHook();

const loadUserInfo = async () => {
  const userResult = await getUserInfo();
  const avatarUrl = userResult.avatar.startsWith("http")
    ? userResult.avatar
    : `${BASE_URL}${userResult.avatar}`;
  userStore.SET_AVATAR(avatarUrl);
  userStore.SET_NICKNAME(userResult.nickName);
  userStore.SET_ID(userResult.id);
};
```

### 10.2 导航栏头像显示

```typescript
// useNav.ts 中头像处理逻辑
const userAvatar = computed((): string => {
  const store = useUserStoreHook();
  let avatar = store.avatar;
  if (!avatar) {
    return Avatar;
  }
  if (avatar.startsWith("http")) {
    return avatar;
  }
  return `${BASE_URL}${avatar}`;
});
```

### 10.3 用户信息获取

```typescript
// 调用 GET /bookkeepingUser 接口获取用户信息
const loadUserInfo = async () => {
  const result = await getUserInfo();
  userInfo.value = result;
  // 非编辑状态头像显示
  avatarUrl.value = result.avatar.startsWith("http")
    ? result.avatar
    : `${BASE_URL}${result.avatar}`;
};
```

### 10.4 用户信息更新

```typescript
// 调用 PUT /bookkeepingUser 接口更新用户信息
// 注意：avatar 字段传入上传头像返回的 fileName
const handleSave = async () => {
  const updateData: Partial<BookkeepingUser> = {
    id: userInfo.value.id,
    nickName: editForm.value.nickName,
    gender: editForm.value.gender,
    email: editForm.value.email,
    mobilePhone: editForm.value.mobilePhone
  };
  if (pendingAvatar.value) {
    updateData.avatar = pendingAvatar.value;
  }
  const result = await updateUserInfo(updateData);
  // 使用返回的最新数据更新页面
  userInfo.value = result;
  // 更新store中的用户信息
  const avatarUrl = result.avatar.startsWith("http")
    ? result.avatar
    : `${BASE_URL}${result.avatar}`;
  userStore.SET_NICKNAME(result.nickName);
  userStore.SET_AVATAR(avatarUrl);
};
```

### 10.5 修改密码

```typescript
// 调用 POST /bookkeepingUser/changePwd 接口修改密码
const handleChangePassword = async () => {
  await changePassword({
    oldPassword: passwordForm.oldPassword,
    newPassword: passwordForm.newPassword,
    confirmNewPassword: passwordForm.confirmNewPassword
  });
};
```

### 10.6 上传头像（仅编辑状态可见）

```typescript
// 调用 POST /bookkeepingUser/avatar 接口上传头像
// 仅在编辑状态下可用
const handleAvatarChange = (uploadFile: any) => {
  const file = uploadFile.raw;
  if (!file) return;

  if (!file.type.startsWith("image/")) {
    ElMessage.error("请选择图片文件");
    return;
  }

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error("图片大小不能超过2MB");
    return;
  }

  const formData = new FormData();
  formData.append("file", file);

  uploadAvatar(formData).then(result => {
    // 保存fileName用于提交，保存url用于预览
    pendingAvatar.value = result.fileName;
    pendingAvatarUrl.value = result.url;
    userInfo.value.avatar = result.url;
  });
};
```