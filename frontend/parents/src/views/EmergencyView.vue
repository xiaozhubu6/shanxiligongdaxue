<template>
  <div class="emergency-container">
    <div class="header">
      <h2>紧急呼叫</h2>
      <p>遇到紧急情况？请立即求助</p>
    </div>
    
    <div class="emergency-content">
      <!-- 紧急按钮 -->
      <div class="emergency-button" @click="handleEmergencyCall">
        <div class="pulse-ring"></div>
        <div class="emergency-icon">
          <el-icon size="80"><Warning /></el-icon>
        </div>
        <div class="emergency-text">
          <h3>紧急呼叫</h3>
          <p>点击求助</p>
        </div>
      </div>
      
      <!-- 状态显示 -->
      <div class="status-section" v-if="emergencyStatus">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>呼叫状态</span>
              <el-tag :type="getStatusType(emergencyStatus.status)">
                {{ getStatusText(emergencyStatus.status) }}
              </el-tag>
            </div>
          </template>
          
          <div class="status-content">
            <div class="status-item">
              <div class="status-icon">
                <el-icon size="32" :color="getStatusColor(emergencyStatus.status)">
                  <Warning v-if="emergencyStatus.status === 'UNHANDLED'" />
                  <CircleCheck v-else />
                </el-icon>
              </div>
              <div class="status-text">
                <h4>{{ emergencyStatus.eventType }}</h4>
                <p>{{ emergencyStatus.description || '紧急呼叫求助' }}</p>
                <p class="time">呼叫时间：{{ emergencyStatus.createdAt }}</p>
                <p v-if="emergencyStatus.handledAt" class="handled-time">
                  处理时间：{{ emergencyStatus.handledAt }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 联系信息 -->
      <div class="contact-section">
        <el-card>
          <template #header>
            <span>联系信息</span>
          </template>
          
          <div class="contact-content">
            <div class="contact-item">
              <div class="contact-icon">
                <el-icon><Phone /></el-icon>
              </div>
              <div class="contact-info">
                <h4>社区管理员</h4>
                <p>电话：400-123-4567</p>
                <p>24小时在线</p>
              </div>
            </div>
            
            <div class="contact-item">
              <div class="contact-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="contact-info">
                <h4>家属联系人</h4>
                <p>张小明：138-0013-8001</p>
                <p>张小红：138-0013-8002</p>
              </div>
            </div>
            
            <div class="contact-item">
              <div class="contact-icon">
                <el-icon><LocationInformation /></el-icon>
              </div>
              <div class="contact-info">
                <h4>地址</h4>
                <p>北京市朝阳区前进路123号</p>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 取消按钮 -->
    <div class="cancel-section" v-if="emergencyStatus && emergencyStatus.status === 'UNHANDLED'">
      <el-button 
        type="danger" 
        size="large" 
        @click="cancelEmergency"
        style="font-size: 20px; padding: 15px 30px;"
      >
        取消呼叫
      </el-button>
    </div>
    
    <!-- 语音提示 -->
    <div class="voice-prompt" v-if="voiceMessage">
      <el-icon><Bell /></el-icon>
      <span>{{ voiceMessage }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning, Phone, User, LocationInformation, Bell } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { emergencyApi } from '@/api'

const userStore = useUserStore()

const emergencyStatus = ref(null)
const voiceMessage = ref('')

// 处理紧急呼叫
const handleEmergencyCall = async () => {
  try {
    const emergencyData = {
      elderId: userStore.elderInfo?.id || 1, // 使用elderInfo中的id
      eventType: 'EMERGENCY_CALL',
      description: '老人通过系统发起紧急呼叫'
    }
    
    const response = await emergencyApi.createEvent(emergencyData)
    emergencyStatus.value = response
    
    voiceMessage.value = '紧急呼叫已发送，请耐心等待工作人员处理'
    ElMessage.success('紧急呼叫已发送')
    
    // 语音播报
    speak('紧急呼叫已发送，请耐心等待工作人员处理')
    
  } catch (error) {
    ElMessage.error('呼叫失败，请重试')
    voiceMessage.value = '呼叫失败，请重试'
    speak('呼叫失败，请重试')
  }
}

// 取消紧急呼叫
const cancelEmergency = async () => {
  try {
    await ElMessageBox.confirm('确定要取消紧急呼叫吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    voiceMessage.value = '已取消紧急呼叫'
    ElMessage.info('已取消紧急呼叫')
    
  } catch (error) {
    if (error !== 'cancel') {
      voiceMessage.value = '操作失败'
    }
  }
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'UNHANDLED': return 'danger'
    case 'HANDLED': return 'success'
    default: return 'info'
  }
}

// 获取状态颜色
const getStatusColor = (status) => {
  switch (status) {
    case 'UNHANDLED': return '#f56c6c'
    case 'HANDLED': return '#67c23a'
    default: return '#909399'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'UNHANDLED': return '待处理'
    case 'HANDLED': return '已处理'
    default: return '未知'
  }
}

// 语音播报功能
const speak = (text) => {
  if ('speechSynthesis' in window) {
    const utterance = new SpeechSynthesisUtterance(text)
    utterance.lang = 'zh-CN'
    utterance.rate = 0.8
    utterance.pitch = 1
    utterance.volume = 1
    speechSynthesis.speak(utterance)
  }
}

// 加载紧急状态
const loadEmergencyStatus = async () => {
  // 模拟获取最新状态
  try {
    const events = await emergencyApi.getEventsByElder(userStore.elderId)
    if (events && events.length > 0) {
      emergencyStatus.value = events[0] // 获取最新的紧急事件
    }
  } catch (error) {
    console.error('加载紧急状态失败:', error)
  }
}

onMounted(() => {
  loadEmergencyStatus()
})
</script>

<style scoped>
.emergency-container {
  min-height: 100vh;
  background: #fff5f5;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h2 {
  color: #e74c3c;
  margin: 0;
  font-size: 32px;
  font-weight: 600;
}

.header p {
  color: #666;
  margin: 10px 0 0;
  font-size: 18px;
}

.emergency-content {
  max-width: 800px;
  margin: 0 auto;
}

.emergency-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-bottom: 40px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.emergency-button:hover {
  transform: scale(1.05);
}

.pulse-ring {
  width: 120px;
  height: 120px;
  border: 8px solid #e74c3c;
  border-radius: 50%;
  position: relative;
  margin-bottom: 20px;
}

.pulse-ring::before {
  content: '';
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border: 8px solid #e74c3c;
  border-radius: 50%;
  animation: pulse-ring 1.5s infinite;
}

@keyframes pulse-ring {
  0% {
    transform: scale(0.95);
    opacity: 1;
  }
  50% {
    transform: scale(1);
    opacity: 0.5;
  }
 100% {
    transform: scale(1.05);
    opacity: 1;
  }
}

.emergency-icon {
  color: #e74c3c;
  margin-bottom: 10px;
}

.emergency-text {
  color: #333;
  text-align: center;
}

.emergency-text h3 {
  margin: 0;
  font-size: 24px;
}

.emergency-text p {
  margin: 5px 0 0;
  font-size: 16px;
}

.status-section {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 18px;
}

.status-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 10px;
}

