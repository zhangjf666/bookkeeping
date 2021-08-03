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