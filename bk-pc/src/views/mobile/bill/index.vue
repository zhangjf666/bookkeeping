<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { showNotify, showConfirmDialog } from "vant";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import { deleteIncomeExpense } from "@/api/incomeExpense";
import { formatNumber } from "@/utils/format";
import { getClassifyIcon } from "@/utils/classifyIcons";
import dayjs from "dayjs";

defineOptions({
  name: "MobileBill"
});

const router = useRouter();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

const loading = ref(false);
const refreshing = ref(false);
const finished = ref(false);
const loadingMore = ref(false);
const initialized = ref(false);

const userId = computed(() => userStore.id);

// 筛选弹窗控制
const showFilter = ref(false);
const filterForm = ref({
  classifyList: [] as { mainClassifyId: number; subClassifyId: number | null }[],
  remark: "",
  amountStart: "" as string,
  amountEnd: "" as string,
  dateStart: "" as string,
  dateEnd: "" as string,
  tagCodes: [] as number[]
});

// 分类筛选状态
const selectedClassifyIds = ref<number[]>([]);
const showClassifyFilter = ref(false);
const classifyFilterType = ref<"EXPENSE" | "INCOME">("EXPENSE");

// 标签筛选
const showTagFilter = ref(false);
const selectedTagIds = ref<number[]>([]);

// 日期筛选
const showDateFilter = ref(false);
const tempDateRange = ref<string[]>([]);

// 是否有筛选条件
const hasFilter = computed(() => {
  return (
    filterForm.value.classifyList.length > 0 ||
    filterForm.value.remark ||
    filterForm.value.amountStart ||
    filterForm.value.amountEnd ||
    filterForm.value.dateStart ||
    filterForm.value.dateEnd ||
    filterForm.value.tagCodes.length > 0
  );
});

// 分类树数据
const classifyTreeData = computed(() => {
  return billStore.classifyTree.filter(c => c.type === classifyFilterType.value);
});

// 已选分类显示文本
const selectedClassifyText = computed(() => {
  if (filterForm.value.classifyList.length === 0) return "";
  return `已选 ${filterForm.value.classifyList.length} 项`;
});

// 已选标签显示文本
const selectedTagText = computed(() => {
  if (filterForm.value.tagCodes.length === 0) return "";
  return `已选 ${filterForm.value.tagCodes.length} 项`;
});

// 日期范围显示文本
const dateRangeText = computed(() => {
  if (filterForm.value.dateStart && filterForm.value.dateEnd) {
    return `${filterForm.value.dateStart} 至 ${filterForm.value.dateEnd}`;
  }
  if (filterForm.value.dateStart) {
    return `${filterForm.value.dateStart} 起`;
  }
  if (filterForm.value.dateEnd) {
    return `至 ${filterForm.value.dateEnd}`;
  }
  return "";
});

// 金额范围显示文本
const amountRangeText = computed(() => {
  if (filterForm.value.amountStart && filterForm.value.amountEnd) {
    return `¥${filterForm.value.amountStart} - ¥${filterForm.value.amountEnd}`;
  }
  if (filterForm.value.amountStart) {
    return `¥${filterForm.value.amountStart} 起`;
  }
  if (filterForm.value.amountEnd) {
    return `至 ¥${filterForm.value.amountEnd}`;
  }
  return "";
});

// 加载数据
const loadData = async (isRefresh = false) => {
  if (!userId.value) return;

  if (isRefresh) {
    billStore.queryParams.pageNo = 1;
    finished.value = false;
  }

  loading.value = true;
  try {
    await billStore.loadList(userId.value, false);
    if (billStore.list.length >= billStore.total) {
      finished.value = true;
    }
    initialized.value = true;
  } catch (error: any) {
    showNotify({ type: "danger", message: error?.message || "加载失败" });
  } finally {
    loading.value = false;
    loadingMore.value = false;
  }
};

// 下拉刷新
const onRefresh = async () => {
  refreshing.value = true;
  await loadData(true);
  refreshing.value = false;
};

