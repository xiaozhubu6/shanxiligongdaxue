<template>
  <div class="home-container">
    <!-- 老人信息卡片 -->
    <div class="elder-info-card">
      <div class="avatar-section">
        <el-avatar :size="80" :src="elderInfo?.avatar">
          {{ elderInfo?.name?.charAt(0) }}
        </el-avatar>
        <h2>{{ elderInfo?.name || '老人' }}</h2>
        <p>{{ elderInfo?.age }}岁 · {{ elderInfo?.gender }}</p>
      </div>
      <div class="status-section">
        <div class="status-item">
          <span class="label">今日状态</span>
          <el-tag :type="elderInfo?.todayFaceChecked ? 'success' : 'warning'" size="large">
            {{ elderInfo?.todayFaceChecked ? '已打卡' : '待打卡' }}
          </el-tag>
        </div>
        <div class="status-item">
          <span class="label">账户余额</span>
          <div class="balance-container">
            <span class="value">¥{{ elderInfo?.balance || 0 }}</span>
            <el-button 
              type="text" 
              size="small" 
              @click="refreshBalance"
              :loading="refreshingBalance"
              title="刷新余额"
            >
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </div>
        <div class="status-item">
          <el-button @click="handleLogout" type="danger" plain size="small">
            <el-icon><SwitchButton /></el-icon>
            退出系统
          </el-button>
        </div>
      </div>
    </div>

    <!-- 功能模块 -->
    <div class="functions-container">
      <!-- 滑动区域 -->
      <div class="functions-scrollable" @click="handleScrollContainerClick">
        <div class="functions-grid">
          <!-- 每日刷脸 -->
          <div class="function-card" @click.stop="handleDailyFaceCheck">
            <div class="function-icon">
              <el-icon size="48" color="#409eff"><UserFilled /></el-icon>
            </div>
            <h3>每日打卡</h3>
            <p>每日拍照打卡，确认安全</p>
            <div class="function-status">
              <el-tag 
                :type="getFaceCheckTagType()"
                :effect="elderInfo?.hasRecheckRequest ? 'dark' : 'plain'"
              >
                {{ getFaceCheckStatusText() }}
              </el-tag>
            </div>
          </div>

          <!-- 代购服务 -->
          <div class="function-card" @click.stop="goToPurchase">
            <div class="function-icon">
              <el-icon size="48" color="#67c23a"><ShoppingCart /></el-icon>
            </div>
            <h3>代购服务</h3>
            <p>买菜、日用品代购</p>
            <div class="function-status">
              <el-tag type="info">社区代购</el-tag>
            </div>
          </div>

          <!-- AI聊天 -->
          <div class="function-card" @click.stop="goToChat">
            <div class="function-icon">
              <el-icon size="48" color="#e6a23c"><ChatDotRound /></el-icon>
            </div>
            <h3>AI聊天</h3>
            <p>智能陪伴与问答</p>
            <div class="function-status">
              <el-tag type="info">在线助手</el-tag>
            </div>
          </div>

          <!-- 一键呼叫 -->
          <div class="function-card" @click.stop="handleEmergencyCall">
            <div class="function-icon">
              <el-icon size="48" color="#f56c6c"><Warning /></el-icon>
            </div>
            <h3>一键呼叫</h3>
            <p>紧急求助，快速响应</p>
            <div class="function-status">
              <el-tag type="danger">紧急呼叫</el-tag>
            </div>
          </div>

          <!-- 每月评价 -->
          <div class="function-card" @click.stop="goToReview">
            <div class="function-icon">
              <el-icon size="48" color="#909399"><Star /></el-icon>
            </div>
            <h3>每月评价</h3>
            <p>服务满意度评价</p>
            <div class="function-status">
              <el-tag type="warning">月度评价</el-tag>
            </div>
          </div>

          <!-- 其他功能 -->
          <div class="function-card" @click.stop="showOtherFeatures">
            <div class="function-icon">
              <el-icon size="48" color="#606266"><More /></el-icon>
            </div>
            <h3>其他功能</h3>
            <p>更多服务功能</p>
            <div class="function-status">
              <el-tag type="info">更多功能</el-tag>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 滑动指示器 -->
      <div class="scroll-indicator">
        <div class="indicator-dots">
          <div 
            v-for="(_, index) in totalPages" 
            :key="index"
            :class="['dot', { active: currentPage === index }]"
            @click="scrollToPage(index)"
          ></div>
        </div>
      </div>
    </div>

    <!-- 今日提醒 -->
    <div class="reminders-section">
      <h3>今日提醒</h3>
      <div class="reminders-list">
        <div class="reminder-item" v-for="reminder in reminders" :key="reminder.id">
          <div class="reminder-icon">
            <el-icon :color="reminder.color">
              <component :is="reminder.icon" />
            </el-icon>
          </div>
          <div class="reminder-content">
            <h4>{{ reminder.title }}</h4>
            <p>{{ reminder.content }}</p>
            <span class="time">{{ reminder.time }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 摄像头弹窗 -->
  <el-dialog 
    v-model="showCameraDialog" 
    title="每日打卡拍照" 
    width="600px" 
    :show-close="false"
    :close-on-click-modal="false"
  >
    <div class="camera-container">
      <!-- 摄像头预览 -->
      <div class="camera-preview" v-loading="cameraLoading">
        <video 
          ref="videoRef"
          autoplay 
          playsinline 
          muted
          style="width: 100%; max-height: 300px; object-fit: cover; background: #000;"
        ></video>
        <div v-if="!cameraStream && !cameraLoading" class="camera-overlay">
          <el-icon size="48" color="#909399"><Camera /></el-icon>
          <p>摄像头准备中...</p>
        </div>
      </div>
      
      <!-- 拍照预览 -->
      <div class="photo-preview" v-if="capturedPhoto">
        <el-image 
          :src="capturedPhoto" 
          style="width: 100%; max-height: 300px; object-fit: contain;"
          fit="contain"
        />
      </div>
      
      <!-- 操作按钮 -->
      <div class="camera-actions">
        <!-- 调试信息 -->
        <div style="margin-bottom: 10px; padding: 10px; background: #f5f7fa; border-radius: 4px; font-size: 12px;">
          <div>capturedPhoto状态: {{ capturedPhoto ? '有数据(' + capturedPhoto.length + '字符)' : '无数据' }}</div>
          <div>cameraStream状态: {{ cameraStream ? '有流' : '无流' }}</div>
        </div>
        
        <div class="action-buttons">
          <el-button 
            v-if="!capturedPhoto"
            type="primary" 
            size="large" 
            @click="takePhoto"
            :loading="cameraLoading"
          >
            <el-icon><Camera /></el-icon>
            拍照
          </el-button>
          
          <el-button 
            v-if="capturedPhoto"
            type="success" 
            size="large" 
            @click="uploadPhoto"
            :loading="cameraLoading"
          >
            <el-icon><Upload /></el-icon>
            上传
          </el-button>
        </div>
        
        <div class="action-buttons">
          <el-button 
            v-if="capturedPhoto"
            @click="retakePhoto"
          >
            <el-icon><Refresh /></el-icon>
            重新拍照
          </el-button>
          
          <el-button 
            @click="closeCamera"
          >
            <el-icon><Close /></el-icon>
            取消
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>

  <!-- 其他功能弹窗 -->
  <el-dialog v-model="showFeaturesDialog" title="其他功能" width="500px">
    <div class="features-grid">
      <div class="feature-item" v-for="feature in otherFeatures" :key="feature.name">
        <div class="feature-icon">
          <el-icon :size="32" :color="feature.color">
            <component :is="feature.icon" />
          </el-icon>
        </div>
        <div class="feature-info">
          <h4>{{ feature.name }}</h4>
          <p>{{ feature.description }}</p>
          <el-tag type="info" size="small">正在开发</el-tag>
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="showFeaturesDialog = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 隐藏的canvas用于拍照 -->
  <canvas id="camera-canvas" style="display: none;"></canvas>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  ShoppingCart, 
  Warning, 
  ChatDotRound, 
  UserFilled, 
  Bell,
  SwitchButton,
  Camera,
  Upload,
  Close,
  Refresh,
  Star,
  More,
  Service,
  Message,
  Calendar,
  Setting,
  Sunny,
  Users,
  Document
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { emergencyApi, faceCheckApi } from '@/api'

const router = useRouter()
const userStore = useUserStore()

const elderInfo = computed(() => userStore.elderInfo)
const refreshingBalance = ref(false)

// 刷新余额
const refreshBalance = async () => {
  try {
    refreshingBalance.value = true
    await userStore.refreshBalance()
    ElMessage.success('余额已刷新')
  } catch (error) {
    console.error('刷新余额失败:', error)
    ElMessage.error('刷新余额失败')
  } finally {
    refreshingBalance.value = false
  }
}

// 打卡提醒相关
const hasCheckInNotify = ref(false)
const checkInNotifyInterval = ref(null)
const latestCheckInNotify = ref(null)

// 检查是否有新的打卡提醒
const checkCheckInNotify = async () => {
  if (!elderInfo.value?.id) return
  
  try {
    const response = await fetch(`http://localhost:8082/api/emergency/elder/${elderInfo.value.id}`)
    const events = await response.json()
    
    if (events && events.length > 0) {
      // 查找最新的打卡提醒事件
      const checkInNotifies = events.filter(event => 
        event.eventType === 'CHECKIN_NOTIFY' && event.status === 'pending'
      )
      
      if (checkInNotifies.length > 0) {
        // 获取最新的打卡提醒
        const latestNotify = checkInNotifies.sort((a, b) => {
          return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        })[0]
        
        // 如果是新的提醒，显示提醒
        if (!latestCheckInNotify.value || latestCheckInNotify.value.id !== latestNotify.id) {
          latestCheckInNotify.value = latestNotify
          showCheckInNotify(latestNotify.description || '请进行今日打卡')
        }
      }
    }
  } catch (error) {
    console.error('检查打卡提醒失败:', error)
  }
}

// 显示打卡提醒
const showCheckInNotify = (message) => {
  hasCheckInNotify.value = true
  
  // 显示消息提示
  ElMessage.warning({
    message: message,
    duration: 10000,
    showClose: true
  })
  
  // 语音播报
  speak(message)
  
  // 标记为已处理
  markNotifyAsHandled()
}

// 标记提醒为已处理
const markNotifyAsHandled = async () => {
  if (!latestCheckInNotify.value) return
  
  try {
    await fetch(`http://localhost:8082/api/emergency/${latestCheckInNotify.value.id}/handle`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    latestCheckInNotify.value.status = 'HANDLED'
  } catch (error) {
    console.error('标记提醒为已处理失败:', error)
  }
}

// 启动定期检查打卡提醒的定时器
const startCheckInNotifyInterval = () => {
  // 每30秒检查一次
  checkInNotifyInterval.value = setInterval(() => {
    checkCheckInNotify()
  }, 30000)
  
  // 立即检查一次
  checkCheckInNotify()
}

// 清除定时器
const clearCheckInNotifyInterval = () => {
  if (checkInNotifyInterval.value) {
    clearInterval(checkInNotifyInterval.value)
    checkInNotifyInterval.value = null
  }
}

// 加载老人信息
const loadElderInfo = async () => {
  try {
    // 强制刷新余额，确保显示最新数据
    await userStore.refreshBalance()
    await userStore.loadElderInfo()
    console.log('老人信息加载成功:', elderInfo.value)
  } catch (error) {
    console.error('加载老人信息失败:', error)
    ElMessage.error('加载个人信息失败')
  }
}

const reminders = ref([
  {
    id: 1,
    title: '每日刷脸提醒',
    content: '请记得今天进行刷脸确认，确保系统记录您的安全状态',
    time: '全天有效',
    icon: 'Bell',
    color: '#409eff'
  },
  {
    id: 2,
    title: '代购服务',
    content: '需要代购请联系社区，我们将为您提供生活用品代购服务',
    time: '服务时间 9:00-18:00',
    icon: 'ShoppingCart',
    color: '#67c23a'
  },
  {
    id: 3,
    title: '健康监测',
    content: '系统将监测您的日常活动，如有异常会及时通知社区',
    time: '24小时监测',
    icon: 'Heart',
    color: '#e6a23c'
  }
])

// 检查今日刷脸状态
const checkTodayFaceStatus = async () => {
  if (!elderInfo.value?.id) return
  
  try {
    const response = await faceCheckApi.checkTodayStatus(elderInfo.value.id)
    console.log('今日刷脸状态:', response)
    
    // 检查是否有重新打卡请求
    const recheckResponse = await faceCheckApi.checkRecheckRequest(elderInfo.value.id)
    console.log('重新打卡请求状态:', recheckResponse)
    
    // 更新老人信息中的刷脸状态
    const updatedElderInfo = {
      ...elderInfo.value,
      todayFaceChecked: response?.todayChecked || false,
      lastFaceCheckTime: response?.lastFaceCheckTime || null,
      lastFaceCheckPhoto: response?.photoUrl || null,
      hasRecheckRequest: recheckResponse?.hasRequest || false
    }
    userStore.setElderInfo(updatedElderInfo)
    
    // 如果有重新打卡请求，显示提示
    if (recheckResponse?.hasRequest) {
      ElMessage.warning('管理员请求您重新打卡，请完成今日打卡')
    }
    
  } catch (error) {
    console.error('检查刷脸状态失败:', error)
  }
}

// 获取打卡状态标签类型
const getFaceCheckTagType = () => {
  if (elderInfo.value?.hasRecheckRequest) {
    return 'warning' // 橙色，表示需要重新打卡
  }
  if (elderInfo.value?.todayFaceChecked) {
    return 'success' // 绿色，表示已打卡
  }
  return 'info' // 蓝色，表示待打卡
}

// 获取打卡状态文本
const getFaceCheckStatusText = () => {
  if (elderInfo.value?.hasRecheckRequest) {
    return '需重新打卡'
  }
  if (elderInfo.value?.todayFaceChecked) {
    return '今日已打卡'
  }
  return '待打卡'
}

// 每日刷脸确认
const handleDailyFaceCheck = async () => {
  // 检查是否已打卡且没有重新打卡请求
  if (elderInfo.value?.todayFaceChecked && !elderInfo.value?.hasRecheckRequest) {
    ElMessage.info('今日已完成打卡')
    return
  }

  // 如果有重新打卡请求，显示特殊提示
  if (elderInfo.value?.hasRecheckRequest) {
    try {
      await ElMessageBox.confirm(
        '管理员请求您重新打卡，请重新拍照确认。',
        '重新打卡请求',
        {
          confirmButtonText: '重新拍照',
          cancelButtonText: '取消',
          type: 'warning',
        }
      )
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('操作失败，请重试')
        console.error('重新打卡失败:', error)
      } else {
        ElMessage.info('已取消重新打卡')
      }
      return
    }
  } else {
    // 正常打卡确认
    try {
      await ElMessageBox.confirm(
        '确认进行每日打卡？需要拍照记录。',
        '每日打卡',
        {
          confirmButtonText: '开始拍照',
          cancelButtonText: '取消',
          type: 'info',
        }
      )
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('打卡失败，请重试')
        console.error('打卡失败:', error)
      } else {
        ElMessage.info('已取消打卡')
      }
      return
    }
  }

  try {
    // 打开摄像头界面
    await openCamera()
  } catch (error) {
    ElMessage.error('打开摄像头失败，请重试')
    console.error('打开摄像头失败:', error)
  }
}

