<template>
	<div class="ordering" v-loading.fullscreen.lock="loading">
		<div class="heading">订单管理</div>
		<div class="content-view">
			<!-- 查询 -->
			<div class="query-view">
				<!-- 订单号查询 -->
				<div class="quotation-query">
					<div>订单号</div>
					<div>
						<el-input v-model="statevalue" placeholder="请输入订单号"></el-input>
					</div>
				</div>
				<!-- 按钮 -->
				<div class="quotation-query"><el-button type="success" size="medium" @click="queryFun()">查询</el-button></div>
			</div>
			<div class="button-view">

			  <el-button type="warning" size="small" @click="refresh_order()">刷新订单</el-button>
			  <el-button type="primary" size="small" @click="openRatingManagement()">评价管理</el-button>

			</div>
			<!-- 是否有数据 -->
			<div v-if="nodatas">
				<!-- 表头 -->
				<div class="tab-list">
					<span v-for="(item,index) in tablist" :key="index">{{item}}</span>
				</div>
				<!-- 表格 -->
				<div class="tab-table-quo" v-for="(item,index) in tabcont" :key="index">
					<div>{{item.order_no}}</div>
					<div>{{item.table_number}}</div>
					<div class="remarks-text">
						<el-button type="small" :loading="index == deta_load ? true : false" @click="detailed_menu(index,item)">详细菜单</el-button>
					</div>
					<div>{{Price(item.sett_amount)}}</div>
					<div>
						<el-button type="info" size="small" v-if="item.order_receiving == 'mis_orders'" :loading="index == rece_load ? true : false" @click="receiving(index,item.id)">待接单</el-button>
						<el-button size="small" type="success" v-if="item.order_receiving == 'rec_order'">已接单</el-button>
					</div>
					<div>
						<el-button size="small" type="success" disabled v-if="item.transac_status == 'success'">用户已支付</el-button>
						<el-button size="small" type="warning" disabled v-if="item.transac_status == 'unsettled'">用户未付款</el-button>
					</div>
					<div>
						<el-button type="primary" size="small" v-if="!item.is_delivered" @click="updateDeliveryStatus(index, item.id, true)">派送</el-button>
						<el-button type="success" size="small" disabled v-else>已派送</el-button>
					</div>
				</div>
				<!-- 分页 -->
				<el-pagination
				  background
				  layout="prev, pager, next"
				  :hide-on-single-page="true"
				  :total="total"
				  @current-change="currentchange"
				  >
				</el-pagination>
			</div>
			<!-- 没有数据 -->
			<div class="nodatas" v-if="!nodatas">{{nodvalue}}</div>
			<!-- 订单详细弹窗 -->
			<el-dialog
			  title="订单详情"
			  :visible.sync="dialogVisible"
			  width="600px"
			  :center="true"
			  >
			  <div class="order-detail-info">
				  <div class="info-item">
					  <span class="label">订单编号：</span>
					  <span class="value">{{currentOrder.order_no}}</span>
				  </div>
				  <div class="info-item">
					  <span class="label">用餐人数：</span>
					  <span class="value">{{currentOrder.number_of_diners || '-'}}人</span>
				  </div>
				  <div class="info-item">
					  <span class="label">下单时间：</span>
					  <span class="value">{{currentOrder.order_time}}</span>
				  </div>
				  <div class="info-item">
					  <span class="label">支付时间：</span>
					  <span class="value">{{currentOrder.pay_time || '-'}}</span>
				  </div>
				  <div class="info-item">
					  <span class="label">支付交易号：</span>
					  <div style="flex: 1; text-align: left !important;">{{currentOrder.transaction_id || '-'}}</div>
				  </div>
				  <div class="info-item">
					  <span class="label">桌号：</span>
					  <span class="value">{{currentOrder.table_number}}</span>
				  </div>
				  <div class="info-item">
					  <span class="label">派送状态：</span>
					  <span class="value">{{currentOrder.is_delivered ? '已派送' : '未派送'}}</span>
				  </div>
				  <div class="info-item">
					  <span class="label">派送时间：</span>
					  <span class="value">{{currentOrder.delivery_time || '-'}}</span>
				  </div>
				  <div class="info-item">
					  <span class="label">用餐评分：</span>
					  <span class="value">{{currentOrder.rating ? currentOrder.rating + '分' : '-'}}</span>
				  </div>
				  <div class="info-item">
						<span class="label">评价时间：</span>
						<span class="value">{{currentOrder.comment_time || '-'}}</span>
					</div>
					<div class="info-item">
						<span class="label">评价内容：</span>
						<span class="value">{{currentOrder.comment || '-'}}</span>
					</div>
			  </div>
			  <div class="menu-title">菜品清单</div>
			  <div class="menu-list">
				  <div class="menu-item" v-for="(item,index) in user_menu" :key="index">
					  <div class="menu-detail">
						  <span class="dish-name">{{item.name}}</span>
						  <span class="dish-quantity">{{item.quantity}}{{item.unit}}</span>
					  </div>
				  </div>
			  </div>
			</el-dialog>
			
			<!-- 评价管理弹窗 -->
			<el-dialog
			  title="评价管理"
			  :visible.sync="ratingDialogVisible"
			  width="800px"
			  :center="true"
			  >
			  <div class="rating-management">
				  <div class="rating-stats">
					  <h3>评价统计</h3>
					  <div class="stats-grid">
						  <div class="stat-item">
							  <div class="stat-label">总评价数</div>
							  <div class="stat-value">{{ratingStats.totalRatings}}</div>
						  </div>
						  <div class="stat-item">
							  <div class="stat-label">平均评分</div>
							  <div class="stat-value">{{ratingStats.averageRating.toFixed(1)}}分</div>
						  </div>
						  <div class="stat-item">
							  <div class="stat-label">最高分</div>
							  <div class="stat-value">{{ratingStats.maxRating}}分</div>
						  </div>
						  <div class="stat-item">
							  <div class="stat-label">最低分</div>
							  <div class="stat-value">{{ratingStats.minRating}}分</div>
						  </div>
					  </div>
				  </div>
				  <div class="rating-distribution">
					  <h3>评分分布</h3>
					  <div class="distribution-bar">
						  <div v-for="(count, score) in ratingStats.distribution" :key="score" class="bar-item">
							  <div class="bar-label">{{score}}分</div>
							  <div class="bar-container">
								  <div class="bar-fill" :style="{ width: (count / ratingStats.totalRatings * 100) + '%' }"></div>
							  </div>
							  <div class="bar-count">{{count}}</div>
						  </div>
					  </div>
				  </div>
				  <div class="rating-list">
					  <h3>评价详情</h3>
					  <div class="rating-search">
						  <el-select v-model="selectedRating" placeholder="按评分等级搜索">
							  <el-option label="全部" value=""></el-option>
							  <el-option v-for="score in 10" :key="score" :label="score + '分'" :value="score"></el-option>
						  </el-select>
						  <el-button type="primary" size="small" @click="searchRating()">搜索</el-button>
					  </div>
					  <el-table :data="filteredRatingList" style="width: 100%">
						  <el-table-column prop="order_no" label="订单号" width="180"></el-table-column>
						  <el-table-column prop="comment_time" label="评价时间"></el-table-column>
						  <el-table-column prop="rating" label="评价等级"></el-table-column>
					  </el-table>
				  </div>
			  </div>
			</el-dialog>
		</div>
		<div style="height: 120px;"></div>
	</div>
