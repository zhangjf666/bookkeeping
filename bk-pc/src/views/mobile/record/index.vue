<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { storeToRefs } from "pinia";
import dayjs from "dayjs";
import { useUserStoreHook } from "@/store/modules/user";
import { useAccountBookStore } from "@/store/modules/accountBook";
import { useUserTagStore } from "@/store/modules/userTag";
import { useRemarkStore } from "@/store/modules/remark";
import { useClassifyStore } from "@/store/modules/classify";
import { useBillStore } from "@/store/modules/bill";
import {
  getIncomeExpenseById,
  createIncomeExpense,
  updateIncomeExpense
} from "@/api/incomeExpense";
import type { IncomeExpense, IncomeExpenseRecord } from "@/types/bill";
import type { Classify } from "@/types/classify";
import type { UserRemark } from "@/types/remark";
import {
  showLoading,
  hideLoading,
  showError,
  showSuccess
} from "@/utils/mobile/message";
import AmountInput from "@/components/mobile/AmountInput.vue";
import TagPicker from "@/components/mobile/TagPicker.vue";
import RemarkPicker from "@/components/mobile/RemarkPicker.vue";
import ClassifyPicker from "@/components/mobile/ClassifyPicker.vue";

defineOptions({
  name: "MobileRecord"
});

const route = useRoute();
const router = useRouter();
const { t } = useI18n();

// 用户信息
const userStore = useUserStoreHook();
const userId = computed(() => userStore.id);

// Stores
const accountBookStore = useAccountBookStore();
const userTagStore = useUserTagStore();
const remarkStore = useRemarkStore();
const classifyStore = useClassifyStore();
const billStore = useBillStore();

// 路由参数
const recordId = computed(() => route.params.id as string);
const isEdit = computed(() => !!recordId.value);

// 表单数据
const formData = ref({
  type: "EXPENSE" as "EXPENSE" | "INCOME",
  amount: 0,
  accountBookId: null as number | null,
  mainClassifyId: null as number | null,
  subClassifyId: null as number | null,
  date: dayjs().format("YYYY-MM-DD"),
  remark: "",
  addToRemark: false,
  tagIds: [] as number[],
  isCreditCard: false
});

// 弹窗状态
const showTagPicker = ref(false);
const showRemarkPicker = ref(false);
const showDatePicker = ref(false);
const showAccountBookPicker = ref(false);
const showClassifyPicker = ref(false);

// 日期选择器
const selectedDate = ref(new Date());

// 初始化选中日期
watch(
  () => formData.value.date,
  val => {
    if (val) {
      selectedDate.value = new Date(val);
    }
  },
  { immediate: true }
);

// 日期选择变化 - 直接保存并关闭弹窗
const handleDateSelect = (date: Date) => {
  formData.value.date = dayjs(date).format("YYYY-MM-DD");
  showDatePicker.value = false;
};

// 加载状态
const loading = ref(false);
const saving = ref(false);

// 账本列表
const { list: accountBookList } = storeToRefs(accountBookStore);
const { list: tagList } = storeToRefs(userTagStore);
const { list: remarkList } = storeToRefs(remarkStore);
const { list: classifyList } = storeToRefs(classifyStore);

// 选中的账本名称
const selectedAccountBookName = computed(() => {
  const book = accountBookList.value.find(
    item => item.id === formData.value.accountBookId
  );
  return book?.name || "";
});

// 选中的分类名称
const selectedClassifyName = computed(() => {
  if (!formData.value.mainClassifyId) return "";

  const mainClassify = classifyList.value.find(
    item => item.id === formData.value.mainClassifyId
  );
  if (!mainClassify) return "";

  if (formData.value.subClassifyId) {
    const subClassify = classifyList.value.find(
      item => item.id === formData.value.subClassifyId
    );
    if (subClassify) {
      return `${mainClassify.name}-${subClassify.name}`;
    }
  }

  return mainClassify.name;
});

// 选中的标签列表
const selectedTags = computed(() => {
  return tagList.value.filter(item => formData.value.tagIds.includes(item.id));
});