// 拍照相关状态
const showCameraDialog = ref(false)
const cameraStream = ref(null)
const capturedPhoto = ref('')
const cameraLoading = ref(false)
const videoRef = ref(null)

// 打开摄像头
const openCamera = async () => {
  try {
    // 检查浏览器是否支持摄像头
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      ElMessage.error('您的浏览器不支持摄像头功能')
      return
    }

    cameraLoading.value = true
    console.log('开始请求摄像头权限...')
    
    // 获取摄像头权限
    const stream = await navigator.mediaDevices.getUserMedia({ 
      video: { 
        width: { ideal: 640 },
        height: { ideal: 480 },
        facingMode: 'user'
      } 
    })
    
    console.log('摄像头权限获取成功，视频流:', stream)
    cameraStream.value = stream
    showCameraDialog.value = true
    
    // 等待DOM更新后绑定视频流
    setTimeout(() => {
      if (videoRef.value) {
        console.log('绑定视频流到video元素')
        videoRef.value.srcObject = stream
        videoRef.value.play().then(() => {
          console.log('视频播放成功')
          cameraLoading.value = false
        }).catch(err => {
          console.error('视频播放失败:', err)
          ElMessage.error('视频播放失败，请重试')
          cameraLoading.value = false
        })
      } else {
        console.error('video元素未找到')
        cameraLoading.value = false
      }
    }, 300)
    
  } catch (error) {
    console.error('摄像头访问失败:', error)
    let errorMessage = '无法访问摄像头'
    
    if (error.name === 'NotAllowedError') {
      errorMessage = '摄像头权限被拒绝，请在浏览器设置中允许摄像头访问'
    } else if (error.name === 'NotFoundError') {
      errorMessage = '未找到摄像头设备'
    } else if (error.name === 'NotReadableError') {
      errorMessage = '摄像头被其他应用占用'
    }
    
    ElMessage.error(errorMessage)
    cameraLoading.value = false
  }
}

