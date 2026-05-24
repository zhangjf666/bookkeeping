<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from "vue";
import { useI18n } from "vue-i18n";
import { showError } from "@/utils/mobile/message";
import { useUserStoreHook } from "@/store/modules/user";
import { getUserInfo, updateUserInfo, uploadAvatar } from "@/api/user";
import type { BookkeepingUser } from "@/api/user";

defineOptions({
  name: "MobileUserProfile"
});

const { t } = useI18n();
const userStore = useUserStoreHook();

const BASE_URL = import.meta.env.VITE_BASE_URL as string;

// 用户信息
const userInfo = ref<BookkeepingUser | null>(null);

// 表单数据
const formData = ref({
  nickname: "",
  gender: "" as "MALE" | "FEMALE" | "",
  email: "",
  mobilePhone: ""
});

// 弹窗状态
const showNicknameEditor = ref(false);
const showGenderPicker = ref(false);
const showEmailEditor = ref(false);
const showPhoneEditor = ref(false);

// 编辑临时数据
const editNickname = ref("");
const editEmail = ref("");
const editPhone = ref("");

// 加载状态
const loading = ref(false);
const saving = ref(false);

// 头像上传
const fileInput = ref<HTMLInputElement | null>(null);
const avatarUploading = ref(false);

// 输入框引用
const nicknameInput = ref<{ $el: HTMLElement } | null>(null);
const emailInput = ref<{ $el: HTMLElement } | null>(null);
const phoneInput = ref<{ $el: HTMLElement } | null>(null);

// 聚焦输入框并将光标移到末尾
const focusInput = (inputRef: typeof nicknameInput, value: string) => {
  // 使用 setTimeout 确保 DOM 完全渲染后再聚焦
  setTimeout(() => {
    const input = inputRef.value?.$el?.querySelector(
      "input"
    ) as HTMLInputElement | null;
    if (input) {
      input.focus();
      // 将光标移动到内容末尾（email/tel/number 等类型不支持 setSelectionRange）
      const len = value.length;
      const supportedTypes = ["text", "search", "url", "tel", "password"];
      if (supportedTypes.includes(input.type) || !input.type) {
        input.setSelectionRange(len, len);
      }
    }
  }, 100);
};

// 性别选项
const genderOptions = computed(() => [
  { value: "MALE", label: t("mobile.user.male") },
  { value: "FEMALE", label: t("mobile.user.female") }
]);

// 性别显示文本
const genderText = computed(() => {
  const option = genderOptions.value.find(
    item => item.value === formData.value.gender
  );
  return option?.label || t("mobile.user.notSet");
});

// 用户头像 - 处理相对路径和绝对路径
const userAvatar = computed(() => {
  const avatar = userInfo.value?.avatar;
  if (!avatar) {
    return "https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg";
  }
  // 如果已经是完整URL，直接返回
  if (avatar.startsWith("http")) {
    return avatar;
  }
  // 否则拼接 BASE_URL
  return `${BASE_URL}${avatar}`;
});

// 加载用户信息
const loadUserInfo = async () => {
  loading.value = true;
  try {
    const data = await getUserInfo();
    userInfo.value = data;
    formData.value = {
      nickname: data.nickName || "",
      gender: data.gender || "",
      email: data.email || "",
      mobilePhone: data.mobilePhone || ""
    };
  } catch (error: any) {
    showError(error?.message || t("mobile.common.failed"));
  } finally {
    loading.value = false;
  }
};

// 点击头像上传
const handleAvatarClick = () => {
  fileInput.value?.click();
};

// 文件选择变化
const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  // 验证文件类型
  if (!file.type.startsWith("image/")) {
    showError(t("mobile.common.failed"));
    return;
  }

  // 验证文件大小（最大 2MB）
  if (file.size > 2 * 1024 * 1024) {
    showError(t("mobile.common.failed"));
    return;
  }

  avatarUploading.value = true;
  try {
    const formDataObj = new FormData();
    formDataObj.append("file", file);
    const result = await uploadAvatar(formDataObj);

    // 更新用户信息到数据库
    const updatedUser = await updateUserInfo({ avatar: result.fileName });

    // 更新本地 userInfo
    userInfo.value = updatedUser;

    // 更新 store，使用返回的 avatar 字段
    userStore.SET_AVATAR(updatedUser.avatar);
  } catch (error: any) {
    showError(error?.message || t("mobile.common.failed"));
  } finally {
    avatarUploading.value = false;
    // 清空文件输入
    target.value = "";
  }
};