// 上滑加载更多
const onLoadMore = async () => {
  if (!initialized.value) {
    loadingMore.value = false;
    return;
  }

  if (finished.value) {
    loadingMore.value = false;
    return;
  }

  if (billStore.list.length >= billStore.total) {
    finished.value = true;
    loadingMore.value = false;
    return;
  }

  billStore.queryParams.pageNo += 1;

  try {
    const prevLength = billStore.list.length;
    await billStore.loadList(userId.value, true);
    if (billStore.list.length === prevLength || billStore.list.length >= billStore.total) {
      finished.value = true;
    }
  } catch (error: any) {
    showNotify({ type: "danger", message: error?.message || "加载失败" });
    billStore.queryParams.pageNo -= 1;
  } finally {
    loadingMore.value = false;
  }
};

// 打开筛选弹窗
const openFilter = () => {
  showFilter.value = true;
};

// 选择分类
const toggleClassify = (classify: any) => {
  const id = classify.id;
  const index = selectedClassifyIds.value.indexOf(id);
  if (index === -1) {
    selectedClassifyIds.value = [...selectedClassifyIds.value, id];
  } else {
    selectedClassifyIds.value = selectedClassifyIds.value.filter(i => i !== id);
  }
};

// 判断分类是否选中
const isClassifySelected = (classify: any) => {
  return selectedClassifyIds.value.includes(classify.id);
};

// 选择全部支出/收入
const selectAllClassify = () => {
  const allIds: number[] = [];
  billStore.classifyList.forEach((c: any) => {
    if (c.type === classifyFilterType.value) {
      allIds.push(c.id);
    }
  });
  selectedClassifyIds.value = allIds;
};

// 清空分类选择
const clearClassify = () => {
  selectedClassifyIds.value = [];
};

// 确认分类选择
const confirmClassify = () => {
  const result: { mainClassifyId: number; subClassifyId: number | null }[] = [];
  selectedClassifyIds.value.forEach(id => {
    const classify = billStore.classifyList.find((c: any) => c.id === id);
    if (classify) {
      if ((classify as any).pid === -1) {
        result.push({ mainClassifyId: id, subClassifyId: null });
      } else {
        result.push({ mainClassifyId: (classify as any).pid, subClassifyId: id });
      }
    }
  });
  filterForm.value.classifyList = result;
  showClassifyFilter.value = false;
};

// 选择标签
const toggleTag = (tagId: number) => {
  const index = selectedTagIds.value.indexOf(tagId);
  if (index === -1) {
    selectedTagIds.value = [...selectedTagIds.value, tagId];
  } else {
    selectedTagIds.value = selectedTagIds.value.filter(i => i !== tagId);
  }
};

// 判断标签是否选中
const isTagSelected = (tagId: number) => {
  return selectedTagIds.value.includes(tagId);
};

// 确认标签选择
const confirmTag = () => {
  filterForm.value.tagCodes = [...selectedTagIds.value];
  showTagFilter.value = false;
};

// 清空标签选择
const clearTag = () => {
  selectedTagIds.value = [];
};

// 选择日期范围
const onDateRangeConfirm = (values: Date[]) => {
  filterForm.value.dateStart = dayjs(values[0]).format("YYYY-MM-DD");
  filterForm.value.dateEnd = dayjs(values[1]).format("YYYY-MM-DD");
  showDateFilter.value = false;
};

// 清空日期
const clearDate = () => {
  filterForm.value.dateStart = "";
  filterForm.value.dateEnd = "";
};

// 清空金额
const clearAmount = () => {
  filterForm.value.amountStart = "";
  filterForm.value.amountEnd = "";
};

// 清空备注
const clearRemark = () => {
  filterForm.value.remark = "";
};

// 重置筛选
const resetFilter = () => {
  filterForm.value = {
    classifyList: [],
    remark: "",
    amountStart: "",
    amountEnd: "",
    dateStart: "",
    dateEnd: "",
    tagCodes: []
  };
  selectedClassifyIds.value = [];
  selectedTagIds.value = [];
};