// 拍照
const takePhoto = () => {
  console.log('=== 开始拍照 ===')
  console.log('videoRef.value:', videoRef.value)
  console.log('capturedPhoto.value:', capturedPhoto.value)
  
  try {
    if (!videoRef.value) {
      console.error('摄像头初始化失败')
      ElMessage.error('摄像头初始化失败')
      return
    }
    
    const canvas = document.getElementById('camera-canvas')
    if (!canvas) {
      console.error('画布初始化失败')
      ElMessage.error('画布初始化失败')
      return
    }
    
    console.log('视频尺寸:', videoRef.value.videoWidth, 'x', videoRef.value.videoHeight)
    
    // 设置canvas尺寸
    canvas.width = videoRef.value.videoWidth || 640
    canvas.height = videoRef.value.videoHeight || 480
    
    console.log('Canvas尺寸:', canvas.width, 'x', canvas.height)
    
    // 绘制视频帧到canvas
    const context = canvas.getContext('2d')
    context.drawImage(videoRef.value, 0, 0, canvas.width, canvas.height)
    
    // 转换为Base64
    const photoData = canvas.toDataURL('image/jpeg', 0.8)
    console.log('照片数据长度:', photoData.length)
    console.log('照片数据前50字符:', photoData.substring(0, 50))
    
    capturedPhoto.value = photoData
    console.log('设置capturedPhoto后:', capturedPhoto.value.length)
    
    ElMessage.success('拍照成功！')
    
  } catch (error) {
    console.error('拍照失败:', error)
    ElMessage.error('拍照失败，请重试')
  }
  
  console.log('=== 拍照结束 ===')
}

