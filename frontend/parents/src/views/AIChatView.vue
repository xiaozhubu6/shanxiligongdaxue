<template>
  <div class="chat-container">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
      <div class="decoration-circle circle-4"></div>
    </div>

    <!-- 聊天头部 -->
    <div class="chat-header">
      <div class="header-content">
        <div class="back-button" @click="goBack">
          <el-icon size="24"><ArrowLeft /></el-icon>
        </div>
        <div class="ai-info">
          <div class="ai-avatar">
            <div class="avatar-glow"></div>
            <el-icon size="32"><ChatDotRound /></el-icon>
          </div>
          <div class="ai-details">
            <h1>智能助手</h1>
            <p>您的贴心健康伙伴，随时为您服务</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 聊天消息区域 -->
    <div class="chat-messages" ref="messagesContainer">
      <div 
        v-for="message in messages" 
        :key="message.id"
        :class="['message-item', message.type === 'user' ? 'user-message' : 'ai-message']"
      >
        <div class="message-avatar">
          <div class="avatar-wrapper" :class="message.type === 'user' ? 'user-avatar' : 'ai-avatar'">
            <span v-if="message.type === 'user'">{{ elderInfo?.name?.charAt(0) || '老' }}</span>
            <el-icon v-else size="24"><ChatDotRound /></el-icon>
          </div>
        </div>
        <div class="message-content">
          <div class="message-bubble">
            <div class="message-text">{{ message.content }}</div>
            <div class="message-time">{{ formatTime(message.timestamp) }}</div>
          </div>
        </div>
      </div>
      
      <!-- AI正在输入指示器 -->
      <div class="typing-indicator" v-if="isTyping">
        <div class="typing-avatar">
          <el-icon size="24"><ChatDotRound /></el-icon>
        </div>
        <div class="typing-bubble">
          <div class="typing-dots">
            <div class="dot"></div>
            <div class="dot"></div>
            <div class="dot"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input">
      <div class="input-wrapper">
        <div class="input-container">
          <div class="input-field">
            <textarea
              v-model="inputMessage"
              placeholder="说点什么吧，我在听..."
              @keydown.enter.exact.prevent="sendMessage"
              :disabled="isTyping"
              class="message-input"
              rows="1"
            ></textarea>
            <div class="input-actions">
              <button 
                class="send-button"
                @click="sendMessage" 
                :disabled="!inputMessage.trim() || isTyping"
              >
                <el-icon size="20"><Promotion /></el-icon>
              </button>
            </div>
          </div>
        </div>
        
        <!-- 快捷回复 -->
        <div class="quick-replies">
          <div class="quick-reply-header">
            <el-icon><ChatDotRound /></el-icon>
            <span>快捷回复</span>
          </div>
          <div class="quick-reply-grid">
            <div 
              v-for="reply in quickReplies" 
              :key="reply.id"
              class="quick-reply-item"
              @click="sendQuickReply(reply.text)"
            >
              <div class="reply-icon">
                <el-icon>
                  <component :is="getReplyIcon(reply.text)" />
                </el-icon>
              </div>
              <span>{{ reply.text }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { 
  ChatDotRound, 
  ArrowLeft,
  Promotion,
  User,
  QuestionFilled,
  DocumentCopy,
  Phone,
  ChatLineSquare
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

const elderInfo = computed(() => userStore.elderInfo)
const messagesContainer = ref()
const inputMessage = ref('')
const isTyping = ref(false)

const messages = ref([
  {
    id: 1,
    type: 'ai',
    content: '您好！我是您的智能健康助手。我可以帮您解答健康问题、提供生活建议、陪您聊天。今天感觉怎么样？有什么需要帮助的吗？',
    timestamp: new Date()
  }
])

const quickReplies = ref([
  { id: 1, text: '我今天感觉很好' },
  { id: 2, text: '我有点不舒服' },
  { id: 3, text: '我想了解健康知识' },
  { id: 4, text: '提醒我吃药' },
  { id: 5, text: '帮我联系社区' },
  { id: 6, text: '我想聊天' }
])

// 获取快捷回复图标
const getReplyIcon = (text: string) => {
  if (text.includes('很好')) return User
  if (text.includes('不舒服')) return QuestionFilled
  if (text.includes('健康')) return DocumentCopy
  if (text.includes('药')) return Phone
  if (text.includes('联系')) return ChatLineSquare
  return ChatDotRound
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isTyping.value) return
  
  const userMsg = {
    id: Date.now(),
    type: 'user',
    content: inputMessage.value.trim(),
    timestamp: new Date()
  }
  
  messages.value.push(userMsg)
  inputMessage.value = ''
  isTyping.value = true
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()
  
  try {
    // 调用后端AI聊天API
    const response = await request.post('/ai-chat', {
      message: userMsg.content
    })
    
    const aiMsg = {
      id: Date.now() + 1,
      type: 'ai',
      content: response.message,
      timestamp: new Date()
    }
    
    messages.value.push(aiMsg)
    isTyping.value = false
    
    nextTick(() => {
      scrollToBottom()
    })
    
    // 语音播报
    speak(aiMsg.content)
  } catch (error) {
    console.error('AI回复失败:', error)
    
    const errorMsg = {
      id: Date.now() + 1,
      type: 'ai',
      content: '抱歉，我暂时无法为您提供帮助。请稍后再试。',
      timestamp: new Date()
    }
    
    messages.value.push(errorMsg)
    isTyping.value = false
    
    nextTick(() => {
      scrollToBottom()
    })
    
    // 语音播报错误信息
    speak(errorMsg.content)
  }
}

const sendQuickReply = (replyText: string) => {
  inputMessage.value = replyText
  sendMessage()
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const formatTime = (timestamp: Date) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const goBack = () => {
  router.push('/home')
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
})
</script>

<style scoped>
.chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #87CEFA 0%, #4682B4 100%);
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
  animation: float 8s ease-in-out infinite;
}