// 确认筛选
const confirmFilter = () => {
  // 构建查询参数
  const queryParams: any = {
    pageNo: 1
  };

  if (filterForm.value.classifyList.length > 0) {
    queryParams.classifyList = filterForm.value.classifyList;
  }

  if (filterForm.value.remark) {
    queryParams.remark = filterForm.value.remark;
  }

  if (filterForm.value.amountStart || filterForm.value.amountEnd) {
    queryParams.amount = [
      filterForm.value.amountStart ? parseFloat(filterForm.value.amountStart) : null,
      filterForm.value.amountEnd ? parseFloat(filterForm.value.amountEnd) : null
    ];
  }

  if (filterForm.value.dateStart && filterForm.value.dateEnd) {
    queryParams.date = [filterForm.value.dateStart, filterForm.value.dateEnd];
  }

  if (filterForm.value.tagCodes.length > 0) {
    const tagCodes = filterForm.value.tagCodes
      .map(tagId => {
        const tag = billStore.tagList.find(t => t.id === tagId);
        return tag ? (tag as any).code : null;
      })
      .filter(code => code !== null);
    queryParams.tagCodes = tagCodes;
  }

  billStore.setQueryParams(queryParams);
  showFilter.value = false;
  loadData(true);
};

// 编辑
const handleEdit = (id: number) => {
  router.push(`/record/${id}`);
};

// 删除
const handleDelete = async (id: number) => {
  try {
    await showConfirmDialog({
      title: "确认删除",
      message: "删除后无法恢复，确定要删除吗？"
    });
    await deleteIncomeExpense([id]);
    showNotify({ type: "success", message: "删除成功" });
    await loadData(true);
  } catch {
    // 取消删除
  }
};

// 记账
const goRecord = () => {
  router.push("/record");
};

// 格式化日期分组
const groupedRecords = computed(() => {
  const records = billStore.list;
  const groups: Record<string, typeof records> = {};

  records.forEach(record => {
    const date = record.date;
    if (!groups[date]) {
      groups[date] = [];
    }
    groups[date].push(record);
  });

  return Object.entries(groups).sort((a, b) => b[0].localeCompare(a[0]));
});

// 获取标签颜色
const getTagColor = (tagId: number) => {
  const tag = billStore.tagList.find(t => t.id === tagId);
  return tag ? tag.color : "#d83d34";
};

// 获取标签名称
const getTagName = (tagId: number) => {
  const tag = billStore.tagList.find(t => t.id === tagId);
  return tag ? tag.name : "";
};

onMounted(() => {
  if (!userStore.id) {
    router.replace("/login");
    return;
  }
  billStore.loadClassifyAndTag(userStore.id);
  loadData(true);
});

// 监听筛选弹窗打开，同步选中状态
watch(showFilter, (val) => {
  if (val) {
    // 同步分类选中状态
    const ids: number[] = [];
    filterForm.value.classifyList.forEach(item => {
      if (item.subClassifyId) {
        ids.push(item.subClassifyId);
      } else {
        ids.push(item.mainClassifyId);
      }
    });
    selectedClassifyIds.value = ids;
    selectedTagIds.value = [...filterForm.value.tagCodes];
  }
});
</script>

