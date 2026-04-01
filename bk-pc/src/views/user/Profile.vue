<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
import type { UploadInstance } from "element-plus";
import { getUserInfo, updateUserInfo, uploadAvatar } from "@/api/user";
import type { BookkeepingUser } from "@/api/user";
import ChangePasswordDialog from "./ChangePasswordDialog.vue";
import { useUserStoreHook } from "@/store/modules/user";
import { Camera } from "@element-plus/icons-vue";

defineOptions({
  name: "Profile"
});

const { t } = useI18n();
const userStore = useUserStoreHook();

const BASE_URL = import.meta.env.VITE_BASE_URL as string;

const loading = ref(false);
const saving = ref(false);
const userInfo = ref<BookkeepingUser>({
  id: 0,
  username: "",
  nickName: "",
  gender: "",
  avatar: "",
  email: "",
  mobilePhone: "",
  createTime: "",
  updateTime: ""
});

const editForm = ref({
  nickName: "",
  gender: "" as "MALE" | "FEMALE" | "",
  email: "",
  mobilePhone: ""
});

const isEditing = ref(false);
const showPasswordDialog = ref(false);
const avatarInputRef = ref<UploadInstance>();
const pendingAvatar = ref("");
const pendingAvatarUrl = ref("");

const avatarUrl = computed(() => {
  if (!userInfo.value.avatar) return "";
  if (userInfo.value.avatar.startsWith("http")) return userInfo.value.avatar;
  return `${BASE_URL}${userInfo.value.avatar}`;
});

const editedAvatarUrl = computed(() => {
  if (!pendingAvatarUrl.value) return avatarUrl.value;
  if (pendingAvatarUrl.value.startsWith("http")) return pendingAvatarUrl.value;
  return `${BASE_URL}${pendingAvatarUrl.value}`;
});

const validateEmail = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback();
  } else {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(value)) {
      callback(new Error(t("profile.pureEmailFormatError")));
    } else {
      callback();
    }
  }
};

const validatePhone = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback();
  } else {
    const phoneRegex = /^\+?\d{1,3}[\s-]?\d{6,14}$/;
    if (!phoneRegex.test(value)) {
      callback(new Error(t("profile.purePhoneFormatError")));
    } else {
      callback();
    }
  }
};

const formRules = {
  nickName: [
    {
      required: true,
      message: t("profile.pureNicknameRequired"),
      trigger: "blur"
    },
    { max: 20, message: t("profile.pureNicknameLength"), trigger: "blur" }
  ],
  email: [{ validator: validateEmail, trigger: "blur" }],
  phone: [{ validator: validatePhone, trigger: "blur" }]
};

const loadUserInfo = async () => {
  loading.value = true;
  try {
    const result = await getUserInfo();
    userInfo.value = result;
    editForm.value = {
      nickName: result.nickName || "",
      gender: result.gender || "",
      email: result.email || "",
      mobilePhone: result.mobilePhone || ""
    };
  } catch {
    ElMessage.error(t("profile.pureLoadUserInfoFailed"));
  } finally {
    loading.value = false;
  }
};

const handleEdit = () => {
  editForm.value = {
    nickName: userInfo.value.nickName || "",
    gender: userInfo.value.gender || "",
    email: userInfo.value.email || "",
    mobilePhone: userInfo.value.mobilePhone || ""
  };
  isEditing.value = true;
};

const handleCancel = () => {
  pendingAvatar.value = "";
  pendingAvatarUrl.value = "";
  isEditing.value = false;
};

const handleSave = async () => {
  if (!editForm.value.nickName.trim()) {
    ElMessage.error(t("profile.pureNicknameRequired"));
    return;
  }

  if (editForm.value.email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(editForm.value.email)) {
      ElMessage.error(t("profile.pureEmailFormatError"));
      return;
    }
  }

  if (editForm.value.mobilePhone) {
    const phoneRegex = /^\+?\d{1,3}[\s-]?\d{6,14}$/;
    if (!phoneRegex.test(editForm.value.mobilePhone)) {
      ElMessage.error(t("profile.purePhoneFormatError"));
      return;
    }
  }

  saving.value = true;
  try {
    const updateData: Partial<BookkeepingUser> = {
      id: userInfo.value.id,
      nickName: editForm.value.nickName,
      gender: editForm.value.gender,
      email: editForm.value.email,
      mobilePhone: editForm.value.mobilePhone
    };
    if (pendingAvatar.value) {
      updateData.avatar = pendingAvatar.value;
    }
    const result = await updateUserInfo(updateData);
    userInfo.value = result;
    const avatarUrl = result.avatar.startsWith("http")
      ? result.avatar
      : `${BASE_URL}${result.avatar}`;
    userStore.SET_NICKNAME(result.nickName);
    userStore.SET_AVATAR(avatarUrl);
    pendingAvatar.value = "";
    pendingAvatarUrl.value = "";
    isEditing.value = false;
    ElMessage.success(t("profile.pureUpdateSuccess"));
  } catch {
    ElMessage.error(t("profile.pureUpdateFailed"));
  } finally {
    saving.value = false;
  }
};

