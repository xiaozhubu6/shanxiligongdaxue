<template>
  <div class="service-review">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>服务评价管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="pushElderReview" :loading="pushingElder">
              <el-icon><User /></el-icon>
              推送老人评价
            </el-button>
            <el-button type="success" @click="pushChildReview" :loading="pushingChild">
              <el-icon><UserFilled /></el-icon>
              推送子女评价
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 统计信息 -->
      <el-row :gutter="20" style="margin-bottom: 30px;">
        <el-col :span="8">
          <el-statistic title="本月评价推送人数" :value="reviewStats.pushedCount" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="未完成人数" :value="reviewStats.incompleteCount" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="完成率" :value="reviewStats.completionRate" suffix="%" />
        </el-col>
      </el-row>

      <!-- 评价结果图表 -->
      <div class="charts-section">
        <div class="chart-container">
          <div ref="scoreChartRef" style="height: 400px;"></div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, UserFilled } from '@element-plus/icons-vue'
import { reviewApi } from '@/api'
import * as echarts from 'echarts'

// 响应式数据
const pushingElder = ref(false)
const pushingChild = ref(false)
const scoreChartRef = ref()
let scoreChart = null

// 统计数据
const reviewStats = ref({
  pushedCount: 0,
  incompleteCount: 0,
  completionRate: 0
})

// 推送老人评价
const pushElderReview = async () => {
  try {
    await ElMessageBox.confirm(
      '确定推送老人评价任务吗？推送后所有老人将收到评价通知。',
      '推送确认',
      {
        type: 'warning',
        confirmButtonText: '确定推送',
        cancelButtonText: '取消'
      }
    )
    
    pushingElder.value = true
    const currentMonth = new Date().toISOString().slice(0, 7) // YYYY-MM
    
    const response = await reviewApi.pushElderTasks({ reviewMonth: currentMonth })
    
    if (response.includes('0 个')) {
      ElMessage.warning(`${currentMonth}月的老人评价任务已存在，无需重复推送`)
    } else {
      ElMessage.success(response)
    }
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('推送老人评价任务失败:', error)
      ElMessage.error('推送失败，请重试')
    }
  } finally {
    pushingElder.value = false
  }
}

// 推送子女评价
const pushChildReview = async () => {
  try {
    await ElMessageBox.confirm(
      '确定推送子女评价任务吗？推送后所有签约子女将收到评价通知。',
      '推送确认',
      {
        type: 'warning',
        confirmButtonText: '确定推送',
        cancelButtonText: '取消'
      }
    )
    
    pushingChild.value = true
    const currentMonth = new Date().toISOString().slice(0, 7) // YYYY-MM
    
    const response = await reviewApi.pushChildTasks({ reviewMonth: currentMonth })
    
    if (response.includes('0 个')) {
      ElMessage.warning(`${currentMonth}月的子女评价任务已存在，无需重复推送`)
    } else {
      ElMessage.success(response)
    }
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('推送子女评价任务失败:', error)
      ElMessage.error('推送失败，请重试')
    }
  } finally {
    pushingChild.value = false
  }
}

// 加载数据
const loadData = async () => {
  try {
    // 加载统计数据
    await loadStatistics()
    
    // 加载图表数据
    await loadChartData()
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    // 获取所有已推送的评价任务
    const response = await reviewApi.getReviewTasks({ status: 'PUSHED' })
    
    if (response) {
      const pushedCount = response.length
      const completedCount = response.filter(r => r.status === 'COMPLETED').length
      const incompleteCount = pushedCount - completedCount
      
      reviewStats.value = {
        pushedCount,
        incompleteCount,
        completionRate: pushedCount > 0 ? Math.round((completedCount / pushedCount) * 100) : 0
      }
      
      console.log('统计数据:', reviewStats.value)
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载图表数据
const loadChartData = async () => {
  try {
    // 加载评分分布数据
    const scoreDistribution = await loadScoreDistribution()
    
    // 等待DOM更新后初始化图表
    await nextTick()
    initChart(scoreDistribution)
  } catch (error) {
    console.error('加载图表数据失败:', error)
  }
}

// 加载评分分布
const loadScoreDistribution = async () => {
  try {
    // 获取所有已完成的评价，不限制月份
    const response = await reviewApi.getReviewTasks({ status: 'COMPLETED' })
    
    if (response) {
      console.log('获取到的评价数据:', response)
      // 直接返回评价任务数据，让图表处理统计
      return response
    }
    
    return []
  } catch (error) {
    console.error('加载评分分布失败:', error)
    return []
  }
}

// 获取评价分数
const getReviewScore = (review, index) => {
  switch (index) {
    case 0: return review.attitude || 0
    case 1: return review.response || 0
    case 2: return review.quality || 0
    case 3: return review.satisfaction || 0
    case 4: return review.score || 0
    default: return 0
  }
}

// 初始化图表
const initChart = (scoreData) => {
  if (scoreChartRef.value) {
    scoreChart = echarts.init(scoreChartRef.value)
    
    // 统计每个问题的评分分布
    const questionNames = ['服务态度', '响应速度', '服务质量', '问题解决能力', '整体满意度']
    const series = []
    
    // 为每个分数创建一个系列
    for (let score = 1; score <= 10; score++) {
      const data = questionNames.map((questionName, questionIndex) => {
        let count = 0
        scoreData.forEach(review => {
          const reviewScore = getReviewScore(review, questionIndex)
          if (reviewScore === score) {
            count++
          }
        })
        return count
      })
      
      series.push({
        name: `${score}分`,
        type: 'bar',
        stack: 'total',
        emphasis: {
          focus: 'series'
        },
        data: data
      })
    }
    
    const option = {
      title: {
        text: '各问题评分分布',
        left: 'center',
        top: 10,
        padding: [0, 0, 20, 0]
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: function(params) {
          let result = params[0].axisValue + '<br/>'
          params.forEach(param => {
            if (param.value > 0) {
              result += param.seriesName + ': ' + param.value + '人<br/>'
            }
          })
          return result
        }
      },
      legend: {
        data: ['1分', '2分', '3分', '4分', '5分', '6分', '7分', '8分', '9分', '10分'],
        top: 30
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: questionNames
      },
      yAxis: {
        type: 'value',
        name: '人数'
      },
      series: series
    }
    
    scoreChart.setOption(option)
  }
}

// 生命周期
onMounted(() => {
  loadData()
  
  // 监听窗口大小变化，重新渲染图表
  window.addEventListener('resize', () => {
    if (scoreChart) scoreChart.resize()
  })
})
</script>

<style scoped>
.service-review {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.charts-section {
  margin-top: 50px;
  margin-bottom: 30px;
}

.chart-container {
  background: white;
  border-radius: 8px;
  padding: 20px;
  padding-top: 40px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {
  .service-review {
    padding: 15px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .chart-container {
    margin-bottom: 20px;
  }
}
</style>
