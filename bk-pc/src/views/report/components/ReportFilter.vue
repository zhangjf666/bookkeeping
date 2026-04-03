<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useBillStoreHook } from "@/store/modules/bill";
import { getAccountBookIcon } from "@/utils/accountBook";
import { getClassifyIcon } from "@/utils/classifyIcons";
import dayjs from "dayjs";

defineOptions({
  name: "ReportFilter"
});

interface Props {
  accountBookId?: number;
  billType?: "month" | "year" | "custom";
  month?: string;
  year?: string;
  dateRange?: [string, string] | null;
  classifyList?: (number | string)[];
  remark?: string;
  tagCodes?: number[];
}

interface Emits {
  "update:accountBookId": [value: number | undefined];
  "update:billType": [value: "month" | "year" | "custom"];
  "update:month": [value: string];
  "update:year": [value: string];
  "update:dateRange": [value: [string, string] | null | undefined];
  "update:classifyList": [value: (number | string)[]];
  "update:remark": [value: string];
  "update:tagCodes": [value: number[]];
  query: [];
  reset: [];
}

const props = withDefaults(defineProps<Props>(), {
  billType: "month",
  month: () => dayjs().format("YYYY-MM"),
  year: () => dayjs().format("YYYY"),
  dateRange: null,
  classifyList: () => [],
  remark: "",
  tagCodes: () => []
});

const emit = defineEmits<Emits>();

const { t } = useI18n();
const billStore = useBillStoreHook();

const localAccountBookId = computed({
  get: () => props.accountBookId,
  set: (val) => emit("update:accountBookId", val)
});

const localBillType = computed({
  get: () => props.billType,
  set: (val) => emit("update:billType", val)
});

const localMonth = computed({
  get: () => props.month,
  set: (val) => emit("update:month", val)
});

const localYear = computed({
  get: () => props.year,
  set: (val) => emit("update:year", val)
});

const localDateRange = computed({
  get: () => props.dateRange,
  set: (val) => emit("update:dateRange", val ?? undefined)
});

const localClassifyList = computed({
  get: () => props.classifyList,
  set: (val) => emit("update:classifyList", val)
});

const localRemark = computed({
  get: () => props.remark,
  set: (val) => emit("update:remark", val)
});

const localTagCodes = computed({
  get: () => props.tagCodes,
  set: (val) => emit("update:tagCodes", val)
});

const filterRemarkText = ref("");
const filterTagText = ref("");

const filteredRemarkList = computed(() => {
  if (!filterRemarkText.value) return billStore.remarkList;
  return billStore.remarkList.filter(remark =>
    remark.remark.toLowerCase().includes(filterRemarkText.value.toLowerCase())
  );
});

const filteredTagList = computed(() => {
  if (!filterTagText.value) return billStore.tagList;
  return billStore.tagList.filter(tag =>
    tag.name.toLowerCase().includes(filterTagText.value.toLowerCase())
  );
});

const getFilterTagName = (tagId: number) => {
  const tag = billStore.tagList.find(t => t.id === tagId);
  return tag ? tag.name : "";
};

const getFilterTagColor = (tagId: number) => {
  const tag = billStore.tagList.find(t => t.id === tagId);
  return tag ? tag.color : "#409eff";
};

const toggleFilterTag = (tagId: number) => {
  const index = localTagCodes.value.indexOf(tagId);
  if (index === -1) {
    emit("update:tagCodes", [...localTagCodes.value, tagId]);
  } else {
    emit("update:tagCodes", localTagCodes.value.filter(id => id !== tagId));
  }
};

const removeFilterTag = (tagId: number) => {
  emit("update:tagCodes", localTagCodes.value.filter(id => id !== tagId));
};

const selectFilterRemark = (remark: string) => {
  emit("update:remark", remark);
};

const accountBookOptions = computed(() => {
  if (billStore.accountBooks.length > 0) {
    return billStore.accountBooks;
  }
  if (billStore.currentAccountBook) {
    return [billStore.currentAccountBook];
  }
  return [];
});

const treeProps = {
  children: "children",
  label: "name"
};

const classifyTreeRef = ref();
const selectAllExpense = ref(false);
const selectAllIncome = ref(false);
const isIndeterminateExpense = ref(false);
const isIndeterminateIncome = ref(false);

