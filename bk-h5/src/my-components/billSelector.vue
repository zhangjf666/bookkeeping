<template>
    <view class="billSelectorContent">
        <view class="select">
            <view class="selectButton" @click="openOption">选择</view>
            <view class="selectContent" @click="openOption">{{selectText}}</view>
            <view class="selectFilter" :style="filterColor" @click="filterClick">
                <view>筛选</view>
                <text class="iconfont icon-shaixuan"></text>
            </view>
        </view>
        <u-popup v-model="popUpShow" mode="bottom" width="100%" height="50%" @close="selectOver">
            <view>
                <view class="selectMode">
                    <u-subsection :list="subsectionList" :current="mode" @change="subsectionChange" active-color="#252569" mode="subsection" font-size="28" height="56" width="60%"></u-subsection>
                </view>
                <view class="selectPicker">
                    <date-picker id="year" :dateValue.sync="normalPickerValue" v-if="mode == 0 || mode == 1" :type="mode"></date-picker>
                    <view class="custom" v-if="mode == 2">
                        <view @click="showBegin" class="beginTitle">
                            <view>开始时间</view>
                            <view>{{beginPickerText}}</view>
                        </view>
                        <date-picker id="begin" :dateValue.sync="beginPickerValue" v-if="showBeginPicker" :type="mode"></date-picker>
                        <view @click="showEnd" class="endTitle">
                            <view>结束时间</view>
                            <view >{{endPickerText}}</view>
                        </view>
                        <date-picker id="end" :dateValue.sync="endPickerValue" v-if="showEndPicker" :type="mode"></date-picker>
                    </view>
                </view>
            </view>
        </u-popup>
        <u-popup v-model="filterShow" mode="right" width="85%">
            <scroll-view scroll-y class="filter">
                <view class="filterMainTitle">筛选收支类型</view>
                <view class="filterContent">
                    <view class="filterButton" :class="filterAllSelected(0)" hover-class="click-bg" hover-stay-time="200" @click="allExpense">
                        <text>全部支出</text>
                    </view>
                    <view class="filterButton" :class="filterAllSelected(1)" hover-class="click-bg" hover-stay-time="200" @click="allIncome">
                        <text>全部收入</text>
                    </view>
                </view>
                <view class="filterMainTitle">筛选分类</view>
                <view class="filterSubTitle" style="color: #d83d34;">支出</view>
                <view class="filterContent">
                    <view class="filterButton" :class="filterSelected(item)" v-for="(item) in expenseList" :key="item.id" @click="filterButtonClick(item)" hover-class="click-bg" hover-stay-time="200">
                        <text>{{item.name}}</text>
                    </view>
                </view>
                <view class="filterSubTitle" style="color: #00a151;">收入</view>
                <view class="filterContent">
                    <view class="filterButton" :class="filterSelected(item)" v-for="(item) in incomeList" :key="item.id" @click="filterButtonClick(item)" hover-class="click-bg" hover-stay-time="200">
                        <text>{{item.name}}</text>
                    </view>
                </view>
            </scroll-view>
            <view class="filterButtom">
                <view @click="filterReset" :style="resetColor" hover-class="click-bg" hover-stay-time="200">重置</view>
                <view @click="filterConfirm" hover-class="click-bg" hover-stay-time="200">确定</view>
            </view>
        </u-popup>
    </view>
</template>

