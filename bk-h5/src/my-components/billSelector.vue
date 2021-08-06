<template>
    <view class="billSelectorContent">
        <view class="select">
            <view class="selectButton" @click="openOption">选择</view>
            <view class="selectContent" @click="openOption">{{selectText}}</view>
            <view class="selectFilter">
                <text >筛选</text>
                <text class="iconfont icon-canyin"></text>
            </view>
        </view>
        <u-popup v-model="popUpShow" mode="bottom" width="100%" height="45%" @close="selectOver">
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
            //筛选收支类型
            classifyList:[],
            //选中的收支类型
            selectClassifyList: [],
            subsectionList: ['月账单','年账单','自定义'],
            popUpShow: false,
            showBeginPicker: true,
            showEndPicker: false,
            //月和年选择器的值
            normalPickerValue: [],
            //自定义选择器的值
            beginPickerValue: [],
            endPickerValue: []
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
                option['beginDate'] = this.$moment(this.normalPickerValue);
            } else if(this.mode == 2){
                option['beginDate'] = this.$moment(this.beginPickerValue);
                option['endDate'] = this.$moment(this.endPickerValue);
            }
            this.$emit('change', option);
        }
    },
    computed: {
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
        }
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
    }
    .selectFilter {
        color: white;
        margin-left: auto;
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
}
button.primary {
  background-color: $mColor;
  border-radius: 4rpx;
  width: 100%;
}
</style>