</template>

<script>
// 交易状态
import {staff} from '../../../config/state-type.js'
// 价格补领
const Price = require('e-commerce_price') // 重新导入 Price 函数
export default{
	data() {
			return {
				Price:Price, // 恢复 Price 数据属性
				options:staff(),
				loading: true,
				nodatas:true,
				dialogVisible:false,//弹出详细菜单
				ratingDialogVisible:false,//评价管理弹窗
				deta_load:-1,//查看详细菜单
				rece_load:-1,//接单
				check_load:-1,//结账
				deliver_load:-1,//派送
				time:'',//交易时间
				statevalue:'',//交易状态
				nodvalue:'没有订单数据',
				total:0,//总条数
				pagenum:0,
				pickerOptions: {
				    disabledDate(time) {return time.getTime() > Date.now();}
				},
				tablist:['订单号','桌号','菜单详情','交易金额(元)','接单状态','交易状态','机器人派送'],
				tabcont:[],
				user_menu:[],//用户详细菜单
				currentOrder: {}, // 当前查看的订单信息
				dingdan:0,//订单提醒
				refreshTimer: null, // 添加定时器变量
				ratingStats: {
					totalRatings: 0,
					averageRating: 0,
					maxRating: 0,
					minRating: 0,
					distribution: {} // 评分分布，key为分数，value为数量
				},
				ratingList: [], // 评价详情列表
				selectedRating: '', // 选中的评分等级
				filteredRatingList: [] // 过滤后的评价列表
			}
	},
	methods:{
		// 分页
		currentchange(e){
			this.pagenum = e - 1
			this.obtainorder(0)
		},
		// 获取订单
		async obtainorder(vle){
			try{
				let res = await new this.Request(this.Urls.m().obtainorder,{"pageNum":this.pagenum+1,"query":this.statevalue}).modepost()
				console.log('后端返回的订单数据:', res.data)
				this.nodatas = res.data.total == 0  ? false : true
				this.tabcont = res.data.orderList
				this.total = res.data.total
				this.loading = false
				if(vle == 1){
					localStorage.setItem('order_num',0)
					this.$store.commit('order_remind',0)
				}
			}catch(e){
				new this.mytitle(this.$message,'error','服务器发生错误,请重试').funtitle()
			}
		},
		// 查看详细菜单
		async detailed_menu(index, order){
			this.deta_load = index
			console.log('order对象', order); // 调试用，查看用餐人数字段
			try{
				let res = await new this.Request(this.Urls.m().vieworder + '?id=' + order.id).modeget()
				console.log(res)
				this.user_menu = res.data.list
				this.currentOrder = order // 保存当前订单信息
				this.deta_load = -1
				this.dialogVisible = true
			}catch(e){
				this.deta_load = -1
				new this.mytitle(this.$message,'error','服务器发生错误,请重试').funtitle()
			}
		},
		// 接单
		async receiving(index, id, retryCount = 0) {
			const MAX_RETRIES = 3; // 最大重试次数
			const RETRY_DELAY = 500; // 重试延迟，毫秒

			console.log(`[Receiving] Attempt: ${retryCount}, Order ID: ${id}`);
			this.rece_load = index; // 设置加载状态
			let res = null; // 确保res在整个try-catch-finally块中可见
			let finalOutcome = 'pending'; // 'success', 'fail', 'retry'
			let finalErrorMessage = '';

			try {
				res = await new this.Request(this.Urls.m().receiving, { id: id, order_receiving: 'rec_order' }).modepost();
				console.log(`[Receiving] Response for attempt ${retryCount}:`, res);

				if (res.code === 0) {
					finalOutcome = 'success';
					this.$set(this.tabcont[index], 'order_receiving', 'rec_order');
				} else {
					console.log(`[Receiving] Failure for attempt ${retryCount}. Message: ${res.msg}`);
					if (res.msg === "用户未支付，不能接单" && retryCount < MAX_RETRIES) {
						finalOutcome = 'retry';
						console.log(`[Receiving] Retrying for attempt ${retryCount}...`);
						setTimeout(() => {
							this.receiving(index, id, retryCount + 1);
						}, RETRY_DELAY);
						return; // 立即返回，等待重试结果，不执行后续的finally块的清理
					} else {
						finalOutcome = 'fail';
						finalErrorMessage = res.msg === "用户未支付，不能接单" ?
											 '订单状态尚未更新，请稍后刷新重试。' :
											 res.msg || '接单失败';
						console.log(`[Receiving] Final failure for attempt ${retryCount}. Error: ${finalErrorMessage}`);
					}
				}
			} catch (e) {
				console.error(`[Receiving] Catch block error for attempt ${retryCount}:`, e);
				finalOutcome = 'fail';
				finalErrorMessage = '服务器发生错误,请重试';
			} finally {
				console.log(`[Receiving] Finally block for attempt ${retryCount}. Final outcome: ${finalOutcome}`);
				if (finalOutcome === 'success') {
					// 延迟obtainorder和清除加载状态，确保toast可见
					setTimeout(() => {
						this.obtainorder(0);
						this.rece_load = -1;
					}, 100); // 延迟100毫秒
				} else if (finalOutcome === 'fail') {
					// 延迟obtainorder和清除加载状态，确保toast可见
					setTimeout(() => {
						this.obtainorder(0);
						this.rece_load = -1;
					}, 100); // 延迟100毫秒
				} else {
					// finalOutcome === 'retry'，不需要在这里做任何事情，因为函数已经返回，等待下一次重试。
					// rece_load 会在最终成功或失败时被清除。
				}
			}
		},
		// 开始自动刷新
		startAutoRefresh() {
			// 每10秒自动刷新一次
			this.refreshTimer = setInterval(() => {
				this.obtainorder(0)
			}, 10000)
		},
		// 停止自动刷新
		stopAutoRefresh() {
			if (this.refreshTimer) {
				clearInterval(this.refreshTimer)
				this.refreshTimer = null
			}
		},
		// 刷新订单
		refresh_order(){
			this.loading = true
			this.deta_load = -1,//查看详细菜单
			this.rece_load = -1,//接单
			this.check_load = -1,//结账
			this.obtainorder(1)
		},
		// 查询订单
		queryFun(){
			this.pagenum = 0
			this.obtainorder(0)
		},
		// 更新派送状态
		async updateDeliveryStatus(index, orderId, isDelivered) {
			this.deliver_load = index
			try {
				let res = await new this.Request(this.Urls.m().updateDeliveryStatus, { 
					orderId: orderId, 
					isDelivered: isDelivered 
				}).modepost()
				
				console.log('派送状态更新响应:', res)
				
				if (res.code === 0) {
					// 先更新本地状态，提供即时反馈
					this.$set(this.tabcont[index], 'is_delivered', isDelivered)
					if (isDelivered) {
						this.$set(this.tabcont[index], 'delivery_time', new Date().toLocaleString())
					}
					// 延迟刷新订单列表
					setTimeout(() => {
						this.obtainorder(0)
					}, 500)
				} else {
					// 刷新订单列表
					this.obtainorder(0)
				}
			} catch (e) {
				console.error('派送状态更新错误:', e)
				// 刷新订单列表
				this.obtainorder(0)
			} finally {
				// 延迟清除加载状态，确保用户看到反馈
				setTimeout(() => {
					this.deliver_load = -1
				}, 500)
			}
		},
		// 打开评价管理
		async openRatingManagement() {
			this.ratingDialogVisible = true
			this.loading = true
			try {
				// 打印API URL
				console.log('评价管理API URL:', this.Urls.m().getRatingStats)
				
				// 调用真实的后端API获取评价数据
				let res = await new this.Request(this.Urls.m().getRatingStats).modeget()
				
				console.log('评价数据响应:', res)
				
				// axios返回的响应中，实际数据在res.data中
				const responseData = res.data
				console.log('实际评价数据:', responseData)
				
				if (responseData && responseData.code === 0) {
					// 确保distribution对象的键是字符串类型
					const formattedDistribution = {}
					if (responseData.distribution) {
						for (let key in responseData.distribution) {
							formattedDistribution[key.toString()] = responseData.distribution[key]
						}
					}
					
					// 构造正确的ratingStats对象
					this.ratingStats = {
						totalRatings: responseData.totalRatings || 0,
						averageRating: responseData.averageRating || 0,
						maxRating: responseData.maxRating || 0,
						minRating: responseData.minRating || 0,
						distribution: formattedDistribution,
						ratingList: responseData.ratingList || []
					}
					
					this.ratingList = responseData.ratingList || []
					// 初始化过滤后的列表
					this.filteredRatingList = [...this.ratingList]
					console.log('处理后的数据:', this.ratingStats, this.ratingList, this.filteredRatingList)
				} else {
					console.error('获取评价数据失败:', responseData)
					new this.mytitle(this.$message,'error',responseData && responseData.msg || '获取评价数据失败').funtitle()
				}
			} catch (e) {
				console.error('获取评价数据错误:', e)
				new this.mytitle(this.$message,'error','获取评价数据失败').funtitle()
			} finally {
				this.loading = false
			}
		},
		// 搜索评分
		searchRating() {
			if (this.selectedRating) {
				this.filteredRatingList = this.ratingList.filter(item => item.rating == this.selectedRating)
			} else {
				this.filteredRatingList = [...this.ratingList]
			}
		}
	},
	created() {
		// 获取订单
		this.obtainorder(0)
		// 订单提醒
		this.dingdan = localStorage.getItem("order_num")
		// 启动自动刷新
		this.startAutoRefresh()
	},
	beforeDestroy() {
		// 组件销毁前停止自动刷新
		this.stopAutoRefresh()
	},
	watch: {
		// 监听订单提醒
		"$store.state.remind"(newValue, oldValue) {
			this.dingdan = newValue.num
		}
	},
}
</script>

