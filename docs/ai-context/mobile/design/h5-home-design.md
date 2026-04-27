# H5 首页详细设计文档

## 1. 页面概述

### 1.1 页面定位
首页是用户进入应用后的第一个页面，展示本月收支概览和近期账单记录，提供快速记账入口。

### 1.2 页面特性
- **一级页面**：无顶部导航栏，有底部 TabBar
- **支持下拉刷新**：摘要区域可下拉刷新重新加载数据
- **数据缓存**：用户基础数据缓存到 Store，其他页面可复用

---

## 2. 数据加载

### 2.1 页面初始化数据加载

进入首页时需要加载以下数据：

| 数据类型 | API 接口 | 存储位置 | 说明 |
|----------|----------|----------|------|
| 用户账本 | `/accountBook/list` | `store.accountBook` | 用户所有账本列表 |
| 用户标签 | `/userTag/list` | `store.userTag` | 用户所有标签列表 |
| 用户备注 | `/remark/list` | `store.remark` | 用户常用备注列表 |
| 用户配置 | `/userConfig/get` | `store.userConfig` | 用户个性化配置 |
| 摘要数据 | `/incomeExpense/summary` | 不存储 | 本月收支概览和近三日明细 |

### 2.2 数据加载流程

```
┌─────────────────────────────────────────────────────────────┐
│                      页面初始化                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  并行加载用户基础数据（账本、标签、备注、配置）        │   │
│  │  Promise.all([                                        │   │
│  │    getAccountBookList(),                             │   │
│  │    getUserTagList(),                                 │   │
│  │    getRemarkList(),                                  │   │
│  │    getUserConfig()                                   │   │
│  │  ])                                                  │   │
│  └─────────────────────────────────────────────────────┘   │
│                           ↓                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  存储到 Pinia Store                                   │   │
│  │  - accountBookStore.setList()                        │   │
│  │  - userTagStore.setList()                            │   │
│  │  - remarkStore.setList()                             │   │
│  │  - userConfigStore.setConfig()                       │   │
│  └─────────────────────────────────────────────────────┘   │
│                           ↓                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  加载摘要数据                                         │   │
│  │  getIncomeExpenseSummary({                           │   │
│  │    accountBookId: defaultAccountBookId,              │   │
│  │    type: 'month'                                     │   │
│  │  })                                                  │   │
│  └─────────────────────────────────────────────────────┘   │
│                           ↓                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  渲染页面                                             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 2.3 摘要接口参数

```typescript
interface SummaryParams {
  accountBookId: string;  // 账本ID，默认使用用户设置的默认账本
  type: 'month' | 'year'; // 统计类型，首页固定为 month
  date?: string;          // 日期，可选，默认当前月份
}

// 调用示例
const fetchSummary = async () => {
  const accountBookStore = useAccountBookStore();
  const defaultAccountBookId = accountBookStore.defaultAccountBookId;

  const result = await getIncomeExpenseSummary({
    accountBookId: defaultAccountBookId,
    type: 'month'
  });

  return result;
};
```

### 2.4 摘要接口返回数据结构

```typescript
interface SummaryResult {
  // 本月概览
  monthExpense: number;      // 本月支出
  monthIncome: number;       // 本月收入
  monthBalance: number;      // 本月结余

  // 限额相关（根据用户配置计算）
  expenseLimit?: number;     // 支出限额
  remainingLimit?: number;   // 剩余限额
  limitType?: 'month' | 'year'; // 限额类型

  // 近三日收支明细
  recentRecords: RecentRecord[];
}

