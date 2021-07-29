import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex);

const store = new Vuex.Store({
    state: {
        // token
        token: '',
        // user
        user: {}
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
        }
    }
})
export default store