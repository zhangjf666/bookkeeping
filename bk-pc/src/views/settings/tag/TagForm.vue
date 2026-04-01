<script setup lang="ts">
import { ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
import type { FormInstance } from "element-plus";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import { createUserTag, updateUserTag } from "@/api/userTag";
import type { UserTag, UserTagForm } from "@/types/userTag";

defineOptions({
  name: "TagForm"
});

const { t } = useI18n();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

interface Props {
  visible: boolean;
  data?: UserTag | null;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  "update:visible": [value: boolean];
  success: [tagId: number];
}>();

const formRef = ref<FormInstance>();
const loading = ref(false);

const formData = ref<UserTagForm>({
  userId: userStore.id,
  name: "",
  color: "#409EFF",
  sort: 0
});

const rules = {
  name: [
    { required: true, message: t("tag.pureTagNameRequired"), trigger: "blur" },
    { max: 20, message: t("tag.pureTagNameLength"), trigger: "blur" }
  ],
  color: [
    { required: true, message: t("tag.pureColorRequired"), trigger: "change" }
  ]
};

const colorPresets = [
  "#FF6B6B",
  "#FF8C69",
  "#FFA07A",
  "#FFB347",
  "#FFD700",
  "#FFEAA7",
  "#F7DC6F",
  "#98D8C8",
  "#96CEB4",
  "#4ECDC4",
  "#45B7D1",
  "#87CEEB",
  "#6BB3F0",
  "#DDA0DD",
  "#DA70D6",
  "#FF69B4",
  "#FF1493",
  "#DC143C",
  "#B22222",
  "#2F4F4F",
  "#708090",
  "#778899",
  "#A9A9A9",
  "#000000",
  "#FF0000",
  "#00FF00",
  "#0000FF",
  "#FFFF00",
  "#00FFFF",
  "#FF00FF"
];

const rgbToHex = (rgb: string): string => {
  if (!rgb || !rgb.startsWith("rgb")) return rgb;
  const rgba = rgb.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*([\d.]+))?\)/);
  if (!rgba) return rgb;

  const r = parseInt(rgba[1]).toString(16).padStart(2, "0");
  const g = parseInt(rgba[2]).toString(16).padStart(2, "0");
  const b = parseInt(rgba[3]).toString(16).padStart(2, "0");
  const a = rgba[4]
    ? Math.round(parseFloat(rgba[4]) * 255)
        .toString(16)
        .padStart(2, "0")
    : "";

  return `#${r}${g}${b}${a}`;
};

const refreshTagCache = async () => {
  if (userStore.id) {
    const { getTagList } = await import("@/api/incomeExpense");
    try {
      const result = await getTagList(userStore.id);
      billStore.tagList = result || [];
    } catch {
      billStore.tagList = [];
    }
  }
};

const handleColorChange = (color: string) => {
  formData.value.color = color;
};

const dialogVisible = ref(false);

watch(
  () => props.visible,
  (val) => {
    dialogVisible.value = val;
    if (val) {
      if (props.data) {
        formData.value = {
          id: props.data.id,
          userId: props.data.userId,
          name: props.data.name,
          color: props.data.color,
          sort: props.data.sort
        };
      } else {
        formData.value = {
          userId: userStore.id,
          name: "",
          color: "#409EFF",
          sort: 0
        };
      }
    }
  }
);

watch(dialogVisible, (val) => {
  emit("update:visible", val);
});

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  const submitData = {
    ...formData.value,
    color: rgbToHex(formData.value.color)
  };

  loading.value = true;
  try {
    let newTagId: number | undefined;
    if (submitData.id) {
      await updateUserTag(submitData);
      ElMessage.success(t("tag.pureUpdateSuccess"));
      newTagId = submitData.id;
    } else {
      newTagId = await createUserTag(submitData);
      ElMessage.success(t("tag.pureCreateSuccess"));
    }
    await refreshTagCache();
    dialogVisible.value = false;
    emit("success", newTagId);
  } catch (error: any) {
    ElMessage.error(error?.message || t("tag.pureOperationFail"));
  } finally {
    loading.value = false;
  }
};

const handleClose = () => {
  dialogVisible.value = false;
  formRef.value?.resetFields();
};
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    :title="data ? t('tag.pureEdit') : t('tag.pureAddTag')"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item :label="t('tag.pureTagName')" prop="name">
        <el-input
          v-model="formData.name"
          :placeholder="t('tag.pureTagNamePlaceholder')"
          maxlength="20"
          show-word-limit
        />
      </el-form-item>
      <el-form-item :label="t('tag.pureColor')" prop="color">
        <el-color-picker
          v-model="formData.color"
          show-alpha
          :predefine="colorPresets"
          @change="handleColorChange"
        />
        <span class="color-input-hint">{{ t("tag.pureColorHint") }}</span>
      </el-form-item>
      <el-form-item :label="t('tag.purePreview')">
        <el-tag :color="formData.color" :style="{ color: '#fff' }">
          {{ formData.name || t("tag.pureTagNamePlaceholder") }}
        </el-tag>
      </el-form-item>
      <el-form-item :label="t('tag.pureSort')" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" :max="9999" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">{{ t("tag.pureCancel") }}</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ t("tag.pureTagConfirm") }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
.color-input-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}
</style>