<template>
  <div class="login-container">
    <div class="header">
      <h1>欢迎登录助老系统</h1>
      <p class="subtitle">账号登录</p>
    </div>

    <div class="login-form">
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        label-width="0"
        size="large"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名 (如: elder1)"
            prefix-icon="User"
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            :disabled="loading"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%; font-size: 18px; padding: 15px;"
            :loading="loading"
            @click="handleLogin"
          >
            <el-icon><User /></el-icon>
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-tips">
        <p>💡 登录提示：</p>
        <p>初始密码：123456</p>
      </div>
    </div>

    <!-- 语音提示 -->
    <div class="voice-prompt" v-if="voiceMessage">
      <el-icon><Bell /></el-icon>
      <span>{{ voiceMessage }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Bell, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const voiceMessage = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  userType: 'elder'
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^elder\d+$/, message: '用户名格式错误，应为：elder+数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true
    voiceMessage.value = '正在登录...'

    const response = await authApi.login({
      username: loginForm.username,
      password: loginForm.password,
      userType: loginForm.userType
    })

    // 响应拦截器已经处理了code检查，这里直接使用response
    const userData = response

    // 设置用户信息
    userStore.setUser({
      id: userData.id,
      name: userData.name,
      role: 'ELDER',
      token: userData.token,
      elderId: userData.id,
      username: userData.username
    })

    // 设置老人信息
    userStore.setElderInfo({
      id: userData.id,
      name: userData.name,
      age: userData.age,
      gender: userData.gender,
      balance: userData.balance,
      todayFaceChecked: false
    })

    voiceMessage.value = `登录成功！欢迎${userData.name}`
    ElMessage.success('登录成功')

    // 语音播报
    speak(`登录成功！欢迎${userData.name}`)

    setTimeout(() => {
      router.push('/home')
    }, 1500)

  } catch (error: any) {
    console.error('登录错误:', error)
    voiceMessage.value = '登录失败，请重试'
    ElMessage.error(error.response?.data?.message || error.message || '登录失败')
    speak('登录失败，请重试')
  } finally {
    loading.value = false
    setTimeout(() => {
      voiceMessage.value = ''
    }, 3000)
  }
}

// 语音播报功能
const speak = (text: string) => {
  if ('speechSynthesis' in window) {
    const utterance = new SpeechSynthesisUtterance(text)
    utterance.lang = 'zh-CN'
    utterance.rate = 0.8
    utterance.pitch = 1
    utterance.volume = 1
    speechSynthesis.speak(utterance)
  }
}

onMounted(() => {
  userStore.initUser()
  if (userStore.token) {
    router.push('/home')
  }
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.login-container {
  width: 100vw;
  height: 100vh;
  background: url('@/2.png') center/cover no-repeat;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-family: 'Microsoft YaHei', sans-serif;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
}

.header {
  text-align: center;
  margin-bottom: 50px;
  position: relative;
  z-index: 2;
}

.header h1 {
  color: #2c3e50;
  margin: 0 0 10px 0;
  font-size: 48px;
  font-weight: 700;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.subtitle {
  color: #34495e;
  margin: 0;
  font-size: 24px;
  font-weight: 500;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
}

.login-form {
  width: 400px;
  max-width: 90vw;
  position: relative;
  z-index: 2;
}

.login-tips {
  margin-top: 20px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(5px);
  border-radius: 10px;
  font-size: 14px;
  color: #2c3e50;
  text-align: left;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.login-tips p {
  margin: 5px 0;
  line-height: 1.4;
}

.voice-prompt {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
  color: white;
  padding: 15px 30px;
  border-radius: 15px;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 10px;
  z-index: 1000;
  animation: fadeInOut 3s ease-in-out;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

@keyframes fadeInOut {
  0% { opacity: 0; transform: translateX(-50%) translateY(-20px); }
  20% { opacity: 1; transform: translateX(-50%) translateY(0); }
  80% { opacity: 1; transform: translateX(-50%) translateY(0); }
  100% { opacity: 0; transform: translateX(-50%) translateY(-20px); }
}

/* 老人端特殊样式优化 */
@media (max-width: 768px) {
  .header h1 {
    font-size: 36px;
  }

  .subtitle {
    font-size: 20px;
  }

  .camera-placeholder {
    width: 200px;
    height: 200px;
  }

  .actions .el-button {
    font-size: 18px;
    padding: 12px 20px;
  }
}
</style>
