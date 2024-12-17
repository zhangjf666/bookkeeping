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
      <view v-if="expenseLimitShow" class="expenseLimit">
        <text class="expenseLimit-limit">{{expenseLimit}}</text>
        <u-icon name="edit-pen" :size="28" @click="editExpenseLimit"></u-icon>
        <text class="expenseLimit-surplus" :style="surplusStyle()">{{expenseSurplus}}</text>
      </view>
    </view>
    <view class="btn-add">
      <button class="primary" type="primary" @tap="goRecordPage">记一笔</button>
    </view>
    <view class="recently">
      <view class="title">近三日新增账单 {{ summary.incomeExpenseList.length }} 笔</view>
      <!-- 近3日账单 -->
      <scroll-view scroll-y class="scroll">
        <record-list v-if="summary.incomeExpenseList" :list="summary.incomeExpenseList" :sortType="0"></record-list>
      </scroll-view>
    </view>
    <u-popup v-model="expenseValueSelectorShow" mode="center" :negative-top="300" width="70%" height="20%" border-radius="14">
			<view class="expense-popup">
        <text class="title">当前支出限额</text>
      </view>
      <view class="expense-popup">
				<text class="text">{{expenseValue}}元</text>
			</view>
			<view class="buttom">
				<u-button class="cancel" @click="popupCancel" type="error">取 消</u-button>
				<u-button class="confirm" @click="popupConfirm" :loading="popupConfirmLoading" type="error">确 定</u-button>
			</view>
			<u-keyboard ref="uKeyboard" :tooltip="false" :mask-close-able="false" :mask="false" @change="valChange" @backspace="backspace" v-model="expenseValueSelectorShow"></u-keyboard>
		</u-popup>
  </view>
</template>

<script>
import { mapState, mapMutations, mapGetters } from "vuex";
import { userConfig,userAccountBook,userClassify,userRemark,setAdditionalExpenseLimit } from "@/api/user.js";
import { querySummary } from "@/api/incomeExpense.js";
import recordList from "@/my-components/recordList.vue";
import { formatNumber } from "@/utils/utils.js"

