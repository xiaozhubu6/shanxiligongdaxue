<template>
  <div class="bill-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>账单查看</span>
          <div class="header-actions">
            <el-button type="primary" @click="loadBills">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button type="success" @click="exportBills" :loading="exportLoading">
              <el-icon><Download /></el-icon>
              导出账单
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
      
      <!-- 账户概览 -->
      <div class="account-overview" v-if="selectedElder">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="当前余额" :value="selectedElder.balance || 0" suffix="元" />
            <el-button type="text" size="small" @click="refreshBalance" :loading="refreshingBalance">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </el-col>
          <el-col :span="6">
            <el-statistic title="本月支出" :value="monthlyExpense" suffix="元" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="本月充值" :value="monthlyRecharge" suffix="元" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="账单总数" :value="bills.length" suffix="笔" />
          </el-col>
        </el-row>
      </div>
      
      <!-- 账单列表 -->
      <div class="bill-list" v-if="selectedElder">
        <el-table :data="filteredBills" style="width: 100%" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="账单号" width="180" />
          <el-table-column prop="billType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getBillTypeColor(row.billType)">
                {{ getBillTypeText(row.billType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="{ row }">
              <span :class="getAmountClass(row.billType)">
                {{ (row.billType === 'recharge' || row.billType === 'RECHARGE') ? '+' : '-' }}¥{{ row.amount }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="paymentMethod" label="支付方式" width="120">
            <template #default="{ row }">
              {{ getPaymentMethodText(row.paymentMethod) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'warning'">
                {{ row.status === 'SUCCESS' ? '成功' : (row.status || '完成') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
      
      <!-- 无老人选择提示 -->
      <div v-else class="no-elder-selected">
        <el-empty description="请选择要查看账单的老人" />
      </div>
    </el-card>

    <!-- 账单详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="账单详情" width="600px">
      <div class="bill-detail" v-if="selectedBill">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="账单号">{{ selectedBill.id }}</el-descriptions-item>
          <el-descriptions-item label="账单类型">
            <el-tag :type="getBillTypeColor(selectedBill.billType)">
              {{ getBillTypeText(selectedBill.billType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="描述">{{ selectedBill.description }}</el-descriptions-item>
          <el-descriptions-item label="金额">
            <span :class="getAmountClass(selectedBill.billType)">
              {{ (selectedBill.billType === 'recharge' || selectedBill.billType === 'RECHARGE') ? '+' : '-' }}¥{{ selectedBill.amount }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ getPaymentMethodText(selectedBill.paymentMethod) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedBill.status === 'SUCCESS' ? 'success' : 'warning'">
              {{ selectedBill.status === 'SUCCESS' ? '成功' : '处理中' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(selectedBill.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ selectedBill.completedAt ? formatDateTime(selectedBill.completedAt) : '未完成' }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 消费明细 -->
        <div class="bill-items" v-if="selectedBill.items && selectedBill.items.length > 0">
          <h4>消费明细</h4>
          <el-table :data="selectedBill.items" style="width: 100%">
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="unitPrice" label="单价" width="100">
              <template #default="{ row }">
                ¥{{ row.unitPrice }}
              </template>
            </el-table-column>
            <el-table-column prop="totalPrice" label="小计" width="100">
              <template #default="{ row }">
                ¥{{ row.totalPrice }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Download } from '@element-plus/icons-vue'
import { elderApi, accountApi } from '@/api'
import * as XLSX from 'xlsx'

// 响应式数据
const myElders = ref([])
const selectedElderId = ref('')
const selectedElder = ref(null)
const bills = ref([])
const selectedBill = ref(null)
const selectedBills = ref([])

// 分页
const pagination = ref({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 表单数据
const filterForm = ref({
  billType: '',
  dateRange: []
})

// 对话框控制
const showDetailDialog = ref(false)

// 状态
const exportLoading = ref(false)
const refreshingBalance = ref(false)

// 计算属性
const filteredBills = computed(() => {
  let result = bills.value
  
  // 应用筛选
  if (filterForm.value.billType) {
    result = result.filter(bill => bill.billType === filterForm.value.billType)
  }
  
  if (filterForm.value.dateRange && filterForm.value.dateRange.length === 2) {
    const [startDate, endDate] = filterForm.value.dateRange
    result = result.filter(bill => {
      const billDate = bill.createdAt.split(' ')[0]
      return billDate >= startDate && billDate <= endDate
    })
  }
  
  // 分页
  pagination.value.total = result.length
  const start = (pagination.value.currentPage - 1) * pagination.value.pageSize
  const end = start + pagination.value.pageSize
  return result.slice(start, end)
})

const monthlyExpense = computed(() => {
  if (!selectedElder.value || !bills.value.length) return 0
  
  const currentMonth = new Date().getMonth()
  const currentYear = new Date().getFullYear()
  
  return bills.value
    .filter(bill => {
      if (bill.billType === 'recharge' || bill.billType === 'RECHARGE') return false // 排除充值记录
      const billDate = new Date(bill.createdAt)
      return billDate.getMonth() === currentMonth && billDate.getFullYear() === currentYear
    })
    .reduce((sum, bill) => sum + bill.amount, 0)
    .toFixed(2)
})

const monthlyRecharge = computed(() => {
  if (!selectedElder.value || !bills.value.length) return 0
  
  const currentMonth = new Date().getMonth()
  const currentYear = new Date().getFullYear()
  
  return bills.value
    .filter(bill => {
      if (bill.billType !== 'recharge' && bill.billType !== 'RECHARGE') return false // 只计算充值
      const billDate = new Date(bill.createdAt)
      return billDate.getMonth() === currentMonth && billDate.getFullYear() === currentYear
    })
    .reduce((sum, bill) => sum + bill.amount, 0)
    .toFixed(2)
})

// 计算属性方法
const getBillTypeColor = (type) => {
  const colorMap = {
    'purchase': 'warning',
    'recharge': 'success',
    '代购消费': 'warning',
    'PURCHASE': 'warning',
    'RECHARGE': 'success',
    'SERVICE': 'info'
  }
  return colorMap[type] || 'default'
}

const getBillTypeText = (type) => {
  const textMap = {
    'purchase': '消费',
    'recharge': '充值',
    '代购消费': '消费',
    'PURCHASE': '消费',
    'RECHARGE': '充值',
    'SERVICE': '服务'
  }
  return textMap[type] || '未知'
}

const getPaymentMethodText = (method) => {
  const textMap = {
    'BALANCE': '账户余额',
    'ONLINE': '在线支付',
    'BANK': '银行转账',
    'CASH': '现金'
  }
  return textMap[method] || '账户余额'
}

const getMethodType = (method) => {
  const typeMap = {
    'ONLINE': 'primary',
    'BANK': 'success',
    'CASH': 'warning'
  }
  return typeMap[method] || 'info'
}

const getMethodText = (method) => {
  const textMap = {
    'ONLINE': '在线支付',
    'BANK': '银行转账',
    'CASH': '现金充值'
  }
  return textMap[method] || '未知'
}

// 标准时间格式化方法
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

const getAmountClass = (billType) => {
  return billType === 'recharge' || billType === 'RECHARGE' ? 'amount-income' : 'amount-expense'
}

// 方法
const loadMyElders = async () => {
  try {
    // 从localStorage获取用户信息
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null
    
    if (!user || !user.id) {
      ElMessage.error('用户信息无效，请重新登录')
      return
    }
    
    // 子女端：获取当前登录用户关联的老人
    const eldersResponse = await elderApi.getAssociatedElders(user.id)
    console.log('关联的老人:', eldersResponse)
    
    if (eldersResponse && eldersResponse.length > 0) {
      // 为每个老人获取余额信息
      const eldersWithBalance = await Promise.all(
        eldersResponse.map(async (elder) => {
          const balanceResponse = await accountApi.getBalance(elder.id)
          console.log(`老人 ${elder.name} 的余额API响应:`, balanceResponse)
          
          // 检查响应格式
          let balance = 0
          if (typeof balanceResponse === 'number') {
            balance = balanceResponse
          } else if (balanceResponse && balanceResponse.balance !== undefined) {
            balance = balanceResponse.balance
          } else {
            console.warn('余额API返回格式异常:', balanceResponse)
          }
          
          return {
            ...elder,
            balance: balance
          }
        })
      )
      
      myElders.value = eldersWithBalance
      
      // 默认选择第一个老人
      const firstElder = eldersWithBalance[0]
      selectedElderId.value = firstElder.id
      selectedElder.value = firstElder
      loadBills()
      loadBalance()
    } else {
      ElMessage.warning('未找到关联的老人')
    }
  } catch (error) {
    console.error('加载老人信息失败:', error)
    ElMessage.error('加载老人信息失败')
  }
}

const onElderChange = (elderId) => {
  selectedElder.value = myElders.value.find(e => e.id === elderId)
  loadBills()
  loadBalance()
}

const loadBills = async () => {
  if (!selectedElder.value) return
  
  try {
    const response = await accountApi.getBills(selectedElder.value.id)
    bills.value = response || []
    console.log('加载账单数据:', bills.value)
    
    // 重置分页
    pagination.value.currentPage = 1
  } catch (error) {
    console.error('加载账单失败:', error)
    ElMessage.error('加载账单失败')
  }
}

const refreshBalance = async () => {
  if (!selectedElder.value) return
  
  try {
    refreshingBalance.value = true
    ElMessage.info('正在刷新余额...')
    
    // 强制清除localStorage中的缓存数据
    localStorage.removeItem('user')
    
    // 重新获取用户信息
    const userStr = localStorage.getItem('user')
    if (userStr) {
      const user = JSON.parse(userStr)
      // 清除余额字段
      delete user.balance
      localStorage.setItem('user', JSON.stringify(user))
    }
    
    // 使用专门的余额API获取准确的账户余额
    const response = await accountApi.getBalance(selectedElder.value.id)
    console.log('余额API响应:', response)
    
    // 检查响应格式
    if (typeof response === 'number') {
      selectedElder.value.balance = response
      ElMessage.success(`余额已更新：¥${response}`)
    } else if (response && response.balance !== undefined) {
      selectedElder.value.balance = response.balance
      ElMessage.success(`余额已更新：¥${response.balance}`)
    } else {
      console.warn('余额API返回格式异常:', response)
      ElMessage.warning('余额数据格式异常')
    }
  } catch (error) {
    console.error('加载余额失败:', error)
    ElMessage.error('加载余额失败')
  } finally {
    refreshingBalance.value = false
  }
}

const applyFilter = () => {
  // 筛选逻辑在computed属性中处理
  pagination.value.currentPage = 1
}

const resetFilter = () => {
  filterForm.value = {
    billType: '',
    dateRange: []
  }
  pagination.value.currentPage = 1
}

const handleSelectionChange = (selection) => {
  selectedBills.value = selection
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.currentPage = 1
}

const handleCurrentChange = (page) => {
  pagination.value.currentPage = page
}

const exportBills = async () => {
  if (!selectedElder.value) {
    ElMessage.warning('请先选择老人')
    return
  }
  
  // 如果没有选中账单，导出所有账单
  const billsToExport = selectedBills.value.length > 0 ? selectedBills.value : bills.value
  
  if (billsToExport.length === 0) {
    ElMessage.warning('没有可导出的账单')
    return
  }
  
  try {
    exportLoading.value = true
    
    // 准备Excel数据
    const exportData = billsToExport.map(bill => ({
      '账单号': bill.id,
      '类型': getBillTypeText(bill.billType),
      '描述': bill.description,
      '金额': bill.amount,
      '支付方式': getPaymentMethodText(bill.paymentMethod),
      '状态': bill.status === 'SUCCESS' ? '成功' : '处理中',
      '创建时间': formatDateTime(bill.createdAt)
    }))
    
    // 创建工作簿
    const wb = XLSX.utils.book_new()
    const ws = XLSX.utils.json_to_sheet(exportData)
    
    // 添加工作表到工作簿
    XLSX.utils.book_append_sheet(wb, ws)
    
    // 生成Excel文件
    const excelBuffer = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
    
    // 创建下载链接
    const blob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    
    // 创建下载链接并触发下载
    const link = document.createElement('a')
    link.href = url
    link.download = `${selectedElder.value.name}_账单明细_${new Date().toISOString().slice(0, 10)}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('账单导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请重试')
  } finally {
    exportLoading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadMyElders()
})
</script>

<style scoped>
.bill-view {
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

.account-overview {
  margin: 20px 0;
}

.filter-section {
  margin: 20px 0;
  padding: 20px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.bill-list {
  margin-top: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.no-elder-selected {
  text-align: center;
  padding: 60px 0;
}

.amount-income {
  color: #67c23a;
  font-weight: bold;
}

.amount-expense {
  color: #f56c6c;
  font-weight: bold;
}

/* 打印样式 */
@media print {
  .bill-view {
    padding: 0;
    background: white;
  }
  
  .card-header,
  .header-actions,
  .elder-selector,
  .filter-section,
  .pagination {
    display: none !important;
  }
  
  .account-overview {
    margin: 0;
    padding: 20px;
    background: white;
    border: 1px solid #ddd;
    border-radius: 4px;
  }
  
  .bill-list {
    margin: 0;
    padding: 20px;
    background: white;
  }
  
  .el-table {
    width: 100% !important;
    border: 1px solid #ddd !important;
  }
  
  .el-table th,
  .el-table td {
    border: 1px solid #ddd !important;
    padding: 8px !important;
    font-size: 12px !important;
  }
  
  .el-table th {
    background: #f5f7fa !important;
    font-weight: bold !important;
  }
  
  .el-tag {
    border: 1px solid #ddd !important;
    background: white !important;
    color: #606266 !important;
  }
  
  .el-button {
    display: none !important;
  }
  
  .el-pagination {
    display: none !important;
  }
  
  .el-statistic {
    text-align: center !important;
    margin-bottom: 20px !important;
  }
  
  .el-statistic .head {
    font-size: 14px !important;
    color: #606266 !important;
  }
  
  .el-statistic .content {
    font-size: 18px !important;
    font-weight: bold !important;
    color: #303133 !important;
  }
  
  /* 打印标题 */
  .bill-list::before {
    content: "账单明细";
    display: block;
    font-size: 20px;
    font-weight: bold;
    text-align: center;
    margin-bottom: 20px;
    color: #303133;
  }
  
  /* 打印时间范围 */
  .bill-list::after {
    content: attr(data-print-range);
    display: block;
    font-size: 14px;
    text-align: center;
    margin-bottom: 20px;
    color: #606266;
  }
}

.bill-detail {
  padding: 20px 0;
}

.bill-items {
  margin-top: 20px;
}

.bill-items h4 {
  margin: 0 0 10px 0;
  color: #303133;
}
</style>
