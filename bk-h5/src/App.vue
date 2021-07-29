<script>
	import { userInfo } from '@/api/auth.js';
	export default {
		onLaunch: function() {
			console.log('App Launch')
			let token = uni.getStorageSync('token')
			if (token) {
				userInfo().then(data => {
					this.$store.commit('setToken', token);
					this.$store.commit('setUser', data.user);
				}).catch(data =>{
					uni.setStorageSync('token', null);
					uni.setStorageSync('user', null);
				})
			}
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style lang="scss">
	/*每个页面公共css */
	/* 注意要写在第一行，同时给style标签加入lang="scss"属性 */
    /* @import "uview-ui/index.scss"; */
	/* @import '/static/font/iconfont.css';
	@import '/static/font/iconfont-other.css'; */
	/* 解决头条小程序组件内引入字体不生效的问题 */
	/* #ifdef MP-TOUTIAO */
	@font-face {
		font-family: uniicons;
		src: url('/static/uni.ttf');
	}
/* #endif */
</style>
