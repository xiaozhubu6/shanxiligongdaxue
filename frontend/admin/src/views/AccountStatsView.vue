<template>
  <div class="account-stats-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>老人账户统计</span>
          <!-- 筛选器 -->
          <div class="filter-section" style="margin-bottom: 20px;">
            <el-select v-model="timeFilter" placeholder="选择时间范围" style="width: 200px;">
              <el-option label="全部" value="" />
              <el-option label="今日" value="today" />
              <el-option label="本周" value="current_week" />
              <el-option label="本月" value="current_month" />
              <el-option label="上月" value="last_month" />
              <el-option label="本年" value="current_year" />
              <el-option label="去年" value="last_year" />
            </el-select>
          </div>
        </div>
      </template>
      
      <!-- 统计信息 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="8">
          <el-statistic title="老人总数" :value="statistics.totalElders" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="余额不足" :value="statistics.lowBalanceCount" />
        </el-col>
        <el-col :span="8">
          <el-button type="primary" @click="loadAccountStats" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </el-col>
      </el-row>
      
      <!-- 账户列表 -->
      <el-table :data="accountStats" style="width: 100%" :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold' }">
        <el-table-column prop="elderName" label="老人姓名" min-width="120" />
        <el-table-column prop="balance" label="账户余额" min-width="100">
          <template #default="{ row }">
            <span :style="{ color: row.balance < 50 ? '#f56c6c' : '#67c23a' }">
              ¥{{ row.balance.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="totalExpense" label="总消费" min-width="100">
          <template #default="{ row }">
            ¥{{ row.totalExpense.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastRecharge" label="最后充值" min-width="140">
          <template #default="{ row }">
            {{ formatDateTime(row.lastRecharge) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="160">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="primary" 
              @click="handleRecharge(row)"
            >
              充值
            </el-button>
            <el-button 
              size="small" 
              @click="viewBills(row)"
            >
              查看账单
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 账单详情对话框 -->
    <el-dialog v-model="showBillsDialog" :title="`${selectedElder?.elderName} - 账单详情`" width="80%">
      <div class="bills-content">
        <!-- 时间筛选 -->
        <div class="bills-filter" style="margin-bottom: 20px;">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-date-picker
                v-model="billDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-col>
            <el-col :span="8">
              <el-select v-model="billTypeFilter" placeholder="账单类型" style="width: 100%;">
                <el-option label="全部" value="" />
                <el-option label="代购消费" value="代购消费" />
                <el-option label="账户充值" value="recharge" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-button type="primary" @click="loadBills">查询</el-button>
              <el-button type="success" @click="exportBillsExcel">导出Excel</el-button>
            </el-col>
          </el-row>
        </div>
        
        <!-- 账单列表 -->
        <el-table :data="bills" style="width: 100%">
          <el-table-column prop="createdAt" label="时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="billType" label="类型" width="120">
            <template #default="{ row }">
              <el-tag :type="row.billType === 'recharge' ? 'success' : 'danger'">
                {{ row.billType === 'recharge' ? '充值' : '消费' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="详情">
            <template #default="{ row }">
              {{ row.billType === 'recharge' ? '账户充值' : row.description }}
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="{ row }">
              <span :style="{ 
                color: row.billType === 'recharge' ? '#67c23a' : '#f56c6c',
                fontWeight: 'bold'
              }">
                {{ row.billType === 'recharge' ? '+' : '-' }}¥{{ row.amount.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 汇总信息 -->
        <div class="bills-summary" style="margin-top: 20px; padding: 20px; background: #f5f7fa; border-radius: 8px;">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="summary-item">
                <span class="label">一共充值金额：</span>
                <span class="value income">+¥{{ billsSummary.totalRecharge.toFixed(2) }}</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <span class="label">消费金额：</span>
                <span class="value expense">-¥{{ billsSummary.totalExpense.toFixed(2) }}</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <span class="label">账户余额：</span>
                <span class="value" :style="{ color: billsSummary.accountBalance >= 0 ? '#67c23a' : '#f56c6c' }">
                  ¥{{ billsSummary.accountBalance.toFixed(2) }}
                </span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <span class="label">余额验证：</span>
                <span class="value" :style="{ color: billsSummary.balanceCorrect ? '#67c23a' : '#f56c6c' }">
                  {{ billsSummary.balanceCorrect ? '✓ 正确' : '✗ 错误' }}
                </span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showBillsDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { accountApi, billApi } from '@/api'
import * as XLSX from 'xlsx'

const accountStats = ref([])
const showBillsDialog = ref(false)
const selectedElder = ref(null)
const bills = ref([])
const timeFilter = ref('')

// 账单筛选
const billDateRange = ref([])
const billTypeFilter = ref('')

const statistics = reactive({
  totalElders: 0,
  lowBalanceCount: 0
})

const billsSummary = reactive({
  totalIncome: 0,
  totalExpense: 0,
  totalRecharge: 0,
  accountBalance: 0,
  balanceCorrect: true
})

// 格式化日期时间
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

// 加载账户统计
const loadAccountStats = async () => {
  try {
    const response = await accountApi.getAccountStats(timeFilter.value)
    accountStats.value = response || []
    
    // 计算统计信息
    statistics.totalElders = accountStats.value.length
    statistics.lowBalanceCount = accountStats.value.filter(item => item.balance < 50).length
    
  } catch (error) {
    console.error('加载账户统计失败:', error)
    ElMessage.error('加载账户统计失败')
  }
}

// 查看账单
const viewBills = async (elder) => {
  selectedElder.value = elder
  showBillsDialog.value = true
  await loadBills()
}

// 加载账单
const loadBills = async () => {
  if (!selectedElder.value) return
  
  try {
    const params = {
      elderId: selectedElder.value.elderId,
      startDate: billDateRange.value?.[0] || '',
      endDate: billDateRange.value?.[1] || '',
      billType: billTypeFilter.value
    }
    
    const response = await billApi.getBillsByFilter(params)
    bills.value = response || []
    
    // 计算汇总
    billsSummary.totalIncome = bills.value
      .filter(bill => bill.billType === 'recharge')
      .reduce((sum, bill) => sum + bill.amount, 0)
    billsSummary.totalExpense = bills.value
      .filter(bill => bill.billType !== 'recharge')
      .reduce((sum, bill) => sum + bill.amount, 0)
    
    // 使用真实的账户余额和充值金额
    const currentElder = accountStats.value.find(account => account.elderId === selectedElder.value.elderId)
    billsSummary.accountBalance = currentElder ? currentElder.balance : 0
    billsSummary.totalRecharge = currentElder ? currentElder.totalRecharge : 0
    
    // 验证余额计算是否正确
    const calculatedBalance = billsSummary.totalRecharge - billsSummary.totalExpense
    billsSummary.balanceCorrect = Math.abs(calculatedBalance - billsSummary.accountBalance) < 0.01
    
  } catch (error) {
    console.error('加载账单失败:', error)
    ElMessage.error('加载账单失败')
  }
}

// 充值处理
const handleRecharge = async (elder) => {
  try {
    const { value: amount } = await ElMessageBox.prompt(
      `请输入为${elder.elderName}的充值金额（元）：`,
      '账户充值',
      {
        confirmButtonText: '确认充值',
        cancelButtonText: '取消',
        inputPattern: /^\d+(\.\d{1,2})?$/,
        inputErrorMessage: '请输入有效的金额',
        inputPlaceholder: '请输入充值金额'
      }
    )
    
    if (!amount || parseFloat(amount) <= 0) {
      ElMessage.warning('请输入有效的充值金额')
      return
    }
    
    await ElMessageBox.confirm(
      `确认给${elder.elderName}充值¥${amount}？`,
      '充值确认',
      {
        confirmButtonText: '确认充值',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用充值API
    await accountApi.recharge(elder.elderId, parseFloat(amount))
    ElMessage.success(`已为${elder.elderName}充值¥${amount}`)
    
    // 重新加载数据
    await loadAccountStats()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('充值失败:', error)
      ElMessage.error('充值失败')
    }
  }
}

// 导出Excel
const exportBillsExcel = () => {
  if (!selectedElder.value || bills.value.length === 0) {
    ElMessage.warning('没有可导出的账单数据')
    return
  }
  
  try {
    // 准备Excel数据
    const exportData = bills.value.map(bill => ({
      '时间': formatDateTime(bill.createdAt),
      '类型': bill.billType === 'recharge' ? '充值' : '消费',
      '详情': bill.billType === 'recharge' ? '账户充值' : bill.description,
      '金额': bill.billType === 'recharge' ? bill.amount : -bill.amount
    }))
    
    // 创建工作簿
    const ws = XLSX.utils.json_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    
    // 添加工作表到工作簿
    XLSX.utils.book_append_sheet(wb, ws, '账单明细')
    
    // 生成Excel文件
    const excelBuffer = XLSX.write(wb, { bookType: 'xlsx', type: 'buffer' })
    
    // 创建Blob并下载
    const blob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${selectedElder.value.elderName}_账单_${billDateRange.value?.[0] || '全部'}_${billDateRange.value?.[1] || '全部'}.xlsx`
    link.click()
    URL.revokeObjectURL(url)
    
    ElMessage.success('账单Excel已生成并下载')
    
  } catch (error) {
    console.error('导出Excel失败:', error)
    ElMessage.error('导出Excel失败')
  }
}

onMounted(() => {
  loadAccountStats()
})
</script>

<style scoped>
.account-stats-container {
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

.bills-content {
  max-height: 60vh;
  overflow-y: auto;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.summary-item .label {
  color: #666;
}

.summary-item .value.income {
  color: #67c23a;
}

.summary-item .value.expense {
  color: #f56c6c;
}

/* 打印样式 */
@media print {
  .no-print {
    display: none !important;
  }
  
  .bills-content {
    max-height: none !important;
  }
  
  .el-dialog__wrapper {
    position: static !important;
  }
  
  .el-dialog {
    position: static !important;
    margin: 0 !important;
    box-shadow: none !important;
  }
}

/* 强制禁止所有组件的水平滚动 */
* {
  overflow-x: hidden !important;
}

.el-table {
  overflow-x: hidden !important;
  max-width: 100%;
}

.el-card {
  overflow-x: hidden !important;
  max-width: 100%;
}

.el-dialog {
  overflow-x: hidden !important;
}

/* 打印样式 */
@media print {
  .service-review {
    padding: 0;
    background: white;
  }
  
  .card-header,
  .header-actions,
  .filter-section {
    display: none !important;
  }
  
  .bills-content {
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
  
  .bills-summary {
    margin-top: 20px;
    padding: 20px;
    background: #f5f7fa;
    border: 1px solid #ddd;
    border-radius: 4px;
  }
  
  .summary-item {
    margin-bottom: 10px;
    font-size: 14px;
  }
  
  .label {
    font-weight: bold;
    color: #303133;
  }
  
  .value {
    color: #606266;
  }
  
  .value.income {
    color: #67c23a;
    font-weight: bold;
  }
  
  .value.expense {
    color: #f56c6c;
    font-weight: bold;
  }
  
  /* 打印标题 */
  .bills-content::before {
    content: "账单明细";
    display: block;
    font-size: 20px;
    font-weight: bold;
    text-align: center;
    margin-bottom: 20px;
    color: #303133;
  }
  
  /* 打印时间范围 */
  .bills-content::after {
    content: attr(data-print-range);
    display: block;
    font-size: 14px;
    text-align: center;
    margin-bottom: 20px;
    color: #606266;
  }
}
</style>
