<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
import { useBillStoreHook } from "@/store/modules/bill";
import { getAccountBookIcon } from "@/utils/accountBook";
import { getClassifyIcon } from "@/utils/classifyIcons";
import { exportData } from "@/api/incomeExpense";
import { useUserStoreHook } from "@/store/modules/user";

defineOptions({
  name: "BillFilter"
});

const emit = defineEmits<{
  query: [accountBookId: number | undefined];
  reset: [accountBookId: number | undefined];
}>();

const { t } = useI18n();
const billStore = useBillStoreHook();
const userStore = useUserStoreHook();

const currentAccountBookId = computed(() => billStore.currentAccountBook?.id);

const filterForm = ref({
  accountBookId: undefined as number | undefined,
  date: [] as string[],
  amount: [] as number[],
  classifyList: [] as {
    mainClassifyId: number;
    subClassifyId: number | null;
  }[],
  remark: "",
  tagCodes: [] as number[]
});

watch(
  () => currentAccountBookId.value,
  val => {
    if (val) {
      filterForm.value.accountBookId = val;
    }
  },
  { immediate: true }
);

const getSelectedAccountBookLabel = (val: number | undefined) => {
  if (!val) return "";
  const book = billStore.accountBooks.find((b: any) => b.id === val);
  if (!book) return "";
  return `${getAccountBookIcon(book.image)} ${book.name}`;
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
      name: t("bill.pureAllExpense"),
      type: "EXPENSE",
      isAll: true,
      children: buildTree(expenseParents)
    },
    {
      id: "income-all",
      name: t("bill.pureAllIncome"),
      type: "INCOME",
      isAll: true,
      children: buildTree(incomeParents)
    }
  ];
});

const handleClassifyTreeChange = () => {
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

    if (
      expenseParentIds.includes(node.id) ||
      incomeParentIds.includes(node.id)
    ) {
      result.push({ mainClassifyId: node.id, subClassifyId: null as any });
    } else {
      result.push({ mainClassifyId: node.pid, subClassifyId: node.id });
    }
  });

  filterForm.value.classifyList = result;
  updateCheckboxState();
};

const updateCheckboxState = () => {
  const checkedKeys = classifyTreeRef.value?.getCheckedKeys() || [];

  const expenseParentIds = billStore.classifyList
    .filter((c: any) => c.pid === -1 && c.type === "EXPENSE")
    .map((c: any) => c.id);

  const incomeParentIds = billStore.classifyList
    .filter((c: any) => c.pid === -1 && c.type === "INCOME")
    .map((c: any) => c.id);

  const allExpenseChildren = billStore.classifyList
    .filter((c: any) => c.pid === -1 && c.type === "EXPENSE")
    .flatMap((p: any) => [
      p.id,
      ...billStore.classifyList
        .filter((c: any) => c.pid === p.id)
        .map((c: any) => c.id)
    ]);

  const allIncomeChildren = billStore.classifyList
    .filter((c: any) => c.pid === -1 && c.type === "INCOME")
    .flatMap((p: any) => [
      p.id,
      ...billStore.classifyList
        .filter((c: any) => c.pid === p.id)
        .map((c: any) => c.id)
    ]);

  const checkedExpenseKeys = checkedKeys.filter((id: any) =>
    allExpenseChildren.includes(id)
  );
  const checkedIncomeKeys = checkedKeys.filter((id: any) =>
    allIncomeChildren.includes(id)
  );

  selectAllExpense.value =
    allExpenseChildren.length > 0 &&
    checkedExpenseKeys.length === allExpenseChildren.length;
  isIndeterminateExpense.value =
    checkedExpenseKeys.length > 0 &&
    checkedExpenseKeys.length < allExpenseChildren.length;

  selectAllIncome.value =
    allIncomeChildren.length > 0 &&
    checkedIncomeKeys.length === allIncomeChildren.length;
  isIndeterminateIncome.value =
    checkedIncomeKeys.length > 0 &&
    checkedIncomeKeys.length < allIncomeChildren.length;
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
          ...billStore.classifyList
            .filter((c: any) => c.pid === p.id)
            .map((c: any) => c.id)
        ]);
      classifyTreeRef.value?.setCheckedKeys(
        currentKeys.filter((k: any) => !expenseKeys.includes(k))
      );
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
          ...billStore.classifyList
            .filter((c: any) => c.pid === p.id)
            .map((c: any) => c.id)
        ]);
      classifyTreeRef.value?.setCheckedKeys(
        currentKeys.filter((k: any) => !incomeKeys.includes(k))
      );
    }
    handleClassifyTreeChange();
  }
};

