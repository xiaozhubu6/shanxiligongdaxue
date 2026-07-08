<template>
  <div class="purchase-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>代购管理</span>
          <div class="header-actions">
            <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 120px; margin-right: 10px;">
              <el-option label="全部" value="" />
              <el-option label="待处理" value="pending" />
              <el-option label="已确认" value="confirmed" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
            <el-button type="success" @click="refreshBalances" :loading="refreshingBalances">
              <el-icon><Refresh /></el-icon>
              刷新余额
            </el-button>
            <el-button type="primary" @click="loadRequests">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 统计信息 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="12">
          <el-statistic title="待处理" :value="statistics.pending" />
        </el-col>
        <el-col :span="12">
          <el-statistic title="本月完成" :value="statistics.month" />
        </el-col>
      </el-row>
      
      <el-table :data="requests" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="elderName" label="老人姓名" />
        <el-table-column prop="content" label="代购内容" />
        <el-table-column prop="accountBalance" label="当前余额" width="120">
          <template #default="{ row }">
            <span v-if="row.accountBalance !== undefined" style="color: #409eff; font-weight: bold;">
              ¥{{ row.accountBalance }}
            </span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="actualAmount" label="实际金额" width="120">
          <template #default="{ row }">
            <span v-if="row.actualAmount" style="color: #67c23a; font-weight: bold;">
              ¥{{ row.actualAmount }}
            </span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 'pending'"
              size="small" 
              type="success" 
              @click="confirmPurchase(row)"
            >
              确认
            </el-button>
            <el-button 
              v-if="row.status === 'pending'"
              size="small" 
              type="danger" 
              @click="rejectPurchase(row)"
            >
              拒绝
            </el-button>
            <el-button 
              size="small" 
              @click="viewDetails(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 确认代购对话框 -->
    <el-dialog v-model="showConfirmDialog" title="确认代购" width="500px">
      <el-form :model="confirmForm" :rules="rules" ref="formRef">
        <el-form-item label="代购内容">
          <el-input v-model="confirmForm.content" readonly />
        </el-form-item>
        <el-form-item label="账户余额">
          <el-input v-model="confirmForm.accountBalance" readonly>
            <template #suffix>
              <span style="color: #67c23a; font-weight: bold;">元</span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="实际金额" prop="actualAmount">
          <el-input-number 
            v-model="confirmForm.actualAmount" 
            :precision="2"
            :min="0"
            placeholder="请输入实际花费金额"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="confirmForm.remark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入备注信息（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showConfirmDialog = false">取消</el-button>
        <el-button type="primary" @click="submitConfirm" :loading="loading">
          确认
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailsDialog" title="代购详情" width="600px">
      <div v-if="selectedRequest" class="details-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ selectedRequest.id }}</el-descriptions-item>
          <el-descriptions-item label="老人姓名">{{ selectedRequest.elderName }}</el-descriptions-item>
          <el-descriptions-item label="代购内容">{{ selectedRequest.content }}</el-descriptions-item>
          <el-descriptions-item label="实际金额">¥{{ selectedRequest.actualAmount || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedRequest.status)">
              {{ getStatusText(selectedRequest.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDateTime(selectedRequest.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDateTime(selectedRequest.updatedAt) }}</el-descriptions-item>
          <el-descriptions-item label="备注" span="2">
            {{ selectedRequest.remark || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showDetailsDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { purchaseApi, accountApi } from '@/api'

const requests = ref([])
const showConfirmDialog = ref(false)
const showDetailsDialog = ref(false)
const loading = ref(false)
const refreshingBalances = ref(false) // 刷新余额的加载状态
const statusFilter = ref('') // 状态筛选
const selectedRequest = ref(null) // 当前选中的代购请求

const formRef = ref()

const statistics = reactive({
  pending: 0,
  month: 0
})

const confirmForm = reactive({
  id: null,
  content: '',
  accountBalance: null,
  actualAmount: null,
  remark: ''
})

const rules = {
  actualAmount: [
    { required: true, message: '请输入实际金额', trigger: 'blur' },
    { type: 'number', min: 0, message: '金额必须大于等于0', trigger: 'blur' }
  ]
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

const getStatusType = (status) => {
  switch (status) {
    case 'pending': return 'warning'
    case 'confirmed': return 'success'
    case 'rejected': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'pending': return '待处理'
    case 'confirmed': return '已确认'
    case 'rejected': return '已拒绝'
    default: return '未知'
  }
}

const loadRequests = async () => {
  try {
    let response;
    
    // 根据筛选条件选择API
    if (statusFilter.value === '') {
      // 获取所有请求
      response = await purchaseApi.getAllRequests()
    } else if (statusFilter.value === 'pending') {
      // 只获取待处理请求
      response = await purchaseApi.getPendingRequests()
    } else {
      // 获取所有请求，然后在前端筛选
      const allResponse = await purchaseApi.getAllRequests()
      response = allResponse.filter(item => item.status === statusFilter.value)
    }
    
    // 为每个请求添加账户余额信息
    if (response && response.length > 0) {
      for (const request of response) {
        try {
          console.log(`开始获取老人${request.elderId}的余额...`)
          const balanceResponse = await accountApi.getBalance(request.elderId)
          console.log(`老人${request.elderId}余额API完整响应:`, balanceResponse)
          
          if (balanceResponse !== undefined && balanceResponse !== null) {
            request.accountBalance = balanceResponse
            console.log(`老人${request.elderId}余额设置成功:`, balanceResponse)
          } else {
            request.accountBalance = 0
            console.log(`老人${request.elderId}余额响应为空，设置为0`)
          }
        } catch (error) {
          console.error(`获取老人${request.elderId}余额失败:`, error)
          console.error('错误详情:', {
            response: error.response?.data,
            status: error.response?.status,
            message: error.message
          })
          request.accountBalance = 0
        }
      }
    }
    
    requests.value = response || []
    
    // 加载统计信息
    const statsResponse = await purchaseApi.getStatistics()
    const stats = statsResponse || {}
    statistics.pending = stats.pending || 0
    statistics.month = stats.month || 0
    
  } catch (error) {
    console.error('加载代购请求失败:', error)
    ElMessage.error('加载代购请求失败')
  }
}

const confirmPurchase = async (request) => {
  Object.assign(confirmForm, request)
  
  // 获取老人账户余额
  try {
    const balanceResponse = await accountApi.getBalance(request.elderId)
    // API直接返回余额数值，不是对象
    confirmForm.accountBalance = balanceResponse || 0
  } catch (error) {
    console.error('获取账户余额失败:', error)
    confirmForm.accountBalance = 0
  }
  
  showConfirmDialog.value = true
}

const rejectPurchase = async (request) => {
  try {
    await ElMessageBox.confirm('确定要拒绝这个代购请求吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await purchaseApi.rejectPurchase(request.id, '商品不可用')
    ElMessage.success('已拒绝代购请求')
    loadRequests()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('拒绝失败')
    }
  }
}

const submitConfirm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    // 验证实际金额
    if (!confirmForm.actualAmount || confirmForm.actualAmount <= 0) {
      ElMessage.error('请输入有效的实际金额')
      return
    }
    
    // 保存表单数据，避免在API调用过程中被重置
    const formData = {
      actualAmount: Number(confirmForm.actualAmount),
      remark: confirmForm.remark || null
    }
    
    console.log('提交的代购确认数据:', {
      id: confirmForm.id,
      data: formData,
      remarkValue: confirmForm.remark,
      remarkType: typeof confirmForm.remark
    })
    
    // 调试API调用
    try {
      const response = await purchaseApi.confirmPurchase(confirmForm.id, formData)
      console.log('代购确认API响应:', response)
    } catch (error) {
      console.error('代购确认API调用失败:', error)
      console.error('错误详情:', {
        response: error.response?.data,
        status: error.response?.status,
        message: error.message
      })
      throw error
    }
    
    ElMessage.success('代购确认成功')
    showConfirmDialog.value = false
    
    // 重置表单
    Object.assign(confirmForm, { 
      id: null, 
      content: '', 
      accountBalance: null, 
      actualAmount: null, 
      remark: '' 
    })
    
    loadRequests()
  } catch (error) {
    console.error('代购确认失败:', error)
    console.error('错误详情:', {
      response: error.response?.data,
      status: error.response?.status,
      message: error.message
    })
    
    // 详细显示后端错误信息
    if (error.response?.data) {
      console.error('后端返回的具体错误:', JSON.stringify(error.response.data, null, 2))
    }
    
    const errorMessage = error.response?.data?.message || 
                      error.response?.data?.error || 
                      error.response?.data?.details ||
                      error.message || 
                      '代购确认失败'
    
    // 针对余额不足提供更友好的提示
    if (errorMessage.includes('余额不足')) {
      ElMessage.error('老人账户余额不足，请先充值后再确认代购')
    } else {
      ElMessage.error(errorMessage)
    }
  } finally {
    loading.value = false
  }
}

const viewDetails = (request) => {
  selectedRequest.value = request
  showDetailsDialog.value = true
}

// 刷新所有余额
const refreshBalances = async () => {
  try {
    refreshingBalances.value = true
    ElMessage.info('正在刷新所有老人余额...')
    
    // 重新加载代购请求，这会触发余额的重新获取
    await loadRequests()
    
    ElMessage.success('余额刷新完成')
  } catch (error) {
    console.error('刷新余额失败:', error)
    ElMessage.error('刷新余额失败')
  } finally {
    refreshingBalances.value = false
  }
}

onMounted(() => {
  loadRequests()
})

// 监听状态筛选变化
watch(statusFilter, () => {
  loadRequests()
})
</script>

<style scoped>
.purchase-container {
  padding: 20px;
  overflow-x: hidden !important;
  overflow-y: auto;
  max-width: 100%;
  box-sizing: border-box;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
}

/* 强制禁止所有组件的水平滚动 */
* {
  overflow-x: hidden !important;
}

.el-table {
  overflow-x: hidden !important;
}

/* 详情对话框样式 */
.details-content {
  padding: 10px 0;
}

.details-content .el-descriptions {
  margin-bottom: 0;
}

.details-content .el-descriptions-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.details-content .el-descriptions-item:last-child {
  border-bottom: none;
}

.details-content .el-descriptions-item__label {
  font-weight: 600;
  color: #606266;
  width: 120px;
}

.details-content .el-descriptions-item__content {
  color: #303133;
  font-size: 14px;
}



.el-card {
  overflow-x: hidden !important;
  max-width: 100%;
}

.el-dialog {
  overflow-x: hidden !important;
}
</style>
