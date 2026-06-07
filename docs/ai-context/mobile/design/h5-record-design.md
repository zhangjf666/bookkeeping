# H5 记账页面详细设计文档

## 1. 页面概述

### 1.1 页面定位
记账页面用于新增或编辑收入支出记录，是一个二级页面，可从首页、账单页等多个入口进入。

### 1.2 页面特性
- **二级页面**：有顶部导航栏，提供返回功能
- **复用设计**：新增和编辑共用同一页面，通过路由参数区分
- **动态表单**：根据收支类型切换显示不同的分类选项

### 1.3 页面入口

| 入口页面 | 路由 | 导航栏标题 | 返回目标 |
|----------|------|------------|----------|
| 首页新增 | `/record` | 新增收入支出记录 | 返回首页 |
| 首页编辑 | `/record/:id` | 编辑收入支出记录 | 返回首页 |
| 账单新增 | `/record` | 新增收入支出记录 | 返回账单 |
| 账单编辑 | `/record/:id` | 编辑收入支出记录 | 返回账单 |

---

## 2. 页面布局

### 2.1 整体布局结构

```
┌─────────────────────────────────────────────────────────────┐
│  导航栏                                                     │
│  ← 返回    新增收入支出记录                                  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │   支出    │    收入        ← 类型切换 Tab             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ¥ 0.00                          ← 金额显示/输入      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  账本：默认账本                        → 选择        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  分类：请选择分类                      → 选择        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  日期：2024-04-24                      → 选择        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  备注：请选择或输入备注                 → 选择/输入   │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  加入常用备注  ○                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  标签：请选择标签                      → 选择        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  信用卡消费  ○         ← 仅支出类型显示              │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              保 存                                   │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. 功能模块详细设计

### 3.1 导航栏

```vue
<van-nav-bar
  :title="isEdit ? '编辑收入支出记录' : '新增收入支出记录'"
  left-arrow
  @click-left="handleBack"
/>
```

- **返回按钮**：点击返回上一级页面（使用 `router.back()`）
- **标题**：根据是否编辑模式显示不同标题

### 3.2 类型切换 Tab

```vue
<van-tabs v-model:active="recordType" @change="handleTypeChange">
  <van-tab :name="1">{{ t('mobile.record.expense') }}</van-tab>
  <van-tab :name="2">{{ t('mobile.record.income') }}</van-tab>
</van-tabs>
```

**交互逻辑**：
- 默认选中"支出"
- 切换类型时，自动过滤分类列表（只显示对应类型的分类）
- 切换到"收入"时，隐藏"信用卡消费"开关
- 如果当前选中的分类与新类型不匹配，清空分类选择

```typescript
// 类型切换处理
const handleTypeChange = (type: 1 | 2) => {
  // 清空当前分类选择（分类需要重新选择）
  formData.value.classifyId = '';
  formData.value.parentClassifyId = '';

  // 如果是收入类型，清空信用卡消费
  if (type === 2) {
    formData.value.isCreditCard = false;
  }

  // 重新加载分类列表
  loadClassifyList(type);
};
```

### 3.3 金额输入

#### 3.3.1 显示区域

```vue
<div class="amount-display" @click="showAmountKeyboard = true">
  <span class="currency">¥</span>
  <span class="amount">{{ formatAmount(formData.amount) }}</span>
</div>
```

#### 3.3.2 数字键盘

使用 Vant 的 NumberKeyboard 组件：

```vue
<van-number-keyboard
  v-model:show="showAmountKeyboard"
  v-model="formData.amount"
  theme="custom"
  extra-key="."
  close-button-text="确定"
  @close="handleAmountConfirm"
  @blur="showAmountKeyboard = false"
>
  <template #title>
    <div class="keyboard-title">输入金额</div>
  </template>
</van-number-keyboard>
```

#### 3.3.3 金额处理逻辑

```typescript
// 金额输入值（字符串形式）
const amountInput = ref('0');

// 格式化显示金额
const formatAmount = (value: string) => {
  if (!value || value === '0') return '0.00';

  // 限制最多2位小数
  const parts = value.split('.');
  if (parts[1] && parts[1].length > 2) {
    parts[1] = parts[1].substring(0, 2);
    amountInput.value = parts.join('.');
  }

  // 格式化为2位小数
  const num = parseFloat(parts.join('.'));
  return num.toFixed(2);
};

