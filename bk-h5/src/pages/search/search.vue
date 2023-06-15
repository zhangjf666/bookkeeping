<template>
    <view class='content'>
        <!-- 头部导航栏 -->
        <u-navbar z-index="9999" back-icon-color="#fff" back-icon-size="38" :background="{background: '#252569'}">
			<view class="slot-wrap">
				<u-search @search="getSearch" :show-action="false" color="rgb(255,255,255)" placeholder-color="rgba(255,255,255,.5)" bg-color="" search-icon="" :placeholder="placeholder" v-model="search"></u-search>
			</view>
		</u-navbar>
        <!-- 内容显示 -->
        <view v-if="showSearch && searchList.length > 0" class="history">
            <view class="historyTitle">
                <view>历史记录</view>
                <text class="iconfont icon-shanchu" @click="deleteSearchList"></text>
            </view>
            <view class="historyContent">
                <!-- <view class="contentButton" v-for="(item) in searchList" :key="item.id" @click="historyClick(item)">
                    <text>{{item.content}}</text>
                </view> -->
                <u-tag class="contentButton" v-for="(item) in searchList" :key="item.id" :text="item.content" type="info" @click="historyClick(item)"/>
            </view>
        </view>
        <view class="content-list" v-if="searchListVisible">
            <view class="option">
                <u-subsection :list="optionList" :current="optionType" @change="subsectionChange" active-color="#252569" mode="subsection" font-size="26" height="56"></u-subsection>
            </view>
            <record-list :list="filterList" :sortType="2"></record-list>
        </view>
    </view>
</template>

<script>
import { mapMutations, mapState } from 'vuex';
import { getIncomeExpense } from "@/api/incomeExpense.js";
import { nearlySearch, deleteUserAllSearch } from "@/api/user.js";
import recordList from "@/my-components/recordList.vue";

export default {
    components: {
        recordList
    },
    data() {
		return {
            optionList: [{
                name: "支出",
            },
            {
                name: "收入",
            }],
            optionType: 0,
            //搜索词
			search:'',
            placeholder:'搜索账单备注',
            //显示搜索列表页
            searchListVisible: false,
            //最近搜索词
            searchList: [],
            //显示搜索历史
            showSearch: true,
            //搜索结果
            searchResult: []
		};
	},
    onLoad(){
		this.getNearlySearch();
	},
    methods: {
        // 获得最近搜索词
		async getNearlySearch(){
            await nearlySearch({ userId: this.user.id }).then(data => {
                this.searchList = data;
            })
		},
        // 搜索触发事件
		getSearch(){
            getIncomeExpense({ userId: this.user.id, remark: this.search }).then(res => {
                this.searchResult = res;
                this.getNearlySearch();
            })
            this.showSearch = false;
            this.optionType = 0;
            this.searchListVisible = true;
		},
        //更新子组件状态
        updateComponent() {
            if(this.search == '' || this.search.lenght == 0){
                this.showSearch = true;
                this.searchListVisible = false;
            }
        },
        deleteSearchList() {
            deleteUserAllSearch({ userId: this.user.id }).then(res => {
                this.searchList = []
            })
        },
        historyClick(item) { 
            this.search = item.content;
            this.getSearch();
        },
        subsectionChange(e) {
            this.optionType = e;
        }
    },
    computed: {
        ...mapState(['user']),
        filterList() {
            if(this.optionType == 0){
                return this.searchResult.filter(x=>x.type == '0')
            } else {
                return this.searchResult.filter(x=>x.type == '1')
            }
        }
    },
    watch: {
        search() {
            this.updateComponent();
        },
        searchResult() {
            var expenseCount = this.searchResult.filter(x=>x.type == '0').length
            this.optionList[0].name = "支出 " + expenseCount + "笔";
            this.optionList[1].name = "收入 " + (this.searchResult.length - expenseCount) + "笔";
        }
    }
}
</script>

<style lang="scss" scoped>
$bColor: #d83d34;
.content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  height: 100vh;
}
.slot-wrap{
    width: 95%;
    /deep/.u-content{
        border-radius: 0 !important;
        padding: 0;
        border-bottom: 1px solid rgba($color: #fff, $alpha: .5);
    }
}
.history {
    padding: 30rpx;
    display: flex;
    flex-direction: column;
    width: 100%;
    height: 100%;
    font-size: 30rpx;
    .historyTitle {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
        :last-child {
            font-size: 30rpx;
        }
    }
    .historyContent {
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
    }
}
</style>