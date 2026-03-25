<script setup lang="ts">
import { ref, watch, computed } from "vue";
import { useI18n } from "vue-i18n";
import { message } from "@/utils/message";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import type { IncomeExpense, IncomeExpenseForm } from "@/types/bill";
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

const formData = ref<IncomeExpenseForm>({
  id: undefined,
  accountBookId: billStore.currentAccountBook?.id || 0,
  amount: 0,
  type: "EXPENSE",
  date: dayjs().format("YYYY-MM-DD"),
  remark: "",
  mainClassify: 0,
  subClassify: undefined,
  isCreditCard: "NO",
  isAddRemark: "NO",
  tagCodes: ""
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

const selectedClassifyId = ref<number | undefined>(undefined);

const selectedTags = ref<number[]>([]);

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

const getTagColor = (tagId: number) => {
  const tag = billStore.tagList.find(t => t.id === tagId);
  return tag ? tag.color : "";
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
      formData.value = {
        id: undefined,
        accountBookId: billStore.currentAccountBook?.id || 0,
        amount: 0,
        type: "EXPENSE",
        date: dayjs().format("YYYY-MM-DD"),
        remark: "",
        mainClassify: 0,
        subClassify: undefined,
        isCreditCard: "NO",
        isAddRemark: "NO",
        tagCodes: ""
      };
      selectedClassifyId.value = undefined;
      selectedTags.value = [];
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
    formData.value.mainClassify = 0;
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

const handleSubmit = async () => {
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
          :label="book.name"
          :value="book.id"
        />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('bill.pureClassify')" prop="mainClassify">
      <el-tree-select
        v-model="selectedClassifyId"
        :data="classifyTreeData"
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
      <el-autocomplete
        v-model="formData.remark"
        :fetch-suggestions="queryFormRemarks"
        :placeholder="t('bill.pureRemarkPlaceholder')"
        clearable
        style="width: 100%"
        @select="handleRemarkSelect"
      >
        <template #append>
          <el-checkbox
            v-model="formData.isAddRemark"
            true-value="YES"
            false-value="NO"
          >
            {{ t("bill.pureAddToCommon") }}
          </el-checkbox>
        </template>
      </el-autocomplete>
    </el-form-item>
    <el-form-item :label="t('bill.pureTag')">
      <el-select
        v-model="selectedTags"
        multiple
        filterable
        :placeholder="t('bill.pureSelectPlaceholder')"
        style="width: 100%"
      >
        <el-option
          v-for="tag in billStore.tagList"
          :key="tag.id"
          :label="tag.name"
          :value="tag.id"
        >
          <span :style="{ color: tag.color }">{{ tag.name }}</span>
        </el-option>
      </el-select>
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
</template>
