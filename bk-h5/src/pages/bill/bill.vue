<template>
    <view class="content">
        <view class="header">
            <view class="title">账单</view>
            <text class="iconfont icon-sousuo search" @click="goSearch"></text>
        </view>
        <view class="option">
            <bill-selector></bill-selector>
        </view>
        <!-- <div style="width: 100%; height: 100%" ref="chart"></div> -->
    </view>
</template>

<script>
import billSelector from "@/my-components/datePicker.vue";
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
            chart: null
        }
    },
    mounted() {
        //初始化echarts
        this.chart = echarts.init(this.$refs.chart);
        //配置数据
        let option = {
            xAxis: { type: 'category', data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] }, //X轴
            yAxis: { type: 'value' }, //Y轴
            series: [{ data: [120, 200, 150, 80, 70, 110, 130], type: 'bar' }] //配置项
        };
        this.chart.setOption(option);
    },
    methods: {
        goSearch() {

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
</style>