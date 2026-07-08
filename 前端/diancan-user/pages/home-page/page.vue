<template>
  <view>
    <view>
      <!-- 顶部 -->
      <view class="top-view">
        <view>{{ number_people }}人就餐</view>
        <view class="top-view-flex">
          <image src="/static/tab/fenxiang.svg" mode="widthFix" class="top-search"></image>
        </view>
      </view>
      <!-- 跑马灯区域 -->
      <view class="order-view">
        <view class="commodity">
          <!-- 左 -->
          <view class="order-left">
            <scroll-view scroll-y="true" class="scroll-Hei" :scroll-with-animation="true" :enhanced="true"
                         :show-scrollbar="false">
              <block v-for="(item,index) in itemize" :key="index">
                <view class="itemize-text" :class="{active: index == trigger}" @click="itemIze(index,item.cid)">
                  <text>{{ item.value }}</text>
                  <text v-if="item.sele_quantity > 0">{{ item.sele_quantity }}</text>
                </view>
              </block>
            </scroll-view>
          </view>
          <!-- 右 -->
          <view class="order-right">
            <!-- 跑马灯区域 -->
            <view class="marquee-area">
              <view class="marquee-text-container">
                <text class="marquee-text">{{ currentMarqueeMessage }}</text>
              </view>
            </view>
            <swiper class="swiper" circular :indicator-dots="true" :autoplay="true" :interval="3000"
                :duration="500" indicator-color="rgba(255,255,255,0.5)" indicator-active-color="#ff6b6b">
              <!-- 推荐菜品轮播 -->
              <swiper-item v-for="(item, index) in recommendDishes" :key="index">
                <view class="swiper-item recommend-item" @click="showRecommendDetail(item)">
                  <view class="recommend-bg">
                    <image :src="baseUrl + '/image/dish/' + item.image" mode="aspectFill" class="recommend-image"></image>
                    <view class="recommend-overlay">
                      <view class="recommend-content">
                        <view class="recommend-title">{{ item.name }}</view>
                        <view class="recommend-subtitle">热销推荐 · 已售{{ item.sales }}份</view>
                        <view class="recommend-price">
                          <text class="price-symbol">¥</text>
                          <text class="price-number">{{ item.total_price?item.total_price:10 }}</text>
                          <text class="price-unit">/{{ item.unit }}</text>
                        </view>
                      </view>
                      <view class="recommend-badge">
                        <text>HOT</text>
                      </view>
                    </view>
                  </view>
                </view>
              </swiper-item>
              <!-- 默认轮播图（当没有推荐数据时显示） -->
              <swiper-item v-if="recommendDishes.length === 0">
                <view class="swiper-item default-item">
                  <image src="https://bkimg.cdn.bcebos.com/pic/5366d0160924ab18972b2b5af2a9f1cd7b899e519d18" mode="aspectFill" class="default-image"></image>
                  <view class="default-overlay">
                    <text class="default-text">美味佳肴，等您品尝</text>
                  </view>
                </view>
              </swiper-item>
            </swiper>
            <scroll-view scroll-y="true" class="scroll-Hei" :scroll-with-animation="true" :enhanced="true"
                         :show-scrollbar="false" :scroll-into-view="scroll_into" @scroll="scroLl">
              <block v-for="(item,index) in goods" :key="index">
                <view :id="item.cid" class="rig-height">
                  <view class="classif">{{ item.label }}</view>
                  <view class="classif-goods" v-for="(itemgood,good_index) in item.dishList" :key="good_index"
                        @click="popup_item(true,index,good_index,item.cid,itemgood)">
                    <view class="goods-image">
                      <image :src="baseUrl+'/image/dish/'+itemgood.image" mode="aspectFill"></image>
                      <!-- 热销标签 -->
                      <view class="hot-tag" v-if="isHotDish(itemgood.name)">HOT</view>
                    </view>
                    <view class="goods-Price">
                      <view class="goods-name">
                        <text class="Bold">{{ itemgood.name }}</text>
                        <text class="Thinning">月售 {{ itemgood.monthlysale || 0 }}份</text>
                      </view>
                      <view class="unit-price">
                        <text class="Symbol">¥</text>
                        <text class="Bold">{{ itemgood.unitprice }}</text>
                        <text class="Thinning">/{{ itemgood.unit }}</text>
                      </view>
                    </view>
                    <view class="quantity">
                      <view class="quantity-btn">
                        <text class="quantity-icon" :class="{'disabled-text': itemgood.quantity === 0}" @click="handleReduce(index,good_index,item.cid,itemgood)">-</text>
                      </view>
                      <view>
                        <text v-if="itemgood.quantity >= 0">{{ itemgood.quantity }}</text>
                      </view>
                      <view class="quantity-btn">
                        <text class="quantity-icon" @click.stop="plus(index,good_index,item.cid,itemgood)">+</text>
                      </view>
                    </view>
                  </view>
                </view>
              </block>
              <view style="height: 400rpx;"></view>
            </scroll-view>
          </view>
        </view>
        <!-- 底部 -->
        <view class="order-bottom" @click="pop_Shopping()" :style="{'padding-bottom': Modelmes ? '68rpx' : ''}">
          <view class="Shopping" style="width: 115rpx;">
            <view class="Shopping-left">
              <image src="/static/tab/gouwuche.png" mode="widthFix"></image>
            </view>
            <view class="Shopping-number" v-if="total_quantity > 0">{{ total_quantity }}</view>
          </view>
          <view class="Shopping-title" v-if="total_quantity > 0">已点{{ total_quantity }}份菜品</view>
          <view class="place-order" @click="handleAddToCart">
            <button plain="true" open-type="getUserInfo">加入购物车</button>
          </view>
        </view>
      </view>
    </view>
    <!-- 购物车 -->
    <Cart v-if="card" :shopping_card="shopping_card"></Cart>
    <!-- 引入单个商品弹出 -->
    <Details v-if="popupitem" :pro_details="pro_details"></Details>
    <!-- 骨架屏 -->
    <Home v-if="exist"></Home>

  </view>
