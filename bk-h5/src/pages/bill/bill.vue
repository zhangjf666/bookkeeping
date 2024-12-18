<template>
    <view class="content">
		<!-- 头部导航栏 -->
		<u-navbar z-index="9999" back-icon-color="#fff" :is-back="false" back-icon-size="38" :background="{background: '#252569'}" :border-bottom="false">
			<view class="slot-wrap">
				<view class="header">
				    <view class="title">账单</view>
				    <text class="iconfont icon-sousuo search" @click="goSearch"></text>
				</view>
			</view>
		</u-navbar>
        <view class="option">
            <bill-selector ref="billSelector" @change="dateChange"></bill-selector>
        </view>
        <view class="chartContent">
            <u-radio-group class="chartRadio" v-model="radioValue" size="28">
                <u-radio name="expense" label-size="28" active-color="#d83d34">{{expenseText}}
                    <text style="color: #d83d34;">{{expenseValue}}</text>
                </u-radio>
                <u-radio name="income" label-size="28" active-color="#00a151">{{incomeText}}
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
            <view class="detailTitle">
                <view class="text">账单明细</view>
                <view v-if="mode == 0 || mode == 2" class="detailButton" @click="billSortClick">{{btnText}}</view>
            </view>
            <scroll-view scroll-y class="scroll">
                <record-list v-if="mode == 0 || mode == 2" :list="incomeExpenseList" :sortType="billSort"></record-list>
                <view class="detailYear" v-if="mode == 1">
                    <view class="row" v-for="(item, index) in yearRow" :key="index">
                        <view class="rowSum" @click="rowClick(item)">
                            <u-icon class="arrow" :name="yearArrow(item)"></u-icon>
                            <view class="month">{{yearMonth(item)}}</view>
                            <view class="income">
                                <view>收入</view>
                                <text>{{yearIncome(item)}}</text>
                            </view>
                            <view class="expense">
                                <view>支出</view>
                                <text>{{yearExpense(item)}}</text>
                            </view>
                            <view class="remain">
                                <view>结余</view>
                                <text>{{yearRemain(item)}}</text>
                            </view>
                        </view>
                        <record-list style="background-color: #FAFAFA;" v-if="rowDetailShow(item)" :list="rowDetailList(item)" :sortType="0"></record-list>
                    </view>
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
            billChart: null,
            mode: 0,
            expenseTotal: 0,
            incomeTotal: 0,
            expenseLimit: 0,
            expenseSurplus: 0,
            radioValue: 'expense',
            incomeExpenseList: [],
            //图表横坐标
            xAxisList: [],
            yAxisIncomeList: [],
            yAxisExpenseList: [],
            //账单排序,0:按时间,1:按金额
            billSort: 0,
            //控制年账单中明细是否打开
            rowDetailOpen: [],
			//配置数据
			option : {
				notMerge: true,
			    tooltip: {
					formatterStatus: true,
					formatterType: 'money',
			        trigger: 'axis',
			        triggerOn:'none',
			        confine: true,
			        formatter: this.tooltipFormatter
			    },
			    dataZoom: [
			        {
			            id: 'dataZoomX',
			            type: 'inside',
			            xAxisIndex: [0],
			            filterMode: 'none',
			            zoomLock: true,
			            startValue: 0,
			            minValueSpan: 31,
			            maxValueSpan: 31
			        }
			    ],
			    grid: {
			        y: "0%",
			        height: "80%"
			    },
			    xAxis: { type: 'category',axisTick: {show:false}, data: [] }, //X轴
			    yAxis: { type: 'value', axisLine: {show:false},axisTick: {show:false},splitLine:{show:false},axisLabel : { formatter: ""}
			    }, //Y轴
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
        var param = {userId: this.user.id, queryMode: '0', mode: '0'};
        param['beginDate'] = this.$moment().format('YYYY-MM-DD');
        param['endDate'] = this.$moment().format('YYYY-MM-DD');
        this.getSumPeriod(param);
    },
    mounted() {
        
    },
    methods: {
		echartsClick(params) {
			console.log('点击数据', params)
		},
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
                //更新图表
                this.xAxisList = [];
                this.yAxisIncomeList = [];
                this.yAxisExpenseList = [];
                var sum = res.incomeExpenseSum;
                var keys = Object.keys(sum);
                for(var i = 0; i<keys.length; i++){
                    if(this.mode == 0) {
                        this.xAxisList.push(keys[i].substring(5));
                    } else if(this.mode == 1){
                         this.xAxisList.push(keys[i].substring(5) + '月');
                    } else {
                        this.xAxisList.push(keys[i]);
                    } 
                }
                var values= Object.values(sum);
                var imax = 0;
                var emax = 0;
                if(values.length > 0){
                    var ilist = values.map((item)=> { return item.income});
                    var elist = values.map((item)=> { return item.expense});
                    imax = Math.max.apply(null, ilist);
                    emax = Math.max.apply(null, elist);
                }
                for(var i = 0; i<values.length; i++){
                    //避免极值的影响
                    var ivalue = values[i].income;
                    var showiValue = ivalue;
                    if(ivalue == 0) {
                        showeValue = (imax * 0.01).toFixed(1)
                    } else if(ivalue * 20 < imax){
                        showiValue = (imax * 0.04).toFixed(1)
                    }else if(ivalue * 10 < imax){
                        showiValue = (imax * 0.09).toFixed(1);
                    }
                    this.yAxisIncomeList.push({value: showiValue, fact:values[i].income});
                    var evalue = values[i].expense;
                    var showeValue = evalue;
                    if(evalue == 0) {
                        showeValue = (emax * 0.01).toFixed(1)
                    } else if(evalue * 20 < emax){
                        showeValue = (emax * 0.04).toFixed(1)
                    }else if(evalue * 10 < emax){
                        showeValue = (emax * 0.08).toFixed(1);
                    }
                    this.yAxisExpenseList.push({value: showeValue, fact:values[i].expense});
                }
                // 填入数据
                this.updateChart(0);
            }).finally(() => {
                uni.hideLoading();
            })
        },
        goSearch() {
            uni.navigateTo({
                url: `../search/search`,
            });
        },
        dateChange(opt) {
            this.mode = opt.mode;
            var param = {userId: this.user.id, queryMode: '0'};
            param['mode'] = opt.mode;
            param['beginDate'] = opt.beginDate;
            param['endDate'] = opt.endDate;
            param['classifyList'] = opt.classifyList.map((item) => { return item.id });
            this.getSumPeriod(param);
        },
        updateChart(type){
            this.radioValue = type == 0 ? 'expense' : 'income';
            var chartType = this.mode == 1 ? 'line':'bar';
            var seriesExpense = {
                        // 根据名字对应到相应的系列
                        name: '支出',
                        data: this.yAxisExpenseList,
                        type: chartType,
                        color: ['#d83d34'],
                        barWidth: 2
                    }
            var seriesIncome = {
                        // 根据名字对应到相应的系列
                        name: '收入',
                        data: this.yAxisIncomeList,
                        type: chartType,
                        color: ['#00a151'],
                        barWidth: 2
                    }
            var series = type == 0 ? [seriesExpense] : [seriesIncome];
			
			this.option.xAxis.data = this.xAxisList;
			this.option.series = series;
        },
        billSortClick() {
            this.billSort = this.billSort == 0 ? 1 : 0;
        },
        rowClick(item) {
            var index = this.rowDetailOpen.indexOf(item.date)
            if(index == -1) {
                this.rowDetailOpen.push(item.date);
            } else {
                this.rowDetailOpen.splice(index, 1);
            }
        },
        //自定义图表提示
        tooltipFormatter(param) {
            return param[0].name + ' ￥' + formatNumber(param[0].data.fact); 
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
        btnText() {
            return this.billSort == 0 ? '按时间' : '按金额';
        },
        yearRow() {
            var row = [];
            for(var i = 0; i< this.xAxisList.length; i++){
                var item = {date :this.xAxisList[i], income: this.yAxisIncomeList[i], expense: this.yAxisExpenseList[i]};
                row.unshift(item);
            }
            return row;
        },
        yearArrow() {
            return(item) => {
                return this.rowDetailOpen.indexOf(item.date) != -1 ? 'arrow-down' : 'arrow-right';    
            }
        },
        yearMonth() {
            return(item) => {
                return item.date
            }
        },
        yearIncome() {
            return(item) => {
                return '￥ ' + formatNumber(item.income.fact);
            }
        },
        yearExpense() {
            return(item) => {
                return '￥ ' + formatNumber(item.expense.fact);
            }
        },
        yearRemain() {
            return(item) => {
                return '￥ ' + formatNumber((item.income.fact - item.expense.fact));
            }
        },
        rowDetailList() {
            return (item) => {
                var date = item.date.substring(0, item.date.length - 1);
                var list = [];
                for(var i=0; i<this.incomeExpenseList.length;i++){
                    if(this.$moment(this.incomeExpenseList[i].date).month() + 1 == date){
                        list.push(this.incomeExpenseList[i]);
                    }
                }
                return list;
            }
        },
        rowDetailShow() {
            return (item) => {
                return this.rowDetailOpen.indexOf(item.date) != -1;
            }
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
    .search {
        position: absolute;
        right: 30rpx;
        font-size: 40rpx;
        color: white;
        margin-left: auto;
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
    height: 400rpx;
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
        height: 300rpx;
		.echarts {
			width: 100%;
			height: 300rpx;
		}
    }
}
.detail {
    padding: 30rpx;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    .detailTitle {
        margin-bottom: 20rpx;
        height: 56rpx;
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
        .text {
            display: flex;
            flex-direction: row;
            font-weight: bold;
            font-size: 30rpx;
        }
        .detailButton {
            display: flex;
            flex-direction: row;
            font-size: 24rpx;
            padding: 10rpx 16rpx 10rpx 16rpx;
            border-style: solid;
            border-width: 1rpx;
            border-color: rgb(0, 0, 0);
            border-radius: 6rpx;
        }
    }
    .scroll {
        display: flex;
        width: 100%;
        height: 750rpx;
        // padding: 0rpx 30rpx 0 30rpx;
    }
    .detailYear {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        .row {
            display: flex;
            flex-direction: column;
            width: 100%;
        }
        .rowSum {
            display: flex;
            flex-direction: row;
            width: 100%;
            font-size: 30rpx;
            height: 120rpx;
            border-bottom-style: solid;
            border-bottom-width: 1px;
            border-bottom-color: rgb(220, 220, 220);
            .arrow {
                width: 10%;
            }
            .month {
                font-size: 34rpx;
                width: 20%;
                display: flex;
                flex-direction: column;
                align-items: flex-start;
                justify-content: center;
            }
            .income {
                width: 30%;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                :last-child {
                    font-size: 26rpx;
                    color: #00a151;
                }
            }
            .expense {
                width: 30%;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                :last-child {
                    font-size: 26rpx;
                    color: #d83d34;
                }
            }
            .remain {
                width: 30%;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                :last-child {
                    font-size: 26rpx;
                }
            }
        }
    }
}
</style>