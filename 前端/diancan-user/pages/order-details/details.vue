<template>
<view class="details-view">
	<view class="order-top">
		<view class="order-remind" v-if="!payment_success">
			<view>{{order_status_message}}</view>
			<view>{{order_status_submessage}}</view>
		</view>
		<view class="order-remind" v-else>
			<view>下单成功，坐等开吃</view>
			<view>菜品已在制作中</view>
		</view>
	</view>

	<!-- 购物车内容 (未支付) -->
	<view class="food-list" v-if="!payment_success">
		<view class="foot-back">
			<view class="foot-til">
				<text>我的购物车</text>
			</view>
			<block v-for="(item,index) in goods_data" :key="index">
				<view class="foot-deta">
					<view>
						<image :src="baseUrl+'/image/dish/'+item.image" mode="aspectFill"></image>
					</view>
					<view class="foot-name">
						<text>{{item.name}}</text>
						<text>{{item.quantity}}{{item.unit}}</text>
					</view>
					<view class="foot-total">¥{{item.total_price}}</view>
				</view>
			</block>
			<!-- 总计 -->
			<view class="total-view">
				<view>共 {{overall}} 份</view>
				<view class="total-price">
					<text>总计</text>
					<text>¥{{Price(Number(other_data.sett_amount))}}</text>
				</view>
			</view>
		</view>
		<!-- 桌台名称 -->
		<view class="foot-back order-number">
			<text>桌台名称：{{other_data.table_number}}</text>
			<text v-if="other_data.number_of_diners">用餐人数：{{other_data.number_of_diners}}</text>
		</view>
		<view style="height: 300rpx;"></view>
	</view>

	<!-- 订单成功内容 (已支付) -->
	<view class="food-list" v-else>
		<view class="foot-back">
			<view class="foot-til">
				<text>我的订单</text>
			</view>
			<block v-for="(item,index) in order_info.goods_list" :key="index">
				<view class="foot-deta">
					<view>
						<image :src="baseUrl+'/image/dish/'+item.image" mode="aspectFill"></image>
					</view>
					<view class="foot-name">
						<text>{{item.name}}</text>
						<text>{{item.quantity}}{{item.unit}}</text>
					</view>
					<view class="foot-total">¥{{item.total_price}}</view>
				</view>
			</block>
			<!-- 总计 -->
			<view class="total-view">
				<view>共 {{order_info.goods_list ? order_info.goods_list.length : 0}} 份</view>
				<view class="total-price">
					<text>总计</text>
					<text>¥{{Price(Number(order_info.sett_amount))}}</text>
				</view>
			</view>
		</view>
		<!-- 订单号和下单时间，桌台名称 -->
		<view class="foot-back order-number">
			<text>订单编号：{{order_info.order_no}}</text>
			<text>下单时间：{{order_info.order_time ? new Date(order_info.order_time).toLocaleString() : ''}}</text>
			<text>支付时间：{{order_info.pay_time ? new Date(order_info.pay_time).toLocaleString() : ''}}</text>
			<text>桌台名称：{{order_info.table_number}}</text>
			<text>用餐人数：{{order_info.number_of_diners}}</text>
			<text>派送状态：{{order_info.is_delivered ? '已派送' : '未派送'}}</text>
			<text v-if="order_info.delivery_time">派送时间：{{new Date(order_info.delivery_time).toLocaleString()}}</text>
			<text v-if="order_info.rating">用餐评分：{{order_info.rating}}分</text>
			<text v-if="order_info.comment_time">评价时间：{{new Date(order_info.comment_time).toLocaleString()}}</text>
			<text v-if="order_info.transaction_id">交易号：{{order_info.transaction_id}}</text>
		</view>
		
		<!-- 评价区域 -->
		<view class="foot-back" v-if="!order_info.rating && order_info.is_delivered">
			<view class="foot-til">
				<text>用餐评价</text>
			</view>
			<view class="rating-area">
				<text>请为本次用餐评分：</text>
				<view class="rating-stars">
					<text 
						v-for="(star, index) in 10" 
						:key="star"
						class="star"
						:class="{ 'active': (index + 1) <= rating }"
						@click="handleRatingClick(index + 1)"
					>
						★
					</text>
				</view>
				<view class="comment-area">
					<text>评价内容：</text>
					<textarea 
						v-model="comment"
						placeholder="请输入您的评价..."
						class="comment-input"
					></textarea>
				</view>
				<view class="rating-btn">
					<view @click="submitRating">提交评价</view>
				</view>
			</view>
		</view>
		<view class="foot-back" v-else-if="!order_info.rating && !order_info.is_delivered">
			<view class="foot-til">
				<text>用餐评价</text>
			</view>
			<view class="rating-area">
				<text>管理员尚未派送，派送后可评价</text>
			</view>
		</view>
		
		<view style="height: 300rpx;"></view>
	</view>

	<!-- 底部按钮 -->
	<view class="add-a-dish" :style="{'padding-bottom': Modelmes ? '68rpx' : ''}" v-if="!payment_success">
		<view class="button-container">
			<view class="cart-button" @click="continueAdding">
				继续加菜
			</view>
			<view class="cart-button primary" @click="handlePayment">
				去支付
			</view>
		</view>
	</view>
	<view class="add-a-dish" :style="{'padding-bottom': Modelmes ? '68rpx' : ''}" v-else>
		<view 
			:class="{'payment-success-btn': payment_success && button_text === '支付成功'}"
			@click="handleButtonClick"
		>
			{{ button_text }}
		</view>
	</view>
