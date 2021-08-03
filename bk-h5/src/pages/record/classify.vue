<template>
    <view class="classify-list">
        <swiper :style="'height:100%'" next-margin="50rpx" v-if="currentList.length > 0">
            <swiper-item class="classify-swiper-item" v-for="(item, index) in classifyList" :key="index">
                <u-grid :col="5" @click="classifyClick">
                    <u-grid-item v-for="(k) in item" :key="k.id">
                        <u-icon class="item-image" :name="k.image"></u-icon>
                        <view class="item-text">{{k.name}}</view>
                    </u-grid-item>
                </u-grid>
            </swiper-item>
        </swiper>
    </view>
</template>

<script>

export default {
    props: {
		currentList: {
			type: Array
		},
        currentSelect: {
            type: String
        },
        type: {
            type: String
        }
	},
    computed: {
		classifyList() {
			let list = [];
			for (let i = 0; i < this.currentList.length; i += 10) {
				let endVal = i + 10;
				if (endVal > this.currentList.length) {
					endVal = this.currentList.length;
				}
				list.push(this.currentList.slice(i, endVal));
			}
			return list;
		}
	},
    methods: {
        
    }
}
</script>

<style lang="scss" scoped>
.classify-list {
    display: flex;
    width: 100%;
    flex-direction: column;
    height: 180px;
    .classify-swiper-item {
        
    }
}
</style>