// 确认金额
const handleAmountConfirm = () => {
  const num = parseFloat(amountInput.value) || 0;
  formData.value.amount = parseFloat(num.toFixed(2));
  showAmountKeyboard.value = false;
};

// 实时更新显示
watch(amountInput, (newVal) => {
  // 限制最多2位小数
  if (newVal.includes('.')) {
    const parts = newVal.split('.');
    if (parts[1] && parts[1].length > 2) {
      amountInput.value = parts[0] + '.' + parts[1].substring(0, 2);
    }
  }
});
```

#### 3.3.4 金额验证规则

- 最多2位小数
- 不能为负数
- 最大金额限制：999,999,999.99
- 多余的小数位输入无效（自动截断）

### 3.4 账本选择

#### 3.4.1 显示与选择

```vue
<van-cell
  title="账本"
  :value="selectedAccountBook?.name || '请选择账本'"
  is-link
  @click="showAccountBookPicker = true"
/>

<van-action-sheet v-model:show="showAccountBookPicker" title="选择账本">
  <div class="account-book-list">
    <van-radio-group v-model="formData.accountBookId">
      <van-cell
        v-for="book in accountBookList"
        :key="book.id"
        clickable
        @click="handleSelectAccountBook(book)"
      >
        <template #title>
          <div class="book-item">
            <span class="book-name">{{ book.name }}</span>
            <van-tag v-if="book.isDefault" type="primary" size="small">默认</van-tag>
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

#### 3.4.2 默认值逻辑

```typescript
// 初始化时设置默认账本
const initAccountBook = () => {
  const accountBookStore = useAccountBookStore();
  formData.value.accountBookId = accountBookStore.defaultAccountBookId;
};
```

### 3.5 分类选择

#### 3.5.1 显示与选择

```vue
<van-cell
  title="分类"
  :value="classifyDisplayName || '请选择分类'"
  is-link
  @click="showClassifyPicker = true"
/>
```

#### 3.5.2 分类选择器设计

分类支持选择顶级分类或子分类，使用弹窗展示分类网格：

```
┌─────────────────────────────────────────────────────────────┐
│  选择分类                                              [×]  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐                        │
│  │ 🍔  │  │ 🚗  │  │ 🏠  │  │ 👕  │                        │
│  │ 餐饮│  │ 交通│  │ 居住│  │ 购物│                        │
│  └─────┘  └─────┘  └─────┘  └─────┘                        │
│                                                             │
│  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐                        │
│  │ 🎮  │  │ 📱  │  │ 🏥  │  │ 💰  │                        │
│  │ 娱乐│  │ 通讯│  │ 医疗│  │ 其他│                        │
│  └─────┘  └─────┘  └─────┘  └─────┘                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘

点击有子分类的分类后，展开子分类：

┌─────────────────────────────────────────────────────────────┐
│  选择分类 - 餐饮                                        [×]  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ← 返回上级                                                 │
│                                                             │
│  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐                        │
│  │ 🌅  │  │ 🍱  │  │ 🍜  │  │ 🍵  │                        │
│  │ 早餐│  │ 午餐│  │ 晚餐│  │ 饮料│                        │
│  └─────┘  └─────┘  └─────┘  └─────┘                        │
│                                                             │
│  ┌─────┐  ┌─────┐                                          │
│  │ 🍰  │  │ 🍿  │                                          │
│  │ 零食│  │ 水果│                                          │
│  └─────┘  └─────┘                                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

#### 3.5.3 分类选择逻辑

```typescript
// 分类数据
const classifyList = ref<Classify[]>([]);
const currentParentClassify = ref<Classify | null>(null);

// 加载分类列表
const loadClassifyList = async (type: 1 | 2) => {
  const classifyStore = useClassifyStore();
  classifyList.value = classifyStore.getClassifyByType(type);
};

// 选择分类
const handleSelectClassify = (classify: Classify) => {
  if (classify.children && classify.children.length > 0) {
    // 有子分类，进入下一级
    currentParentClassify.value = classify;
  } else {
    // 无子分类，直接选中
    formData.value.parentClassifyId = currentParentClassify.value?.id || '';
    formData.value.classifyId = classify.id;
    formData.value.classifyName = classify.name;
    showClassifyPicker.value = false;
  }
};