// 重新拍照
const retakePhoto = () => {
  capturedPhoto.value = ''
  ElMessage.info('可以重新拍照')
}

// 上传照片
const uploadPhoto = async () => {
  if (!capturedPhoto.value) {
    ElMessage.warning('请先拍照')
    return
  }
  
  try {
    console.log('准备上传照片，数据长度:', capturedPhoto.value.length)
    console.log('照片数据前50字符:', capturedPhoto.value.substring(0, 50))
    
    ElMessage.info('正在上传照片...')
    
    // 构造请求数据
    const requestData = {
      result: 'success',
      photoData: capturedPhoto.value
    }
    console.log('请求数据:', requestData)
    console.log('请求数据中photoData长度:', requestData.photoData.length)
    
    // 调用后端API记录刷脸（包含照片）
    const response = await faceCheckApi.recordFaceCheck(
      elderInfo.value?.id || 1, 
      'success',
      capturedPhoto.value
    )
    console.log('打卡API响应:', response)
    console.log('响应类型:', typeof response)
    console.log('响应内容:', JSON.stringify(response))
    
    // 关闭摄像头对话框
    closeCamera()
    
    // 重新从后端获取最新的老人信息
    await checkTodayFaceStatus()
    
    ElMessage.success('打卡成功！今日状态已更新')
    
    // 语音播报
    speak('打卡成功，祝您生活愉快')

  } catch (error) {
    console.error('上传失败:', error)
    console.error('错误详情:', error.response?.data)
    ElMessage.error('上传失败，请重试')
  }
}

