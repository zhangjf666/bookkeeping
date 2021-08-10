<template>
    <view class="content">
        <view class="header">
            <view class="title">账单</view>
            <text class="iconfont icon-sousuo search" @click="goSearch"></text>
        </view>
        <view class="option">
            <bill-selector @change="dateChange"></bill-selector>
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
            <view class="chartView">
                <div id="billChart" style="width: 100%; height: 100%" ref="billChart"></div>
            </view>
        </view>
        <view class="detail">
            <view class="detailTitle">
                <view class="text">账单明细</view>
                <view v-if="mode == 0 || mode == 2" class="detailButton" @click="billSortClick">{{btnText}}</view>
            </view>
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
        </view>
    </view>
</template>

<script>
import { mapState, mapMutations, mapGetters } from "vuex";
import billSelector from "@/my-components/billSelector.vue";
import { querySumPeriod } from "@/api/incomeExpense.js";
import recordList from "@/my-components/recordList.vue";
import { formatNumber } from "../../utils/utils.js"
//echarts
import * as echarts from 'echarts/core';
import {
  BarChart,
  PieChart,
  LineChart
} from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DatasetComponent,
  DataZoomComponent
} from 'echarts/components';
import {
  CanvasRenderer
} from 'echarts/renderers';

echarts.use(
  [
    TitleComponent,
    TooltipComponent,
    GridComponent,
    BarChart,
    PieChart,
    LineChart,
    LegendComponent,
    DatasetComponent,
    CanvasRenderer,
    DataZoomComponent
  ]
);

export default {
    components: {
        billSelector, recordList
    },
    data() {
        return {
            //初始化图表
            billChart: null,
            mode: 0,
            expenseTotal: 0,
            incomeTotal: 0,
            radioValue: 'expense',
            incomeExpenseList: [],
            //图表横坐标
            xAxisList: [],
            yAxisIncomeList: [],
            yAxisExpenseList: [],
            //账单排序,0:按时间,1:按金额
            billSort: 0,
            //控制年账单中明细是否打开
            rowDetailOpen: []
        }
    },
    onLoad() {
        //默认获取当月数据
        var param = {userId: this.user.id, queryMode: '0', mode: '0'};
        param['beginDate'] = this.$moment().format('YYYY-MM-DD');
        param['endDate'] = this.$moment().format('YYYY-MM-DD');
        this.getSumPeriod(param);
    },
    mounted() {
        //初始化echarts
        this.billChart = echarts.init(document.getElementById("billChart"));
        //配置数据
        let option = {
            tooltip: {
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
                height: 74
            },
            xAxis: { type: 'category',axisTick: {show:false}, data: [] }, //X轴
            yAxis: { type: 'value', axisLine: {show:false},axisTick: {show:false},splitLine:{show:false},axisLabel : {
                formatter: function(){
                        return "";
                    }
                }
            }, //Y轴
            series: [] //配置项
        };
        this.billChart.setOption(option);
        //添加点击事件
        this.billChart.getZr().on('click', (params) => {
                let pointInPixel = [params.offsetX, params.offsetY]
                console.log(pointInPixel)
                if (this.billChart.containPixel('grid', pointInPixel)) {
                    let xIndex = this.billChart.convertFromPixel({ seriesIndex: 0 }, [params.offsetX, params.offsetY])[0]
                    if(xIndex >= 0) {
                        var data = this.radioValue == 'income' ? this.yAxisIncomeList[xIndex] : this.yAxisExpenseList[xIndex];
                        //显示提示
                        this.billChart.dispatchAction({
                            type: 'showTip',
                            // 系列的 index，在 tooltip 的 trigger 为 axis 的时候可选。
                            seriesIndex: 0,
                            // 数据项的 index，如果不指定也可以通过 name 属性根据名称指定数据项
                            dataIndex: xIndex,
                            // 本次显示 tooltip 的位置。只在本次 action 中生效。
                            // 缺省则使用 option 中定义的 tooltip 位置。
                            position: [pointInPixel.offsetX, 10],
                        })
                    }
                }
            })
    },
    methods: {
        //获取统计数据
        getSumPeriod(data) {
            querySumPeriod(data).then(res => {
                this.incomeTotal = res.incomeTotal;
                this.expenseTotal = res.expenseTotal;
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
                for(var i = 0; i<values.length; i++){
                    this.yAxisIncomeList.push(values[i].income);
                    this.yAxisExpenseList.push(values[i].expense);
                }
                // 填入数据
                this.updateChart(0);
            })
        },
        goSearch() {
            uni.navigateTo({
                url: `../search/search`,
            });
        },
        dateChange(option) {
            this.mode = option.mode;
            var param = {userId: this.user.id, queryMode: '0'};
            param['mode'] = option.mode;
            param['beginDate'] = option.beginDate;
            param['endDate'] = option.endDate;
            param['classifyList'] = option.classifyList.map((item) => { return item.id });
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
            this.billChart.setOption({
                    xAxis: {
                        data: this.xAxisList
                    },
                    series: series
                });
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
            return param[0].name + ' ￥' + formatNumber(param[0].data); 
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
                return item.date + '月'
            }
        },
        yearIncome() {
            return(item) => {
                return '￥ ' + formatNumber(item.income);
            }
        },
        yearExpense() {
            return(item) => {
                return '￥ ' + formatNumber(item.expense);
            }
        },
        yearRemain() {
            return(item) => {
                return '￥ ' + formatNumber((item.income - item.expense));
            }
        },
        rowDetailList() {
            return (item) => {
                var date = item.date;
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
//   padding-bottom: 1rpx;
}
.header {
    padding: 30rpx;
    height: 100rpx;
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
    .chartView { 
        width: 100%;
        height: 320rpx;
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
            font-size: 26rpx;
            height: 120rpx;
            border-bottom-style: solid;
            border-bottom-width: 1px;
            border-bottom-color: rgb(220, 220, 220);
            .arrow {
                width: 10%;
            }
            .month {
                font-size: 30rpx;
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
                justify-content: space-evenly;
                :last-child {
                    font-size: 20rpx;
                    color: #00a151;
                }
            }
            .expense {
                width: 30%;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: space-evenly;
                :last-child {
                    font-size: 20rpx;
                    color: #d83d34;
                }
            }
            .remain {
                width: 30%;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: space-evenly;
                :last-child {
                    font-size: 20rpx;
                }
            }
        }
    }
}
</style>