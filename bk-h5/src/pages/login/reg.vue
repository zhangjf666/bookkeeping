<template>
	<view class="content">
		<u-navbar
			class="navbar"
			:background="{ backgroundColor: '#efeff4' }"
			back-icon-color="#000"
			back-icon-size="38"
			back-text="注册"
            z-index="0"
			:back-text-style="{ color: '#000', fontSize: '30rpx', marginLeft: '10rpx' }"
		></u-navbar>
		<view class="input-group">
			<view class="input-row border">
				<text class="title">账号：</text>
				<u-input type="text" focus v-model="username" placeholder="请输入账号"></u-input>
			</view>
			<view class="input-row border">
				<text class="title">密码：</text>
				<u-input type="password" v-model="password" placeholder="请输入密码"></u-input>
			</view>
			<view class="input-row">
				<text class="title">确认密码：</text>
				<u-input type="password" v-model="confirmPassword" placeholder="请确认密码"></u-input>
			</view>
		</view>
		<view class="btn-row">
			<button type="primary" class="primary" @tap="register">注册并登录</button>
		</view>
	</view>
</template>

<script>
    import { registerUser, login } from '@/api/auth.js';
	import { mapMutations } from 'vuex';

	export default {
		components: {

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
.content {
	display: flex;
    flex: 1;
    flex-direction: column;
    background-color: #efeff4;
	width: 100%;
	height: 100vh;
}
.navbar {
	/deep/ .u-border-bottom:after {
		border-bottom-width: 0px;
	}
}
.input-group {
	margin: 30rpx;
	padding: 0rpx 20rpx 0rpx 20rpx;
    display: flex;
    flex-direction: column;
    background-color: #ffffff;
    border-radius: 6px;
}

.input-group::before {
    position: absolute;
    right: 0;
    top: 0;
    left: 0;
    height: 1px;
    content: '';
    -webkit-transform: scaleY(.5);
    transform: scaleY(.5);
    background-color: #c8c7cc;
}

.input-group::after {
    position: absolute;
    right: 0;
    bottom: 0;
    left: 0;
    height: 1px;
    content: '';
    -webkit-transform: scaleY(.5);
    transform: scaleY(.5);
    background-color: #c8c7cc;
}
.input-row {
    display: flex;
    flex-direction: row;
    /* font-size: 18px; */
    height: 40px;
	align-items: center;
	border-bottom: #c8c7cc solid 1px;
	.title {
		width: 140rpx;
	}
}
.btn-row {
    margin-top: 20rpx;
    padding: 30rpx;
}

button.primary {
    background-color: #d83d34;
}
</style>