export default {
  components: {
    recordList
  },
  data() {
    return {
      expenseTotal: 0,
      incomeTotal: 0,
      incomeExpenseList: [],
      expenseValueSelectorShow: false,
      expenseValue: '0.00',
			expenseValueSelectorShow: false,
			popupConfirmLoading: false
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
      this.getUserSummary();
      this.getUserRemark(); 
    }
  },
  onPullDownRefresh() {
    this.getUserSummary();
    uni.stopPullDownRefresh();
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
      if(this.userRemark.length == 0) {
        this.getUserRemark();
      }
    }
  },
  methods: {
    ...mapMutations(['setUserConfig','setClassify','setAccountBook','updateSummary', 'setUserRemark', 'setUserSummary']),
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
    // 获取摘要信息
    getUserSummary() {
      uni.showLoading({
        title: '加载中'
      });
      querySummary({userId: this.user.id}).then((data) => {
        this.setUserSummary(data)
      }).finally(() => {
        uni.hideLoading();
      });
    },
    // 跳转记录页面
    goRecordPage() {
      if (!this.loginFlag) {
        uni.showToast({
          icon: 'none',
          title: "请先登录",
          duration: 1000
        });
        return;
      }
      uni.navigateTo({
        url: `../record/record`,
      });
    },
    //编辑当前支出限额
    editExpenseLimit() {
      this.expenseValue = '0.00';
      this.expenseValueSelectorShow = true;
    },
    // 按键被点击(点击退格键不会触发此事件)
		valChange(val) {
			//判断是否是初始状态
			var isDefault = this.expenseValue == "0.00" ? true : false;
			if(isDefault) {
				if(val != '.') {
				this.expenseValue = String(val)
				} else {
				this.expenseValue = "0."
				}
			} else {
				this.expenseValue += "";
				if(val == '.' && this.expenseValue.indexOf('.') != -1) {
				return;
				} else {
				var index = this.expenseValue.indexOf('.')
				if(index != -1 && this.expenseValue.length - index > 2){
					return;
				}
				this.expenseValue += val;
				}
			}
		},
		// 退格键被点击
		backspace() {
			// 删除value的最后一个字符
			this.expenseValue += "";
			if(this.expenseValue.length == 1 || this.expenseValue == "0.") {
				this.expenseValue = '0.00';
			} else {
				this.expenseValue = this.expenseValue.substr(0, this.expenseValue.length - 1);
			}
		},
		popupCancel () {
			this.expenseValueSelectorShow =false;
		},
		popupConfirm () {
			this.popupConfirmLoading = true;
			try {
				//设置当前支出限额
        setAdditionalExpenseLimit({userId: this.user.id, type: this.userConfig['show_expense_limit'].value, expenseLimit: this.expenseValue}).then((data) => {
          this.updateSummary();
        })
				this.expenseValueSelectorShow =false;
			} finally {
				this.popupConfirmLoading = false;
			}
		},
    surplusStyle() {
      let percent = this.summary.expenseSurplus/this.summary.expenseLimit;
      return percent <= 0.1 ? 'color: #d83d34;' : percent <= 0.3 ? 'color: #00a151;' : 'color: #00a151;'
    }
  },
  computed: {
    ...mapState(['user', 'summary','userConfig','classify','accountBook','userRemark']),
    ...mapGetters(["loginFlag"]),
    recordSize() {
      return this.incomeExpenseList.length;
    },
    incomeShow() {
      return '￥' + this.incomeTotal;
    },
    expenseShow() {
      return '￥' + this.expenseTotal;
    },
    expenseLimitShow() {
      if(this.userConfig['show_expense_limit'] == null){
        return false;
      }
      return this.userConfig['show_expense_limit'].value != '1';
    },
    expenseLimit() {
      let type = this.userConfig['show_expense_limit'].value;
      if(type == '1'){
        return '';
      }
      let text = type == '2' ? '本月支出限额 ':'本年支出限额 ';
      text = text + formatNumber(this.summary.expenseLimit);
      return text;
    },
    expenseSurplus() {
      let type = this.userConfig['show_expense_limit'].value;
      if(type == '1'){
        return '';
      }
      return '剩余限额 ' + formatNumber(this.summary.expenseSurplus);
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
    margin-top: 80rpx;
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
  .expenseLimit {
    display: flex;
    font-size: 28rpx;
    margin: 0rpx 0rpx 0rpx 20rpx;
    color: gainsboro;
    .expenseLimit-limit {

    }
    .expenseLimit-surplus {
      margin-left: auto;
      margin-right: 20rpx;
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
  height: 100%;
  flex-direction: column;
  padding: 30rpx;
  .title {
    display: flex;
    font-size: 26rpx;
    font-weight: bold;
    margin-bottom: 10rpx;
  }
  .scroll {
    display: flex;
    width: 100%;
    height: 784rpx;
    // padding: 0rpx 30rpx 0 30rpx;
  }
}
.expense-popup {
	display: flex;
	align-items: center;
	justify-content: center;
  .title {
    margin-top: 30rpx;
  }
	.text {
		margin-top: 40rpx 0rpx 40rpx 0rpx;
		height: 100rpx;
		font-size: 60rpx;
		text-align: center;
		vertical-align: middle;
		color: rgb(92, 92, 92);
	}
}
.buttom {
    display: flex;
    padding: 30rpx;
    width: 100%;
    align-self: flex-end;
    .cancel {
		z-index: 10090;
        font-size: 26rpx;
        border-style: solid;
        border-width: 1px;
        border-color: $mColor;
        color: $mColor;
        background-color: #fff;
        width: 44%;
    }
    .confirm {
		z-index: 10090;
        background-color: $mColor;
        font-size: 26rpx;
        color: #fff;
        width: 44%;
    }
}
</style>