// 将标签ID转换为标签code
const getTagCodesByIds = (tagIds: number[]) => {
  return tagIds
    .map(id => {
      const tag = tagList.value.find(t => t.id === id);
      return tag ? String((tag as any).code) : null;
    })
    .filter(code => code !== null)
    .join(",");
};

// 将标签code转换为标签ID
const getTagIdsByCodes = (tagCodesStr: string) => {
  if (!tagCodesStr) return [];
  const codes = tagCodesStr.split(",").map(c => Number(c.trim()));
  return tagList.value
    .filter(tag => codes.includes((tag as any).code))
    .map(tag => tag.id);
};

// 构造用于本地更新的 IncomeExpense 对象
const buildLocalRecord = (): IncomeExpense => {
  const mainClassify = classifyList.value.find(
    item => item.id === formData.value.mainClassifyId
  );
  const subClassify = classifyList.value.find(
    item => item.id === formData.value.subClassifyId
  );
  const accountBook = accountBookList.value.find(
    item => item.id === formData.value.accountBookId
  );
  const tagCodes = getTagCodesByIds(formData.value.tagIds);
  const tagNames = tagList.value
    .filter(tag => formData.value.tagIds.includes(tag.id))
    .map(tag => tag.name)
    .join(",");

  return {
    id: isEdit.value ? parseInt(recordId.value) : Date.now(),
    userId: userId.value || 0,
    accountBookId: formData.value.accountBookId!,
    accountBookName: accountBook?.name || "",
    amount: formData.value.amount || 0,
    type: formData.value.type,
    date: formData.value.date,
    remark: formData.value.remark || "",
    mainClassify: formData.value.mainClassifyId!,
    mainClassifyName: mainClassify?.name || "",
    mainClassifyImage: mainClassify?.image || "",
    subClassify: formData.value.subClassifyId || null,
    subClassifyName: subClassify?.name || "",
    subClassifyImage: subClassify?.image || "",
    isCreditCard: formData.value.isCreditCard ? "YES" : "NO",
    tagCodes,
    tagNames,
    createTime: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    updateTime: dayjs().format("YYYY-MM-DD HH:mm:ss")
  } as IncomeExpense;
};

// 类型切换
const handleTypeChange = () => {
  // 清空分类选择
  formData.value.mainClassifyId = null;
  formData.value.subClassifyId = null;
  // 如果是收入类型，清空信用卡消费
  if (formData.value.type === "INCOME") {
    formData.value.isCreditCard = false;
  }
};

// 选择分类
const handleClassifySelect = (
  mainClassify: Classify,
  subClassify?: Classify
) => {
  formData.value.mainClassifyId = mainClassify.id;
  formData.value.subClassifyId = subClassify?.id || null;
};

// 选择备注
const handleRemarkSelect = (remark: UserRemark) => {
  formData.value.remark = remark.remark;

  // 如果备注关联了分类，自动设置分类
  if (remark.classifyId) {
    // 先查找是否是主分类（pid 为 0、-1、null 或 undefined）
    const mainClassify = classifyList.value.find(
      item =>
        item.id === remark.classifyId &&
        (item.pid === 0 ||
          item.pid === -1 ||
          item.pid === null ||
          item.pid === undefined)
    );

    if (mainClassify) {
      // 根据分类类型切换收入/支出
      if (mainClassify.type !== formData.value.type) {
        formData.value.type = mainClassify.type;
      }
      formData.value.mainClassifyId = mainClassify.id;
      formData.value.subClassifyId = null;
    } else {
      // 查找是否是子分类，通过 pid 找到父分类
      const subClassify = classifyList.value.find(
        item => item.id === remark.classifyId
      );
      if (subClassify && subClassify.pid) {
        // 找到父分类
        const parentClassify = classifyList.value.find(
          item => item.id === subClassify.pid
        );
        if (parentClassify) {
          // 根据分类类型切换收入/支出
          if (parentClassify.type !== formData.value.type) {
            formData.value.type = parentClassify.type;
          }
          formData.value.mainClassifyId = parentClassify.id;
          formData.value.subClassifyId = subClassify.id;
        }
      }
    }
  }
};

