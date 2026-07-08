<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <h2>管理员登录</h2>
          <p>关爱老人，服务社会</p>
        </div>
      </template>
      
      <el-form :model="loginForm" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'admin123',
  userType: 'admin'
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
    await formRef.value.validate()
    loading.value = true
    
    const response = await authApi.login(loginForm)
    
    // 响应拦截器已经处理了code检查，这里直接使用response
    const userData = response
    
    // 设置用户信息
    userStore.setUser({
      id: userData.id,
      name: userData.name,
      role: 'ADMIN',
      token: userData.token,
      username: userData.username
    })
    
    ElMessage.success('登录成功')
    
    // 跳转到仪表盘
    router.push('/community')
    
  } catch (error: any) {
    console.error('登录错误:', error)
    ElMessage.error(error.response?.data?.message || error.message || '登录失败')
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
  background: url('@/1.png') center/cover no-repeat;
  position: relative;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 1;
}

.login-card {
  width: 450px;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 2;
  backdrop-filter: blur(15px);
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 10px;
}

.login-card :deep(.el-card__header) {
  text-align: center;
  background: rgba(248, 249, 250, 0.7);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.login-header h2 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-weight: 600;
  font-size: 24px;
}

.login-header p {
  margin: 0;
  color: #7f8c8d;
  font-size: 14px;
  font-style: italic;
}

.login-card :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.login-card :deep(.el-button) {
  border-radius: 10px;
  font-weight: 600;
  letter-spacing: 0.5px;
  background: rgba(64, 158, 255, 0.9);
  border: 1px solid rgba(64, 158, 255, 0.3);
}
</style>