.circle-1 {
  width: 150px;
  height: 150px;
  top: 10%;
  left: 5%;
  animation-delay: 0s;
}

.circle-2 {
  width: 100px;
  height: 100px;
  top: 20%;
  right: 10%;
  animation-delay: 2s;
}

.circle-3 {
  width: 80px;
  height: 80px;
  bottom: 30%;
  left: 15%;
  animation-delay: 4s;
}

.circle-4 {
  width: 120px;
  height: 120px;
  bottom: 10%;
  right: 20%;
  animation-delay: 6s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

/* 聊天头部 */
.chat-header {
  position: relative;
  z-index: 1;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
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

.ai-info {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
}

.ai-avatar {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(64, 158, 255, 0.3);
}

.avatar-glow {
  position: absolute;
  top: -5px;
  left: -5px;
  right: -5px;
  bottom: -5px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  opacity: 0.3;
  animation: glow 2s ease-in-out infinite;
}

@keyframes glow {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(1.1);
  }
}

.ai-details h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.ai-details p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

/* 聊天消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  position: relative;
  z-index: 1;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
  gap: 15px;
  animation: slideIn 0.3s ease-out;
}

.user-message {
  flex-direction: row-reverse;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-avatar {
  flex-shrink: 0;
}

.avatar-wrapper {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 18px;
  color: white;
  position: relative;
}

.user-avatar {
  background: linear-gradient(135deg, #f56c6c 0%, #e74c3c 100%);
  box-shadow: 0 5px 15px rgba(245, 108, 108, 0.3);
}

.ai-avatar {
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  box-shadow: 0 5px 15px rgba(64, 158, 255, 0.3);
}

.message-content {
  max-width: 70%;
  flex: 1;
}

.message-bubble {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 15px 20px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.user-message .message-bubble {
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  color: white;
  border-radius: 20px 20px 20px 5px;
}

.user-message .message-bubble::before {
  content: '';
  position: absolute;
  top: 15px;
  right: -8px;
  width: 0;
  height: 0;
  border-left: 8px solid #67c23a;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
}

.ai-message .message-bubble {
  background: rgba(255, 255, 255, 0.95);
  color: #333;
  border-radius: 20px 5px 20px 20px;
}

.ai-message .message-bubble::before {
  content: '';
  position: absolute;
  top: 15px;
  left: -8px;
  width: 0;
  height: 0;
  border-right: 8px solid rgba(255, 255, 255, 0.95);
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
}

.message-text {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 8px;
  word-wrap: break-word;
}

.message-time {
  font-size: 12px;
  opacity: 0.7;
}

/* AI正在输入指示器 */
.typing-indicator {
  display: flex;
  margin-bottom: 20px;
  gap: 15px;
  animation: slideIn 0.3s ease-out;
}

.typing-avatar {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 5px 15px rgba(64, 158, 255, 0.3);
}

.typing-bubble {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 15px 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px 5px 20px 20px;
}

.typing-dots {
  display: flex;
  gap: 4px;
  align-items: center;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #667eea;
  animation: typing 1.4s infinite;
}

.dot:nth-child(1) { animation-delay: 0s; }
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  30% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 输入区域 */
.chat-input {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  padding: 20px;
}

.input-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

.input-container {
  margin-bottom: 20px;
}

.input-field {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 25px;
  padding: 15px 20px;
  display: flex;
  align-items: flex-end;
  gap: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.message-input {
  flex: 1;
  border: none;
  background: transparent;
  color: #333;
  font-size: 16px;
  line-height: 1.5;
  resize: none;
  outline: none;
  font-family: inherit;
}

.message-input::placeholder {
  color: #999;
}

.message-input:disabled {
  opacity: 0.6;
}

.input-actions {
  display: flex;
  gap: 10px;
}

.send-button {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 5px 15px rgba(64, 158, 255, 0.3);
}

.send-button:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.4);
}

.send-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: scale(1);
}

/* 快捷回复 */
.quick-replies {
  margin-top: 20px;
}

.quick-reply-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  color: white;
  font-size: 16px;
  font-weight: 600;
  opacity: 0.9;
}

.quick-reply-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.quick-reply-item {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 15px;
  padding: 12px 15px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: white;
  font-size: 14px;
  text-align: center;
}

.quick-reply-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.reply-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }
  
  .ai-details h1 {
    font-size: 20px;
  }
  
  .ai-details p {
    font-size: 14px;
  }
  
  .chat-messages {
    padding: 15px;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .message-bubble {
    padding: 12px 15px;
  }
  
  .message-text {
    font-size: 15px;
  }
  
  .input-field {
    padding: 12px 15px;
  }
  
  .quick-reply-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
  
  .quick-reply-item {
    padding: 10px 12px;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .chat-container {
    padding: 10px;
  }
  
  .chat-header {
    padding: 15px;
  }
  
  .ai-avatar {
    width: 50px;
    height: 50px;
  }
  
  .avatar-wrapper {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }
  
  .message-content {
    max-width: 90%;
  }
  
  .quick-reply-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-reply-item {
    padding: 12px;
    font-size: 14px;
  }
}
</style>