// 关闭摄像头
const closeCamera = () => {
  // 停止视频流
  if (cameraStream.value) {
    cameraStream.value.getTracks().forEach(track => track.stop())
    cameraStream.value = null
  }
  
  // 清理状态
  capturedPhoto.value = ''
  showCameraDialog.value = false
  cameraLoading.value = false
}

// 处理紧急呼叫
const handleEmergencyCall = async () => {
  try {
    await ElMessageBox.confirm(
      '确认发起紧急呼叫？社区将立即收到通知。',
      '紧急呼叫',
      {
        confirmButtonText: '确认呼叫',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    // 调用后端API创建紧急事件
    const emergencyData = {
      elderId: elderInfo.value?.id || 1,
      eventType: 'EMERGENCY_CALL',
      description: '老人通过系统发起紧急呼叫'
    }
    
    const response = await emergencyApi.createEvent(emergencyData)
    console.log('紧急呼叫API响应:', response)

    ElMessage.warning('紧急呼叫已发送！社区正在赶来')
    
    // 记录紧急事件
    console.log('紧急呼叫事件已记录', {
      elderId: elderInfo.value?.id,
      timestamp: new Date(),
      type: 'EMERGENCY_CALL',
      response: response
    })

    // 语音播报
    speak('紧急呼叫已发送，请保持冷静，帮助正在路上')

  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('呼叫失败，请重试')
      console.error('紧急呼叫失败:', error)
    }
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

const goToPurchase = () => {
  console.log('点击代购服务按钮')
  try {
    router.push('/purchase')
  } catch (error) {
    console.error('路由跳转失败:', error)
    ElMessage.error('页面跳转失败')
  }
}

const goToReview = () => {
  console.log('点击每月评价按钮')
  try {
    router.push('/review')
  } catch (error) {
    console.error('路由跳转失败:', error)
    ElMessage.error('页面跳转失败')
  }
}

// 滑动相关
const currentPage = ref(0)
const totalPages = ref(2) // 6个功能，每页显示4个，需要2页

const scrollToPage = (pageIndex) => {
  currentPage.value = pageIndex
  const scrollContainer = document.querySelector('.functions-scrollable')
  if (scrollContainer) {
    const scrollWidth = scrollContainer.scrollWidth
    const containerWidth = scrollContainer.clientWidth
    const scrollPosition = (scrollWidth - containerWidth) * (pageIndex / (totalPages.value - 1))
    scrollContainer.scrollTo({
      left: scrollPosition,
      behavior: 'smooth'
    })
  }
}

// 其他功能弹窗
const showOtherFeatures = () => {
  showFeaturesDialog.value = true
}

const handleScrollContainerClick = (event) => {
  // 阻止滑动容器的点击事件冒泡，但不阻止子元素的点击
  event.stopPropagation()
}

const otherFeatures = [
  { name: '健康监测', icon: 'Service', color: '#67c23a', description: '血压、心率监测' },
  { name: '用药提醒', icon: 'Bell', color: '#409eff', description: '定时用药提醒' },
  { name: '消息通知', icon: 'Message', color: '#e6a23c', description: '家人消息推送' },
  { name: '日程提醒', icon: 'Calendar', color: '#f56c6c', description: '重要日程提醒' },
  { name: '系统设置', icon: 'Setting', color: '#909399', description: '个人设置管理' },
  { name: '天气查询', icon: 'Sunny', color: '#ffd700', description: '今日天气预报' },
  { name: '社区活动', icon: 'Users', color: '#409eff', description: '社区活动通知' },
  { name: '健康档案', icon: 'Document', color: '#67c23a', description: '个人健康记录' }
]

// 退出系统
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确认退出系统？退出后需要重新登录。',
      '退出系统',
      {
        confirmButtonText: '确认退出',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    // 清除用户信息
    userStore.clearUser()
    
    // 跳转到登录页
    router.push('/login')
    
    ElMessage.success('已安全退出系统')
    
    // 语音播报
    speak('您已安全退出系统，再见')

  } catch {
    ElMessage.info('已取消退出')
  }
}

const goToChat = () => {
  console.log('点击AI聊天按钮')
  try {
    router.push('/chat')
  } catch (error) {
    console.error('路由跳转失败:', error)
    ElMessage.error('页面跳转失败')
  }
}

onMounted(() => {
  loadElderInfo()
  setupScrollListener()
  startCheckInNotifyInterval()
})

onUnmounted(() => {
  clearCheckInNotifyInterval()
})

const setupScrollListener = () => {
  const scrollContainer = document.querySelector('.functions-scrollable')
  if (scrollContainer) {
    scrollContainer.addEventListener('scroll', () => {
      const scrollLeft = scrollContainer.scrollLeft
      const scrollWidth = scrollContainer.scrollWidth
      const containerWidth = scrollContainer.clientWidth
      
      if (scrollWidth > containerWidth) {
        const scrollPercentage = scrollLeft / (scrollWidth - containerWidth)
        currentPage.value = Math.round(scrollPercentage * (totalPages.value - 1))
      }
    })
  }
}

</script>

<style scoped>
.home-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: 100vh;
}

.elder-info-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 20px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  min-height: 120px;
}

