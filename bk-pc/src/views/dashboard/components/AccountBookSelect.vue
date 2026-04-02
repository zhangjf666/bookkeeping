<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { useBillStoreHook } from "@/store/modules/bill";
import { getAccountBookIcon } from "@/utils/accountBook";

defineOptions({
  name: "AccountBookSelect"
});

const { t } = useI18n();
const billStore = useBillStoreHook();

const currentBook = computed(() => billStore.currentAccountBook);

const handleChange = (value: number) => {
  const book = billStore.accountBooks.find((item: any) => item.id === value);
  if (book) {
    billStore.setCurrentAccountBook(book);
  }
};
</script>

<template>
  <div class="account-book-select">
    <span class="label">{{ t("dashboard.pureCurrentBook") }}:</span>
    <el-select
      :model-value="currentBook?.id"
      placeholder="请选择账本"
      style="width: 200px"
      @change="handleChange"
    >
        <el-option
          v-for="book in billStore.accountBooks"
          :key="book.id"
          :label="`${getAccountBookIcon(book.image)} ${book.name}`"
          :value="book.id"
        >
        <div class="book-option">
          <span>{{ getAccountBookIcon(book.image) }} {{ book.name }}</span>
          <el-tag v-if="book.isDefault === 'YES'" size="small" type="success">
            {{ t("dashboard.pureDefault") }}
          </el-tag>
        </div>
      </el-option>
    </el-select>
  </div>
</template>

<style lang="scss" scoped>
.account-book-select {
  display: flex;
  gap: 12px;
  align-items: center;

  .label {
    font-size: 14px;
    color: #606266;
    white-space: nowrap;
  }
}

.book-option {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: space-between;
}
</style>