</template>

<script>
import {getBaseUrl, requestUtil} from "../../utils/requestUtil.js"

const app = getApp()
const {Modelmes} = app.globalData
// 骨架屏
import Home from '../skeleton-view/home.vue'
// 小程序端一次性只返回20条数据；云函数段100条；外部nodejs，java返回10条
// 引入购物车子组件
import Cart from './components/shopping-cart.vue'
// 引入单个商品弹出
import Details from './components/goods-details.vue'
// 订单编号
import {Code} from '../../config/order.js'
// 计算当天的销售额
import {analysis} from '../../config/Date_analysis.js'

const db = wx.cloud.database()
const _ = db.command
const good_collect = db.collection('order-data')
const dishes_data = db.collection('dishes-data')
export default {
  components: {Cart, Details, Home},
  data() {
    return {
      baseUrl: '',
      exist: true,
      Modelmes,
      itemize: [],//类目
      trigger: 0,//类目选中的值
      goods: [],//所有菜品
      heightset: [],//存储右边每一个分类菜品的高度
      tophei: 0,//滚动时距离顶部的高度
      scroll_into: '',
      card: false,//购物车隐藏
      shopping_card: [],//购物车里的数据
      popupitem: false,//单个商品弹出框隐藏
      pro_details: {},//单个商品弹出框里的数据
      tmplIds: 'FANEJh9NPNhJrLpqQx7UhNerntR5GwEsLKK-95tuvNM',//模板id
      number_people: 0,//用餐人数
      recommendDishes: [],//推荐菜品数据
      marqueeMessages: [], // 初始化为空数组，从API获取
      currentMarqueeIndex: 0,
      marqueeInterval: null
    }
  },
  methods: {
    // 点击类目加上背景色
    itemIze(index, cid) {
      this.trigger = index
      this.scroll_into = cid
      setTimeout(() => {
        this.scroll_into = ''
      }, 200)
    },
    // 右边菜品滚动时触发
    scroLl(event) {
      // console.log(event.detail.scrollTop)
      let scrollTop = event.detail.scrollTop
      if (scrollTop >= this.tophei) {//上拉
        // 当前分类商品的高度小于滚动高度时跳转下一个分类
        if (scrollTop >= this.heightset[this.trigger]) {
          this.trigger += 1
        }
      } else {//下拉
        // 当前分类商品的高度大于滚动高度时跳转下一个分类
        if (scrollTop < this.heightset[this.trigger - 1]) {
          this.trigger -= 1
        }
      }
      this.tophei = scrollTop
    },

    // 单个商品+
    plus(index, good_index, cid, itemgood) {
      const {quantity, image, name, unitprice, unit, id} = itemgood
      const QU = quantity + 1
      this.$set(this.goods[index].dishList[good_index], 'quantity', QU)
      const arr = {image, name, unitprice, quantity: QU, unit, total_price: unitprice * QU, id, cid, good_index}
      this.shopping_Cart(arr)
    },

    // 单个商品-
    handleReduce(index, good_index, cid, itemgood) {
      if (itemgood.quantity > 0) {
        this.reduce(index, good_index, cid, itemgood);
      }
    },

    // 单个商品-
    reduce(index, good_index, cid, itemgood) {
      const {quantity, image, name, unitprice, unit, id} = itemgood
      const QU = quantity - 1
      this.$set(this.goods[index].dishList[good_index], 'quantity', QU)
      const arr = {image, name, unitprice, quantity: QU, unit, total_price: unitprice * QU, id, cid, good_index}
      this.shopping_Cart(arr)
    },

    // 添加进购物车的商品
    shopping_Cart(arr) {
      // 一：购物车没有数据，空数组：
      // 直接添加进数据
      // 二：购物车里有数据
      // 1.没有相同的菜品存在
      // 2.有相同的菜品存在
      if (this.shopping_card.length == 0) {
        // 一：购物车没有数据，空数组：
        this.shopping_card.push(arr)
      } else {
        // 二：购物车里有数据
        let itemindex = this.shopping_card.findIndex(item => item.id == arr.id)
        if (itemindex == -1) {
          // 没有相同的菜品存在
          this.shopping_card.unshift(arr)
        } else {
          this.$set(this.shopping_card[itemindex], 'quantity', arr.quantity)
          this.$set(this.shopping_card[itemindex], 'total_price', arr.total_price)
        }
      }
      console.log(this.shopping_card)
      this.qunint_of_goods()
    },

    // 计算左边各分类下添加了多少菜品
    qunint_of_goods() {
      let array = this.shopping_card
      let res = {}
      array.forEach(item => {
        if (res[item.cid]) {
          res[item.cid] += item.quantity
        } else {
          res[item.cid] = item.quantity
        }
      })
      let M = []
      for (let k in res) {
        M.push({cid: k, value: res[k]})
      }
      M.forEach(item => {
        let res_index = this.itemize.findIndex(iteming => iteming.cid == item.cid)
        this.$set(this.itemize[res_index], 'sele_quantity', item.value)
      })
    },

    //购物车商品加减数量
    shopping_Cart_add_sub(index, QU, id, cid, good_index, unitprice) {
      this.$set(this.shopping_card[index], 'quantity', QU)
      this.$set(this.shopping_card[index], 'total_price', QU * unitprice)
      // 根据id唯一标识查询商品的数量做到商品加减同步
      const itemcid = this.goods.findIndex(item => item.cid == cid)
      this.$set(this.goods[itemcid].dishList[good_index], 'quantity', QU)
      this.qunint_of_goods()
    },

    // 清空已点：被子组件调用
    empty_data() {
      this.shopping_card = []
      this.itemize.forEach(item => {
        item.sele_quantity = 0
      })
      this.goods.forEach(item => {
        item.dishList.forEach(T => {
          T.quantity = 0
        })
      })
      this.pop_Shopping(false)
    },

    // 弹出或隐藏单个商品弹出框
    popup_item(value = true, index, good_index, cid, itemgood) {
      this.popupitem = value
      this.pro_details = {index, good_index, cid, itemgood}
      console.log(this.pro_details)
    },
    // 显示购物车组件
    pop_Shopping(value = true) {
      this.card = value
    },

    // 获取推荐菜品数据
    async getRecommendDishes() {
      try {
        const res = await requestUtil({url: "/order/recommend?limit=3", method: "get"})
        console.log("推荐菜品：", res)
        if (res.code === 0 && res.recommendList) {
          this.recommendDishes = res.recommendList
        }
      } catch (error) {
        console.error("获取推荐菜品失败：", error)
        this.recommendDishes = []
      }
    },

    // 点击推荐菜品详情
    showRecommendDetail(item) {
      // 查找对应的菜品详情
      for (let i = 0; i < this.goods.length; i++) {
        const category = this.goods[i]
        for (let j = 0; j < category.dishList.length; j++) {
          const dish = category.dishList[j]
          if (dish.name === item.name) {
            this.popup_item(true, i, j, category.cid, dish)
            return
          }
        }
      }
      // 如果没找到，显示提示
      wx.showToast({
        title: '菜品详情加载中',
        icon: 'none'
      })
    },

    // 判断是否为热销菜品
    isHotDish(dishName) {
      return this.recommendDishes.some(item => item.name === dishName)
    },

    // 请求数据
    async dishEs() {
      let table_num = wx.getStorageSync('table_num')
      const res = await requestUtil({url: "/category/listAll", method: "get"})
      const res2 = await requestUtil({url: "/dish/listAll/"+table_num, method: "get"})
      const res3 = await requestUtil({url: "/order/getCart/" + table_num, method: "get"})

      console.log("购物车：", res3);
      console.log(res)

      this.itemize = res.categoryListAll;
      this.goods = res2.allDish;

      // 确保所有菜品的 quantity 属性初始化为 0
      this.goods.forEach(category => {
        category.dishList.forEach(dish => {
          this.$set(dish, 'quantity', 0); // 强制设置为0，覆盖旧值
        });
      });

      // 根据购物车数据更新菜品数量
      if (res3.carts && res3.carts.length > 0) {
        this.shopping_card = res3.carts.map(({table_num, ...rest}) => rest); // 更新购物车数据
        res3.carts.forEach(cartItem => {
          const categoryIndex = this.goods.findIndex(category => category.cid === cartItem.cid);
          if (categoryIndex !== -1) {
            const dishIndex = this.goods[categoryIndex].dishList.findIndex(dish => dish.id === cartItem.id);
            if (dishIndex !== -1) {
              this.$set(this.goods[categoryIndex].dishList[dishIndex], 'quantity', cartItem.quantity);
            }
          }
        });
      }
      
      this.qunint_of_goods() // 重新计算左侧分类数量
      
      // 获取推荐菜品
      await this.getRecommendDishes()
      
      this.$nextTick(() => {
        this.goods_height()
      })
    },
    
    // 计算右边每个分类菜品的高度
    goods_height() {
      this.heightset = []
      let cate_height = 0
      const query = wx.createSelectorQuery()
      query.selectAll('.rig-height').boundingClientRect()
      query.exec((res) => {
        res[0].forEach((item) => {
          cate_height += item.height
          this.heightset.push(cate_height)
        })
        this.exist = false
      })
    },

    // 弹出订阅消息弹窗
    placean_order() {
      // 消息弹窗
      wx.requestSubscribeMessage({
        tmplIds: [this.tmplIds],
        success: (res) => {
          this.sub_database()
        },
        fail: (err) => {
          this.sub_database()
        }
      })
    },

    // 提交订单
    async sub_database() {
      wx.showLoading({title: '正在加入购物车', mask: true})
      let table_number = wx.getStorageSync('table_num')
      try {
        // 只调用购物车接口，不再创建订单
        const res = await requestUtil({
          url: "/order/addCart/" + table_number, 
          data: this.shopping_card, 
          method: "post"
        })
        if (res.code == 0) {
          wx.redirectTo({
            url: '/pages/order-details/details'
          })
          wx.hideLoading()
        } else {
          wx.hideLoading()
          wx.showToast({
            title: res.msg || '加入购物车失败',
            icon: 'none',
            duration: 2000
          })
        }
      } catch (e) {
        wx.hideLoading()
        wx.showToast({
          title: '服务器错误，请重试',
          icon: 'none',
          duration: 2000
        })
      }
    },

    // 处理加入购物车
    handleAddToCart() {
      if (this.total_quantity > 0) {
        this.placean_order();
      }
    },

    // 推送订单提醒
    push_message() {
      var pubsub = this.goeasy.pubsub;
      pubsub.publish({
        channel: "my_channel",//替换为您自己的channel
        message: "小程序端来的",//替换为您想要发送的消息内容
        onSuccess: () => {
          console.log("消息发布成功。");
        },
        onFailed: (error) => {
          console.log("消息发送失败，错误编码：" + error.code + " 错误信息：" + error.content);
        }
      });
    },

    // 我的订单
    my_order() {
      wx.navigateTo({
        url: '/pages/my-order/my-order'
      })
    },

    // 获取公告列表
    async getAnnouncements() {
      try {
        const res = await requestUtil({url: "/announcement/list", method: "get"})
        console.log("公告API响应:", res);
        
        // 检查API返回格式并过滤只显示启用的公告(status=1)
        if (res && Array.isArray(res)) {
          // 只保留status为1的公告
          this.marqueeMessages = res
            .filter(item => item.status === 1 && item.content && item.content.trim())
            .map(item => item.content);
        } else if (res && typeof res === 'object' && res.data && Array.isArray(res.data)) {
          // 处理可能的包装格式 {data: [...]}
          this.marqueeMessages = res.data
            .filter(item => item.status === 1 && item.content && item.content.trim())
            .map(item => item.content);
        } else if (res && typeof res === 'object' && res.list && Array.isArray(res.list)) {
          // 处理可能的其他包装格式 {list: [...]}
          this.marqueeMessages = res.list
            .filter(item => item.status === 1 && item.content && item.content.trim())
            .map(item => item.content);
        } else {
          // 如果API返回异常，使用默认公告
          this.marqueeMessages = [
            "欢迎光临，祝您用餐愉快！",
            "今日特价菜品：鲜美红烧肉，不容错过！",
            "请扫描桌面二维码点餐，方便快捷！",
            "关注我们，获取最新优惠和活动信息！",
            "本店所有菜品新鲜制作，请您放心品尝！"
          ]
        }
        
        // 确保至少有一个公告消息
        if (this.marqueeMessages.length === 0) {
          this.marqueeMessages = ["暂无公告"];
        }
        
        console.log("最终公告消息:", this.marqueeMessages);
      } catch (error) {
        console.error("获取公告失败：", error)
        // 使用默认公告
        this.marqueeMessages = [
          "欢迎光临，祝您用餐愉快！",
          "今日特价菜品：鲜美红烧肉，不容错过！",
          "请扫描桌面二维码点餐，方便快捷！",
          "关注我们，获取最新优惠和活动信息！",
          "本店所有菜品新鲜制作，请您放心品尝！"
        ]
      }
    },

    // 跑马灯切换消息
    startMarquee() {
      // 计算每个消息的滚动时间，根据长度动态调整
      const message = this.marqueeMessages[this.currentMarqueeIndex];
      const messageLength = message.length;
      // 每个字符大约需要0.5秒滚动时间，最小10秒，最大20秒
      const scrollTime = Math.max(10000, Math.min(20000, messageLength * 500));
      
      // 滚动完成后切换到下一条消息
      this.marqueeInterval = setTimeout(() => {
        this.currentMarqueeIndex = (this.currentMarqueeIndex + 1) % this.marqueeMessages.length;
        // 递归调用，实现循环
        this.startMarquee();
      }, scrollTime);
    },
    stopMarquee() {
      if (this.marqueeInterval) {
        clearTimeout(this.marqueeInterval);
        this.marqueeInterval = null;
      }
    }
  },
  onLoad() {
    // 获取用餐人数
    this.number_people = wx.getStorageSync('number_of_diners')
    this.baseUrl = getBaseUrl()
    this.dishEs()
    this.getAnnouncements() // 获取公告列表
    this.startMarquee() // 开始跑马灯
  },
  onUnload() {
    this.stopMarquee(); // 页面卸载时停止跑马灯
  },
  computed: {
    // 计算购物车的菜品总数
    total_quantity() {
      let quantity = 0
      this.shopping_card.forEach(item => {
        quantity += item.quantity
      })
      return quantity
    },
    currentMarqueeMessage() {
      return this.marqueeMessages[this.currentMarqueeIndex];
    }
  }
}
</script>

