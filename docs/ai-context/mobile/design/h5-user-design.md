# H5 我的页面详细设计文档

## 1. 页面概述

### 1.1 页面定位
"我的"页面是用户个人中心入口，提供用户信息展示、个人设置、账本管理、分类管理、标签管理、备注管理、常用设置等功能。

### 1.2 页面特性
- **一级页面**：无顶部导航栏，有底部 TabBar
- **分组列表**：使用 `van-cell-group` 分组展示功能入口
- **多级页面**：部分功能跳转到独立的二级页面

### 1.3 页面结构

```
├── 我的 (/user/profile)                    # 一级页面，主入口
│   ├── 用户信息卡片                         # 头像、昵称、用户名，点击进入个人设置
│   └── 功能列表                            # 分组展示各功能入口
│
├── 个人设置 (/user/settings/profile)       # 二级页面
│   ├── 头像上传
│   ├── 用户名（只读）
│   ├── 昵称编辑
│   ├── 性别选择
│   ├── 邮箱编辑
│   └── 手机编辑
│
├── 修改密码 (/user/settings/password)      # 二级页面
│   ├── 原密码输入
│   ├── 新密码输入
│   └── 确认密码输入
│
├── 常用设置 (/user/settings/common)        # 二级页面
│   ├── 信用卡默认开关
│   ├── 支出限额显示模式
│   ├── 月支出限额
│   └── 年支出限额
│
├── 账本管理 (/user/settings/account-book)  # 二级页面
│   ├── 搜索框
│   ├── 账本列表（左滑删除）
│   └── 新增/编辑弹窗
│
├── 分类管理 (/user/settings/classify)      # 二级页面
│   ├── 类型筛选（支出/收入）
│   ├── 分类列表
│   └── 新增/编辑弹窗
│
├── 备注管理 (/user/settings/remark)        # 二级页面
│   ├── 搜索框
│   ├── 备注列表（左滑删除）
│   └── 新增/编辑弹窗
│
└── 标签管理 (/user/settings/tag)           # 二级页面
    ├── 搜索框
    ├── 标签列表（左滑删除）
    └── 新增/编辑弹窗
```

---

## 2. 我的页面主页面设计

### 2.1 页面布局

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ┌────┐                                             │   │
│  │  │头像│  昵称：张三                          →      │   │  ← 用户信息卡片，点击进入个人设置
│  │  │    │  账号：zhangsan                           │   │
│  │  └────┘                                             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  账本管理                                    →      │   │
│  │  分类管理                                    →      │   │
│  │  标签管理                                    →      │   │
│  │  备注管理                                    →      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  常用设置                                    →      │   │
│  │  修改密码                                    →      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  关于                                        →      │   │
│  │  退出登录                                           │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  底部 TabBar                                                │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 用户信息卡片

```vue
<template>
  <div class="user-card" @click="handleGoProfile">
    <div class="avatar-section">
      <van-image
        round
        width="60"
        height="60"
        :src="userAvatar"
        :error-icon="defaultAvatar"
      />
    </div>
    <div class="info-section">
      <div class="nickname">
        <span class="label">{{ t('mobile.user.nickname') }}</span>
        <span class="value">{{ userInfo?.nickname || '-' }}</span>
      </div>
      <div class="username">
        <span class="label">{{ t('mobile.user.username') }}</span>
        <span class="value">{{ userInfo?.username }}</span>
      </div>
    </div>
    <van-icon name="arrow" class="arrow-icon" />
  </div>
</template>
```

**交互逻辑**：
- 点击整个卡片，跳转到个人设置页面 (`/user/settings/profile`)
- 头像显示：使用用户头像，无头像时显示默认头像

### 2.3 功能列表

```vue
<template>
  <!-- 数据管理分组 -->
  <van-cell-group inset class="function-group">
    <van-cell
      :title="t('mobile.user.accountBook')"
      is-link
      @click="handleGoAccountBook"
    />
    <van-cell
      :title="t('mobile.user.classifyManage')"
      is-link
      @click="handleGoClassify"
    />
    <van-cell
      :title="t('mobile.user.tagManage')"
      is-link
      @click="handleGoTag"
    />
    <van-cell
      :title="t('mobile.user.remarkManage')"
      is-link
      @click="handleGoRemark"
    />
  </van-cell-group>

  <!-- 设置分组 -->
  <van-cell-group inset class="function-group">
    <van-cell
      :title="t('mobile.user.commonSettings')"
      is-link
      @click="handleGoCommonSettings"
    />
    <van-cell
      :title="t('mobile.user.changePassword')"
      is-link
      @click="handleGoPassword"
    />
  </van-cell-group>

  <!-- 其他分组 -->
  <van-cell-group inset class="function-group">
    <van-cell
      :title="t('mobile.user.about')"
      is-link
      @click="handleGoAbout"
    />
    <van-cell
      :title="t('mobile.user.logout')"
      @click="handleLogout"
    />
  </van-cell-group>
</template>
```

### 2.4 退出登录

```typescript
const handleLogout = async () => {
  try {
    await showConfirmDialog({
      title: t('mobile.user.logoutConfirmTitle'),
      message: t('mobile.user.logoutConfirmMessage'),
      confirmButtonColor: '#d83d34'
    });

    // 清除用户信息
    const userStore = useUserStore();
    userStore.logout();

    // 跳转到登录页
    router.replace('/login');
  } catch {
    // 取消
  }
};
```

---

## 3. 个人设置页面设计

### 3.1 页面布局

```
┌─────────────────────────────────────────────────────────────┐
│  导航栏                                                     │
│  ← 返回    个人信息                                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │              ┌────┐                                 │   │
│  │              │头像│           ← 点击上传头像         │   │
│  │              │    │                                 │   │
│  │              └────┘                                 │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  用户名：zhangsan                           (只读)  │   │
│  │  昵称：张三                                  →      │   │
│  │  性别：男                                    →      │   │
│  │  邮箱：zhangsan@example.com                  →      │   │
│  │  手机：138****8888                           →      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 头像上传

```vue
<template>
  <div class="avatar-upload" @click="handleAvatarClick">
    <van-image
      round
      width="80"
      height="80"
      :src="formData.avatar || defaultAvatar"
    />
    <div class="upload-hint">{{ t('mobile.user.clickToUpload') }}</div>
  </div>

  <!-- 隐藏的文件输入 -->
  <input
    ref="fileInput"
    type="file"
    accept="image/*"
    style="display: none"
    @change="handleFileChange"
  />

  <!-- 图片裁剪弹窗 -->
  <van-popup
    v-model:show="showCropper"
    position="bottom"
    round
    :style="{ height: '60%' }"
  >
    <div class="cropper-container">
      <VueCropper
        ref="cropper"
        :img="imgSrc"
        :auto-crop="true"
        :auto-crop-width="200"
        :auto-crop-height="200"
        :fixed="true"
        :fixed-number="[1, 1]"
      />
      <div class="cropper-footer">
        <van-button @click="showCropper = false">{{ t('mobile.common.cancel') }}</van-button>
        <van-button type="danger" @click="handleCropConfirm">{{ t('mobile.common.confirm') }}</van-button>
      </div>
    </div>
  </van-popup>
</template>
```

**上传流程**：
1. 点击头像区域，触发文件选择
2. 选择图片后，打开裁剪弹窗
3. 裁剪确认后，上传到服务器
4. 上传成功后更新显示

### 3.3 表单字段编辑

#### 3.3.1 昵称编辑

```vue
<van-cell
  :title="t('mobile.user.nickname')"
  :value="formData.nickname || t('mobile.user.notSet')"
  is-link
  @click="showNicknameEditor = true"
/>

<!-- 昵称编辑弹窗 -->
<van-dialog
  v-model:show="showNicknameEditor"
  :title="t('mobile.user.editNickname')"
  show-cancel-button
  :before-close="handleNicknameConfirm"