interface RecentRecord {
  id: string;                // 记录ID
  date: string;              // 日期 YYYY-MM-DD
  type: 1 | 2;               // 1: 支出, 2: 收入
  amount: number;            // 金额
  classifyId: string;        // 分类ID
  classifyName: string;      // 分类名称
  classifyIcon: string;      // 分类图标
  parentClassifyName?: string; // 父分类名称
  remark?: string;           // 备注
  accountBookId: string;     // 账本ID
  tags?: string[];           // 标签ID列表
}
```

---

## 3. 页面布局

### 3.1 整体布局结构

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │  摘要区域 (固定高度，不随滚动)                        │   │
│  │  - 本月支出（大字体）                                │   │
│  │  - 本月收入（小字体）                                │   │
│  │  - 支出限额（根据配置显示）                           │   │
│  │  - 剩余限额（根据配置显示）                           │   │
│  │  - 下拉刷新功能                                      │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                     │   │
│  │  近三日收支记录列表（可滚动）                          │   │
│  │                                                     │   │
│  │  ┌─────────────────────────────────────────────┐   │   │
│  │  │ 日期  │ 分类图标 │ 分类名称/备注 │ 金额      │   │   │
│  │  └─────────────────────────────────────────────┘   │   │
│  │  ┌─────────────────────────────────────────────┐   │   │
│  │  │ 日期  │ 分类图标 │ 分类名称/备注 │ 金额      │   │   │
│  │  └─────────────────────────────────────────────┘   │   │
│  │  ...                                               │   │
│  │                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│                                          ┌──────────────┐   │
│                                          │   记一笔     │   │
│                                          │   (悬浮按钮)  │   │
│                                          └──────────────┘   │
├─────────────────────────────────────────────────────────────┤
│  底部 TabBar                                                │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 摘要区域设计

#### 3.2.1 布局结构

```
┌─────────────────────────────────────────────────────────────┐
│                                        我的账本 ▼           │
│   本月支出                                                  │
│   ¥ 1,234.56                           ← 大字体，红色       │
│                                                             │
│   本月收入                                                  │
│   ¥ 5,678.90                           ← 小字体，绿色       │
│                                                             │
│   ─────────────────────────────────────────────────────    │
│                                                             │
│   支出限额          剩余限额           ← 根据配置显示       │
│   ¥ 3,000.00        ¥ 1,765.44        ← 根据配置显示       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

#### 3.2.2 账本选择功能

**显示位置**：摘要区域右上角

**交互流程**：
1. 点击账本名称，弹出账本选择弹窗
2. 选择账本后，切换当前账本
3. 重新查询摘要数据并刷新显示

**账本选择弹窗设计**：

```
┌─────────────────────────────────────────────────────────────┐
│  选择账本                                              [×]  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ○ 日常账本        默认              ← 默认账本，绿色标识   │
│  ● 旅游账本                          ← 当前选中账本         │
│  ○ 投资账本                                                 │
│  ○ 工作账本                                                 │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**默认账本标识样式**：
- 文字："默认"
- 颜色：绿色 `#00a151`
- 位置：账本名称右侧

```vue
<!-- 账本选择弹窗 -->
<van-action-sheet v-model:show="showAccountBookPicker" title="选择账本">
  <div class="account-book-list">
    <van-radio-group v-model="currentAccountBookId">
      <van-cell
        v-for="book in accountBookList"
        :key="book.id"
        clickable
        @click="handleSelectAccountBook(book)"
      >
        <template #title>
          <div class="book-item">
            <span class="book-name">{{ book.name }}</span>
            <span v-if="book.isDefault" class="default-tag">默认</span>
          </div>
        </template>
        <template #right-icon>
          <van-radio :name="book.id" />
        </template>
      </van-cell>
    </van-radio-group>
  </div>
</van-action-sheet>
```

```scss
.default-tag {
  margin-left: 8px;
  font-size: 12px;
  color: #00a151;  // 绿色
}
```

**切换账本逻辑**：

```typescript
// 当前选中的账本ID
const currentAccountBookId = ref<string>('');

// 初始化时设置为默认账本
const initAccountBook = () => {
  const accountBookStore = useAccountBookStore();
  currentAccountBookId.value = accountBookStore.defaultAccountBookId;
};

// 选择账本
const handleSelectAccountBook = async (book: AccountBook) => {
  currentAccountBookId.value = book.id;
  showAccountBookPicker.value = false;

  // 重新加载摘要数据
  await loadSummary(book.id);
};

// 加载摘要数据（支持指定账本）
const loadSummary = async (accountBookId?: string) => {
  const targetId = accountBookId || currentAccountBookId.value;
  if (!targetId) return;

  try {
    const result = await getIncomeExpenseSummary({
      accountBookId: targetId,
      type: 'month'
    });
    summaryData.value = result;
  } catch (error) {
    console.error('加载摘要数据失败:', error);
    throw error;
  }
};
```

#### 3.2.3 显示规则

