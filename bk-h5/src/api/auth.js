import request from '@/utils/request.js'

let prefix = '/auth'
export function login(data) {
    return request({
        url: prefix + '/login',
        method: 'POST',
        data,
        header: {'content-type': 'application/json'}
    })
}

export function registerUser(data) {
    return request({
        url: prefix + '/register',
        method: 'POST',
        data,
        header: {'content-type': 'application/json'}
    })
}

export function logout() {
    var data = {};
    return request({
        url: prefix + '/logout',
        method: 'POST',
        data
    })
}

export function userInfo() {
    var data = {};
    return request({
        url: prefix + '/user-info',
        method: 'POST',
        data
    })
}