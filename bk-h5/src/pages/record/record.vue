<template>
  <view class="content">
    <u-navbar
      class="navbar"
      :background="{ backgroundColor: '#fff' }"
      back-icon-color="#000"
      back-icon-size="38"
      back-text="收支记录"
      z-index="0"
      :back-text-style="{
        color: '#000',
        fontSize: '30rpx',
        marginLeft: '10rpx',
      }"
    ></u-navbar>
    <view class="direction">
        <u-subsection :list="list" :current="type" active-color="#252569" mode="subsection" font-size="28" height="56"></u-subsection>
    </view>
    <view class="amount">
        <view class="amount-text">账单金额</view>
        <view class="amount-number">{{formatAmount}}</view>
    </view>
    <view class="classify">
        <classify :currentList="mainClassifyList" :currentSelect="mainClassify"></classify>
    </view>
    <view class="options">
        <view class="options-date">
            <u-icon name="calendar" :size="40"></u-icon>
            <text @click="showCalendar()">{{calendar}}</text>
            <u-checkbox v-model="creditChecked" shape="circle" class="credit-check">信用卡产生</u-checkbox>
        </view>
        <view class="options-remark">
            <u-input v-model="remark" placeholder="请输入备注信息" />
        </view>
    </view>
    <view class="buttom">
        <u-button class="again" @click="recordAgain" type="error">在记一笔</u-button>
        <u-button class="save" @click="recordSave" type="error">保 存</u-button>
    </view>
    <u-calendar v-model="isShowCalendar" @change="changeDate"></u-calendar>
  </view>
</template>

<script>
import UIcon from '../../uview-ui/components/u-icon/u-icon.vue';
import classify from './classify.vue';

export default {
  components: {
      classify,UIcon
  },
  data() {
    return {
      //收入支出类型0:支出,1:收入
      type: 0,
      //金额
      amount: 0,
      //主分类
      mainClassify: "",
      //子分类
      subClassify: "",
      //信用卡
      isCreditCard: "0",
      //发生日期
      date: "",
      //发生日期是否是当天
      isToday: true,
      //备注
      remark: "",
      //分段器类型
      list: [
        {
          name: "支出",
        },
        {
          name: "收入",
        },
      ],
      //主分类列表
      mainClassifyList: [],
      //显示日历
      isShowCalendar: false
    };
  },
  computed: {
      formatAmount() {
        return '￥' + this.amount
      },
      calendar() {
          if(this.isToday) {
              return '今天'
          }
          return this.date;
      },
      creditChecked: {
          get: function() {
            return this.isCreditCard == '0' ? false: true;
          },
          set: function(e) {
              this.isCreditCard = e ? '1' : '0';
          }
      }
  },
  methods: {
    showCalendar() {
        this.isShowCalendar = true;
    },
    changeDate(e) {
        this.isToday = e.isToday;
        this.date = e.result;
    }
  }
};
</script>

<style lang="scss" scoped>
$mColor: #d83d34;
.content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}
.direction {
    margin: 20rpx;
    width: 40%;
}
.amount {
    padding: 30rpx;
    width: 100%;
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    .amount-text {
        font-size: 32rpx;
        font-weight: bold;
    }
    .amount-number {
        font-size: 80rpx;
        font-weight: bold;
    }
}
.classify {
    width: 100%;
    padding: 30rpx;
    display: flex;
}
.options {
    width: 100%;
    padding: 30rpx;
    display: flex;
    flex-direction: column;
    .options-date {
        width: 100%;
        display: flex;
        align-items: center;
        .credit-check {
            margin-left: auto;
        }
    }
    .options-remark {
        margin-top: 10rpx;
        width: 100%;
        border-bottom-style: solid;
        border-bottom-width: 1rpx;
        border-bottom-color: rgb(220, 220, 220);
    }
}
.buttom {
    display: flex;
    position: fixed;
    left: 0;
    bottom: 0;
    padding: 30rpx;
    width: 100%;
    align-self: flex-end;
    .again {
        font-size: 26rpx;
        border-style: solid;
        border-width: 1px;
        border-color: $mColor;
        color: $mColor;
        background-color: #fff;
        width: 44%;
    }
    .save {
        background-color: $mColor;
        font-size: 26rpx;
        color: #fff;
        width: 44%;
    }
}
</style>