>
  <van-field
    v-model="editNickname"
    :placeholder="t('mobile.user.nicknamePlaceholder')"
    maxlength="20"
    show-word-limit
  />
</van-dialog>
```

#### 3.3.2 性别选择

```vue
<van-cell
  :title="t('mobile.user.gender')"
  :value="genderText"
  is-link
  @click="showGenderPicker = true"
/>

<!-- 性别选择弹窗 -->
<van-action-sheet
  v-model:show="showGenderPicker"
  :title="t('mobile.user.selectGender')"
>
  <div class="gender-list">
    <van-cell
      v-for="item in genderOptions"
      :key="item.value"
      :title="item.label"
      clickable
      @click="handleGenderSelect(item.value)"
    >
      <template #right-icon>
        <van-icon v-if="formData.gender === item.value" name="success" color="#d83d34" />
      </template>
    </van-cell>
  </div>
</van-action-sheet>
```

```typescript
const genderOptions = [
  { value: 1, label: t('mobile.user.male') },
  { value: 2, label: t('mobile.user.female') },
  { value: 0, label: t('mobile.user.unknown') }
];

const genderText = computed(() => {
  const option = genderOptions.find(item => item.value === formData.value.gender);
  return option?.label || t('mobile.user.notSet');
});
```

#### 3.3.3 邮箱编辑

```vue
<van-cell
  :title="t('mobile.user.email')"
  :value="formData.email || t('mobile.user.notSet')"
  is-link
  @click="showEmailEditor = true"
/>

<!-- 邮箱编辑弹窗 -->
<van-dialog
  v-model:show="showEmailEditor"
  :title="t('mobile.user.editEmail')"
  show-cancel-button
  :before-close="handleEmailConfirm"
>
  <van-field
    v-model="editEmail"
    type="email"
    :placeholder="t('mobile.user.emailPlaceholder')"
  />
</van-dialog>
```

#### 3.3.4 手机编辑

```vue
<van-cell
  :title="t('mobile.user.phone')"
  :value="formData.phone || t('mobile.user.notSet')"
  is-link
  @click="showPhoneEditor = true"
/>

<!-- 手机编辑弹窗 -->
<van-dialog
  v-model:show="showPhoneEditor"
  :title="t('mobile.user.editPhone')"
  show-cancel-button
  :before-close="handlePhoneConfirm"
>
  <van-field
    v-model="editPhone"
    type="tel"
    :placeholder="t('mobile.user.phonePlaceholder')"
  />
</van-dialog>
```

---

## 4. 修改密码页面设计

### 4.1 页面布局

```
┌─────────────────────────────────────────────────────────────┐
│  导航栏                                                     │
│  ← 返回    修改密码                                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  原密码                                          👁  │   │
│  │  ───────────────────────────────────────────────   │   │
│  │  新密码                                          👁  │   │
│  │  ───────────────────────────────────────────────   │   │
│  │  确认密码                                        👁  │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │                    确认修改                          │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 表单设计

```vue
<template>
  <van-cell-group inset class="password-form">
    <van-field
      v-model="formData.oldPassword"
      type="password"
      :label="t('mobile.password.oldPassword')"
      :placeholder="t('mobile.password.oldPasswordPlaceholder')"
      :right-icon="showOldPassword ? 'eye-o' : 'closed-eye'"
      @click-right-icon="showOldPassword = !showOldPassword"
    />
    <van-field
      v-model="formData.newPassword"
      type="password"
      :label="t('mobile.password.newPassword')"
      :placeholder="t('mobile.password.newPasswordPlaceholder')"
      :right-icon="showNewPassword ? 'eye-o' : 'closed-eye'"
      @click-right-icon="showNewPassword = !showNewPassword"
    />
    <van-field
      v-model="formData.confirmPassword"
      type="password"
      :label="t('mobile.password.confirmPassword')"
      :placeholder="t('mobile.password.confirmPasswordPlaceholder')"
      :right-icon="showConfirmPassword ? 'eye-o' : 'closed-eye'"
      @click-right-icon="showConfirmPassword = !showConfirmPassword"
    />
  </van-cell-group>

  <div class="submit-btn">
    <van-button
      type="danger"
      block
      round
      :loading="saving"
      @click="handleSubmit"
    >
      {{ t('mobile.password.confirmChange') }}
    </van-button>
  </div>
</template>
```

### 4.3 表单验证

```typescript
const validate = () => {
  if (!formData.value.oldPassword) {
    showError(t('mobile.password.oldPasswordRequired'));
    return false;
  }

  if (!formData.value.newPassword) {
    showError(t('mobile.password.newPasswordRequired'));
    return false;
  }

  if (formData.value.newPassword.length < 6) {
    showError(t('mobile.password.passwordMinLength'));
    return false;
  }

  if (formData.value.newPassword !== formData.value.confirmPassword) {
    showError(t('mobile.password.passwordNotMatch'));
    return false;
  }

  return true;
};
```

---

## 5. 常用设置页面设计

### 5.1 页面布局

```
┌─────────────────────────────────────────────────────────────┐
│  导航栏                                                     │
│  ← 返回    常用设置                                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  信用卡消费默认                                    ○  │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  支出限额显示                                       │   │
│  │  ┌─────────────────────────────────────────────┐   │   │
│  │  │  不显示  │  月限额  │  年限额  │             │   │   │
│  │  └─────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  月支出限额                                          │   │
│  │  ───────────────────────────────────────────────   │   │
│  │  年支出限额                                          │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 信用卡默认开关

```vue
<van-cell-group inset>
  <van-cell center :title="t('mobile.commonSettings.defaultCreditCard')">
    <template #right-icon>
      <van-switch
        v-model="isCreditCard"
        size="20"
        active-color="#d83d34"
        @change="handleCreditCardChange"
      />
    </template>
  </van-cell>
</van-cell-group>
```

### 5.3 支出限额显示模式

```vue
<van-cell-group inset class="limit-group">
  <van-cell :title="t('mobile.commonSettings.showExpenseLimit')" />

  <van-radio-group
    v-model="showExpenseLimit"
    direction="horizontal"
    class="limit-radio-group"
    @change="handleShowExpenseLimitChange"
  >
    <van-radio name="1">{{ t('mobile.commonSettings.notDisplay') }}</van-radio>
    <van-radio name="2">{{ t('mobile.commonSettings.monthlyLimit') }}</van-radio>
    <van-radio name="3">{{ t('mobile.commonSettings.yearlyLimit') }}</van-radio>
  </van-radio-group>
</van-cell-group>
```

### 5.4 限额输入

```vue
<van-cell-group inset>
  <van-field
    v-model="monthlyExpenseLimit"
    type="number"
    :label="t('mobile.commonSettings.monthlyExpenseLimit')"
    :placeholder="t('mobile.commonSettings.amountPlaceholder')"
    @blur="handleMonthlyLimitBlur"
  >
    <template #button>
      <span class="currency">¥</span>
    </template>
  </van-field>
  <van-field
    v-model="yearlyExpenseLimit"
    type="number"
    :label="t('mobile.commonSettings.yearlyExpenseLimit')"
    :placeholder="t('mobile.commonSettings.amountPlaceholder')"
    @blur="handleYearlyLimitBlur"
  >
    <template #button>
      <span class="currency">¥</span>
    </template>
  </van-field>
</van-cell-group>
```

---

## 6. 账本管理页面设计

### 6.1 页面布局

```
┌─────────────────────────────────────────────────────────────┐
│  导航栏                                                     │
│  ← 返回    账本管理                          [+ 新增]       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  🔍 搜索账本...                                      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  📒  日常账本                        默认    →      │   │  ← 左滑显示删除按钮
│  ├─────────────────────────────────────────────────────┤   │
│  │  ✈️  旅游账本                                →      │   │
│  ├─────────────────────────────────────────────────────┤   │
│  │  💼  工作账本                                →      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 6.2 搜索框

