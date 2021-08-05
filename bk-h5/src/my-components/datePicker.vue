<template>
	<view class="datePickerContent">
		<picker-view class="picker-view" v-if="visible" :indicator-style="indicatorStyle" :value="value" @change="bindChange">
		    <picker-view-column>
		        <view class="item" v-for="(item,index) in years" :key="index">{{item}}年</view>
		    </picker-view-column>
		    <picker-view-column v-if="type != 1">
		        <view class="item" v-for="(item,index) in months" :key="index">{{item + 1}}月</view>
		    </picker-view-column>
		    <picker-view-column v-if="type == 2">
		        <view class="item" v-for="(item,index) in days" :key="index">{{item + 1}}日</view>
		    </picker-view-column>
		</picker-view>
	</view>
</template>

<script>
	export default {
		props: {
			type: Number,
			dateValue: Array
		},
		data() {
		    const date = new Date()
		    const years = []
		    const year = this.$moment(date).year()
		    const months = []
		    const month = this.$moment(date).month()
		    const days = []
		    const day = this.$moment(date).day()
		    for (let i = 2010; i <= date.getFullYear(); i++) {
		        years.push(i)
		    }
		    for (let i = 0; i < 12; i++) {
		        months.push(i)
		    }
		    for (let i = 0; i < 31; i++) {
		        days.push(i)
		    }
			return {
		        title: 'picker-view',
		        years,
		        year,
		        months,
		        month,
		        days,
		        day,
		        value: [9999, month - 1, day - 1],
		        visible: true,
		        // indicatorStyle: `height: ${Math.round(uni.getSystemInfoSync().screenWidth/(750/100))}px;`,
				indicatorStyle: `height: 60rpx;`,
				currentYear: 2019,
				currentMonth: 1,
				currentDay: 1
			}
		},
		mounted() {
			var date = new Date()
			this.currentYear = this.$moment(date).year()
			this.currentMonth = this.$moment(date).month()
			this.currentDay = this.$moment(date).day()

			if(this.dateValue != null){
				var init = [];
				init[0] = this.years.indexOf(this.dateValue[0]);
				init[1] = this.months.indexOf(this.dateValue[1]);
				init[2] = this.days.indexOf(this.dateValue[2]);
				this.value = init;
			}
		},
		computed: {
		},
		methods: {
			bindChange: function (e) {
			    const val = e.detail.value
			    this.year = this.years[val[0]]
			    this.month = this.months[val[1]]
			    this.day = this.days[val[2]]
				
				var y = parseInt(this.year)
				var m = parseInt(this.month)
				var d = parseInt(this.day)
				
				if(m==0||m==2||m==4||m==6||m==7||m==9||m==11){
					if(this.days.length!=31){
						this.days = []
						for (let i = 0; i < 31; i++) {
						    this.days.push(i)
						}
					}
				}else if(m==3||m==5||m==8||m==10){
					if(this.days.length!=30){
						this.days = []
						for (let i = 0; i < 30; i++) {
						    this.days.push(i)
						}
					}
				}else if(m==1){
					if((y%4==0&&y%100!=0)||(y%400==0)){//闰年
						if(this.days.length!=29){
							this.days = []
							for (let i = 0; i < 29; i++) {
							    this.days.push(i)
							}
						}
					}else{//平年
						if(this.days.length!=28){
							this.days = []
							for (let i = 0; i < 28; i++) {
							    this.days.push(i)
							}
						}
					}
				}
				//选择当前年月
				if(y==this.currentYear){
					if(this.months.length!=this.currentMonth){
						this.months = []
						for (let i = 0; i <= this.currentMonth; i++) {
						    this.months.push(i)
						}
					}
					if(m==this.currentMonth){
						if(this.days.length!=this.currentDay){
							this.days = []
							for (let i = 0; i <= this.currentDay; i++) {
							    this.days.push(i)
							}
						}
					}
				}else{
					this.months = []
					for (let i = 0; i < 12; i++) {
					    this.months.push(i)
					}
				}
				var date = [this.year,this.month,this.day];
				this.$emit('update:dateValue',date);
			},
		}
	}
</script>

<style lang="scss" scoped>
.datePickerContent{
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	background-color: #FFFFFF;
	width: 100%;
	height: 100%;
	.picker-view {
		width: 100%;
		height: 100%;
	}
	.item {
		font-size: 44rpx;
		height: 60rpx;
		align-items: center;
		justify-content: center;
		text-align: center;
	}
}
</style>