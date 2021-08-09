export function getDeviceUUID() {
	let deviceId = uni.getStorageSync('uni_deviceId') ||
		uni.getSystemInfoSync().deviceId ||
		uni.getSystemInfoSync().system + '_' + Math.random().toString(36).substr(2);

	uni.setStorageSync('uni_deviceId', deviceId)
	return deviceId;
}

export function formatNumber(num, decimal, separator) {
	decimal = decimal == undefined ? 2 : decimal;
	separator = separator || ','
	// 将num转为Number类型，因为其值可能为字符串数值，调用toFixed会报错
	num = Number(num);
	num = num.toFixed(Number(decimal));
	num += '';
	const x = num.split('.');
	let x1 = x[0];
	const x2 = x.length > 1 ? '.' + x[1] : '';
	const rgx = /(\d+)(\d{3})/;
	if (separator && isNaN(parseFloat(separator))) {
		while (rgx.test(x1)) {
			x1 = x1.replace(rgx, '$1' + separator + '$2');
		}
	}
	return x1 + x2;
}