// 选择账本
const handleSelectAccountBook = (bookId: number) => {
  formData.value.accountBookId = bookId;
  showAccountBookPicker.value = false;
};

// 表单验证
const validate = () => {
  if (!formData.value.amount || formData.value.amount <= 0) {
    showError(t("mobile.record.validateAmount"));
    return false;
  }
  if (!formData.value.accountBookId) {
    showError(t("mobile.record.validateAccountBook"));
    return false;
  }
  if (!formData.value.mainClassifyId) {
    showError(t("mobile.record.validateClassify"));
    return false;
  }
  if (!formData.value.date) {
    showError(t("mobile.record.validateDate"));
    return false;
  }
  return true;
};

// 保存
const handleSave = async () => {
  if (!validate() || !userId.value) return;

  saving.value = true;
  showLoading(t("mobile.common.loading"));

  try {
    const params = {
      accountBookId: formData.value.accountBookId!,
      amount: formData.value.amount,
      type: formData.value.type,
      date: formData.value.date,
      remark: formData.value.remark || "",
      mainClassify: formData.value.mainClassifyId!,
      subClassify: formData.value.subClassifyId || undefined,
      isCreditCard: formData.value.isCreditCard ? "YES" : "NO",
      isAddRemark: formData.value.addToRemark ? "YES" : "NO",
      tagCodes: getTagCodesByIds(formData.value.tagIds)
    };

    if (isEdit.value) {
      await updateIncomeExpense(userId.value, {
        id: parseInt(recordId.value),
        ...params
      });
    } else {
      await createIncomeExpense(userId.value, params);
    }

    hideLoading();

    // 如果勾选了加入常用备注，重新加载备注列表
    if (formData.value.addToRemark) {
      await remarkStore.fetchList(userId.value);
    }

    // 本地更新列表数据，避免返回后全量刷新
    const record = buildLocalRecord();
    if (isEdit.value) {
      billStore.localUpdateRecord(record);
    } else {
      billStore.localAddRecord(record);
    }

    showSuccess(t("mobile.record.saveSuccess"));
    router.back();
  } catch (error: any) {
    hideLoading();
    showError(error?.message || t("mobile.record.saveFailed"));
  } finally {
    saving.value = false;
  }
};

// 加载编辑数据
const loadEditData = async () => {
  if (!isEdit.value) return;

  // 优先使用 billStore 传递的数据（列表页点击时传入）
  const storedData = billStore.editRecordData;
  if (storedData) {
    try {
      // 处理标签ID：从 tagCodes（标签code）转换为标签ID
      let tagIds: number[] = [];
      if (storedData.tagCodes) {
        if (typeof storedData.tagCodes === "string") {
          tagIds = getTagIdsByCodes(storedData.tagCodes);
        } else if (Array.isArray(storedData.tagCodes)) {
          tagIds = getTagIdsByCodes(storedData.tagCodes.join(","));
        }
      }

      formData.value = {
        type: storedData.type as "EXPENSE" | "INCOME",
        amount: storedData.amount,
        accountBookId: storedData.accountBookId,
        mainClassifyId: storedData.mainClassify,
        subClassifyId: storedData.subClassify,
        date: storedData.date,
        remark: storedData.remark || "",
        addToRemark: false,
        tagIds,
        isCreditCard: storedData.isCreditCard === "YES"
      };
      // 使用后清除
      billStore.setEditRecordData(null);
      return;
    } catch (e) {
      console.error("解析存储数据失败:", e);
    }
  }

  // 没有本地数据时调用接口查询
  loading.value = true;
  showLoading(t("mobile.common.loading"));

  try {
    const data = await getIncomeExpenseById(parseInt(recordId.value));

    // 处理标签ID：从 tagCodes（标签code）转换为标签ID
    let tagIds: number[] = [];
    if (data.tagCodes) {
      if (typeof data.tagCodes === "string") {
        tagIds = getTagIdsByCodes(data.tagCodes);
      } else if (Array.isArray(data.tagCodes)) {
        tagIds = getTagIdsByCodes(data.tagCodes.join(","));
      }
    }

    formData.value = {
      type: data.type as "EXPENSE" | "INCOME",
      amount: data.amount,
      accountBookId: data.accountBookId,
      mainClassifyId: data.mainClassify,
      subClassifyId: data.subClassify,
      date: data.date,
      remark: data.remark || "",
      addToRemark: false,
      tagIds,
      isCreditCard: data.isCreditCard === "YES"
    };

    hideLoading();
  } catch (error: any) {
    hideLoading();
    showError(error?.message || t("mobile.common.failed"));
    router.back();
  } finally {
    loading.value = false;
  }
};