<template>
  <div class="mobile-bill">
    <!-- 顶部筛选栏 - 固定 -->
    <div class="filter-bar">
      <div class="filter-actions">
        <van-button size="small" :type="hasFilter ? 'primary' : 'default'" @click="openFilter">
          <van-icon name="filter-o" />
          筛选
          <van-badge v-if="hasFilter" dot />
        </van-button>
      </div>
      <div class="filter-tags" v-if="hasFilter">
        <van-tag v-if="filterForm.classifyList.length > 0" type="primary" closeable @close="filterForm.classifyList = []">
          分类 {{ filterForm.classifyList.length }}
        </van-tag>
        <van-tag v-if="filterForm.remark" type="primary" closeable @close="clearRemark">
          备注
        </van-tag>
        <van-tag v-if="filterForm.amountStart || filterForm.amountEnd" type="primary" closeable @close="clearAmount">
          金额
        </van-tag>
        <van-tag v-if="filterForm.dateStart || filterForm.dateEnd" type="primary" closeable @close="clearDate">
          日期
        </van-tag>
        <van-tag v-if="filterForm.tagCodes.length > 0" type="primary" closeable @close="filterForm.tagCodes = []">
          标签 {{ filterForm.tagCodes.length }}
        </van-tag>
      </div>
    </div>

    <!-- 账单列表 - 独立滚动 -->
    <div class="bill-scroll">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loadingMore"
          :finished="finished"
          :immediate-check="false"
          finished-text="没有更多了"
          @load="onLoadMore"
        >
          <van-loading v-if="loading && billStore.list.length === 0" class="loading" />

          <van-empty v-else-if="billStore.list.length === 0 && !loading" description="暂无账单记录">
            <van-button type="primary" size="small" @click="goRecord">
              去记账
            </van-button>
          </van-empty>

          <div v-else class="bill-list">
            <div v-for="[date, records] in groupedRecords" :key="date" class="date-group">
              <div class="date-header">
                <span class="date">{{ date }}</span>
                <span class="count">{{ records.length }} 笔</span>
              </div>

              <van-swipe-cell v-for="record in records" :key="record.id">
                <div class="record-item" @click="handleEdit(record.id)">
                  <div class="record-left">
                    <span class="classify-icon">{{ getClassifyIcon(record.mainClassifyImage) }}</span>
                    <div class="record-info">
                      <span class="classify-name">
                        {{ record.subClassifyName ? `${record.mainClassifyName} - ${record.subClassifyName}` : record.mainClassifyName }}
                      </span>
                      <span class="remark" v-if="record.remark">{{ record.remark }}</span>
                    </div>
                  </div>
                  <div class="record-right">
                    <span class="amount" :class="{ income: record.type === 'INCOME' }">
                      {{ record.type === 'INCOME' ? '+' : '-' }}¥{{ formatNumber(record.amount) }}
                    </span>
                  </div>
                </div>

                <template #right>
                  <van-button square type="danger" text="删除" class="delete-btn" @click="handleDelete(record.id)" />
                </template>
              </van-swipe-cell>
            </div>
          </div>
        </van-list>
      </van-pull-refresh>
    </div>

    <!-- 悬浮记账按钮 -->
    <div class="fab-button" @click="goRecord">
      <van-icon name="plus" />
    </div>

    <!-- 筛选弹窗 -->
    <van-popup
      v-model:show="showFilter"
      position="bottom"
      round
      style="height: 80%"
    >
      <div class="filter-popup">
        <div class="popup-header">
          <span class="title">筛选条件</span>
          <van-icon name="cross" @click="showFilter = false" />
        </div>

        <div class="popup-content">
          <!-- 分类筛选 -->
          <div class="filter-section">
            <div class="section-header">
              <span class="label">分类</span>
              <span class="value" @click="showClassifyFilter = true">
                {{ selectedClassifyText || '全部' }}
                <van-icon name="arrow" />
              </span>
            </div>
          </div>

          <!-- 备注筛选 -->
          <div class="filter-section">
            <div class="section-header">
              <span class="label">备注</span>
            </div>
            <van-field
              v-model="filterForm.remark"
              placeholder="输入备注关键词"
              clearable
            />
          </div>

          <!-- 金额筛选 -->
          <div class="filter-section">
            <div class="section-header">
              <span class="label">金额范围</span>
              <span v-if="amountRangeText" class="clear-btn" @click="clearAmount">清除</span>
            </div>
            <div class="amount-inputs">
              <van-field
                v-model="filterForm.amountStart"
                type="number"
                placeholder="最小金额"
                class="amount-input"
              />
              <span class="separator">-</span>
              <van-field
                v-model="filterForm.amountEnd"
                type="number"
                placeholder="最大金额"
                class="amount-input"
              />
            </div>
          </div>

          <!-- 日期筛选 -->
          <div class="filter-section">
            <div class="section-header">
              <span class="label">日期范围</span>
              <span v-if="dateRangeText" class="clear-btn" @click="clearDate">清除</span>
            </div>
            <div class="date-inputs">
              <van-field
                v-model="filterForm.dateStart"
                placeholder="开始日期"
                readonly
                clickable
                class="date-input"
                @click="showDateFilter = true"
              />
              <span class="separator">-</span>
              <van-field
                v-model="filterForm.dateEnd"
                placeholder="结束日期"
                readonly
                clickable
                class="date-input"
                @click="showDateFilter = true"
              />
            </div>
          </div>

          <!-- 标签筛选 -->
          <div class="filter-section">
            <div class="section-header">
              <span class="label">标签</span>
              <span class="value" @click="showTagFilter = true">
                {{ selectedTagText || '全部' }}
                <van-icon name="arrow" />
              </span>
            </div>
          </div>
        </div>

        <div class="popup-footer">
          <van-button block @click="resetFilter">重置</van-button>
          <van-button type="primary" block @click="confirmFilter">确定</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 分类选择弹窗 -->
    <van-popup
      v-model:show="showClassifyFilter"
      position="bottom"
      round
      style="height: 70%"
    >
      <div class="classify-filter-popup">
        <div class="popup-header">
          <span class="title">选择分类</span>
          <van-icon name="cross" @click="showClassifyFilter = false" />
        </div>

        <div class="classify-tabs">
          <span
            class="tab"
            :class="{ active: classifyFilterType === 'EXPENSE' }"
            @click="classifyFilterType = 'EXPENSE'"
          >支出</span>
          <span
            class="tab"
            :class="{ active: classifyFilterType === 'INCOME' }"
            @click="classifyFilterType = 'INCOME'"
          >收入</span>
        </div>

        <div class="classify-content">
          <div class="select-all" @click="selectAllClassify">
            <van-icon name="checked" v-if="selectedClassifyIds.length > 0" />
            <van-icon name="circle" v-else />
            <span>全选</span>
          </div>

          <div class="classify-grid">
            <div
              v-for="item in classifyTreeData"
              :key="item.id"
              class="classify-item"
              :class="{ active: isClassifySelected(item) }"
              @click="toggleClassify(item)"
            >
              <span class="icon">{{ getClassifyIcon(item.image) }}</span>
              <span class="name">{{ item.name }}</span>
            </div>
          </div>

          <div v-if="classifyTreeData.length === 0" class="empty-tip">暂无分类</div>
        </div>

        <div class="popup-footer">
          <van-button block @click="clearClassify">清空</van-button>
          <van-button type="primary" block @click="confirmClassify">确定</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 标签选择弹窗 -->
    <van-popup
      v-model:show="showTagFilter"
      position="bottom"
      round
      style="height: 50%"
    >
      <div class="tag-filter-popup">
        <div class="popup-header">
          <span class="title">选择标签</span>
          <van-icon name="cross" @click="showTagFilter = false" />
        </div>

        <div class="tag-content">
          <div class="tag-grid">
            <span
              v-for="tag in billStore.tagList"
              :key="tag.id"
              class="tag-item"
              :class="{ active: isTagSelected(tag.id) }"
              :style="{
                background: isTagSelected(tag.id) ? tag.color : 'transparent',
                color: isTagSelected(tag.id) ? '#fff' : tag.color,
                border: `1px solid ${tag.color}`
              }"
              @click="toggleTag(tag.id)"
            >
              {{ tag.name }}
            </span>
          </div>
          <van-empty v-if="billStore.tagList.length === 0" description="暂无标签" />
        </div>

        <div class="popup-footer">
          <van-button block @click="clearTag">清空</van-button>
          <van-button type="primary" block @click="confirmTag">确定</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 日期范围选择 -->
    <van-calendar
      v-model:show="showDateFilter"
      type="range"
      :min-date="new Date('2020-01-01')"
      :max-date="new Date()"
      @confirm="onDateRangeConfirm"
    />
  </div>
