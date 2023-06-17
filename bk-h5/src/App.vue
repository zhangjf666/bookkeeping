<script>
	import { userInfo } from '@/api/auth.js';
	export default {
		onLaunch: function() {
			console.log('App Launch')
			// uni.configMTLS({
			//       certificates: [{
			//       host: 'www.zhiizh.com',  // 换成你证书设定的域名（也就是https请求的域名）
			//       client: '/static/client.p12',
			//       clientPassword: '39518605',
			//       server: ['/static/server.crt']
			//   }],
			//   complete (res) {
			//       console.log('res', res)
			//   }
			// })
			let token = uni.getStorageSync('token')
			if (token) {
				userInfo().then(data => {
					this.$store.commit('setToken', token);
					this.$store.commit('setUser', data.user);
					this.$isResolve();
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
    @import "uview-ui/index.scss";
	@import '/static/iconfont.css';
	body {
		display: flex;
		flex-direction: column;
		height: 100%;
	}
</style>
