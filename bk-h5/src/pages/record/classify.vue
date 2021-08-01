<template>
    <view class="classify-list">
        <swiper :style="'height:100%'" next-margin="50rpx" v-if="currentList.length > 0">
            <swiper-item class="classify-swiper-item" v-for="(item, index) in classifyList" :key="index">
                <view class="song-item" hover-class="click-bg" hover-stay-time="200" @click="addPlay(k)" v-for="(k) in item" :key="k.id">
                    <u-image class="item-image" v-if="k.picUrl" :src="k.picUrl" mode="widthFix" width="100" height="100" border-radius="7px"></u-image>
                    <view class="item-text">
                        <view style="width: 80%">
                            <text class="song-name">{{ k.name }}</text>
                            <text class="song-singer">- {{ songSinger(k) }}</text>
                        </view>
                        <text class="iconfont item-icon" :class="showPlayIcon(k) ? 'icon-pause':'icon-play'"></text>
                    </view>
                </view>
                <u-grid :col="5" @click="classifyClick">
                    <u-grid-item v-for="(k) in item" :key="k.id">
                        <u-image class="item-image" :src="imageUrl" mode="widthFix" width="100" height="100" border-radius="7px"></u-image>
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
		},
        imageUrl() {
            return (k) => {
                return '../static/main-classify'+k.image;
            }
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
    height: 100%;
    .music-swiper-item {
        // padding-left: 12rpx;
        box-sizing: border-box;
        .song-item {
            display: flex;
            width: 100%;
            .item-image {
                margin: 8rpx 16rpx 0 0;
            }

            .item-text {
                width: calc(100% - 160rpx);
                display: flex;
                align-items: center;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                border-bottom: 1upx solid rgba(0, 0, 0, 0.1);
                .song-name {
                    color: #000;
                    font-size: 30rpx;
                    margin-right: 5px;
                    text-overflow: ellipsis;
                }
                .song-singer {
                    font-size: 12rpx;
                    color: #666;
                    text-overflow: ellipsis;
                }
                .item-icon {
                    margin-left: auto;
                    font-size: 40rpx;
                    color: #d83d34;
                }
            }
        }
    }
}
</style>