// 返回上级分类
const handleBackToParent = () => {
  currentParentClassify.value = null;
};

// 显示名称
const classifyDisplayName = computed(() => {
  if (!formData.value.classifyId) return '';

  const parentName = currentParentClassify.value?.name;
  if (parentName) {
    return `${parentName}-${formData.value.classifyName}`;
  }
  return formData.value.classifyName;
});
```

### 3.6 日期选择

#### 3.6.1 显示与选择

```vue
<van-cell
  title="日期"
  :value="formatDate(formData.date)"
  is-link
  @click="showDatePicker = true"
/>

<van-popup v-model:show="showDatePicker" position="bottom" round>
  <van-date-picker
    v-model="selectedDate"
    title="选择日期"
    :min-date="minDate"
    :max-date="maxDate"
    @confirm="handleDateConfirm"
    @cancel="showDatePicker = false"
  />
</van-popup>
```

#### 3.6.2 日期处理逻辑

```typescript
import dayjs from 'dayjs';

// 默认今天
const formData = ref({
  date: dayjs().format('YYYY-MM-DD')
});

// 日期范围
const minDate = new Date(2020, 0, 1);
const maxDate = new Date();

// 格式化显示
const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD');
};

// 确认日期
const handleDateConfirm = ({ selectedValues }) => {
  formData.value.date = selectedValues.join('-');
  showDatePicker.value = false;
};
```

#### 3.6.3 当前日期醒目标识

Vant DatePicker 默认会高亮当前日期，可通过 CSS 进一步强调：

```scss
:deep(.van-picker__toolbar) {
  // 当前日期标题样式
}

:deep(.van-date-picker) {
  // 当前日期选中样式
}
```

### 3.7 备注选择

#### 3.7.1 显示与选择

```vue
<van-cell title="备注" is-link @click="showRemarkPicker = true">
  <template #value>
    <span v-if="formData.remark">{{ formData.remark }}</span>
    <span v-else class="placeholder">请选择或输入备注</span>
  </template>
</van-cell>
```

#### 3.7.2 备注选择器设计

```
┌─────────────────────────────────────────────────────────────┐
│  选择备注                                              [×]  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  自定义备注输入框                                    │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  常用备注：                                                 │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐                    │
│  │ 豆浆油条│  │ 打车回家│  │ 午餐补贴│                    │
│  └─────────┘  └─────────┘  └─────────┘                    │
│                                                             │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐                    │
│  │ 地铁通勤│  │ 生日礼物│  │ 房租    │                    │
│  └─────────┘  └─────────┘  └─────────┘                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

#### 3.7.3 备注处理逻辑

```typescript
// 备注列表
const remarkList = computed(() => {
  const remarkStore = useRemarkStore();
  return remarkStore.list;
});

// 选择常用备注
const handleSelectRemark = (remark: Remark) => {
  formData.value.remark = remark.content;

  // 自动设置关联的分类
  if (remark.classifyId) {
    formData.value.classifyId = remark.classifyId;
    formData.value.parentClassifyId = remark.parentClassifyId || '';

    // 查找分类名称
    const classifyStore = useClassifyStore();
    const classify = classifyStore.getClassifyById(remark.classifyId);
    if (classify) {
      formData.value.classifyName = classify.name;
    }
  }

  showRemarkPicker.value = false;
};

// 自定义备注输入
const customRemark = ref('');
const handleCustomRemarkConfirm = () => {
  if (customRemark.value.trim()) {
    formData.value.remark = customRemark.value.trim();
    showRemarkPicker.value = false;
  }
};
```

### 3.8 加入常用备注开关

```vue
<van-cell center title="加入常用备注">
  <template #right-icon>
    <van-switch v-model="formData.addToRemark" size="20" />
  </template>
</van-cell>
```

**逻辑说明**：
- 勾选后，提交记录时 `addToRemark` 参数设为 `true`
- 保存成功后，需要重新加载用户备注列表并更新 Store

```typescript
// 保存成功后的处理
const handleSaveSuccess = async () => {
  // 如果勾选了加入常用备注，重新加载备注列表
  if (formData.value.addToRemark) {
    const remarkStore = useRemarkStore();
    await remarkStore.fetchList();
  }

  showSuccess('保存成功');
  router.back();
};
```

