data() {
  return {
    dishData: [],
    classificationData: [],
    recommendDishes: [], // 新增：用于存储推荐菜品数据
    carts: [], // 新增：用于存储购物车数据
    loading: true,
    page: 1,
    pageSize: 6,
    hasMore: true,
    imageLoadStatus: {}, // 记录图片加载状态
    cacheTime: 10 * 60 * 1000, // 缓存时间改为10分钟
    isFirstLoad: true,
    tableNum: '',
  }
},

onLoad(options) {
  // 获取桌号
  this.tableNum = options.table_num || '';
  // 尝试加载缓存数据
  if (this.loadCachedData()) {
    this.loading = false;
    return;
  }
  // 首次加载数据
  this.loadInitialData();
},

// 加载初始数据
async loadInitialData() {
  this.loading = true;
  const startTime = Date.now(); // 记录开始时间
  try {
    // 并行请求所有初始数据：分类、菜品（第一页）、推荐菜品、购物车
    const [categoryRes, dishRes, recommendRes, cartsRes] = await Promise.all([
      this.$request('/api/category/listAll', 'GET', {}),
      this.$request('/api/dish/list', 'GET', {
        page: 1,
        pageSize: this.pageSize
      }),
      this.$request('/order/recommend', 'GET', { limit: 5 }), // 获取推荐菜品
      this.$request('/api/order/carts', 'GET', {}) // 获取购物车数据
    ]);

    // 处理分类数据
    if (categoryRes.code === 200) {
      this.classificationData = categoryRes.content.categoryListAll;
    }

    // 处理菜品数据
    if (dishRes.code === 200 && dishRes.content && Array.isArray(dishRes.content)) {
      this.dishData = dishRes.content;
      this.page = 1; // 重置页码
      this.hasMore = dishRes.content.length === this.pageSize;
    } else {
      console.warn('加载初始菜品数据返回格式不正确:', dishRes);
      this.dishData = [];
      this.hasMore = false;
    }

    // 处理推荐菜品数据
    if (recommendRes.code === 0 && recommendRes.recommendList && Array.isArray(recommendRes.recommendList)) {
      this.recommendDishes = recommendRes.recommendList;
    } else {
      console.warn('加载推荐菜品数据返回格式不正确或无数据:', recommendRes);
      this.recommendDishes = [];
    }

    // 处理购物车数据
    if (cartsRes.code === 0 && cartsRes.carts && Array.isArray(cartsRes.carts)) {
      this.carts = cartsRes.carts;
    } else {
      console.warn('加载购物车数据返回格式不正确或无数据:', cartsRes);
      this.carts = [];
    }

    this.saveToCache();
  } catch (error) {
    console.error('加载数据失败:', error);
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'none'
    });
    // 错误时清空数据并禁用加载更多
    this.dishData = [];
    this.hasMore = false;
  } finally {
    this.loading = false;
    this.isFirstLoad = false;
  }
},

// 加载缓存数据
loadCachedData() {
  const cachedDishData = uni.getStorageSync('dishData');
  const cachedCategoryData = uni.getStorageSync('categoryData');
  const cacheTime = uni.getStorageSync('cacheTime');
  const now = new Date().getTime();
  
  if (cachedDishData && cachedCategoryData && cacheTime && (now - cacheTime < this.cacheTime)) {
    this.dishData = cachedDishData;
    this.classificationData = cachedCategoryData;
    return true;
  }
  return false;
},

// 保存数据到缓存
saveToCache() {
  uni.setStorageSync('dishData', this.dishData);
  uni.setStorageSync('categoryData', this.classificationData);
  uni.setStorageSync('cacheTime', new Date().getTime());
},

// 下拉刷新
onPullDownRefresh() {
  this.page = 1;
  this.hasMore = true;
  this.loadInitialData();
  uni.stopPullDownRefresh();
},

// 上拉加载更多
onReachBottom() {
  if (this.hasMore && !this.loading) {
    this.page++;
    this.loadMoreData();
  }
},

// 加载更多数据
async loadMoreData() {
  if (this.loading) return;
  
  this.loading = true;
  try {
    const res = await this.$request('/api/dish/list', 'GET', {
      page: this.page,
      pageSize: this.pageSize
    });

    console.log('loadMoreData 后端返回:', res);

    if (res.code === 200 && res.content && Array.isArray(res.content)) {
      const newData = res.content;
      this.dishData = [...this.dishData, ...newData];
      this.hasMore = newData.length === this.pageSize;
    } else {
      console.warn('加载更多数据返回格式不正确或无数据:', res);
      this.hasMore = false;
    }
  } catch (error) {
    console.error('加载更多失败:', error);
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'none'
    });
    this.hasMore = false;
  } finally {
    this.loading = false;
  }
},

// 图片加载成功
onImageLoad(index) {
  this.$set(this.imageLoadStatus, index, 'loaded');
},

// 图片加载失败
onImageError(index) {
  this.$set(this.imageLoadStatus, index, 'error');
  this.dishData[index].image = '/static/images/default-dish.png';
},

// 预加载图片
preloadImages() {
  const preloadCount = 3;
  const currentIndex = this.page * this.pageSize;
  
  for (let i = 0; i < preloadCount; i++) {
    const index = currentIndex + i;
    if (index < this.dishData.length) {
      const img = new Image();
      img.src = this.dishData[index].image;
    }
  }
},

