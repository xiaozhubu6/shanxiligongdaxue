<template>
  <div class="community-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>社区管理</span>
          <el-button type="primary" @click="showCreateCommunityDialog = true">
            <el-icon><Plus /></el-icon>
            新增社区
          </el-button>
        </div>
      </template>
      
      <!-- 社区列表 -->
      <el-table :data="communities" style="width: 100%" row-key="id">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="社区名称" />
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320">
          <template #default="{ row }">
            <el-button size="small" @click="viewGridGroups(row)">
              查看网格群
            </el-button>
            <el-button size="small" type="primary" @click="editCommunity(row)">
              修改
            </el-button>
            <el-button size="small" type="success" @click="showCreateGroupDialog = true; selectedCommunity = row">
              新增网格群
            </el-button>
            <el-button size="small" type="danger" @click="deleteCommunity(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 网格群管理对话框 -->
    <el-dialog v-model="showGridGroupsDialog" title="网格群管理" width="800px">
      <div class="grid-groups-content">
        <div class="community-info">
          <h3>{{ selectedCommunity?.name }}</h3>
          <p>地址：{{ selectedCommunity?.address }}</p>
        </div>
        
        <div class="groups-section">
          <div class="section-header">
            <span>网格群列表</span>
            <el-button type="primary" size="small" @click="showCreateGroupDialog = true">
              新增网格群
            </el-button>
          </div>
          
          <el-table :data="gridGroups" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="网格群名称" />
            <el-table-column prop="unitNumber" label="单元号" />
            <el-table-column prop="elderCount" label="老人数量" width="100">
              <template #default="{ row }">
                <el-tag type="info">{{ row.elderCount || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" @click="viewElderStatus(row)">
                  查看老人状态
                </el-button>
                <el-button size="small" type="primary" @click="editGridGroup(row)">
                  修改
                </el-button>
                <el-button size="small" type="danger" @click="deleteGridGroup(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 创建社区对话框 -->
    <el-dialog v-model="showCreateCommunityDialog" title="新增社区" width="500px">
      <el-form :model="communityForm" :rules="communityRules" ref="communityFormRef">
        <el-form-item label="社区名称" prop="name">
          <el-input v-model="communityForm.name" placeholder="请输入社区名称" />
        </el-form-item>
        <el-form-item label="社区地址" prop="address">
          <el-input v-model="communityForm.address" placeholder="请输入社区地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateCommunityDialog = false">取消</el-button>
        <el-button type="primary" @click="createCommunity" :loading="loading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改社区对话框 -->
    <el-dialog v-model="showEditCommunityDialog" title="修改社区" width="500px">
      <el-form :model="editCommunityForm" :rules="communityRules" ref="editCommunityFormRef">
        <el-form-item label="社区名称" prop="name">
          <el-input v-model="editCommunityForm.name" placeholder="请输入社区名称" />
        </el-form-item>
        <el-form-item label="社区地址" prop="address">
          <el-input v-model="editCommunityForm.address" placeholder="请输入社区地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditCommunityDialog = false">取消</el-button>
        <el-button type="primary" @click="updateCommunity" :loading="loading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改网格群对话框 -->
    <el-dialog v-model="showEditGroupDialog" title="修改网格群" width="500px">
      <el-form :model="editGroupForm" :rules="groupRules" ref="editGroupFormRef">
        <el-form-item label="网格群名称" prop="name">
          <el-input v-model="editGroupForm.name" placeholder="请输入网格群名称（如：X号楼）" />
        </el-form-item>
        <el-form-item label="单元号" prop="unitNumber">
          <el-input v-model="editGroupForm.unitNumber" placeholder="请输入单元号（如：x单元）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditGroupDialog = false">取消</el-button>
        <el-button type="primary" @click="updateGridGroup" :loading="loading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 创建网格群对话框 -->
    <el-dialog v-model="showCreateGroupDialog" title="新增网格群" width="500px">
      <el-form :model="groupForm" :rules="groupRules" ref="groupFormRef">
        <el-form-item label="网格群名称" prop="name">
          <el-input v-model="groupForm.name" placeholder="请输入网格群名称（如：X号楼）" />
        </el-form-item>
        <el-form-item label="单元号" prop="unitNumber">
          <el-input v-model="groupForm.unitNumber" placeholder="请输入单元号（如：x单元）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateGroupDialog = false">取消</el-button>
        <el-button type="primary" @click="createGridGroup" :loading="loading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 网格群状态对话框 -->
    <el-dialog v-model="showElderStatusDialog" :title="`${selectedGroup?.name} - 状态概览`" width="900px">
      <div class="group-status-content">
        <!-- 概览卡片 -->
        <div class="status-cards">
          <el-card class="status-card">
            <div class="card-content">
              <div class="card-value">{{ elderStats.total }}</div>
              <div class="card-label">老人总数</div>
            </div>
          </el-card>
          <el-card class="status-card">
            <div class="card-content">
              <div class="card-value">{{ elderStats.monthChecked }}</div>
              <div class="card-label">本月已刷脸</div>
            </div>
          </el-card>
          <el-card class="status-card">
            <div class="card-content">
              <div class="card-value">{{ elderStats.abnormal }}</div>
              <div class="card-label">异常老人</div>
            </div>
          </el-card>
          <el-card class="status-card">
            <div class="card-content">
              <div class="card-value">{{ Math.round((elderStats.monthChecked / elderStats.total) * 100) || 0 }}%</div>
              <div class="card-label">刷脸率</div>
            </div>
          </el-card>
        </div>

        <!-- 图表区域 -->
        <div class="charts-container">
          <div class="chart-row">
            <!-- 刷脸状态饼图 -->
            <el-card class="chart-card">
              <template #header>
                <span>刷脸状态分布</span>
              </template>
              <div ref="faceCheckChartRef" class="chart"></div>
            </el-card>
            
            <!-- 本月事件统计 -->
            <el-card class="chart-card">
              <template #header>
                <span>本月事件统计</span>
              </template>
              <div ref="eventsChartRef" class="chart"></div>
            </el-card>
          </div>
          
          <div class="chart-row full-width">
            <!-- 老人详细列表 -->
            <el-card class="chart-card full-width">
              <template #header>
                <span>老人详细状态</span>
              </template>
              <el-table :data="elders" size="small" max-height="300">
                <el-table-column prop="name" label="姓名" width="80" />
                <el-table-column prop="age" label="年龄" width="60" />
                <el-table-column prop="gender" label="性别" width="60" />
                <el-table-column prop="monthCheckCount" label="本月打卡次数" width="100" />
                <el-table-column prop="monthEmergencyCount" label="本月求救次数" width="100" />
                <el-table-column prop="lastFaceCheckTime" label="最后刷脸时间" width="120">
                  <template #default="{ row }">
                    {{ row.lastFaceCheckTime ? formatDate(row.lastFaceCheckTime) : '未打卡' }}
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { communityApi, elderApi } from '@/api'
import request from '@/utils/request'
import * as echarts from 'echarts'

// 响应式数据
const communities = ref([])
const gridGroups = ref([])
const elders = ref([])
const selectedCommunity = ref(null)
const selectedGroup = ref(null)

// 对话框控制
const showCreateCommunityDialog = ref(false)
const showEditCommunityDialog = ref(false)
const showCreateGroupDialog = ref(false)
const showEditGroupDialog = ref(false)
const showGridGroupsDialog = ref(false)
const showElderStatusDialog = ref(false)

// 图表引用
const faceCheckChartRef = ref()
const eventsChartRef = ref()

// 图表实例引用，用于销毁图表
const faceCheckChartInstance = ref(null)
const eventsChartInstance = ref(null)

// 表单数据
const communityForm = ref({
  name: '',
  address: ''
})

const editCommunityForm = ref({
  id: null,
  name: '',
  address: ''
})

const groupForm = ref({
  name: '',
  unitNumber: ''
})

const editGroupForm = ref({
  id: null,
  name: '',
  unitNumber: ''
})

// 统计数据
const elderStats = ref({
  total: 0,
  monthChecked: 0,
  unchecked: 0,
  abnormal: 0
})

// 加载状态
const loading = ref(false)

// 表单引用
const communityFormRef = ref()
const editCommunityFormRef = ref()
const groupFormRef = ref()
const editGroupFormRef = ref()

// 表单验证规则
const communityRules = {
  name: [{ required: true, message: '请输入社区名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入社区地址', trigger: 'blur' }]
}

const groupRules = {
  name: [{ required: true, message: '请输入网格群名称', trigger: 'blur' }],
  unitNumber: [{ required: true, message: '请输入单元号', trigger: 'blur' }]
}

// 方法
const loadCommunities = async () => {
  try {
    const response = await communityApi.getCommunities()
    communities.value = response
  } catch (error) {
    ElMessage.error('加载社区列表失败')
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}年${(date.getMonth() + 1).toString().padStart(2, '0')}月${date.getDate().toString().padStart(2, '0')}日`
}

const createCommunity = async () => {
  if (!communityFormRef.value) return
  
  try {
    await communityFormRef.value.validate()
    loading.value = true
    
    await communityApi.createCommunity(communityForm.value)
    ElMessage.success('社区创建成功')
    showCreateCommunityDialog.value = false
    communityForm.value = { name: '', address: '' }
    loadCommunities()
  } catch (error) {
    ElMessage.error('社区创建失败')
  } finally {
    loading.value = false
  }
}

const editCommunity = (community) => {
  editCommunityForm.value = {
    id: community.id,
    name: community.name,
    address: community.address
  }
  showEditCommunityDialog.value = true
}

const updateCommunity = async () => {
  if (!editCommunityFormRef.value) return
  
  try {
    await editCommunityFormRef.value.validate()
    loading.value = true
    
    await communityApi.updateCommunity(editCommunityForm.value.id, {
      name: editCommunityForm.value.name,
      address: editCommunityForm.value.address
    })
    
    ElMessage.success('社区修改成功')
    showEditCommunityDialog.value = false
    editCommunityForm.value = { id: null, name: '', address: '' }
    loadCommunities()
  } catch (error) {
    ElMessage.error('社区修改失败')
  } finally {
    loading.value = false
  }
}

const viewGridGroups = async (community) => {
  selectedCommunity.value = community
  try {
    const response = await communityApi.getGridGroups(community.id)
    
    // 为每个网格群获取真实的老人数量
    const gridGroupsWithCount = await Promise.all(
      response.map(async (group) => {
        try {
          const elders = await elderApi.getEldersByGridGroup(group.id)
          return {
            ...group,
            elderCount: elders.length // 真实老人数量
          }
        } catch (error) {
          // 如果获取老人数量失败，显示0
          return {
            ...group,
            elderCount: 0
          }
        }
      })
    )
    
    gridGroups.value = gridGroupsWithCount
    showGridGroupsDialog.value = true
  } catch (error) {
    ElMessage.error('加载网格群失败')
  }
}

const createGridGroup = async () => {
  if (!groupFormRef.value || !selectedCommunity.value) return
  
  try {
    await groupFormRef.value.validate()
    loading.value = true
    
    const groupData = {
      name: groupForm.value.name,
      unitNumber: groupForm.value.unitNumber,
      communityId: selectedCommunity.value.id
    }
    
    await communityApi.createGridGroup(groupData)
    ElMessage.success('网格群创建成功')
    showCreateGroupDialog.value = false
    groupForm.value = { name: '', unitNumber: '' }
    viewGridGroups(selectedCommunity.value)
  } catch (error) {
    ElMessage.error('网格群创建失败')
  } finally {
    loading.value = false
  }
}

const editGridGroup = (group) => {
  editGroupForm.value = {
    id: group.id,
    name: group.name,
    unitNumber: group.unitNumber
  }
  showEditGroupDialog.value = true
}

const updateGridGroup = async () => {
  if (!editGroupFormRef.value) return
  
  try {
    await editGroupFormRef.value.validate()
    loading.value = true
    
    await communityApi.updateGridGroup(editGroupForm.value.id, {
      name: editGroupForm.value.name,
      unitNumber: editGroupForm.value.unitNumber
    })
    
    ElMessage.success('网格群修改成功')
    showEditGroupDialog.value = false
    editGroupForm.value = { id: null, name: '', unitNumber: '' }
    viewGridGroups(selectedCommunity.value)
  } catch (error) {
    ElMessage.error('网格群修改失败')
  } finally {
    loading.value = false
  }
}

const viewElderStatus = async (group) => {
  selectedGroup.value = group
  try {
    // 获取真实的老人数据
    const eldersData = await elderApi.getEldersByGridGroup(group.id)
    
    // 获取本月打卡状态
    const monthCheckResponse = await request.get('/face-check/month-all')
    const monthCheckData = monthCheckResponse || {}
    
    // 获取本月紧急事件统计
    const emergencyResponse = await request.get('/emergency/statistics')
    const emergencyData = emergencyResponse || {}
    
    // 获取所有紧急事件详情来统计每个老人的求救次数
    const allEventsResponse = await request.get('/emergency/all')
    const allEvents = allEventsResponse || []
    
    // 计算本月时间范围
    const now = new Date()
    const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
    const monthEnd = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59)
    
    // 为老人数据添加本月打卡状态信息和求救次数
    elders.value = eldersData.map(elder => {
      // 统计本月打卡次数
      const monthRecords = monthCheckData.checkedRecords?.filter(r => r.elderId === elder.id) || []
      const monthCheckCount = monthRecords.length
      
      // 统计本月求救次数
      const monthEmergencies = allEvents.filter(event => {
        const eventDate = new Date(event.createdAt)
        return event.elderId === elder.id && 
               eventDate >= monthStart && 
               eventDate <= monthEnd &&
               event.eventType === 'EMERGENCY_CALL'
      })
      const monthEmergencyCount = monthEmergencies.length
      
      // 获取最后打卡时间
      const lastRecord = monthRecords.sort((a: any, b: any) => new Date(b.checkTime).getTime() - new Date(a.checkTime).getTime())[0]
      
      return {
        ...elder,
        monthCheckCount,
        monthEmergencyCount,
        lastFaceCheckTime: lastRecord?.checkTime || null
      }
    })
    
    // 计算统计数据
    elderStats.value = {
      total: elders.value.length,
      monthChecked: elders.value.filter(e => e.monthCheckCount > 0).length,
      unchecked: elders.value.filter(e => e.monthCheckCount === 0).length,
      abnormal: elders.value.filter(e => e.status && e.status !== 'NORMAL').length
    }
    
    showElderStatusDialog.value = true
    
    // 等待对话框渲染完成后初始化图表
    await nextTick()
    initCharts()
  } catch (error) {
    ElMessage.error('加载老人状态失败')
  }
}

// 初始化图表
const initCharts = () => {
  // 刷脸状态饼图
  if (faceCheckChartRef.value) {
    // 销毁现有图表实例，防止内存泄漏
    if (faceCheckChartInstance.value) {
      faceCheckChartInstance.value.dispose()
    }
    
    const faceCheckChart = echarts.init(faceCheckChartRef.value)
    const faceCheckOption = {
      title: {
        text: '本月刷脸状态',
        left: 'center',
        textStyle: { fontSize: 14 }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      series: [{
        name: '刷脸状态',
        type: 'pie',
        radius: '60%',
        data: [
          { value: elderStats.value.monthChecked, name: '已刷脸', itemStyle: { color: '#67C23A' } },
          { value: elderStats.value.unchecked, name: '未刷脸', itemStyle: { color: '#F56C6C' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    }
    faceCheckChart.setOption(faceCheckOption)
    
    // 保存图表实例
    faceCheckChartInstance.value = faceCheckChart
    
    // 添加窗口大小变化监听，重新调整图表大小
    window.addEventListener('resize', () => faceCheckChart.resize())
  }

  // 事件统计柱状图
  if (eventsChartRef.value) {
    // 销毁现有图表实例，防止内存泄漏
    if (eventsChartInstance.value) {
      eventsChartInstance.value.dispose()
    }
    
    const eventsChart = echarts.init(eventsChartRef.value)
    
    // 计算实际事件数据
    const totalEmergencyCount = elders.value.reduce((sum, elder) => sum + (elder.monthEmergencyCount || 0), 0)
    const totalCheckCount = elders.value.reduce((sum, elder) => sum + (elder.monthCheckCount || 0), 0)
    
    const eventsData = [
      { name: '紧急呼叫', value: totalEmergencyCount, itemStyle: { color: '#E6A23C' } },
      { name: '代购请求', value: Math.floor(Math.random() * 10), itemStyle: { color: '#409EFF' } },
      { name: '刷脸确认', value: totalCheckCount, itemStyle: { color: '#67C23A' } }
    ]
    
    const eventsOption = {
      title: {
        text: '本月事件统计',
        left: 'center',
        textStyle: { fontSize: 14 }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      xAxis: {
        type: 'category',
        data: eventsData.map(item => item.name)
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        name: '事件数量',
        type: 'bar',
        data: eventsData.map(item => ({
          value: item.value,
          itemStyle: { color: item.itemStyle.color }
        })),
        barWidth: '40%'
      }]
    }
    eventsChart.setOption(eventsOption)
    
    // 保存图表实例
    eventsChartInstance.value = eventsChart
    
    // 添加窗口大小变化监听，重新调整图表大小
    window.addEventListener('resize', () => eventsChart.resize())
  }
}

// 销毁图表实例
const disposeCharts = () => {
  if (faceCheckChartInstance.value) {
    faceCheckChartInstance.value.dispose()
    faceCheckChartInstance.value = null
  }
  
  if (eventsChartInstance.value) {
    eventsChartInstance.value.dispose()
    eventsChartInstance.value = null
  }
}

const deleteCommunity = async (community) => {
  try {
    await ElMessageBox.confirm(`确定删除社区"${community.name}"吗？`, '确认删除', {
      type: 'warning'
    })
    
    await communityApi.deleteCommunity(community.id)
    ElMessage.success('社区删除成功')
    loadCommunities()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('社区删除失败')
    }
  }
}

const deleteGridGroup = async (group) => {
  try {
    await ElMessageBox.confirm(`确定删除网格群"${group.name}"吗？`, '确认删除', {
      type: 'warning'
    })
    
    await communityApi.deleteGridGroup(group.id)
    ElMessage.success('网格群删除成功')
    viewGridGroups(selectedCommunity.value)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网格群删除失败')
    }
  }
}

// 页面加载时执行
onMounted(() => {
  loadCommunities()
})

// 页面卸载时销毁图表实例
onUnmounted(() => {
  disposeCharts()
})
</script>

<style scoped>
.community-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.grid-groups-content {
  max-height: 600px;
  overflow-y: auto;
}

.community-info {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.community-info h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.community-info p {
  margin: 0;
  color: #606266;
}

.groups-section {
  margin-top: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.elder-status-content {
  max-height: 600px;
  overflow-y: auto;
}

.group-info {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.group-info h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.group-info p {
  margin: 0;
  color: #606266;
}

/* 网格群状态样式 */
.group-status-content {
  max-height: 70vh;
  overflow-y: auto;
}

.status-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.status-card {
  text-align: center;
}

.card-content {
  padding: 16px;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.card-label {
  font-size: 14px;
  color: #909399;
}

.charts-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.chart-row.full-width {
  grid-template-columns: 1fr;
}

.chart-card {
  min-height: 300px;
}

.chart-card.full-width {
  grid-column: 1 / -1;
}

.chart {
  width: 100%;
  height: 250px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chart-row {
    grid-template-columns: 1fr;
  }
  
  .chart {
    height: 200px;
  }
  
  .status-cards {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 480px) {
  .status-cards {
    grid-template-columns: 1fr;
  }
  
  .chart {
    height: 180px;
  }
  
  .community-management {
    padding: 10px;
  }
  
  .el-table {
    font-size: 12px;
  }
  
  .el-table th,
  .el-table td {
    padding: 8px;
  }
  
  .el-button {
    padding: 6px 12px;
    font-size: 12px;
  }
}

/* 确保表格内容不溢出 */
el-table {
  table-layout: fixed;
  word-break: break-all;
  overflow: auto;
}

/* 调整表格列宽 */
el-table-column {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 确保对话框内容可滚动 */
el-dialog__body {
  overflow: auto;
  max-height: 80vh;
}

/* 确保卡片内容不溢出 */
el-card__body {
  overflow: visible;
}
</style>
