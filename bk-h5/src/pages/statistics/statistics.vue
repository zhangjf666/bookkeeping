<template>
    <view class="content">
		<!-- 头部导航栏 -->
		<u-navbar z-index="9999" back-icon-color="#fff" :is-back="false" back-icon-size="38" :background="{background: '#252569'}" :border-bottom="false">
			<view class="slot-wrap">
				<view class="header">
				    <view class="title">报表</view>
				</view>
			</view>
		</u-navbar>
        <view class="option">
            <bill-selector ref="billSelector" @change="dateChange"></bill-selector>
        </view>
        <view class="chartContent">
            <u-radio-group class="chartRadio" v-model="radioValue" size="26">
                <u-radio name="expense" label-size="26" active-color="#d83d34">{{expenseText}}
                    <text style="color: #d83d34;">{{expenseValue}}</text>
                </u-radio>
                <u-radio name="income" label-size="26" active-color="#00a151">{{incomeText}}
                    <text style="color: #00a151;">{{incomeValue}}</text>
                </u-radio>
            </u-radio-group>
            <view class="expenseLimit">
                <text>当前支出限额: {{ expenseLimitText }}</text>
                <text :style="surplusStyle()">剩余限额: {{ expenseSurplusText }}</text>
            </view>
            <view class="chartView">
                <echarts :option="option" class="echarts" @click="echartsClick"></echarts>
            </view>
        </view>
        <view class="detail">
            <scroll-view scroll-y class="scroll">
                <view class="detailRow" v-for="(item) in filterSumList" :key="item.classify" hover-class="click-bg" hover-stay-time="200" @click="detailClick(item)">
                    <text class="item-image" :class="'iconfont icon-' + item.classifyImage" :style="itemColor(item)"></text>
                    <view class="item-text">
                        <view class="item-classify">{{classifyTextFormat(item)}}</view>
                        <u-line-progress class="item-percent" :percent="item.percent" :show-percent="false" :height="15" :active-color="item.expense ? '#d83d34' : '#00a151'"></u-line-progress>
                    </view>
                    <view class="item-amount">
                        <view class="item-value" :style="itemColor(item)">{{amountFormat(item)}}</view>
                        <view class="item-num">{{item.num + '笔'}}</view>
                    </view>
                    <u-icon style="color: #7A7E83;" name="arrow-right"></u-icon>
                </view>
            </scroll-view>
        </view>
    </view>
</template>

<script>
import { mapState, mapMutations, mapGetters } from "vuex";
import billSelector from "@/my-components/billSelector.vue";
import { querySumPeriod } from "@/api/incomeExpense.js";
import recordList from "@/my-components/recordList.vue";
import { formatNumber } from "../../utils/utils.js"
import Echarts from '@/my-components/echarts/echarts.vue'