| 字段 | 显示条件 | 样式 |
|------|----------|------|
| 当前账本 | 始终显示 | 右上角，可点击，带下拉箭头 |
| 本月支出 | 始终显示 | 大字体 (28px)，红色 `#d83d34` |
| 本月收入 | 始终显示 | 小字体 (20px)，绿色 `#00a151` |
| 支出限额 | `userConfig.showExpenseLimit === true` | 根据 `userConfig.limitType` 显示月限额或年限额 |
| 剩余限额 | `userConfig.showExpenseLimit === true` | 根据 `userConfig.limitType` 显示月剩余或年剩余 |

#### 3.2.4 限额计算逻辑

```typescript
// 获取限额显示数据
const getLimitData = computed(() => {
  const config = userConfigStore.config;

  if (!config.showExpenseLimit) {
    return null;
  }

  const limitType = config.limitType || 'month'; // 默认月限额

  if (limitType === 'month') {
    return {
      type: 'month',
      expenseLimit: config.monthExpenseLimit || 0,
      remainingLimit: (config.monthExpenseLimit || 0) - summaryData.monthExpense
    };
  } else {
    return {
      type: 'year',
      expenseLimit: config.yearExpenseLimit || 0,
      remainingLimit: (config.yearExpenseLimit || 0) - summaryData.yearExpense
    };
  }
});
```

### 3.3 收支记录列表设计

#### 3.3.1 列表项布局

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  ┌──────┐                                                   │
│  │ 04-23│  ┌────┐  餐饮-早餐                               │
│  │ 周三 │  │ 🍔 │  豆浆油条                    ¥ 15.00     │
│  └──────┘  └────┘                                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  ┌──────┐                                                   │
│  │ 04-22│  ┌────┐  工资                                    │
│  │ 周二 │  │ 💰 │  4月工资                   ¥ 8,000.00    │
│  └──────┘  └────┘                                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

#### 3.3.2 列表项字段说明

| 字段 | 说明 | 样式 |
|------|------|------|
| 日期 | 格式：MM-DD 周几 | 灰色文字，固定宽度 |
| 分类图标 | 根据分类配置显示 | 居中显示 |
| 分类名称 | 格式：父分类-子分类（无子分类时只显示父分类） | 黑色文字 |
| 备注 | 显示在分类名称下方 | 灰色小字 |
| 金额 | 支出：红色，收入：绿色，不带正负号 | 右对齐 |

#### 3.3.3 金额显示格式

```typescript
// 金额格式化（不带正负号，通过颜色区分收支类型）
const formatAmount = (amount: number, type: 1 | 2) => {
  const color = type === 1 ? '#d83d34' : '#00a151';
  const formattedAmount = amount.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');

  return {
    text: `¥ ${formattedAmount}`,
    color
  };
};
```

### 3.4 悬浮按钮设计

#### 3.4.1 按钮样式

```scss
.floating-btn {
  position: fixed;
  right: 16px;
  bottom: calc(50px + env(safe-area-inset-bottom) + 16px); // TabBar 高度 + 安全区域 + 间距
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background-color: $color-primary;
  color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;

  &:active {
    transform: scale(0.95);
  }
}
```

#### 3.4.2 按钮交互

- 点击：跳转到记账页面 (`/record`)
- 页面滚动时：保持固定位置

---

## 4. 交互设计

### 4.1 下拉刷新

```vue
<template>
  <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
    <!-- 摘要区域 -->
    <div class="summary-section">
      <!-- ... -->
    </div>
  </van-pull-refresh>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { showSuccess, showError } from '@/utils/mobile/message';

const refreshing = ref(false);

const onRefresh = async () => {
  try {
    await fetchSummary();
    showSuccess('刷新成功');
  } catch (error) {
    showError('刷新失败');
  } finally {
    refreshing.value = false;
  }
};
</script>
```

### 4.2 列表项点击

```typescript
// 点击列表项，跳转到编辑页面
const handleRecordClick = (record: RecentRecord) => {
  router.push(`/record/${record.id}`);
};
```

### 4.3 悬浮按钮点击

```typescript
// 点击记一笔按钮，跳转到新增页面
const handleAddRecord = () => {
  router.push('/record');
};
```

---

## 5. Store 设计

### 5.1 账本 Store