.avatar-section {
  text-align: center;
  flex-shrink: 0;
}

.avatar-section h2 {
  margin: 10px 0 5px 0;
  font-size: 24px;
  font-weight: 600;
}

.avatar-section p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.status-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  flex-shrink: 0;
  margin-left: 20px;
}

.status-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  padding: 10px 15px;
  border-radius: 10px;
  backdrop-filter: blur(10px);
  min-height: 80px;
}

.status-item .label {
  font-size: 12px;
  opacity: 0.9;
  margin-bottom: 5px;
  text-align: center;
}

.status-item .value {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}

.status-item .el-button {
  margin-top: 5px;
  font-size: 12px;
  padding: 4px 8px;
  min-height: auto;
}

.status-item .el-button:hover {
  background-color: #f56c6c;
  color: white;
  border-color: #f56c6c;
}

.functions-container {
  margin-bottom: 30px;
}

.functions-scrollable {
  overflow-x: auto;
  overflow-y: hidden;
  scroll-behavior: smooth;
  scrollbar-width: thin;
  scrollbar-color: #ddd transparent;
  padding-bottom: 10px;
  pointer-events: auto;
}

.functions-scrollable::-webkit-scrollbar {
  height: 6px;
}

.functions-scrollable::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.functions-scrollable::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