export default {
    components: {
        billSelector, recordList, Echarts
    },
    data() {
        return {
            //初始化图表
            chart: null,
            mode: 0,
            expenseTotal: 0,
            incomeTotal: 0,
            expenseLimit: 0,
            expenseSurplus: 0,
            radioValue: 'expense',
            incomeExpenseList: [],
            incomeExpenseSum: [],
            //账单排序,0:按时间,1:按金额
            billSort: 0,
            //控制年账单中明细是否打开
            rowDetailOpen: [],
            chartIncomeData: [],
            chartExpenseData: [],
			//配置数据
			option : {
			    series: [] //配置项
			}
        }
    },
    onPullDownRefresh() {
        this.$refs.billSelector.selectOver();
        uni.stopPullDownRefresh();
    },
    onLoad() {
        //默认获取当月数据
        var param = {userId: this.user.id, queryMode: '1', mode: '0'};
        param['beginDate'] = this.$moment().format('YYYY-MM-DD');
        param['endDate'] = this.$moment().format('YYYY-MM-DD');
        this.getSumPeriod(param);
    },
    mounted() {
    },
    methods: {
        //获取统计数据
        getSumPeriod(data) {
            uni.showLoading({
				title: '加载中'
			});
            querySumPeriod(data).then(res => {
                this.incomeTotal = res.incomeTotal;
                this.expenseTotal = res.expenseTotal;
                this.expenseLimit = res.expenseLimit;
                this.expenseSurplus = res.expenseSurplus;
                this.incomeExpenseList = res.incomeExpenseList;
                this.incomeExpenseSum = res.incomeExpenseSum;
                //更新图表
                var values= Object.values(this.incomeExpenseSum);
                var incomelist = [];
                var expenselist = [];
                for(var i = 0; i<values.length; i++){
                    if(values[i].percent >= 1){
                        var value = {name: values[i].classifyName, value: formatNumber(values[i].percent, 0), classify: values[i].classify};
                        if(values[i].income) {
                            incomelist.push(value);
                        } else {
                            expenselist.push(value);
                        }
                    }
                }
                this.chartIncomeData = incomelist;
                this.chartExpenseData = expenselist;
                // 填入数据
                this.updateChart(0);
            }).finally(() => {
                uni.hideLoading();
            })
        },
        dateChange(option) {
            this.mode = option.mode;
            var param = {userId: this.user.id, queryMode: '1'};
            param['mode'] = option.mode;
            param['beginDate'] = option.beginDate;
            param['endDate'] = option.endDate;
            param['classifyList'] = option.classifyList.map((item) => { return item.id });
            this.getSumPeriod(param);
        },
        updateChart(type){
            this.radioValue = type == 0 ? 'expense' : 'income';
            var series = {
                        // 根据名字对应到相应的系列
                        name: 'expense',
                        data: type == 0 ? this.chartExpenseData : this.chartIncomeData,
                        selectedMode: 'single',
                        type: 'pie',
                        radius: ['40%', '70%'],
                        clockwise: false,
                        label: {
                            position: 'outside', // 设置标签向外
                            formatter: '{b} {c}%' // 设置标签格式
                        },
                    }
            // this.statisticsChart.setOption({
            //         series: series
            //     });
			this.option.series = series;
        },
        itemColor(item) {
            return item.expense ? 'color: #d83d34;' : 'color: #00a151;'
        },
        detailClick(item) {
            //提取数据
            var recordList = this.incomeExpenseList.filter(x => x.mainClassify == item.classify);
            var data = { classifyName: item.classifyName, recordList: recordList };
            uni.navigateTo({
                url: `./statisticsDetail?data=` + encodeURIComponent(JSON.stringify(data)),
                animationType: 'slide-in-right', 
                animationDuration: 300
            });
        },
		echartsClick(param) {
			var data = param.data;
			data['classifyName'] = data.name;
			//点击在移动端会连续多次触发,需要添加一个停顿
			setTimeout(() => {
			    this.detailClick(data);
			}, 200);
		},
        chartClick() {
            this.statisticsChart.on('click', (param) => {
                var data = param.data;
                data['classifyName'] = data.name;
                //点击在移动端会连续多次触发,需要添加一个停顿
                setTimeout(() => {
                    this.detailClick(data);
                }, 200);
            })
        },
        surplusStyle() {
            let percent = this.expenseSurplus/this.expenseLimit;
            return percent <= 0.1 ? 'color: #d83d34;' : percent <= 0.3 ? 'color: #00a151;' : 'color: #00a151;'
        }
    },
    computed: {
        ...mapState(['user']),
        expenseText() {
            return this.mode == 0 ? '月支出' : this.mode == 1 ? '年支出' : '支出';
        },
        expenseValue() {
            return ' ￥' + formatNumber(this.expenseTotal);
        },
        incomeText() {
            return this.mode == 0 ? '月收入' : this.mode == 1 ? '年收入' : '收入';
        },
        incomeValue() {
            return ' ￥' + formatNumber(this.incomeTotal);
        },
        //返回金额前面的RMB图标
        amountFormat() {
            return (item) => {
                return item.expense ? '￥' + formatNumber(item.expense) : '￥' + formatNumber(item.income);
            }
        },
        //返回分类名称
        classifyTextFormat() {
            return (item) => {
                var text = item.classifyName + " ";
                if(item.percent < 1){
                    text += "<1%";
                } else {
                    text += formatNumber(item.percent, 0) + "%";
                }
                return text;
            }
        },
        filterSumList() {
            var list = Object.values(this.incomeExpenseSum);
            if(this.radioValue == 'expense'){
                list = list.filter(x => x.expense);
            } else {
                list = list.filter(x => x.income);
            }
            return list;
        },
        expenseLimitText() {
            return formatNumber(this.expenseLimit);
        },
        expenseSurplusText() {
            return formatNumber(this.expenseSurplus);
        }
    },
    watch: {
        radioValue(e) {
            //切换显示
            e == 'expense' ? this.updateChart(0) : this.updateChart(1);
        }
    }
}
</script>

<style lang="scss" scoped>
.content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  height: 100%;
}
.slot-wrap{
    display: flex;
	align-items: center;
	flex: 1;
}
.header {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    width: 100%;
    background-color: #252569;
    .title {
        color: white;
        font-size: 40rpx;
    }
}
.option {
    padding: 30rpx;
    height: 200rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100%;
    background-color: #252569;
}
.chartContent {
    margin-top: -100rpx;
    width: 95%;
    height: 520rpx;
    align-items: center;
    justify-content: space-between;
    flex-direction: column;
    display: flex;
    background-color: white;
    border-radius: 10rpx;
    box-shadow: 0px 6px 6px rgb(207, 207, 207);
    .chartRadio {
        padding-top: 20rpx;
        width: 100%;
        align-items: center;
        justify-content: center;
        flex-direction: row;
        display: flex;
    }
    .expenseLimit {
        display: flex;
        width: 90%;
        justify-content: space-around;
        color: rgb(133, 133, 133);
    }
    .chartView { 
        width: 100%;
        height: 440rpx;
		.echarts {
			width: 100%;
			height: 440rpx;
		}
    }
}
.detail {
    padding: 30rpx;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    .scroll {
        display: flex;
        width: 100%;
        height: 700rpx;
        // padding: 0rpx 30rpx 0 30rpx;
    }
    .detailRow {
        width: 100%;
        height: 120rpx;
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: center;
        border-bottom: rgb(220, 220, 220) solid 1px;
        .item-image {
            width: 15%;
            font-size: 50rpx;
            margin-right: 20rpx
        };
        .item-text {
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: column;
            font-size: 30rpx;
            align-items: flex-start;
            justify-content: space-evenly;
            .item-percent {
                border-radius: 20rpx
            }
        };
        .item-amount {
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: flex-end;
            justify-content: center;
            .item-value {
                font-size: 32rpx;
            }
            .item-num {
                color: #7A7E83;
            }
        }
    }
}
.click-bg {
	background-color:rgb(240, 240, 240);
}
</style>