```typescript
// store/modules/accountBook.ts
import { defineStore } from 'pinia';
import { getAccountBookList } from '@/api/accountBook';
import type { AccountBook } from '@/types/accountBook';

interface AccountBookState {
  list: AccountBook[];
  defaultId: string | null;
  loading: boolean;
}

export const useAccountBookStore = defineStore('accountBook', {
  state: (): AccountBookState => ({
    list: [],
    defaultId: null,
    loading: false
  }),

  getters: {
    // 默认账本
    defaultAccountBook: (state) => {
      return state.list.find(item => item.isDefault) || state.list[0] || null;
    },
    // 默认账本ID
    defaultAccountBookId: (state) => {
      const defaultBook = state.list.find(item => item.isDefault);
      return defaultBook?.id || state.list[0]?.id || null;
    }
  },

  actions: {
    async fetchList() {
      this.loading = true;
      try {
        const result = await getAccountBookList();
        this.list = result || [];
      } finally {
        this.loading = false;
      }
    },

    setList(list: AccountBook[]) {
      this.list = list;
    }
  }
});
```

### 5.2 标签 Store

```typescript
// store/modules/userTag.ts
import { defineStore } from 'pinia';
import { getUserTagList } from '@/api/userTag';
import type { UserTag } from '@/types/userTag';

interface UserTagState {
  list: UserTag[];
  loading: boolean;
}

export const useUserTagStore = defineStore('userTag', {
  state: (): UserTagState => ({
    list: [],
    loading: false
  }),

  getters: {
    // 根据ID获取标签
    getTagById: (state) => (id: string) => {
      return state.list.find(item => item.id === id);
    }
  },

  actions: {
    async fetchList() {
      this.loading = true;
      try {
        const result = await getUserTagList();
        this.list = result || [];
      } finally {
        this.loading = false;
      }
    },

    setList(list: UserTag[]) {
      this.list = list;
    }
  }
});
```

### 5.3 备注 Store

```typescript
// store/modules/remark.ts
import { defineStore } from 'pinia';
import { getRemarkList } from '@/api/remark';
import type { Remark } from '@/types/remark';

interface RemarkState {
  list: Remark[];
  loading: boolean;
}

export const useRemarkStore = defineStore('remark', {
  state: (): RemarkState => ({
    list: [],
    loading: false
  }),

  getters: {
    // 根据分类ID获取备注列表
    getRemarksByClassifyId: (state) => (classifyId: string) => {
      return state.list.filter(item => item.classifyId === classifyId);
    }
  },

  actions: {
    async fetchList() {
      this.loading = true;
      try {
        const result = await getRemarkList();
        this.list = result || [];
      } finally {
        this.loading = false;
      }
    },

    setList(list: Remark[]) {
      this.list = list;
    }
  }
});
```

### 5.4 用户配置 Store

```typescript
// store/modules/userConfig.ts
import { defineStore } from 'pinia';
import { getUserConfig } from '@/api/userConfig';
import type { UserConfig } from '@/types/userConfig';

interface UserConfigState {
  config: UserConfig | null;
  loading: boolean;
}

export const useUserConfigStore = defineStore('userConfig', {
  state: (): UserConfigState => ({
    config: null,
    loading: false
  }),

  getters: {
    // 是否显示支出限额
    showExpenseLimit: (state) => {
      return state.config?.showExpenseLimit ?? false;
    },
    // 限额类型
    limitType: (state) => {
      return state.config?.limitType ?? 'month';
    },
    // 月支出限额
    monthExpenseLimit: (state) => {
      return state.config?.monthExpenseLimit ?? 0;
    },
    // 年支出限额
    yearExpenseLimit: (state) => {
      return state.config?.yearExpenseLimit ?? 0;
    }
  },

  actions: {
    async fetchConfig() {
      this.loading = true;
      try {
        const result = await getUserConfig();
        this.config = result;
      } finally {
        this.loading = false;
      }
    },

    setConfig(config: UserConfig) {
      this.config = config;
    }
  }
});
```

---

## 6. 组件设计

### 6.1 首页组件结构

```
src/views/mobile/home/
├── index.vue              # 首页主组件
├── components/
│   ├── SummaryCard.vue    # 摘要卡片组件
│   └── RecordItem.vue     # 收支记录项组件（可复用）
```

### 6.2 SummaryCard 组件