</view>
</template>

<script>
	import {getBaseUrl, requestUtil} from "../../utils/requestUtil.js"
const app = getApp()
const {Modelmes} = app.globalData
// 骨架屏
// import Order from '../skeleton-view/order.vue'
const db = wx.cloud.database()
const _ = db.command
const good_collect= db.collection('order-data')
// 价格补零
const Price = require('e-commerce_price')
export default{
	components:{},
	data() {
			return {
				baseUrl:'',
				exist:true,
				Modelmes,
				Price,
				overall:0,//总的多少分
				other_data:{},
				comp_data:[],//完整数据
				goods_data:[],//购物车数据
				table_number: null, // 新增：用于存储桌号
				payment_success: false, // 新增：支付成功状态
				order_info: {}, // 新增：支付成功后订单信息
				button_text: '去支付', // 按钮文本
				is_button_disabled: false, // 按钮是否禁用
				is_paying: false, // 是否正在支付中
				rating: 0, // 评价分数，默认0分
				comment: '', // 评价内容
				rating_submitted: false, // 评价是否已提交
				order_status_message: '请提交菜品', // 订单状态主消息
				order_status_submessage: '你可以选择继续增加菜品或结账', // 订单状态子消息
				is_delivered: false, // 派送状态
				refreshTimer: null, // 自动刷新定时器
				lastDeliveryStatus: false // 上次派送状态，用于检测状态变化
			}
		},
	methods:{
		async get_menu(){
			try{
				// 取出本地缓存的桌号
				let table_number = wx.getStorageSync('table_num')
				this.table_number = table_number // 将桌号存储到data中
				const res=await requestUtil({url:"/order/getCart/"+table_number,method:"get"})
				
				let res_data = res.carts
				
				this.overall=res_data.length
				// 总计和桌台号
			let totalAmount = 0;
			if (res.total_price !== undefined && res.total_price !== null) {
				totalAmount = parseFloat(res.total_price.toString()) || 0;
			}
			this.other_data = {"sett_amount": totalAmount ,"table_number":table_number}
			
				this.goods_data=res_data
				
				// 检查是否有未支付订单
				await this.checkOrderStatus();
				
				this.exist = false
				
			}catch(e){
				//TODO handle the exception
				console.error("获取购物车信息失败", e);
				wx.showToast({
					title: '获取购物车信息失败',
					icon: 'none',
					duration: 2000
				});
			}
		},
		// 检查订单状态
		async checkOrderStatus() {
			try {
				// 获取最新的未支付订单
				const orderRes = await requestUtil({
					url: "/order/get",
					method: "get",
					data: {
						table_number: this.table_number,
						transac_status: "unpaid"
					}
				});
				
				if (orderRes && orderRes.menu) {
					this.is_delivered = orderRes.menu.is_delivered || false;
					// 根据派送状态更新消息
					if (this.is_delivered) {
						this.order_status_message = '你的菜品正在处理';
						this.order_status_submessage = '请耐心等待';
					} else {
						this.order_status_message = '购物车';
						this.order_status_submessage = '你可以选择继续加菜或结账';
					}
				}
			} catch (e) {
				console.error("获取订单状态失败", e);
				// 默认为初始状态
				this.order_status_message = '购物车';
				this.order_status_submessage = '你可以选择继续加菜或结账';
			}
		},
		async handlePayment() {
			if (this.goods_data.length === 0) {
				wx.showToast({
					title: '购物车为空，无法支付',
					icon: 'none',
					duration: 2000
				});
				return;
			}

			// 设置支付中状态
			this.is_paying = true;
			this.button_text = '支付中...';
			this.is_button_disabled = true;

			wx.showLoading({
				title: '正在生成订单...',
				mask: true
			});

			try {
				// 获取用餐人数，确保不为空
				let numberOfDiners = wx.getStorageSync('number_of_diners');
				if (!numberOfDiners) {
					wx.showModal({
						title: '提示',
						content: '请先设置用餐人数',
						showCancel: false,
						success: () => {
							wx.navigateTo({
								url: '/pages/set-diners/set-diners'
							});
						}
					});
					this.button_text = '去支付';
					this.is_button_disabled = false;
					wx.hideLoading();
					return;
				}

				// 生成交易号
				const transactionId = 'TXN' + Date.now() + Math.floor(Math.random() * 10000);

				// 调用后端API创建已支付订单
				const orderCreateRes = await requestUtil({
					url: `/order/createPaidOrderFromCart/${this.table_number}`,
					method: "POST",
					data: {
						numberOfDiners: String(numberOfDiners),
						table_number: this.table_number,
						goods_list: this.goods_data,
						sett_amount: this.other_data.sett_amount,
						transaction_id: transactionId
					}
				});
				
				wx.hideLoading();

				if (orderCreateRes.code === 0) {
					this.payment_success = true;
					
					// 确保order_info包含所有必要字段
					this.order_info = {
						id: orderCreateRes.id,
						order_no: orderCreateRes.order_no,
						table_number: orderCreateRes.table_number,
						sett_amount: orderCreateRes.sett_amount,
						transac_status: orderCreateRes.transac_status,
						order_receiving: orderCreateRes.order_receiving,
						order_time: orderCreateRes.order_time,
						pay_time: orderCreateRes.pay_time || new Date(),
						transaction_id: transactionId,
						is_delivered: orderCreateRes.is_delivered || false,
						delivery_time: orderCreateRes.delivery_time,
						goods_list: orderCreateRes.goods_list,
						number_of_diners: numberOfDiners // 确保用餐人数被正确设置
					};
					
					// 初始化上次派送状态
					this.lastDeliveryStatus = orderCreateRes.is_delivered || false;
					
					// 启动自动刷新
					this.startAutoRefresh();
					
					this.button_text = '再来一单';
					this.is_button_disabled = false;
					
					// 清空购物车
					wx.removeStorageSync('cart_items');
					
					wx.showToast({
						title: '下单成功，等待管理员派送后可评价',
						icon: 'success',
						duration: 2000
					});
				} else {
					this.button_text = '去支付';
					this.is_button_disabled = false;
					wx.showToast({
						title: orderCreateRes.msg || '生成订单失败',
						icon: 'none',
						duration: 2000
					});
				}
			} catch (e) {
				wx.hideLoading();
				this.button_text = '去支付';
				this.is_button_disabled = false;
				console.error("订单创建请求失败", e);
				wx.showToast({
					title: '订单创建请求失败',
					icon: 'none',
					duration: 2000
				});
			}
		},
		// 继续加菜
		continueAdding() {
			wx.reLaunch({
			  url: '/pages/home-page/page'
			});
		},
		// 展开全部
		opEn(index){
			this.$set(this.goods_data[index], 'goods_list',this.comp_data[index].goods_list)
			this.$set(this.goods_data[index],'max',0)
		},
		// 加菜 (这个方法将不再用于"去支付"后的页面，但为了不报错暂时保留)
		add_Dish(){
			wx.reLaunch({
			  url: '/pages/home-page/page'
			})
		},
		goToHomePage() {
			wx.reLaunch({
				url: '/pages/home-page/page'
			});
		},
		// 提交评价
		async submitRating() {
			if (this.rating_submitted) {
				wx.showToast({
					title: '评价已提交',
					icon: 'none',
					duration: 2000
				});
				return;
			}

			// 验证评分
			console.log('提交评价时的评分:', this.rating);
			if (this.rating < 1) {
				wx.showToast({
					title: '请选择评分',
					icon: 'none',
					duration: 2000
				});
				return;
			}

			wx.showLoading({
				title: '提交评价中...',
				mask: true
			});

			try {
				const res = await requestUtil({
				url: "/order/submitRating",
				method: "post",
				data: {
					orderId: this.order_info.id,
					rating: this.rating,
					comment: this.comment
				}
			});

				wx.hideLoading();

				if (res.code === 0) {
					this.rating_submitted = true;
					this.order_info.rating = this.rating;
					this.order_info.comment_time = new Date();
					wx.showToast({
						title: '评价提交成功',
						icon: 'success',
						duration: 2000
					});
				} else {
					wx.showToast({
						title: res.msg || '评价提交失败',
						icon: 'none',
						duration: 2000
					});
				}
			} catch (e) {
				wx.hideLoading();
				console.error("评价提交失败", e);
				wx.showToast({
					title: '评价提交失败',
					icon: 'none',
					duration: 2000
				});
			}
		},

		// 处理星级评分点击
		handleRatingClick(star) {
			console.log('点击星星:', star);
			this.rating = star;
			console.log('更新后的评分:', this.rating);
		},

		// 处理底部按钮点击
		handleButtonClick() {
			if (this.payment_success) {
				this.goToHomePage();
			} else {
				this.handlePayment();
			}
		},
		// 开始自动刷新
		startAutoRefresh() {
			// 每2秒自动刷新一次
			this.refreshTimer = setInterval(() => {
				this.refreshOrderStatus();
			}, 2000)
		},
		// 停止自动刷新
		stopAutoRefresh() {
			if (this.refreshTimer) {
				clearInterval(this.refreshTimer)
				this.refreshTimer = null
			}
		},
		// 刷新订单状态
		async refreshOrderStatus() {
			if (!this.payment_success || !this.order_info.id) {
				return;
			}
			
			try {
				// 获取最新的订单状态
				const orderRes = await requestUtil({
					url: "/order/getOrderById/" + this.order_info.id,
					method: "get"
				});
				
				if (orderRes && orderRes.code === 0) {
					const updatedOrder = orderRes.data;
					// 更新订单信息，只更新与派送状态相关的字段，不更新评分相关字段，避免覆盖用户正在选择的评分
					this.order_info = {
						...this.order_info,
						is_delivered: updatedOrder.is_delivered || false,
						delivery_time: updatedOrder.delivery_time
					};
					
					// 检测派送状态变化
					if (updatedOrder.is_delivered && !this.lastDeliveryStatus) {
						// 派送状态从 false 变为 true，显示提示
						wx.showToast({
							title: '您的菜品已派送，请准备评价',
							icon: 'success',
							duration: 2000
						});
						// 停止自动刷新
						this.stopAutoRefresh();
					}
					
					// 更新上次派送状态
					this.lastDeliveryStatus = updatedOrder.is_delivered || false;
				}
			} catch (e) {
				console.error("刷新订单状态失败", e);
			}
		}
	},
	onLoad() {
		this.get_menu()
		this.baseUrl=getBaseUrl()
	},
	onUnload() {
		// 页面卸载时停止自动刷新
		this.stopAutoRefresh()
	}
}
</script>

