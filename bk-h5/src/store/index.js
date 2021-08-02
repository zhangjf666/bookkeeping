import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex);

const store = new Vuex.Store({
    state: {
        // token
        token: '',
        // user
        user: {},
        //用户配置
        userConfig: {},
        //用户分类
        classify: []
    },
    getters: {
        // 返回是否已登录
        loginFlag(state) {
            return state.token != null && state.token != '';
        }
    },
    mutations: {
        // 设置token
        setToken(state, token){
            state.token = token;
        },
        // 设置user
        setUser(state, user){
            state.user = user;
        },
        // 设置用户配置
        setUserConfig(state, config) {
            state.userConfig[config.name] = config.value;
        },
        // 设置用户分类
        setClassify(state, classify) {
            state.classify = classify;
        }
    }
})
export default store