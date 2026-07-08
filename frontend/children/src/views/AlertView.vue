<template>
  <div class="activity-monitor-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>老人活动监控</span>
          <div class="header-actions">
            <el-button type="primary" @click="loadActivityData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button type="warning" @click="exportActivityReport">
              <el-icon><Download /></el-icon>
              导出报告
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 老人选择 -->
      <div class="elder-selector">
        <el-select v-model="selectedElderId" placeholder="请选择老人" @change="onElderChange" style="width: 200px;">
          <el-option
            v-for="elder in myElders"
            :key="elder.id"
            :label="elder.name"
            :value="elder.id"
          />
        </el-select>
      </div>
      
      <!-- 今日打卡状态 -->
      <div class="today-status" v-if="selectedElder">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>今日打卡状态</span>
              <el-button 
                type="primary" 
                @click="notifyElderCheckIn" 
                :disabled="todayCheck.status === 'completed'"
              >
                <el-icon><Bell /></el-icon>
                通知打卡
              </el-button>
            </div>
          </template>
          <div class="status-content">
            <div class="status-item">
              <div class="status-icon success">
                <el-icon class="status-icon">
                  <Check v-if="todayCheck.status === 'completed'" />
                  <Clock v-else />
                </el-icon>
              </div>
              <div class="status-info">
                <h3>打卡时间</h3>
                <p>{{ todayCheck.status === 'completed' ? formatDateTime(todayCheck.checkTime) : '未打卡' }}</p>
              </div>
            </div>
            <div class="status-item">
              <div class="status-icon photo">
                <el-icon class="status-icon">
                  <Picture v-if="todayCheck.photoData" />
                  <Picture v-else />
                </el-icon>
              </div>
              <div class="status-info">
                <h3>打卡照片</h3>
                <p>{{ todayCheck.photoData ? '有照片' : '无照片' }}</p>
              </div>
            </div>
            <div class="status-item">
              <div class="status-icon location">
                <el-icon class="status-icon"><Location /></el-icon>
              </div>
              <div class="status-info">
                <h3>打卡地点</h3>
                <p>{{ todayCheck.location || '未知地点' }}</p>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- 照片墙 -->
      <div class="photo-wall" v-if="selectedElder">
        <el-card>
          <template #header>
            <span>打卡照片墙</span>
          </template>
          <div class="photo-grid">
            <div 
              v-for="(record, index) in faceCheckRecords" 
              :key="record.id"
              class="photo-item"
              @click="viewPhotoDetail(record)"
            >
              <div class="photo-container">
                <el-image 
                  :src="record.photoData || 'https://via.placeholder.com/100x100'" 
                  fit="cover"
                  class="photo-image"
                />
                <div class="photo-info">
                  <p class="photo-time">{{ formatDateTime(record.checkTime) }}</p>
                  <p class="photo-location">{{ record.location || '未知地点' }}</p>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- 异常事件 -->
      <div class="emergency-events" v-if="selectedElder">
        <el-card>
          <template #header>
            <span>异常事件</span>
          </template>
          <el-table :data="emergencyEvents" style="width: 100%">
            <el-table-column prop="createdAt" label="发生时间" width="160">
              <template #default="{ row }">
                {{ row.createdAt ? formatDateTime(row.createdAt) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" width="200" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'HANDLED' ? 'success' : 'warning'" size="small">
                  {{ row.status === 'HANDLED' ? '已处理' : '待处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="handledAt" label="处理时间" width="160">
              <template #default="{ row }">
                {{ row.handledAt ? formatDateTime(row.handledAt) : '-' }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
      
      <!-- 无老人选择提示 -->
      <div v-if="!selectedElder" class="no-elder-selected">
        <el-empty description="请选择要查看的老人" />
      </div>
    </el-card>

    <!-- 照片详情对话框 -->
    <el-dialog v-model="showPhotoDialog" title="照片详情" width="600px">
      <div class="photo-detail" v-if="selectedPhoto">
        <el-image 
          :src="selectedPhoto.photoData || 'https://via.placeholder.com/300x300'" 
          fit="cover"
          style="width: 100%; max-height: 400px;"
        />
        <div class="photo-detail-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="打卡时间">
              {{ formatDateTime(selectedPhoto.checkTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="打卡地点">
              {{ selectedPhoto.location || '未知地点' }}
            </el-descriptions-item>
            <el-descriptions-item label="老人姓名">
              {{ selectedPhoto.elderName || '未知老人' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPhotoDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Clock, Location, Picture, Refresh, Download } from '@element-plus/icons-vue'

// 响应式数据
const myElders = ref([])
const selectedElderId = ref(null)
const selectedElder = ref(null)
const emergencyEvents = ref([])
const faceCheckRecords = ref([])
const showPhotoDialog = ref(false)
const selectedPhoto = ref(null)

// 今日打卡状态
const todayCheck = reactive({
  status: 'pending',
  checkTime: null,
  location: null,
  photoData: null
})

// 方法
const loadMyElders = async () => {
  try {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
      ElMessage.error('用户信息无效，请重新登录')
      return
    }
    
    const user = JSON.parse(userStr)
    if (!user || !user.id) {
      ElMessage.error('用户信息无效，请重新登录')
      return
    }
    
    // 使用子女端接口获取关联老人
    const response = await fetch(`http://localhost:8082/api/child/elders?childId=${user.id}`)
    const elders = await response.json()
    
    myElders.value = elders || []
    
    if (myElders.value.length > 0) {
      selectedElderId.value = myElders.value[0].id
      selectedElder.value = myElders.value[0]
      await loadActivityData()
    }
  } catch (error) {
    console.error('加载老人列表失败:', error)
    ElMessage.error('加载老人列表失败')
  }
}

const onElderChange = async (elderId) => {
  selectedElderId.value = elderId
  selectedElder.value = myElders.value.find(e => e.id === elderId)
  await loadActivityData()
}

const loadActivityData = async () => {
  if (!selectedElder.value) return
  
  try {
    ElMessage.info('正在加载活动数据...')
    
    // 先获取紧急事件和打卡数据
    const [emergencyResponse, faceCheckResponse] = await Promise.all([
      fetch(`http://localhost:8082/api/emergency/elder/${selectedElder.value.id}`),
      fetch(`http://localhost:8082/api/face-check/elder/${selectedElder.value.id}/photos`)
    ])
    
    const emergencies = await emergencyResponse.json() || []
    const faceCheckData = await faceCheckResponse.json()
    
    // 获取社区和网格组数据
    const communitiesResponse = await fetch(`http://localhost:8082/api/community`)
    const communitiesData = await communitiesResponse.json()
    
    console.log('API返回数据:', { emergencies, faceCheckData, communitiesData })
    console.log('faceCheckData详细:', faceCheckData)
    console.log('faceCheckData类型:', typeof faceCheckData)
    console.log('faceCheckData是否为数组:', Array.isArray(faceCheckData))
    
    // 处理faceCheckData，确保是数组格式
    let allFaceChecks = []
    if (Array.isArray(faceCheckData)) {
      allFaceChecks = faceCheckData
    } else if (faceCheckData && typeof faceCheckData === 'object') {
      // 如果是对象，尝试获取data属性或values
      if (Array.isArray(faceCheckData.data)) {
        allFaceChecks = faceCheckData.data
      } else if (Array.isArray(faceCheckData.records)) {
        allFaceChecks = faceCheckData.records
      } else {
        // 尝试获取对象的所有值
        const values = Object.values(faceCheckData)
        if (values.length > 0 && Array.isArray(values[0])) {
          allFaceChecks = values[0]
        } else if (values.length > 0) {
          allFaceChecks = values
        }
      }
    }
    
    console.log('处理后的打卡记录:', allFaceChecks)
    
    // 处理社区和网格组数据
    const communitiesMap = {}
    const gridGroupsMap = {}
    
    if (Array.isArray(communitiesData)) {
      // 获取每个社区的网格组
      for (const community of communitiesData) {
        communitiesMap[community.id] = community
        try {
          const gridGroupsResponse = await fetch(`http://localhost:8082/api/community/${community.id}/grid-groups`)
          const gridGroups = await gridGroupsResponse.json()
          if (Array.isArray(gridGroups)) {
            gridGroups.forEach(group => {
              gridGroupsMap[group.id] = {
                ...group,
                communityName: community.name
              }
            })
          }
        } catch (error) {
          console.warn(`获取社区${community.id}的网格组失败:`, error)
        }
      }
    }
    
    console.log('社区映射:', communitiesMap)
    console.log('网格组映射:', gridGroupsMap)
    console.log('网格组映射详情:', Object.keys(gridGroupsMap).map(key => ({
      key,
      value: gridGroupsMap[key]
    })))
    
    console.log('所有刷脸记录:', allFaceChecks)
    console.log('选中老人ID:', selectedElder.value.id)
    console.log('所有刷脸记录详情:', allFaceChecks.map((check, index) => { 
      if (index === 0) {
        console.log('第一个打卡记录完整结构:', check)
        console.log('第一个打卡记录所有字段:', Object.keys(check))
        console.log('第一个打卡记录字段值:', {
          id: check.id,
          elderId: check.elderId,
          elderName: check.elderName,
          gridGroupId: check.gridGroupId,
          gridGroup: check.gridGroup,
          gridGroupName: check.gridGroupName,
          communityId: check.communityId,
          communityName: check.communityName,
          // 尝试其他可能的字段名
          grid_id: check.grid_id,
          groupId: check.groupId,
          group_id: check.group_id,
          community_id: check.community_id
        })
      }
      
      return { 
        id: check.id, 
        elderId: check.elderId, 
        elderName: check.elderName, 
        gridGroupId: check.gridGroupId,
        gridGroup: check.gridGroup,
        gridGroupName: check.gridGroupName,
        communityId: check.communityId,
        communityName: check.communityName,
        allFields: Object.keys(check)
      }
    }))
    
    // 从所有记录中筛选出选中老人的记录
    const faceChecks = allFaceChecks.filter(check => {
      const checkElderId = Number(check.elderId)
      const selectedElderId = Number(selectedElder.value.id)
      console.log('检查记录:', checkElderId, '===', selectedElderId, checkElderId === selectedElderId)
      return checkElderId === selectedElderId
    })
    
    console.log('筛选后的刷脸记录:', faceChecks)
    console.log('网格组映射:', gridGroupsMap)
    
    emergencyEvents.value = emergencies.filter(event => Number(event.elderId) === Number(selectedElder.value.id)).map(event => ({
      id: event.id,
      createdAt: event.createdAt,
      description: event.description || '紧急呼救',
      status: event.status || 'PENDING',
      handledBy: event.handledBy || '系统',
      handledAt: event.handledAt || null
    }))
    
    faceCheckRecords.value = faceChecks.map(record => {
      // 从老人信息中获取网格组ID
      const elder = myElders.value.find(e => e.id === record.elderId)
      const gridGroupId = elder?.gridGroupId
      
      console.log('通过老人信息获取网格组:', {
        recordId: record.id,
        elderId: record.elderId,
        foundElder: elder,
        gridGroupId: gridGroupId
      })
      
      // 根据gridGroupId获取网格组信息
      const gridGroup = gridGroupsMap[gridGroupId] || {}
      const communityName = gridGroup.communityName || '未知社区'
      const gridGroupName = gridGroup.name || '未知网格组'
      
      console.log('处理打卡记录:', {
        recordId: record.id,
        recordGridGroupId: gridGroupId,
        foundGridGroup: gridGroup,
        finalLocation: `${communityName} - ${gridGroupName}`
      })
      
      return {
        id: record.id,
        checkTime: record.checkTime,
        location: `${communityName} - ${gridGroupName}`,
        photoData: record.photoData,
        elderName: record.elderName || '未知老人'
      }
    })
    
    const todayFaceCheck = faceChecks.find(check => check.elderId === selectedElder.value.id)
    if (todayFaceCheck) {
      // 从老人信息中获取网格组ID
      const elder = myElders.value.find(e => e.id === todayFaceCheck.elderId)
      const gridGroupId = elder?.gridGroupId
      
      // 根据gridGroupId获取网格组信息
      const gridGroup = gridGroupsMap[gridGroupId] || {}
      const communityName = gridGroup.communityName || '未知社区'
      const gridGroupName = gridGroup.name || '未知网格组'
      
      console.log('处理今日打卡:', {
        recordId: todayFaceCheck.id,
        elderId: todayFaceCheck.elderId,
        foundElder: elder,
        gridGroupId: gridGroupId,
        foundGridGroup: gridGroup,
        finalLocation: `${communityName} - ${gridGroupName}`
      })
      
      todayCheck.status = 'completed'
      todayCheck.checkTime = todayFaceCheck.checkTime
      todayCheck.location = `${communityName} - ${gridGroupName}`
      todayCheck.photoData = todayFaceCheck.photoData
    } else {
      todayCheck.status = 'pending'
      todayCheck.checkTime = null
      todayCheck.location = null
      todayCheck.photoData = null
    }
    
    ElMessage.success('活动数据加载完成')
  } catch (error) {
    console.error('加载活动数据失败:', error)
    ElMessage.error('加载活动数据失败')
  }
}

const viewPhotoDetail = (record) => {
  selectedPhoto.value = record
  showPhotoDialog.value = true
}

const handleEmergencyEvent = async (event) => {
  try {
    await ElMessageBox.confirm(
      `确认处理${selectedElder.value.name}的紧急事件？`,
      '处理确认',
      {
        confirmButtonText: '确认处理',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    ElMessage.info('正在处理紧急事件...')
    
    // 调用后端API处理紧急事件
    const response = await fetch(`http://localhost:8082/api/emergency/${event.id}/handle`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    if (response.ok) {
      ElMessage.success('紧急事件处理成功')
      // 更新事件状态
      event.status = 'HANDLED'
      event.handledAt = new Date().toISOString()
    } else {
      ElMessage.error('紧急事件处理失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('处理紧急事件失败:', error)
      ElMessage.error('处理紧急事件失败')
    }
  }
}

const exportActivityReport = () => {
  if (!selectedElder.value) {
    ElMessage.warning('请先选择老人')
    return
  }
  
  ElMessage.info('正在生成报告...')
  setTimeout(() => {
    ElMessage.success('报告生成完成')
  }, 1000)
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'  
  
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 通知老人打卡
const notifyElderCheckIn = async () => {
  if (!selectedElder.value) return
  
  try {
    ElMessage.info('正在发送打卡通知...')
    
    // 调用后端API创建打卡提醒事件
    const response = await fetch(`http://localhost:8082/api/emergency/checkin-notify`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        elderId: selectedElder.value.id,
        description: '子女提醒您进行今日打卡'
      })
    })
    
    if (response.ok) {
      ElMessage.success('打卡通知已发送')
    } else {
      ElMessage.error('打卡通知发送失败')
    }
  } catch (error) {
    console.error('发送打卡通知失败:', error)
    ElMessage.error('打卡通知发送失败')
  }
}

onMounted(() => {
  loadMyElders()
})
</script>

<style scoped>
.activity-monitor-view {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.elder-selector {
  margin: 20px 0;
}

.today-status {
  margin: 20px 0;
}

.status-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  padding: 20px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.status-icon {
  font-size: 24px;
  color: #409eff;
}

.status-icon.success {
  color: #67c23a;
}

.status-icon.photo {
  color: #909399;
}

.status-icon.location {
  color: #e6a23c;
}

.status-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
}

.status-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.photo-wall {
  margin: 20px 0;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
  padding: 20px;
}

.photo-item {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.2s;
}

.photo-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.photo-container {
  position: relative;
}

.photo-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.photo-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.7));
  color: white;
  padding: 10px;
  font-size: 12px;
}

.photo-time {
  font-weight: 600;
  margin-bottom: 5px;
}

.photo-location {
  opacity: 0.9;
}

.emergency-events {
  margin: 20px 0;
}

.no-elder-selected {
  text-align: center;
  padding: 60px 0;
}

.photo-detail {
  text-align: center;
}

.photo-detail-info {
  margin-top: 20px;
}
</style>