<style>
page{background-color: #f4f4f4;}
.details-view{position: relative;}
.order-top{
	background:linear-gradient(to bottom, #f7d45f,#f7d562,#f8d561,#f9db76, #f9de80);
	height: 300rpx;
}
.order-remind view:nth-child(1){
	font-size: 35rpx;
	font-weight: bold;
	padding-bottom: 20rpx;
}
.order-remind{
	height: 200rpx;
	padding: 50rpx 0 0 50rpx;
}
.food-list{
	/* position: absolute; */
	/* top: 200rpx; */
	left: 20rpx;
	right: 20rpx;
	margin-top: -100rpx; /* 调整top-view和food-list之间的间距 */
}
.foot-back{
	background-color: #fefefe;
	border-radius: 10rpx;
	padding: 0 20rpx;
	margin-bottom: 30rpx;
}
.foot-til{
	height: 100rpx;
	color: #999999;
	display: flex;
	align-items: center;
	justify-content: space-between;
}
.foot-deta image{
	display: block;
	width: 130rpx;
	height: 130rpx;
	border-radius: 10rpx;
}
.foot-deta{
	display: flex;
	justify-content: space-between;
	height: 130rpx;
	margin: 40rpx 0;
}
.foot-name{
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	padding: 0 20rpx;
	font-size: 30rpx;
}
.foot-name text:nth-child(1){font-size: 31rpx !important;font-weight: bold;}
.foot-name text:nth-child(2){color: #666666;}
.foot-total{font-weight: bold;}
/* 展开更多 */
.expand-more image{
	width: 25rpx;
	height: 25rpx;
	display: block;
	padding-left: 10rpx;
}
.expand-more{
	display: flex;
	align-items: center;
	justify-content: center;
	color: #999999;
	font-size: 25rpx;
	padding: 30rpx 0;
	border-bottom: 1rpx solid #f1f1f2;
}
/* 总计 */
.total-price{
	display: flex;
	align-items: center;
	color: #333333;
	padding-left: 40rpx;
}
.total-price text:nth-child(2){
	font-size: 35rpx;
	font-weight: bold;
	padding-left: 30rpx;
	}
.total-view{
	display: flex;
	justify-content: flex-end;
	align-items: center;
	padding: 30rpx 0;
}
.total-view view:nth-child(1){
	color: #999999;
}
/* 订单号 */
.order-number text{
	display: block;
	padding: 15rpx 0;
	font-size: 28rpx;
	color: #999999;
}
/* 加菜 */
.add-a-dish{
	background-color: #fefefe;
	height: 120rpx;
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex; /* 重新添加flex布局 */
	align-items: center; /* 垂直居中 */
	box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);
	z-index: 99;
}
.add-a-dish view{
	width: 100%; /* 修改为100%宽度 */
	height: 70rpx;
	background:linear-gradient(to right,#f8da81,#f8d771,#f7d362,#f6cb4a);
	line-height: 70rpx; /* 统一高度和行高 */
	text-align: center;
	border-radius: 50rpx;
	font-weight: bold;
	padding: 0 20rpx; /* 将左右内边距移到内部view */
}

.payment-success-btn {
  background: #cccccc; /* 灰色背景 */
  color: #666666; /* 灰色文字 */
  pointer-events: none; /* 禁用点击 */
}

/* 购物车页面按钮容器 */
.button-container {
  display: flex;
  justify-content: space-between;
  padding: 0 20rpx;
  width: 100%;
}

.cart-button {
  flex: 1;
  height: 70rpx;
  background: #f0f0f0;
  line-height: 70rpx;
  text-align: center;
  border-radius: 50rpx;
  font-weight: bold;
  margin: 0 10rpx;
  font-size: 28rpx;
}

.cart-button.primary {
  background: linear-gradient(to right,#f8da81,#f8d771,#f7d362,#f6cb4a);
  color: #333;
}

/* 评价区域样式 */
.rating-area {
  padding: 20rpx 0;
}

.rating-area text {
  display: block;
  margin-bottom: 20rpx;
  font-size: 30rpx;
}

.rating-stars {
  display: flex;
  margin-bottom: 30rpx;
}

.star {
  font-size: 40rpx;
  color: #cccccc;
  margin-right: 10rpx;
  cursor: pointer;
  transition: color 0.3s;
}

.star.active {
  color: #f7d45f;
}

.rating-btn {
  display: flex;
  justify-content: center;
}

.rating-btn view {
				width: 200rpx;
				height: 70rpx;
				background:linear-gradient(to right,#f8da81,#f8d771,#f7d362,#f6cb4a);
				line-height: 70rpx;
				text-align: center;
				border-radius: 50rpx;
				font-weight: bold;
			}

			/* 评价内容区域样式 */
			.comment-area {
				margin: 20rpx 0;
			}

			.comment-area text {
				display: block;
				margin-bottom: 10rpx;
				font-size: 30rpx;
			}

			.comment-input {
				width: 100%;
				height: 150rpx;
				border: 1rpx solid #e5e5e5;
				border-radius: 10rpx;
				padding: 15rpx;
				font-size: 28rpx;
				background-color: #f9f9f9;
				resize: none;
			}
</style>
