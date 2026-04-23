<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { showNotify, showConfirmDialog } from "vant";
import dayjs from "dayjs";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import { createIncomeExpense, updateIncomeExpense, deleteIncomeExpense, getIncomeExpenseById } from "@/api/incomeExpense";
import type { IncomeExpenseForm, IncomeExpense } from "@/types/bill";
import { formatNumber } from "@/utils/format";
import { getClassifyIcon } from "@/utils/classifyIcons";

defineOptions({
  name: "MobileRecord"
});

const route = useRoute();
const router = useRouter();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

const isEdit = computed(() => !!route.params.id);
const recordId = computed(() => Number(route.params.id));

// 表单数据
const form = ref<IncomeExpenseForm>({
  accountBookId: 0,
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

const loading = ref(false);
const submitting = ref(false);

// 弹窗控制
const showClassifyPicker = ref(false);
const showSubClassifyPicker = ref(false);
const showCalendar = ref(false);
const showTagPicker = ref(false);
const showRemarkPicker = ref(false);
const showNumberKeyboard = ref(false);

// 选中的分类ID
const selectedClassifyId = ref<number | undefined>(undefined);
const selectedTags = ref<number[]>([]);

// 分类选择状态
const tempSelectedMainClassifyId = ref<number | undefined>(undefined);

// 标签搜索
const tagSearchKeyword = ref("");

// 金额输入（用于数字键盘）
const amountInput = ref("");

// 金额显示值（键盘打开时显示原始输入，关闭时显示格式化后的值）
const amountDisplay = computed(() => {
  if (showNumberKeyboard.value) {
    // 键盘打开时，直接显示输入的值
    return amountInput.value || "0";
  }
  // 键盘关闭时，格式化为两位小数
  if (form.value.amount === 0) return "0.00";
  return form.value.amount.toFixed(2);
});

// 打开金额键盘
const openAmountKeyboard = () => {
  // 将当前金额转为字符串
  amountInput.value = form.value.amount === 0 ? "" : String(form.value.amount);
  showNumberKeyboard.value = true;
};

// 金额键盘输入
const onAmountInput = (key: string) => {
  // 处理小数点
  if (key === ".") {
    if (amountInput.value.includes(".")) {
      return; // 已有小数点，不能再输入
    }
    if (amountInput.value === "") {
      amountInput.value = "0."; // 空时以0.开头
    } else {
      amountInput.value += ".";
    }
    return;
  }

  // 处理数字
  // 检查小数位数，如果已有两位小数，不能再输入
  const dotIndex = amountInput.value.indexOf(".");
  if (dotIndex !== -1) {
    const decimalPart = amountInput.value.substring(dotIndex + 1);
    if (decimalPart.length >= 2) {
      return; // 已有两位小数，不能再输入
    }
  }

  amountInput.value += key;
};

// 金额键盘删除
const onAmountDelete = () => {
  amountInput.value = amountInput.value.slice(0, -1);
};

// 金额键盘关闭时更新表单
const onAmountKeyboardClose = () => {
  // 格式化为两位小数并更新表单
  if (amountInput.value && amountInput.value !== "." && amountInput.value !== "0.") {
    form.value.amount = parseFloat(amountInput.value);
  } else {
    form.value.amount = 0;
  }
  showNumberKeyboard.value = false;
};

// 日期显示
const dateDisplay = computed(() => {
  const d = dayjs(form.value.date);
  return `${d.format("YYYY-MM-DD")} ${["周日", "周一", "周二", "周三", "周四", "周五", "周六"][d.day()]}`;
});

// 分类列表（根据类型筛选）
const classifyTreeData = computed(() => {
  return billStore.classifyTree.filter(c => c.type === form.value.type);
});

// 当前选中的主分类
const currentMainClassify = computed(() => {
  if (!tempSelectedMainClassifyId.value) return null;
  return classifyTreeData.value.find(c => c.id === tempSelectedMainClassifyId.value);
});

// 子分类列表
const subClassifyList = computed(() => {
  if (!currentMainClassify.value?.children) return [];
  return currentMainClassify.value.children;
});

// 过滤后的标签列表
const filteredTagList = computed(() => {
  if (!tagSearchKeyword.value) return billStore.tagList;
  return billStore.tagList.filter(tag =>
    tag.name.toLowerCase().includes(tagSearchKeyword.value.toLowerCase())
  );
});

// 选中的分类名称显示
const classifyName = computed(() => {
  if (!selectedClassifyId.value) return "";
  const classify = billStore.classifyList.find(c => c.id === selectedClassifyId.value);
  if (!classify) return "";
  const icon = getClassifyIcon(classify.image || "");
  if (classify.pid === -1) {
    return `${icon} ${classify.name}`;
  } else {
    const parent = billStore.classifyList.find(c => c.id === classify.pid);
    return `${icon} ${parent?.name || ""} - ${classify.name}`;
  }
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

// 根据标签ID获取标签code
const getTagCodesByIds = (tagIds: number[]) => {
  return tagIds
    .map(id => {
      const tag = billStore.tagList.find(t => t.id === id);
      return tag ? (tag as any).code : null;
    })
    .filter(code => code !== null)
    .join(",");
};

// 根据标签code获取标签ID
const getTagIdsByCodes = (tagCodesStr: string) => {
  if (!tagCodesStr) return [];
  const codes = tagCodesStr.split(",").map(c => Number(c.trim()));
  return billStore.tagList
    .filter(tag => codes.includes((tag as any).code))
    .map(tag => tag.id);
};

// 切换标签选中状态
const toggleTag = (tagId: number) => {
  const index = selectedTags.value.indexOf(tagId);
  if (index === -1) {
    selectedTags.value = [...selectedTags.value, tagId];
  } else {
    selectedTags.value = selectedTags.value.filter(id => id !== tagId);
  }
};

// 移除标签
const removeTag = (tagId: number) => {
  selectedTags.value = selectedTags.value.filter(id => id !== tagId);
};

// 加载编辑数据
const loadEditData = async () => {
  if (!isEdit.value || !recordId.value) return;

  loading.value = true;
  try {
    // 先从store中查找，找不到则从API获取
    let record = billStore.list.find((r: IncomeExpense) => r.id === recordId.value);

    if (!record) {
      // 从API获取单条记录
      record = await getIncomeExpenseById(recordId.value);
    }

    if (record) {
      // 先设置类型，确保分类列表正确筛选
      form.value.type = record.type;

      form.value = {
        id: record.id,
        accountBookId: record.accountBookId,
        amount: record.amount,
        type: record.type,
        date: record.date,
        remark: record.remark,
        mainClassify: record.mainClassify,
        subClassify: record.subClassify || undefined,
        isCreditCard: record.isCreditCard,
        isAddRemark: "NO",
        tagCodes: record.tagCodes || ""
      };
      if (record.subClassify) {
        selectedClassifyId.value = record.subClassify;
      } else if (record.mainClassify) {
        selectedClassifyId.value = record.mainClassify;
      }
      selectedTags.value = getTagIdsByCodes(record.tagCodes || "");
    }
  } catch (error: any) {
    showNotify({ type: "danger", message: error?.message || "加载失败" });
  } finally {
    loading.value = false;
  }
};

// 初始化信用卡配置
const initCreditCardFromConfig = () => {
  const config = billStore.userConfigList.find(c => c.name === "is_credit_card");
  form.value.isCreditCard = config?.value === "1" ? "YES" : "NO";
};

// 初始化
onMounted(async () => {
  if (!userStore.id) {
    router.replace("/login");
    return;
  }

  await billStore.loadClassifyAndTag(userStore.id);

  if (billStore.currentAccountBook) {
    form.value.accountBookId = billStore.currentAccountBook.id;
  }

  initCreditCardFromConfig();
  await loadEditData();
});

// 监听分类选择变化
watch(selectedClassifyId, val => {
  if (val) {
    const classify = billStore.classifyList.find(c => c.id === val);
    if (classify) {
      if (classify.pid === -1) {
        form.value.mainClassify = val;
        form.value.subClassify = undefined;
      } else {
        form.value.mainClassify = classify.pid;
        form.value.subClassify = val;
      }
    }
  } else {
    form.value.mainClassify = 0;
    form.value.subClassify = undefined;
  }
});

// 监听标签变化
watch(selectedTags, val => {
  form.value.tagCodes = getTagCodesByIds(val);
}, { deep: true });

// 切换类型
const toggleType = (type: "INCOME" | "EXPENSE") => {
  form.value.type = type;
  selectedClassifyId.value = undefined;
  form.value.mainClassify = 0;
  form.value.subClassify = undefined;
  tempSelectedMainClassifyId.value = undefined;
};

// 打开分类选择弹窗时，初始化临时选中状态
const onOpenClassifyPicker = () => {
  // 根据当前选中的分类ID，设置临时选中状态
  if (selectedClassifyId.value) {
    const classify = billStore.classifyList.find(c => c.id === selectedClassifyId.value);
    if (classify) {
      // 如果选中的是子分类，设置其父分类为临时选中
      if (classify.pid !== -1) {
        tempSelectedMainClassifyId.value = classify.pid;
      } else {
        tempSelectedMainClassifyId.value = selectedClassifyId.value;
      }
    }
  } else {
    tempSelectedMainClassifyId.value = undefined;
  }
};

// 选择主分类
const onSelectMainClassify = (classify: any) => {
  tempSelectedMainClassifyId.value = classify.id;
};

// 确认主分类选择（点击确定按钮）
const onConfirmMainClassify = () => {
  if (tempSelectedMainClassifyId.value) {
    selectedClassifyId.value = tempSelectedMainClassifyId.value;
    showClassifyPicker.value = false;
    tempSelectedMainClassifyId.value = undefined;
  }
};

// 打开子分类选择
const openSubClassifyPicker = () => {
  showSubClassifyPicker.value = true;
};

// 选择子分类
const onSelectSubClassify = (classifyId: number) => {
  selectedClassifyId.value = classifyId;
  showSubClassifyPicker.value = false;
  showClassifyPicker.value = false;
  tempSelectedMainClassifyId.value = undefined;
};

// 关闭子分类弹窗（不选择子分类，不改变分类）
const onCloseSubClassifyPicker = () => {
  showSubClassifyPicker.value = false;
  // 不改变分类，保持原来的状态
};

// 日期选择 - 点击日期后自动关闭并填入
const onSelectDate = (date: Date) => {
  form.value.date = dayjs(date).format("YYYY-MM-DD");
  showCalendar.value = false;
};

// 关闭分类弹窗时清除临时选择
const onCloseClassifyPicker = () => {
  tempSelectedMainClassifyId.value = undefined;
};

// 打开标签选择弹窗时，清空搜索
const onOpenTagPicker = () => {
  tagSearchKeyword.value = "";
};

// 选择常用备注
const onSelectCommonRemark = (remark: { remark: string; classifyId: number }) => {
  form.value.remark = remark.remark;
  // 根据classifyId选中对应分类
  if (remark.classifyId) {
    const classify = billStore.classifyList.find(c => c.id === remark.classifyId);
    if (classify) {
      // 切换到对应的类型（收入/支出）
      if (classify.type !== form.value.type) {
        form.value.type = classify.type;
        selectedClassifyId.value = undefined;
        form.value.mainClassify = 0;
        form.value.subClassify = undefined;
      }
      // 设置分类
      if (classify.pid === -1) {
        selectedClassifyId.value = classify.id;
      } else {
        selectedClassifyId.value = classify.id;
      }
    }
  }
  showRemarkPicker.value = false;
};

// 提交
const handleSubmit = async () => {
  if (form.value.amount <= 0) {
    showNotify({ type: "warning", message: "请输入金额" });
    return;
  }

  if (!form.value.mainClassify) {
    showNotify({ type: "warning", message: "请选择分类" });
    return;
  }

  if (form.value.type === "INCOME") {
    form.value.isCreditCard = "NO";
  }

  submitting.value = true;
  try {
    // 构建提交数据，确保 subClassify 为 null 时明确传递
    const submitData = {
      ...form.value,
      subClassify: form.value.subClassify ?? null
    };

    if (isEdit.value) {
      await updateIncomeExpense(userStore.id!, submitData);
      showNotify({ type: "success", message: "修改成功" });
    } else {
      await createIncomeExpense(userStore.id!, submitData);
      showNotify({ type: "success", message: "记账成功" });
    }
    router.back();
  } catch (error: any) {
    showNotify({ type: "danger", message: error?.message || "操作失败" });
  } finally {
    submitting.value = false;
  }
};

// 删除
const handleDelete = async () => {
  try {
    await showConfirmDialog({
      title: "确认删除",
      message: "删除后无法恢复，确定要删除吗？"
    });
    await deleteIncomeExpense([recordId.value]);
    showNotify({ type: "success", message: "删除成功" });
    router.back();
  } catch {
    // 取消删除
  }
};
</script>

<template>
  <div class="mobile-record">
    <van-loading v-if="loading" class="page-loading" />

    <template v-else>
      <!-- 类型切换 -->
      <div class="type-tabs">
        <div
          class="tab"
          :class="{ active: form.type === 'EXPENSE' }"
          @click="toggleType('EXPENSE')"
        >
          支出
        </div>
        <div
          class="tab"
          :class="{ active: form.type === 'INCOME' }"
          @click="toggleType('INCOME')"
        >
          收入
        </div>
      </div>

      <!-- 金额输入 -->
      <div class="amount-section" @click="openAmountKeyboard">
        <div class="amount-label">{{ form.type === 'EXPENSE' ? '支出' : '收入' }}金额</div>
        <div class="amount-input">
          <span class="currency">¥</span>
          <span class="input">{{ amountDisplay }}</span>
        </div>
      </div>

      <!-- 表单项 -->
      <van-cell-group inset class="form-group">
        <!-- 分类 -->
        <van-cell
          title="分类"
          :value="classifyName || '请选择'"
          is-link
          @click="showClassifyPicker = true"
        >
          <template #icon>
            <span class="cell-icon">📂</span>
          </template>
        </van-cell>

        <!-- 日期 -->
        <van-cell
          title="日期"
          :value="dateDisplay"
          is-link
          @click="showCalendar = true"
        >
          <template #icon>
            <span class="cell-icon">📅</span>
          </template>
        </van-cell>

        <!-- 备注 -->
        <van-cell
          title="备注"
          :value="form.remark || '添加备注'"
          is-link
          @click="showRemarkPicker = true"
        >
          <template #icon>
            <span class="cell-icon">📝</span>
          </template>
        </van-cell>

        <!-- 标签 -->
        <van-cell title="标签" is-link @click="showTagPicker = true">
          <template #icon>
            <span class="cell-icon">🏷️</span>
          </template>
          <template #value>
            <div v-if="selectedTags.length > 0" class="selected-tags-preview">
              <span
                v-for="tagId in selectedTags"
                :key="tagId"
                class="selected-tag"
                :style="{ background: getTagColor(tagId) }"
              >
                {{ getTagName(tagId) }}
              </span>
            </div>
            <span v-else class="placeholder-text">添加标签</span>
          </template>
        </van-cell>

        <!-- 信用卡消费（仅支出类型显示） -->
        <van-cell v-if="form.type === 'EXPENSE'" title="信用卡消费">
          <template #icon>
            <span class="cell-icon">💳</span>
          </template>
          <template #value>
            <van-switch
              v-model="form.isCreditCard"
              active-value="YES"
              inactive-value="NO"
              size="20"
              active-color="#d83d34"
            />
          </template>
        </van-cell>

        <!-- 添加到常用备注 -->
        <van-cell title="添加到常用备注">
          <template #icon>
            <span class="cell-icon">📌</span>
          </template>
          <template #value>
            <van-switch
              v-model="form.isAddRemark"
              active-value="YES"
              inactive-value="NO"
              size="20"
              active-color="#d83d34"
            />
          </template>
        </van-cell>
      </van-cell-group>

      <!-- 操作按钮 -->
      <div class="actions">
        <van-button
          type="primary"
          block
          :loading="submitting"
          @click="handleSubmit"
        >
          保存
        </van-button>

        <van-button
          v-if="isEdit"
          type="danger"
          block
          plain
          @click="handleDelete"
        >
          删除
        </van-button>
      </div>
    </template>

    <!-- 主分类选择弹窗 -->
    <van-popup
      v-model:show="showClassifyPicker"
      position="bottom"
      round
      style="height: 70%"
      @open="onOpenClassifyPicker"
      @close="onCloseClassifyPicker"
    >
      <div class="classify-picker">
        <div class="picker-header">
          <span class="title">选择分类</span>
          <van-icon name="cross" @click="showClassifyPicker = false" />
        </div>
        <div class="classify-grid">
          <div
            v-for="item in classifyTreeData"
            :key="item.id"
            class="classify-item"
            :class="{ active: tempSelectedMainClassifyId === item.id }"
            @click="onSelectMainClassify(item)"
          >
            <span class="icon">{{ getClassifyIcon(item.image) }}</span>
            <span class="name">{{ item.name }}</span>
          </div>
        </div>
        <div class="picker-footer">
          <van-button
            v-if="currentMainClassify?.children?.length > 0"
            type="default"
            block
            @click="openSubClassifyPicker"
          >
            选择子分类
          </van-button>
          <van-button type="primary" block @click="onConfirmMainClassify">
            确定
          </van-button>
        </div>
      </div>
    </van-popup>

    <!-- 子分类选择弹窗 -->
    <van-popup
      v-model:show="showSubClassifyPicker"
      position="bottom"
      round
      style="height: 60%"
      @close="onCloseSubClassifyPicker"
    >
      <div class="classify-picker">
        <div class="picker-header">
          <span class="title">选择子分类 - {{ currentMainClassify?.name }}</span>
          <van-icon name="cross" @click="showSubClassifyPicker = false" />
        </div>
        <div class="classify-grid">
          <div
            v-for="item in subClassifyList"
            :key="item.id"
            class="classify-item"
            :class="{ active: selectedClassifyId === item.id }"
            @click="onSelectSubClassify(item.id)"
          >
            <span class="icon">{{ getClassifyIcon(item.image) }}</span>
            <span class="name">{{ item.name }}</span>
          </div>
        </div>
      </div>
    </van-popup>

    <!-- 日期选择弹窗 -->
    <van-popup
      v-model:show="showCalendar"
      position="bottom"
      round
      style="height: 580px"
    >
      <div class="calendar-picker">
        <div class="picker-header">
          <span class="title">选择日期</span>
          <van-icon name="cross" @click="showCalendar = false" />
        </div>
        <van-calendar
          :show-confirm="false"
          :default-date="new Date(form.date)"
          :min-date="new Date('2020-01-01')"
          :max-date="new Date()"
          :poppable="false"
          :show-title="false"
          class="calendar-content"
          @select="onSelectDate"
        />
      </div>
    </van-popup>

    <!-- 备注选择弹窗 -->
    <van-popup
      v-model:show="showRemarkPicker"
      position="bottom"
      round
      style="height: 50%"
    >
      <div class="remark-picker">
        <div class="picker-header">
          <span class="title">添加备注</span>
          <van-icon name="cross" @click="showRemarkPicker = false" />
        </div>
        <div class="remark-content">
          <van-field
            v-model="form.remark"
            placeholder="请输入备注内容"
            class="remark-input"
          />
          <div class="common-remarks" v-if="billStore.remarkList.length > 0">
            <div class="section-title">常用备注</div>
            <div class="remark-tags">
              <span
                v-for="remark in billStore.remarkList.slice(0, 12)"
                :key="remark.id"
                class="remark-tag"
                @click="onSelectCommonRemark(remark)"
              >
                {{ remark.remark }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </van-popup>

    <!-- 标签选择弹窗 -->
    <van-popup
      v-model:show="showTagPicker"
      position="bottom"
      round
      style="height: 60%"
      @open="onOpenTagPicker"
    >
      <div class="tag-picker">
        <div class="picker-header">
          <span class="title">选择标签</span>
          <van-icon name="cross" @click="showTagPicker = false" />
        </div>
        <div class="tag-content">
          <van-search
            v-model="tagSearchKeyword"
            placeholder="搜索标签"
            class="tag-search"
          />
          <div class="tag-list">
            <span
              v-for="tag in filteredTagList"
              :key="tag.id"
              class="tag-item"
              :class="{ active: selectedTags.includes(tag.id) }"
              :style="{
                background: selectedTags.includes(tag.id) ? tag.color : 'transparent',
                color: selectedTags.includes(tag.id) ? '#fff' : tag.color,
                border: `1px solid ${tag.color}`
              }"
              @click="toggleTag(tag.id)"
            >
              {{ tag.name }}
            </span>
          </div>
          <van-empty v-if="filteredTagList.length === 0" description="暂无标签" />
        </div>
        <div class="picker-footer" v-if="selectedTags.length > 0">
          <div class="selected-tags">
            <span class="label">已选：</span>
            <span
              v-for="tagId in selectedTags"
              :key="tagId"
              class="selected-tag-item"
              :style="{ background: getTagColor(tagId) }"
            >
              {{ getTagName(tagId) }}
              <van-icon name="cross" class="remove-icon" @click.stop="removeTag(tagId)" />
            </span>
          </div>
        </div>
      </div>
    </van-popup>

    <!-- 金额数字键盘 -->
    <van-number-keyboard
      :show="showNumberKeyboard"
      theme="custom"
      extra-key="."
      close-button-text="完成"
      @input="onAmountInput"
      @delete="onAmountDelete"
      @close="onAmountKeyboardClose"
      @blur="onAmountKeyboardClose"
    />
  </div>
</template>

<style lang="scss" scoped>
.mobile-record {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 20px;
}

.page-loading {
  display: flex;
  justify-content: center;
  padding-top: 100px;
}

.type-tabs {
  display: flex;
  background: #fff;
  padding: 15px 20px;
  gap: 12px;

  .tab {
    flex: 1;
    text-align: center;
    padding: 12px;
    border-radius: 8px;
    background: #f7f8fa;
    font-size: 16px;
    font-weight: 500;
    color: #646566;
    transition: all 0.3s;

    &.active {
      background: #d83d34;
      color: #fff;
    }
  }
}

.amount-section {
  background: #fff;
  padding: 24px 20px;
  margin-bottom: 10px;
  cursor: pointer;

  .amount-label {
    font-size: 14px;
    color: #969799;
    margin-bottom: 12px;
  }

  .amount-input {
    display: flex;
    align-items: baseline;

    .currency {
      font-size: 28px;
      font-weight: 600;
      color: #323233;
      margin-right: 4px;
    }

    .input {
      flex: 1;
      font-size: 40px;
      font-weight: 600;
      color: #323233;
    }
  }
}

.form-group {
  margin-bottom: 20px;

  .cell-icon {
    font-size: 18px;
    margin-right: 8px;
  }

  .selected-tags-preview {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    justify-content: flex-end;

    .selected-tag {
      display: inline-block;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 12px;
      color: #fff;
    }
  }

  .placeholder-text {
    color: #969799;
  }
}

.actions {
  padding: 0 16px;

  .van-button--primary {
    background: #d83d34;
    border-color: #d83d34;
  }

  .van-button--danger {
    &.van-button--plain {
      background: #fff;
      border-color: #d83d34;
      color: #d83d34;
    }
  }

  .van-button {
    margin-bottom: 12px;
  }
}

// 分类选择器
.classify-picker {
  height: 100%;
  display: flex;
  flex-direction: column;

  .picker-header {
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

  .classify-grid {
    flex: 1;
    overflow-y: auto;
    padding: 12px;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
    align-content: start;

    .classify-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 12px 8px;
      border-radius: 12px;
      background: #f7f8fa;
      cursor: pointer;
      transition: all 0.2s;

      &:active {
        background: #e8e8e8;
      }

      &.active {
        background: #fff5f5;
        border: 2px solid #d83d34;
      }

      .icon {
        font-size: 28px;
        margin-bottom: 6px;
      }

      .name {
        font-size: 12px;
        color: #323233;
        text-align: center;
      }
    }
  }

  .picker-footer {
    padding: 12px 16px;
    border-top: 1px solid #ebedf0;
    display: flex;
    gap: 12px;

    .van-button {
      flex: 1;
    }

    .van-button--primary {
      background: #d83d34;
      border-color: #d83d34;
    }
  }
}

// 日历选择器
.calendar-picker {
  height: 100%;
  display: flex;
  flex-direction: column;

  .picker-header {
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

  .calendar-content {
    flex: 1;
    height: auto;
  }

  :deep(.van-calendar) {
    height: 100%;

    .van-calendar__selected-day {
      background: #d83d34;
    }

    .van-calendar__top-info {
      color: #d83d34;
    }

    // 当前日期红色边框 - 只显示边框，不影响选中状态
    .van-calendar__day--today {
      position: relative;

      &::after {
        content: "";
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 28px;
        height: 28px;
        border: 2px solid #d83d34;
        border-radius: 50%;
        pointer-events: none;
        z-index: 1;
      }

      // 如果今天被选中，边框颜色变白以保持可见
      &.van-calendar__selected-day::after {
        border-color: #fff;
      }
    }
  }
}

// 备注选择器
.remark-picker {
  height: 100%;
  display: flex;
  flex-direction: column;

  .picker-header {
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

  .remark-content {
    flex: 1;
    overflow-y: auto;
    padding: 12px;

    .remark-input {
      margin-bottom: 16px;
    }

    .section-title {
      font-size: 14px;
      color: #969799;
      margin-bottom: 12px;
    }

    .remark-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .remark-tag {
        display: inline-block;
        padding: 6px 12px;
        background: #f7f8fa;
        border-radius: 6px;
        font-size: 13px;
        color: #323233;
        cursor: pointer;

        &:active {
          background: #e8e8e8;
        }
      }
    }
  }
}

// 标签选择器
.tag-picker {
  height: 100%;
  display: flex;
  flex-direction: column;

  .picker-header {
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

  .tag-content {
    flex: 1;
    overflow-y: auto;
    padding: 12px;

    .tag-search {
      margin-bottom: 12px;
      padding: 0;
    }

    .tag-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .tag-item {
        display: inline-block;
        padding: 6px 14px;
        border-radius: 6px;
        font-size: 13px;
        cursor: pointer;
        transition: all 0.2s;

        &:active {
          opacity: 0.8;
        }

        &.active {
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        }
      }
    }
  }

  .picker-footer {
    padding: 12px 16px;
    border-top: 1px solid #ebedf0;
    background: #fff;

    .selected-tags {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 8px;

      .label {
        font-size: 14px;
        color: #646566;
      }

      .selected-tag-item {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 4px 10px;
        border-radius: 4px;
        font-size: 12px;
        color: #fff;

        .remove-icon {
          font-size: 12px;
          cursor: pointer;
        }
      }
    }
  }
}
</style>
