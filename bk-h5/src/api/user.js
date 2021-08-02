import request from '@/utils/request.js'

let prefix = '/user'
export function userConfig(data) {
    return request({
        url: '/userConfig',
        method: 'GET',
        data,
        header: {'content-type': 'application/json'}
    })
}