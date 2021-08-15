<template>
    <view class='content'>
        <!-- 头部导航栏 -->
        <u-navbar z-index="9999" back-icon-color="#fff" back-icon-size="38" :background="{background: '#252569'}">
			<view class="slot-wrap">
				<view>{{title}}</view>
			</view>
		</u-navbar>
        <view class="content-list">
            <view class="option">
                <u-subsection :list="optionList" :current="optionType" @change="subsectionChange" active-color="#252569" mode="subsection" font-size="26" height="56"></u-subsection>
            </view>
            <record-list :list="recordList" :sortType="optionType"></record-list>
        </view>
    </view>
</template>

<script>
import recordList from "@/my-components/recordList.vue";
export default {
    components: {
        recordList
    },
  data() {
      return {
          classifyName:'',
          recordList: [],
          optionList: [{
                name: "按时间",
            },
            {
                name: "按金额",
            }],
            optionType: 0,
      }
  },
  onLoad(option) {
      if(option.data){
          const data = JSON.parse(decodeURIComponent(option.data));
          this.classifyName = data.classifyName;
          this.recordList = data.recordList;
      }
  },
  methods: {
      subsectionChange(e) {
            this.optionType = e;
        }
  },
  computed: {
      title() {
          return this.classifyName + "(" + this.recordList.length + "笔)";
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
.slot-wrap{
    width: 90%;
    display: flex;
    align-items: center;
    justify-content: center;
	color: #fff;
    /deep/.u-content{
        border-radius: 0 !important;
        padding: 0;
        border-bottom: 1px solid rgba($color: #fff, $alpha: .5);
    }
}
.content-list {
    padding: 30rpx;
    display: flex;
    flex-direction: column;
    width: 100%;
    height: 100%;
    .option {
        display: flex;
        flex-direction: column;
        width: 100%;
        margin-bottom: 20rpx;
    }
}
</style>