const handleQuery = () => {
  const selectedTagCodes = filterForm.value.tagCodes
    .map(tagId => {
      const tag = billStore.tagList.find(t => t.id === tagId);
      return tag ? String((tag as any).code) : null;
    })
    .filter(code => code !== null) as string[];

  const queryParams: any = {};

  if (filterForm.value.accountBookId !== undefined) {
    queryParams.accountBookId = filterForm.value.accountBookId;
  }
  if (filterForm.value.date && filterForm.value.date.length === 2) {
    queryParams.date = filterForm.value.date;
  }
  if (
    filterForm.value.amount[0] !== null ||
    filterForm.value.amount[1] !== null
  ) {
    queryParams.amount = filterForm.value.amount;
  }
  if (
    filterForm.value.classifyList &&
    filterForm.value.classifyList.length > 0
  ) {
    queryParams.classifyList = filterForm.value.classifyList;
  }
  if (filterForm.value.remark) {
    queryParams.remark = filterForm.value.remark;
  }
  if (selectedTagCodes && selectedTagCodes.length > 0) {
    queryParams.tagCodes = selectedTagCodes;
  }

  billStore.setQueryParams(queryParams);
  emit("query", filterForm.value.accountBookId);
};

const handleReset = () => {
  filterForm.value = {
    accountBookId: filterForm.value.accountBookId,
    date: [],
    amount: [],
    classifyList: [],
    remark: "",
    tagCodes: []
  };
  selectAllExpense.value = false;
  selectAllIncome.value = false;
  isIndeterminateExpense.value = false;
  isIndeterminateIncome.value = false;
  classifyTreeRef.value?.setCheckedKeys([]);
  emit("reset", filterForm.value.accountBookId);
};

const handleExport = async () => {
  if (!filterForm.value.date || filterForm.value.date.length !== 2) {
    ElMessage.warning(t("bill.exportDateRequired"));
    return;
  }

  const beginDate = new Date(filterForm.value.date[0]);
  const endDate = new Date(filterForm.value.date[1]);
  const diffDays = Math.ceil(
    (endDate.getTime() - beginDate.getTime()) / (1000 * 60 * 60 * 24)
  );

  if (diffDays > 365) {
    ElMessage.warning(t("bill.exportDateLimit"));
    return;
  }

  const selectedTagCodes = filterForm.value.tagCodes
    .map(tagId => {
      const tag = billStore.tagList.find(t => t.id === tagId);
      return tag ? String((tag as any).code) : null;
    })
    .filter(code => code !== null) as string[];

  const exportParams: any = {
    userId: userStore.id,
    date: filterForm.value.date
  };

  if (filterForm.value.accountBookId !== undefined) {
    exportParams.accountBookId = filterForm.value.accountBookId;
  }
  if (
    filterForm.value.amount[0] !== null ||
    filterForm.value.amount[1] !== null
  ) {
    exportParams.amount = filterForm.value.amount;
  }
  if (
    filterForm.value.classifyList &&
    filterForm.value.classifyList.length > 0
  ) {
    exportParams.classifyList = filterForm.value.classifyList;
  }
  if (filterForm.value.remark) {
    exportParams.remark = [filterForm.value.remark];
  }
  if (selectedTagCodes && selectedTagCodes.length > 0) {
    exportParams.tagCodes = selectedTagCodes;
  }

  try {
    const blob = await exportData(exportParams);
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `账单导出_${filterForm.value.date[0]}_${filterForm.value.date[1]}.xlsx`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    ElMessage.success(t("bill.exportSuccess"));
  } catch {
    ElMessage.error(t("bill.exportFailed"));
  }
};

const getFilterTagColor = (tagId: number) => {
  const tag = billStore.tagList.find(t => t.id === tagId);
  return tag ? tag.color : "";
};

const getFilterTagName = (tagId: number) => {
  const tag = billStore.tagList.find(t => t.id === tagId);
  return tag ? tag.name : "";
};

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

const toggleFilterTag = (tagId: number) => {
  const index = filterForm.value.tagCodes.indexOf(tagId);
  if (index === -1) {
    filterForm.value.tagCodes = [...filterForm.value.tagCodes, tagId];
  } else {
    filterForm.value.tagCodes = filterForm.value.tagCodes.filter(
      id => id !== tagId
    );
  }
};

const removeFilterTag = (tagId: number) => {
  filterForm.value.tagCodes = filterForm.value.tagCodes.filter(
    id => id !== tagId
  );
};

const selectFilterRemark = (remark: string) => {
  filterForm.value.remark = remark;
};

