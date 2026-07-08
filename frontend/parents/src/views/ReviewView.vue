<template>
  <div class="review-container">
    <div class="header">
      <h2>服务评价</h2>
      <p>您的反馈对我们很重要</p>
    </div>
    
    <!-- 评价状态 -->
    <div class="review-status" v-if="currentReview">
      <el-card>
        <div class="status-info">
          <h3>{{ currentReview.reviewMonth }} 月度评价</h3>
          <el-tag :type="getStatusType(currentReview.status)">
            {{ getStatusText(currentReview.status) }}
          </el-tag>
        </div>
        
        <!-- 待评价状态 -->
        <div v-if="currentReview.status === 'PUSHED'" class="pending-review">
          <div class="review-progress">
            <div class="progress-dots">
              <div 
                v-for="(question, index) in reviewQuestions" 
                :key="index"
                :class="['progress-dot', { active: currentQuestionIndex === index, completed: reviewForm.scores[index] > 0 }]"
                @click="scrollToQuestion(index)"
              ></div>
            </div>
            <div class="progress-text">
              {{ currentQuestionIndex + 1 }} / {{ reviewQuestions.length }}
            </div>
          </div>

          <div class="questions-swiper" ref="questionsSwiperRef" @scroll="handleScroll">
            <div class="questions-container">
              <div class="question-page" v-for="(question, index) in reviewQuestions" :key="index">
                <div class="question-header">
                  <h3>问题 {{ index + 1 }}</h3>
                  <div class="question-progress">{{ index + 1 }} / {{ reviewQuestions.length }}</div>
                </div>
                
                <div class="question-content">
                  <div class="question-text">{{ question }}</div>
                  
                  <div class="score-selector">
                    <div class="score-label">请评分（1-10分）</div>
                    <div class="score-buttons">
                      <el-button 
                        v-for="score in 10" 
                        :key="score"
                        :class="getScoreButtonClass(score, reviewForm.scores[index])"
                        @click="selectScore(index, score)"
                        size="large"
                      >
                        {{ score }}
                      </el-button>
                    </div>
                    
                    <div class="selected-score" v-if="reviewForm.scores[index] > 0">
                      <span class="score-label">已选择：</span>
                      <span :class="getScoreClass(reviewForm.scores[index])">{{ reviewForm.scores[index] }}分</span>
                    </div>
                  </div>
                </div>
                
                <div class="question-actions">
                  <el-button 
                    v-if="index < reviewQuestions.length - 1"
                    type="primary" 
                    size="large"
                    @click="nextQuestion"
                    :disabled="reviewForm.scores[index] === 0"
                  >
                    下一题
                  </el-button>
                  <el-button 
                    v-else
                    type="success" 
                    size="large"
                    @click="submitReview"
                    :loading="submitting"
                    :disabled="!allQuestionsAnswered"
                  >
                    提交评价
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 已完成状态 -->
        <div v-else-if="currentReview.status === 'COMPLETED'" class="completed-review">
          <div class="review-summary">
            <h4>您的评价已提交</h4>
            <div class="score-display">
              <div class="score-item" v-for="(question, index) in reviewQuestions" :key="index">
                <span class="question-text">{{ question }}：</span>
                <span class="score-value">{{ getReviewScore(index) }}分</span>
              </div>
            </div>
            <div class="completion-time">
              <p>提交时间：{{ formatDateTime(currentReview.completedAt) }}</p>
            </div>
            
            <div class="review-comment" v-if="currentReview.comment">
              <h4>评价意见</h4>
              <div class="comment-content">
                {{ currentReview.comment }}
              </div>
            </div>
          </div>
          
          <div class="review-actions">
          </div>
        </div>
        
        <!-- 未推送状态 -->
        <div v-else class="no-review">
          <el-empty description="本月评价任务尚未推送">
            <template #image>
              <el-icon size="64" color="#909399"><Clock /></el-icon>
            </template>
            <p>请等待管理员推送评价任务</p>
          </el-empty>
        </div>
      </el-card>
    </div>
    
    <!-- 无评价任务 -->
    <div v-else class="no-review">
      <el-empty description="暂无评价任务">
        <template #image>
          <el-icon size="64" color="#909399"><Document /></el-icon>
        </template>
        <p>本月暂无评价任务</p>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, Document } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { elderApi } from '@/api'

