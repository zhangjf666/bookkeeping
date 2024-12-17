<template>
  <view class="content">
	<u-navbar
	  class="navbar"
	  :background="{ backgroundColor: '#252569' }"
	  back-icon-color="#fff"
	  back-icon-size="38"
	  back-text="收支记录"
	  z-index="0"
	  :back-text-style="{
	    color: '#fff',
	    fontSize: '34rpx',
	    marginLeft: '10rpx',
	  }"
	></u-navbar>
    <view class="mainContent">
      <view class="direction">
          <u-subsection :list="list" :current="type" @change="subsectionChange" active-color="#252569" mode="subsection" font-size="28" height="56"></u-subsection>
      </view>
      <view class="amount">
          <view class="amount-text">账单金额</view>
          <view class="amount-number" :style="inputStyle" @click="getFocus">{{formatAmount}}</view>
          <view class="amount-cursor" :style="inputBackground"></view>
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
              <u-checkbox v-if="type == 0" v-model="creditChecked" active-color="#d83d34" shape="circle" class="credit-check">信用卡产生</u-checkbox>
          </view>
          <view class="options-remark">
              <u-input v-model="remark" placeholder="请输入备注信息" />
              <u-checkbox v-model="addRemark" active-color="#d83d34" shape="circle" class="credit-check">加入常用备注</u-checkbox>
          </view>
      </view>
      <!-- 内容显示 -->
      <view class="remark">
          <view class="remarkTitle">
              <view>常用备注</view>
              <text v-if="remarkDeleteMode == false" class="iconfont icon-shanchu" @click="checkRemarkMode(true)"></text>
              <u-icon v-else name="checkmark" @click="checkRemarkMode(false)"></u-icon>
          </view>
          <view class="remarkContent">
              <!-- <view class="contentButton" v-for="(item) in userRemark" :key="item.id" @click="remarkClick(item)">
                  <text>{{item.remark}}</text>
                  <u-icon size="10" v-if="remarkDeleteMode" name="close" class="delIcon"></u-icon>
              </view> -->
              <u-tag class="contentButton" v-for="(item) in userRemark" :key="item.id" :text="item.remark" type="info" :closeable="remarkDeleteMode" @click="remarkClick(item)" @close="deleteRemark(item)"/>
          </view>
      </view>
    </view>
    <view class="buttom">
        <u-button class="again" @click="recordAgain" :loading="againBtnLoading" type="error">{{buttonText}}</u-button>
        <u-button class="save" @click="recordSave" :loading="saveBtnLoading" type="error">保 存</u-button>
    </view>
    <u-calendar v-model="isShowCalendar" @change="changeDate" :selectedDate="date"></u-calendar>
    <u-keyboard ref="uKeyboard" mode="number" :tooltip="false" v-model="isShowKeyboard" @change="valChange" @backspace="backspace"></u-keyboard>
  </view>
</template>

<script>
import { mapGetters, mapMutations, mapState } from "vuex";
import { createIncomeExpense, deleteIncomeExpense, updateIncomeExpense } from "@/api/incomeExpense.js";
import { deleteUserRemark } from "@/api/user.js";
import moment from 'moment'

