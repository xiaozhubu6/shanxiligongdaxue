<template>
  <div class="login-container">
    <div class="login-form">
      <div class="form-header">
        <h2>欢迎登录</h2>
        <p>子女端・守护老人更安心</p>
      </div>
      
      <el-form :model="loginForm" :rules="rules" ref="formRef" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" show-password />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleLogin">
            <span v-if="!loading">立即登录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  userType: 'child',
  rememberMe: false
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) {
      ElMessage.error('请检查输入信息')
      return
    }
    
    loading.value = true
    
    const response = await axios.post('http://localhost:8082/api/auth/login', {
      username: loginForm.username,
      password: loginForm.password,
      userType: loginForm.userType
    })
    
    if (response.data.code === 200) {
      const userData = response.data.data
      
      if (userData.token) {
        localStorage.setItem('token', userData.token)
      }
      
      localStorage.setItem('user', JSON.stringify(userData))
      
      ElMessage.success(`欢迎回来，${userData.name || '用户'}！`)
      
      router.push('/dashboard')
    } else {
      ElMessage.error(response.data.message || '登录失败')
    }
  } catch (error) {
    console.error('登录错误:', error)
    ElMessage.error('登录失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: url('@/assets/6.png') center/cover no-repeat;
  font-family: 'Source Han Sans CN', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.login-form {
  background: rgba(255, 255, 255, 0.95);
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(123, 97, 255, 0.15);
  width: 400px;
  backdrop-filter: blur(10px);
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 12px 0;
}

.form-header p {
  font-size: 14px;
  color: #000000;
  margin: 0;
  line-height: 1.5;
}

.login-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #7B61FF 0%, #9A85FF 100%);
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: linear-gradient(135deg, #8A71FF 0%, #A595FF 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(123, 97, 255, 0.35);
}
</style>