<style scoped="scoped">
@import url("../../../style/pubiss.css");
@import url("../../../style/popup.css");
::v-deep .el-pagination.is-background .el-pager li:not(.disabled).active {
	background-color: #00be06;
	color: #fff;
}
::v-deep .el-badge{
	margin-right: 5px;
}
.menu-padd{
	border-bottom: 1px solid #f8f8f8;
}
.Menu-details{
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 50px;
}
.menu-margin{margin: 20px 0;}
.menu-span{
	font-weight: bold;
	font-size: 15px;
	padding-top: 20px;
}
::v-deep .el-dialog{
	height: 600px;
	overflow-y: auto;
	border-radius: 5px !important;
}
.order-detail-info {
    padding: 15px;
    background: #f5f7fa;
    border-radius: 4px;
    margin-bottom: 20px;
}

.info-item {
    margin-bottom: 10px;
    display: flex;
    align-items: center;
}

.info-item .label {
    color: #606266;
    width: 80px;
    text-align: right;
    margin-right: 10px;
}

.info-item .value {
    color: #303133;
    flex: 1;
}

.menu-title {
    font-size: 16px;
    font-weight: bold;
    color: #303133;
    margin-bottom: 15px;
    padding-left: 10px;
    border-left: 3px solid #409EFF;
}