<style scoped>
.top-view {
  background: linear-gradient(to bottom, #f7d45f, #f7d562, #f8d561, #f9db76, #f9de80);
  height: 120rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20rpx;
  position: fixed;
  top: 0;
  right: 0;
  left: 0;
  z-index: 10; /* 确保在跑马灯上方 */
}

.top-view image {
  display: block;
  width: 35rpx;
  height: 35rpx;
}

.top-view-flex {
  display: flex;
  align-items: center;
}

.top-search {
  padding-right: 50rpx;
}

/* 点餐界面 */
.order-view {
  margin-top: 140rpx; /* top-view (120rpx) + 20rpx buffer */
}

.commodity {
  display: flex;
  position: fixed;
  top: 140rpx; /* top-view (120rpx) + 20rpx buffer */
  left: 0;
  right: 0;
}

.order-left {
  background-color: #fafafa;
  width: 150rpx;
  overflow-y: auto;
}

.itemize-text {
  font-size: 27rpx;
  padding: 30rpx 10rpx;
  display: flex;
  align-items: center;
  color: #797979;
}

.itemize-text text:nth-child(1) {
  flex: 1;
}

.itemize-text text:nth-child(2) {
  background-color: #eb5941;
  border-radius: 50%;
  font-size: 20rpx;
  color: #FFFFFF;
  width: 30rpx;
  height: 30rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-left: 2rpx;
}

.scroll-Hei {
  height: calc(100vh - 190rpx - 120rpx); /* 100vh - 固定顶部高度 - 固定底部高度 */
}

.order-right {
  background-color: #FFFFFF;
  flex: 1;
  overflow-y: auto;
  height: 100vh;
}

.classif {
  font-size: 27rpx;
  padding: 30rpx 20rpx;
  color: #797979;
}

/* 分类商品 */
.classif-goods {
  display: flex;
  justify-content: space-between;
  padding: 0 20rpx;
  height: 150rpx;
  font-size: 30rpx;
  margin-bottom: 45rpx;
}

.goods-image {
  position: relative;
}

.goods-image image {
  display: block;
  width: 150rpx;
  height: 150rpx;
  border-radius: 10rpx;
}

/* 热销标签 */
.hot-tag {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  background-color: #ff6b6b;
  color: white;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  top: -5rpx;
  right: -5rpx;
  background: linear-gradient(45deg, #ff6b6b, #ff8e8e);
  color: white;
  font-size: 18rpx;
  padding: 4rpx 8rpx;
  border-radius: 8rpx;
  font-weight: bold;
  box-shadow: 0 2rpx 8rpx rgba(255, 107, 107, 0.3);
}

.goods-Price {
  flex: 1;
  position: relative;
  padding: 0 20rpx;
}

.goods-Price text {
  display: block;
}

.goods-name {
  display: flex;
  flex-direction: column;
  position: relative;
  top: 0;
}

.goods-name text:nth-child(1) {
  padding-bottom: 9rpx;
}

.unit-price {
  position: absolute;
  bottom: 0;
  display: flex;
  align-items: baseline;
}

.Bold {
  font-weight: bold;
}

.Symbol {
  font-size: 20rpx;
}

.Thinning {
  font-size: 25rpx;
  color: #cccccc;
}

.quantity view {
  width: 50rpx;
  height: 50rpx;
  text-align: center;
  line-height: 50rpx;
}

.quantity-btn {
  background-color: #f7d45f;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.quantity-icon {
  font-size: 40rpx;
  font-weight: bold;
  color: #000000; /* 黑色 */
}

.disabled-text {
  color: #cccccc !important; /* 禁用时变灰 */
}

.quantity {
  display: flex;
  align-items: center;
  align-self: flex-end;
  width: 200rpx;
  justify-content: space-between;
}

.order-bottom {
  background-color: #fefefe;
  height: 120rpx;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  box-shadow: 0rpx -1.9rpx 1rpx 1rpx #f9f9f9;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20rpx;
  z-index: 9;
}

.Shopping image {
  width: 75rpx;
  height: 75rpx;
  display: block;
}

.Shopping-left {
  width: 75rpx;
  height: 75rpx;
}

.Shopping {
  display: flex;
  align-items: center;
}

.Shopping-number {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  background-color: #eb5941;
  color: #FFFFFF;
  font-size: 20rpx;
  width: 35rpx;
  height: 35rpx;
  border-radius: 50%;
  text-align: center;
  line-height: 35rpx;
}

.Shopping-title {
  flex: 1;
  padding: 0 25rpx;
  color: #999999;
}

.place-order button {
  border: none;
  background: linear-gradient(to right, #f8da81, #f8d771, #f7d362, #f6cb4a);
  width: 200rpx;
  height: 75rpx;
  line-height: 75rpx;
  border-radius: 50rpx;
  font-weight: bold;
  z-index: 9;
}

/* 点击分类列表加上背景色 */
.active {
  background-color: #FFFFFF;
  color: #000000 !important;
}

/* 轮播图样式 */
.uni-margin-wrap {
  width: 690rpx;
  width: 100%;
}

.swiper {
  height: 300rpx;
  border-radius: 15rpx;
  overflow: hidden;
  margin: 20rpx;
  box-shadow: 0 8rpx 25rpx rgba(0, 0, 0, 0.15);
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
  overflow: hidden;
}

.recommend-image {
  width: 100%;
  height: 100%;
  display: block;
}

.recommend-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  padding: 40rpx 30rpx 30rpx;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.recommend-content {
  flex: 1;
}

.recommend-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 8rpx;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.5);
}

.recommend-subtitle {
  font-size: 24rpx;
  opacity: 0.9;
  margin-bottom: 12rpx;
}

.recommend-price {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 24rpx;
  margin-right: 4rpx;
}

.price-number {
  font-size: 36rpx;
  font-weight: bold;
  color: #ffeb3b;
}

.price-unit {
  font-size: 22rpx;
  margin-left: 4rpx;
  opacity: 0.8;
}

.recommend-badge {
  background: linear-gradient(45deg, #ff6b6b, #ff8e8e);
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: bold;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 107, 0.4);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.disabled-btn {
  pointer-events: none; /* 禁用点击事件 */
}

.marquee-area {
  background-color: #000000;
  display: flex;
  align-items: center;
  padding: 10rpx 20rpx;
  overflow: hidden;
  white-space: nowrap;
  margin: 0 20rpx;
  border-radius: 15rpx;
  /* box-shadow: 0 2rpx 5rpx rgba(0, 0, 0, 0.05); */
  /* z-index: 9; */
}

.marquee-text-container {
  animation: marquee 15s linear infinite;
}

.marquee-text {
  font-size: 28rpx;
  color: #ffffff; /* 白色字体 */
}

@keyframes marquee {
  0% {
    transform: translateX(100%);
  }
  100% {
    transform: translateX(-100%);
  }
}
</style>