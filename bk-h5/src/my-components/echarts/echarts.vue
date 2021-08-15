<template>
	<view>
		<view class="echarts" :id="option.id" :prop="option" :change:prop="echarts.update" @click="echarts.onClick"></view>
	</view>
</template>

<script>
	export default {
		name: 'Echarts',
		props: {
			option: {
				type: Object,
				required: true
			}
		},
		created() {
			// 设置随机数id
			let t = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
			let len = t.length
			let id = ''
			for (let i = 0; i < 32; i++) {
				id += t.charAt(Math.floor(Math.random() * len))
			}
			this.option.id = id
		},
		methods: {
			/**
			 * renderjs内的点击事件，回调到父组件
			 * @param {Object} params
			 */
			onViewClick(params) {
				this.$emit('click', params)
			}
		}
	}
</script>

<script module="echarts" lang="renderjs">
	export default {
		data() {
			return {
				chart: null,
				clickData: null // echarts点击事件的值
			}
		},
		mounted() {
			if (typeof window.echarts === 'object') {
				this.init()
			} else {
				// 动态引入类库
				const script = document.createElement('script')
				script.src = './static/echarts.min.js'
				script.onload = this.init
				document.head.appendChild(script)
			}
		},
		methods: {
			/**
			 * 初始化echarts
			 */
			init() {
				// 根据id初始化图表
				this.chart = echarts.init(document.getElementById(this.option.id))
				this.update(this.option)
				// echarts的点击事件
				this.chart.on('click', params => {
					// 把点击事件的数据缓存下来
					this.clickData = params
				})
				this.chart.getZr().on('click', (params) => {
				        let pointInPixel = [params.offsetX, params.offsetY]
				        if (this.chart.containPixel('grid', pointInPixel)) {
				            let xIndex = this.chart.convertFromPixel({ seriesIndex: 0 }, [params.offsetX, params.offsetY])[0]
				            if(xIndex >= 0) {
				                var data = this.option.series[xIndex];
				                //显示提示
				                this.chart.dispatchAction({
				                    type: 'showTip',
				                    // 系列的 index，在 tooltip 的 trigger 为 axis 的时候可选。
				                    seriesIndex: 0,
				                    // 数据项的 index，如果不指定也可以通过 name 属性根据名称指定数据项
				                    dataIndex: xIndex,
				                    // 本次显示 tooltip 的位置。只在本次 action 中生效。
				                    // 缺省则使用 option 中定义的 tooltip 位置。
				                    // position: [params.offsetX, params.offsetY],
				                })
				            }
				        }
				    })
			},
			/**
			 * 点击事件，可传递到外部
			 * @param {Object} event
			 * @param {Object} instance
			 */
			onClick(event, instance) {
				if (this.clickData) {
					// 把echarts点击事件相关的值传递到renderjs外
					instance.callMethod('onViewClick', {
						name : this.clickData.name, 
						data : this.clickData.data,
						seriesName: this.clickData.seriesName
					})
					// 上次点击数据置空
					this.clickData = null
				}
			},
			/**
			 * 监测数据更新
			 * @param {Object} option
			 */
			update(option) {
				if (this.chart) {
					this.chart.dispatchAction({
					    type: 'hideTip'
					});
					// 因App端，回调函数无法从renderjs外传递，故在此自定义设置相关回调函数
					// #ifdef APP-PLUS
					if (option.tooltip) {
						// 判断是否设置tooltip的位置
						if (option.tooltip.positionStatus) {
							option.tooltip.position = this.tooltipPosition()
						}
						// 判断是否格式化tooltip
						if (option.tooltip.formatterStatus) {
							option.tooltip.formatter = this.tooltipFormatter(option.tooltip.formatterType)
						}
					}
					// #endif
					// 设置新的option
					this.chart.setOption(option, option.notMerge)
				}
			},
			/**
			 * 设置tooltip的位置，防止超出画布
			 */
			tooltipPosition() {
				return (point, params, dom, rect, size) => {
					// 其中point为当前鼠标的位置，size中有两个属性：viewSize和contentSize，分别为外层div和tooltip提示框的大小
					let x = point[0]
					let y = point[1]
					let viewWidth = size.viewSize[0]
					let viewHeight = size.viewSize[1]
					let boxWidth = size.contentSize[0]
					let boxHeight = size.contentSize[1]
					let posX = 0 // x坐标位置
					let posY = 0 // y坐标位置
					if (x >= boxWidth) { // 左边放的下
						posX = x - boxWidth - 1
					}
					if (y >= boxHeight) { // 上边放的下
						posY = y - boxHeight - 1
					}
					return [posX, posY]
				}
			},
			/**
			 * tooltip格式化
			 * @param {Object} unit 数值后的单位
			 * @param {Object} formatFloat2 是否保留两位小数
			 * @param {Object} formatThousands 是否添加千分位
			 */
			tooltipFormatter(type) {
				return params => {
					if(type == 'money') {
						return params[0].name + ' ￥' + this.formatNumber(params[0].data.fact); 
					}
					return params; 
				}
			},
			//格式化金额
			formatNumber(num, decimal, separator) {
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
		}
	}
</script>

<style lang="scss" scoped>
	.echarts {
		width: 100%;
		height: 100%;
	}
</style>