```vue
<van-search
  v-model="searchKeyword"
  shape="round"
  :placeholder="t('mobile.accountBook.searchPlaceholder')"
  @search="handleSearch"
  @clear="handleClear"
/>
```

### 6.3 账本列表（左滑删除）

```vue
<van-cell-group inset>
  <van-swipe-cell v-for="book in filteredList" :key="book.id">
    <van-cell
      :title="book.name"
      is-link
      @click="handleEdit(book)"
    >
      <template #icon>
        <span class="book-icon">{{ getAccountBookIcon(book.image) }}</span>
      </template>
      <template #value>
        <van-tag v-if="book.isDefault === 'YES'" type="success" size="small">
          {{ t('mobile.accountBook.default') }}
        </van-tag>
      </template>
    </van-cell>

    <template #right>
      <van-button
        square
        type="danger"
        :text="t('mobile.common.delete')"
        @click="handleDelete(book)"
      />
    </template>
  </van-swipe-cell>
</van-cell-group>

<van-empty v-if="filteredList.length === 0" :description="t('mobile.common.noData')" />
```

### 6.4 新增/编辑弹窗

```
┌─────────────────────────────────────────────────────────────┐
│  新增账本                                              [×]  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  账本名称                                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  日常账本                                    4/20    │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  账本图标                                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │  📒   ✈️   💼   🏠   🚗   💰   🎯   ❤️              │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  设为默认账本                                        ○     │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    确 定                             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

```vue
<van-popup
  v-model:show="showEditor"
  position="bottom"
  round
  :style="{ height: '60%' }"
>
  <div class="account-book-editor">
    <div class="editor-header">
      <span class="title">{{ isEdit ? t('mobile.accountBook.edit') : t('mobile.accountBook.add') }}</span>
      <van-icon name="cross" @click="showEditor = false" />
    </div>

    <!-- 账本名称 -->
    <van-field
      v-model="formData.name"
      :label="t('mobile.accountBook.name')"
      :placeholder="t('mobile.accountBook.namePlaceholder')"
      maxlength="20"
      show-word-limit
    />

    <!-- 账本图标 -->
    <div class="icon-selector">
      <div class="selector-label">{{ t('mobile.accountBook.icon') }}</div>
      <div class="icon-grid">
        <div
          v-for="item in iconOptions"
          :key="item.value"
          class="icon-item"
          :class="{ active: formData.image === item.value }"
          @click="formData.image = item.value"
        >
          {{ item.label }}
        </div>
      </div>
    </div>

    <!-- 设为默认 -->
    <van-cell center :title="t('mobile.accountBook.setAsDefault')">
      <template #right-icon>
        <van-switch
          v-model="formData.isDefault"
          size="20"
          active-color="#d83d34"
        />
      </template>
    </van-cell>

    <!-- 确认按钮 -->
    <div class="editor-footer">
      <van-button
        type="danger"
        block
        round
        :loading="saving"
        @click="handleSubmit"
      >
        {{ t('mobile.common.confirm') }}
      </van-button>
    </div>
  </div>
</van-popup>
```

### 6.5 账本图标选项

```typescript
// 账本图标映射
const ACCOUNT_BOOK_ICON_MAP: Record<string, string> = {
  book: '📒',
  travel: '✈️',
  work: '💼',
  home: '🏠',
  car: '🚗',
  money: '💰',
  target: '🎯',
  heart: '❤️',
  star: '⭐',
  gift: '🎁'
};

const iconOptions = Object.entries(ACCOUNT_BOOK_ICON_MAP).map(([value, label]) => ({
  value,
  label
}));
```

---

## 7. 分类管理页面设计

### 7.1 页面布局

```
┌─────────────────────────────────────────────────────────────┐
│  导航栏                                                     │
│  ← 返回    分类管理                          [+ 新增]       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │   支出   │   收入        ← 类型筛选                  │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  🍔  餐饮                            启用    →      │   │
│  │      ├── 🌅 早餐                            →      │   │
│  │      ├── 🍱 午餐                            →      │   │
│  │      └── 🍜 晚餐                            →      │   │
│  ├─────────────────────────────────────────────────────┤   │
│  │  🚗  交通                            启用    →      │   │
│  │      ├── 🚕 打车                            →      │   │
│  │      └── 🚇 地铁                            →      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 7.2 类型筛选

```vue
<van-tabs v-model:active="currentType" shrink @change="handleTypeChange">
  <van-tab name="EXPENSE">{{ t('mobile.record.expense') }}</van-tab>
  <van-tab name="INCOME">{{ t('mobile.record.income') }}</van-tab>
</van-tabs>
```

### 7.3 分类列表（树形展示）

```vue
<van-cell-group inset>
  <template v-for="mainClassify in treeData" :key="mainClassify.id">
    <!-- 父分类 -->
    <van-swipe-cell>
      <van-cell
        :title="mainClassify.name"
        is-link
        @click="handleEdit(mainClassify)"
      >
        <template #icon>
          <span class="classify-icon">{{ getClassifyIcon(mainClassify.image) }}</span>
        </template>
        <template #value>
          <van-tag v-if="mainClassify.enable === 'YES'" type="success" size="small">
            {{ t('mobile.classify.enabled') }}
          </van-tag>
        </template>
      </van-cell>
      <template #right>
        <van-button square type="danger" :text="t('mobile.common.delete')" @click="handleDelete(mainClassify)" />
      </template>
    </van-swipe-cell>

    <!-- 子分类 -->
    <template v-if="mainClassify.children?.length">
      <van-swipe-cell v-for="subClassify in mainClassify.children" :key="subClassify.id">
        <van-cell
          :title="subClassify.name"
          is-link
          class="sub-classify-cell"
          @click="handleEdit(subClassify)"
        >
          <template #icon>
            <span class="classify-icon">{{ getClassifyIcon(subClassify.image) }}</span>
          </template>
        </van-cell>
        <template #right>
          <van-button square type="danger" :text="t('mobile.common.delete')" @click="handleDelete(subClassify)" />
        </template>
      </van-swipe-cell>
    </template>
  </template>
</van-cell-group>
```

### 7.4 新增/编辑弹窗

```
┌─────────────────────────────────────────────────────────────┐
│  新增分类                                              [×]  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  类型：支出（不可编辑）                                      │
│                                                             │
│  父分类                                                     │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  顶级分类                                     ▼      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  分类名称                                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  餐饮                                      4/20      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  分类图标                                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  🍔  🍱  🍜  🍰  🍵  🍿  🥤  🍦                      │   │
│  │  🍎  🍊  🍋  🍌  🍉  🍇  🍓  🍈                      │   │
│  │  ...                                                 │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  排序                                                       │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  1                                           ▲ ▼    │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  启用                                                ○     │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    确 定                             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

```vue
<van-popup
  v-model:show="showEditor"
  position="bottom"
  round
  :style="{ height: '80%' }"