<script>
import { mapState } from 'vuex';
import datePicker from './datePicker.vue';
export default {
    components: { datePicker },
    //日期选择器
    name: 'billSelector',
    data() {
        return {
            mode: 0,
            //选中的收支类型
            selectClassifyList: [],
            subsectionList: ['月账单','年账单','自定义'],
            popUpShow: false,
            filterShow: false,
            showBeginPicker: true,
            showEndPicker: false,
            //月和年选择器的值
            normalPickerValue: [],
            //自定义选择器的值
            beginPickerValue: [],
            endPickerValue: [],
            allExpenseSelected: false,
            allIncomeSelected: false
        }
    },
    mounted() {
        this.normalPickerValue = [this.$moment(new Date()).year(),this.$moment(new Date()).month(),this.$moment(new Date()).date()];
        this.beginPickerValue = [this.$moment(new Date()).year(),this.$moment(new Date()).month(),this.$moment(new Date()).date()];
        this.endPickerValue = [this.$moment(new Date()).year(),this.$moment(new Date()).month(),this.$moment(new Date()).date()];
    },
    methods: {
        subsectionChange(e) {
            this.mode = e;
        },
        openOption(){
            this.popUpShow = true;
        },
        showBegin() {
            this.showBeginPicker = true;
            this.showEndPicker = false;
        },
        showEnd() {
            this.showEndPicker = true;
            this.showBeginPicker = false;
        },
        selectOver() {
            var option = {};
            option['mode'] = this.mode;
            option['classifyList'] = this.selectClassifyList;
            if(this.mode == 0 || this.mode == 1){
                option['beginDate'] = this.$moment(this.normalPickerValue).format('YYYY-MM-DD');
                option['endDate'] = option['beginDate'];
            } else if(this.mode == 2){
                option['beginDate'] = this.$moment(this.beginPickerValue).format('YYYY-MM-DD');
                option['endDate'] = this.$moment(this.endPickerValue).format('YYYY-MM-DD');
            }
            this.$emit('change', option);
        },
        filterClick() {
            this.filterShow = true;
        },
        filterReset() {
            this.selectClassifyList = [];
        },
        filterConfirm() {
            this.filterShow = false;
            this.selectOver();
        },
        allExpense() {
            var list = this.classify.filter((item) => { return item.type == '0' && item.pid == -1; })
            if(this.allExpenseSelected) {
                //移除所有选中支出
                for(var i = 0; i< list.length; i++){
                    var index = this.selectClassifyList.findIndex(x => x.id == list[i].id)
                    if(index != -1){
                        this.selectClassifyList.splice(index, 1);
                    }
                }
            } else {
                //添加所有选中支出
                for(var i = 0; i< list.length; i++){
                    var index = this.selectClassifyList.findIndex(x => x.id == list[i].id)
                    if(index == -1){
                        this.selectClassifyList.push(list[i]);
                    }
                }
            }
        },
        allIncome() {
            var list = this.classify.filter((item) => { return item.type == '1' && item.pid == -1; })
            if(this.allIncomeSelected) {
                //移除所有选中收入
                for(var i = 0; i< list.length; i++){
                    var index = this.selectClassifyList.findIndex(x => x.id == list[i].id)
                    if(index != -1){
                        this.selectClassifyList.splice(index, 1);
                    }
                }
            } else {
                //添加所有选中收入
                for(var i = 0; i< list.length; i++){
                    var index = this.selectClassifyList.findIndex(x => x.id == list[i].id)
                    if(index == -1){
                        this.selectClassifyList.push(list[i]);
                    }
                }
            }
        },
        filterButtonClick(item) {
            var index = this.selectClassifyList.findIndex(x => x.id == item.id)
            if(index == -1){
                this.selectClassifyList.push(item);
            } else {
                this.selectClassifyList.splice(index, 1);
            }
        }
    },
    computed: {
        ...mapState(['classify']),
        beginPickerText() {
            return this.$moment(this.beginPickerValue).format('YYYY年MM月DD日 dddd')
        },
        endPickerText() {
            return this.$moment(this.endPickerValue).format('YYYY年MM月DD日 dddd')
        },
        selectText() {
            if(this.mode == 0) {
                return this.$moment(this.normalPickerValue).format('YYYY年MM月')
            } else if(this.mode == 1){
                return this.$moment(this.normalPickerValue).format('YYYY年')
            } else if(this.mode == 2){
                return this.$moment(this.beginPickerValue).format('YYYY年MM月DD日') + '-' + this.$moment(this.endPickerValue).format('YYYY年MM月DD日');
            }
            return '';
        },
        expenseList() {
            var list = this.classify.filter((item) => {
                return item.type == '0' && item.pid == -1;
            })
            return list;
        },
        incomeList() {
            var list = this.classify.filter((item) => {
                return item.type == '1' && item.pid == -1;
            })
            return list;
        },
        filterColor() {
            return this.selectClassifyList.length > 0 ? 'color: #d83d34;' : ''
        },
        filterSelected() {
            return (item) => {
                return this.selectClassifyList.findIndex(x => x.id == item.id) != -1 ? 'filterButtonSelected' : '';
            }
        },
        filterAllSelected() {
            return (type) => {
                var all = true;
                var list = type == 0 ? this.expenseList : this.incomeList;
                for(var i = 0; i<list.length; i++) {
                    if(this.selectClassifyList.findIndex(x => x.id == list[i].id) == -1){
                        all = false;
                        break;
                    }
                }
                if(type == 0){
                    this.allExpenseSelected = all;
                } else {
                    this.allIncomeSelected = all;
                }
                return all ? 'filterButtonSelected' : '';
            }
        },
        resetColor() {
            return this.selectClassifyList.length > 0 ? 'color: #000;' : ''
        },
    }
}
</script>

