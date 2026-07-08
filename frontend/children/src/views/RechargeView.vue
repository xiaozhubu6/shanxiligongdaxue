<template>
  <div class="recharge-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>账户充值</span>
          <el-button type="primary" @click="loadBalance">
            <el-icon><Refresh /></el-icon>
            刷新余额
          </el-button>
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
      
      <!-- 账户信息 -->
      <div class="account-info" v-if="selectedElder">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="老人姓名">{{ selectedElder.name }}</el-descriptions-item>
          <el-descriptions-item label="当前余额">
            <span class="balance-amount">¥{{ selectedElder.balance || 0 }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="账户状态">
            <el-tag type="success">正常</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 充值表单 -->
      <div class="recharge-form" v-if="selectedElder">
        <h3>快速充值</h3>
        <div class="quick-amounts">
          <el-button 
            v-for="amount in quickAmounts" 
            :key="amount"
            :type="rechargeForm.amount === amount ? 'primary' : 'default'"
            @click="rechargeForm.amount = amount"
          >
            ¥{{ amount }}
          </el-button>
        </div>
        
        <el-form :model="rechargeForm" :rules="rechargeRules" ref="rechargeFormRef" label-width="100px">
          <el-form-item label="充值金额" prop="amount">
            <el-input-number 
              v-model="rechargeForm.amount" 
              :min="1" 
              :max="10000"
              :precision="2"
              style="width: 200px"
              placeholder="请输入充值金额"
            >
              <template #prefix>¥</template>
            </el-input-number>
            <span class="amount-tip">单次充值限额：¥1 - ¥10,000</span>
          </el-form-item>
          
          <el-form-item label="充值方式" prop="method">
            <el-radio-group v-model="rechargeForm.method">
              <el-radio value="ONLINE">在线支付</el-radio>
              <el-radio value="BANK">银行转账</el-radio>
              <el-radio value="CASH">现金充值</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="备注" prop="remark">
            <el-input 
              v-model="rechargeForm.remark" 
              type="textarea" 
              :rows="3"
              placeholder="请输入备注信息（可选）"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="submitRecharge" :loading="rechargeLoading" size="large">
              立即充值
            </el-button>
            <el-button @click="resetForm" size="large">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 充值记录 -->
      <div class="recharge-history" v-if="selectedElder">
        <h3>最近充值记录</h3>
        <el-table :data="rechargeRecords" style="width: 100%">
          <el-table-column prop="id" label="订单号" width="180" />
          <el-table-column prop="amount" label="充值金额" width="120">
            <template #default="{ row }">
              <span class="amount-text">+¥{{ row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="method" label="充值方式" width="100">
            <template #default="{ row }">
              <el-tag :type="getMethodType(row.method)">
                {{ getMethodText(row.method) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'warning'">
                {{ row.status === 'SUCCESS' ? '成功' : '处理中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" />
          <el-table-column prop="createdAt" label="充值时间" width="180" />
        </el-table>
      </div>
      
      <!-- 无老人选择提示 -->
      <div v-else class="no-elder-selected">
        <el-empty description="请选择要充值的老人" />
      </div>
    </el-card>

    <!-- 充值确认对话框 -->
    <el-dialog v-model="showConfirmDialog" title="充值确认" width="400px">
      <div class="confirm-content">
        <div class="confirm-item">
          <span>充值对象：</span>
          <strong>{{ selectedElder?.name }}</strong>
        </div>
        <div class="confirm-item">
          <span>充值金额：</span>
          <strong class="amount-highlight">¥{{ rechargeForm.amount }}</strong>
        </div>
        <div class="confirm-item">
          <span>充值方式：</span>
          <strong>{{ getMethodText(rechargeForm.method) }}</strong>
        </div>
        <div class="confirm-item" v-if="rechargeForm.remark">
          <span>备注：</span>
          <strong>{{ rechargeForm.remark }}</strong>
        </div>
      </div>
      <template #footer>
        <el-button @click="showConfirmDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmRecharge" :loading="rechargeLoading">
          确认充值
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { elderApi, accountApi } from '@/api'

// 响应式数据
const myElders = ref([])
const selectedElderId = ref('')
const selectedElder = ref(null)
const rechargeRecords = ref([])

// 快速充值金额
const quickAmounts = [50, 100, 200, 500, 1000]

// 表单数据
const rechargeForm = ref({
  amount: 100,
  method: 'ONLINE',
  remark: ''
})

// 对话框控制
const showConfirmDialog = ref(false)

// 状态
const rechargeLoading = ref(false)

// 表单引用
const rechargeFormRef = ref()

// 表单验证规则
const rechargeRules = {
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { type: 'number', min: 1, max: 10000, message: '充值金额必须在1-10000之间', trigger: 'blur' }
  ],
  method: [{ required: true, message: '请选择充值方式', trigger: 'change' }]
}

// 计算属性
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
    
    const response = await elderApi.getAssociatedElders(user.id)
    myElders.value = response || []
    
    // 默认选择第一个老人
    if (myElders.value.length > 0) {
      selectedElderId.value = myElders.value[0].id
      selectedElder.value = myElders.value[0]
      loadBalance()
      loadRechargeRecords()
    }
  } catch (error) {
    console.error('加载老人列表失败:', error)
    ElMessage.error('加载老人列表失败')
  }
}

const onElderChange = (elderId) => {
  selectedElder.value = myElders.value.find(e => e.id === elderId)
  loadBalance()
  loadRechargeRecords()
}

const loadBalance = async () => {
  if (!selectedElder.value) return
  
  try {
    // 基于账单计算余额，而不是数据库余额
    const response = await accountApi.getBills(selectedElder.value.id)
    const bills = response || []
    
    // 计算总充值和总消费
    const totalRecharge = bills
      .filter(bill => bill.billType === 'recharge' || bill.billType === 'RECHARGE')
      .reduce((sum, bill) => sum + bill.amount, 0)
    
    const totalExpense = bills
      .filter(bill => bill.billType !== 'recharge' && bill.billType !== 'RECHARGE')
      .reduce((sum, bill) => sum + bill.amount, 0)
    
    const calculatedBalance = totalRecharge - totalExpense
    
    if (selectedElder.value) {
      selectedElder.value.balance = calculatedBalance
    }
  } catch (error) {
    console.error('加载余额失败:', error)
    ElMessage.error('加载余额失败')
  }
}

const loadRechargeRecords = async () => {
  if (!selectedElder.value) return
  
  try {
    const response = await accountApi.getBills(selectedElder.value.id)
    const bills = response || []
    
    // 筛选充值记录
    const rechargeBills = bills
      .filter(bill => bill.billType === 'recharge' || bill.billType === 'RECHARGE')
      .slice(0, 10) // 只显示最近10条
    
    // 添加状态字段
    rechargeRecords.value = rechargeBills.map(bill => ({
      ...bill,
      status: 'SUCCESS', // 充值记录都是成功的
      method: bill.paymentMethod || 'ONLINE' // 默认在线支付
    }))
  } catch (error) {
    console.error('加载充值记录失败:', error)
    ElMessage.error('加载充值记录失败')
  }
}

const submitRecharge = async () => {
  if (!rechargeFormRef.value) return
  
  try {
    await rechargeFormRef.value.validate()
    
    // 显示确认对话框
    showConfirmDialog.value = true
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const confirmRecharge = async () => {
  try {
    rechargeLoading.value = true
    
    // 调用真实的充值API
    const response = await accountApi.recharge(selectedElder.value.id, rechargeForm.value.amount)
    
    if (response) {
      ElMessage.success('充值成功！')
      showConfirmDialog.value = false
      resetForm()
      
      // 重新加载余额和记录
      loadBalance()
      loadRechargeRecords()
    } else {
      ElMessage.error('充值失败，请重试')
    }
  } catch (error) {
    console.error('充值失败:', error)
    ElMessage.error('充值失败，请重试')
  } finally {
    rechargeLoading.value = false
  }
}

const resetForm = () => {
  rechargeForm.value = {
    amount: 100,
    method: 'ONLINE',
    remark: ''
  }
  if (rechargeFormRef.value) {
    rechargeFormRef.value.resetFields()
  }
}

// 生命周期
onMounted(() => {
  loadMyElders()
})
</script>

<style scoped>
.recharge-view {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.elder-selector {
  margin: 20px 0;
}

.account-info {
  margin: 20px 0;
}

.balance-amount {
  font-size: 18px;
  font-weight: bold;
  color: #67c23a;
}

.recharge-form {
  margin: 30px 0;
  padding: 20px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.recharge-form h3 {
  margin: 0 0 20px 0;
  color: #303133;
}

.quick-amounts {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.amount-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.recharge-history {
  margin: 30px 0;
}

.recharge-history h3 {
  margin: 0 0 15px 0;
  color: #303133;
}

.amount-text {
  color: #67c23a;
  font-weight: bold;
}

.no-elder-selected {
  text-align: center;
  padding: 60px 0;
}

.confirm-content {
  padding: 20px 0;
}

.confirm-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.confirm-item:last-child {
  border-bottom: none;
}

.amount-highlight {
  color: #67c23a;
  font-size: 18px;
}
</style>
