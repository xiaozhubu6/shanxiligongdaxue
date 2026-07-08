<template>
	<div id="backcont">
		<!-- logo -->
		<div class="meituan-content">
			<div class="login-cont">
				<div class="meituan-title">智慧餐厅</div>
				<div class="meituan-user">
					<p>账号</p>
					<el-input class="inptflex" v-model="userName" placeholder="请输入账号"></el-input>
				</div>
				<div class="meituan-user">
					<p>密码</p>
					<el-input class="inptflex" v-model="password" placeholder="请输入密码" show-password></el-input>
				</div>
				<!-- 登录 -->
				<el-button v-if="regi == '注册'" type="success" class="meituan-btn" @click="signin()" :loading="load" :disabled="load">登录</el-button>

			</div>
		</div>
	</div>
</template>

<script>
export default{
	data() {
		return {
			regi:'注册',
			load:false,
			userName: '',
			password:''
		}
	},
	methods:{
		// 登录
		async signin(){
		  if(this.userName==''){
        new this.mytitle(this.$message,'warning','请输入用户名！').funtitle()
        return;
      }
      if(this.password==''){
        new this.mytitle(this.$message,'warning','请输入密码！').funtitle()
        return;
      }
			this.load = true
			let obj = {userName:this.userName,password:this.password}
			try{
				let res = await new this.Request(this.Urls.m().login,obj).modepost()
        console.log(res)
				if(res.data.code != 0){
					new this.mytitle(this.$message,'warning','用户名或者密码错误！').funtitle()
				}else{
					let ids = '1'
          console.log("token:"+res.data.token)
					localStorage.setItem("nuvmenuid", JSON.stringify(ids))
					localStorage.setItem("token", res.data.token)
          localStorage.setItem("currentUser", JSON.stringify(res.data.resultAdmin))
          this.$router.push({name:'index'})
				}
				this.load = false
			}catch(e){
				console.log(e)
				this.load = false
				new this.mytitle(this.$message,'info','发生错误,重试').funtitle()
			}
		}
	}
}
</script>

<style scoped="scoped">
#backcont {
	background-image: url(../../../static/login/beijing.jpg);
	background-attachment: fixed;
	background-repeat: no-repeat;
	background-size: cover;
	-webkit-background-size: cover;
	moz-background-size: cover;
	min-height: 100vh;
}
.meituan-content {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
}
.login-cont {
	width: 500px;
	height: 300px;
	background: #f9de80;
	border-radius: 5px;
}
.meituan-title {
	text-align: center;
	color: #000000;
	font-size: 30px;
	padding-top: 30px;
	font-family: Arial, Helvetica, sans-serif;
}
.meituan-user {
	width: 400px;
	margin: 0 auto;
	padding-top: 30px;
	height: 40px;
	display: flex;
	align-items: center;
}

.meituan-user p{width: 50px; color: #000000;
font-size:16px;
}

.inptflex{flex: 1;}

.reg-view{
	width: 400px;
	display: flex;
	justify-content: flex-end;
	margin: 0 auto;
	padding-top: 10px;
	cursor:pointer;
}

.meituan-btn {
	width: 200px;
	display: flex;
	align-items: center;
	justify-content: center;
	margin: 20px auto 0 auto;
	font-size: 16px;
}
</style>