>
  <div class="classify-editor">
    <div class="editor-header">
      <span class="title">{{ isEdit ? t('mobile.classify.edit') : t('mobile.classify.add') }}</span>
      <van-icon name="cross" @click="showEditor = false" />
    </div>

    <!-- 类型（新增时显示，编辑时只读） -->
    <van-cell v-if="!isEdit" :title="t('mobile.classify.type')">
      <template #value>
        <van-radio-group v-model="formData.type" direction="horizontal">
          <van-radio name="EXPENSE">{{ t('mobile.record.expense') }}</van-radio>
          <van-radio name="INCOME">{{ t('mobile.record.income') }}</van-radio>
        </van-radio-group>
      </template>
    </van-cell>

    <!-- 父分类 -->
    <van-cell
      :title="t('mobile.classify.parentClassify')"
      is-link
      @click="showParentPicker = true"
    >
      <template #value>
        {{ parentClassifyName || t('mobile.classify.topLevel') }}
      </template>
    </van-cell>

    <!-- 分类名称 -->
    <van-field
      v-model="formData.name"
      :label="t('mobile.classify.name')"
      :placeholder="t('mobile.classify.namePlaceholder')"
      maxlength="20"
      show-word-limit
    />

    <!-- 分类图标 -->
    <div class="icon-selector">
      <div class="selector-label">{{ t('mobile.classify.icon') }}</div>
      <div class="icon-grid">
        <div
          v-for="item in iconOptions"
          :key="item.value"
          class="icon-item"
          :class="{ active: formData.image === item.value }"
          @click="formData.image = item.value"
        >
          {{ item.label }}
        </div>
      </div>
    </div>

    <!-- 排序 -->
    <van-field
      v-model="formData.sort"
      type="digit"
      :label="t('mobile.classify.sort')"
    >
      <template #button>
        <van-stepper v-model="formData.sort" min="1" max="999" />
      </template>
    </van-field>

    <!-- 启用 -->
    <van-cell center :title="t('mobile.classify.enable')">
      <template #right-icon>
        <van-switch
          v-model="formData.enable"
          size="20"
          active-color="#d83d34"
        />
      </template>
    </van-cell>

    <!-- 确认按钮 -->
    <div class="editor-footer">
      <van-button
        type="danger"
        block
        round
        :loading="saving"
        @click="handleSubmit"
      >
        {{ t('mobile.common.confirm') }}
      </van-button>
    </div>
  </div>
</van-popup>
```

---

## 8. 备注管理页面设计

### 8.1 页面布局

```
┌─────────────────────────────────────────────────────────────┐
│  导航栏                                                     │
│  ← 返回    备注管理                          [+ 新增]       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  🔍 搜索备注...                                      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  豆浆油条                        餐饮-早餐    →      │   │  ← 左滑显示删除按钮
│  ├─────────────────────────────────────────────────────┤   │
│  │  打车回家                        交通-打车    →      │   │
│  ├─────────────────────────────────────────────────────┤   │
│  │  午餐补贴                        餐饮-午餐    →      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 8.2 搜索框

```vue
<van-search
  v-model="searchKeyword"
  shape="round"
  :placeholder="t('mobile.remark.searchPlaceholder')"
  @search="handleSearch"
  @clear="handleClear"
/>
```

### 8.3 备注列表（左滑删除）

```vue
<van-cell-group inset>
  <van-swipe-cell v-for="remark in filteredList" :key="remark.id">
    <van-cell
      :title="remark.remark"
      is-link
      @click="handleEdit(remark)"
    >
      <template #value>
        <span class="classify-name">{{ getClassifyName(remark.classifyId) }}</span>
      </template>
    </van-cell>

    <template #right>
      <van-button
        square
        type="danger"
        :text="t('mobile.common.delete')"
        @click="handleDelete(remark)"
      />
    </template>
  </van-swipe-cell>
</van-cell-group>

<van-empty v-if="filteredList.length === 0" :description="t('mobile.common.noData')" />
```

### 8.4 新增/编辑弹窗

```
┌─────────────────────────────────────────────────────────────┐
│  新增备注                                              [×]  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  备注名称                                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  豆浆油条                                    4/20    │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  关联分类                                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  餐饮-早餐                                   ▼      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    确 定                             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

```vue
<van-popup
  v-model:show="showEditor"
  position="bottom"
  round
  :style="{ height: '50%' }"
>
  <div class="remark-editor">
    <div class="editor-header">
      <span class="title">{{ isEdit ? t('mobile.remark.edit') : t('mobile.remark.add') }}</span>
      <van-icon name="cross" @click="showEditor = false" />
    </div>

    <!-- 备注名称 -->
    <van-field
      v-model="formData.remark"
      :label="t('mobile.remark.name')"
      :placeholder="t('mobile.remark.namePlaceholder')"
      maxlength="20"
      show-word-limit
    />

    <!-- 关联分类 -->
    <van-cell
      :title="t('mobile.remark.classify')"
      is-link
      @click="showClassifyPicker = true"
    >
      <template #value>
        {{ selectedClassifyName || t('mobile.remark.selectClassify') }}
      </template>
    </van-cell>

    <!-- 确认按钮 -->
    <div class="editor-footer">
      <van-button
        type="danger"
        block
        round
        :loading="saving"
        @click="handleSubmit"
      >
        {{ t('mobile.common.confirm') }}
      </van-button>
    </div>
  </div>
</van-popup>
```

---

## 9. 标签管理页面设计

### 9.1 页面布局

```
┌─────────────────────────────────────────────────────────────┐
│  导航栏                                                     │
│  ← 返回    标签管理                          [+ 新增]       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  🔍 搜索标签...                                      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ┌────────┐                                          │   │
│  │  │ 工作   │                          →      │   │  ← 左滑显示删除按钮
│  │  └────────┘                                          │   │
│  ├─────────────────────────────────────────────────────┤   │
│  │  ┌────────┐                                          │   │
│  │  │ 生活   │                          →      │   │
│  │  └────────┘                                          │   │
│  ├─────────────────────────────────────────────────────┤   │
│  │  ┌────────┐                                          │   │
│  │  │ 重要   │                          →      │   │
│  │  └────────┘                                          │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 9.2 搜索框

```vue
<van-search
  v-model="searchKeyword"
  shape="round"
  :placeholder="t('mobile.tag.searchPlaceholder')"
  @search="handleSearch"
  @clear="handleClear"
/>
```

### 9.3 标签列表（左滑删除）

```vue
<van-cell-group inset>
  <van-swipe-cell v-for="tag in filteredList" :key="tag.id">
    <van-cell is-link @click="handleEdit(tag)">
      <template #title>
        <van-tag :color="tag.color" text-color="#fff">
          {{ tag.name }}
        </van-tag>
      </template>
    </van-cell>

    <template #right>
      <van-button
        square
        type="danger"
        :text="t('mobile.common.delete')"
        @click="handleDelete(tag)"
      />
    </template>
  </van-swipe-cell>
</van-cell-group>

<van-empty v-if="filteredList.length === 0" :description="t('mobile.common.noData')" />
```

### 9.4 新增/编辑弹窗

```
┌─────────────────────────────────────────────────────────────┐
│  新增标签                                              [×]  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  标签名称                                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  工作                                        4/20    │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  标签颜色                                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │  ■   ■   ■   ■   ■   ■   ■   ■                      │   │
│  │  ■   ■   ■   ■   ■   ■   ■   ■                      │   │
│  │                                                     │   │
│  │  自定义颜色：#FF5733                                │   │
│  │  ┌─────────────────────────────────────────────┐   │   │
│  │  │  [颜色选择器]                                 │   │   │
│  │  └─────────────────────────────────────────────┘   │   │
│  │                                                     │   │
│  │  透明度：80%                                        │   │
│  │  ┌─────────────────────────────────────────────┐   │   │
│  │  │  [滑块]                                       │   │   │
│  │  └─────────────────────────────────────────────┘   │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  排序                                                       │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  1                                           ▲ ▼    │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  预览：[工作]                                               │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    确 定                             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 9.5 颜色选择器设计

```vue
<template>
  <div class="color-selector">
    <!-- 预设颜色 -->
    <div class="preset-colors">
      <div
        v-for="color in presetColors"
        :key="color"
        class="color-item"
        :style="{ backgroundColor: color }"
        :class="{ active: formData.color === color }"
        @click="handleSelectColor(color)"
      />
    </div>

    <!-- 自定义颜色 -->
    <div class="custom-color">
      <van-field
        v-model="formData.color"
        :label="t('mobile.tag.customColor')"
        :placeholder="'#FF5733'"
      >
        <template #button>
          <div
            class="color-preview"
            :style="{ backgroundColor: formData.color }"
          />
        </template>
      </van-field>
    </div>

    <!-- 透明度滑块 -->
    <div class="opacity-slider">
      <van-field :label="t('mobile.tag.opacity')">
        <template #input>
          <van-slider
            v-model="formData.opacity"
            min="0"
            max="100"
            :step="5"
          />
        </template>
      </van-field>
    </div>

    <!-- 预览 -->
    <div class="color-preview-section">
      <span class="preview-label">{{ t('mobile.tag.preview') }}：</span>
      <van-tag :color="finalColor" text-color="#fff">
        {{ formData.name || t('mobile.tag.tagName') }}
      </van-tag>
    </div>
  </div>
