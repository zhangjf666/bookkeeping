<template>
    <view class="content">
        <scroll-view scroll-y class="scroll">
            <view class="time-list" v-if="sortType==0 || sortType == 2">
                <view class="time-range" v-for="(k, index) in timeSortList" :key="index">
                    <view class="time-text" v-if="sortType==0">{{dateFormat(k.date)}}</view>
                    <view class="item" v-for="(item) in k.subList" :key="item.id" hover-class="click-bg" hover-stay-time="200" @click="itemClick(item)">
                        <text class="item-image" :class="'iconfont icon-' + classifyImageFormat(item)" :style="itemColor(item)"></text>
                        <view class="item-text">
                            <view class="item-classify">{{classifyTextFormat(item)}}</view>
                            <view class="item-remark" v-if="sortType==0">{{item.remark}}</view>
                            <view class="item-remark" v-else>{{remarkTextFormat(item)}}</view>
                        </view>
                        <view class="item-amount" :style="itemColor(item)">{{amountFormat(item.amount)}}</view>
                    </view>
                </view>
            </view>
            <view class="amount-list" v-if="sortType==1">
                <view class="item" v-for="(item) in amountSortList" :key="item.id" hover-class="click-bg" hover-stay-time="200" @click="itemClick(item)">
                    <text class="item-image" :class="'iconfont icon-' + classifyImageFormat(item)" :style="itemColor(item)"></text>
                    <view class="item-text">
                        <view class="item-classify">{{classifyTextFormat(item)}}</view>
                        <view class="item-remark">{{remarkTextFormat(item)}}</view>
                    </view>
                    <view class="item-amount" :style="itemColor(item)">{{amountFormat(item.amount)}}</view>
                </view>
            </view>
        </scroll-view>
    </view>
</template>

<script>
import { formatNumber } from "../utils/utils.js"
export default {
    name: 'recordList',
    props:{
        list: Array,
        sortType: Number
    },
    data() {
        return {

        }
    },
    computed: {
        //按金额排序
        amountSortList() {
            var amountList = this.$u.deepClone(this.list);
            amountList = amountList.sort((a,b) => {
                return b.amount-a.amount;
            })
            return amountList;
        },
        //按时间排序
        timeSortList() {
            // 定义空数组，用于存储新组装的数据
            let newArr = [];
            // 遍历数组
            this.list.forEach((item, i) => {
                // 默认当前操作的数据下标 -1 
                let index = -1;
                // 判断数组中是否已经存在当前遍历数据的时间
                let isExists = newArr.some((newItem, j) => {
                    if (item.date == newItem.date) {
                        // 存在就保存当前数据下标  用于插入数据
                        index = j;
                        return true;
                    }
                })
                // 如果没有就存储一条新对象数据
                if (!isExists) {
                    newArr.push({
                        date: item.date,
                        subList: [item]
                    })
                } else {
                    // 如果有就插入到已存在的对象中
                    newArr[index].subList.push(item);
                }
            })
            // 返回新数组前按时间排序
            newArr = newArr.sort((a,b) => {
                return this.$moment(a.date) > this.$moment(b.date);
            })
            return newArr;
        },
        //返回金额前面的RMB图标
        amountFormat() {
            return (amount) => {
                return '￥' + formatNumber(amount);
            }
        },
        //返回分类图标名称
        classifyImageFormat() {
            return (item) => {
                return item.subClassifyImage ? item.subClassifyImage : item.mainClassifyImage;
            }
        },
        //返回分类名称
        classifyTextFormat() {
            return (item) => {
                var name = item.mainClassifyName;
                name += item.subClassifyName ? "-" + item.subClassifyName : "";
                return name;
            }
        },
        //日期格式化
        dateFormat() {
            return (date) => {
                return this.$moment(date).format('YYYY年MM月DD日 dddd')
            }
        },
        remarkTextFormat() {
            return (item) => {
                return this.$moment(item.date).format('YYYY年MM月DD日') + ' ' + item.remark;
            }
        }
    },
    methods: {
        itemClick(item) {
            uni.navigateTo({
                url: `/pages/record/record?item=` + encodeURIComponent(JSON.stringify(item))
            });
        },
        itemColor(item) {
            return item.type === '0' ? 'color: #d83d34;' : 'color: #00a151;'
        }
    }
}
</script>

<style lang="scss" scoped>
.content {
    width: 100%;
    display: flex;
    flex-direction: column;
    .time-text {
        margin-top: 10rpx 0rpx 10rpx 0rpx;
        font-size: 22rpx;
        color: #7A7E83;
    };
    .item {
        width: 100%;
        height: 120rpx;
        display: flex;
        flex-direction: row;
        align-items: center;
        border-bottom-style: solid;
        border-bottom-width: 1rpx;
        border-bottom-color: rgb(220, 220, 220);
        .item-image {
            font-size: 50rpx;
            margin-right: 20rpx
        };
        .item-text {
            display: flex;
            flex-direction: column; 
            .item-classify {
                font-size: 30rpx;
            };
            .item-remark {
                font-size: 22rpx;
                color: #7A7E83;
            }
        };
        .item-amount {
            margin-left: auto;
            font-size: 40rpx;
        }
    }
}
.click-bg {
	background-color:rgb(240, 240, 240);
}
</style>