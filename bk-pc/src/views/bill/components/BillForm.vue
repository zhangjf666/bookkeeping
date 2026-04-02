<script setup lang="ts">
import { ref, watch, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { message } from "@/utils/message";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import type { IncomeExpense, IncomeExpenseForm } from "@/types/bill";
import { getAccountBookIcon } from "@/utils/accountBook";
import { getClassifyIcon } from "@/utils/classifyIcons";
import TagForm from "@/views/settings/tag/TagForm.vue";
import dayjs from "dayjs";

defineOptions({
  name: "BillForm"
});

interface Props {
  data: IncomeExpense | null;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  success: [];
  cancel: [];
}>();

const { t } = useI18n();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

const userId = computed(() => userStore.id);

const formRef = ref();
const loading = ref(false);
const showTagDialog = ref(false);
const selectedTags = ref<number[]>([]);
const tagSearchText = ref("");
const remarkSearchText = ref("");

const filteredTagList = computed(() => {
  if (!tagSearchText.value) return billStore.tagList;
  return billStore.tagList.filter(tag =>
    tag.name.toLowerCase().includes(tagSearchText.value.toLowerCase())
  );
});

const filteredRemarkList = computed(() => {
  if (!remarkSearchText.value) return billStore.remarkList;
  return billStore.remarkList.filter(remark =>
    remark.remark.toLowerCase().includes(remarkSearchText.value.toLowerCase())
  );
});

const getTagColor = (tagId: number) => {
  const tag = billStore.tagList.find(t => t.id === tagId);
  return tag ? tag.color : "";
};

const toggleTag = (tagId: number) => {
  const index = selectedTags.value.indexOf(tagId);
  if (index === -1) {
    selectedTags.value = [...selectedTags.value, tagId];
  } else {
    selectedTags.value = selectedTags.value.filter(id => id !== tagId);
  }
};

const removeTag = (tagId: number) => {
  selectedTags.value = selectedTags.value.filter(id => id !== tagId);
};

const selectRemark = (remark: string) => {
  formData.value.remark = remark;
  handleRemarkAutoSelect(remark);
};

const handleRemarkAutoSelect = (remarkValue: string) => {
  const remarkItem = billStore.remarkList.find(r => r.remark === remarkValue);
  if (!remarkItem || !remarkItem.classifyId) return;

  const classify = billStore.classifyList.find(
    c => c.id === remarkItem.classifyId
  );
  if (!classify) return;

  if (classify.type) {
    formData.value.type = String(classify.type);
  }

  if (classify.pid === -1) {
    selectedClassifyId.value = classify.id;
  } else {
    const parentClassify = billStore.classifyList.find(
      c => c.id === classify.pid
    );
    if (parentClassify) {
      selectedClassifyId.value = parentClassify.id;
      formData.value.subClassify = classify.id;
    }
  }
};

const formData = ref<IncomeExpenseForm>({
  id: undefined,
  accountBookId: billStore.currentAccountBook?.id || 0,
  amount: 0,
  type: "EXPENSE",
  date: dayjs().format("YYYY-MM-DD"),
  remark: "",
  mainClassify: undefined,
  subClassify: undefined,
  isCreditCard: "NO",
  isAddRemark: "NO",
  tagCodes: ""
});

const initCreditCardFromConfig = () => {
  const config = billStore.userConfigList.find(c => c.name === "is_credit_card");
  formData.value.isCreditCard = config?.value === "1" ? "YES" : "NO";
};

onMounted(() => {
  initCreditCardFromConfig();
});

const rules = {
  accountBookId: [
    {
      required: true,
      message: t("bill.pureAccountBookRequired"),
      trigger: "change"
    }
  ],
  amount: [
    { required: true, message: t("bill.pureAmountRequired"), trigger: "blur" },
    {
      type: "number" as const,
      min: 0.01,
      message: t("bill.pureAmountInvalid"),
      trigger: "blur"
    }
  ],
  type: [
    { required: true, message: t("bill.pureTypeRequired"), trigger: "change" }
  ],
  mainClassify: [
    {
      required: true,
      message: t("bill.pureClassifyRequired"),
      trigger: "change"
    }
  ],
  date: [
    { required: true, message: t("bill.pureDateRequired"), trigger: "change" }
  ]
};

const typeOptions = [
  { label: t("bill.pureExpense"), value: 0 },
  { label: t("bill.pureIncome"), value: 1 }
];

const classifyTreeData = computed(() => {
  const type = formData.value.type || "EXPENSE";
  return billStore.classifyTree.filter((c: any) => c.type === type);
});

const classifyTreeDataWithIcon = computed(() => {
  const transformNode = (node: any): any => {
    return {
      ...node,
      name: `${getClassifyIcon(node.image)} ${node.name}`,
      children: node.children?.map(transformNode)
    };
  };
  return classifyTreeData.value.map(transformNode);
});

const selectedClassifyId = ref<number | undefined>(undefined);

const getTagCodesByIds = (tagIds: number[]) => {
  return tagIds
    .map(id => {
      const tag = billStore.tagList.find(t => t.id === id);
      return tag ? (tag as any).code : null;
    })
    .filter(code => code !== null)
    .join(",");
};

const getTagIdsByCodes = (tagCodesStr: string) => {
  if (!tagCodesStr) return [];
  const codes = tagCodesStr.split(",").map(c => Number(c.trim()));
  return billStore.tagList
    .filter(tag => codes.includes((tag as any).code))
    .map(tag => tag.id);
};

const getTagName = (tagId: number) => {
  const tag = billStore.tagList.find(t => t.id === tagId);
  return tag ? tag.name : "";
};

const handleRemarkSelect = (value: any) => {
  if (!value || !value.value) return;
  const remarkValue = value.value;
  const remarkItem = billStore.remarkList.find(r => r.remark === remarkValue);
  if (!remarkItem || !remarkItem.classifyId) return;

  const classify = billStore.classifyList.find(
    c => c.id === remarkItem.classifyId
  );
  if (!classify) return;

  if (classify.type) {
    formData.value.type = String(classify.type);
  }

  if (classify.pid === -1) {
    selectedClassifyId.value = classify.id;
    formData.value.mainClassify = classify.id;
    formData.value.subClassify = undefined;
  } else {
    selectedClassifyId.value = classify.id;
    formData.value.mainClassify = classify.pid;
    formData.value.subClassify = classify.id;
  }
};

const queryFormRemarks = (
  queryString: string,
  cb: (results: { value: string }[]) => void
) => {
  const results = queryString
    ? billStore.remarkList.filter(item =>
        item.remark.toLowerCase().includes(queryString.toLowerCase())
      )
    : billStore.remarkList;
  cb(results.map(item => ({ value: item.remark })));
};

const resetForm = () => {
  formData.value = {
    id: undefined,
    accountBookId: billStore.currentAccountBook?.id || 0,
    amount: 0,
    type: "EXPENSE",
    date: dayjs().format("YYYY-MM-DD"),
    remark: "",
    mainClassify: undefined,
    subClassify: undefined,
    isCreditCard: "NO",
    isAddRemark: "NO",
    tagCodes: ""
  };
  initCreditCardFromConfig();
  selectedClassifyId.value = undefined;
  selectedTags.value = [];
  formRef.value?.resetFields();
};

watch(
  () => props.data,
  val => {
    if (val) {
      const isCreditCardVal =
        String(val.isCreditCard) === "1" || String(val.isCreditCard) === "YES";
      const isAddRemarkVal =
        String(val.isAddRemark) === "1" || String(val.isAddRemark) === "YES";
      formData.value = {
        id: val.id,
        accountBookId: val.accountBookId,
        amount: val.amount,
        type: String(val.type),
        date: val.date,
        remark: val.remark,
        mainClassify: val.mainClassify,
        subClassify: val.subClassify || undefined,
        isCreditCard: isCreditCardVal ? "YES" : "NO",
        isAddRemark: isAddRemarkVal ? "YES" : "NO",
        tagCodes: val.tagCodes
      };
      if (val.subClassify) {
        selectedClassifyId.value = val.subClassify;
      } else if (val.mainClassify) {
        selectedClassifyId.value = val.mainClassify;
      } else {
        selectedClassifyId.value = undefined;
      }
      selectedTags.value = getTagIdsByCodes(val.tagCodes);
    } else {
      resetForm();
    }
  },
  { immediate: true }
);

watch(selectedClassifyId, val => {
  if (val) {
    const classify = billStore.classifyList.find(c => c.id === val);
    if (classify) {
      if (classify.pid === -1) {
        formData.value.mainClassify = val;
        formData.value.subClassify = undefined;
      } else {
        formData.value.mainClassify = classify.pid;
        formData.value.subClassify = val;
      }
    }
  } else {
    formData.value.mainClassify = undefined;
    formData.value.subClassify = undefined;
  }
});

watch(
  selectedTags,
  val => {
    formData.value.tagCodes = getTagCodesByIds(val);
  },
  { deep: true }
);

const handleTagCreated = (tagId: number) => {
  if (tagId && !selectedTags.value.includes(tagId)) {
    selectedTags.value = [...selectedTags.value, tagId];
  }
};

const handleSubmit = async () => {
  if (!selectedClassifyId.value) {
    message(t("bill.pureClassifyRequired"), { type: "warning" });
    return;
  }
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  if (!userId.value) return;

  loading.value = true;
  try {
    if (formData.value.id) {
      await billStore.update(userId.value, formData.value);
      message(t("bill.pureUpdateSuccess"), { type: "success" });
    } else {
      await billStore.create(userId.value, formData.value);
      message(t("bill.pureCreateSuccess"), { type: "success" });
    }
    emit("success");
  } catch (error: any) {
    message(error?.message || t("bill.pureOperationFail"), { type: "error" });
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
    <el-form-item :label="t('bill.pureAmount')" prop="amount">
      <el-input-number
        v-model="formData.amount"
        :min="0"
        :precision="2"
        controls-position="right"
        style="width: 100%"
      />
    </el-form-item>
    <el-form-item :label="t('bill.pureType')" prop="type">
      <el-radio-group v-model="formData.type">
        <el-radio value="EXPENSE">{{ t("bill.pureExpense") }}</el-radio>
        <el-radio value="INCOME">{{ t("bill.pureIncome") }}</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item :label="t('bill.pureAccountBook')" prop="accountBookId">
      <el-select
        v-model="formData.accountBookId"
        :placeholder="t('bill.pureSelectPlaceholder')"
        style="width: 100%"
      >
        <el-option
          v-for="book in billStore.accountBooks"
          :key="book.id"
          :label="`${getAccountBookIcon(book.image)} ${book.name}`"
          :value="book.id"
        >
          <div class="book-option">
            <span>{{ getAccountBookIcon(book.image) }} {{ book.name }}</span>
            <el-tag
              v-if="book.isDefault === 'YES'"
              size="small"
              type="success"
            >
              {{ t("dashboard.pureDefault") }}
            </el-tag>
          </div>
        </el-option>
      </el-select>
    </el-form-item>
    <el-form-item :label="t('bill.pureClassify')" prop="mainClassify">
      <el-tree-select
        v-model="selectedClassifyId"
        :data="classifyTreeDataWithIcon"
        :props="{ label: 'name', value: 'id', children: 'children' }"
        :placeholder="t('bill.pureSelectPlaceholder')"
        check-strictly
        clearable
        filterable
        :render-after-expand="false"
        style="width: 100%"
      />
    </el-form-item>
    <el-form-item :label="t('bill.pureDate')" prop="date">
      <el-date-picker
        v-model="formData.date"
        type="date"
        :placeholder="t('bill.pureSelectPlaceholder')"
        value-format="YYYY-MM-DD"
        style="width: 100%"
      />
    </el-form-item>
    <el-form-item :label="t('bill.pureRemark')">
      <div class="remark-wrapper">
        <el-popover placement="bottom-start" :width="400" trigger="click">
          <template #reference>
            <el-input
              v-model="formData.remark"
              :placeholder="t('bill.pureRemarkPlaceholder')"
              class="remark-dropdown-trigger"
              clearable
            />
          </template>
          <div class="remark-popover-content">
            <el-input
              v-model="remarkSearchText"
              placeholder="搜索备注"
              clearable
              class="remark-search-input"
            />
            <div class="remark-grid-container">
              <div class="remark-grid">
                <div
                  v-for="remark in filteredRemarkList"
                  :key="remark.id"
                  class="remark-item"
                  @click="selectRemark(remark.remark)"
                >
                  {{ remark.remark }}
                </div>
              </div>
            </div>
          </div>
        </el-popover>
        <el-checkbox
          v-model="formData.isAddRemark"
          true-value="YES"
          false-value="NO"
          class="add-remark-checkbox"
        >
          {{ t("bill.pureAddToCommon") }}
        </el-checkbox>
      </div>
    </el-form-item>
    <el-form-item :label="t('bill.pureTag')">
      <div class="tag-select-wrapper">
        <el-popover placement="bottom-start" :width="400" trigger="click">
          <template #reference>
            <div class="tag-dropdown-trigger">
              <span v-if="selectedTags.length === 0" class="placeholder">
                {{ t("bill.pureSelectPlaceholder") }}
              </span>
              <span v-else class="selected-tags">
                <el-tag
                  v-for="tagId in selectedTags"
                  :key="tagId"
                  :color="getTagColor(tagId)"
                  :style="{ color: '#fff' }"
                  size="small"
                  class="selected-tag"
                  closable
                  @close="removeTag(tagId)"
                >
                  {{ getTagName(tagId) }}
                </el-tag>
              </span>
            </div>
          </template>
          <div class="tag-popover-content">
            <el-input
              v-model="tagSearchText"
              :placeholder="t('bill.pureQuery')"
              clearable
              class="tag-search-input"
            />
            <div class="tag-grid-container">
              <div class="tag-grid">
                <div
                  v-for="tag in filteredTagList"
                  :key="tag.id"
                  class="tag-item"
                  :class="{ active: selectedTags.includes(tag.id) }"
                  @click="toggleTag(tag.id)"
                >
                  <el-tag
                    :color="tag.color"
                    :style="{ color: '#fff' }"
                    size="small"
                  >
                    {{ tag.name }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </el-popover>
        <el-button
          type="primary"
          size="default"
          class="add-tag-btn"
          @click="showTagDialog = true"
        >
          +
        </el-button>
      </div>
    </el-form-item>
    <el-form-item v-if="formData.type === 'EXPENSE'">
      <el-checkbox
        v-model="formData.isCreditCard"
        true-value="YES"
        false-value="NO"
      >
        {{ t("bill.pureCreditCard") }}
      </el-checkbox>
    </el-form-item>
    <el-form-item>
      <el-button @click="emit('cancel')">{{ t("bill.pureCancel") }}</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ t("bill.pureConfirm") }}
      </el-button>
    </el-form-item>
  </el-form>

  <TagForm v-model:visible="showTagDialog" @success="handleTagCreated" />
</template>

<style lang="scss" scoped>
.book-option {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: space-between;
}

.remark-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
  width: 100%;

  .remark-input {
    flex: 1;
  }

  .add-remark-checkbox {
    flex-shrink: 0;
    white-space: nowrap;
  }
}

.tag-select-wrapper {
  display: flex;
  gap: 8px;
  align-items: center;
  width: 100%;

  .tag-select {
    flex: 1;
  }

  .add-tag-btn {
    display: flex;
    flex-shrink: 0;
    align-items: center;
    justify-content: center;
    width: 31px;
    height: 31px;
    padding: 0;
  }
}

.tag-option {
  display: inline-block;
  padding: 2px 8px;
  margin: 2px;
  color: #fff;
  border-radius: 10px;
}

.remark-dropdown-trigger,
.tag-dropdown-trigger {
  display: flex;
  flex: 1;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
  min-height: 32px;
  padding: 0 8px;
  cursor: pointer;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;

  .placeholder {
    color: #999;
  }

  &:hover {
    border-color: #409eff;
  }
}

.tag-dropdown-trigger {
  min-height: 32px;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

.selected-tag {
  margin: 0;

  :deep(.el-tag__close) {
    color: #fff;
    background-color: rgb(0 0 0 / 30%);

    &:hover {
      background-color: rgb(0 0 0 / 60%);
    }
  }
}

.remark-popover-content {
  display: flex;
  flex-direction: column;
  width: 100%;

  .remark-search-input {
    flex-shrink: 0;
    margin-bottom: 12px;
  }

  .remark-grid-container {
    max-height: 240px;
    overflow-y: auto;
  }

  .remark-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
    width: 100%;

    .remark-item {
      padding: 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      text-align: center;
      white-space: nowrap;
      cursor: pointer;
      border-radius: 4px;

      &:hover {
        background-color: #f5f7fa;
      }
    }
  }
}

.tag-popover-content {
  display: flex;
  flex-direction: column;
  width: 100%;

  .tag-search-input {
    flex-shrink: 0;
    margin-bottom: 12px;
  }

  .tag-grid-container {
    max-height: 240px;
    overflow-y: auto;
  }

  .tag-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
    width: 100%;

    .tag-item {
      display: flex;
      justify-content: center;
      padding: 4px;
      cursor: pointer;
      border-radius: 4px;

      &:hover {
        background-color: #f5f7fa;
      }

      &.active {
        background-color: #ecf5ff;
      }
    }
  }
}
</style>
