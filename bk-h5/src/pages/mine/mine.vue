<template name="Mine">
	<view class="mine">
		<!-- user info -->
		<view class="mine-user">
			<view class="user-info">
				<view class="user-ico"><text class="user-icon text-black iconfont icon-yonghu"></text></view>
				<view class="user-msg" v-if="loginFlag">{{userName}}</view>
				<view class="user-msg" v-else>登录立享手机电脑多端同步</view>
			</view>
			<view class="user-login" v-if="!loginFlag" style="font-size: 24rpx;padding: 6rpx;" @click="goLoginPage()">立即登录</view>
		</view>
		
		<!-- 退出 -->
		<view class="btn-logout">
			<button type="primary" class="primary" v-if="loginFlag" @tap="doLogout">退出</button>
		</view>
	</view>
</template>

<script>
import { mapState, mapMutations, mapGetters } from 'vuex';
import { logout } from '@/api/auth.js';

export default {
	name: 'Mine',
	components: {},
	data() {
		return {

		};
	},
	methods: {
		...mapMutations(['setToken','setUser']),
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
			this.updateUserSonglist();
			uni.setStorageSync('token', null);
			uni.setStorageSync('user', null);
		}
	},
	computed: {
		...mapState(['user', 'token']),
		...mapGetters(['loginFlag']),
		userName() {
			return this.user.nickName;
		}
	}
};
</script>

<style lang="scss" scoped>
page {
    width: 100%;
    height: 100%;
    display: block;
}
.mine{
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: column;
	align-items: center;
	background-color: #efeff4;
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
    padding: 10px;
	button.primary {
		background-color: #d83d34;
	}
}
</style>
