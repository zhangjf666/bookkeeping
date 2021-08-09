<template>
    <view class="content">
        <view class="header">
            <view class="title">报表</view>
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
                <div id="chart" style="width: 100%; height: 100%" ref="chart"></div>
            </view>
        </view>
        <view class="detail">
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
  DatasetComponent
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
    CanvasRenderer
  ]
);
export default {
    components: {
        billSelector, recordList
    },
    data() {
        return {
            //初始化图表
            chart: null,
            mode: 0,
            expenseTotal: 0,
            incomeTotal: 0,
            radioValue: 'expense',
            incomeExpenseList: [],
            incomeExpenseSum: [],
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
        var param = {userId: this.user.id, queryMode: '1', mode: '0'};
        param['beginDate'] = this.$moment().format('YYYY-MM-DD');
        param['endDate'] = this.$moment().format('YYYY-MM-DD');
        this.getSumPeriod(param);
    },
    mounted() {
        //初始化echarts
        this.chart = echarts.init(document.getElementById("chart"));
        //配置数据
        let option = {
            tooltip: {
                trigger: 'axis',
                triggerOn:'mouseover',
                axisPointer: { // 坐标轴指示器配置项。
                    type: 'line', // 'line' 直线指示器  'shadow' 阴影指示器  'none' 无指示器  'cross' 十字准星指示器。
                    axis: 'auto', // 指示器的坐标轴。 
                    snap: true, // 坐标轴指示器是否自动吸附到点上
                },
                confine: true,
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
        this.chart.setOption(option);
        //添加点击事件
        this.chart.on('click', (params) => {
            console.log(params)
        })
        // this.chart.getZr().on('click', function(params) {
        //         let pointInPixel = [params.offsetX, params.offsetY]
        //         console.log(pointInPixel)
        //         if (this.chart.containPixel('grid', pointInPixel)) {
        //             let xIndex = this.chart.convertFromPixel({ seriesIndex: 0 }, [params.offsetX, params.offsetY])[0]
        //             console.log(xIndex)
        //         }
        //     })
    },
    methods: {
        //获取统计数据
        getSumPeriod(data) {
            querySumPeriod(data).then(res => {
                this.incomeTotal = res.incomeTotal;
                this.expenseTotal = res.expenseTotal;
                this.incomeExpenseList = res.incomeExpenseList;
                this.incomeExpenseSum = res.incomeExpenseSum;
                //更新图表
                this.xAxisList = [];
                this.yAxisIncomeList = [];
                this.yAxisExpenseList = [];

                var sum = res.incomeExpenseSum;
                var keys = Object.keys(sum);
                if(this.mode == 0 || this.mode == 1) {
                    for(var i = 0; i<keys.length; i++){
                        this.xAxisList.push(keys[i]);
                    }
                } else {
                    this.xAxisList = keys;
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
            var chartType = this.mode == 1 ? 'line':'bar';
            var seriesExpense = {
                        // 根据名字对应到相应的系列
                        name: 'expense',
                        data: this.yAxisExpenseList,
                        type: chartType,
                        color: ['#d83d34'],
                        barWidth: 2
                    }
            var seriesIncome = {
                        // 根据名字对应到相应的系列
                        name: 'income',
                        data: this.yAxisIncomeList,
                        type: chartType,
                        color: ['#00a151'],
                        barWidth: 2
                    }
            var series = type == 0 ? [seriesExpense] : [seriesIncome];
            this.chart.setOption({
                    xAxis: {
                        data: this.xAxisList
                    },
                    series: series
                });
        },
        itemColor(item) {
            return item.expense ? 'color: #d83d34;' : 'color: #00a151;'
        },
        detailClick(item) {
            console.log(item)
            //提取数据
            var recordList = this.incomeExpenseList.filter(x => x.mainClassify == item.classify);
            var data = { classifyName: item.classifyName, recordList: recordList };
            uni.navigateTo({
                url: `./statisticsDetail?data=` + encodeURIComponent(JSON.stringify(data))
            });
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
    height: 320rpx;
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
        height: 180rpx;
    }
}
.detail {
    padding: 30rpx;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
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
            justify-content: space-evenly;
            .item-value {
                font-size: 32rpx;
            }
            .item-num {
                font-size: 20rpx;
                color: #7A7E83;
            }
        }
    }
}
.click-bg {
	background-color:rgb(240, 240, 240);
}
</style>