const handleAvatarChange = (uploadFile: any) => {
  const file = uploadFile.raw;
  if (!file) return;

  if (!file.type.startsWith("image/")) {
    ElMessage.error(t("profile.pureInvalidImageType"));
    return;
  }

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error(t("profile.pureImageTooLarge"));
    return;
  }

  const formData = new FormData();
  formData.append("file", file);

  uploadAvatar(formData)
    .then(result => {
      pendingAvatar.value = result.fileName;
      pendingAvatarUrl.value = result.url;
      userInfo.value.avatar = result.url;
      ElMessage.success(t("profile.pureAvatarUpdateSuccess"));
    })
    .catch(() => {
      ElMessage.error(t("profile.pureAvatarUpdateFailed"));
    })
    .finally(() => {
      if (avatarInputRef.value) {
        avatarInputRef.value.clearFiles();
      }
    });
};

const handlePasswordSuccess = () => {
  showPasswordDialog.value = false;
};

onMounted(() => {
  loadUserInfo();
});
</script>

<template>
  <div class="profile-container">
    <el-card v-loading="loading" shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ t("profile.pureTitle") }}</span>
        </div>
      </template>

      <div class="profile-content">
        <div class="avatar-section">
          <el-upload
            v-if="isEditing"
            ref="avatarInputRef"
            class="avatar-uploader"
            :show-file-list="false"
            :auto-upload="false"
            accept="image/*"
            @change="(uploadFile: any) => handleAvatarChange(uploadFile)"
          >
            <img :src="editedAvatarUrl" class="avatar" />
            <div class="avatar-hover">
              <el-icon><Camera /></el-icon>
            </div>
          </el-upload>
          <img v-else :src="avatarUrl" class="avatar" />
          <div v-if="isEditing" class="avatar-tip">
            {{ t("profile.pureClickToChangeAvatar") }}
          </div>
        </div>

        <el-divider />

        <el-form label-width="100px" class="profile-form">
          <el-form-item :label="t('profile.pureUsername')">
            <span>{{ userInfo.username }}</span>
          </el-form-item>

          <el-form-item :label="t('profile.purePassword')">
            <span>********</span>
            <el-button type="primary" link @click="showPasswordDialog = true">
              {{ t("profile.pureChangePassword") }}
            </el-button>
          </el-form-item>

          <el-form-item :label="t('profile.pureNickname')">
            <el-input
              v-if="isEditing"
              v-model="editForm.nickName"
              :placeholder="t('profile.pureNicknamePlaceholder')"
              maxlength="20"
            />
            <span v-else>{{ userInfo.nickName || "-" }}</span>
          </el-form-item>

          <el-form-item :label="t('profile.pureGender')">
            <el-radio-group v-if="isEditing" v-model="editForm.gender">
              <el-radio-button value="MALE">{{
                t("profile.pureMale")
              }}</el-radio-button>
              <el-radio-button value="FEMALE">{{
                t("profile.pureFemale")
              }}</el-radio-button>
            </el-radio-group>
            <span v-else>
              {{
                userInfo.gender === "MALE"
                  ? t("profile.pureMale")
                  : userInfo.gender === "FEMALE"
                    ? t("profile.pureFemale")
                    : "-"
              }}
            </span>
          </el-form-item>

          <el-form-item :label="t('profile.pureEmail')">
            <el-input
              v-if="isEditing"
              v-model="editForm.email"
              :placeholder="t('profile.pureEmailPlaceholder')"
            />
            <span v-else>{{ userInfo.email || "-" }}</span>
          </el-form-item>

          <el-form-item :label="t('profile.purePhone')">
            <el-input
              v-if="isEditing"
              v-model="editForm.mobilePhone"
              :placeholder="t('profile.purePhonePlaceholder')"
            />
            <span v-else>{{ userInfo.mobilePhone || "-" }}</span>
          </el-form-item>
        </el-form>

        <div class="form-actions">
          <el-button v-if="!isEditing" type="primary" @click="handleEdit">
            {{ t("profile.pureEdit") }}
          </el-button>
          <template v-else>
            <el-button @click="handleCancel">
              {{ t("profile.pureCancel") }}
            </el-button>
            <el-button type="primary" :loading="saving" @click="handleSave">
              {{ t("profile.pureSave") }}
            </el-button>
          </template>
        </div>
      </div>
    </el-card>

    <ChangePasswordDialog
      v-model:visible="showPasswordDialog"
      @success="handlePasswordSuccess"
    />
  </div>
</template>

<style lang="scss" scoped>
.profile-container {
  max-width: 700px;
  padding: 16px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: 500;
}

.profile-content {
  padding: 0 16px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.avatar-uploader {
  position: relative;
  display: inline-block;
  cursor: pointer;

  .avatar {
    width: 100px;
    height: 100px;
    object-fit: cover;
    border: 2px solid #ebeef5;
    border-radius: 50%;
  }

  .avatar-hover {
    position: absolute;
    top: 0;
    left: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100px;
    height: 100px;
    font-size: 24px;
    color: #fff;
    background: rgb(0 0 0 / 50%);
    border-radius: 50%;
    opacity: 0;
    transition: opacity 0.3s;
  }

  &:hover .avatar-hover {
    opacity: 1;
  }
}

.avatar:not(.avatar-uploader .avatar) {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border: 2px solid #ebeef5;
  border-radius: 50%;
}

.avatar-tip {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
}

.profile-form {
  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #606266;
  }

  :deep(.el-form-item__content) {
    display: flex;
    align-items: center;
  }

  :deep(.el-input) {
    width: 250px;
  }

  :deep(.el-radio-group) {
    display: flex;
  }
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 32px;
}
</style>
