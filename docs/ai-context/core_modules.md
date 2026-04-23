# 核心模块说明

## 1. 设备检测模块

### 文件位置
`src/utils/device.ts`

### 功能
检测当前访问设备类型，用于路由判断和组件渲染。

### 实现方式
```typescript
// src/utils/device.ts

/**
 * 检测是否为移动设备
 * 通过 User-Agent 判断
 */
export const isMobile = (): boolean => {
  if (typeof navigator === 'undefined') return false;

  const ua = navigator.userAgent;
  const mobileRegex = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini|Mobile|mobile/i;

  return mobileRegex.test(ua);
};

/**
 * 检测是否为 PC 设备
 */
export const isPC = (): boolean => {
  return !isMobile();
};

/**
 * 检测屏幕宽度是否为移动端尺寸
 * 用于响应式调整
 */
export const isMobileScreen = (): boolean => {
  if (typeof window === 'undefined') return false;
  return window.innerWidth < 768;
};

/**
 * 获取设备类型
 */
export const getDeviceType = (): 'mobile' | 'pc' => {
  return isMobile() ? 'mobile' : 'pc';
};
```

---

## 2. 路由模块

### 2.1 主路由入口
`src/router/index.ts`

负责根据设备类型加载对应路由配置。

### 2.2 PC 端路由
`src/router/pc.ts`

保持现有路由结构，添加 `/pc/` 前缀或保持原路径。

### 2.3 移动端路由
`src/router/mobile.ts`

```typescript
// 移动端路由配置
const mobileRoutes = [
  {
    path: '/',
    name: 'MobileHome',
    component: () => import('@/views/mobile/home/index.vue'),
    meta: { title: '首页', keepAlive: true }
  },
  {
    path: '/bill',
    name: 'MobileBill',
    component: () => import('@/views/mobile/bill/index.vue'),
    meta: { title: '账单', keepAlive: true }
  },
  {
    path: '/record',
    name: 'MobileRecord',
    component: () => import('@/views/mobile/record/index.vue'),
    meta: { title: '记账' }
  },
  {
    path: '/record/:id',
    name: 'MobileRecordEdit',
    component: () => import('@/views/mobile/record/index.vue'),
    meta: { title: '编辑记录' }
  },
  {
    path: '/report',
    name: 'MobileReport',
    component: () => import('@/views/mobile/report/index.vue'),
    meta: { title: '报表', keepAlive: true }
  },
  {
    path: '/report/detail',
    name: 'MobileReportDetail',
    component: () => import('@/views/mobile/report/detail.vue'),
    meta: { title: '报表详情' }
  },
  {
    path: '/search',
    name: 'MobileSearch',
    component: () => import('@/views/mobile/search/index.vue'),
    meta: { title: '搜索' }
  },
  {
    path: '/user',
    name: 'MobileUser',
    component: () => import('@/views/mobile/user/index.vue'),
    meta: { title: '我的' }
  },
  {
    path: '/login',
    name: 'MobileLogin',
    component: () => import('@/views/mobile/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'MobileRegister',
    component: () => import('@/views/mobile/register/index.vue'),
    meta: { title: '注册' }
  }
];
```

---

## 3. 布局模块

### 3.1 移动端布局
`src/layouts/MobileLayout.vue`

```vue
<template>
  <div class="mobile-layout">
    <!-- 顶部导航栏 -->
    <van-nav-bar
      v-if="showNavBar"
      :title="pageTitle"
      :left-arrow="showBack"
      @click-left="onBack"
    >
      <template #right>
        <slot name="nav-right" />
      </template>
    </van-nav-bar>

    <!-- 页面内容 -->
    <div class="mobile-content">
      <router-view v-slot="{ Component }">
        <keep-alive :include="cachedPages">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </div>

    <!-- 底部 TabBar -->
    <van-tabbar v-model="activeTab" route v-if="showTabBar">
      <van-tabbar-item to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/bill" icon="notes-o">账单</van-tabbar-item>
      <van-tabbar-item to="/report" icon="bar-chart-o">报表</van-tabbar-item>
      <van-tabbar-item to="/user" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>
```

---

## 4. 状态管理模块

### 4.1 用户状态
`src/store/modules/user.ts`

存储用户登录信息、Token、用户配置等。

### 4.2 账单状态
`src/store/modules/bill.ts`

存储账单列表、分类、标签、账本等数据。

**主要状态：**
- `accountBooks` - 账本列表
- `currentAccountBook` - 当前账本
- `classifyList` - 分类列表
- `classifyTree` - 分类树
- `tagList` - 标签列表
- `remarkList` - 备注列表

**主要方法：**
- `loadAccountBooks()` - 加载账本
- `loadClassifyAndTag()` - 加载分类和标签
- `loadList()` - 加载账单列表
- `create()` - 创建账单
- `update()` - 更新账单
- `remove()` - 删除账单

---

## 5. API 模块

### 5.1 HTTP 请求封装
`src/utils/http/index.ts`

基于 Axios 封装，支持：
- 请求/响应拦截
- Token 自动注入
- 错误统一处理
- 401 自动跳转登录

### 5.2 主要 API 接口

| 模块 | 文件 | 主要接口 |
|------|------|----------|
| 认证 | `api/auth.ts` | login, register, captcha |
| 用户 | `api/user.ts` | getUserInfo, updateUserInfo |
| 收支 | `api/incomeExpense.ts` | getIncomeExpenseList, createIncomeExpense, updateIncomeExpense, deleteIncomeExpense, getSummary, getTrendData |
| 分类 | `api/classify.ts` | getClassifyList, createClassify, updateClassify, deleteClassify |
| 账本 | `api/accountBook.ts` | getAccountBooks, createAccountBook, updateAccountBook, deleteAccountBook |
| 标签 | `api/userTag.ts` | getTagList, createTag, updateTag, deleteTag |
| 备注 | `api/remark.ts` | getRemarkList, createRemark, deleteRemark |
| 配置 | `api/userConfig.ts` | getUserConfigList, updateUserConfig |

---

## 6. 类型定义模块

### 6.1 账单类型
`src/types/bill.ts`

```typescript
// 核心类型定义
export interface IncomeExpense {
  id: number;
  userId: number;
  accountBookId: number;
  amount: number;
  type: string;           // 'INCOME' | 'EXPENSE'
  date: string;
  remark: string;
  mainClassify: number;
  subClassify: number | null;
  isCreditCard: string;
  tagCodes: string;
  // ... 其他字段
}

export interface IncomeExpenseForm {
  id?: number;
  accountBookId: number;
  amount: number;
  type: string;
  date: string;
  remark: string;
  mainClassify: number;
  subClassify?: number;
  isCreditCard: string;
  isAddRemark: string;
  tagCodes: string;
}

export interface Summary {
  expenseAmount: number;
  incomeAmount: number;
  expenseLimit: number;
  expenseSurplus: number;
  incomeExpenseList: IncomeExpenseRecord[];
}
```

### 6.2 分类类型
`src/types/classify.ts`

```typescript
export interface Classify {
  id: number;
  pid: number;
  name: string;
  userId: number;
  image: string;
  sort: number;
  type: 0 | 1;           // 0: 支出, 1: 收入
  enable: boolean;
  children?: Classify[];
}
```

### 6.3 账本类型
`src/types/accountBook.ts`

```typescript
export interface AccountBook {
  id: number;
  name: string;
  description: string;
  isDefault: string;     // 'YES' | 'NO'
  userId: number;
}
```