</template>
```

```typescript
// 预设颜色列表
const presetColors = [
  '#d83d34', '#e91e63', '#9c27b0', '#673ab7',
  '#3f51b5', '#2196f3', '#03a9f4', '#00bcd4',
  '#009688', '#4caf50', '#8bc34a', '#cddc39',
  '#ffeb3b', '#ffc107', '#ff9800', '#ff5722',
  '#795548', '#9e9e9e', '#607d8b', '#000000'
];

// 最终颜色（带透明度）
const finalColor = computed(() => {
  const color = formData.value.color || '#d83d34';
  const opacity = formData.value.opacity / 100;

  // 将 hex 转换为 rgba
  const r = parseInt(color.slice(1, 3), 16);
  const g = parseInt(color.slice(3, 5), 16);
  const b = parseInt(color.slice(5, 7), 16);

  return `rgba(${r}, ${g}, ${b}, ${opacity})`;
});
```

---

## 10. 路由配置

```typescript
// router/modules/user.ts
export default {
  path: '/user',
  component: () => import('@/mobile-layout/MobileLayout.vue'),
  children: [
    {
      path: 'profile',
      name: 'MobileUser',
      component: () => import('@/views/mobile/user/index.vue'),
      meta: {
        title: '我的',
        showNavBar: false,
        showTabBar: true
      }
    },
    {
      path: 'settings/profile',
      name: 'MobileUserProfile',
      component: () => import('@/views/mobile/user/ProfileSettings.vue'),
      meta: {
        title: '个人信息',
        showNavBar: true,
        showTabBar: false
      }
    },
    {
      path: 'settings/password',
      name: 'MobileUserPassword',
      component: () => import('@/views/mobile/user/PasswordChange.vue'),
      meta: {
        title: '修改密码',
        showNavBar: true,
        showTabBar: false
      }
    },
    {
      path: 'settings/common',
      name: 'MobileCommonSettings',
      component: () => import('@/views/mobile/user/CommonSettings.vue'),
      meta: {
        title: '常用设置',
        showNavBar: true,
        showTabBar: false
      }
    },
    {
      path: 'settings/account-book',
      name: 'MobileAccountBook',
      component: () => import('@/views/mobile/user/AccountBook.vue'),
      meta: {
        title: '账本管理',
        showNavBar: true,
        showTabBar: false
      }
    },
    {
      path: 'settings/classify',
      name: 'MobileClassify',
      component: () => import('@/views/mobile/user/Classify.vue'),
      meta: {
        title: '分类管理',
        showNavBar: true,
        showTabBar: false
      }
    },
    {
      path: 'settings/remark',
      name: 'MobileRemark',
      component: () => import('@/views/mobile/user/Remark.vue'),
      meta: {
        title: '备注管理',
        showNavBar: true,
        showTabBar: false
      }
    },
    {
      path: 'settings/tag',
      name: 'MobileTag',
      component: () => import('@/views/mobile/user/Tag.vue'),
      meta: {
        title: '标签管理',
        showNavBar: true,
        showTabBar: false
      }
    }
  ]
};
```

---

## 11. 组件结构

```
src/views/mobile/user/
├── index.vue                    # 我的页面主入口
├── ProfileSettings.vue          # 个人设置页面
├── PasswordChange.vue           # 修改密码页面
├── CommonSettings.vue           # 常用设置页面
├── AccountBook.vue              # 账本管理页面
├── Classify.vue                 # 分类管理页面
├── Remark.vue                   # 备注管理页面
├── Tag.vue                      # 标签管理页面
└── components/
    ├── AvatarUploader.vue       # 头像上传组件
    ├── ColorPicker.vue          # 颜色选择器组件
    └── IconSelector.vue         # 图标选择器组件
```

---

## 12. 国际化文案

```yaml
# locales/mobile/zh-CN.yaml

user:
  title: 我的
  nickname: 昵称
  username: 账号
  notSet: 未设置
  clickToUpload: 点击上传
  editNickname: 编辑昵称
  nicknamePlaceholder: 请输入昵称
  gender: 性别
  selectGender: 选择性别
  male: 男
  female: 女
  unknown: 未知
  email: 邮箱
  editEmail: 编辑邮箱
  emailPlaceholder: 请输入邮箱
  phone: 手机
  editPhone: 编辑手机
  phonePlaceholder: 请输入手机号
  accountBook: 账本管理
  classifyManage: 分类管理
  tagManage: 标签管理
  remarkManage: 备注管理
  commonSettings: 常用设置
  changePassword: 修改密码
  about: 关于
  logout: 退出登录
  logoutConfirmTitle: 退出确认
  logoutConfirmMessage: 确定要退出登录吗？

password:
  oldPassword: 原密码
  oldPasswordPlaceholder: 请输入原密码
  oldPasswordRequired: 请输入原密码
  newPassword: 新密码
  newPasswordPlaceholder: 请输入新密码
  newPasswordRequired: 请输入新密码
  passwordMinLength: 密码长度不能少于6位
  confirmPassword: 确认密码
  confirmPasswordPlaceholder: 请再次输入新密码
  passwordNotMatch: 两次密码输入不一致
  confirmChange: 确认修改
  changeSuccess: 密码修改成功

commonSettings:
  defaultCreditCard: 信用卡消费默认
  showExpenseLimit: 支出限额显示
  notDisplay: 不显示
  monthlyLimit: 月限额
  yearlyLimit: 年限额
  monthlyExpenseLimit: 月支出限额
  yearlyExpenseLimit: 年支出限额
  amountPlaceholder: 请输入金额
  updateSuccess: 设置已保存

accountBook:
  searchPlaceholder: 搜索账本
  default: 默认
  add: 新增账本
  edit: 编辑账本
  name: 账本名称
  namePlaceholder: 请输入账本名称
  icon: 账本图标
  setAsDefault: 设为默认账本
  cannotDeleteDefault: 不能删除默认账本
  deleteConfirm: 确定要删除该账本吗？
  createSuccess: 创建成功
  updateSuccess: 更新成功
  deleteSuccess: 删除成功

classify:
  add: 新增分类
  edit: 编辑分类
  type: 类型
  parentClassify: 父分类
  topLevel: 顶级分类
  name: 分类名称
  namePlaceholder: 请输入分类名称
  icon: 分类图标
  sort: 排序
  enable: 启用
  enabled: 启用
  disabled: 禁用
  deleteConfirm: 确定要删除该分类吗？
  createSuccess: 创建成功
  updateSuccess: 更新成功
  deleteSuccess: 删除成功

remark:
  searchPlaceholder: 搜索备注
  add: 新增备注
  edit: 编辑备注
  name: 备注名称
  namePlaceholder: 请输入备注名称
  classify: 关联分类
  selectClassify: 请选择分类
  deleteConfirm: 确定要删除该备注吗？
  createSuccess: 创建成功
  updateSuccess: 更新成功
  deleteSuccess: 删除成功

tag:
  searchPlaceholder: 搜索标签
  add: 新增标签
  edit: 编辑标签
  name: 标签名称
  namePlaceholder: 请输入标签名称
  color: 标签颜色
  customColor: 自定义颜色
  opacity: 透明度
  preview: 预览
  sort: 排序
  deleteConfirm: 确定要删除该标签吗？
  createSuccess: 创建成功
  updateSuccess: 更新成功
  deleteSuccess: 删除成功
```

```yaml
# locales/mobile/en.yaml

