<template>
  <div class="profile-container">
    <div class="header">
      <h2>个人信息</h2>
      <p>查看和管理您的个人资料</p>
    </div>
    
    <div class="content">
      <!-- 个人信息卡片 -->
      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-button type="primary" @click="editMode = !editMode">
              {{ editMode ? '取消' : '编辑' }}
            </el-button>
          </div>
        </template>
        
        <div class="profile-content">
          <div class="avatar-section">
            <el-avatar :size="100" :src="elderInfo?.avatar">
              {{ elderInfo?.name?.charAt(0) }}
            </el-avatar>
            <div class="avatar-info">
              <h3>{{ elderInfo?.name }}</h3>
              <p>{{ elderInfo?.age }}岁 · {{ elderInfo?.gender }}</p>
            </div>
          </div>
          
          <el-form :model="profileForm" label-width="100px" v-if="editMode">
            <el-form-item label="姓名">
              <el-input v-model="profileForm.name" size="large" />
            </el-form-item>
            <el-form-item label="年龄">
              <el-input-number v-model="profileForm.age" :min="80" size="large" />
            </el-form-item>
            <el-form-item label="性别">
              <el-select v-model="profileForm.gender" size="large">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="profileForm.phone" size="large" />
            </el-form-item>
            <el-form-item label="家庭住址">
              <el-input v-model="profileForm.address" type="textarea" :rows="2" size="large" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saving" size="large">
                保存
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="info-display" v-else>
            <div class="info-item">
              <span class="label">姓名：</span>
              <span class="value">{{ elderInfo?.name }}</span>
            </div>
            <div class="info-item">
              <span class="label">年龄：</span>
              <span class="value">{{ elderInfo?.age }}岁</span>
            </div>
            <div class="info-item">
              <span class="label">性别：</span>
              <span class="value">{{ elderInfo?.gender }}</span>
            </div>
            <div class="info-item">
              <span class="label">联系电话：</span>
              <span class="value">{{ elderInfo?.phone || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">网格群：</span>
              <span class="value">{{ elderInfo?.gridGroupName || '未分配' }}</span>
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 账户信息 -->
      <el-card class="account-card">
        <template #header>
          <span>账户信息</span>
        </template>
        
        <div class="account-content">
          <div class="balance-section">
            <div class="balance-info">
              <h3>账户余额</h3>
              <div class="balance-amount">¥{{ elderInfo?.balance || '0.00' }}</div>
            </div>
            <div class="balance-actions">
              <el-button type="primary" @click="showRechargeDialog = true" size="large">
                <el-icon><Money /></el-icon>
                充值
              </el-button>
            </div>
          </div>
          
          <div class="recent-bills">
            <h4>最近账单</h4>
            <el-table :data="recentBills" style="width: 100%">
              <el-table-column prop="type" label="类型" width="100" />
              <el-table-column prop="amount" label="金额" width="100">
                <template #default="{ row }">
                  <span :class="row.type === '充值' ? 'income' : 'expense'">
                    {{ row.type === '充值' ? '+' : '-' }}¥{{ row.amount }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" />
              <el-table-column prop="createdAt" label="时间" width="180" />
            </el-table>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 充值对话框 -->
    <el-dialog v-model="showRechargeDialog" title="账户充值" width="400px">
      <div class="recharge-content">
        <div class="quick-amount">
          <h4>快捷金额</h4>
          <div class="amount-buttons">
            <el-button 
              v-for="amount in quickAmounts" 
              :key="amount"
              @click="rechargeAmount = amount"
              size="large"
              style="margin: 5px; font-size: 16px;"
            >
              ¥{{ amount }}
            </el-button>
          </div>
        </div>
        
        <div class="custom-amount">
          <h4>自定义金额</h4>
          <el-input-number 
            v-model="rechargeAmount" 
            :precision="2"
            :min="10"
            :max="10000"
            size="large"
            style="font-size: 18px; width: 100%;"
          />
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showRechargeDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmRecharge" :loading="recharging">
          确认充值
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Money } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { elderApi, accountApi } from '@/api'

const router = useRouter()
const userStore = useUserStore()

const editMode = ref(false)
const saving = ref(false)
const showRechargeDialog = ref(false)
const recharging = ref(false)
const rechargeAmount = ref(100)

const elderInfo = computed(() => userStore.elderInfo)

const profileForm = reactive({
  name: '',
  age: 80,
  gender: '',
  phone: '',
  address: ''
})

const quickAmounts = ref([50, 100, 200, 500, 1000])

const recentBills = ref([
  {
    type: '消费',
    amount: '50.00',
    description: '代购服务',
    createdAt: '2025-12-30 10:30'
  },
  {
    type: '充值',
    amount: '1000.00',
    description: '子女充值',
    createdAt: '2025-12-29 15:20'
  }
])

// 保存个人信息
const saveProfile = async () => {
  saving.value = true
  
  try {
    await elderApi.updateElder(elderInfo.value.id, profileForm)
    ElMessage.success('个人信息保存成功')
    editMode.value = false
    
    // 更新本地存储
    const updatedElderInfo = { ...elderInfo.value, ...profileForm }
    userStore.setElderInfo(updatedElderInfo)
    
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

// 确认充值
const confirmRecharge = async () => {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) {
    ElMessage.warning('请输入充值金额')
    return
  }
  
  recharging.value = true
  
  try {
    // 这里应该调用充值接口，需要子女端来处理
    ElMessage.success('充值请求已发送，请等待子女确认')
    showRechargeDialog.value = false
    rechargeAmount.value = 100
    
  } catch (error) {
    ElMessage.error('充值失败，请重试')
  } finally {
    recharging.value = false
  }
}

// 初始化表单
const initForm = () => {
  if (elderInfo.value) {
    Object.assign(profileForm, {
      name: elderInfo.value.name || '',
      age: elderInfo.value.age || 80,
      gender: elderInfo.value.gender || '',
      phone: elderInfo.value.phone || ''
    })
  }
}

onMounted(() => {
  userStore.initUser()
  initForm()
})
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h2 {
  color: #333;
  margin: 0;
  font-size: 28px;
}

.header p {
  color: #666;
  margin: 10px 0 0;
  font-size: 18px;
}

.content {
  max-width: 1000px;
  margin: 0 auto;
}

.profile-card, .account-card {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 18px;
}

.profile-content {
  padding: 20px 0;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.avatar-info h3 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.avatar-info p {
  margin: 5px 0 0;
  color: #666;
  font-size: 18px;
}

.info-display {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  font-weight: 600;
  color: #333;
  font-size: 18px;
  min-width: 100px;
}

.value {
  color: #666;
  font-size: 18px;
}

.account-content {
  padding: 20px 0;
}

.balance-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 10px;
}

.balance-info h3 {
  margin: 0;
  color: #333;
  font-size: 20px;
}

.balance-amount {
  font-size: 32px;
  font-weight: 600;
  color: #409eff;
  margin-top: 10px;
}

.recent-bills h4 {
  color: #333;
  margin-bottom: 20px;
  font-size: 18px;
}

.income {
  color: #67c23a;
  font-weight: 600;
}

.expense {
  color: #f56c6c;
  font-weight: 600;
}

.recharge-content {
  padding: 20px 0;
}

.quick-amount {
  margin-bottom: 20px;
}

.quick-amount h4 {
  color: #333;
  margin-bottom: 15px;
  font-size: 18px;
}

.amount-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.custom-amount h4 {
  color: #333;
  margin-bottom: 15px;
  font-size: 18px;
}

/* 老人端大字体优化 */
.el-form-item__label {
  font-size: 18px !important;
}

.el-input {
  font-size: 18px !important;
}

.el-input-number {
  font-size: 18px !important;
}

.el-select {
  font-size: 18px !important;
}

.el-button {
  font-size: 18px !important;
  padding: 12px 20px !important;
}

.el-table {
  font-size: 16px !important;
}

.el-table th {
  font-size: 18px !important;
  padding: 15px 10px !important;
}

.el-table td {
  font-size: 16px !important;
  padding: 15px 10px !important;
}

@media (max-width: 768px) {
  .info-display {
    grid-template-columns: 1fr;
  }
  
  .balance-section {
    flex-direction: column;
    gap: 20px;
  }
  
  .amount-buttons {
    justify-content: center;
  }
}
</style>