// 打开昵称编辑器
const openNicknameEditor = () => {
  editNickname.value = formData.value.nickname;
  showNicknameEditor.value = true;
  focusInput(nicknameInput, editNickname.value);
};

// 昵称弹窗关闭处理
const handleNicknameBeforeClose = (action: string) => {
  if (action === "confirm") {
    if (!editNickname.value.trim()) {
      showError(t("mobile.user.nicknamePlaceholder"));
      return false; // 返回 false 阻止关闭
    }
    saving.value = true;
    updateUserInfo({ nickName: editNickname.value.trim() })
      .then(() => {
        formData.value.nickname = editNickname.value.trim();
        userStore.SET_NICKNAME(editNickname.value.trim());
        showNicknameEditor.value = false; // 手动关闭
      })
      .catch((error: any) => {
        showError(error?.message || t("mobile.common.failed"));
      })
      .finally(() => {
        saving.value = false;
      });
    return false; // 阻止自动关闭，手动控制
  }
  return true; // 其他操作允许关闭
};

// 选择性别
const handleGenderSelect = async (value: "MALE" | "FEMALE" | "") => {
  showGenderPicker.value = false;
  if (value === formData.value.gender) return;

  saving.value = true;
  try {
    await updateUserInfo({ gender: value });
    formData.value.gender = value;
  } catch (error: any) {
    showError(error?.message || t("mobile.common.failed"));
  } finally {
    saving.value = false;
  }
};

// 打开邮箱编辑器
const openEmailEditor = () => {
  editEmail.value = formData.value.email;
  showEmailEditor.value = true;
  focusInput(emailInput, editEmail.value);
};

// 邮箱弹窗关闭处理
const handleEmailBeforeClose = (action: string) => {
  if (action === "confirm") {
    saving.value = true;
    updateUserInfo({ email: editEmail.value.trim() })
      .then(() => {
        formData.value.email = editEmail.value.trim();
        showEmailEditor.value = false;
      })
      .catch((error: any) => {
        showError(error?.message || t("mobile.common.failed"));
      })
      .finally(() => {
        saving.value = false;
      });
    return false;
  }
  return true;
};

// 打开手机编辑器
const openPhoneEditor = () => {
  editPhone.value = formData.value.mobilePhone;
  showPhoneEditor.value = true;
  focusInput(phoneInput, editPhone.value);
};

// 手机弹窗关闭处理
const handlePhoneBeforeClose = (action: string) => {
  if (action === "confirm") {
    saving.value = true;
    updateUserInfo({ mobilePhone: editPhone.value.trim() })
      .then(() => {
        formData.value.mobilePhone = editPhone.value.trim();
        showPhoneEditor.value = false;
      })
      .catch((error: any) => {
        showError(error?.message || t("mobile.common.failed"));
      })
      .finally(() => {
        saving.value = false;
      });
    return false;
  }
  return true;
};

onMounted(() => {
  loadUserInfo();
});
</script>