user:
  title: Me
  nickname: Nickname
  username: Username
  notSet: Not set
  clickToUpload: Click to upload
  editNickname: Edit Nickname
  nicknamePlaceholder: Enter nickname
  gender: Gender
  selectGender: Select Gender
  male: Male
  female: Female
  unknown: Unknown
  email: Email
  editEmail: Edit Email
  emailPlaceholder: Enter email
  phone: Phone
  editPhone: Edit Phone
  phonePlaceholder: Enter phone number
  accountBook: Account Books
  classifyManage: Categories
  tagManage: Tags
  remarkManage: Remarks
  commonSettings: Settings
  changePassword: Change Password
  about: About
  logout: Logout
  logoutConfirmTitle: Logout
  logoutConfirmMessage: Are you sure you want to logout?

password:
  oldPassword: Current Password
  oldPasswordPlaceholder: Enter current password
  oldPasswordRequired: Please enter current password
  newPassword: New Password
  newPasswordPlaceholder: Enter new password
  newPasswordRequired: Please enter new password
  passwordMinLength: Password must be at least 6 characters
  confirmPassword: Confirm Password
  confirmPasswordPlaceholder: Enter new password again
  passwordNotMatch: Passwords do not match
  confirmChange: Confirm
  changeSuccess: Password changed successfully

commonSettings:
  defaultCreditCard: Default Credit Card
  showExpenseLimit: Expense Limit Display
  notDisplay: Don't show
  monthlyLimit: Monthly Limit
  yearlyLimit: Yearly Limit
  monthlyExpenseLimit: Monthly Expense Limit
  yearlyExpenseLimit: Yearly Expense Limit
  amountPlaceholder: Enter amount
  updateSuccess: Settings saved

accountBook:
  searchPlaceholder: Search account books
  default: Default
  add: Add Account Book
  edit: Edit Account Book
  name: Name
  namePlaceholder: Enter account book name
  icon: Icon
  setAsDefault: Set as default
  cannotDeleteDefault: Cannot delete default account book
  deleteConfirm: Are you sure to delete this account book?
  createSuccess: Created successfully
  updateSuccess: Updated successfully
  deleteSuccess: Deleted successfully

classify:
  add: Add Category
  edit: Edit Category
  type: Type
  parentClassify: Parent Category
  topLevel: Top Level
  name: Name
  namePlaceholder: Enter category name
  icon: Icon
  sort: Sort
  enable: Enable
  enabled: Enabled
  disabled: Disabled
  deleteConfirm: Are you sure to delete this category?
  createSuccess: Created successfully
  updateSuccess: Updated successfully
  deleteSuccess: Deleted successfully

remark:
  searchPlaceholder: Search remarks
  add: Add Remark
  edit: Edit Remark
  name: Name
  namePlaceholder: Enter remark name
  classify: Category
  selectClassify: Select category
  deleteConfirm: Are you sure to delete this remark?
  createSuccess: Created successfully
  updateSuccess: Updated successfully
  deleteSuccess: Deleted successfully

tag:
  searchPlaceholder: Search tags
  add: Add Tag
  edit: Edit Tag
  name: Name
  namePlaceholder: Enter tag name
  color: Color
  customColor: Custom Color
  opacity: Opacity
  preview: Preview
  sort: Sort
  deleteConfirm: Are you sure to delete this tag?
  createSuccess: Created successfully
  updateSuccess: Updated successfully
  deleteSuccess: Deleted successfully
```

---

## 13. API 接口调用

### 13.1 用户信息相关

```typescript
// 获取用户信息
const loadUserInfo = async () => {
  const userStore = useUserStore();
  userInfo.value = await getUserInfo(userStore.id);
};

// 更新用户信息
const updateUserInfo = async (data: Partial<UserInfo>) => {
  const userStore = useUserStore();
  await updateUserInfoApi(userStore.id, data);
  showSuccess(t('mobile.common.success'));
  await loadUserInfo();
};

// 上传头像
const uploadAvatar = async (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  const result = await uploadFile(formData);
  return result.url;
};

// 修改密码
const changePassword = async (data: PasswordChangeParams) => {
  const userStore = useUserStore();
  await changePasswordApi(userStore.id, data);
  showSuccess(t('mobile.password.changeSuccess'));
  router.back();
};
```

### 13.2 账本管理相关

```typescript
// 获取账本列表
const loadAccountBooks = async () => {
  const userStore = useUserStore();
  const result = await getAccountBookList(userStore.id);
  accountBookList.value = result || [];
};

// 创建账本
const handleCreate = async () => {
  const userStore = useUserStore();
  await createAccountBook({
    userId: userStore.id,
    name: formData.value.name,
    image: formData.value.image,
    isDefault: formData.value.isDefault ? 'YES' : 'NO'
  });
  showSuccess(t('mobile.accountBook.createSuccess'));
  showEditor.value = false;
  await loadAccountBooks();
  await refreshAccountBookStore();
};

// 更新账本
const handleUpdate = async () => {
  await updateAccountBook({
    id: formData.value.id,
    userId: formData.value.userId,
    name: formData.value.name,
    image: formData.value.image,
    isDefault: formData.value.isDefault ? 'YES' : 'NO'
  });
  showSuccess(t('mobile.accountBook.updateSuccess'));
  showEditor.value = false;
  await loadAccountBooks();
  await refreshAccountBookStore();
};

// 删除账本
const handleDelete = async (book: AccountBook) => {
  if (book.isDefault === 'YES') {
    showError(t('mobile.accountBook.cannotDeleteDefault'));
    return;
  }

  try {
    await showConfirmDialog({
      message: t('mobile.accountBook.deleteConfirm')
    });
    await deleteAccountBook([book.id]);
    showSuccess(t('mobile.accountBook.deleteSuccess'));
    await loadAccountBooks();
    await refreshAccountBookStore();
  } catch {
    // cancelled
  }
};
```

### 13.3 刷新 Store 缓存

```typescript
// 刷新账本缓存
const refreshAccountBookStore = async () => {
  const userStore = useUserStore();
  const accountBookStore = useAccountBookStore();
  await accountBookStore.fetchList(userStore.id);
};

// 刷新分类缓存
const refreshClassifyStore = async () => {
  const userStore = useUserStore();
  const classifyStore = useClassifyStore();
  await classifyStore.fetchList(userStore.id);
};

// 刷新标签缓存
const refreshTagStore = async () => {
  const userStore = useUserStore();
  const userTagStore = useUserTagStore();
  await userTagStore.fetchList(userStore.id);
};

// 刷新备注缓存
const refreshRemarkStore = async () => {
  const userStore = useUserStore();
  const remarkStore = useRemarkStore();
  await remarkStore.fetchList(userStore.id);
};

// 刷新用户配置缓存
const refreshUserConfigStore = async () => {
  const userStore = useUserStore();
  const userConfigStore = useUserConfigStore();
  await userConfigStore.fetchConfigs(userStore.id);
};
```

---

## 14. 数据同步机制

### 14.1 概述

设置页面中的数据（账本、分类、标签、备注、用户配置）在多个页面中被使用，如首页、记账页、账单页等。当用户在设置页面进行新增、编辑、删除操作后，**必须**重新从服务器获取最新数据并更新到 Pinia Store 中，以确保其他页面能够使用最新的数据。

### 14.2 数据同步流程

```
┌─────────────────────────────────────────────────────────────┐
│                    设置页面操作                              │
│  （新增/编辑/删除）                                          │
├─────────────────────────────────────────────────────────────┤
│                           ↓                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  1. 调用 API 接口执行操作（create/update/delete）    │   │
│  └─────────────────────────────────────────────────────┘   │
│                           ↓                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  2. 操作成功后，调用对应的 list 接口获取全部数据      │   │
│  └─────────────────────────────────────────────────────┘   │
│                           ↓                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  3. 更新 Pinia Store 中的数据                        │   │
│  └─────────────────────────────────────────────────────┘   │
│                           ↓                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  4. 其他页面从 Store 获取数据时自动得到最新数据       │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 14.3 各设置页面数据同步详情