const router = useRouter()
const userStore = useUserStore()

const currentReview = ref(null)
const submitting = ref(false)
const currentQuestionIndex = ref(0)
const questionsSwiperRef = ref()

const reviewForm = ref({
  scores: [0, 0, 0, 0, 0] // 5个问题的评分
})

const reviewQuestions = [
  '您对社区工作人员的服务态度是否满意？',
  '您对问题响应速度是否满意？',
  '您对服务质量是否满意？',
  '您对问题解决能力是否满意？',
  '您对整体服务是否满意？'
]

const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'info',
    'PUSHED': 'warning',
    'COMPLETED': 'success'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待推送',
    'PUSHED': '待评价',
    'COMPLETED': '已完成'
  }
  return textMap[status] || '未知'
}

const getScoreClass = (score) => {
  if (score <= 3) return 'score-low'
  if (score <= 7) return 'score-medium'
  return 'score-high'
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const loadCurrentReview = async () => {
  try {
    // 获取当前月份的评价任务
    const currentMonth = new Date().toISOString().slice(0, 7) // YYYY-MM
    const response = await elderApi.getReviews(userStore.elderInfo?.id)
    const reviews = response || []
    
    // 查找当前月份的评价
    currentReview.value = reviews.find(r => r.reviewMonth === currentMonth && r.type === 'ELDER')
    
    if (currentReview.value && currentReview.value.status === 'COMPLETED') {
      // 如果已完成，加载已提交的评分
      reviewForm.value.scores = currentReview.value.scores || [0, 0, 0, 0, 0]
    }
  } catch (error) {
    console.error('加载评价任务失败:', error)
  }
}

const submitReview = async () => {
  // 检查是否所有问题都已评分
  const unansweredQuestions = reviewForm.value.scores.filter(score => score === 0)
  if (unansweredQuestions.length > 0) {
    ElMessage.warning('请为所有问题评分后再提交')
    return
  }
  
  try {
    submitting.value = true
    
    const reviewData = {
      reviewId: currentReview.value.id,
      scores: reviewForm.value.scores,
      averageScore: reviewForm.value.scores.reduce((sum, score) => sum + score, 0) / 5
    }
    
    // 调用API提交评价
    await elderApi.submitReview(reviewData)
    
    ElMessage.success('评价提交成功')
    
    // 重新加载评价状态
    await loadCurrentReview()
  } catch (error) {
    console.error('提交评价失败:', error)
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 滑动相关方法
const scrollToQuestion = (index) => {
  currentQuestionIndex.value = index
  const swiper = questionsSwiperRef.value
  if (swiper) {
    const pageWidth = swiper.clientWidth
    swiper.scrollTo({
      left: pageWidth * index,
      behavior: 'smooth'
    })
  }
}

const nextQuestion = () => {
  if (currentQuestionIndex.value < reviewQuestions.length - 1) {
    scrollToQuestion(currentQuestionIndex.value + 1)
  }
}

const handleScroll = () => {
  const swiper = questionsSwiperRef.value
  if (swiper) {
    const scrollLeft = swiper.scrollLeft
    const pageWidth = swiper.clientWidth
    const newIndex = Math.round(scrollLeft / pageWidth)
    currentQuestionIndex.value = Math.min(newIndex, reviewQuestions.length - 1)
  }
}

const selectScore = (questionIndex, score) => {
  reviewForm.value.scores[questionIndex] = score
}

const getScoreButtonClass = (score, selectedScore) => {
  if (selectedScore === score) {
    if (score <= 3) return 'score-low'
    if (score <= 7) return 'score-medium'
    return 'score-high'
  }
  return 'score-default'
}

const allQuestionsAnswered = computed(() => {
  return reviewForm.value.scores.every(score => score > 0)
})

// 获取评价分数
const getReviewScore = (index) => {
  if (!currentReview.value) return '-'
  
  const review = currentReview.value
  switch (index) {
    case 0: return review.attitude || '-'
    case 1: return review.response || '-'
    case 2: return review.quality || '-'
    case 3: return review.satisfaction || '-'
    case 4: return review.score || '-'
    default: return '-'
  }
}

onMounted(() => {
  loadCurrentReview()
})
</script>

<style scoped>
.review-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h2 {
  color: #333;
  margin-bottom: 10px;
}

.header p {
  color: #666;
  font-size: 16px;
}

.review-status {
  margin-bottom: 20px;
}

.status-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.status-info h3 {
  margin: 0;
  color: #333;
}

.pending-review {
  position: relative;
  height: 600px;
}

.review-progress {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;
  background: rgba(255, 255, 255, 0.9);
  padding: 10px 15px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.progress-dots {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.progress-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ddd;
  cursor: pointer;
  transition: all 0.3s ease;
}

.progress-dot.active {
  background: #409eff;
  transform: scale(1.2);
}

.progress-dot.completed {
  background: #67c23a;
}

.progress-text {
  font-size: 12px;
  color: #666;
  text-align: center;
}

.questions-swiper {
  height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  scroll-snap-type: x mandatory;
  scroll-behavior: smooth;
}

.questions-container {
  display: flex;
  height: 100%;
}

.question-page {
  min-width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
  box-sizing: border-box;
  scroll-snap-align: start;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.question-header h3 {
  margin: 0;
  color: #333;
  font-size: 20px;
  font-weight: 600;
}

.question-progress {
  font-size: 14px;
  color: #666;
  background: #f0f0f0;
  padding: 4px 12px;
  border-radius: 12px;
}

.question-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.question-text {
  font-size: 18px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 40px;
  font-weight: 500;
}

.score-selector {
  width: 100%;
  max-width: 400px;
}

.score-label {
  font-size: 16px;
  color: #666;
  margin-bottom: 20px;
}

.score-buttons {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
  margin-bottom: 20px;
}

.score-buttons .el-button {
  font-size: 16px;
  font-weight: 600;
  height: 50px;
  border-radius: 8px;
}

.score-default {
  background: #f5f7fa;
  border-color: #dcdfe6;
  color: #606266;
}

.score-default:hover {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}

.score-low {
  background: #fef0f0;
  border-color: #f56c6c;
  color: #f56c6c;
}

.score-medium {
  background: #fdf6ec;
  border-color: #e6a23c;
  color: #e6a23c;
}

.score-high {
  background: #f0f9ff;
  border-color: #67c23a;
  color: #67c23a;
}

.selected-score {
  margin-top: 15px;
  font-size: 18px;
  font-weight: 600;
}

.selected-score .score-label {
  color: #666;
  margin-right: 8px;
}

.question-actions {
  margin-top: 40px;
  text-align: center;
}

.question-actions .el-button {
  min-width: 120px;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .pending-review {
    height: 500px;
  }
  
  .review-progress {
    top: 10px;
    right: 10px;
    padding: 8px 12px;
  }
  
  .question-header h3 {
    font-size: 18px;
  }
  
  .question-text {
    font-size: 16px;
    margin-bottom: 30px;
  }
  
  .score-buttons {
    grid-template-columns: repeat(5, 1fr);
    gap: 8px;
  }
  
  .score-buttons .el-button {
    height: 45px;
    font-size: 14px;
  }
  
  .question-actions .el-button {
    min-width: 100px;
    height: 45px;
    font-size: 14px;
  }
}

.score-low :deep(.el-radio-button__inner) {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: white;
}

.score-medium :deep(.el-radio-button__inner) {
  background-color: #e6a23c;
  border-color: #e6a23c;
  color: white;
}

.score-high :deep(.el-radio-button__inner) {
  background-color: #67c23a;
  border-color: #67c23a;
  color: white;
}

.review-actions {
  text-align: center;
  margin-top: 30px;
}

.review-actions .el-button {
  margin: 0 10px;
}

.completed-review {
  padding: 20px;
  text-align: center;
}

.review-summary h4 {
  color: #67c23a;
  margin-bottom: 20px;
}

.score-display {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.score-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #e4e7ed;
}

.score-item:last-child {
  border-bottom: none;
}

.question-text {
  color: #666;
}

.score-value {
  font-weight: bold;
  color: #409eff;
  font-size: 18px;
}

.completion-time {
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.review-comment {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.review-comment h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 16px;
}

.comment-content {
  white-space: pre-line;
  line-height: 1.6;
  color: #666;
}

.no-review {
  text-align: center;
  padding: 40px 0;
}

.features-list {
  display: grid;
  gap: 15px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.feature-item span {
  flex: 1;
  font-weight: 500;
}
</style>
