<template>
	<view class="content">
		<u-navbar
			class="navbar"
			:background="{ backgroundColor: '#D83D34' }"
			back-icon-color="#fff"
			back-icon-size="38"
			back-text="注册"
            z-index="0"
			:back-text-style="{ color: '#fff', fontSize: '34rpx', marginLeft: '10rpx' }"
		></u-navbar>
		<view class="input-group">
			<view class="input-row border">
				<text class="title">账号：</text>
				<m-input type="text" focus clearable v-model="username" placeholder="请输入账号"></m-input>
			</view>
			<view class="input-row border">
				<text class="title">密码：</text>
				<m-input type="password" displayable v-model="password" placeholder="请输入密码"></m-input>
			</view>
			<view class="input-row">
				<text class="title">确认密码：</text>
				<m-input type="password" displayable v-model="confirmPassword" placeholder="请确认密码"></m-input>
			</view>
		</view>
		<view class="btn-row">
			<button type="primary" class="primary" @tap="register">注册并登录</button>
		</view>
	</view>
</template>

<script>
	import mInput from '@/my-components/m-input/m-input.vue';
    import { registerUser, login } from '@/api/auth.js';
	import { mapMutations } from 'vuex';

	export default {
		components: {
			mInput
		},
		data() {
			return {
				username: '',
				password: '',
				confirmPassword: ''
			}
		},
		methods: {
			...mapMutations(['setToken','setUser']),
			register() {
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
				if (this.password !== this.confirmPassword) {
					uni.showToast({
						icon: 'none',
						title: '两次密码输入不一致'
					});
					return;
				}

				const registerdata = {
					username: this.username,
					password: this.password,
                    repeatPassword: this.confirmPassword
				}
                //注册
                registerUser(registerdata).then(data => {
                    uni.showToast({
								title: '注册成功'
							});
                    //注册成功后登录
                    const logindata = {
                        username: this.username,
                        password: this.password
                    }
                    login(logindata).then(data => {
						this.setToken(data.token);
						this.setUser(data.user);
                        uni.setStorageSync('token', data.token);
                        uni.setStorageSync('user', data.user);
                        uni.reLaunch({
                            url: '../index/index',
                        });
                    })
                })
            },
            login() {
                //注册成功后登录
                const logindata = {
                    username: this.username,
                    password: this.password
                }
                login(logindata).then(data => {
                    uni.setStorageSync('token', data.token)
                    uni.setStorageSync('user', data.user)
                    uni.reLaunch({
                        url: '../index/index',
                    });
                })
            }
		}
	}
</script>

<style lang="scss" scoped>
@import "page.css";
.navbar {
	/deep/ .u-border-bottom:after {
		border-bottom-width: 0px;
	}
}
</style>