#### 14.3.1 账本管理页面

**Store**: `useAccountBookStore`

**数据用途**：
- 首页：账本选择下拉列表、默认账本
- 记账页：账本选择器
- 账单页：账本筛选

**同步时机**：新增、编辑、删除账本后

```typescript
// 账本管理页面 - 数据同步实现
import { useAccountBookStore } from '@/store/modules/accountBook';

const accountBookStore = useAccountBookStore();

// 新增账本
const handleCreate = async () => {
  try {
    await createAccountBook({
      userId: userStore.id,
      name: formData.value.name,
      image: formData.value.image,
      isDefault: formData.value.isDefault ? 'YES' : 'NO'
    });

    showSuccess(t('mobile.accountBook.createSuccess'));
    showEditor.value = false;

    // 重新获取账本列表并更新 Store
    await accountBookStore.fetchList(userStore.id);

    // 刷新页面列表
    await loadData();
  } catch (error: any) {
    showError(error?.message || t('mobile.common.failed'));
  }
};

// 编辑账本
const handleUpdate = async () => {
  try {
    await updateAccountBook({
      id: formData.value.id,
      userId: formData.value.userId,
      name: formData.value.name,
      image: formData.value.image,
      isDefault: formData.value.isDefault ? 'YES' : 'NO'
    });

    showSuccess(t('mobile.accountBook.updateSuccess'));
    showEditor.value = false;

    // 重新获取账本列表并更新 Store
    await accountBookStore.fetchList(userStore.id);

    // 刷新页面列表
    await loadData();
  } catch (error: any) {
    showError(error?.message || t('mobile.common.failed'));
  }
};

// 删除账本
const handleDelete = async (book: AccountBook) => {
  if (book.isDefault === 'YES') {
    showError(t('mobile.accountBook.cannotDeleteDefault'));
    return;
  }

  try {
    await showConfirmDialog({
      message: t('mobile.accountBook.deleteConfirm')
    });

    await deleteAccountBook([book.id]);

    showSuccess(t('mobile.accountBook.deleteSuccess'));

    // 重新获取账本列表并更新 Store
    await accountBookStore.fetchList(userStore.id);

    // 刷新页面列表
    await loadData();
  } catch {
    // cancelled
  }
};
```

#### 14.3.2 分类管理页面

**Store**: `useClassifyStore`

**数据用途**：
- 记账页：分类选择器（根据收支类型过滤）
- 账单页：分类筛选
- 备注管理：关联分类选择

**同步时机**：新增、编辑、删除分类后

```typescript
// 分类管理页面 - 数据同步实现
import { useClassifyStore } from '@/store/modules/classify';

const classifyStore = useClassifyStore();

// 新增分类
const handleCreate = async () => {
  try {
    await createClassify({
      userId: userStore.id,
      name: formData.value.name,
      pid: formData.value.pid,
      image: formData.value.image,
      sort: formData.value.sort,
      type: formData.value.type,
      enable: formData.value.enable ? 'YES' : 'NO'
    });

    showSuccess(t('mobile.classify.createSuccess'));
    showEditor.value = false;

    // 重新获取分类列表并更新 Store
    await classifyStore.fetchList(userStore.id);

    // 刷新页面列表
    await loadData();
  } catch (error: any) {
    showError(error?.message || t('mobile.common.failed'));
  }
};

// 编辑分类
const handleUpdate = async () => {
  try {
    await updateClassify({
      id: formData.value.id,
      userId: formData.value.userId,
      name: formData.value.name,
      pid: formData.value.pid,
      image: formData.value.image,
      sort: formData.value.sort,
      type: formData.value.type,
      enable: formData.value.enable ? 'YES' : 'NO'
    });

    showSuccess(t('mobile.classify.updateSuccess'));
    showEditor.value = false;

    // 重新获取分类列表并更新 Store
    await classifyStore.fetchList(userStore.id);

    // 刷新页面列表
    await loadData();
  } catch (error: any) {
    showError(error?.message || t('mobile.common.failed'));
  }
};

// 删除分类
const handleDelete = async (classify: Classify) => {
  try {
    await showConfirmDialog({
      message: t('mobile.classify.deleteConfirm')
    });

    await deleteClassify([classify.id]);

    showSuccess(t('mobile.classify.deleteSuccess'));

    // 重新获取分类列表并更新 Store
    await classifyStore.fetchList(userStore.id);

    // 刷新页面列表
    await loadData();
  } catch {
    // cancelled
  }
};
```

#### 14.3.3 标签管理页面

**Store**: `useUserTagStore`

**数据用途**：
- 记账页：标签选择器
- 账单页：标签筛选
- 首页/账单列表：标签显示

**同步时机**：新增、编辑、删除标签后

```typescript
// 标签管理页面 - 数据同步实现
import { useUserTagStore } from '@/store/modules/userTag';

const userTagStore = useUserTagStore();

// 新增标签
const handleCreate = async () => {
  try {
    await createUserTag({
      userId: userStore.id,
      name: formData.value.name,
      color: formData.value.color,
      sort: formData.value.sort
    });

    showSuccess(t('mobile.tag.createSuccess'));
    showEditor.value = false;

    // 重新获取标签列表并更新 Store
    await userTagStore.fetchList(userStore.id);

    // 刷新页面列表
    await loadData();
  } catch (error: any) {
    showError(error?.message || t('mobile.common.failed'));
  }
};

// 编辑标签
const handleUpdate = async () => {
  try {
    await updateUserTag({
      id: formData.value.id,
      userId: formData.value.userId,
      name: formData.value.name,
      color: formData.value.color,
      sort: formData.value.sort
    });

    showSuccess(t('mobile.tag.updateSuccess'));
    showEditor.value = false;

    // 重新获取标签列表并更新 Store
    await userTagStore.fetchList(userStore.id);

    // 刷新页面列表
    await loadData();
  } catch (error: any) {
    showError(error?.message || t('mobile.common.failed'));
  }
};

// 删除标签
const handleDelete = async (tag: UserTag) => {
  try {
    await showConfirmDialog({
      message: t('mobile.tag.deleteConfirm')
    });

    await deleteUserTag([tag.id]);

    showSuccess(t('mobile.tag.deleteSuccess'));

    // 重新获取标签列表并更新 Store
    await userTagStore.fetchList(userStore.id);

    // 刷新页面列表
    await loadData();
  } catch {
    // cancelled
  }
};
```

#### 14.3.4 备注管理页面

**Store**: `useRemarkStore`

**数据用途**：
- 记账页：备注选择器、自动填充分类

**同步时机**：新增、编辑、删除备注后

```typescript
// 备注管理页面 - 数据同步实现
import { useRemarkStore } from '@/store/modules/remark';

const remarkStore = useRemarkStore();

// 新增备注
const handleCreate = async () => {
  try {
    await createUserRemark({
      userId: userStore.id,
      remark: formData.value.remark,
      classifyId: formData.value.classifyId
    });

    showSuccess(t('mobile.remark.createSuccess'));
    showEditor.value = false;

    // 重新获取备注列表并更新 Store
    await remarkStore.fetchList(userStore.id);

    // 刷新页面列表
    await loadData();
  } catch (error: any) {
    showError(error?.message || t('mobile.common.failed'));
  }
};

// 编辑备注
const handleUpdate = async () => {
  try {
    await updateUserRemark({
      id: formData.value.id,
      userId: formData.value.userId,
      remark: formData.value.remark,
      classifyId: formData.value.classifyId
    });

    showSuccess(t('mobile.remark.updateSuccess'));
    showEditor.value = false;

    // 重新获取备注列表并更新 Store
    await remarkStore.fetchList(userStore.id);

    // 刷新页面列表
    await loadData();
  } catch (error: any) {
    showError(error?.message || t('mobile.common.failed'));
  }
};

// 删除备注
const handleDelete = async (remark: UserRemark) => {
  try {
    await showConfirmDialog({
      message: t('mobile.remark.deleteConfirm')
    });

    await deleteUserRemark([remark.id]);

    showSuccess(t('mobile.remark.deleteSuccess'));

    // 重新获取备注列表并更新 Store
    await remarkStore.fetchList(userStore.id);

    // 刷新页面列表
    await loadData();
  } catch {
    // cancelled
  }
};
```