const classifyTreeData = computed(() => {
  const expenseParents = billStore.classifyList.filter(
    (c: any) => c.pid === -1 && c.type === "EXPENSE"
  );
  const incomeParents = billStore.classifyList.filter(
    (c: any) => c.pid === -1 && c.type === "INCOME"
  );

  const buildTree = (parents: any[]) => {
    return parents.map(parent => {
      const children = billStore.classifyList.filter(
        (c: any) => c.pid === parent.id
      );
      const node: any = {
        id: parent.id,
        name: parent.name,
        image: parent.image,
        type: parent.type,
        isParent: true
      };
      if (children.length > 0) {
        node.children = children.map(child => ({
          id: child.id,
          name: child.name,
          image: child.image,
          type: child.type,
          pid: child.pid,
          isParent: false
        }));
      }
      return node;
    });
  };

  return [
    {
      id: "expense-all",
      name: t('bill.pureAllExpense'),
      type: "EXPENSE",
      isAll: true,
      children: buildTree(expenseParents)
    },
    {
      id: "income-all",
      name: t('bill.pureAllIncome'),
      type: "INCOME",
      isAll: true,
      children: buildTree(incomeParents)
    }
  ];
});

const selectedClassifyIds = ref<number[]>([]);

const handleClassifyTreeChange = () => {
  const checkedNodes = classifyTreeRef.value?.getCheckedNodes(false) || [];
  selectedClassifyIds.value = checkedNodes.map((node: any) => node.id);
  updateClassifyListFromTree();
  updateCheckboxState();
};

const updateClassifyListFromTree = () => {
  const checkedNodes = classifyTreeRef.value?.getCheckedNodes(false) || [];
  const result: { mainClassifyId: number; subClassifyId: number | null }[] = [];
  
  const expenseParentIds = billStore.classifyList
    .filter((c: any) => c.pid === -1 && c.type === "EXPENSE")
    .map((c: any) => c.id);
  
  const incomeParentIds = billStore.classifyList
    .filter((c: any) => c.pid === -1 && c.type === "INCOME")
    .map((c: any) => c.id);

  checkedNodes.forEach((node: any) => {
    if (node.isAll) return;
    
    if (expenseParentIds.includes(node.id) || incomeParentIds.includes(node.id)) {
      result.push({ mainClassifyId: node.id, subClassifyId: null as any });
    } else {
      result.push({ mainClassifyId: node.pid, subClassifyId: node.id });
    }
  });

  emit("update:classifyList", result);
};

const updateCheckboxState = () => {
  const checkedKeys = classifyTreeRef.value?.getCheckedKeys() || [];
  
  const expenseParentIds = billStore.classifyList
    .filter((c: any) => c.pid === -1 && c.type === "EXPENSE")
    .map((c: any) => c.id);
  
  const incomeParentIds = billStore.classifyList
    .filter((c: any) => c.pid === -1 && c.type === "INCOME")
    .map((c: any) => c.id);

  const expenseKeys = checkedKeys.filter((id: any) => expenseParentIds.includes(id));
  const incomeKeys = checkedKeys.filter((id: any) => incomeParentIds.includes(id));

  const allExpenseChildren = billStore.classifyList
    .filter((c: any) => c.pid === -1 && c.type === "EXPENSE")
    .flatMap((p: any) => [
      p.id,
      ...billStore.classifyList.filter((c: any) => c.pid === p.id).map((c: any) => c.id)
    ]);
  
  const allIncomeChildren = billStore.classifyList
    .filter((c: any) => c.pid === -1 && c.type === "INCOME")
    .flatMap((p: any) => [
      p.id,
      ...billStore.classifyList.filter((c: any) => c.pid === p.id).map((c: any) => c.id)
    ]);

  const checkedExpenseKeys = checkedKeys.filter((id: any) => allExpenseChildren.includes(id));
  const checkedIncomeKeys = checkedKeys.filter((id: any) => allIncomeChildren.includes(id));

  selectAllExpense.value = allExpenseChildren.length > 0 && checkedExpenseKeys.length === allExpenseChildren.length;
  isIndeterminateExpense.value = checkedExpenseKeys.length > 0 && checkedExpenseKeys.length < allExpenseChildren.length;

  selectAllIncome.value = allIncomeChildren.length > 0 && checkedIncomeKeys.length === allIncomeChildren.length;
  isIndeterminateIncome.value = checkedIncomeKeys.length > 0 && checkedIncomeKeys.length < allIncomeChildren.length;
};

const handleSelectAllExpense = (checked: boolean) => {
  const expenseNodes = classifyTreeRef.value?.getNode("expense-all");
  if (expenseNodes) {
    if (checked) {
      const allKeys = expenseNodes.childNodes.flatMap((node: any) => {
        const keys = [node.data.id];
        if (node.childNodes) {
          keys.push(...node.childNodes.map((child: any) => child.data.id));
        }
        return keys;
      });
      classifyTreeRef.value?.setCheckedKeys(allKeys);
    } else {
      const currentKeys = classifyTreeRef.value?.getCheckedKeys() || [];
      const expenseKeys = billStore.classifyList
        .filter((c: any) => c.pid === -1 && c.type === "EXPENSE")
        .flatMap((p: any) => [
          p.id,
          ...billStore.classifyList.filter((c: any) => c.pid === p.id).map((c: any) => c.id)
        ]);
      classifyTreeRef.value?.setCheckedKeys(currentKeys.filter((k: any) => !expenseKeys.includes(k)));
    }
    handleClassifyTreeChange();
  }
};

