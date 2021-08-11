
let base_url="";//请求地址
if (process.env.NODE_ENV === "development") {
    // 开发环境
    base_url = "http://127.0.0.1:8080";
} else if (process.env.NODE_ENV === "test") {
    // 测试环境
    base_url = "http://192.168.2.88:8080";
} else if (process.env.NODE_ENV === "production") {
    // 正式环境
    base_url = "https://www.zhiizh.com/bookkeeping/api";
}

function service(options = {}) {
    options.url = `${base_url}${options.url}`;
    //配置请求头
    if(options.header == null) {
        options.header = {
            'content-type': 'application/x-www-form-urlencoded'
        }
    }
    options.header["Authorization"] = uni.getStorageSync('token');

    return new Promise((resolved, rejected) => {
        //成功
        options.success = (res) => {
            if (Number(res.data.code) == 0) {  //请求成功
                resolved(res.data.data);
            } else {
                uni.showToast({
                    icon: 'none',   
                    duration: 3000,
                    title: `${res.data.msg}`
                });
                rejected(res.data.msg);//错误
            }
        }
        //错误
        options.fail = (err) => {
            rejected(err); //错误
        }
        uni.request(options);
    });
}
export default service;