export default {
  components: {
      
  },
  data() {
    return {
      //记录id
      id: '',
      //选择的账本
      accountBookId: '',
      //收入支出类型0:支出,1:收入
      type: 0,
      //金额
      amount: "0.00",
      //主分类
      mainClassify: "",
      //子分类
      subClassify: "",
      //信用卡
      isCreditCard: "",
      //加入常用备注
      isAddRemark: "0",
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
      incomeSwiperCurrent:0,
      //备注删除模式
      remarkDeleteMode: false,
      //再记一笔按钮加载
      againBtnLoading: false,
      //保存按钮加载
      saveBtnLoading: false
    };
  },
  onLoad(option) {
      this.mainClassifyList = this.classify.filter(item => { return item.pid == -1});
      this.updateExpenseClassifyList();
      this.updateIncomeClassifyList();
      this.initUserConfig();
      this.date = moment(new Date()).format('YYYY-MM-DD');

      if(option.item){
        const item = JSON.parse(decodeURIComponent(option.item));
        this.id = item.id;
        this.accountBookId = item.accountBookId;
        this.type = item.type == '0' ? 0 : 1;
        this.amount = item.amount + "";
        this.date = item.date;
        this.isToday = new Date().toLocaleDateString() == new Date(this.date).toLocaleDateString();
        if(this.type == 0){
          this.expenseMainClassify = item.mainClassify;
        } else {
          this.incomeMainClassify = item.mainClassify
        }
        this.remark = item.remark;
        this.isCreditCard = item.isCreditCard;
        this.updateSwiper()
      }
      this.getFocus();
  },
  computed: {
      ...mapState(['accountBook','classify','userConfig', 'user', 'userRemark']),
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
              this.setUserConfigItem({name: 'is_credit_card', value: this.isCreditCard});
          }
      },
      addRemark: {
          get: function() {
            return this.isAddRemark == '0' ? false: true;
          },
          set: function(e) {
              this.isAddRemark = e ? '1' : '0';
          }
      },
      inputStyle() {
        return this.type == 0 ? 'color: #d83d34;' : 'color: #00a151;';
      },
      inputBackground() {
        return this.isShowKeyboard ? this.type == 0 ? 'background-color: #d83d34;' : 'background-color: #00a151;' : 'opacity: 0;';
      },
      //左边按钮显示的文字
      buttonText() {
        return this.id != null && this.id != '' ? "删 除": "再记一笔"
      }
  },
  methods: {
    ...mapMutations(['updateSummary','setUserConfigItem','updateUserRemark']),
    initUserConfig() {
      //初始选中默认账本
      this.accountBook.forEach(item => {
        if(item.isDefault == '1'){
          this.accountBookId = item.id;
        }
      });
      //设置信用卡
      this.isCreditCard = this.userConfig['is_credit_card'].value;
    },
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
      this.updateSwiper()
    },
    isSelect(item) {
      if(this.type == 0){
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
    //更新swiper,指向选中的分类的那一页
    updateSwiper(){
        if(this.type == 0){
          if(this.expenseMainClassify == null || this.expenseMainClassify == '') {
            this.expenseSwiperCurrent = 0
          }
          let expenseList = this.showClassifyList['expense']
          for(let i = 0; i , expenseList.length; i++){
            if(expenseList[i].some(item => item.id == this.expenseMainClassify)){
              this.expenseSwiperCurrent = i
              break
            }
          }
        } else {
          if(this.incomeMainClassify == null || this.incomeMainClassify == '') {
            this.incomeSwiperCurrent = 0
          }
          let incomeList = this.showClassifyList['income']
          for(let i = 0; i , incomeList.length; i++){
            if(incomeList[i].some(item => item.id == this.incomeMainClassify)){
              this.incomeSwiperCurrent = i
              break
            }
          }
        }
    },
    valChange(val) {
      //判断是否是初始状态
      var isDefault = this.amount == "0.00" ? true : false;
      if(isDefault) {
        if(val != '.') {
          this.amount = String(val)
        } else {
          this.amount = "0."
        }
      } else {
        this.amount += "";
        if(val == '.' && this.amount.indexOf('.') != -1) {
          return;
        } else {
          var index = this.amount.indexOf('.')
          if(index != -1 && this.amount.length - index > 2){
            return;
          }
          this.amount += val;
        }
      }
    },
    backspace() {
      // 删除value的最后一个字符
      this.amount += "";
			if(this.amount.length == 1 || this.amount == "0.") {
        this.amount = '0.00';
      } else {
        this.amount = this.amount.substr(0, this.amount.length - 1);
      }
    },
    getFocus() {
      this.isShowKeyboard = true;
    },
    //保存记录
    recordSave() {
      if(this.againBtnLoading == true){
        return;
      }
      if(this.amount == 0){
        uni.showToast({
            icon: 'none',   
            duration: 3000,
            title: `请输入金额`
        });
        return;
      }
      this.saveBtnLoading = true;
      if(this.id == null || this.id == ''){
        var data = {userId : this.user.id, accountBookId: this.accountBookId, amount: this.amount, type : this.type + "", remark:this.remark, date: this.date, isAddRemark:this.isAddRemark};
        data['mainClassify'] = this.type == 0 ? this.expenseMainClassify : this.incomeMainClassify;
        if(this.type == 0) {
          data['isCreditCard'] = this.isCreditCard;
        }
        createIncomeExpense(data).then((res) => {
          this.updateSummary();
          if(this.isAddRemark == '1'){
            this.updateUserRemark();
          }
          //跳转到首页
          uni.switchTab({
            url: `/pages/index/index`,
          });
        }).finally(() => {
          this.saveBtnLoading = false;
        });
      } else {
        var data = {id:this.id, userId : this.user.id, accountBookId: this.accountBookId, amount: this.amount, type : this.type + "", remark:this.remark, date: this.date, isAddRemark:this.isAddRemark};
        data['mainClassify'] = this.type == 0 ? this.expenseMainClassify : this.incomeMainClassify;
        if(this.type == 0) {
          data['isCreditCard'] = this.isCreditCard;
        }
        updateIncomeExpense(data).then((res) => {
          this.updateSummary();
          if(this.isAddRemark == '1'){
            this.updateUserRemark();
          }
          //跳转回之前页面
          uni.navigateBack();
        }).finally(() => {
          this.saveBtnLoading = false;
        });
      }
      
    },
    //再记一笔或者是删除
    recordAgain() {
      if(this.saveBtnLoading == true){
        return;
      }
      if(this.amount == 0){
        uni.showToast({
            icon: 'none',   
            duration: 3000,
            title: `请输入金额`
        });
        return;
      }
      this.againBtnLoading = true;
      if(this.id == null || this.id == ''){
        var data = {userId : this.user.id, accountBookId: this.accountBookId, amount: this.amount, type : this.type + "", remark:this.remark, date: this.date, isAddRemark:this.isAddRemark};
        data['mainClassify'] = this.type == 0 ? this.expenseMainClassify : this.incomeMainClassify;
        if(this.type == 0) {
          data['isCreditCard'] = this.isCreditCard;
        }
        createIncomeExpense(data).then((res) => {
          this.updateSummary();
          if(this.isAddRemark == '1'){
            this.updateUserRemark();
          }
          //清空数据
          this.amount = "0.00";
          this.remark = "";
          this.isAddRemark = "0";
        }).finally(() => {
          this.againBtnLoading = false;
        })
      } else {
        deleteIncomeExpense([this.id]).then((res) => {
          this.updateSummary();
          //跳转回之前页面
          uni.navigateBack();
        }).finally(() => {
          this.againBtnLoading = false;
        })
      }
    },
    remarkClick(item) {
      if(!this.remarkDeleteMode){
        this.remark = item.remark;
        let tlist = this.mainClassifyList.filter(c => { return c.id == item.classifyId})
        if(tlist.length != 0){
          this.type = tlist[0].type
          if(this.type == 0){
            this.expenseMainClassify = item.classifyId
          } else {
            this.incomeMainClassify = item.classifyId
          }
        }
        this.updateSwiper()
      }
    },
    deleteRemark(item) {
      deleteUserRemark([item.id]).then((res) => {
        //移除备注
        var index = this.userRemark.findIndex(x=> x.id == item.id)
        if(index != -1){
          this.userRemark.splice(index, 1);
        }
      });
    },
    checkRemarkMode(e) {
      this.remarkDeleteMode = e;
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
  height: 100vh;
}
.navbar {
    display: flex;
    flex-direction: column;
  }
.mainContent {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-content: flex-start;
  align-items: flex-start;
  flex-flow: row wrap;
  height: 90%;
  .direction {
      display: flex;
      flex-direction: column;
      margin: 20rpx;
      width: 40%;
  }
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
        width: 30%;
    }
    .amount-number {
        font-size: 70rpx;
        margin-left: auto;
    }
    //光标动画
    .amount-cursor {
        width: 2px;
        height: 70rpx;
        display: flex;
        align-items: center;
        font-size: 70rpx;
        animation:cursorBlink 1.2s infinite steps(1, start);
        @keyframes cursorBlink {
          0%, 100% {
            opacity: 0;
          }
          50% {
            opacity: 1;
          }
        }
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
      font-size: 22rpx;
    }
}
.options {
    width: 100%;
    padding: 30rpx;
    display: flex;
    flex-direction: column;
    flex-grow: 1;
    .options-date {
        width: 100%;
        display: flex;
        align-items: center;
        .credit-check {
            height: 40rpx;
            margin-left: auto;
        }
    }
    .options-remark {
        margin-top: 10rpx;
        width: 100%;
        border-bottom-style: solid;
        border-bottom-width: 1rpx;
        border-bottom-color: rgb(220, 220, 220);
        display: flex;
        align-items: center;
        .credit-check {
            height: 40rpx;
            margin-left: auto;
        }
    }
}
.remark {
    padding-left: 30rpx;
    padding-right: 30rpx;
    display: flex;
    flex-direction: column;
    width: 100%;
    height: 100%;
    font-size: 30rpx;
    .remarkTitle {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
        :last-child {
            font-size: 30rpx;
        }
    }
    .remarkContent {
        display: flex;
        flex-direction: row;
        width: 100%;
        flex-wrap: wrap;
        .contentButton {
            display: flex;
            flex-direction: row;
            align-items: center;
            justify-content: center;
            margin: 20rpx 20rpx 0 0;
            font-size: 26rpx;
            background-color: #f3f3f3;
            border-radius: 4rpx;
        }
    }
}
.buttom {
    display: flex;
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