const handleSelectAllIncome = (checked: boolean) => {
  const incomeNodes = classifyTreeRef.value?.getNode("income-all");
  if (incomeNodes) {
    if (checked) {
      const allKeys = incomeNodes.childNodes.flatMap((node: any) => {
        const keys = [node.data.id];
        if (node.childNodes) {
          keys.push(...node.childNodes.map((child: any) => child.data.id));
        }
        return keys;
      });
      const currentKeys = classifyTreeRef.value?.getCheckedKeys() || [];
      classifyTreeRef.value?.setCheckedKeys([...currentKeys, ...allKeys]);
    } else {
      const currentKeys = classifyTreeRef.value?.getCheckedKeys() || [];
      const incomeKeys = billStore.classifyList
        .filter((c: any) => c.pid === -1 && c.type === "INCOME")
        .flatMap((p: any) => [
          p.id,
          ...billStore.classifyList.filter((c: any) => c.pid === p.id).map((c: any) => c.id)
        ]);
      classifyTreeRef.value?.setCheckedKeys(currentKeys.filter((k: any) => !incomeKeys.includes(k)));
    }
    handleClassifyTreeChange();
  }
};

const prevHasAllExpense = ref(false);
const prevHasAllIncome = ref(false);

const billTypeOptions = [
  { label: t('bill.pureMonthBill'), value: "month" },
  { label: t('bill.pureYearBill'), value: "year" },
  { label: t('bill.pureCustom'), value: "custom" }
];

const handleQuery = () => {
  emit("query");
};

const handleReset = () => {
  localBillType.value = "month";
  localMonth.value = dayjs().format("YYYY-MM");
  localYear.value = dayjs().format("YYYY");
  localDateRange.value = null;
  localClassifyList.value = [];
  localRemark.value = "";
  localTagCodes.value = [];
  selectAllExpense.value = false;
  selectAllIncome.value = false;
  isIndeterminateExpense.value = false;
  isIndeterminateIncome.value = false;
  classifyTreeRef.value?.setCheckedKeys([]);
  emit("reset");
};
</script>

