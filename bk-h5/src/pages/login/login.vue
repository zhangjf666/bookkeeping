<template>
	<view class="content">
		<u-navbar
			class="navbar"
			:background="{ backgroundColor: '#efeff4' }"
			back-icon-color="#000"
			back-icon-size="38"
			back-text="登录"
            z-index="0"
			:back-text-style="{ color: '#000', fontSize: '30rpx', marginLeft: '10rpx' }"
		></u-navbar>
		<view class="login-type">
			<view v-for="(item,index) in loginTypeList" :key="index" @click="loginType = index" :class="{act: loginType === index}"
			 class="login-type-btn">{{item}}</view>
		</view>
		<view class="input-group" v-if="loginType === 0">
			<view class="input-row border">
				<text class="title">手机：</text>
				<m-input class="m-input" type="text" clearable focus v-model="mobile" placeholder="请输入手机号码"></m-input>
			</view>
			<view class="input-row">
				<text class="title">验证码：</text>
				<m-input type="text" v-model="code" placeholder="请输入验证码"></m-input>
				<view class="send-code-btn" @click="sendSmsCode">{{codeDuration ? codeDuration + 's' : '发送验证码' }}</view>
			</view>
		</view>
		<view class="input-group" v-else>
			<view class="input-row border">
				<text class="title">账号：</text>
				<m-input class="m-input" type="text" clearable focus v-model="username" placeholder="请输入账号"></m-input>
			</view>
			<view class="input-row border">
				<text class="title">密码：</text>
				<m-input type="password" displayable v-model="password" placeholder="请输入密码"></m-input>
			</view>
			<view v-if="needCaptcha" class="input-row">
				<text class="title">验证码：</text>
				<m-input type="text" v-model="captchaText" placeholder="请输入验证码"></m-input>
				<view class="send-code-btn captcha-view" @click="captcha('refreshCaptcha')">
					<i v-if="captchaing" class="uni-icon_toast uni-loading"></i>
					<img v-if="!captchaing" :src="captchaBase64" width="100%" height="100%"></img>
				</view>
			</view>
		</view>
		<view class="btn-row">
			<button type="primary" class="primary" :loading="loginBtnLoading" @tap="bindLogin">登录</button>
		</view>
		<view class="action-row">
			<navigator url="./reg">注册账号</navigator>
		</view>
	</view>
</template>

<script>
	import { mapState, mapMutations } from 'vuex';
	import mInput from '@/my-components/m-input/m-input.vue';
	import { getDeviceUUID } from '@/utils/utils.js';
	import { login } from '@/api/auth.js';

	let weixinAuthService;
	const captchaOptions = {
		deviceId: getDeviceUUID(),
		scene: 'login'
	}

	export default {
		components: {
			mInput
		},
		data() {
			return {
				platform: uni.getSystemInfoSync().platform,
				loginType: 1,
				loginTypeList: ['手机号登录', '密码登录'],
				mobile: '',
				code: '',
				username: '',
				password: '',
				positionTop: 0,
				isDevtools: false,
				codeDuration: 0,
				loginBtnLoading: false,
				hasAppleLogin: false,
				needCaptcha: uni.getStorageSync('needCaptcha'),
				captchaing: false,
				captchaBase64: '',
				captchaText: ''
			}
		},
		computed: {
			
		},
		onLoad() {
			
		},
		methods: {
			...mapMutations(['setToken','setUser','updateSummary']),
			initPosition() {
				/**
				 * 使用 absolute 定位，并且设置 bottom 值进行定位。软键盘弹出时，底部会因为窗口变化而被顶上来。
				 * 反向使用 top 进行定位，可以避免此问题。
				 */
				this.positionTop = uni.getSystemInfoSync().windowHeight - 100;
			},
			sendSmsCode() {
				if (this.codeDuration) {
					uni.showModal({
						content: `请在${this.codeDuration}秒后重试`,
						showCancel: false
					})
				}
				if (!/^1\d{10}$/.test(this.mobile)) {
					uni.showModal({
						content: '手机号码填写错误',
						showCancel: false
					})
					return
				}
			},
			async loginByPwd() {
				/**
				 * 客户端对账号信息进行一些必要的校验。
				 * 实际开发中，根据业务需要进行处理，这里仅做示例。
				 */
				if (this.username.length < 3) {
					uni.showToast({
						icon: 'none',
						title: '账号最短为 3 个字符'
					});
					return;
				}
				if (this.password.length < 6) {
					uni.showToast({
						icon: 'none',
						title: '密码最短为 6 个字符'
					});
					return;
				}
				const data = {
					username: this.username,
					password: this.password,
					captcha: this.captchaText,
					...captchaOptions
				};
				this.loginBtnLoading = true
				//登录
                const logindata = {
                    username: this.username,
                    password: this.password
                }
                await login(logindata).then(data => {
					this.setToken(data.token);
					this.setUser(data.user);
					uni.setStorageSync('token', data.token);
					uni.setStorageSync('user', data.user);
					this.updateSummary();
					uni.reLaunch({
						url: '../index/index',
					})
				}).catch(() => {
					this.loginBtnLoading = false;
				});
			},
			loginBySms() {
				if (!/^1\d{10}$/.test(this.mobile)) {
					uni.showModal({
						content: '手机号码填写错误',
						showCancel: false
					})
					return
				}
				if (!/^\d{6}$/.test(this.code)) {
					uni.showModal({
						title: '验证码为6位纯数字',
						showCancel: false
					});
					return;
				}
				uni.showToast({
					icon: 'none',
					title: '暂不支持'
				});
			},
			bindLogin() {
				switch (this.loginType) {
					case 0:
						this.loginBySms()
						break;
					case 1:
						this.loginByPwd()
						break;
					default:
						break;
				}
			},
			async captcha(action, args) {
				if (this.captchaing) return;

				// 验证不loading
				this.captchaing = true;

				let {
					result: res
				} = await uniCloud.callFunction({
					name: 'user-center',
					data: {
						action,
						params: {
							...captchaOptions,
							...args
						}
					}
				})
				this.captchaing = false;
				if (res.code === 0) {
					this.captchaBase64 = res.captchaBase64
				} else {
					uni.showToast({
						icon: 'none',
						title: res.message,
						duration: 1000
					})
				}
				return res;
			}
		},
		onReady() {
			this.initPosition();
			// #ifdef MP-WEIXIN
			this.isDevtools = uni.getSystemInfoSync().platform === 'devtools';
			// #endif
		}
	}
</script>

<style lang="scss" scoped>
@import "page.css";
$mColor: #d83d34;
.navbar {
	/deep/ .u-border-bottom:after {
		border-bottom-width: 0px;
	}
}
.login-type {
    display: flex;
    justify-content: center;
}

.login-type-btn {
    line-height: 30px;
    margin: 0px 15px;
}

.login-type-btn.act {
    color: $mColor;
    border-bottom: solid 1px $mColor;
}

.send-code-btn {
    width: 100px;
    text-align: center;
    background-color: $mColor;
    color: #FFFFFF;
}

.action-row {
    display: flex;
    flex-direction: row;
    justify-content: center;
}

.action-row navigator {
    color: #a8a8a8;
    padding: 0 10px;
}

.captcha-view {
    line-height: 0;
    justify-content: center;
    align-items: center;
    display: flex;
    position: relative;
    background-color: #f3f3f3;
}
</style>