.status-icon {
  flex-shrink: 0;
}

.status-text h4 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.status-text p {
  margin: 5px 0 0;
  color: #666;
  font-size: 16px;
}

.time {
  color: #999;
  font-size: 14px;
  margin-top: 5px;
}

.handled-time {
  color: #67c23a;
  font-size: 14px;
  margin-top: 5px;
}

.contact-section {
  margin-bottom: 30px;
}

.contact-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 10px;
}

.contact-icon {
  color: #409eff;
  font-size: 24px;
}

.contact-info h4 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.contact-info p {
  margin: 5px 0 0;
  color: #666;
  font-size: 16px;
}

.cancel-section {
  text-align: center;
}

.voice-prompt {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 20px 30px;
  border-radius: 10px;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 10px;
  z-index: 1000;
  animation: fadeInOut 3s ease-in-out;
}

@keyframes fadeInOut {
  0% { opacity: 0; transform: translate(-50%, -50%); }
  20% { opacity: 1; transform: translate(-50%, -50%); }
  80% { opacity: 1; transform: translate(-50%, -50%); }
  100% { opacity: 0; transform: translate(-50%, -50%); }
}

/* 老人端大字体优化 */
.el-button {
  font-size: 20px !important;
  padding: 15px 30px !important;
}

.el-tag {
  font-size: 18px !important;
  padding: 8px 15px !important;
}

.el-card {
  font-size: 18px !important;
}

.el-card__header {
  font-size: 18px !important;
  padding: 20px !important;
}
</style>