#### 14.3.5 常用设置页面

**Store**: `useUserConfigStore`

**数据用途**：
- 首页：支出限额显示、限额类型、月/年限额值
- 记账页：信用卡消费默认值

**同步时机**：修改任何设置项后

```typescript
// 常用设置页面 - 数据同步实现
import { useUserConfigStore } from '@/store/modules/userConfig';

const userConfigStore = useUserConfigStore();

// 修改信用卡默认值
const handleCreditCardChange = async (value: boolean) => {
  try {
    const config = getConfigByName('is_credit_card');
    if (!config) return;

    await updateUserConfig({
      id: config.id,
      userId: config.userId,
      name: config.name,
      value: value ? '1' : '0',
      description: config.description,
      enable: config.enable
    });

    showSuccess(t('mobile.commonSettings.updateSuccess'));

    // 重新获取用户配置并更新 Store
    await userConfigStore.fetchConfigs(userStore.id);
  } catch (error: any) {
    showError(error?.message || t('mobile.common.failed'));
    // 恢复原值
    await loadData();
  }
};

// 修改支出限额显示模式
const handleShowExpenseLimitChange = async (value: '1' | '2' | '3') => {
  try {
    const config = getConfigByName('show_expense_limit');
    if (!config) return;

    await updateUserConfig({
      id: config.id,
      userId: config.userId,
      name: config.name,
      value: value,
      description: config.description,
      enable: config.enable
    });

    showSuccess(t('mobile.commonSettings.updateSuccess'));

    // 重新获取用户配置并更新 Store
    await userConfigStore.fetchConfigs(userStore.id);
  } catch (error: any) {
    showError(error?.message || t('mobile.common.failed'));
    await loadData();
  }
};

// 修改月支出限额
const handleMonthlyLimitBlur = async () => {
  const value = defaultMonthlyExpenseLimit.value;
  const numValue = parseFloat(value);

  if (value && (isNaN(numValue) || numValue < 0)) {
    showError(t('mobile.commonSettings.invalidAmount'));
    return;
  }

  try {
    const config = getConfigByName('default_monthly_expense_limit');
    if (!config) return;

    await updateUserConfig({
      id: config.id,
      userId: config.userId,
      name: config.name,
      value: value || '',
      description: config.description,
      enable: config.enable
    });

    showSuccess(t('mobile.commonSettings.updateSuccess'));

    // 重新获取用户配置并更新 Store
    await userConfigStore.fetchConfigs(userStore.id);
  } catch (error: any) {
    showError(error?.message || t('mobile.common.failed'));
    await loadData();
  }
};

// 修改年支出限额
const handleYearlyLimitBlur = async () => {
  const value = defaultYearlyExpenseLimit.value;
  const numValue = parseFloat(value);

  if (value && (isNaN(numValue) || numValue < 0)) {
    showError(t('mobile.commonSettings.invalidAmount'));
    return;
  }

  try {
    const config = getConfigByName('default_yearly_expense_limit');
    if (!config) return;

    await updateUserConfig({
      id: config.id,
      userId: config.userId,
      name: config.name,
      value: value || '',
      description: config.description,
      enable: config.enable
    });

    showSuccess(t('mobile.commonSettings.updateSuccess'));

    // 重新获取用户配置并更新 Store
    await userConfigStore.fetchConfigs(userStore.id);
  } catch (error: any) {
    showError(error?.message || t('mobile.common.failed'));
    await loadData();
  }
};
```

### 14.4 数据同步汇总表

| 设置页面 | Store | API 接口 | 同步时机 | 数据用途页面 |
|----------|-------|----------|----------|--------------|
| 账本管理 | `useAccountBookStore` | `/accountBook/list` | 新增/编辑/删除后 | 首页、记账页、账单页 |
| 分类管理 | `useClassifyStore` | `/classify/list` | 新增/编辑/删除后 | 记账页、账单页、备注管理 |
| 标签管理 | `useUserTagStore` | `/userTag/list` | 新增/编辑/删除后 | 记账页、账单页、首页 |
| 备注管理 | `useRemarkStore` | `/remark/list` | 新增/编辑/删除后 | 记账页 |
| 常用设置 | `useUserConfigStore` | `/userConfig/list` | 修改任何设置项后 | 首页、记账页 |

### 14.5 注意事项

1. **同步顺序**：先执行操作 API（create/update/delete），成功后再调用 list 接口更新 Store

2. **错误处理**：如果操作失败，不应更新 Store，保持原有数据

3. **页面刷新**：更新 Store 后，同时刷新当前页面的列表数据，确保页面显示最新

4. **默认值处理**：
   - 删除默认账本时需要阻止，或提示用户先设置其他账本为默认
   - 编辑账本设为默认时，需要处理原默认账本的更新

5. **关联数据处理**：
   - 删除分类时，需要考虑该分类下的备注是否需要处理
   - 删除账本时，需要考虑该账本下的账单数据（通常只做软删除或提示用户）

### 14.6 统一同步函数封装

可以将同步逻辑封装成统一的工具函数，便于复用：

```typescript
// utils/mobile/dataSync.ts
import { useUserStore } from '@/store/modules/user';
import { useAccountBookStore } from '@/store/modules/accountBook';
import { useClassifyStore } from '@/store/modules/classify';
import { useUserTagStore } from '@/store/modules/userTag';
import { useRemarkStore } from '@/store/modules/remark';
import { useUserConfigStore } from '@/store/modules/userConfig';

/**
 * 同步账本数据到 Store
 */
export const syncAccountBookStore = async () => {
  const userStore = useUserStore();
  const accountBookStore = useAccountBookStore();
  if (userStore.id) {
    await accountBookStore.fetchList(userStore.id);
  }
};

/**
 * 同步分类数据到 Store
 */
export const syncClassifyStore = async () => {
  const userStore = useUserStore();
  const classifyStore = useClassifyStore();
  if (userStore.id) {
    await classifyStore.fetchList(userStore.id);
  }
};

/**
 * 同步标签数据到 Store
 */
export const syncUserTagStore = async () => {
  const userStore = useUserStore();
  const userTagStore = useUserTagStore();
  if (userStore.id) {
    await userTagStore.fetchList(userStore.id);
  }
};

/**
 * 同步备注数据到 Store
 */
export const syncRemarkStore = async () => {
  const userStore = useUserStore();
  const remarkStore = useRemarkStore();
  if (userStore.id) {
    await remarkStore.fetchList(userStore.id);
  }
};

/**
 * 同步用户配置数据到 Store
 */
export const syncUserConfigStore = async () => {
  const userStore = useUserStore();
  const userConfigStore = useUserConfigStore();
  if (userStore.id) {
    await userConfigStore.fetchConfigs(userStore.id);
  }
};

/**
 * 同步所有基础数据到 Store
 */
export const syncAllStores = async () => {
  await Promise.all([
    syncAccountBookStore(),
    syncClassifyStore(),
    syncUserTagStore(),
    syncRemarkStore(),
    syncUserConfigStore()
  ]);
};
```

**使用示例**：

```typescript
import { syncAccountBookStore } from '@/utils/mobile/dataSync';

// 新增账本后同步
const handleCreate = async () => {
  await createAccountBook({ ... });
  showSuccess(t('mobile.accountBook.createSuccess'));

  // 同步到 Store
  await syncAccountBookStore();

  // 刷新页面列表
  await loadData();
};
```