### 3.9 标签选择

#### 3.9.1 显示与选择

```vue
<van-cell title="标签" is-link @click="showTagPicker = true">
  <template #value>
    <span v-if="selectedTags.length">{{ selectedTags.map(t => t.name).join(', ') }}</span>
    <span v-else class="placeholder">请选择标签</span>
  </template>
</van-cell>
```

#### 3.9.2 标签选择器设计

```
┌─────────────────────────────────────────────────────────────┐
│  选择标签                                              [×]  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  🔍 搜索标签...                                      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  已选择：工作、重要                                          │
│                                                             │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐                    │
│  │ 工作    │  │ 生活    │  │ 重要    │                    │
│  │ (选中)  │  │         │  │ (选中)  │                    │
│  └─────────┘  └─────────┘  └─────────┘                    │
│                                                             │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐                    │
│  │ 购物    │  │ 旅游    │  │ 健康    │                    │
│  └─────────┘  └─────────┘  └─────────┘                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

#### 3.9.3 标签样式设计

```scss
// 未选中状态
.tag-item {
  background: transparent;
  border: 1px solid var(--tag-color);
  color: var(--tag-color);
}

// 选中状态
.tag-item.selected {
  background: var(--tag-color);
  border: 1px solid var(--tag-color);
  color: #fff;
}
```

#### 3.9.4 标签选择逻辑

```typescript
// 标签搜索
const tagSearchKeyword = ref('');
const filteredTags = computed(() => {
  const userTagStore = useUserTagStore();
  const list = userTagStore.list;

  if (!tagSearchKeyword.value) return list;

  return list.filter(tag =>
    tag.name.toLowerCase().includes(tagSearchKeyword.value.toLowerCase())
  );
});

// 已选标签
const selectedTagIds = ref<string[]>([]);

// 切换标签选择
const toggleTag = (tag: UserTag) => {
  const index = selectedTagIds.value.indexOf(tag.id);
  if (index > -1) {
    selectedTagIds.value.splice(index, 1);
  } else {
    selectedTagIds.value.push(tag.id);
  }
};

// 已选标签对象
const selectedTags = computed(() => {
  const userTagStore = useUserTagStore();
  return selectedTagIds.value.map(id => userTagStore.getTagById(id)).filter(Boolean);
});
```

### 3.10 信用卡消费开关

```vue
<van-cell v-if="formData.type === 1" center title="信用卡消费">
  <template #right-icon>
    <van-switch v-model="formData.isCreditCard" size="20" />
  </template>
</van-cell>
```

**显示条件**：仅在支出类型（`type === 1`）时显示

---

## 4. 表单数据结构

```typescript
interface RecordFormData {
  id?: string;                    // 编辑时有值
  type: 1 | 2;                    // 1: 支出, 2: 收入
  amount: number;                 // 金额
  accountBookId: string;          // 账本ID
  parentClassifyId: string;       // 父分类ID
  classifyId: string;             // 分类ID
  classifyName: string;           // 分类名称
  date: string;                   // 日期 YYYY-MM-DD
  remark: string;                 // 备注
  addToRemark: boolean;           // 是否加入常用备注
  tagIds: string[];               // 标签ID列表
  isCreditCard: boolean;          // 是否信用卡消费（仅支出）
}
```

---

## 5. 提交逻辑

### 5.1 表单验证

```typescript
const validate = () => {
  if (!formData.value.amount || formData.value.amount <= 0) {
    showError('请输入金额');
    return false;
  }

  if (!formData.value.accountBookId) {
    showError('请选择账本');
    return false;
  }

  if (!formData.value.classifyId) {
    showError('请选择分类');
    return false;
  }

  if (!formData.value.date) {
    showError('请选择日期');
    return false;
  }

  return true;
};
```

### 5.2 提交参数

```typescript
interface SaveRecordParams {
  id?: string;                    // 编辑时有值
  type: 1 | 2;                    // 类型
  amount: number;                 // 金额
  accountBookId: string;          // 账本ID
  classifyId: string;             // 分类ID
  date: string;                   // 日期
  remark?: string;                // 备注
  addToRemark?: boolean;          // 加入常用备注
  tagIds?: string[];              // 标签ID
  isCreditCard?: boolean;         // 信用卡消费
}
```

### 5.3 提交流程

```typescript
const handleSave = async () => {
  if (!validate()) return;

  showLoading('保存中...');

  try {
    const params: SaveRecordParams = {
      type: formData.value.type,
      amount: formData.value.amount,
      accountBookId: formData.value.accountBookId,
      classifyId: formData.value.classifyId,
      date: formData.value.date,
      remark: formData.value.remark || undefined,
      addToRemark: formData.value.addToRemark || undefined,
      tagIds: formData.value.tagIds.length > 0 ? formData.value.tagIds : undefined,
      isCreditCard: formData.value.type === 1 ? formData.value.isCreditCard : undefined
    };

    if (isEdit.value) {
      params.id = formData.value.id;
      await updateIncomeExpense(params);
    } else {
      await addIncomeExpense(params);
    }

    hideLoading();

    // 如果勾选了加入常用备注，重新加载备注列表
    if (formData.value.addToRemark) {
      const remarkStore = useRemarkStore();
      await remarkStore.fetchList();
    }

    showSuccess('保存成功');
    router.back();
  } catch (error: any) {
    hideLoading();
    showError(error?.message || '保存失败');
  }
};
```

---

## 6. 编辑模式数据加载

```typescript
// 从路由获取编辑ID
const route = useRoute();
const recordId = computed(() => route.params.id as string);
const isEdit = computed(() => !!recordId.value);