```vue
<!-- src/views/mobile/home/components/SummaryCard.vue -->
<script setup lang="ts">
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useUserConfigStore } from '@/store/modules/userConfig';
import { useAccountBookStore } from '@/store/modules/accountBook';
import type { AccountBook } from '@/types/accountBook';

interface Props {
  monthExpense: number;
  monthIncome: number;
  expenseLimit?: number;
  remainingLimit?: number;
  limitType?: 'month' | 'year';
  showLimit?: boolean;
  currentAccountBookId: string;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  'update:currentAccountBookId': [id: string];
}>();

const { t } = useI18n();
const userConfigStore = useUserConfigStore();
const accountBookStore = useAccountBookStore();

// 账本选择弹窗
const showAccountBookPicker = ref(false);

// 格式化金额
const formatMoney = (amount: number) => {
  return amount.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
};

// 当前账本名称
const currentAccountBookName = computed(() => {
  const book = accountBookStore.list.find(item => item.id === props.currentAccountBookId);
  return book?.name || '';
});

// 选择账本
const handleSelectAccountBook = (book: AccountBook) => {
  emit('update:currentAccountBookId', book.id);
  showAccountBookPicker.value = false;
};
</script>

<template>
  <div class="summary-card">
    <!-- 账本选择 -->
    <div class="account-book-selector" @click="showAccountBookPicker = true">
      <span class="book-name">{{ currentAccountBookName }}</span>
      <van-icon name="arrow-down" size="12" />
    </div>

    <!-- 本月支出 -->
    <div class="expense-row">
      <span class="label">{{ t('mobile.home.monthExpense') }}</span>
      <span class="amount expense">¥ {{ formatMoney(monthExpense) }}</span>
    </div>

    <!-- 本月收入 -->
    <div class="income-row">
      <span class="label">{{ t('mobile.home.monthIncome') }}</span>
      <span class="amount income">¥ {{ formatMoney(monthIncome) }}</span>
    </div>

    <!-- 限额区域 -->
    <template v-if="showLimit">
      <van-divider />
      <div class="limit-row">
        <div class="limit-item">
          <span class="label">
            {{ limitType === 'month' ? '月支出限额' : '年支出限额' }}
          </span>
          <span class="value">¥ {{ formatMoney(expenseLimit || 0) }}</span>
        </div>
        <div class="limit-item">
          <span class="label">剩余限额</span>
          <span class="value" :class="{ warning: (remainingLimit || 0) < 0 }">
            ¥ {{ formatMoney(remainingLimit || 0) }}
          </span>
        </div>
      </div>
    </template>

    <!-- 账本选择弹窗 -->
    <van-action-sheet v-model:show="showAccountBookPicker" title="选择账本">
      <div class="account-book-list">
        <van-radio-group :model-value="currentAccountBookId">
          <van-cell
            v-for="book in accountBookStore.list"
            :key="book.id"
            clickable
            @click="handleSelectAccountBook(book)"
          >
            <template #title>
              <div class="book-item">
                <span class="book-name">{{ book.name }}</span>
                <span v-if="book.isDefault" class="default-tag">默认</span>
              </div>
            </template>
            <template #right-icon>
              <van-radio :name="book.id" />
            </template>
          </van-cell>
        </van-radio-group>
      </div>
    </van-action-sheet>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.summary-card {
  padding: 16px;
  background-color: $color-card;
  border-radius: 12px;
  margin: 16px;
  position: relative;
}

.account-book-selector {
  position: absolute;
  top: 12px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  cursor: pointer;

  .book-name {
    font-size: 13px;
    color: $color-text-secondary;
  }

  &:active {
    opacity: 0.7;
  }
}

.expense-row {
  .amount {
    font-size: 28px;
    font-weight: 600;
    color: $color-primary;
  }
}

.income-row {
  margin-top: 8px;

  .amount {
    font-size: 20px;
    font-weight: 500;
    color: $color-secondary;
  }
}

.label {
  display: block;
  font-size: 12px;
  color: $color-text-secondary;
  margin-bottom: 4px;
}

.limit-row {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
}

.limit-item {
  .value {
    font-size: 16px;
    font-weight: 500;
    color: $color-text-primary;

    &.warning {
      color: $color-danger;
    }
  }
}

.account-book-list {
  max-height: 300px;
  overflow-y: auto;
}

.book-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.default-tag {
  font-size: 12px;
  color: #00a151;  // 绿色
}
</style>
```

