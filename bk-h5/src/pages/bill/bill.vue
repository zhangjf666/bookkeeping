<template>
    <view class="content">
        <view class="header">
            <view class="title">账单</view>
            <text class="iconfont icon-sousuo search" @click="goSearch"></text>
        </view>
        <view class="option">
            <bill-selector @change="dateChange"></bill-selector>
        </view>
        <view class="chart">
            <u-radio-group class="chartRadio" v-model="radioValue" size="26">
                <u-radio name="expense" active-color="#d83d34">{{expenseText}}</u-radio>
                <u-radio name="income" active-color="#00a151">{{incomeText}}</u-radio>
            </u-radio-group>
            <div style="width: 100%; height: 100%" ref="chart"></div>
        </view>
        
    </view>
</template>

<script>
import { mapState, mapMutations, mapGetters } from "vuex";
import billSelector from "@/my-components/billSelector.vue";
import { querySumPeriod } from "@/api/incomeExpense.js";
//echarts
import * as echarts from 'echarts/core';
import {
  BarChart,
  PieChart
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
    LegendComponent,
    DatasetComponent,
    CanvasRenderer
  ]
);
export default {
    components: {
        billSelector
    },
    data() {
        return {
            chart: null,
            expenseTotal: '支出',
            incomeTotal: '收入',
            radioValue: 'expense',
            incomeExpenseList: []
        }
    },
    onLoad() {
        //默认获取当月数据
        var param = {userId: this.user.id, queryMode: '0', mode: '0'};
        param['beginDate'] = this.$moment().format('YYYY-MM-DD');
        this.getSumPeriod(param);
    },
    mounted() {
        //初始化echarts
        this.chart = echarts.init(this.$refs.chart);
        //配置数据
        let option = {
            xAxis: { type: 'category', data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] }, //X轴
            yAxis: { type: 'value' }, //Y轴
            series: [{ data: [], type: 'bar' }] //配置项
        };
        this.chart.setOption(option);
    },
    methods: {
        //获取统计数据
        getSumPeriod(data) {
            querySumPeriod(data).then(res => {
                console.log(res);
                this.incomeTotal = res.incomeTotal;
                this.expenseTotal = res.expenseTotal;
                this.incomeExpenseList = res.incomeExpenseList;
            })
        },
        goSearch() {

        },
        dateChange(option) {

        }
    },
    computed: {
        ...mapState(['user']),
        expenseText() {
            return this.expenseTotal
        },
        incomeText() {
            return this.incomeTotal
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
  height: 100vh;
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
.chart {
    margin-top: -100rpx;
    width: 95%;
    height: 400rpx;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    display: flex;
    background-color: white;
    border-radius: 10rpx;
    box-shadow: 0px 6px 6px rgb(207, 207, 207);
    .chartRadio {
        padding-top: 30rpx;
        width: 100%;
        align-items: center;
        justify-content: center;
        flex-direction: row;
        display: flex;
    }
}
</style>