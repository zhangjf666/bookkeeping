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
        <u-subsection :list="list" :current="type" @change="subsectionChange" active-color="#252569" mode="subsection" font-size="28" height="56"></u-subsection>
    </view>
    <view class="amount">
        <view class="amount-text">账单金额</view>
        <u-input class="amount-number" @focus="getFocus" :clearable="false" :focus="true" type="number" v-model="formatAmount" input-align="right"></u-input>
    </view>
    <view class="classify">
        <swiper ref="expenseSwiper" id="expense" :style="'height:100%'" v-if="type==0" indicator-dots :current="expenseSwiperCurrent">
            <swiper-item class="classify-swiper-item" v-for="(item, index) in showClassifyList['expense']" :key="index">
                <u-grid :col="5" :border="false">
                    <u-grid-item v-for="(k) in item" :key="k.id" :index="k.id" @click="classifyClick">
                        <text class="item-image" :class="'iconfont icon-' + k.image" :style="isSelect(k)"></text>
                        <view class="item-text">{{k.name}}</view>
                    </u-grid-item>
                </u-grid>
            </swiper-item>
        </swiper>
        <swiper ref="incomeSwiper" id="income" :style="'height:100%'" v-if="type==1" indicator-dots :current="incomeSwiperCurrent">
            <swiper-item class="classify-swiper-item" v-for="(item, index) in showClassifyList['income']" :key="index">
                <u-grid :col="5" :border="false">
                    <u-grid-item v-for="(k) in item" :key="k.id" :index="k.id" @click="classifyClick">
                        <text class="item-image" :class="'iconfont icon-' + k.image" :style="isSelect(k)"></text>
                        <view class="item-text">{{k.name}}</view>
                    </u-grid-item>
                </u-grid>
            </swiper-item>
        </swiper>
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
    <u-keyboard ref="uKeyboard" mode="number" :tooltip="false" v-model="isShowKeyboard" @change="valChange" @backspace="backspace"></u-keyboard>
  </view>
</template>

<script>
import UIcon from '../../uview-ui/components/u-icon/u-icon.vue';
import classify from './classify.vue';
import { mapGetters, mapMutations, mapState } from "vuex";

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
      //支出收入显示主分类
      showClassifyList: {},
      //支出选中分类
      expenseMainClassify:"",
      //收入选中分类
      incomeMainClassify:"",
      //显示日历
      isShowCalendar: false,
      //显示键盘
      isShowKeyboard: false,
      //swiper显示第几个
      expenseSwiperCurrent: 0,
      incomeSwiperCurrent:0
    };
  },
  onLoad() {
      this.mainClassifyList = this.classify.filter(item => { return item.pid == -1});
      this.updateExpenseClassifyList();
      this.updateIncomeClassifyList();
  },
  computed: {
      ...mapState(['accountBook','classify','userConfig']),
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
    },
    classifyClick(index) {
      if(this.type == 0){
        this.expenseMainClassify = index
      } else {
        this.incomeMainClassify = index
      }
    },
    subsectionChange(index) {
      this.type = index
    },
    isSelect(item) {
      if(this.type === 0){
        return item.id == this.expenseMainClassify ? 'color: #d83d34;' : 'color: #7A7E83';
      } else {
        return item.id == this.incomeMainClassify ? 'color: #00a151;' : 'color: #7A7E83';
      }
    },
    updateExpenseClassifyList() {
      let tlist = this.mainClassifyList.filter(item => { return item.type == 0})
      let list = []
      for (let i = 0; i < tlist.length; i += 10) {
        let endVal = i + 10;
        if (endVal > tlist.length) {
          endVal = tlist.length;
        }
        list.push(tlist.slice(i, endVal));
      }
      this.showClassifyList['expense'] = list;
      if(this.expenseMainClassify == null || this.expenseMainClassify == '') {
          this.expenseMainClassify = list[0][0].id;
          this.expenseSwiperCurrent = 0;
        }
    },
    updateIncomeClassifyList() {
        let tlist = this.mainClassifyList.filter(item => { return item.type == 1})
        let list = []
        for (let i = 0; i < tlist.length; i += 10) {
          let endVal = i + 10;
          if (endVal > tlist.length) {
            endVal = tlist.length;
          }
          list.push(tlist.slice(i, endVal));
        }
        this.showClassifyList['income'] = list;
        if(this.incomeMainClassify == null || this.incomeMainClassify == '') {
          this.incomeMainClassify = list[0][0].id;
          this.incomeSwiperCurrent = 0;
        }
    },
    valChange(val) {
      console.log(val)
      this.amount += val;
    },
    backspace() {
      // 删除value的最后一个字符
			if(this.amount.length) {
        this.amount = this.amount.substr(0, this.amount.length - 1);
      } else {
        this.amount = 0;
      }
    },
    getFocus() {
      this.isShowKeyboard = true;
    }
  },
  watch: {
    type() {
      
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
        height: 40rpx;
        font-size: 80rpx;
        font-weight: bold;
    }
}
.classify {
    width: 100%;
    padding: 0rpx 30rpx 0rpx 30rpx;
    display: flex;
    flex-direction: column;
    height: 350rpx;
    .item-image {
      font-size: 50rpx;
    }
    .item-text {
      margin-top: 10rpx;
      font-size: 16rpx;
    }
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