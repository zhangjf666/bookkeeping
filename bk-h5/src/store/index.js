import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex);

import { querySummary } from "@/api/incomeExpense.js";
import { updateUserConfig, userRemark } from "@/api/user.js";

const store = new Vuex.Store({
    state: {
        // token
        token: '',
        // user
        user: {},
        //用户配置
        userConfig: {},
        //用户分类
        classify: [],
        //用户账本
        accountBook: [],
        //用户常用备注
        userRemark: [],
        //用户收支信息摘要
        summary: {expenseAmount: 0, incomeAmount: 0, incomeExpenseList: []}
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
            state.userConfig = config;
        },
        // 设置用户配置项
        setUserConfigItem(state, config) {
            state.userConfig[config.name].value = config.value;
            updateUserConfig(state.userConfig[config.name]);
        },
        // 设置用户分类
        setClassify(state, classify) {
            state.classify = classify;
        },
        // 设置用户常用备注
        setUserRemark(state, userRemark) {
            state.userRemark = userRemark;
        },
        // 设置用户账本
        setAccountBook(state, accountBook) {
            state.accountBook = accountBook;
        },
        //更新用户常用配置
        updateUserRemark(state) {
            userRemark({userId: state.user.id}).then(data => {
                state.userRemark = data;
              })
        },
        //更新摘要
        updateSummary(state) {
            querySummary({userId: state.user.id}).then(data => {
                state.summary = data;
              })
        }
    }
})
export default store