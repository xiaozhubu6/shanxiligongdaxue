<template>
  <div class="purchase-container">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
    </div>

    <!-- 页面头部 -->
    <div class="purchase-header">
      <div class="header-content">
        <div class="back-button" @click="goBack">
          <el-icon size="24"><ArrowLeft /></el-icon>
        </div>
        <div class="header-info">
          <h1>智能代购</h1>
          <p>语音说出您的需求，我们为您服务</p>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 语音输入区域 -->
      <div class="voice-section">
        <div class="voice-card">
          <div class="voice-visualizer" :class="{ 'recording': isRecording }">
            <div class="visualizer-waves">
              <div class="wave" v-for="i in 5" :key="i"></div>
            </div>
          </div>
          
          <div class="voice-button-container">
            <button 
              class="voice-button"
              :class="{ 'recording': isRecording, 'processing': isProcessing }"
              @click="toggleVoiceInput"
              :disabled="isProcessing"
            >
              <div class="button-inner">
                <el-icon size="48" class="mic-icon">
                  <Microphone v-if="!isRecording" />
                  <VideoCameraFilled v-else />
                </el-icon>
                <div class="button-text">
                  {{ isRecording ? '录音中...' : '点击说话' }}
                </div>
              </div>
            </button>
          </div>
          
          <!-- 状态提示 -->
          <div class="status-message" v-if="voiceMessage" :class="getMessageClass()">
            <div class="status-icon">
              <el-icon><Bell /></el-icon>
            </div>
            <span>{{ voiceMessage }}</span>
          </div>
          
          <!-- 手动输入区域 -->
          <div class="manual-input-section" v-if="showManualInput || forceShowManualInput">
            <div class="input-header">
              <el-icon><Edit /></el-icon>
              <span>手动输入</span>
            </div>
            <el-input
              v-model="manualInputText"
              type="textarea"
              :rows="3"
              placeholder="请输入您的代购需求"
              style="margin-top: 10px;"
            />
            <button 
              class="confirm-button"
              @click="confirmManualInput"
              style="margin-top: 10px;"
            >
              确认输入
            </button>
          </div>
          
          <!-- 识别结果 -->
          <div class="result-section" v-if="recognizedText">
            <div class="result-header">
              <el-icon><DocumentCopy /></el-icon>
              <span>识别结果</span>
            </div>
            <div class="result-content">
              {{ recognizedText }}
            </div>
          </div>
          
          <!-- 手动输入按钮 -->
          <div class="manual-input-toggle" v-if="!showManualInput && !forceShowManualInput && !recognizedText">
            <button class="manual-input-button" @click="forceShowManualInput = true">
              <el-icon><Edit /></el-icon>
              <span>手动输入需求</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="submit-section" v-if="recognizedText">
        <button 
          class="submit-button"
          @click="submitPurchase"
          :disabled="submitting"
        >
          <div class="submit-content">
            <el-icon size="24"><ShoppingCart /></el-icon>
            <span>{{ submitting ? '提交中...' : '提交代购' }}</span>
          </div>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  ArrowLeft,
  Microphone,
  VideoCameraFilled,
  Bell,
  DocumentCopy,
  ShoppingCart,
  Edit
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { createPurchaseRequest } from '@/api/purchase'

const router = useRouter()
const userStore = useUserStore()

const isRecording = ref(false)
const isProcessing = ref(false)
const voiceMessage = ref('')
const recognizedText = ref('')
const submitting = ref(false)
const showManualInput = ref(false)
const forceShowManualInput = ref(false)
const manualInputText = ref('')
let recognition: any = null

// 切换语音输入
const toggleVoiceInput = () => {
  if (!isRecording.value) {
    startRecording()
  } else {
    stopRecording()
  }
}