.menu-list {
    max-height: 300px;
    overflow-y: auto;
}

.menu-item {
    padding: 10px;
    border-bottom: 1px solid #EBEEF5;
}

.menu-detail {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.dish-name {
    color: #303133;
    font-size: 14px;
}

.dish-quantity {
    color: #909399;
    font-size: 14px;
}

/* 评价管理样式 */
.rating-management {
    padding: 20px 0;
}

.rating-stats {
    margin-bottom: 30px;
}

.rating-stats h3,
.rating-distribution h3,
.rating-list h3 {
    font-size: 16px;
    font-weight: bold;
    color: #303133;
    margin-bottom: 15px;
    padding-left: 10px;
    border-left: 3px solid #409EFF;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 20px;
}

.stat-item {
    background: #f5f7fa;
    padding: 20px;
    border-radius: 4px;
    text-align: center;
}

.stat-label {
    color: #606266;
    font-size: 14px;
    margin-bottom: 10px;
}

.stat-value {
    color: #303133;
    font-size: 24px;
    font-weight: bold;
}

.rating-distribution {
    margin-bottom: 30px;
}

.distribution-bar {
    background: #f5f7fa;
    padding: 20px;
    border-radius: 4px;
}

.bar-item {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
}

.bar-label {
    width: 40px;
    font-size: 14px;
    color: #606266;
}

.bar-container {
    flex: 1;
    height: 20px;
    background: #e4e7ed;
    border-radius: 10px;
    margin: 0 10px;
    overflow: hidden;
}

.bar-fill {
    height: 100%;
    background: #409EFF;
    transition: width 0.3s;
}

.bar-count {
    width: 40px;
    text-align: right;
    font-size: 14px;
    color: #606266;
}

.rating-list {
    margin-top: 20px;
}

.el-table {
    margin-top: 10px;
}
</style>