const queryRemarks = (
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

const treeProps = {
  children: "children",
  label: "name"
};
</script>

<template>
  <el-card class="bill-filter" shadow="never">
    <el-form :model="filterForm" label-width="80px">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-form-item :label="t('bill.pureAccountBook')">
            <el-select
              v-model="filterForm.accountBookId"
              :placeholder="t('bill.pureSelectPlaceholder')"
              :clearable="false"
              style="width: 100%"
            >
              <el-option
                v-for="book in billStore.accountBooks"
                :key="book.id"
                :label="`${getAccountBookIcon(book.image)} ${book.name}`"
                :value="book.id"
              >
                <div class="book-option">
                  <span
                    >{{ getAccountBookIcon(book.image) }} {{ book.name }}</span
                  >
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
        </el-col>
        <el-col :span="6">
          <el-form-item :label="t('bill.pureDate')">
            <el-date-picker
              v-model="filterForm.date"
              type="daterange"
              range-separator="-"
              :start-placeholder="t('bill.pureStartDate')"
              :end-placeholder="t('bill.pureEndDate')"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item :label="t('bill.pureAmount')">
            <el-input-number
              v-model="filterForm.amount[0]"
              :min="0"
              :precision="2"
              controls-position="right"
              style="width: 45%"
            />
            <span style="margin: 0 8px">-</span>
            <el-input-number
              v-model="filterForm.amount[1]"
              :min="0"
              :precision="2"
              controls-position="right"
              style="width: 45%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item :label="t('bill.pureClassify')">
            <div class="filter-classify-wrapper">
              <el-popover placement="bottom-start" :width="320" trigger="click">
                <template #reference>
                  <div class="classify-trigger">
                    <span
                      v-if="
                        !filterForm.classifyList ||
                        filterForm.classifyList.length === 0
                      "
                      class="placeholder"
                    >
                      {{ t("bill.pureSelectPlaceholder") }}
                    </span>
                    <span v-else>{{
                      t("bill.pureSelectedClassify", {
                        count: filterForm.classifyList.length
                      })
                    }}</span>
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
                        <span v-if="data.isAll"
                          >{{ getClassifyIcon("other") }} {{ node.label }}</span
                        >
                        <span v-else
                          >{{ getClassifyIcon(data.image) }}
                          {{ node.label }}</span
                        >
                      </span>
                    </template>
                  </el-tree>
                </div>
              </el-popover>
            </div>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="6">
          <el-form-item :label="t('bill.pureRemark')">
            <el-popover placement="bottom-start" :width="400" trigger="click">
              <template #reference>
                <el-input
                  v-model="filterForm.remark"
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
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item :label="t('bill.pureTag')">
            <el-popover placement="bottom-start" :width="400" trigger="click">
              <template #reference>
                <div class="filter-tag-trigger">
                  <span
                    v-if="filterForm.tagCodes.length === 0"
                    class="placeholder"
                  >
                    {{ t("bill.pureSelectPlaceholder") }}
                  </span>
                  <span v-else class="selected-tags">
                    <el-tag
                      v-for="tagId in filterForm.tagCodes"
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
                    :class="{ active: filterForm.tagCodes.includes(tag.id) }"
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
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item>
            <el-button type="primary" @click="handleQuery">
              {{ t("bill.pureQuery") }}
            </el-button>
            <el-button @click="handleReset">{{
              t("bill.pureReset")
            }}</el-button>
            <el-button type="success" @click="handleExport">
              {{ t("bill.pureExport") }}
            </el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </el-card>
</template>

<style lang="scss" scoped>
.bill-filter {
  margin-bottom: 16px;
}

.book-option {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: space-between;
}

:deep(.el-select-dropdown__item) {
  display: inline-block;
  margin: 4px;
}

:deep(.el-select-dropdown__item .el-checkbox) {
  display: inline;
}

.tag-option {
  display: inline-block;
  padding: 2px 8px;
  margin: 2px;
  color: #fff;
  border-radius: 10px;
}

.filter-tag-trigger {
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

.filter-remark-content,
.filter-tag-content {
  display: flex;
  flex-direction: column;
  width: 100%;

  .filter-remark-search,
  .filter-tag-search {
    flex-shrink: 0;
    margin-bottom: 12px;
  }

  .filter-remark-grid,
  .filter-tag-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
    width: 100%;
    max-height: 240px;
    overflow-y: auto;
  }

  .filter-remark-item,
  .filter-tag-item {
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

  .filter-tag-item.active {
    background-color: #ecf5ff;
  }
}

.filter-remark-trigger {
  flex: 1;
}

.filter-classify-wrapper {
  display: flex;
  flex: 1;
}

.classify-trigger {
  box-sizing: border-box;
  display: flex;
  align-items: center;
  width: 100%;
  min-width: 200px;
  height: 32px;
  padding: 0 8px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;

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
}

.custom-tree-node {
  display: flex;
  gap: 4px;
  align-items: center;
}
</style>