// 开始录音
const startRecording = async () => {
  console.log('开始录音函数被调用')
  
  // 检查浏览器兼容性
  console.log('检查浏览器兼容性')
  const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
  if (!SpeechRecognition) {
    console.error('浏览器不支持语音识别')
    voiceMessage.value = '您的浏览器不支持语音识别，请使用手动输入'
    showManualInput.value = true
    ElMessage.warning('您的浏览器不支持语音识别，请使用手动输入')
    return
  }
  
  console.log('浏览器支持语音识别')
  
  // 清理旧的recognition实例
  if (recognition) {
    try {
      console.log('清理旧的recognition实例')
      recognition.stop()
    } catch (e) {
      console.error('停止旧的recognition实例失败:', e)
    }
    recognition = null
  }
  
  // 请求麦克风权限
  try {
    console.log('请求麦克风权限')
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    console.log('麦克风权限获取成功')
    // 立即关闭流，因为我们只需要权限
    stream.getTracks().forEach(track => track.stop())
  } catch (error: any) {
    console.error('获取麦克风权限失败:', error)
    const errorMessage = error.message || '需要麦克风权限才能进行语音识别'
    voiceMessage.value = errorMessage + '，请在浏览器设置中允许'
    showManualInput.value = true
    ElMessage.warning(errorMessage + '，请在浏览器设置中允许')
    return
  }
  
  isRecording.value = true
  isProcessing.value = false
  voiceMessage.value = '正在录音，请说出您的代购需求...'
  console.log('创建语音识别实例')
  
  // 创建语音识别实例
  recognition = new SpeechRecognition()
  
  // 配置语音识别参数
  recognition.lang = 'zh-CN'
  recognition.continuous = false // 不使用连续识别，减少资源消耗
  recognition.interimResults = false // 不使用中间结果，只获取最终结果
  recognition.maxAlternatives = 1 // 只返回最佳结果
  
  recognition.onstart = () => {
    console.log('语音识别开始')
    voiceMessage.value = '正在录音，请说出您的代购需求...'
  }
  
  recognition.onresult = (event) => {
    console.log('语音识别结果:', event)
    // 检查是否有结果
    if (event.results && event.results.length > 0) {
      const result = event.results[0]
      if (result.isFinal) {
        const recognizedTextResult = result[0].transcript
        recognizedText.value = recognizedTextResult
        voiceMessage.value = '识别完成'
        isProcessing.value = false
        
        // 清除语音消息
        setTimeout(() => {
          voiceMessage.value = ''
        }, 2000)
        
        ElMessage.success('语音识别成功')
        // 停止识别
        if (recognition) {
          try {
            recognition.stop()
          } catch (e) {
            console.error('停止识别失败:', e)
          }
        }
      }
    }
  }
  
  recognition.onerror = (event) => {
    console.error('语音识别错误:', event.error)
    // 根据错误类型显示不同的提示
    let errorMessage = '语音识别失败，请重试'
    if (event.error === 'no-speech') {
      errorMessage = '未检测到语音，请重试'
    } else if (event.error === 'not-allowed') {
      errorMessage = '麦克风权限被拒绝，请在浏览器设置中允许'
      showManualInput.value = true
    } else if (event.error === 'aborted') {
      errorMessage = '语音识别已取消'
    } else if (event.error === 'network') {
      errorMessage = '网络连接失败，无法使用语音识别，请检查网络或是否可以访问 Google 语音服务，改用手动输入'
      showManualInput.value = true
      // 立即显示手动输入界面
      setTimeout(() => {
        showManualInput.value = true
      }, 500)
    } else if (event.error === 'audio-capture') {
      errorMessage = '无法访问麦克风，请检查设备是否正常'
      showManualInput.value = true
    } else if (event.error === 'not-supported') {
      errorMessage = '您的浏览器不支持语音识别，请使用手动输入'
      showManualInput.value = true
    }
    voiceMessage.value = errorMessage
    isProcessing.value = false
    isRecording.value = false
    ElMessage.warning(errorMessage)
  }
  
  recognition.onend = () => {
    console.log('语音识别结束')
    isRecording.value = false
    recognition = null
    if (voiceMessage.value === '正在录音，请说出您的代购需求...') {
      voiceMessage.value = '未检测到语音，请重试或使用手动输入'
      showManualInput.value = true
      ElMessage.warning('未检测到语音，请重试或使用手动输入')
    }
  }
  
  try {
    console.log('启动语音识别')
    recognition.start()
  } catch (error: any) {
    console.error('启动语音识别失败:', error)
    const errorMessage = error.message || '启动语音识别失败'
    voiceMessage.value = errorMessage + '，请使用手动输入'
    showManualInput.value = true
    isProcessing.value = false
    isRecording.value = false
    ElMessage.warning(errorMessage + '，请使用手动输入')
  }
  
  // 设置超时自动停止
  setTimeout(() => {
    if (isRecording.value && recognition) {
      try {
        console.log('自动停止录音')
        recognition.stop()
      } catch (e) {
        console.error('自动停止录音失败:', e)
      }
    }
  }, 10000) // 10秒后自动停止
}

