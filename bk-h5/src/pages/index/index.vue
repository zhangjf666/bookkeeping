<template>
  <view class="content">
	  <!-- 顶部状态栏 -->
	  <view class="stateBar"></view>
    <view class="summary">
      <view class="expense">
        <view class="expense-title">本月支出 (元)</view>
        <view class="expense-sum">
          <text>￥</text>
          <u-count-to ref="countExpense" duration="1300" font-size="80" color="#fff" :end-val="summary.expenseAmount" autoplay decimals="2" separator=","></u-count-to>
        </view>
      </view>
      <view class="income">
        <view class="income-title">本月收入</view>
        <view class="income-sum">
          <text>￥</text>
          <u-count-to ref="countIncome" duration="1300" font-size="28" color="#fff" :end-val="summary.incomeAmount" autoplay decimals="2" separator=","></u-count-to>
        </view>
      </view>
    </view>
    <view class="btn-add">
      <button class="primary" type="primary" @tap="goRecordPage">记一笔</button>
    </view>
    <view class="recently">
      <view class="title">近三日新增账单 {{ summary.incomeExpenseList.length }} 笔</view>
      <!-- 近3日账单 -->
      <record-list v-if="summary.incomeExpenseList" :list="summary.incomeExpenseList" :sortType="0"></record-list>
    </view>
    <u-toast ref="uToast" />
  </view>
</template>

<script>
import { mapState, mapMutations, mapGetters } from "vuex";
import { userConfig,userAccountBook,userClassify,userRemark } from "@/api/user.js";
import recordList from "@/my-components/recordList.vue";

export default {
  components: {
    recordList
  },
  data() {
    return {
      expenseTotal: 0,
      incomeTotal: 0,
      incomeExpenseList: []
    };
  },
  async onLoad() {
    //等待登录成功	
    await this.$onLaunched;
    //加载用户信息
    if(this.loginFlag){
      this.getUserConfig();
      this.getUserClassify();
      this.getUserAccountBook();
      this.updateSummary();
      this.getUserRemark();
    }
  },
  onShow() {
    if(this.loginFlag){
      if(Object.keys(this.userConfig).length == 0) {
        this.getUserConfig();
      }
      if(this.classify.length == 0) {
        this.getUserClassify();
      }
      if(this.accountBook.length == 0) {
        this.getUserAccountBook();
      }
    }
  },
  methods: {
    ...mapMutations(['setUserConfig','setClassify','setAccountBook','updateSummary', 'setUserRemark']),
    // 获取用户配置
    getUserConfig() {
      userConfig({userId: this.user.id}).then((data) => {
        var config = {};
        data.forEach(item => {
          config[item.name] = item;
        })
        this.setUserConfig(config);
      })
    },
    // 获取用户分类
    getUserClassify() {
      userClassify({userId: this.user.id}).then((data) => {
        this.setClassify(data);
      })
    },
    // 获取用户账本
    getUserAccountBook() {
      userAccountBook({userId: this.user.id}).then((data) => {
        this.setAccountBook(data);
      })
    },
    // 获取用户常用备注
    getUserRemark() {
      userRemark({userId: this.user.id}).then((data) => {
        this.setUserRemark(data);
      })
    },
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
    }
  },
  computed: {
    ...mapState(['user', 'summary','userConfig','classify','accountBook']),
    ...mapGetters(["loginFlag"]),
    recordSize() {
      return this.incomeExpenseList.length;
    },
    incomeShow() {
      return '￥' + this.incomeTotal;
    },
    expenseShow() {
      return '￥' + this.expenseTotal;
    }
  },
  watch: {
    summary() {
      
    }
  }
};
</script>

<style lang="scss" scoped>
$mColor: #d83d34;
$sColor: #00a151;
.content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.stateBar {
	height: var(--status-bar-height);  
	width: 100%;
	background-color: #252569;
}
.summary {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 440rpx;
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
    margin-top: 120rpx;
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
    margin-bottom: 10rpx;
  }
}
</style>