### 6.3 RecordItem 组件

```vue
<!-- src/components/mobile/RecordItem.vue -->
<script setup lang="ts">
import { computed } from 'vue';
import dayjs from 'dayjs';

interface Props {
  id: string;
  date: string;
  type: 1 | 2; // 1: 支出, 2: 收入
  amount: number;
  classifyIcon: string;
  classifyName: string;
  parentClassifyName?: string;
  remark?: string;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  click: [id: string];
}>();

// 格式化日期
const formattedDate = computed(() => {
  const date = dayjs(props.date);
  return {
    day: date.format('MM-DD'),
    week: date.format('ddd')
  };
});

// 分类显示名称
const classifyDisplay = computed(() => {
  if (props.parentClassifyName) {
    return `${props.parentClassifyName}-${props.classifyName}`;
  }
  return props.classifyName;
});

// 金额显示（不带正负号，通过颜色区分）
const amountDisplay = computed(() => {
  const formattedAmount = props.amount.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  return {
    text: `¥ ${formattedAmount}`,
    color: props.type === 1 ? '#d83d34' : '#00a151'
  };
});

const handleClick = () => {
  emit('click', props.id);
};
</script>

<template>
  <div class="record-item" @click="handleClick">
    <!-- 日期 -->
    <div class="date-section">
      <div class="day">{{ formattedDate.day }}</div>
      <div class="week">{{ formattedDate.week }}</div>
    </div>

    <!-- 分类图标 -->
    <div class="icon-section">
      <span class="classify-icon">{{ classifyIcon }}</span>
    </div>

    <!-- 分类名称和备注 -->
    <div class="info-section">
      <div class="classify-name">{{ classifyDisplay }}</div>
      <div v-if="remark" class="remark">{{ remark }}</div>
    </div>

    <!-- 金额 -->
    <div class="amount-section" :style="{ color: amountDisplay.color }">
      {{ amountDisplay.text }}
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.record-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background-color: $color-card;
  border-bottom: 1px solid $color-border;

  &:active {
    background-color: #f5f5f5;
  }
}

.date-section {
  width: 48px;
  text-align: center;

  .day {
    font-size: 14px;
    font-weight: 500;
    color: $color-text-primary;
  }

  .week {
    font-size: 12px;
    color: $color-text-secondary;
  }
}

.icon-section {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 12px;
  background-color: #f5f5f5;
  border-radius: 8px;

  .classify-icon {
    font-size: 20px;
  }
}

.info-section {
  flex: 1;
  min-width: 0;

  .classify-name {
    font-size: 14px;
    color: $color-text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .remark {
    font-size: 12px;
    color: $color-text-secondary;
    margin-top: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.amount-section {
  font-size: 14px;
  font-weight: 500;
}
</style>
```

---

## 7. 首页主组件实现

