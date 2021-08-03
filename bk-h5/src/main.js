import Vue from 'vue'
import App from './App'
import store from './store/index.js'

//使用uView
import uView from "uview-ui"
Vue.use(uView)

Vue.config.productionTip = false
Vue.prototype.$store = store
//onlaunch先获取登录信息后其他页面才能在继续
Vue.prototype.$onLaunched = new Promise(resolve => {
  Vue.prototype.$isResolve = resolve
})

App.mpType = 'app'

const app = new Vue({
  store,
  ...App
})
app.$mount()