<style lang="scss" scoped>
$mColor: #d83d34;
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
        width: 80rpx;
        font-size: 28rpx;
    }
    .selectMode {
        padding: 30rpx;
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        .u-subsection {
            width: 60%;
        }
    }
    .selectPicker {
        padding: 0rpx 30rpx 30rpx 30rpx;
        display: flex;
        align-items: flex-end;
        justify-content: center;
        width: 100%;
        height: 600rpx;
    }
    .selectContent {
        color: white;
        font-size: 28rpx;
    }
    .selectFilter {
        display: flex;
        margin-left: auto;
        flex-direction: row;
        justify-content: center;
        align-items: flex-end;
        color: white;
        margin-left: auto;
        border-left-color: white;
        border-left-style: solid;
        border-left-width: 1px;
        font-size: 28rpx;
        :first-child {
            display: flex;
            padding-left: 10rpx;
        }
        :last-child {
            width: 20rpx;
            display: flex;
            font-size: 28rpx;
            margin-left: auto;
        }
    }
    .custom {
        display: flex;
        flex-direction: column;
        width: 100%;
        height: 100%;
        .beginTitle {
            font-size: 26rpx;
            display: flex;
            flex-direction: row;
            align-items: flex-start;
            justify-content: space-between;
            width: 100%;
            border-bottom-style: solid;
            border-bottom-width: 1px;
            border-bottom-color: #e0e0e0;
            margin: 10rpx 0prx 10rpx 0rpx;
            height: 90rpx;
            :last-child {
                font-size: 24rpx;
                color:#696969;
            }
        }
        .endTitle {
            font-size: 26rpx;
            display: flex;
            flex-direction: row;
            align-items: flex-start;
            width: 100%;
            justify-content: space-between;
            border-top-style: solid;
            border-top-width: 1px;
            border-top-color: #e0e0e0;
            height: 90rpx;
            margin: 10rpx 0prx 10rpx 0rpx;
            :last-child {
                font-size: 24rpx;
                color:#696969;
            }
        }
    }
    .filter {
        padding-left: 30rpx;
        display: flex;
        flex-direction: column;
        width: 100%;
        height: 95%;
        .filterMainTitle {
            margin: 30rpx 0rpx 10rpx 0rpx;
            height: 60rpx;
            display: flex;
            flex-direction: column;
            border-bottom-color: #e0e0e0;
            border-bottom-style: solid;
            border-bottom-width: 1px;
            font-size: 30rpx;
        }
        .filterContent {
            display: flex;
            flex-direction: row;
            flex-wrap: wrap;
        }
        .filterButton {
            display: flex;
            flex-direction: row;
            align-items: center;
            justify-content: center;
            margin: 20rpx 20rpx 0 0;
            font-size: 20rpx;
            background-color: #f3f3f3;
            border-radius: 4rpx;
            border-color: rgba(255, 255, 255, 0);
            border-style: solid;
            border-width: 1px;
            :last-child {
                padding: 10rpx;
            }
        }
        .filterButtonSelected {
            border-color: #d83d34;
            border-style: solid;
            border-width: 1px;
        }
        .filterSubTitle {
            margin-top: 30rpx;
            display: flex;
            flex-direction: row;
            font-size: 26rpx;
        }
    }
    .filterButtom {
        height: 5%;
        margin-top: auto;
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: space-around;
        align-items: center;
        font-size: 36rpx;
        :first-child {
            display: flex;
            flex-direction: row;
            align-items: center;
            justify-content: center;
            width: 50%;
            height: 100%;
            background-color: #f3f3f3;
            color: #bdbdbd
        }
        :last-child {
            display: flex;
            flex-direction: row;
            align-items: center;
            justify-content: center;
            width: 50%;
            height: 100%;
            background-color: #d83d34;
            color: white;
        }
    }
}
button.primary {
  background-color: $mColor;
  border-radius: 4rpx;
  width: 100%;
}
.click-bg {
	background-color:rgb(114, 27, 27);
}
</style>