// 加载编辑数据
const loadEditData = async () => {
  if (!isEdit.value) return;

  showLoading('加载中...');

  try {
    const data = await getIncomeExpenseDetail(recordId.value);

    formData.value = {
      id: data.id,
      type: data.type,
      amount: data.amount,
      accountBookId: data.accountBookId,
      parentClassifyId: data.parentClassifyId,
      classifyId: data.classifyId,
      classifyName: data.classifyName,
      date: data.date,
      remark: data.remark || '',
      addToRemark: false,
      tagIds: data.tagIds || [],
      isCreditCard: data.isCreditCard || false
    };

    hideLoading();
  } catch (error: any) {
    hideLoading();
    showError(error?.message || '加载失败');
    router.back();
  }
};
```

---

## 7. 组件结构

```
src/views/mobile/record/
├── index.vue                    # 记账主页面
├── components/
│   ├── AmountInput.vue          # 金额输入组件
│   ├── ClassifyPicker.vue       # 分类选择器
│   ├── RemarkPicker.vue         # 备注选择器
│   └── TagPicker.vue            # 标签选择器
```

---

## 8. 国际化文案

```yaml
# locales/mobile/zh-CN.yaml

record:
  title: 记账
  editTitle: 编辑记录
  expense: 支出
  income: 收入
  amount: 金额
  amountPlaceholder: 请输入金额
  accountBook: 账本
  accountBookPlaceholder: 请选择账本
  classify: 分类
  classifyPlaceholder: 请选择分类
  date: 日期
  datePlaceholder: 请选择日期
  remark: 备注
  remarkPlaceholder: 请选择或输入备注
  remarkCustom: 自定义备注
  addToRemark: 加入常用备注
  tag: 标签
  tagPlaceholder: 请选择标签
  tagSearch: 搜索标签...
  isCreditCard: 信用卡消费
  save: 保存
  saveSuccess: 保存成功
  saveFailed: 保存失败
  validateAmount: 请输入金额
  validateAccountBook: 请选择账本
  validateClassify: 请选择分类
  validateDate: 请选择日期
```

```yaml
# locales/mobile/en.yaml

record:
  title: Record
  editTitle: Edit Record
  expense: Expense
  income: Income
  amount: Amount
  amountPlaceholder: Enter amount
  accountBook: Account Book
  accountBookPlaceholder: Select account book
  classify: Category
  classifyPlaceholder: Select category
  date: Date
  datePlaceholder: Select date
  remark: Remark
  remarkPlaceholder: Select or enter remark
  remarkCustom: Custom remark
  addToRemark: Add to common remarks
  tag: Tags
  tagPlaceholder: Select tags
  tagSearch: Search tags...
  isCreditCard: Credit Card
  save: Save
  saveSuccess: Saved successfully
  saveFailed: Save failed
  validateAmount: Please enter amount
  validateAccountBook: Please select account book
  validateClassify: Please select category
  validateDate: Please select date
```