// 初始化
const init = async () => {
  if (!userId.value) return;

  // 只获取没有数据的store
  const promises = [];
  if (accountBookStore.list.length === 0) {
    promises.push(accountBookStore.fetchList(userId.value));
  }
  if (userTagStore.list.length === 0) {
    promises.push(userTagStore.fetchList(userId.value));
  }
  if (remarkStore.list.length === 0) {
    promises.push(remarkStore.fetchList(userId.value));
  }
  if (classifyStore.list.length === 0) {
    promises.push(classifyStore.fetchList(userId.value));
  }
  if (promises.length > 0) {
    await Promise.all(promises);
  }

  // 设置默认账本
  if (!formData.value.accountBookId) {
    formData.value.accountBookId = accountBookStore.defaultAccountBookId;
  }

  // 如果是编辑模式，加载编辑数据
  if (isEdit.value) {
    await loadEditData();
  }
};

onMounted(() => {
  init();
});
</script>

<template>
  <div class="record-page">
    <!-- 类型切换 -->
    <div class="type-switch">
      <div
        class="type-btn expense"
        :class="{ active: formData.type === 'EXPENSE' }"
        @click.stop="
          formData.type = 'EXPENSE';
          handleTypeChange();
        "
      >
        {{ t("mobile.record.expense") }}
      </div>
      <div
        class="type-btn income"
        :class="{ active: formData.type === 'INCOME' }"
        @click.stop="
          formData.type = 'INCOME';
          handleTypeChange();
        "
      >
        {{ t("mobile.record.income") }}
      </div>
    </div>

    <!-- 金额输入 -->
    <AmountInput v-model="formData.amount" :type="formData.type" @click.stop />

    <!-- 表单区域 -->
    <van-cell-group inset class="form-group">
      <!-- 账本选择 -->
      <van-cell
        :title="t('mobile.record.accountBook')"
        :value="
          selectedAccountBookName || t('mobile.record.accountBookPlaceholder')
        "
        is-link
        @click.stop="showAccountBookPicker = true"
      />

      <!-- 分类选择 -->
      <van-cell
        :title="t('mobile.record.classify')"
        :value="selectedClassifyName || t('mobile.record.classifyPlaceholder')"
        is-link
        @click.stop="showClassifyPicker = true"
      />

      <!-- 日期选择 -->
      <van-cell
        :title="t('mobile.record.date')"
        :value="formData.date"
        is-link
        @click.stop="showDatePicker = true"
      />

      <!-- 备注选择 -->
      <van-cell
        :title="t('mobile.record.remark')"
        :value="formData.remark || t('mobile.record.remarkPlaceholder')"
        is-link
        @click.stop="showRemarkPicker = true"
      />

      <!-- 加入常用备注 -->
      <van-cell center :title="t('mobile.record.addToRemark')">
        <template #right-icon>
          <van-switch
            v-model="formData.addToRemark"
            size="20"
            active-color="#ee0a24"
          />
        </template>
      </van-cell>

      <!-- 标签选择 -->
      <van-cell
        :title="t('mobile.record.tag')"
        is-link
        @click.stop="showTagPicker = true"
      >
        <template #value>
          <div v-if="selectedTags.length > 0" class="selected-tags-cell">
            <van-tag
              v-for="tag in selectedTags"
              :key="tag.id"
              :color="tag.color"
              text-color="#fff"
            >
              {{ tag.name }}
            </van-tag>
          </div>
          <span v-else class="placeholder">{{
            t("mobile.record.tagPlaceholder")
          }}</span>
        </template>
      </van-cell>

      <!-- 信用卡消费（仅支出） -->
      <van-cell
        v-if="formData.type === 'EXPENSE'"
        center
        :title="t('mobile.record.isCreditCard')"
      >
        <template #right-icon>
          <van-switch
            v-model="formData.isCreditCard"
            size="20"
            active-color="#ee0a24"
          />
        </template>
      </van-cell>
    </van-cell-group>

    <!-- 保存按钮 -->
    <div class="save-btn-wrapper">
      <van-button
        type="danger"
        block
        round
        :loading="saving"
        @click.stop="handleSave"
      >
        {{ t("mobile.record.save") }}
      </van-button>
    </div>

    <!-- 分类选择器 -->
    <ClassifyPicker
      v-model="showClassifyPicker"
      :classify-list="classifyList"
      :type="formData.type"
      :main-classify-id="formData.mainClassifyId"
      :sub-classify-id="formData.subClassifyId"
      @select="handleClassifySelect"
    />

    <!-- 标签选择器 -->
    <TagPicker
      v-model="showTagPicker"
      :tag-list="tagList"
      :selected-ids="formData.tagIds"
      @update:selected-ids="formData.tagIds = $event"
    />

    <!-- 备注选择器 -->
    <RemarkPicker
      v-model="showRemarkPicker"
      :remark-list="remarkList"
      :current-remark="formData.remark"
      @select="handleRemarkSelect"
    />

    <!-- 日期选择器 -->
    <van-calendar
      v-model:show="showDatePicker"
      :default-date="selectedDate"
      :min-date="new Date(2020, 0, 1)"
      :max-date="new Date()"
      :show-confirm="false"
      position="bottom"
      round
      teleport="body"
      @select="handleDateSelect"
    />

    <!-- 账本选择器 -->
    <van-action-sheet
      v-model:show="showAccountBookPicker"
      :title="t('mobile.home.selectAccountBook')"
    >
      <div class="account-book-list">
        <van-cell
          v-for="book in accountBookList"
          :key="book.id"
          clickable
          @click="handleSelectAccountBook(book.id)"
        >
          <template #title>
            <div class="book-item">
              <span class="book-name">{{ book.name }}</span>
              <span v-if="book.isDefault === 'YES'" class="default-tag"
                >默认</span
              >
            </div>
          </template>
        </van-cell>
      </div>
    </van-action-sheet>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.record-page {
  min-height: 100vh;
  padding-bottom: calc(80px + env(safe-area-inset-bottom));
  background-color: $color-background;
}

.type-switch {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background-color: $color-card;

  .type-btn {
    flex: 1;
    padding: 10px 0;
    font-size: 14px;
    font-weight: 500;
    text-align: center;
    cursor: pointer;
    border: 1px solid $color-border;
    border-radius: 8px;
    transition: all 0.2s;

    &.expense {
      color: $color-text-secondary;

      &.active {
        color: #fff;
        background-color: $color-primary;
        border-color: $color-primary;
      }
    }

    &.income {
      color: $color-text-secondary;

      &.active {
        color: #fff;
        background-color: $color-secondary;
        border-color: $color-secondary;
      }
    }
  }
}

.form-group {
  margin: 12px 16px;
}

.save-btn-wrapper {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 10;
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
}

.selected-tags-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: flex-end;
}

.placeholder {
  color: $color-text-secondary;
}

.account-book-list {
  max-height: 300px;
  padding-bottom: env(safe-area-inset-bottom);
  overflow-y: auto;
}

.book-item {
  display: flex;
  gap: 8px;
  align-items: center;
}

.default-tag {
  font-size: 12px;
  color: #00a151;
}
</style>