<template>
  <el-card class="report-filter" shadow="never">
    <div class="filter-row">
      <el-form inline>
        <el-form-item :label="t('bill.pureAccountBook')">
          <el-select
            v-model="localAccountBookId"
            :placeholder="t('bill.pureSelectPlaceholder')"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="book in accountBookOptions"
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
        <el-form-item :label="t('bill.pureBillType')">
          <el-select v-model="localBillType" style="width: 100px">
            <el-option
              v-for="item in billTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('bill.pureTimeSelect')">
          <el-date-picker
            v-if="localBillType === 'month'"
            v-model="localMonth"
            type="month"
            value-format="YYYY-MM"
            :placeholder="t('bill.pureMonth')"
            style="width: 140px"
          />
          <el-date-picker
            v-else-if="localBillType === 'year'"
            v-model="localYear"
            type="year"
            value-format="YYYY"
            :placeholder="t('dashboard.pureYear')"
            style="width: 100px"
          />
          <el-date-picker
            v-else
            v-model="localDateRange"
            type="daterange"
            range-separator="-"
            :start-placeholder="t('bill.pureStartDate')"
            :end-placeholder="t('bill.pureEndDate')"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item :label="t('bill.pureClassify')">
          <div class="filter-classify-wrapper">
            <el-popover placement="bottom-start" :width="320" trigger="click">
              <template #reference>
                <div class="classify-trigger">
                <span v-if="!localClassifyList || localClassifyList.length === 0" class="placeholder">
                  {{ t("bill.pureSelectPlaceholder") }}
                </span>
                <span v-else>{{ t('bill.pureSelectedClassify', { count: localClassifyList.length }) }}</span>
              </div>
            </template>
            <div class="classify-popover">
              <el-tree
                ref="classifyTreeRef"
                :data="classifyTreeData"
                :props="treeProps"
                show-checkbox
                node-key="id"
                :default-expand-all="false"
                @check="handleClassifyTreeChange"
              >
                <template #default="{ node, data }">
                  <span class="custom-tree-node">
                    <span v-if="data.isAll">{{ getClassifyIcon('other') }} {{ node.label }}</span>
                    <span v-else>{{ getClassifyIcon(data.image) }} {{ node.label }}</span>
                  </span>
                </template>
              </el-tree>
            </div>
            </el-popover>
          </div>
        </el-form-item>
      </el-form>
    </div>
    <div class="filter-row">
      <el-form inline>
        <el-form-item :label="t('bill.pureRemark')">
          <div class="filter-remark-wrapper">
            <el-popover placement="bottom-start" :width="400" trigger="click">
              <template #reference>
                <el-input
                  v-model="localRemark"
                  :placeholder="t('bill.pureRemarkPlaceholder')"
                  class="filter-remark-trigger"
                  clearable
                />
              </template>
              <div class="filter-remark-content">
                <el-input
                  v-model="filterRemarkText"
                  :placeholder="t('bill.pureSearchRemark')"
                  clearable
                  class="filter-remark-search"
                />
                <div class="filter-remark-grid">
                  <div
                    v-for="remark in filteredRemarkList"
                    :key="remark.id"
                    class="filter-remark-item"
                    @click="selectFilterRemark(remark.remark)"
                  >
                    {{ remark.remark }}
                  </div>
                </div>
              </div>
            </el-popover>
          </div>
        </el-form-item>
        <el-form-item :label="t('bill.pureTag')">
          <div class="filter-tag-wrapper">
            <el-popover placement="bottom-start" :width="400" trigger="click">
              <template #reference>
                <div class="filter-tag-trigger">
                <span
                  v-if="localTagCodes.length === 0"
                  class="placeholder"
                >
                  {{ t("bill.pureSelectPlaceholder") }}
                </span>
                <span v-else class="selected-tags">
                  <el-tag
                    v-for="tagId in localTagCodes"
                    :key="tagId"
                    :color="getFilterTagColor(tagId)"
                    :style="{ color: '#fff' }"
                    size="small"
                    closable
                    @close="removeFilterTag(tagId)"
                  >
                    {{ getFilterTagName(tagId) }}
                  </el-tag>
                </span>
              </div>
            </template>
            <div class="filter-tag-content">
              <el-input
                v-model="filterTagText"
                :placeholder="t('bill.pureQuery')"
                clearable
                class="filter-tag-search"
              />
              <div class="filter-tag-grid">
                <div
                  v-for="tag in filteredTagList"
                  :key="tag.id"
                  class="filter-tag-item"
                  :class="{ active: localTagCodes.includes(tag.id) }"
                  @click="toggleFilterTag(tag.id)"
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
          </el-popover>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            {{ t("bill.pureQuery") }}
          </el-button>
          <el-button @click="handleReset">
            {{ t("bill.pureReset") }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </el-card>
</template>

<style lang="scss" scoped>
.report-filter {
  margin-bottom: 16px;
}

.filter-row {
  &:not(:last-child) {
    margin-bottom: 12px;
  }
}

.book-option {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: space-between;
}

.classify-all-option {
  &.expense {
    font-weight: 600;
    color: #f56c6c !important;
  }

  &.income {
    font-weight: 600;
    color: #67c23a !important;
  }
}

:deep(.el-select-dropdown__item) {
  display: flex;
  align-items: center;

  .option-icon {
    margin-right: 8px;
    font-size: 16px;
  }
}

.filter-tag-trigger {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  width: 360px;
  min-height: 32px;
  padding: 0 8px;
  cursor: pointer;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;

  .placeholder {
    color: #999;
  }

  &:hover {
    border-color: #409eff;
  }

  .selected-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    align-items: center;
  }

  :deep(.el-tag) {
    margin: 0;

    .el-tag__close {
      color: #fff;
      background-color: rgb(0 0 0 / 30%);

      &:hover {
        background-color: rgb(0 0 0 / 60%);
      }
    }
  }
}

.filter-remark-search,
.filter-tag-search {
  margin-bottom: 12px;
}

.filter-remark-grid,
.filter-tag-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.filter-remark-item,
.filter-tag-item {
  padding: 4px 8px;
  text-align: center;
  border-radius: 4px;
  cursor: pointer;

  &:hover {
    background-color: #f5f7fa;
  }

  &.active {
    background-color: #ecf5ff;
  }
}

.filter-remark-trigger {
  flex: 1;
}

.filter-remark-wrapper,
.filter-tag-wrapper,
.filter-classify-wrapper {
  display: flex;
  flex: 1;
}

.classify-trigger {
  display: flex;
  align-items: center;
  width: 100%;
  min-width: 200px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  box-sizing: border-box;

  .placeholder {
    color: #999;
  }

  &:hover {
    border-color: #409eff;
  }
}

.classify-popover {
  width: 100%;
  max-height: 300px;
  overflow-y: auto;

  .classify-header {
    display: flex;
    gap: 16px;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #eee;
  }
}

.custom-tree-node {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>