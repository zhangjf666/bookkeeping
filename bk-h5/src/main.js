import Vue from 'vue'
import App from './App'
import store from './store/index.js'

//使用uView
import uView from "uview-ui"
Vue.use(uView)

// 引入moment
import moment from 'moment'
import 'moment/locale/zh-cn'
// 使用中文时间
moment.locale('zh-cn')
Vue.prototype.$moment = moment

Vue.config.productionTip = false
Vue.prototype.$store = store
//onlaunch先获取登录信息后其他页面才能在继续
Vue.prototype.$onLaunched = new Promise(resolve => {
  Vue.prototype.$isResolve = resolve
})

//echarts排除不能点击的问题
window.wx = {}

App.mpType = 'app'

const app = new Vue({
  store,
  ...App
})
app.$mount()
