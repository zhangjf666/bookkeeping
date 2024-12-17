<template name="Mine">
	<view class="mine">
		<!-- 顶部状态栏 -->
		<view class="stateBar"></view>
		<!-- user info -->
		<view class="mine-user">
			<view class="user-info">
				<view class="user-ico"><text class="user-icon text-black iconfont icon-yonghu"></text></view>
				<view class="user-msg" v-if="loginFlag">{{userName}}</view>
				<view class="user-msg" v-else>登录立享手机电脑多端同步</view>
			</view>
			<view class="user-login" v-if="!loginFlag" style="font-size: 24rpx;padding: 6rpx;" @click="goLoginPage()">立即登录</view>
		</view>
		<u-cell-group v-if="loginFlag">
			<u-cell-item index="1" title="支出限额样式" @click="settingClick" :value="expenseType"></u-cell-item>
			<u-cell-item index="2" title="月默认支出限额设置" @click="settingClick" :value="monthlyExpenseLimit"></u-cell-item>
			<u-cell-item index="3" title="年默认支出限额设置" @click="settingClick" :value="yearlyExpenseLimit"></u-cell-item>
		</u-cell-group>
		<u-select v-model="expenseTypeSelectorShow"  mode="single-column" :list="expenseTypeSelectorList" @confirm="typeSelectorConfirm" :default-value="expenseTypeDefaultValue"></u-select>
		<u-popup v-model="expenseValueSelectorShow" mode="center" :negative-top="300" width="70%" height="20%" border-radius="14">
			<view class="expense-popup">
				<text class="title">{{expenseLimitTitle}}</text>
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
		<!-- 退出 -->
		<view class="btn-logout" v-if="loginFlag">
			<button type="primary" class="primary" @tap="doLogout">退出</button>
		</view>
	</view>
</template>

<script>
import { mapState, mapMutations, mapGetters } from 'vuex';
import { logout } from '@/api/auth.js';
import { formatNumber } from "@/utils/utils.js"

export default {
	name: 'Mine',
	components: {},
	data() {
		return {
			expenseTypeSelectorShow: false,
			expenseTypeSelectorList: [
				{
					value: '1',
					label: '不显示'
				},
				{
					value: '2',
					label: '显示月限额'
				},
				{
					value: '3',
					label: '显示年限额'
				}
			],
			expenseValue: '0.00',
			expenseValueType: '1',
			expenseValueSelectorShow: false,
			popupConfirmLoading: false,
			expenseLimitTitle: ''
		};
	},
	methods: {
		...mapMutations(['setToken','setUser', 'setUserConfigItem', 'updateSummary']),
		//到登录页面
		goLoginPage() {
			uni.navigateTo({
				url: '../login/login',
			});
		},
		//退出
		doLogout() {
			logout();
			this.setToken('');
			this.setUser('');
			uni.setStorageSync('token', null);
			uni.setStorageSync('user', null);
		},
		//设置点击
		settingClick(index) {
			if(index == '1'){
				//支出限额显示
				this.expenseTypeSelectorShow = true;
			} else {
				this.expenseValueType = index == '2' ? '1':'2';
				this.expenseLimitTitle = index == '2' ? '默认月支出限额' : '默认年支出限额'
				//默认支出金额显示
				this.expenseValue = '0.00';
				this.expenseValueSelectorShow = true;
			}
		},
		//支出限额显示设置确认
		typeSelectorConfirm(e) {
			this.setUserConfigItem({name: 'show_expense_limit', value: e[0].value});
			this.updateSummary();
			this.expenseTypeSelectorShow = false;
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
				//更新默认支出限额设置
				let configName = this.expenseValueType == '1' ? 'default_monthly_expense_limit' : 'default_yearly_expense_limit';
				this.setUserConfigItem({name: configName, value: this.expenseValue});
				this.updateSummary();
				this.expenseValueSelectorShow = false;
			} finally {
				this.popupConfirmLoading = false;
			}
		}
	},
	computed: {
		...mapState(['user', 'token', 'userConfig']),
		...mapGetters(['loginFlag']),
		userName() {
			return this.user.nickName;
		},
		expenseType() {
			return this.userConfig['show_expense_limit'].value == '1' ? '不显示' : this.userConfig['show_expense_limit'].value == '2' ? '显示月限额' : '显示年限额'
		},
		expenseTypeDefaultValue: {
			get() {
				return [this.userConfig['show_expense_limit'].value - 1];
			},
			cache: false
		},
		monthlyExpenseLimit() {
			return formatNumber(this.userConfig['default_monthly_expense_limit'].value) + '元';
		},
		yearlyExpenseLimit() {
			return formatNumber(this.userConfig['default_yearly_expense_limit'].value) + '元';
		}
	}
};
</script>

<style lang="scss" scoped>
$mColor: #d83d34;
.stateBar {
	height: var(--status-bar-height);  
	width: 100%;
	background-color: #252569;
}
.mine{
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: column;
	align-items: center;
}
.mine .mine-user{
	width: 100%;
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-top: 60rpx;
	margin-bottom: 30rpx;
}
.mine-user .user-info{
	display: flex;
	align-items: center;
	justify-content: center;
}
.mine-user .user-ico{
	margin-left: 45upx;
	width: 100upx;
	height: 100upx;
	border-radius: 50%;
	background-color: #c0c0c0;
	text-align: center;
	line-height: 110upx;
}
.user-info .user-icon{
	font-size: 36px;
	color: #666;
}
.mine-user .user-msg{
	margin-left: 25upx;
	font-size: 12px;
}
.mine-user .user-login{
	
	margin: 35upx;
	border: 1px solid #c0c0c0;
	border-radius: 10px;
}
.btn-logout {
	width: 100%;
	margin-top: 25px;
    padding: 30rpx;
	button.primary {
		background-color: #d83d34;
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
