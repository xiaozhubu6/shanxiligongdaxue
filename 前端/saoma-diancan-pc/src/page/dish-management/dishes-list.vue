<template>
	<div class="ordering" v-loading.fullscreen.lock="loading">
		<div class="heading">菜品管理</div>
		<div class="content-view">
			<div class="button-view">
				<router-link :to="{name:'upload'}">
				<el-button type="success" size="medium">添加菜品</el-button>
				</router-link>
			</div>
			<div v-if="nodatas">
				<!-- 表头 -->
				<div class="tab-list">
					<span v-for="(item,index) in tablist" :key="index">{{item}}</span>
				</div>
				<!-- 表格 -->
				<div class="tab-table" v-for="(goods_item,index) in tabcont" :key="index">
				<div>{{goods_item.time}}</div>
				<div>{{goods_item.type.label}}</div>
				<div>{{goods_item.name}}</div>
				<div>
					<el-image class="elimg"
					:src="getServerUrl()+'/image/dish/'+goods_item.image"
					fit="cover"
					:lazy="true"
					:preview-src-list="[getServerUrl()+'/image/dish/'+goods_item.image]">
					</el-image>
				</div>
				<div>{{goods_item.unitprice}}</div>
				<div>{{goods_item.monthlysale || 0}}</div>
			  <div>
				  <el-button size="small" @click="edIt(goods_item)" :disabled="goods_item.onsale ? false : true ">编辑</el-button>
				  <el-button v-if="goods_item.onsale" type="danger" size="small" @click="shelf(goods_item.id,false,index)" :loading="index == shelfload ? true : false" :disabled="index == shelfload ? true : false">不显示</el-button>
				  <el-button v-else type="warning" size="small"  @click="shelf(goods_item.id,true,index)">显示</el-button>
          <el-button type="warning" size="small"  @click="deleteDish(goods_item.id)">删除</el-button>
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
			<div class="nodatas" v-if="!nodatas">还没有菜品数据</div>
		</div>
		<div style="height: 120px;"></div>
	</div>
</template>

<script>
import {getServerUrl} from '../../../config/sys.js'
export default{
	data() {
		return {
			loading: true,
			nodatas:true,
			shelfload:-1,//下架loading
			tablist: ['创建时间','类目','菜品名称','菜品图片','价格(元)','月销量','操作'],
			tabcont:[],
			currentnum:1,//当前页数
			total:0,//总条数
			pagenum:0,
		}
	},
	methods:{
    getServerUrl(){
      return getServerUrl()
    },
		// 分页
		currentchange(e){
			// console.log(e)//0:第一页，1：第二页，2：第三页
			this.pagenum = e - 1
			this.obtaindishes()
		},
    // 删除
    deleteDish(id){
      this.$confirm('您确定要删除这个记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(async({ value }) => {
        let res = await new this.Request(this.Urls.m().dishDelete+"?id="+id).modeget()
        if(res.status != 200){
          new this.mytitle(this.$message,'warning',"删除失败").funtitle()
        }else{
          new this.mytitle(this.$message,'success',"删除成功！").funtitle()
          this.obtaindishes()
        }
      }).catch((err) => {
        console.log(err)
      });
    },
		// 获取菜品
		async obtaindishes(){
			try{
				let res = await new this.Request(this.Urls.m().obtaindishes,{"pageNum":this.pagenum+1}).modepost()
				this.nodatas = res.data.total == 0  ? false : true
				this.tabcont = res.data.dishList
				this.total = res.data.total
				this.loading = false
			}catch(e){
				this.loading = false
				new this.mytitle(this.$message,'error','服务器发生错误,请重试').funtitle()
			}
		},
		// 编辑修改
		edIt(item){
			this.$router.push({name:'upload',params:{item}});
		},
		// 下架 / 上架
		async shelf(id,onsale,index){
			this.shelfload = index
			try{
				await new this.Request(this.Urls.m().fromsale,{id:id,onsale:onsale}).modepost()
				this.$set(this.tabcont[index],'onsale',onsale)
				this.shelfload = -1
			}catch(e){
				this.shelfload = -1
				new this.mytitle(this.$message,'error','操作失败，重试').funtitle()
			}
		}
	},
	created() {
		this.obtaindishes()
	},
}
</script>

<style scoped="scoped">
@import url("../../../style/pubiss.css");
::v-deep .el-pagination.is-background .el-pager li:not(.disabled).active {
	background-color: #00be06;
	color: #fff;
}
/* 分页 */
.el-pagination {
	text-align: center;
	margin: 30px 0;
}
</style>
