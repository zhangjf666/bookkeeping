<template>
  <view class="content">
    <view class="summary">
      <view class="expense">
        <view class="expense-title">本月支出 (元)</view>
        <view class="expense-sum">￥1400005.2546</view>
      </view>
      <view class="income">
        <view class="income-title">本月收入</view>
        <view class="income-sum">￥15000.2546</view>
      </view>
    </view>
    <view class="btn-add">
      <button class="primary" type="primary" @tap="goRecordPage">记一笔</button>
    </view>
    <view class="recently">
      <view class="title">近3日新增账单 {{ record }} 笔</view>
      <!-- 近3日账单 -->
      <view class="record"> </view>
    </view>
    <u-toast ref="uToast" />
  </view>
</template>

<script>
import { mapState, mapMutations, mapGetters } from "vuex";
import { userConfig } from "@/api/user.js";

export default {
  data() {
    return {
      record: 5,
    };
  },
  onLoad() {
    
  },
  methods: {
    ...mapMutations(['setUserConfig']),
    // 获取用户配置
    getUserConfig() {
      userConfig({userId: this.user.id}).then((data) => {
        this.setUserConfig(data);
      })
    },
    // 获取用户分类
    // 跳转记录页面
    goRecordPage() {
      if (!this.loginFlag) {
        this.$refs.uToast.show({
          title: "请先登录",
          duration: 1000,
          position: "bottom",
        });
        return;
      }
      uni.navigateTo({
        url: `../record/record`,
      });
    },
  },
  computed: {
    ...mapState(['user']),
    ...mapGetters(["loginFlag"]),
  },
};
</script>

<style lang="scss" scoped>
$mColor: #d83d34;
.content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.summary {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 480rpx;
  background-color: #252569;
  padding: 30rpx;
  .income {
    display: flex;
    margin: 20rpx;
    .income-title {
      color: gainsboro;
      font-size: 28rpx;
    }
    .income-sum {
      margin-left: 20rpx;
      font-size: 28rpx;
      color: white;
    }
  }
  .expense {
    display: flex;
    flex-direction: column;
    margin-top: 160rpx;
    .expense-title {
      color: gainsboro;
      font-size: 32rpx;
      padding: 20rpx;
    }
    .expense-sum {
      font-size: 80rpx;
      color: white;
    }
  }
}
.btn-add {
  display: flex;
  width: 100%;
  margin-top: 20rpx;
  padding: 20rpx;
}
button.primary {
  background-color: $mColor;
  border-radius: 4rpx;
  width: 100%;
}
.recently {
  display: flex;
  width: 100%;
  flex-direction: column;
  padding: 30rpx;
  .title {
    font-size: 26rpx;
    font-weight: bold;
  }
  .record {
  }
}
</style>