</template>

<style lang="scss" scoped>
.mobile-bill {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f7f8fa;
  overflow: hidden;
}

.filter-bar {
  flex-shrink: 0;
  background: #fff;
  padding: 10px 16px;
  z-index: 10;

  .filter-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .filter-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 10px;
  }
}

// 账单列表滚动区域
.bill-scroll {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
}

.loading {
  display: flex;
  justify-content: center;
  padding: 40px;
}

.bill-list {
  padding-bottom: calc(80px + env(safe-area-inset-bottom));

  .date-group {
    margin-bottom: 10px;

    .date-header {
      display: flex;
      justify-content: space-between;
      padding: 12px 16px;
      background: #fff;
      font-size: 14px;
      color: #323233;
      font-weight: 500;

      .count {
        font-size: 12px;
        color: #969799;
        font-weight: 400;
      }
    }

    .record-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 14px 16px;
      background: #fff;

      .record-left {
        display: flex;
        align-items: center;
        gap: 12px;

        .classify-icon {
          width: 40px;
          height: 40px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #f7f8fa;
          border-radius: 10px;
          font-size: 20px;
        }

        .record-info {
          display: flex;
          flex-direction: column;
          gap: 2px;

          .classify-name {
            font-size: 14px;
            color: #323233;
          }

          .remark {
            font-size: 12px;
            color: #969799;
          }
        }
      }

      .record-right {
        .amount {
          font-size: 16px;
          font-weight: 600;
          color: #d83d34;

          &.income {
            color: #00a151;
          }
        }
      }
    }

    :deep(.van-swipe-cell) {
      .van-swipe-cell__right {
        .delete-btn {
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #d83d34;
          border: none;
          color: #fff;
          font-size: 14px;
          padding: 0 20px;
        }
      }
    }
  }
}