<template>
  <div class="profile-page">
    <!-- 头像上传 -->
    <div class="avatar-section" @click="handleAvatarClick">
      <van-image round width="80" height="80" :src="userAvatar" />
      <div class="upload-hint">{{ t("mobile.user.clickToUpload") }}</div>
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        style="display: none"
        @change="handleFileChange"
      />
    </div>

    <!-- 表单区域 -->
    <van-cell-group inset class="form-group">
      <!-- 用户名（只读） -->
      <van-cell :title="t('mobile.user.username')">
        <template #value>
          <span class="readonly-value">{{ userInfo?.username || "-" }}</span>
        </template>
      </van-cell>

      <!-- 昵称 -->
      <van-cell
        :title="t('mobile.user.nickname')"
        :value="formData.nickname || t('mobile.user.notSet')"
        is-link
        @click="openNicknameEditor"
      />

      <!-- 性别 -->
      <van-cell
        :title="t('mobile.user.gender')"
        :value="genderText"
        is-link
        @click="showGenderPicker = true"
      />

      <!-- 邮箱 -->
      <van-cell
        :title="t('mobile.user.email')"
        :value="formData.email || t('mobile.user.notSet')"
        is-link
        @click="openEmailEditor"
      />

      <!-- 手机 -->
      <van-cell
        :title="t('mobile.user.phone')"
        :value="formData.mobilePhone || t('mobile.user.notSet')"
        is-link
        @click="openPhoneEditor"
      />
    </van-cell-group>

    <!-- 昵称编辑弹窗 -->
    <van-dialog
      v-model:show="showNicknameEditor"
      show-confirm-button
      :confirm-button-text="t('mobile.common.confirm')"
      confirm-button-color="#d83d34"
      close-on-click-overlay
      :confirm-button-loading="saving"
      :before-close="handleNicknameBeforeClose"
    >
      <template #title>
        <div class="dialog-header">
          <span>{{ t("mobile.user.editNickname") }}</span>
          <van-icon
            name="cross"
            class="close-icon"
            @click="showNicknameEditor = false"
          />
        </div>
      </template>
      <div class="dialog-content">
        <van-field
          ref="nicknameInput"
          v-model="editNickname"
          :placeholder="t('mobile.user.nicknamePlaceholder')"
          maxlength="20"
          show-word-limit
        />
      </div>
    </van-dialog>

    <!-- 性别选择弹窗 -->
    <van-action-sheet
      v-model:show="showGenderPicker"
      :title="t('mobile.user.selectGender')"
    >
      <div class="gender-list">
        <van-cell
          v-for="item in genderOptions"
          :key="item.value"
          :title="item.label"
          clickable
          @click="handleGenderSelect(item.value as 'MALE' | 'FEMALE' | '')"
        >
          <template #right-icon>
            <van-icon
              v-if="formData.gender === item.value"
              name="success"
              color="#d83d34"
            />
          </template>
        </van-cell>
      </div>
    </van-action-sheet>

    <!-- 邮箱编辑弹窗 -->
    <van-dialog
      v-model:show="showEmailEditor"
      show-confirm-button
      :confirm-button-text="t('mobile.common.confirm')"
      confirm-button-color="#d83d34"
      close-on-click-overlay
      :confirm-button-loading="saving"
      :before-close="handleEmailBeforeClose"
    >
      <template #title>
        <div class="dialog-header">
          <span>{{ t("mobile.user.editEmail") }}</span>
          <van-icon
            name="cross"
            class="close-icon"
            @click="showEmailEditor = false"
          />
        </div>
      </template>
      <div class="dialog-content">
        <van-field
          ref="emailInput"
          v-model="editEmail"
          type="email"
          :placeholder="t('mobile.user.emailPlaceholder')"
        />
      </div>
    </van-dialog>

    <!-- 手机编辑弹窗 -->
    <van-dialog
      v-model:show="showPhoneEditor"
      show-confirm-button
      :confirm-button-text="t('mobile.common.confirm')"
      confirm-button-color="#d83d34"
      close-on-click-overlay
      :confirm-button-loading="saving"
      :before-close="handlePhoneBeforeClose"
    >
      <template #title>
        <div class="dialog-header">
          <span>{{ t("mobile.user.editPhone") }}</span>
          <van-icon
            name="cross"
            class="close-icon"
            @click="showPhoneEditor = false"
          />
        </div>
      </template>
      <div class="dialog-content">
        <van-field
          ref="phoneInput"
          v-model="editPhone"
          type="tel"
          :placeholder="t('mobile.user.phonePlaceholder')"
        />
      </div>
    </van-dialog>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.profile-page {
  min-height: 100vh;
  background-color: $color-background;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  margin-bottom: 12px;
  cursor: pointer;
  background-color: $color-card;

  &:active {
    background-color: #f5f5f5;
  }
}

.upload-hint {
  font-size: 12px;
  color: $color-text-secondary;
}

.form-group {
  margin: 0;
}

.readonly-value {
  color: $color-text-secondary;
}

.gender-list {
  padding-bottom: env(safe-area-inset-bottom);
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 0 16px;

  span {
    flex: 1;
    text-align: left;
  }

  .close-icon {
    font-size: 18px;
    color: #969799;
    cursor: pointer;
  }
}

.dialog-content {
  padding: 16px;

  :deep(.van-field) {
    padding: 10px 12px;
    border: 1px solid #ebedf0;
    border-radius: 4px;

    &:focus-within {
      border-color: #d83d34;
    }
  }
}
</style>
