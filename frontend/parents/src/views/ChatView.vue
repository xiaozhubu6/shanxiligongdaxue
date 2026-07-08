<template>
  <div class="chat-container">
    <div class="header">
      <h2>AI助手</h2>
      <p>有什么需要帮助的吗？我来为您解答</p>
    </div>
    
    <div class="chat-content">
      <!-- 聊天记录 -->
      <div class="chat-messages" ref="messagesContainer">
        <div 
          v-for="message in messages" 
          :key="message.id"
          :class="['message', message.type]"
        >
          <div class="message-avatar">
            <el-avatar :size="40" v-if="message.type === 'user'">
              {{ elderInfo?.name?.charAt(0) }}
            </el-avatar>
            <el-avatar :size="40" v-else>
              <el-icon><Robot /></el-icon>
            </el-avatar>
          </div>
          <div class="message-content">
            <div class="message-text">{{ message.content }}</div>
            <div class="message-time">{{ message.time }}</div>
          </div>
        </div>
      </div>
      
      <!-- 快捷回复 -->
      <div class="quick-replies">
        <h3>快捷回复</h3>
        <div class="reply-buttons">
          <el-button 
            v-for="reply in quickReplies" 
            :key="reply.id"
            size="large"
            @click="sendQuickReply(reply)"
            style="margin: 5px; font-size: 16px;"
          >
            {{ reply.text }}
          </el-button>
        </div>
      </div>
      
      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-container">
          <el-input
            v-model="inputMessage"
            placeholder="请输入您的问题..."
            size="large"
            @keyup.enter="sendMessage"
            style="font-size: 18px;"
          >
            <template #append>
              <el-button 
                type="primary" 
                @click="sendMessage"
                :loading="sending"
                style="font-size: 16px; padding: 12px 20px;"
              >
                发送
              </el-button>
            </template>
          </el-input>
        </div>
        
        <!-- 语音输入按钮 -->
        <div class="voice-input">
          <el-button 
            size="large"
            :type="isRecording ? 'danger' : 'default'"
            @click="toggleVoiceInput"
            style="font-size: 16px; padding: 12px 20px;"
          >
            <el-icon><Microphone v-if="!isRecording" /></el-icon>
            <el-icon><VideoCameraFilled v-else /></el-icon>
            {{ isRecording ? '停止录音' : '语音输入' }}
          </el-button>
        </div>
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Robot, Bell, Microphone, VideoCameraFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const inputMessage = ref('')
const sending = ref(false)
const isRecording = ref(false)
const voiceMessage = ref('')
const messagesContainer = ref(null)

const elderInfo = computed(() => userStore.elderInfo)