// 停止录音
const stopRecording = () => {
  console.log('停止录音函数被调用')
  if (recognition) {
    try {
      console.log('调用 recognition.stop()')
      recognition.stop()
      console.log('recognition.stop() 调用成功')
      isRecording.value = false
      voiceMessage.value = '录音已结束'
      isProcessing.value = false
      recognition = null
    } catch (error) {
      console.error('停止录音失败:', error)
      // 即使停止失败，也要更新状态
      isRecording.value = false
      voiceMessage.value = '录音已结束'
      isProcessing.value = false
      recognition = null
    }
  } else {
    console.warn('recognition 实例不存在')
    isRecording.value = false
    voiceMessage.value = '录音已结束'
    isProcessing.value = false
  }
}

// 确认手动输入
const confirmManualInput = () => {
  if (manualInputText.value.trim()) {
    recognizedText.value = manualInputText.value.trim()
    voiceMessage.value = '输入完成'
    showManualInput.value = false
    forceShowManualInput.value = false
    // 清除语音消息
    setTimeout(() => {
      voiceMessage.value = ''
    }, 2000)
    ElMessage.success('手动输入成功')
  } else {
    ElMessage.warning('请输入代购需求')
  }
}

// 提交代购请求
const submitPurchase = async () => {
  if (!recognizedText.value.trim()) {
    ElMessage.warning('请先进行语音输入或手动输入')
    return
  }
  
  try {
    submitting.value = true
    
    const requestData = {
      elderId: userStore.elderInfo?.id || 0,
      content: recognizedText.value.trim(),
      estimatedAmount: undefined
    }
    
    const response = await createPurchaseRequest(requestData)
    console.log('代购请求响应:', response)
    
    // 响应拦截器已经处理了code，response就是data部分
    if (response && response.id) {
      ElMessage.success('代购请求提交成功')
      recognizedText.value = ''
      
      // 语音播报
      speak('代购请求已提交，我们会尽快为您处理')
    } else {
      ElMessage.error('提交失败')
    }
  } catch (error) {
    console.error('提交代购请求失败:', error)
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

// 语音播报
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

// 获取消息样式类
const getMessageClass = () => {
  if (isRecording.value) return 'recording'
  if (isProcessing.value) return 'processing'
  return 'success'
}

const goBack = () => {
  router.push('/home')
}

onMounted(() => {
  userStore.initUser()
})
</script>

<style scoped>
.purchase-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.circle-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.circle-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

/* 页面头部 */
.purchase-header {
  position: relative;
  z-index: 1;
  padding: 20px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
  color: white;
}

.back-button {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.header-info h1 {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.header-info p {
  margin: 0;
  font-size: 18px;
  opacity: 0.9;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

/* 主要内容 */
.main-content {
  position: relative;
  z-index: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30px;
}

/* 语音输入区域 */
.voice-section {
  width: 100%;
  max-width: 500px;
}

.voice-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 40px 30px;
  backdrop-filter: blur(20px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  text-align: center;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 语音可视化 */
.voice-visualizer {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
  opacity: 0.3;
  transition: opacity 0.3s ease;
}

.voice-visualizer.recording {
  opacity: 1;
}

.visualizer-waves {
  display: flex;
  gap: 4px;
  align-items: center;
}

.wave {
  width: 4px;
  height: 20px;
  background: #667eea;
  border-radius: 2px;
  animation: wave 1s ease-in-out infinite;
}

.wave:nth-child(1) { animation-delay: 0s; }
.wave:nth-child(2) { animation-delay: 0.1s; }
.wave:nth-child(3) { animation-delay: 0.2s; }
.wave:nth-child(4) { animation-delay: 0.3s; }
.wave:nth-child(5) { animation-delay: 0.4s; }

.voice-visualizer.recording .wave {
  animation-duration: 0.5s;
  height: 40px;
}

@keyframes wave {
  0%, 100% {
    height: 20px;
  }
  50% {
    height: 40px;
  }
}

/* 语音按钮 */
.voice-button-container {
  margin-bottom: 30px;
}

.voice-button {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
}

.voice-button:hover {
  transform: scale(1.05);
  box-shadow: 0 15px 40px rgba(102, 126, 234, 0.4);
}

.voice-button.recording {
  background: linear-gradient(135deg, #f56c6c 0%, #e74c3c 100%);
  animation: pulse 2s infinite;
}

.voice-button.processing {
  background: linear-gradient(135deg, #e6a23c 0%, #d68910 100%);
  cursor: not-allowed;
}

.button-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 15px;
}

.mic-icon {
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.button-text {
  font-size: 18px;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

@keyframes pulse {
  0% {
    box-shadow: 0 10px 30px rgba(245, 108, 108, 0.3);
  }
  50% {
    box-shadow: 0 10px 50px rgba(245, 108, 108, 0.6);
  }
  100% {
    box-shadow: 0 10px 30px rgba(245, 108, 108, 0.3);
  }
}

/* 状态消息 */
.status-message {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 15px 20px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.status-message.recording {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
  border: 1px solid rgba(245, 108, 108, 0.2);
}

.status-message.processing {
  background: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
  border: 1px solid rgba(230, 162, 60, 0.2);
}

.status-message.success {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
  border: 1px solid rgba(103, 194, 58, 0.2);
}

.status-icon {
  animation: bounce 1s infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

/* 识别结果 */
.result-section {
  background: rgba(248, 249, 250, 0.8);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  backdrop-filter: blur(10px);
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 15px;
  color: #667eea;
  font-weight: 600;
  font-size: 16px;
}

.result-content {
  font-size: 18px;
  line-height: 1.6;
  color: #333;
  background: white;
  padding: 15px;
  border-radius: 12px;
  border: 1px solid rgba(102, 126, 234, 0.1);
  text-align: center;
}

/* 手动输入区域样式 */
.manual-input-section {
  margin-top: 20px;
  padding: 15px;
  background: rgba(248, 249, 250, 0.8);
  border-radius: 16px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  backdrop-filter: blur(10px);
}

.input-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #667eea;
  font-weight: 600;
  font-size: 16px;
}

.confirm-button {
  width: 100%;
  height: 40px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.confirm-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
}

/* 手动输入切换按钮 */
.manual-input-toggle {
  margin-top: 20px;
  text-align: center;
}

.manual-input-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: 1px solid #667eea;
  border-radius: 20px;
  background: transparent;
  color: #667eea;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.manual-input-button:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.2);
}

/* 提交按钮 */
.submit-section {
  width: 100%;
  max-width: 400px;
}

.submit-button {
  width: 100%;
  height: 60px;
  border: none;
  border-radius: 16px;
  background: linear-gradient(135deg, #67c23a 0%, #5daf34 100%);
  color: white;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 10px 30px rgba(103, 194, 58, 0.3);
  position: relative;
  overflow: hidden;
}

.submit-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 15px 40px rgba(103, 194, 58, 0.4);
}

.submit-button:disabled {
  background: linear-gradient(135deg, #c0c4cc 0%, #a8abb2 100%);
  cursor: not-allowed;
  box-shadow: none;
}

.submit-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  height: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }
  
  .header-info h1 {
    font-size: 28px;
  }
  
  .header-info p {
    font-size: 16px;
  }
  
  .voice-card {
    padding: 30px 20px;
  }
  
  .voice-button {
    width: 150px;
    height: 150px;
  }
  
  .button-text {
    font-size: 16px;
  }
  
  .result-content {
    font-size: 16px;
    padding: 12px;
  }
}

@media (max-width: 480px) {
  .purchase-container {
    padding: 15px;
  }
  
  .voice-button {
    width: 120px;
    height: 120px;
  }
  
  .mic-icon {
    font-size: 36px !important;
  }
  
  .button-text {
    font-size: 14px;
  }
  
  .result-content {
    font-size: 14px;
  }
}
</style>
