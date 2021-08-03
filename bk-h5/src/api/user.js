import request from '@/utils/request.js'

let prefix = '/user'
//用户配置查询
export function userConfig(data) {
    return request({
        url: '/userConfig',
        method: 'GET',
        data,
        header: {'content-type': 'application/json'}
    })
}
//用户分类查询
export function userClassify(data) {
    return request({
        url: '/classify',
        method: 'GET',
        data,
        header: {'content-type': 'application/json'}
    })
}
//用户账本查询
export function userAccountBook(data) {
    return request({
        url: '/accountBook',
        method: 'GET',
        data,
        header: {'content-type': 'application/json'}
    })
}