```vue
<!-- src/views/mobile/home/index.vue -->
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { storeToRefs } from 'pinia';
import { useAccountBookStore } from '@/store/modules/accountBook';
import { useUserTagStore } from '@/store/modules/userTag';
import { useRemarkStore } from '@/store/modules/remark';
import { useUserConfigStore } from '@/store/modules/userConfig';
import { getIncomeExpenseSummary } from '@/api/incomeExpense';
import { showLoading, hideLoading, showError } from '@/utils/mobile/message';
import SummaryCard from './components/SummaryCard.vue';
import RecordItem from '@/components/mobile/RecordItem.vue';

defineOptions({
  name: 'MobileHome'
});

const router = useRouter();
const { t } = useI18n();

// Stores
const accountBookStore = useAccountBookStore();
const userTagStore = useUserTagStore();
const remarkStore = useRemarkStore();
const userConfigStore = useUserConfigStore();

// 状态
const refreshing = ref(false);
const summaryData = ref<any>(null);
const currentAccountBookId = ref<string>('');

// 用户配置
const { config } = storeToRefs(userConfigStore);

// 加载用户基础数据
const loadUserData = async () => {
  try {
    await Promise.all([
      accountBookStore.fetchList(),
      userTagStore.fetchList(),
      remarkStore.fetchList(),
      userConfigStore.fetchConfig()
    ]);
  } catch (error) {
    console.error('加载用户数据失败:', error);
  }
};

// 加载摘要数据
const loadSummary = async (accountBookId?: string) => {
  const targetId = accountBookId || currentAccountBookId.value;
  if (!targetId) return;

  try {
    const result = await getIncomeExpenseSummary({
      accountBookId: targetId,
      type: 'month'
    });
    summaryData.value = result;
  } catch (error) {
    console.error('加载摘要数据失败:', error);
    throw error;
  }
};

// 初始化页面
const initPage = async () => {
  showLoading(t('mobile.common.loading'));

  try {
    await loadUserData();
    // 设置默认账本
    currentAccountBookId.value = accountBookStore.defaultAccountBookId;
    await loadSummary();
  } catch (error) {
    showError(t('mobile.common.failed'));
  } finally {
    hideLoading();
  }
};

// 切换账本
const handleAccountBookChange = async (bookId: string) => {
  currentAccountBookId.value = bookId;
  await loadSummary(bookId);
};

// 下拉刷新
const onRefresh = async () => {
  try {
    await loadSummary();
  } catch (error) {
    showError(t('mobile.common.failed'));
  } finally {
    refreshing.value = false;
  }
};

// 点击记录项
const handleRecordClick = (id: string) => {
  router.push(`/record/${id}`);
};

// 点击记一笔
const handleAddRecord = () => {
  router.push('/record');
};

onMounted(() => {
  initPage();
});
</script>

<template>
  <div class="home-page">
    <!-- 下拉刷新区域 -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <!-- 摘要卡片 -->
      <SummaryCard
        v-if="summaryData"
        v-model:currentAccountBookId="currentAccountBookId"
        @update:currentAccountBookId="handleAccountBookChange"
        :month-expense="summaryData.monthExpense"
        :month-income="summaryData.monthIncome"
        :expense-limit="summaryData.expenseLimit"
        :remaining-limit="summaryData.remainingLimit"
        :limit-type="summaryData.limitType"
        :show-limit="config?.showExpenseLimit"
      />
    </van-pull-refresh>

    <!-- 近三日记录列表 -->
    <div class="record-list">
      <div class="list-header">
        {{ t('mobile.home.recentBills') }}
      </div>

      <div v-if="summaryData?.recentRecords?.length" class="list-content">
        <RecordItem
          v-for="record in summaryData.recentRecords"
          :key="record.id"
          v-bind="record"
          @click="handleRecordClick"
        />
      </div>

      <van-empty v-else :description="t('mobile.common.noData')" />
    </div>

    <!-- 悬浮按钮 -->
    <div class="floating-btn" @click="handleAddRecord">
      <van-icon name="plus" size="24" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.home-page {
  min-height: 100vh;
  background-color: $color-background;
  padding-bottom: calc(50px + env(safe-area-inset-bottom));
}

.record-list {
  margin-top: 12px;

  .list-header {
    padding: 12px 16px;
    font-size: 14px;
    font-weight: 500;
    color: $color-text-primary;
  }

  .list-content {
    background-color: $color-card;
    max-height: calc(100vh - 280px);
    overflow-y: auto;
  }
}

.floating-btn {
  position: fixed;
  right: 16px;
  bottom: calc(50px + env(safe-area-inset-bottom) + 16px);
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background-color: $color-primary;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 100;

  &:active {
    transform: scale(0.95);
  }
}
</style>
```

---

## 8. 国际化文案

```yaml
# locales/mobile/zh-CN.yaml 新增

home:
  title: 首页
  monthExpense: 本月支出
  monthIncome: 本月收入
  monthBalance: 本月结余
  recentBills: 近三日账单
  quickRecord: 记一笔
  expenseLimit: 支出限额
  surplus: 剩余限额
  records: "{count} 笔"
  monthLimit: 月支出限额
  yearLimit: 年支出限额
  remainingLimit: 剩余限额
```

```yaml
# locales/mobile/en.yaml 新增

home:
  title: Home
  monthExpense: Monthly Expense
  monthIncome: Monthly Income
  monthBalance: Balance
  recentBills: Recent Bills
  quickRecord: Add Record
  expenseLimit: Expense Limit
  surplus: Remaining
  records: "{count} records"
  monthLimit: Monthly Limit
  yearLimit: Yearly Limit
  remainingLimit: Remaining
```