<template>
  <view class="container">
    <!-- 骨架屏 -->
    <view v-if="loading" class="skeleton">
      <view class="skeleton-header">
        <view class="skeleton-title"></view>
        <view class="skeleton-subtitle"></view>
      </view>
      <view class="skeleton-content">
        <view v-for="i in 6" :key="i" class="skeleton-item">
          <view class="skeleton-image"></view>
          <view class="skeleton-text">
            <view class="skeleton-name"></view>
            <view class="skeleton-price"></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 实际内容 -->
    <view v-else>
      <view class="dish-list">
        <view v-for="(item, index) in dishData" :key="index" class="dish-item">
          <image 
            :src="item.image" 
            mode="aspectFill" 
            lazy-load
            @load="onImageLoad(index)"
            @error="onImageError(index)"
            class="dish-image"
          />
          <view class="dish-info">
            <text class="dish-name">{{item.name}}</text>
            <text class="dish-price">¥{{item.price}}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss">
/* 骨架屏样式 */
.skeleton {
  padding: 20rpx;
  
  .skeleton-header {
    margin-bottom: 30rpx;
    
    .skeleton-title {
      height: 40rpx;
      background: #f0f0f0;
      border-radius: 8rpx;
      margin-bottom: 20rpx;
      animation: skeleton-loading 1.5s infinite;
    }
    
    .skeleton-subtitle {
      height: 30rpx;
      width: 60%;
      background: #f0f0f0;
      border-radius: 8rpx;
      animation: skeleton-loading 1.5s infinite;
    }
  }
  
  .skeleton-content {
    .skeleton-item {
      display: flex;
      margin-bottom: 30rpx;
      
      .skeleton-image {
        width: 160rpx;
        height: 160rpx;
        background: #f0f0f0;
        border-radius: 8rpx;
        margin-right: 20rpx;
        animation: skeleton-loading 1.5s infinite;
      }
      
      .skeleton-text {
        flex: 1;
        
        .skeleton-name {
          height: 32rpx;
          background: #f0f0f0;
          border-radius: 8rpx;
          margin-bottom: 16rpx;
          animation: skeleton-loading 1.5s infinite;
        }
        
        .skeleton-price {
          height: 28rpx;
          width: 40%;
          background: #f0f0f0;
          border-radius: 8rpx;
          animation: skeleton-loading 1.5s infinite;
        }
      }
    }
  }
}

@keyframes skeleton-loading {
  0% {
    opacity: 0.6;
  }
  50% {
    opacity: 1;
  }
  100% {
    opacity: 0.6;
  }
}

.swiper {
  height: 300rpx;
  border-radius: 15rpx;
  overflow: hidden;
  margin: 0 20rpx; /* 保持左右边距，移除上下边距 */
  box-shadow: 0 8rpx 25rpx rgba(0, 0, 0, 0.15);
  position: fixed; /* 固定定位 */
  top: 0; /* 距离顶部0 */
  left: 0; /* 距离左边0 */
  right: 0; /* 距离右边0 */
  z-index: 99; /* 确保在其他内容之上 */
  background-color: #f7d45f; /* 添加背景色，防止透明 */
}

.swiper-item {
  display: block;
  height: 300rpx;
  position: relative;
}

/* 推荐菜品轮播样式 */
.recommend-item {
  height: 100%;
  width: 100%;
}

.recommend-bg {
  height: 100%;
  width: 100%;
  position: relative;
}

.recommend-image {
  width: 100%;
  height: 100%;
  border-radius: 15rpx;
}

.recommend-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%; /* 覆盖整个图片，使其颜色变暗 */
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0));
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 20rpx;
  box-sizing: border-box;
  border-radius: 15rpx;
}

.recommend-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 10rpx;
}

.recommend-description {
  font-size: 24rpx;
  color: #eee;
  margin-bottom: 10rpx;
}

.recommend-price {
  font-size: 32rpx;
  color: #fff;
  font-weight: bold;
}

.recommend-hot {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  background-color: #ff6b6b;
  color: #fff;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: bold;
}

/* 默认轮播图样式 */
.default-item {
  height: 100%;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background-color: #f0f0f0; /* 默认背景色 */
  border-radius: 15rpx;
}

.default-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 15rpx;
}

.default-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5); /* 黑色半透明遮罩 */
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 15rpx;
}

.default-text {
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
}

/* 滚动区域样式 */
.scroll-Hei {
  height: calc(100vh - var(--status-bar-height) - 44px);
  /* 根据 swiper 的高度和 margin 调整 padding-top */
  padding-top: 340rpx; /* swiper 高度300rpx + 左右margin 20rpx*2 = 340rpx */
}

/* 菜品列表样式 */
.dish-list {
  display: flex;
  flex-wrap: wrap;
  padding: 20rpx;
  justify-content: space-between;
}

.dish-item {
  width: calc(50% - 15rpx);
  margin-bottom: 30rpx;
  background-color: #fff;
  border-radius: 15rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 25rpx rgba(0, 0, 0, 0.1);
}

.dish-image {
  width: 100%;
  height: 250rpx;
  object-fit: cover;
}

.dish-info {
  padding: 20rpx;
  display: flex;
  flex-direction: column;
}

.dish-name {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
  color: #333;
}

.dish-price {
  font-size: 28rpx;
  color: #ff6b6b;
  font-weight: bold;
}
</style>

<script>
export default {
  watch: {
    dishData: {
      handler(newVal) {
        if (newVal.length > 0) {
          this.preloadImages();
        }
      },
      deep: true
    }
  }
}
</script>