<template>
  <div class="emergency-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>紧急事件与老人管理</span>
          <div class="header-actions">
            <el-button :icon="Refresh" @click="loadEvents">刷新</el-button>
          </div>
        </div>
      </template>

      <div class="statistics-row">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="未处理事件" :value="statistics.unhandled" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="今日事件" :value="statistics.today" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="本周事件" :value="statistics.week" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="未刷脸异常" :value="statistics.uncheckedElders" />
          </el-col>
        </el-row>
      </div>

      <div class="event-content">
        <!-- 标签页 -->
        <el-tabs v-model="activeTab" class="event-tabs">
          <el-tab-pane label="老人管理" name="elders">
            <!-- 老人管理 -->
            <div class="elder-management">
              <!-- 老人列表 -->
              <div v-loading="loading" class="loading-container">
                <el-table :data="filteredElders" style="width: 100%" :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold' }">
                  <el-table-column prop="id" label="ID" min-width="60" />
                  <el-table-column prop="name" label="姓名" min-width="100" />
                  <el-table-column prop="age" label="年龄" min-width="60" />
                  <el-table-column prop="gender" label="性别" min-width="60" />
                  <el-table-column label="子女信息" min-width="150">
                    <template #default="{ row }">
                      <div v-if="row.children && row.children.length > 0">
                        <div v-for="(child, index) in row.children" :key="child.id" class="child-info">
                          {{ child.name }} ({{ child.phone }})
                        </div>
                      </div>
                      <span v-else class="no-children">暂无子女信息</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="phone" label="手机号" min-width="120" />
                  <el-table-column prop="createdAt" label="创建时间" min-width="140">
                    <template #default="{ row }">
                      {{ formatDateTime(row.createdAt) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" min-width="180">
                    <template #default="{ row }">
                      <el-button size="small" @click="viewElderDetail(row)">详情</el-button>
                      <el-button size="small" type="primary" @click="viewElderAllPhotos(row)">查看照片</el-button>
                      <el-button size="small" type="warning" @click="viewElderAbnormalEvents(row)">异常事件</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="全部事件" name="all">
            <!-- 筛选器 -->
            <div class="filter-section">
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-select v-model="statusFilter" placeholder="筛选状态" clearable>
                    <el-option label="全部" value="" />
                    <el-option label="未处理" value="pending" />
                    <el-option label="已处理" value="handled" />
                  </el-select>
                </el-col>
                <el-col :span="8">
                  <el-select v-model="eventTypeFilter" placeholder="筛选事件类型" clearable>
                    <el-option label="全部" value="" />
                    <el-option label="紧急呼叫" value="EMERGENCY_CALL" />
                    <el-option label="未刷脸异常" value="FACE_CHECK_EXCEPTION" />
                  </el-select>
                </el-col>
                <el-col :span="8">
                  <el-button @click="clearFilters">清除筛选</el-button>
                </el-col>
              </el-row>
            </div>

            <div v-loading="loading" class="loading-container">
              <el-table :data="filteredEvents" style="width: 100%" :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold' }">
                <el-table-column prop="elderName" label="老人姓名" min-width="100" />
                <el-table-column prop="eventType" label="事件类型" min-width="100">
                  <template #default="{ row }">
                    <el-tag :type="getEventTypeColor(row.eventType)">
                      {{ getEventTypeLabel(row.eventType) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" min-width="80">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 'handled' ? 'success' : 'warning'">
                      {{ row.status === 'handled' ? '已处理' : '未处理' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="发生时间" min-width="140">
                  <template #default="{ row }">
                    {{ formatDateTime(row.createdAt) }}
                  </template>
                </el-table-column>
                <el-table-column prop="handledAt" label="处理时间" min-width="140">
                  <template #default="{ row }">
                    {{ formatDateTime(row.handledAt) }}
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="描述" min-width="150" />
                <el-table-column label="操作" min-width="120">
                  <template #default="{ row }">
                    <el-button
                      v-if="row.status === 'pending'"
                      size="small"
                      type="primary"
                      @click="handleEvent(row)"
                    >
                      处理
                    </el-button>
                    <el-button
                      size="small"
                      @click="viewEvent(row)"
                    >
                      查看
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <el-tab-pane label="打卡记录" name="face_check">
            <!-- 今日打卡统计 -->
            <div class="check-stats" v-loading="loading">
              <el-row :gutter="20">
                <el-col :span="6">
                  <el-statistic title="老人总数" :value="todayCheckStats?.totalElders || 0" />
                </el-col>
                <el-col :span="6">
                  <el-statistic title="已打卡" :value="todayCheckStats?.checkedElders || 0" />
                </el-col>
                <el-col :span="6">
                  <el-statistic title="未打卡" :value="todayCheckStats?.uncheckedElders || 0" />
                </el-col>
                <el-col :span="6">
                  <el-button type="primary" @click="loadTodayCheckStats" :loading="loading">
                    <el-icon><Refresh /></el-icon>
                    刷新
                  </el-button>
                </el-col>
              </el-row>
            </div>

            <!-- 已打卡记录 -->
            <div class="section-container">
              <h3 class="section-title">
                <el-icon><Check /></el-icon>
                已打卡记录 ({{ todayCheckStats?.checkedRecords?.length || 0 }})
              </h3>
              <div v-loading="loading" class="loading-container">
                <el-table :data="todayCheckStats?.checkedRecords || []" style="width: 100%" :header-cell-style="{ background: '#f0f9ff', color: '#606266', fontWeight: 'bold' }">
                  <el-table-column prop="elderName" label="老人姓名" min-width="100" />
                  <el-table-column prop="communityName" label="社区" min-width="120" />
                  <el-table-column prop="unitNumber" label="单元号" min-width="80" />
                  <el-table-column prop="roomNumber" label="房间号" min-width="80" />
                  <el-table-column prop="checkTime" label="打卡时间" min-width="140">
                    <template #default="{ row }">
                      {{ formatDateTime(row.checkTime) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" min-width="80">
                    <template #default="{ row }">
                      <el-tag :type="getStatusType(row.status)">
                        {{ row.status }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="photoUrl" label="照片" min-width="80">
                    <template #default="{ row }">
                      <div v-if="row.photoUrl" class="photo-preview">
                        <el-image
                          :src="getFullPhotoUrl(row.photoUrl)"
                          style="width: 40px; height: 40px; border-radius: 4px;"
                          fit="cover"
                          :preview-src-list="[getFullPhotoUrl(row.photoUrl)]"
                          preview-teleported
                        />
                      </div>
                      <span v-else class="no-photo">-</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" min-width="120">
                    <template #default="{ row }">
                      <el-button
                        v-if="row.status === '需重检'"
                        size="small"
                        type="warning"
                        @click="handleRecheck(row)"
                      >
                        重新打卡
                      </el-button>
                      <el-button
                        size="small"
                        @click="viewFaceCheckDetail(row)"
                      >
                        详情
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>

            <!-- 未打卡人员 -->
            <div class="section-container">
              <h3 class="section-title">
                <el-icon><Warning /></el-icon>
                未打卡人员 ({{ todayCheckStats?.uncheckedRecords?.length || 0 }})
              </h3>
              <div v-loading="loading" class="loading-container">
                <el-table :data="todayCheckStats?.uncheckedRecords || []" style="width: 100%" :header-cell-style="{ background: '#fef2f2', color: '#606266', fontWeight: 'bold' }">
                  <el-table-column prop="elderName" label="老人姓名" min-width="100" />
                  <el-table-column prop="communityName" label="社区" min-width="120" />
                  <el-table-column prop="unitNumber" label="单元号" min-width="80" />
                  <el-table-column prop="roomNumber" label="房间号" min-width="80" />
                  <el-table-column prop="elderAge" label="年龄" min-width="60" />
                  <el-table-column prop="elderGender" label="性别" min-width="60" />
                  <el-table-column prop="status" label="状态" min-width="80">
                    <template #default="{ row }">
                      <el-tag :type="row.isAbnormal ? 'danger' : 'warning'">
                        {{ row.status }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" min-width="100">
                    <template #default="{ row }">
                      <el-button
                        size="small"
                        type="primary"
                        @click="handleNotifyElder(row)"
                      >
                        通知打卡
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>

    <!-- 老人详情对话框 -->
    <el-dialog v-model="showElderDetailDialog" title="老人详情" width="800px">
      <div v-if="selectedElder" class="elder-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ selectedElder.id }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ selectedElder.name }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ selectedElder.age }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ selectedElder.gender }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedElder.status === 'NORMAL' ? 'success' : 'danger'">
              {{ selectedElder.status === 'NORMAL' ? '正常' : '异常' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="网格群">{{ selectedElder.gridGroupName }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(selectedElder.createdAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showElderDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 事件详情对话框 -->
    <el-dialog v-model="showEventDetailDialog" title="事件详情" width="800px">
      <div v-if="selectedEvent" class="event-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="事件ID">{{ selectedEvent.id }}</el-descriptions-item>
          <el-descriptions-item label="老人姓名">{{ selectedEvent.elderName }}</el-descriptions-item>
          <el-descriptions-item label="事件类型">
            <el-tag :type="getEventTypeColor(selectedEvent.eventType)">
              {{ getEventTypeLabel(selectedEvent.eventType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedEvent.status === 'handled' ? 'success' : 'warning'">
              {{ selectedEvent.status === 'handled' ? '已处理' : '未处理' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发生时间">{{ formatDateTime(selectedEvent.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ formatDateTime(selectedEvent.handledAt) }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ selectedEvent.description || '无描述' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showEventDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 打卡记录详情对话框 -->
    <el-dialog v-model="showFaceCheckDetailDialog" title="打卡记录详情" width="800px">
      <div v-if="selectedFaceCheck" class="face-check-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="记录ID">{{ selectedFaceCheck.id }}</el-descriptions-item>
          <el-descriptions-item label="老人姓名">{{ selectedFaceCheck.elderName }}</el-descriptions-item>
          <el-descriptions-item label="刷脸时间">{{ formatDateTime(selectedFaceCheck.checkTime) }}</el-descriptions-item>
          <el-descriptions-item label="结果">
            <el-tag :type="selectedFaceCheck.result === 'success' ? 'success' : 'info'">
              {{ selectedFaceCheck.result === 'success' ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="照片">
            <div v-if="selectedFaceCheck.photoUrl" class="photo-preview">
              <el-image
                :src="selectedFaceCheck.photoUrl"
                style="width: 100%; max-height: 400px; border-radius: 8px;"
                fit="contain"
                :preview-src-list="[selectedFaceCheck.photoUrl]"
                preview-teleported
              />
            </div>
            <span v-else class="no-photo">无照片</span>
          </el-descriptions-item>
          <el-descriptions-item label="年龄">{{ selectedFaceCheck.elderAge }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ selectedFaceCheck.elderGender }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showFaceCheckDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 老人照片查看对话框 -->
    <el-dialog
      v-model="showElderPhotosDialog"
      :title="`${selectedElderForPhotos?.name || '老人'}的所有打卡照片`"
      width="90%"
      top="5vh"
    >
      <div v-if="selectedElderForPhotos" class="elder-photos-container">
        <div class="elder-info-header">
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="姓名">{{ selectedElderForPhotos.name }}</el-descriptions-item>
            <el-descriptions-item label="年龄">{{ selectedElderForPhotos.age }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ selectedElderForPhotos.gender }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-loading="photosLoading" class="photos-content">
          <div v-if="elderPhotos.length > 0" class="photos-timeline">
            <div
              v-for="(photo, index) in elderPhotos"
              :key="photo.id"
              class="photo-item"
            >
              <div class="photo-time">
                {{ formatDateTime(photo.checkTime) }}
                <el-tag
                  :type="photo.result === 'success' ? 'success' : 'warning'"
                  size="small"
                  style="margin-left: 8px;"
                >
                  {{ photo.result === 'success' ? '成功' : '需要重检' }}
                </el-tag>
              </div>
              <div class="photo-content">
                <div class="photo-image">
                  <el-image
                    :src="getFullPhotoUrl(photo.photoUrl)"
                    fit="cover"
                    :preview-src-list="[getFullPhotoUrl(photo.photoUrl)]"
                    preview-teleported
                    class="photo-img"
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                        <span>照片加载失败</span>
                      </div>
                    </template>
                  </el-image>
                </div>
                <div class="photo-details">
                  <p><strong>打卡时间:</strong> {{ formatDateTime(photo.checkTime) }}</p>
                  <p><strong>打卡结果:</strong>
                    <el-tag
                      :type="photo.result === 'success' ? 'success' : 'warning'"
                      size="small"
                    >
                      {{ photo.result === 'success' ? '成功' : '需要重检' }}
                    </el-tag>
                  </p>
                  <p v-if="photo.elderAge"><strong>年龄:</strong> {{ photo.elderAge }}</p>
                  <p v-if="photo.elderGender"><strong>性别:</strong> {{ photo.elderGender }}</p>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="no-photos">
            <el-empty description="暂无打卡照片" />
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showElderPhotosDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 老人异常事件查看对话框 -->
    <el-dialog
      v-model="showElderEventsDialog"
      :title="`${selectedElderForEvents?.name || '老人'}的异常事件`"
      width="80%"
      top="5vh"
    >
      <div v-if="selectedElderForEvents" class="elder-events-container">
        <div class="elder-info-header">
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="姓名">{{ selectedElderForEvents.name }}</el-descriptions-item>
            <el-descriptions-item label="年龄">{{ selectedElderForEvents.age }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ selectedElderForEvents.gender }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-loading="eventsLoading" class="events-content">
          <div v-if="elderEvents.length > 0" class="events-timeline">
            <div
              v-for="(event, index) in elderEvents"
              :key="event.id"
              class="event-item"
            >
              <div class="event-time">
                {{ formatDateTime(event.createdAt) }}
                <el-tag
                  :type="event.status === 'handled' ? 'success' : 'warning'"
                  size="small"
                  style="margin-left: 8px;"
                >
                  {{ event.status === 'handled' ? '已处理' : '未处理' }}
                </el-tag>
              </div>
              <div class="event-content">
                <div class="event-details">
                  <p><strong>事件类型:</strong>
                    <el-tag
                      :type="getEventTypeColor(event.eventType)"
                      size="small"
                    >
                      {{ getEventTypeLabel(event.eventType) }}
                    </el-tag>
                  </p>
                  <p><strong>发生时间:</strong> {{ formatDateTime(event.createdAt) }}</p>
                  <p v-if="event.handledAt"><strong>处理时间:</strong> {{ formatDateTime(event.handledAt) }}</p>
                  <p><strong>描述:</strong> {{ event.description || '无描述' }}</p>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="no-events">
            <el-empty description="暂无异常事件" />
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showElderEventsDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Picture, Check, Warning } from '@element-plus/icons-vue'
import { emergencyApi } from '@/api'
import request from '@/utils/request'

const events = ref([])
const loading = ref(false)
const activeTab = ref('face_check')
const faceCheckedRecords = ref([])
const todayCheckStats = ref(null)
const statusFilter = ref('')
const eventTypeFilter = ref('')
const showEventDetailDialog = ref(false)
const selectedEvent = ref(null)
const showFaceCheckDetailDialog = ref(false)
const selectedFaceCheck = ref(null)
const showElderDetailDialog = ref(false)
const selectedElder = ref(null)
const showElderPhotosDialog = ref(false)
const selectedElderForPhotos = ref(null)
const elderPhotos = ref([])
const photosLoading = ref(false)
const showElderEventsDialog = ref(false)
const selectedElderForEvents = ref(null)
const elderEvents = ref([])
const eventsLoading = ref(false)
const elders = ref([])
const gridGroups = ref([])

// 老人筛选器
const elderFilter = reactive({
  status: '',
  gridGroupId: ''
})

// 打卡记录筛选器
const faceCheckFilter = reactive({
  status: '',
  elderId: '',
  dateRange: []
})

const statistics = reactive({
  unhandled: 0,
  today: 0,
  week: 0,
  uncheckedElders: 0
})

// 计算属性：根据当前标签页和筛选条件过滤事件
const filteredEvents = computed(() => {
  let filtered = [...events.value]

  // 根据标签页过滤
  if (activeTab.value === 'emergency') {
    filtered = filtered.filter(e => e.eventType === 'EMERGENCY_CALL')
  } else if (activeTab.value === 'face_check') {
    filtered = filtered.filter(e => e.eventType === 'FACE_CHECK_EXCEPTION')
  }

  // 根据状态筛选
  if (statusFilter.value) {
    filtered = filtered.filter(e => e.status === statusFilter.value)
  }

  // 根据事件类型筛选
  if (eventTypeFilter.value) {
    filtered = filtered.filter(e => e.eventType === eventTypeFilter.value)
  }

  return filtered
})

// 计算属性：根据筛选条件过滤老人
const filteredElders = computed(() => {
  let filtered = [...elders.value]

  // 根据状态筛选
  if (elderFilter.status) {
    filtered = filtered.filter(e => e.status === elderFilter.status)
  }

  // 根据网格群筛选
  if (elderFilter.gridGroupId) {
    filtered = filtered.filter(e => e.gridGroupId === elderFilter.gridGroupId)
  }

  return filtered
})

// 计算属性：根据筛选条件过滤打卡记录 - 已移除筛选功能，直接返回原始数据
// const filteredFaceChecks = computed(() => {
//   let filtered = [...faceCheckedRecords.value]
//
//   // 根据结果筛选
//   if (faceCheckFilter.status) {
//     filtered = filtered.filter(e => e.result === faceCheckFilter.status)
//   }
//
//   // 根据老人筛选
//   if (faceCheckFilter.elderId) {
//     filtered = filtered.filter(e => e.elderId === faceCheckFilter.elderId)
//   }
//
//   // 根据日期范围筛选
//   if (faceCheckFilter.dateRange && faceCheckFilter.dateRange.length === 2) {
//     const [startDate, endDate] = faceCheckFilter.dateRange
//     filtered = filtered.filter(e => {
//       const checkDate = e.checkTime ? new Date(e.checkTime).toISOString().split('T')[0] : null
//       return checkDate >= startDate && checkDate <= endDate
//     })
//   }
//
//   return filtered
// })

const loadEvents = async () => {
  try {
    loading.value = true

    // 加载统计信息
    const statsResponse = await emergencyApi.getStatistics()
    const stats = statsResponse || {}
    statistics.unhandled = stats.unhandled || 0
    statistics.today = stats.today || 0
    statistics.week = stats.week || 0

    // 加载所有紧急事件（包括已处理和未处理）
    const allEmergencyEvents = await emergencyApi.getAllEvents()
    const emergencyData = allEmergencyEvents.map(event => ({
      ...event,
      eventType: 'EMERGENCY_CALL',
      description: event.description || '老人发起紧急呼叫'
    }))

    // 加载未刷脸异常
    const faceCheckExceptions = await emergencyApi.getAbnormalToday()
    const faceCheckData = faceCheckExceptions.map(elder => ({
      id: `FACE_${elder.elderId}_${Date.now()}`,
      elderId: elder.elderId,
      elderName: elder.elderName,
      eventType: 'FACE_CHECK_EXCEPTION',
      status: 'pending',
      createdAt: new Date().toISOString(),
      handledAt: null,
      description: '老人今日未进行刷脸确认'
    }))

    // 合并事件
    events.value = [...emergencyData, ...faceCheckData]
    statistics.uncheckedElders = faceCheckData.length

    // 加载已刷脸记录
    if (activeTab.value === 'face_check') {
      await loadFaceCheckedRecords()
    }

    ElMessage.success('紧急事件加载成功')
    console.log('紧急事件数据:', events.value)

  } catch (error) {
    console.error('加载紧急事件失败:', error)
    ElMessage.error('加载紧急事件失败')
  } finally {
    loading.value = false
  }
}

// 加载已刷脸记录
const loadFaceCheckedRecords = async () => {
  try {
    const response = await emergencyApi.getTodayFaceChecks()
    console.log('后端返回的打卡记录数据:', response)

    // 后端返回的是List<Map<String, Object>>格式
    // 需要转换为数组格式
    if (Array.isArray(response)) {
      faceCheckedRecords.value = response
    } else if (response && typeof response === 'object') {
      // 如果是对象，提取values数组
      const records = Object.values(response)
      faceCheckedRecords.value = records.flat()
    } else {
      faceCheckedRecords.value = []
    }

    console.log('处理后的打卡记录数据:', faceCheckedRecords.value)
  } catch (error) {
    console.warn('加载已刷脸记录失败:', error)
    ElMessage.error('加载已刷脸记录失败')
  }
}

// 加载今日完整打卡状态
const loadTodayCheckStats = async () => {
  try {
    loading.value = true
    const response = await request.get('/face-check/today-all')
    console.log('今日打卡状态数据:', response)

    // 响应拦截器已经提取了data字段，所以response就是实际数据
    if (response && typeof response === 'object') {
      todayCheckStats.value = response
    } else {
      todayCheckStats.value = null
    }

    ElMessage.success('打卡状态加载成功')
  } catch (error) {
    console.error('加载今日打卡状态失败:', error)
    ElMessage.error('加载今日打卡状态失败')
    todayCheckStats.value = null
  } finally {
    loading.value = false
  }
}

// 获取状态标签类型
const getStatusType = (status) => {
  switch (status) {
    case '正常': return 'success'
    case '需重检': return 'warning'
    case '失败': return 'danger'
    case '异常': return 'danger'
    case '未打卡': return 'warning'
    default: return 'info'
  }
}

// 通知老人打卡
const handleNotifyElder = async (elder) => {
  try {
    await ElMessageBox.confirm(
      `确认通知${elder.elderName}进行打卡？系统会发送提醒通知。`,
      '通知打卡',
      {
        confirmButtonText: '确认通知',
        cancelButtonText: '取消',
        type: 'info',
      }
    )

    ElMessage.info(`正在通知${elder.elderName}打卡...`)

    // TODO: 调用通知API
    // await emergencyApi.notifyElderCheck(elder.elderId)

    ElMessage.success(`已通知${elder.elderName}进行打卡`)

  } catch (error) {
    if (error !== 'cancel') {
      console.error('通知打卡失败:', error)
      ElMessage.error('通知打卡失败')
    }
  }
}

const handleEvent = async (event) => {
  try {
    // 对于未刷脸异常，调用专门的API
    if (event.eventType === 'FACE_CHECK_EXCEPTION') {
      // 调用后端API处理未刷脸异常，基于老人ID
      const response = await emergencyApi.handleFaceCheckException(event.elderId)
      ElMessage.success(`已处理${event.elderName}的未刷脸异常`)
      event.status = 'handled'
      event.handledAt = new Date().toISOString()
      statistics.unhandled--
      return
    }

    // 对于真实的紧急事件，调用后端API
    const response = await emergencyApi.handleEvent(event.id)
    ElMessage.success(`已处理${event.elderName}的事件`)
    event.status = 'handled'
    event.handledAt = new Date().toISOString()
    statistics.unhandled--

  } catch (error) {
    console.error('处理事件失败:', error)
    ElMessage.error('处理事件失败')
  }
}

const viewEvent = (event) => {
  selectedEvent.value = event
  showEventDetailDialog.value = true
}

const viewFaceCheckDetail = (record) => {
  selectedFaceCheck.value = record
  showFaceCheckDetailDialog.value = true
}

// 重新打卡处理
const handleRecheck = async (record) => {
  try {
    await ElMessageBox.confirm(
      `确认协助${record.elderName}重新打卡？系统会通知老人完成打卡。`,
      '重新打卡确认',
      {
        confirmButtonText: '确认请求',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    ElMessage.info('正在发送重新打卡请求...')

    // 构造重新打卡请求数据
    const recheckData = {
      elderId: record.elderId || record.elderId,
      result: 'pending_recheck',
      adminAssisted: true,
      recheckReason: '管理员协助重新打卡'
    }

    // 调用后端API创建重新打卡请求
    const response = await emergencyApi.createRecheckRecord(recheckData)
    console.log('重新打卡API响应:', response)

    ElMessage.success(`已向${record.elderName}发送重新打卡请求`)

    // 重新加载数据
    await loadEvents()

  } catch (error) {
    if (error !== 'cancel') {
      console.error('重新打卡失败:', error)
      ElMessage.error('重新打卡失败')
    }
  }
}

const clearFilters = () => {
  statusFilter.value = ''
  eventTypeFilter.value = ''
}

const clearElderFilters = () => {
  elderFilter.status = ''
  elderFilter.gridGroupId = ''
}

// 已移除清除打卡记录筛选函数，因为不再需要筛选功能
// const clearFaceCheckFilters = () => {
//   faceCheckFilter.status = ''
//   faceCheckFilter.elderId = ''
//   faceCheckFilter.dateRange = []
// }

const viewElderDetail = (elder) => {
  selectedElder.value = elder
  showElderDetailDialog.value = true
}

const manageElder = (elder) => {
  ElMessage.info(`管理${elder.name}`)
  // TODO: 实现老人管理功能
}

const viewElderAllPhotos = async (elder) => {
  try {
    selectedElderForPhotos.value = elder
    showElderPhotosDialog.value = true
    photosLoading.value = true

    // 直接使用request调用API，避免缓存问题
    const response = await request.get(`/face-check/elder/${elder.id}/photos`)
    console.log('老人照片数据:', response)

    // 处理响应数据
    if (response && response.data) {
      elderPhotos.value = response.data
    } else if (Array.isArray(response)) {
      elderPhotos.value = response
    } else {
      elderPhotos.value = []
    }

    ElMessage.success(`已加载${elder.name}的所有打卡照片`)
  } catch (error) {
    console.error('查看照片失败:', error)
    ElMessage.error('查看照片失败')
    elderPhotos.value = []
  } finally {
    photosLoading.value = false
  }
}

const viewElderAbnormalEvents = async (elder) => {
  try {
    selectedElderForEvents.value = elder
    showElderEventsDialog.value = true
    eventsLoading.value = true

    // 直接使用request调用API，避免缓存问题
    const response = await request.get(`/emergency/elder/${elder.id}`)
    console.log('老人异常事件数据:', response)

    // 处理响应数据
    if (response && Array.isArray(response)) {
      elderEvents.value = response
    } else if (response && response.data) {
      elderEvents.value = response.data
    } else {
      elderEvents.value = []
    }

    ElMessage.success(`已加载${elder.name}的异常事件`)
  } catch (error) {
    console.error('查看异常事件失败:', error)
    ElMessage.error('查看异常事件失败')
    elderEvents.value = []
  } finally {
    eventsLoading.value = false
  }
}

const loadElders = async () => {
  try {
    loading.value = true

    // 使用新的API获取包含子女信息的老人列表
    const response = await request.get('/elders/with-children')
    console.log('老人列表数据:', response)

    // 处理响应数据
    if (response && response.data) {
      elders.value = response.data
    } else if (Array.isArray(response)) {
      elders.value = response
    } else {
      elders.value = []
    }

    // 加载网格群列表
    const groupsResponse = await emergencyApi.getAllGridGroups()
    gridGroups.value = groupsResponse || [
      { id: 1, name: '测试网格群' },
      { id: 2, name: '阳光小区A区' },
      { id: 3, name: '阳光小区B区' }
    ]

    ElMessage.success('老人列表加载成功')
  } catch (error) {
    console.error('加载老人列表失败:', error)
    ElMessage.error('加载老人列表失败')
  } finally {
    loading.value = false
  }
}

// 构建完整的照片URL
const getFullPhotoUrl = (photoUrl) => {
  if (!photoUrl) return ''

  // 如果已经是完整URL，直接返回
  if (photoUrl.startsWith('http')) {
    return photoUrl
  }

  // 如果是相对路径，构建完整URL
  const baseUrl = 'http://localhost:8082'
  return photoUrl.startsWith('/') ? `${baseUrl}${photoUrl}` : `${baseUrl}/${photoUrl}`
}

const getEventTypeColor = (type) => {
  switch (type) {
    case 'EMERGENCY_CALL': return 'danger'
    case 'FACE_CHECK_EXCEPTION': return 'warning'
    default: return 'info'
  }
}

const getEventTypeLabel = (type) => {
  switch (type) {
    case 'EMERGENCY_CALL': return '紧急呼叫'
    case 'FACE_CHECK_EXCEPTION': return '未刷脸异常'
    default: return '未知'
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 监听标签页切换
watch(activeTab, () => {
  if (activeTab.value === 'face_check') {
    loadTodayCheckStats()
  }
})

onMounted(() => {
  console.log('EmergencyView 组件已挂载')
  loadEvents()
  loadElders()
  loadTodayCheckStats() // 总是加载打卡数据
})
</script>

<style scoped>
.emergency-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.statistics-row {
  margin-bottom: 20px;
}

.event-content {
  margin-top: 20px;
}

.event-tabs {
  margin-bottom: 20px;
}

.filter-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

/* 新增样式 */
.check-stats {
  margin-bottom: 24px;
  padding: 20px;
  background: linear-gradient(135deg, white 0%, white 100%);
  border-radius: 12px;
  color: black;
}

.check-stats :deep(.el-statistic__content) {
  color: black;
}

.check-stats :deep(.el-statistic__title) {
  color: rgba(255, 255, 255, 0.8);
}

.section-container {
  margin-bottom: 32px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  padding-bottom: 8px;
  border-bottom: 2px solid #e4e7ed;
}

.section-title .el-icon {
  font-size: 20px;
}

.loading-container {
  min-height: 200px;
}

.photo-preview {
  display: flex;
  justify-content: center;
  align-items: center;
}

.no-photo {
  color: #909399;
  font-size: 12px;
}

.loading-container :deep(.el-loading-mask) {
  background-color: rgba(255, 255, 255, 0.8);
}

.loading-container :deep(.el-loading-spinner) {
  margin-right: 8px;
}

.loading-container :deep(.el-loading-text) {
  color: #409eff;
  font-size: 14px;
}

.photo-preview {
  display: flex;
  justify-content: center;
  align-items: center;
}

.no-photo {
  color: #999;
  font-size: 12px;
}

.photo-preview :deep(.el-image) {
  border-radius: 4px;
  overflow: hidden;
}

.photo-preview :deep(.el-image__inner) {
  object-fit: cover;
}

.child-info {
  font-size: 12px;
  line-height: 1.4;
  margin-bottom: 2px;
  color: #606266;
}

.no-children {
  color: #999;
  font-size: 12px;
}

.elder-photos-container {
  max-height: 70vh;
  overflow-y: auto;
}

.elder-info-header {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.photos-content {
  min-height: 300px;
}

.photos-timeline {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.photo-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 15px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.3s ease;
}

.photo-item:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.photo-time {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.photo-content {
  display: flex;
  gap: 15px;
}

.photo-image {
  flex-shrink: 0;
}

.photo-img {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  cursor: pointer;
}

.photo-details {
  flex: 1;
  font-size: 14px;
  line-height: 1.6;
}

.photo-details p {
  margin: 5px 0;
  color: #606266;
}

.photo-details strong {
  color: #303133;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
  font-size: 14px;
}

.image-error .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.no-photos {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.elder-events-container {
  max-height: 70vh;
  overflow-y: auto;
}

.events-content {
  min-height: 300px;
}

.events-timeline {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.event-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 15px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.3s ease;
}

.event-item:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.event-time {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.event-content {
  display: flex;
  gap: 15px;
}

.event-details {
  flex: 1;
  font-size: 14px;
  line-height: 1.6;
}

.event-details p {
  margin: 5px 0;
  color: #606266;
}

.event-details strong {
  color: #303133;
}

.no-events {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}
</style>