.fab-button {
  position: fixed;
  right: 20px;
  bottom: calc(70px + env(safe-area-inset-bottom));
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #d83d34;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  box-shadow: 0 4px 12px rgba(216, 61, 52, 0.4);
  cursor: pointer;
  z-index: 99;
  transition: transform 0.2s, box-shadow 0.2s;

  &:active {
    transform: scale(0.95);
    box-shadow: 0 2px 8px rgba(216, 61, 52, 0.3);
  }
}

// 筛选弹窗
.filter-popup,
.classify-filter-popup,
.tag-filter-popup {
  height: 100%;
  display: flex;
  flex-direction: column;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #ebedf0;

    .title {
      font-size: 16px;
      font-weight: 600;
      color: #323233;
    }
  }

  .popup-content {
    flex: 1;
    overflow-y: auto;
    padding: 12px 16px;
  }

  .popup-footer {
    display: flex;
    gap: 12px;
    padding: 12px 16px;
    border-top: 1px solid #ebedf0;

    .van-button--primary {
      background: #d83d34;
      border-color: #d83d34;
    }
  }
}

.filter-section {
  margin-bottom: 16px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .label {
      font-size: 14px;
      font-weight: 500;
      color: #323233;
    }

    .value {
      font-size: 14px;
      color: #969799;
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .clear-btn {
      font-size: 12px;
      color: #d83d34;
    }
  }

  .amount-inputs,
  .date-inputs {
    display: flex;
    align-items: center;
    gap: 8px;

    .amount-input,
    .date-input {
      flex: 1;
    }

    .separator {
      color: #969799;
    }
  }
}

// 分类筛选
.classify-tabs {
  display: flex;
  padding: 0 16px;
  gap: 12px;
  border-bottom: 1px solid #ebedf0;

  .tab {
    padding: 12px 16px;
    font-size: 14px;
    color: #646566;
    position: relative;

    &.active {
      color: #d83d34;
      font-weight: 500;

      &::after {
        content: "";
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 2px;
        background: #d83d34;
      }
    }
  }
}

.classify-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;

  .select-all {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 0;
    color: #323233;
    font-size: 14px;
    border-bottom: 1px solid #ebedf0;
    margin-bottom: 12px;
  }

  .classify-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;

    .classify-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 12px 8px;
      border-radius: 8px;
      background: #f7f8fa;

      &.active {
        background: #fff5f5;
        border: 1px solid #d83d34;
      }

      .icon {
        font-size: 24px;
        margin-bottom: 4px;
      }

      .name {
        font-size: 12px;
        color: #323233;
        text-align: center;
      }
    }
  }

  .empty-tip {
    text-align: center;
    color: #969799;
    padding: 40px 0;
  }
}

// 标签筛选
.tag-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;

  .tag-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;

    .tag-item {
      padding: 6px 14px;
      border-radius: 6px;
      font-size: 13px;
      cursor: pointer;
    }
  }
}
</style>
