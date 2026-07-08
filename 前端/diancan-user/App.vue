<script>
	import GoEasy from 'goeasy';

	export default {
		globalData:{
			Modelmes:null
		},
		onLaunch() {
			wx.cloud.init({
			  env: 'wxd6aa2b8ebc8382ab',
			  traceUser: true,
			})
			//建立连接
			const goEasy = new GoEasy({
				// TODO: 将 id 设置为能够标识用户的唯一值，例如用户ID或openid
				// 临时设置为一个非空字符串进行测试
				id:"temp_mini_program_id", // pubsub选填，im必填
				data:{}, //必须是一个对象，pubsub选填，im必填，用于上下线提醒和查询在线用户列表时，扩展更多的属性
				onSuccess: function () {  //连接成功
				  console.log("GoEasy 连接成功") //连接成功
				},
				onFailed: function (error) { //连接失败
				  console.log("GoEasy 连接失败");
				  console.error("GoEasy 连接失败:", error); // 添加详细错误日志
				},
				onProgress:function(attempts) { //连接或自动重连中
				  console.log("GoEasy 连接或自动重连中");
				},
				host: 'ht.goeasy.io', // 华北地区
				appkey: 'BC-a463e1ec0fdd4f3e9c5dfa5b01545330', // <-- 已经使用您提供的 appkey 进行替换
				forceTLS: true,
				modules: ['pubsub']
			});
			// 获取设备型号
			wx.getSystemInfo({
				success:(res)=>{
					if(res.safeArea.top > 40){//iphone
						this.globalData.Modelmes = true
					}else{
						this.globalData.Modelmes = false
					}
				}
			})
		},
	}
</script>

<style>
	/*每个页面公共css */
</style>