.functions-scrollable::-webkit-scrollbar-thumb:hover {
  background: #bbb;
}

.functions-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 20px;
  min-width: fit-content;
  padding: 0 10px;
  pointer-events: auto;
}

.scroll-indicator {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

.indicator-dots {
  display: flex;
  gap: 8px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ddd;
  cursor: pointer;
  transition: all 0.3s ease;
}

.dot.active {
  background: #409eff;
  transform: scale(1.2);
}

.function-card {
  background: white;
  border-radius: 20px;
  padding: 25px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  min-width: 160px;
  min-height: 180px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border: 3px solid transparent;
  position: relative;
  overflow: hidden;
  pointer-events: auto;
}

.function-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  border-color: #409eff;
}

.function-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.function-card:hover .function-icon {
  background: #e8f4fd;
  transform: scale(1.05);
}

/* 摄像头弹窗样式 */
.camera-container {
  text-align: center;
}

.camera-preview {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  background: #000;
  position: relative;
}

.camera-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  background: #f5f7fa;
  border-radius: 8px;
}

.camera-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #909399;
  background: #f5f7fa;
  border-radius: 8px;
}

.photo-preview {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
  border: 1px solid #e4e7f0;
}

.camera-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 20px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 10px;
}

.action-buttons .el-button {
  min-width: 120px;
}

.action-buttons .el-button .el-icon {
  margin-right: 5px;
}

.function-card h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.function-card p {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #666;
  line-height: 1.4;
  flex-grow: 1;
}

.function-status {
  margin-top: auto;
}

.reminders-section {
  background: white;
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.reminders-section h3 {
  margin: 0 0 20px 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.reminders-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.reminder-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 15px;
  border-left: 4px solid #409eff;
}

.reminder-icon {
  flex-shrink: 0;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.reminder-content {
  flex: 1;
}

.reminder-content h4 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.reminder-content p {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #666;
  line-height: 1.4;
}

.reminder-content .time {
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
}

/* 其他功能弹窗样式 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  max-height: 400px;
  overflow-y: auto;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: #e9ecef;
  transform: translateY(-2px);
}

.feature-icon {
  flex-shrink: 0;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.feature-info {
  flex: 1;
}

.feature-info h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.feature-info p {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

.feature-info .el-tag {
  font-size: 12px;
}

/* 老人端响应式优化 */
@media (max-width: 768px) {
  .home-container {
    padding: 15px;
  }
  
  .elder-info-card {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .functions-scrollable {
    padding-bottom: 15px;
  }
  
  .functions-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
    padding: 0 5px;
  }
  
  .function-card {
    padding: 20px 15px;
    min-width: 140px;
    min-height: 160px;
  }
  
  .function-icon {
    width: 60px;
    height: 60px;
    margin: 0 auto 10px;
  }
  
  .function-card h3 {
    font-size: 18px;
  }
  
  .function-card p {
    font-size: 14px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .reminders-section {
    padding: 20px;
  }
  
  .reminder-item {
    padding: 15px;
  }
}
</style>
