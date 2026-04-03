<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useBillStoreHook } from "@/store/modules/bill";
import { getAccountBookIcon } from "@/utils/accountBook";
import { getClassifyIcon } from "@/utils/classifyIcons";

defineOptions({
  name: "BillFilter"
});

const emit = defineEmits<{
  query: [accountBookId: number | undefined];
  reset: [];
}>();

const { t } = useI18n();
const billStore = useBillStoreHook();

const classifyTreeData = computed(() => billStore.classifyTree);
const currentAccountBookId = computed(() => billStore.currentAccountBook?.id);

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

const filterForm = ref({
  accountBookId: undefined as number | undefined,
  type: undefined as "EXPENSE" | "INCOME" | undefined,
  date: [] as string[],
  amount: [] as number[],
  mainClassify: undefined as number | undefined,
  subClassify: undefined as number | undefined,
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

watch(
  () => filterForm.value.mainClassify,
  (newVal, oldVal) => {
    if (!newVal && oldVal) {
      filterForm.value.subClassify = undefined;
    }
  }
);

const typeOptions = [
  { label: t("bill.pureExpense"), value: "EXPENSE" },
  { label: t("bill.pureIncome"), value: "INCOME" }
];

const getSelectedAccountBookLabel = (val: number | undefined) => {
  if (!val) return "";
  const book = billStore.accountBooks.find((b: any) => b.id === val);
  if (!book) return "";
  return `${getAccountBookIcon(book.image)} ${book.name}`;
};

const handleQuery = () => {
  const selectedId = filterForm.value.mainClassify;
  let mainClassify: number | undefined = undefined;
  let subClassify: number | undefined = undefined;

  if (selectedId) {
    const classify = billStore.classifyList.find(c => c.id === selectedId);
    if (classify) {
      if (classify.pid === -1) {
        mainClassify = selectedId;
      } else {
        mainClassify = classify.pid;
        subClassify = selectedId;
      }
    }
  }

  const selectedTagCodes = filterForm.value.tagCodes
    .map(tagId => {
      const tag = billStore.tagList.find(t => t.id === tagId);
      return tag ? (tag as any).code : null;
    })
    .filter(code => code !== null) as number[];

  const queryParams: any = {};

  if (filterForm.value.accountBookId !== undefined) {
    queryParams.accountBookId = filterForm.value.accountBookId;
  }
  if (filterForm.value.date && filterForm.value.date.length === 2) {
    queryParams.date = filterForm.value.date;
  }
  if (filterForm.value.amount && filterForm.value.amount.length === 2) {
    queryParams.amount = filterForm.value.amount;
  }
  if (mainClassify) {
    queryParams.mainClassify = mainClassify;
  }
  if (subClassify) {
    queryParams.subClassify = subClassify;
  }
  if (filterForm.value.remark) {
    queryParams.remark = filterForm.value.remark;
  }
  if (selectedTagCodes.length > 0) {
    queryParams.tagCodes = selectedTagCodes;
  }

  if (filterForm.value.type !== undefined) {
    queryParams.type = filterForm.value.type;
  }

  billStore.setQueryParams(queryParams);

  if (!selectedId && billStore.queryParams) {
    delete (billStore.queryParams as any).mainClassify;
    delete (billStore.queryParams as any).subClassify;
  }

  emit("query", filterForm.value.accountBookId);
};

const handleReset = () => {
  filterForm.value = {
    accountBookId: undefined,
    type: undefined,
    date: [],
    amount: [],
    mainClassify: undefined,
    subClassify: undefined,
    remark: "",
    tagCodes: []
  };
  emit("reset");
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
              clearable
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
        </el-col>
        <el-col :span="6">
          <el-form-item :label="t('bill.pureType')">
            <el-select
              v-model="filterForm.type"
              :placeholder="t('bill.pureSelectPlaceholder')"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="item in typeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
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
      </el-row>
      <el-row :gutter="16">
        <el-col :span="6">
          <el-form-item :label="t('bill.pureClassify')">
            <el-tree-select
              v-model="filterForm.mainClassify"
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
        </el-col>
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
</style>
