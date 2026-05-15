<script setup lang="ts">
import { computed } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { showConfirmDialog } from "vant";
import { useUserStoreHook } from "@/store/modules/user";

defineOptions({
  name: "MobileUser"
});

const router = useRouter();
const { t } = useI18n();

// 用户信息
const userStore = useUserStoreHook();

const BASE_URL = import.meta.env.VITE_BASE_URL as string;

// 用户头像 - 处理相对路径和绝对路径
const userAvatar = computed(() => {
  const avatar = userStore.avatar;
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

// 用户昵称
const userNickname = computed(() => {
  return userStore.nickname || "-";
});

// 用户名
const username = computed(() => {
  return userStore.username || "-";
});

// 跳转到个人设置
const handleGoProfile = () => {
  router.push("/user/settings/profile");
};

// 跳转到账本管理
const handleGoAccountBook = () => {
  router.push("/settings/account-book");
};

// 跳转到分类管理
const handleGoClassify = () => {
  router.push("/settings/classify");
};

// 跳转到标签管理
const handleGoTag = () => {
  router.push("/settings/tag");
};

// 跳转到备注管理
const handleGoRemark = () => {
  router.push("/settings/remark");
};

// 跳转到常用设置
const handleGoCommonSettings = () => {
  router.push("/settings/common");
};

// 跳转到修改密码
const handleGoPassword = () => {
  router.push("/user/settings/password");
};

// 跳转到关于页面
const handleGoAbout = () => {
  router.push("/user/settings/about");
};

// 退出登录
const handleLogout = async () => {
  try {
    await showConfirmDialog({
      title: t("mobile.user.logoutConfirmTitle"),
      message: t("mobile.user.logoutConfirmMessage"),
      confirmButtonColor: "#d83d34"
    });

    // 清除用户信息并跳转到登录页
    userStore.logOut();
  } catch {
    // 取消
  }
};
</script>

<template>
  <div class="user-page">
    <!-- 用户信息卡片 -->
    <div class="user-card" @click="handleGoProfile">
      <div class="avatar-section">
        <van-image round width="60" height="60" :src="userAvatar" />
      </div>
      <div class="info-section">
        <div class="nickname">
          <span class="label">{{ t("mobile.user.nickname") }}：</span>
          <span class="value">{{ userNickname }}</span>
        </div>
        <div class="username">
          <span class="label">{{ t("mobile.user.username") }}：</span>
          <span class="value">{{ username }}</span>
        </div>
      </div>
      <van-icon name="arrow" class="arrow-icon" />
    </div>

    <!-- 数据管理分组 -->
    <van-cell-group inset class="function-group">
      <van-cell
        :title="t('mobile.user.accountBook')"
        is-link
        @click="handleGoAccountBook"
      />
      <van-cell
        :title="t('mobile.user.classifyManage')"
        is-link
        @click="handleGoClassify"
      />
      <van-cell
        :title="t('mobile.user.tagManage')"
        is-link
        @click="handleGoTag"
      />
      <van-cell
        :title="t('mobile.user.remarkManage')"
        is-link
        @click="handleGoRemark"
      />
    </van-cell-group>

    <!-- 设置分组 -->
    <van-cell-group inset class="function-group">
      <van-cell
        :title="t('mobile.user.commonSettings')"
        is-link
        @click="handleGoCommonSettings"
      />
      <van-cell
        :title="t('mobile.user.changePassword')"
        is-link
        @click="handleGoPassword"
      />
    </van-cell-group>

    <!-- 其他分组 -->
    <van-cell-group inset class="function-group">
      <van-cell
        :title="t('mobile.user.about')"
        is-link
        @click="handleGoAbout"
      />
      <van-cell
        :title="t('mobile.user.logout')"
        title-style="color: #d83d34;"
        @click="handleLogout"
      />
    </van-cell-group>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mobile/variables.scss" as *;

.user-page {
  background-color: $color-background;
}

.user-card {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 20px 16px;
  margin-bottom: 12px;
  cursor: pointer;
  background-color: $color-card;

  &:active {
    background-color: #f5f5f5;
  }
}

.avatar-section {
  flex-shrink: 0;
}

.info-section {
  flex: 1;
  min-width: 0;

  .nickname,
  .username {
    display: flex;
    gap: 4px;
    align-items: center;
    font-size: 14px;
    line-height: 1.5;

    .label {
      flex-shrink: 0;
      color: $color-text-secondary;
    }

    .value {
      overflow: hidden;
      text-overflow: ellipsis;
      color: $color-text-primary;
      white-space: nowrap;
    }
  }

  .username {
    margin-top: 4px;
  }
}

.arrow-icon {
  flex-shrink: 0;
  color: $color-text-secondary;
}

.function-group {
  margin: 12px 0;

  &:first-of-type {
    margin-top: 0;
  }
}
</style>
