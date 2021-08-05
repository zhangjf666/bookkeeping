<template>
    <view class="billSelectorContent">
        <view class="select">
            <view class="selectButton" @click="openOption">选择</view>
            <view class="selectContent"></view>
            <view class="selectFilter">
                <text >筛选</text>
                <text class="iconfont icon-canyin"></text>
            </view>
        </view>
        <!-- <u-popup v-model="popUpShow" mode="buttom" width="100%" height="40%">
            <view class="mode">
                <u-subsection :list="subsectionList" :current="mode" @change="subsectionChange" active-color="#252569" mode="subsection" font-size="28" height="56"></u-subsection>
            </view>
            <date-picker></date-picker>
        </u-popup> -->
    </view>
</template>

<script>
import datePicker from './datePicker.vue';
export default {
    components: { datePicker },
    //日期选择器
    name: 'billSelector',
    data() {
        return {
            mode: 0,
            //选中的年
            year: '',
            //选中的月份
            month:'',
            //开始日期
            startDate: '',
            //结束日期
            endDate: '',
            //筛选收支类型
            classifyList:[],
            //选择的年份
            selectTitle:'',
            subsectionList: ['月账单','年账单','自定义'],
            tabsList: [],
            popUpShow: false
        }
    },
    mounted() {
        this.startDate = this.$moment()
    },
    methods: {
        subsectionChange() {

        },
        openOption(){
            this.popUpShow = true;
        }
    },
    computed: {
        selectText() {
            if(this.mode == 0) {
                return this.year + '年' + this.month + '月';
            } else if(this.mode == 1) {
                return this.year + '年';
            } else {
                return this.$moment(this.startDate).format('YYYY年MM月DD日') + '-' + this.$moment(this.endDate).format('YYYY年MM月DD日')
            }
        }
    }
}
</script>

<style lang="scss" scoped>
.billSelectorContent {
    width: 100%;
    height: 200rpx;
    display: flex;
    flex-direction: column;
    background-color: #252569;
    .select {
        width: 100%;
        display: flex;
        flex-direction: row;
        font-size: 30rpx;
    }
    .selectButton {
        color: white;
        font-weight: bolder;
    }
    .mode {
        display: flex;
        align-items: center;
    }
    .selectContent {
        font-weight: bolder;
        color: white;
    }
    .selectFilter {
        font-weight: bolder;
        color: white;
        margin-left: auto;
    }
}
</style>