<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { showNotify, showConfirmDialog } from "vant";
import { useUserStoreHook } from "@/store/modules/user";
import { useBillStoreHook } from "@/store/modules/bill";
import { removeToken } from "@/utils/auth";

defineOptions({
  name: "MobileUser"
});

const router = useRouter();
const userStore = useUserStoreHook();
const billStore = useBillStoreHook();

const loading = ref(false);

const userId = computed(() => userStore.id);
const nickname = computed(() => userStore.nickname || userStore.username);
const avatar = computed(() => userStore.avatar || "");

// 菜单项
const menuItems = [
  { icon: "notes-o", title: "账本管理", path: "/settings/account-book" },
  { icon: "apps-o", title: "分类管理", path: "/settings/classify" },
  { icon: "label-o", title: "标签管理", path: "/settings/tag" },
  { icon: "comment-o", title: "备注管理", path: "/settings/remark" },
  { icon: "setting-o", title: "通用设置", path: "/settings/common" }
];

// 退出登录
const handleLogout = async () => {
  try {
    await showConfirmDialog({
      title: "退出登录",
      message: "确定要退出登录吗？"
    });
    removeToken();
    userStore.logOut();
    router.replace("/login");
  } catch {
    // 取消
  }
};

// 跳转
const goPath = (path: string) => {
  router.push(path);
};

onMounted(() => {
  if (!userStore.id) {
    router.replace("/login");
  }
});
</script>

<template>
  <div class="mobile-user">
    <!-- 用户信息 -->
    <div class="user-card">
      <div class="avatar">
        <van-image
          v-if="avatar"
          round
          width="60"
          height="60"
          :src="avatar"
        />
        <van-icon v-else name="user-o" size="40" />
      </div>
      <div class="info">
        <div class="nickname">{{ nickname }}</div>
        <div class="account-book" v-if="billStore.currentAccountBook">
          当前账本：{{ billStore.currentAccountBook.name }}
        </div>
      </div>
    </div>

    <!-- 功能菜单 -->
    <van-cell-group inset class="menu-group">
      <van-cell
        v-for="item in menuItems"
        :key="item.path"
        :title="item.title"
        :icon="item.icon"
        is-link
        @click="goPath(item.path)"
      />
    </van-cell-group>

    <!-- 退出登录 -->
    <div class="logout-section">
      <van-button type="danger" block plain @click="handleLogout">
        退出登录
      </van-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.mobile-user {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px 20px;
  color: #fff;

  .avatar {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
  }

  .info {
    .nickname {
      font-size: 18px;
      font-weight: 500;
      margin-bottom: 5px;
    }

    .account-book {
      font-size: 12px;
      opacity: 0.8;
    }
  }
}

.menu-group {
  margin-top: 15px;
}

.logout-section {
  padding: 30px 20px;
}
</style>