const messages = ref([
  {
    id: 1,
    type: 'bot',
    content: '您好！我是您的AI助手，有什么可以帮助您的吗？',
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
])

const quickReplies = ref([
  { id: 1, text: '我需要买药' },
  { id: 2, text: '我身体不舒服' },
  { id: 3, text: '我想找人聊天' },
  { id: 4, text: '今天天气怎么样' },
  { id: 5, text: '帮我叫个外卖' },
  { id: 6, text: '我需要帮助' }
])

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim()) {
    ElMessage.warning('请输入消息')
    return
  }
  
  const userMessage = {
    id: Date.now(),
    type: 'user',
    content: inputMessage.value,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  
  messages.value.push(userMessage)
  inputMessage.value = ''
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()
  
  // 模拟AI回复
  setTimeout(() => {
    const botReply = generateAIReply(userMessage.content)
    const botMessage = {
      id: Date.now() + 1,
      type: 'bot',
      content: botReply,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
    messages.value.push(botMessage)
    
    nextTick(() => {
      scrollToBottom()
      speak(botReply) // 语音播报AI回复
    })
  }, 1000)
}

// 快捷回复
const sendQuickReply = (reply) => {
  inputMessage.value = reply.text
  sendMessage()
}

// 生成AI回复（模拟）
const generateAIReply = (userMessage) => {
  const replies = [
    '好的，我来帮您联系社区工作人员。',
    '我明白了，您的需求已经记录，我们会尽快处理。',
    '别担心，我会通知相关人员来帮助您。',
    '好的，我来帮您查询相关信息。',
    '我理解您的情况，请稍等，我来协助您。'
  ]
  
  if (userMessage.includes('药')) {
    return '好的，我来帮您联系社区工作人员，他们会帮您购买药品。请稍等片刻。'
  } else if (userMessage.includes('不舒服')) {
    return '听到您不舒服我很担心，我马上通知社区工作人员来查看您的情况。请保持电话畅通。'
  } else if (userMessage.includes('天气')) {
    return '今天天气晴朗，温度25度，适合出门散步。记得多喝水，注意防晒。'
  } else if (userMessage.includes('外卖')) {
    return '好的，我来帮您叫外卖。您想吃什么呢？我可以帮您联系附近的餐厅。'
  } else {
    return replies[Math.floor(Math.random() * replies.length)]
  }
}

// 语音输入
const toggleVoiceInput = () => {
  if (!isRecording.value) {
    startRecording()
  } else {
    stopRecording()
  }
}

// 开始录音
const startRecording = () => {
  if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) {
    ElMessage.warning('您的浏览器不支持语音识别')
    return
  }
  
  isRecording.value = true
  voiceMessage.value = '正在录音，请说话...'
  
  // 创建语音识别实例
  const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
  const recognition = new SpeechRecognition()
  
  recognition.lang = 'zh-CN'
  recognition.continuous = false
  recognition.interimResults = false
  
  recognition.onstart = () => {
    voiceMessage.value = '正在录音，请说话...'
  }
  
  recognition.onresult = (event) => {
    const recognizedText = event.results[0][0].transcript
    inputMessage.value = recognizedText
    voiceMessage.value = ''
    ElMessage.success(`语音识别结果：${recognizedText}`)
    
    // 自动发送消息
    setTimeout(() => {
      sendMessage()
    }, 500)
  }
  
  recognition.onerror = (event) => {
    console.error('语音识别错误:', event.error)
    voiceMessage.value = '语音识别失败，请重试'
    ElMessage.error('语音识别失败，请重试')
    isRecording.value = false
  }
  
  recognition.onend = () => {
    isRecording.value = false
    if (voiceMessage.value === '正在录音，请说话...') {
      voiceMessage.value = ''
    }
  }
  
  recognition.start()
  
  // 设置超时自动停止
  setTimeout(() => {
    if (isRecording.value) {
      recognition.stop()
    }
  }, 10000) // 10秒后自动停止
}

// 停止录音
const stopRecording = () => {
  // 语音识别会在recognition.onend中自动处理
  isRecording.value = false
  voiceMessage.value = '录音结束，正在识别...'
}

// 语音播报
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

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

onMounted(() => {
  nextTick(() => {
    scrollToBottom()
  })
})
</script>

<style scoped>
.chat-container {
  height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 20px;
  flex-shrink: 0;
}

.header h2 {
  color: #333;
  margin: 0;
  font-size: 28px;
}

.header p {
  color: #666;
  margin: 10px 0 0;
  font-size: 18px;
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: white;
  border-radius: 10px;
  margin-bottom: 20px;
}

.message {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
  margin: 0 10px;
}

.message-content {
  flex: 1;
  max-width: 70%;
}

.message-text {
  background: #f0f2f5;
  padding: 15px;
  border-radius: 10px;
  color: #333;
  font-size: 16px;
  line-height: 1.5;
  word-wrap: break-word;
}

.user .message-text {
  background: #409eff;
  color: white;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
  text-align: right;
}

.user .message-time {
  text-align: left;
}

.quick-replies {
  background: white;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 20px;
}

.quick-replies h3 {
  color: #333;
  margin: 0 0 15px 0;
  font-size: 18px;
}

.reply-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.input-area {
  background: white;
  padding: 20px;
  border-radius: 10px;
  display: flex;
  gap: 15px;
  align-items: center;
}

.input-container {
  flex: 1;
}

.voice-input {
  flex-shrink: 0;
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
.el-input {
  font-size: 18px !important;
}

.el-button {
  font-size: 16px !important;
  padding: 12px 20px !important;
}

@media (max-width: 768px) {
  .chat-container {
    padding: 10px;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .input-area {
    flex-direction: column;
    gap: 10px;
  }
  
  .reply-buttons {
    justify-content: center;
  }
}
</style>
