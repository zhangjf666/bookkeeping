import request from '@/utils/request.js'

let prefix = '/incomeExpense'
//创建收入支出
export function createIncomeExpense(data) {
    return request({
        url: prefix,
        method: 'POST',
        data,
        header: {'content-type': 'application/json'}
    })
}
// 更新收入支出
export function updateIncomeExpense(data) {
    return request({
        url: prefix,
        method: 'PUT',
        data,
        header: {'content-type': 'application/json'}
    })
}
// 删除收入支出
export function deleteIncomeExpense(data) {
    return request({
        url: prefix,
        method: 'DELETE',
        data,
        header: {'content-type': 'application/json'}
    })
}
//获取首页统计信息
export function querySummary(data) {
    return request({
        url: prefix + '/summary',
        method: 'GET',
        data
        // header: {'content-type': 'application/json'}
    })
}
//查询账单报表信息
export function querySumPeriod(data) {
    return request({
        url: prefix + '/sumPeriod',
        method: 'GET',
        data,
        header: {'content-type': 'application/json'}
    })
}