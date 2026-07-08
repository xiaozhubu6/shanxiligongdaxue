<template>
  <div class="account-contract">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>账号与签约管理</span>
        </div>
      </template>

      <!-- 账号统计 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="8">
          <el-statistic title="老人账号总数" :value="accountStats.elderCount" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="子女账号总数" :value="accountStats.childCount" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="已签约关系" :value="accountStats.contractCount" />
        </el-col>
      </el-row>

      <!-- 选项卡 -->
      <el-tabs v-model="activeTab">
        <!-- 老人账号管理 -->
        <el-tab-pane label="老人账号" name="elder">
          <div class="account-section">
            <div class="section-header">
              <span>老人账号列表</span>
              <div class="header-actions">
                <el-button type="primary" @click="showAddElderDialog = true">
                  <el-icon><Plus /></el-icon>
                  增加老人账号
                </el-button>
                <el-button size="small" @click="loadElderAccounts">
                  刷新
                </el-button>
              </div>
            </div>

            <el-table
              :data="elderAccounts"
              style="width: 100%"
              :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold' }"
              :row-style="{ height: '70px' }"
              :cell-style="{ padding: '12px 8px' }"
              empty-text="暂无老人账号数据"
            >
              <el-table-column prop="id" label="ID" width="70" align="center">
                <template #default="{ row }">
                  <el-tag type="primary" size="small" effect="plain">#{{ row.id }}</el-tag>
                </template>
              </el-table-column>

              <el-table-column prop="name" label="姓名" min-width="100" align="center">
                <template #default="{ row }">
                  <div class="user-info">
                    <el-avatar :size="28" :style="{ backgroundColor: getAvatarColor(row.name) }">
                      {{ row.name.charAt(0) }}
                    </el-avatar>
                    <span class="user-name">{{ row.name }}</span>
                  </div>
                </template>
              </el-table-column>

              <el-table-column prop="age" label="年龄" width="70" align="center">
                <template #default="{ row }">
                  <el-tag :type="getAgeTagType(row.age)" size="small">{{ row.age }}岁</el-tag>
                </template>
              </el-table-column>

              <el-table-column prop="gender" label="性别" width="70" align="center">
                <template #default="{ row }">
                  <div class="gender-display">
                    <el-icon>
                      <component :is="row.gender === '男' ? 'Male' : 'Female'" />
                    </el-icon>
                    <span>{{ row.gender }}</span>
                  </div>
                </template>
              </el-table-column>

              <el-table-column prop="phone" label="手机号" width="150" align="center">
                <template #default="{ row }">
                  <div class="phone-info">
                    <el-icon><Phone /></el-icon>
                    <span>{{ row.phone || '未设置' }}</span>
                  </div>
                </template>
              </el-table-column>

              <el-table-column prop="accountInfo" label="账号信息" min-width="250" align="center">
                <template #default="{ row }">
                  <div class="account-card">
                    <div class="account-header">
                      <el-icon class="account-icon"><User /></el-icon>
                      <span class="account-title">登录账号</span>
                    </div>
                    <div class="account-content">
                      <div class="account-field">
                        <span class="field-label">账号:</span>
                        <span class="field-value">
                          <el-icon><Key /></el-icon>
                          elder{{ row.id }}
                        </span>
                      </div>
                      <div class="account-field">
                        <span class="field-label">密码:</span>
                        <span class="field-value">
                          <el-icon><Lock /></el-icon>
                          123456
                        </span>
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="操作" width="220" align="center" fixed="right">
                <template #default="{ row }">
                  <div class="action-buttons">
                    <el-button size="small" type="info" effect="plain" :icon="View" @click="viewElderDetail(row)">
                      查看详情
                    </el-button>
                    <el-button size="small" type="primary" effect="plain" :icon="Edit" @click="editElderAccount(row)">
                      修改
                    </el-button>
                    <el-popconfirm
                      title="确定删除该老人账号吗？"
                      description="删除后将无法恢复，请谨慎操作"
                      @confirm="deleteElderAccount(row)"
                      width="200"
                    >
                      <template #reference>
                        <el-button size="small" type="danger" effect="plain" :icon="Delete">
                          删除
                        </el-button>
                      </template>
                    </el-popconfirm>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 子女账号管理 -->
        <el-tab-pane label="子女账号" name="child">
          <div class="account-section">
            <div class="section-header">
              <span>子女账号列表</span>
              <div class="header-actions">
                <el-button type="primary" @click="showAddChildDialog = true">
                  <el-icon><Plus /></el-icon>
                  增加子女账号
                </el-button>
                <el-button size="small" @click="loadChildAccounts">
                  刷新
                </el-button>
              </div>
            </div>

            <el-table
              :data="childAccounts"
              style="width: 100%"
              :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold' }"
              :row-style="{ height: '60px' }"
              empty-text="暂无子女账号数据"
            >
              <el-table-column prop="id" label="ID" width="80" align="center">
                <template #default="{ row }">
                  <el-tag type="primary" size="small" effect="plain">#{{ row.id }}</el-tag>
                </template>
              </el-table-column>

              <el-table-column prop="name" label="姓名" width="120" align="center">
                <template #default="{ row }">
                  <div class="user-info">
                    <el-avatar :size="32" :style="{ backgroundColor: getAvatarColor(row.name) }">
                      {{ row.name.charAt(0) }}
                    </el-avatar>
                    <span class="user-name">{{ row.name }}</span>
                  </div>
                </template>
              </el-table-column>

              <el-table-column prop="phone" label="手机号" width="150" align="center">
                <template #default="{ row }">
                  <div class="phone-info">
                    <el-icon><Phone /></el-icon>
                    <span>{{ row.phone || '未设置' }}</span>
                  </div>
                </template>
              </el-table-column>

              <el-table-column prop="accountInfo" label="账号信息" min-width="250" align="center">
                <template #default="{ row }">
                  <div class="account-card">
                    <div class="account-header">
                      <el-icon class="account-icon"><User /></el-icon>
                      <span class="account-title">登录账号</span>
                    </div>
                    <div class="account-content">
                      <div class="account-field">
                        <span class="field-label">账号:</span>
                        <span class="field-value">
                          <el-icon><Key /></el-icon>
                          child{{ row.id }}
                        </span>
                      </div>
                      <div class="account-field">
                        <span class="field-label">密码:</span>
                        <span class="field-value">
                          <el-icon><Lock /></el-icon>
                          123456
                        </span>
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column prop="createdAt" label="创建时间" width="160" align="center">
                <template #default="{ row }">
                  <div class="time-info">
                    <el-icon><Clock /></el-icon>
                    <span>{{ formatDate(row.createdAt) }}</span>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="绑定老人" width="200" align="center">
                <template #default="{ row }">
                  <div class="bound-elders-display">
                    <el-tag
                      v-for="elder in row.boundElders"
                      :key="elder.id"
                      size="small"
                      type="primary"
                      effect="light"
                      style="margin-right: 5px; margin-bottom: 3px;"
                    >
                      <el-icon><User /></el-icon>
                      {{ elder.name }}
                    </el-tag>
                    <el-tag v-if="!row.boundElders || row.boundElders.length === 0" size="small" type="info" effect="plain">
                      未绑定
                    </el-tag>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="操作" width="150" align="center" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" type="primary" effect="plain" :icon="Link" @click="manageBinding(row)">
                    管理绑定
                  </el-button>
                  <el-popconfirm
                    title="确定删除该子女账号吗？"
                    description="删除后将无法恢复，请谨慎操作"
                    @confirm="deleteChildAccount(row)"
                    width="200"
                  >
                    <template #reference>
                      <el-button size="small" type="danger" effect="plain" :icon="Delete">
                        删除
                      </el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 增加老人账号对话框 -->
    <el-dialog v-model="showAddElderDialog" title="增加老人账号" width="600px" @open="handleDialogOpen">
      <el-form :model="elderForm" ref="elderFormRef" :rules="elderRules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="elderForm.name" placeholder="请输入老人姓名" />
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="elderForm.age" :min="60" :max="120" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="elderForm.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="社区" prop="communityId">
          <el-select
            v-model="elderForm.communityId"
            placeholder="请选择社区"
            style="width: 100%"
            @change="handleCommunityChange"
            clearable
          >
            <el-option
              v-for="community in communities"
              :key="community.id"
              :label="community.name"
              :value="community.id"
            >
              <span>{{ community.name }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="网格群" prop="gridGroupId">
          <el-select
            v-model="elderForm.gridGroupId"
            placeholder="请选择网格群"
            style="width: 100%"
            :disabled="!elderForm.communityId"
            @change="handleGridGroupChange"
          >
            <el-option
              v-for="group in gridGroups"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            >
              <span>{{ group.name }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="单元号" prop="unitNumber">
          <el-select
            v-model="elderForm.unitNumber"
            placeholder="请选择单元号"
            style="width: 100%"
            :disabled="!elderForm.gridGroupId"
            clearable
          >
            <el-option
              v-for="group in gridGroups"
              :key="group.id"
              :label="group.unitNumber"
              :value="group.unitNumber"
            >
              <span>{{ group.unitNumber }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="房间号" prop="roomNumber">
          <el-input v-model="elderForm.roomNumber" placeholder="请输入房间号" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="elderForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddElderDialog = false">取消</el-button>
        <el-button type="primary" @click="addElderAccount" :loading="addingElder">确定</el-button>
      </template>
    </el-dialog>

    <!-- 修改老人账号对话框 -->
    <el-dialog v-model="showEditElderDialog" title="修改老人账号" width="600px">
      <el-form :model="editElderForm" ref="editElderFormRef" :rules="elderRules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editElderForm.name" placeholder="请输入老人姓名" />
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="editElderForm.age" :min="60" :max="120" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="editElderForm.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="社区" prop="communityId">
          <el-select
            v-model="editElderForm.communityId"
            placeholder="请选择社区"
            style="width: 100%"
            @change="handleEditCommunityChange"
            clearable
          >
            <el-option
              v-for="community in communities"
              :key="community.id"
              :label="community.name"
              :value="community.id"
            >
              <span>{{ community.name }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="网格群" prop="gridGroupId">
          <el-select
            v-model="editElderForm.gridGroupId"
            placeholder="请选择网格群"
            style="width: 100%"
            :disabled="!editElderForm.communityId"
            @change="handleEditGridGroupChange"
          >
            <el-option
              v-for="group in gridGroups"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            >
              <span>{{ group.name }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="单元号" prop="unitNumber">
          <el-select
            v-model="editElderForm.unitNumber"
            placeholder="请选择单元号"
            style="width: 100%"
            :disabled="!editElderForm.gridGroupId"
            clearable
          >
            <el-option
              v-for="group in gridGroups"
              :key="group.id"
              :label="group.unitNumber"
              :value="group.unitNumber"
            >
              <span>{{ group.unitNumber }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="房间号" prop="roomNumber">
          <el-input v-model="editElderForm.roomNumber" placeholder="请输入房间号" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editElderForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditElderDialog = false">取消</el-button>
        <el-button type="primary" @click="updateElderAccount" :loading="editingElder">确定</el-button>
      </template>
    </el-dialog>

    <!-- 增加子女账号对话框 -->
    <el-dialog v-model="showAddChildDialog" title="增加子女账号" width="500px">
      <el-form :model="childForm" ref="childFormRef" :rules="childRules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="childForm.name" placeholder="请输入子女姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="childForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddChildDialog = false">取消</el-button>
        <el-button type="primary" @click="addChildAccount" :loading="addingChild">确定</el-button>
      </template>
    </el-dialog>

    <!-- 管理绑定对话框 -->
    <el-dialog v-model="showBindingDialog" title="管理绑定关系" width="600px">
      <div class="binding-info">
        <div class="child-info-card">
          <h4>子女信息</h4>
          <p><strong>姓名：</strong>{{ bindingChild.name }}</p>
          <p><strong>电话：</strong>{{ bindingChild.phone || '未设置' }}</p>
          <p><strong>账号：</strong>child{{ bindingChild.id }}</p>
        </div>

        <el-divider />

        <div class="elder-binding-info">
          <h4>当前绑定老人</h4>
          <div class="bound-elders">
            <el-tag v-for="elder in bindingChild.boundElders" :key="elder.id" closable @close="unbindElder(elder.id)" style="margin: 5px;">
              <el-icon><User /></el-icon>
              {{ elder.name }}
            </el-tag>
            <span v-if="!bindingChild.boundElders || bindingChild.boundElders.length === 0" style="color: #999;">未绑定任何老人</span>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="add-binding">
        <h4>添加绑定</h4>
        <el-select v-model="selectedElderId" placeholder="请选择要绑定的老人" style="width: 100%; margin-bottom: 10px;">
          <el-option v-for="elder in availableElders" :key="elder.id" :label="elder.name" :value="elder.id" />
        </el-select>
        <el-button type="primary" @click="bindElder" :disabled="!selectedElderId" :loading="binding">绑定</el-button>
      </div>

      <template #footer>
        <el-button @click="showBindingDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增绑定关系对话框 -->
    <el-dialog v-model="showAddBindingDialog" title="新增绑定关系" width="500px">
      <el-form :model="bindingForm" ref="bindingFormRef" :rules="bindingRules" label-width="100px">
        <el-form-item label="子女" prop="childId">
          <el-select v-model="bindingForm.childId" placeholder="请选择子女" style="width: 100%">
            <el-option v-for="child in childAccounts" :key="child.id" :label="child.name" :value="child.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="老人" prop="elderId">
          <el-select v-model="bindingForm.elderId" placeholder="请选择老人" style="width: 100%">
            <el-option v-for="elder in elderAccounts" :key="elder.id" :label="elder.name" :value="elder.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddBindingDialog = false">取消</el-button>
        <el-button type="primary" @click="addBinding" :loading="addingBinding">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看老人详情对话框 -->
    <el-dialog v-model="showElderDetailDialog" title="老人详细信息" width="600px">
      <div class="elder-detail" v-if="selectedElder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ selectedElder.name }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ selectedElder.age }}岁</el-descriptions-item>
          <el-descriptions-item label="性别">{{ selectedElder.gender }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ selectedElder.phone || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="单元号">{{ selectedElder.unitNumber || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ selectedElder.roomNumber || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="社区">{{ selectedElder.communityName || '未设置' }}</el-descriptions-item>
        </el-descriptions>


        <div v-if="elderChildren.length > 0">
          <div style="font-size: 16px; font-weight: 600; margin: 15px 0;">
            老人子女信息
          </div>

          <el-table :data="elderChildren" style="width: 100%">
            <el-table-column prop="name" label="姓名" width="120" />
            <el-table-column prop="phone" label="手机号" width="150" />
          </el-table>
        </div>
        <div v-else style="text-align: center; color: #999; padding: 20px;">
          暂无绑定子女信息
        </div>
      </div>
      <template #footer>
        <el-button @click="showElderDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, User, Key, Lock, OfficeBuilding, Grid, Male, Female, Phone, Clock, Link, Edit, HomeFilled, Message, Location, View } from '@element-plus/icons-vue'
import { elderApi, accountApi, childApi, communityApi } from '@/api'

// 响应式数据
const activeTab = ref('elder')
const loading = ref(false)

// 统计数据
const accountStats = ref({
  elderCount: 0,
  childCount: 0,
  contractCount: 0
})

// 老人账号数据
const elderAccounts = ref([])
const communities = ref([])
const gridGroups = ref([])

// 子女账号数据
const childAccounts = ref([])

// 绑定关系数据
const contracts = ref([])

// 对话框状态
const showAddElderDialog = ref(false)
const showEditElderDialog = ref(false)
const showAddChildDialog = ref(false)
const showBindingDialog = ref(false)
const showAddBindingDialog = ref(false)
const showElderDetailDialog = ref(false)
const selectedElder = ref(null)
const elderChildren = ref([])

// 加载状态
const addingElder = ref(false)
const editingElder = ref(false)
const addingChild = ref(false)
const addingBinding = ref(false)
const binding = ref(false)

// 表单数据
const elderForm = reactive({
  name: '',
  age: 80,
  gender: '男',
  communityId: null,
  gridGroupId: null,
  unitNumber: '',
  roomNumber: '',
  address: '',
  phone: ''
})

const editElderForm = reactive({
  id: null,
  name: '',
  age: 80,
  gender: '男',
  communityId: null,
  gridGroupId: null,
  unitNumber: '',
  roomNumber: '',
  phone: ''
})

const childForm = ref({
  name: '',
  phone: ''
})

const bindingForm = ref({
  childId: null,
  elderId: null
})

const bindingChild = ref({
  id: null,
  name: '',
  boundElders: []
})

const selectedElderId = ref(null)
const availableElders = ref([])

// 表单引用
const elderFormRef = ref()
const editElderFormRef = ref()
const childFormRef = ref()
const bindingFormRef = ref()

// 表单验证规则
const elderRules = {
  name: [{ required: true, message: '请输入老人姓名', trigger: 'blur' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  communityId: [{ required: true, message: '请选择社区', trigger: 'change' }],
  gridGroupId: [{ required: true, message: '请选择网格群', trigger: 'change' }],
  roomNumber: [{ required: true, message: '请输入房间号', trigger: 'blur' }]
}

const childRules = {
  name: [{ required: true, message: '请输入子女姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const bindingRules = {
  childId: [{ required: true, message: '请选择子女', trigger: 'change' }],
  elderId: [{ required: true, message: '请选择老人', trigger: 'change' }]
}

// 加载统计数据
const loadAccountStats = async () => {
  try {
    console.log('老人账号数量:', elderAccounts.value.length)
    console.log('子女账号数量:', childAccounts.value.length)
    console.log('绑定关系数量:', contracts.value.length)

    accountStats.value = {
      elderCount: elderAccounts.value.length,
      childCount: childAccounts.value.length,
      contractCount: contracts.value.length
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载社区数据
const loadCommunities = async () => {
  try {
    const response = await communityApi.getCommunities()
    communities.value = response || []
  } catch (error) {
    console.error('加载社区数据失败:', error)
    communities.value = []
  }
}

// 加载网格群数据
const loadGridGroups = async (communityId = null) => {
  try {
    if (communityId) {
      const response = await communityApi.getGridGroups(communityId)
      gridGroups.value = response || []
    } else {
      gridGroups.value = []
    }
  } catch (error) {
    console.error('加载网格群数据失败:', error)
    gridGroups.value = []
  }
}

// 社区选择变化处理
const handleCommunityChange = (communityId) => {
  elderForm.gridGroupId = null
  elderForm.unitNumber = ''
  loadGridGroups(communityId)
}

// 网格群选择变化处理
const handleGridGroupChange = (gridGroupId) => {
  // 根据选择的网格群自动设置单元号
  const selectedGroup = gridGroups.value.find(g => g.id === gridGroupId)
  if (selectedGroup) {
    elderForm.unitNumber = selectedGroup.unitNumber
  }
}

// 修改老人账号时的社区选择变化处理
const handleEditCommunityChange = (communityId) => {
  editElderForm.gridGroupId = null
  editElderForm.unitNumber = ''
  loadGridGroups(communityId)
}

// 修改老人账号时的网格群选择变化处理
const handleEditGridGroupChange = (gridGroupId) => {
  // 根据选择的网格群自动设置单元号
  const selectedGroup = gridGroups.value.find(g => g.id === gridGroupId)
  if (selectedGroup) {
    editElderForm.unitNumber = selectedGroup.unitNumber
  }
}

// 对话框打开时的处理
const handleDialogOpen = async () => {
  await loadCommunities()
  // 重置表单
  elderForm.gridGroupId = null
  gridGroups.value = []
}

// 加载老人账号
const loadElderAccounts = async () => {
  try {
    loading.value = true

    // 先确保社区数据已加载
    if (communities.value.length === 0) {
      await loadCommunities()
    }

    const response = await elderApi.getAllElders()
    console.log('获取到的老人数据:', response)

    // 为每个老人获取社区和网格群信息
    const eldersWithDetails = await Promise.all(
      response.map(async (elder) => {
        let communityName = '未知社区'
        let gridGroupName = '未知网格群'
        let unitNumber = '未设置'

        try {
          // 根据gridGroupId查找对应的社区
          for (const community of communities.value) {
            const groups = await communityApi.getGridGroups(community.id)
            if (groups.some(g => g.id === elder.gridGroupId)) {
              communityName = community.name
              const group = groups.find(g => g.id === elder.gridGroupId)
              if (group) {
                gridGroupName = group.name
                unitNumber = group.unitNumber || '未设置'
              }
              break
            }
          }
        } catch (error) {
          console.error('获取老人社区网格群信息失败:', error)
        }

        return {
          ...elder,
          communityName,
          gridGroupName,
          unitNumber
        }
      })
    )

    elderAccounts.value = eldersWithDetails
    console.log('处理后的老人数据:', elderAccounts.value)
    await loadAccountStats()
  } catch (error) {
    console.error('加载老人账号失败:', error)
    ElMessage.error('加载老人账号失败')
  } finally {
    loading.value = false
  }
}

// 加载子女账号
const loadChildAccounts = async () => {
  try {
    loading.value = true
    const response = await childApi.getChildren()

    // 为每个子女获取绑定关系
    const childrenWithBinding = await Promise.all(
      response.map(async (child) => {
        let boundElders = []
        try {
          const contracts = await childApi.getContractsByChild(child.id)
          // 新API返回的是elder_child关系，需要获取老人信息
          if (contracts && contracts.length > 0) {
            // 获取每个老人的详细信息
            boundElders = await Promise.all(
              contracts.map(async (contract: any) => {
                try {
                  const elderDetail = await elderApi.getElder(contract.elderId)
                  return {
                    id: contract.elderId,
                    name: elderDetail.name || '未知老人'
                  }
                } catch (error) {
                  return {
                    id: contract.elderId,
                    name: '未知老人'
                  }
                }
              })
            )
          }
        } catch (error) {
          // 忽略错误
        }

        return {
          ...child,
          boundElders
        }
      })
    )

    childAccounts.value = childrenWithBinding
    console.log('处理后的子女数据:', childAccounts.value)
    await loadAccountStats()
  } catch (error) {
    console.error('加载子女账号失败:', error)
    ElMessage.error('加载子女账号失败')
  } finally {
    loading.value = false
  }
}

// 加载绑定关系
const loadContracts = async () => {
  try {
    loading.value = true
    const response = await childApi.getContracts()
    // 新API返回的是elder_child关系数组，需要获取详细信息
    if (response && response.length > 0) {
      const contractsWithDetails = await Promise.all(
        response.map(async (contract: any) => {
          let elderName = '未知老人'
          let childName = '未知子女'
          let childPhone = '未知'

          try {
            // 获取老人详细信息
            const elderDetail = await elderApi.getElder(contract.elderId)
            elderName = elderDetail.name || '未知老人'
          } catch (error) {
            console.warn('获取老人信息失败:', contract.elderId)
          }

          try {
            // 获取子女详细信息
            const childDetail = await childApi.getChild(contract.childId)
            childName = childDetail.name || '未知子女'
            childPhone = childDetail.phone || '未知'
          } catch (error) {
            console.warn('获取子女信息失败:', contract.childId)
          }

          return {
            ...contract,
            elderName,
            childName,
            childPhone
          }
        })
      )
      contracts.value = contractsWithDetails
    } else {
      contracts.value = []
    }
    await loadAccountStats()
  } catch (error) {
    console.error('加载绑定关系失败:', error)
    ElMessage.error('加载绑定关系失败')
  } finally {
    loading.value = false
  }
}

// 增加老人账号
const addElderAccount = async () => {
  try {
    await elderFormRef.value.validate()
    addingElder.value = true

    const elderData = {
      name: elderForm.name || '',
      age: elderForm.age || 80,
      gender: elderForm.gender || '男',
      gridGroupId: elderForm.gridGroupId,
      unitNumber: elderForm.unitNumber || '',
      roomNumber: elderForm.roomNumber || '',
      phone: elderForm.phone || ''
    }

    await elderApi.createElder(elderData)
    ElMessage.success('老人账号创建成功')

    showAddElderDialog.value = false
    elderFormRef.value.resetFields()

    // 重新初始化表单数据
    elderForm.name = ''
    elderForm.age = 80
    elderForm.gender = '男'
    elderForm.communityId = null
    elderForm.gridGroupId = null
    elderForm.unitNumber = ''
    elderForm.roomNumber = ''
    elderForm.address = ''
    elderForm.phone = ''

    // 强制重新加载数据
    await loadElderAccounts()
    await loadCommunities() // 确保社区数据也是最新的
  } catch (error) {
    console.error('创建老人账号失败:', error)
    ElMessage.error('创建老人账号失败: ' + (error.message || '未知错误'))
  } finally {
    addingElder.value = false
  }
}

// 修改老人账号
const editElderAccount = async (elder) => {
  try {
    console.log('开始修改老人账号，老人数据:', elder)

    // 先加载所有社区数据
    await loadCommunities()
    console.log('社区数据加载完成:', communities.value)

    // 根据gridGroupId查找对应的社区
    let foundCommunityId = null
    for (const community of communities.value) {
      const groups = await communityApi.getGridGroups(community.id)
      if (groups.some(g => g.id === elder.gridGroupId)) {
        foundCommunityId = community.id
        console.log('找到对应社区:', community.name, 'ID:', community.id)
        break
      }
    }

    // 加载该社区的网格群
    if (foundCommunityId) {
      await loadGridGroups(foundCommunityId)
      console.log('网格群数据加载完成:', gridGroups.value)
    }

    // 设置表单数据（在网格群加载完成后）
    editElderForm.id = elder.id
    editElderForm.name = elder.name || ''
    editElderForm.age = elder.age || 80
    editElderForm.gender = elder.gender || '男'
    editElderForm.communityId = foundCommunityId
    editElderForm.gridGroupId = Number(elder.gridGroupId) || null
    editElderForm.unitNumber = elder.unitNumber || ''
    editElderForm.roomNumber = ''
    editElderForm.phone = elder.phone || ''

    // 使用nextTick确保响应式更新
    await nextTick()
    console.log('表单数据设置完成:', editElderForm)

  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败，请重试')
  }

  // 显示对话框
  showEditElderDialog.value = true
}

// 更新老人账号
const updateElderAccount = async () => {
  try {
    await editElderFormRef.value.validate()
    editingElder.value = true

    const elderData = {
      name: editElderForm.name,
      age: editElderForm.age,
      gender: editElderForm.gender,
      gridGroupId: editElderForm.gridGroupId,
      unitNumber: editElderForm.unitNumber,
      roomNumber: editElderForm.roomNumber,
      phone: editElderForm.phone || ''
    }

    await elderApi.updateElder(editElderForm.id, elderData)
    ElMessage.success('老人账号修改成功')

    showEditElderDialog.value = false
    editElderFormRef.value.resetFields()

    // 重新初始化修改表单数据
    editElderForm.id = null
    editElderForm.name = ''
    editElderForm.age = 80
    editElderForm.gender = '男'
    editElderForm.communityId = null
    editElderForm.gridGroupId = null
    editElderForm.unitNumber = ''
    editElderForm.roomNumber = ''
    editElderForm.phone = ''
    await loadElderAccounts()
  } catch (error) {
    console.error('修改老人账号失败:', error)
    ElMessage.error('修改老人账号失败')
  } finally {
    editingElder.value = false
  }
}

// 增加子女账号
const addChildAccount = async () => {
  try {
    await childFormRef.value.validate()
    addingChild.value = true

    const childData = {
      name: childForm.value.name,
      phone: childForm.value.phone
    }

    await childApi.createChild(childData)
    ElMessage.success('子女账号创建成功')

    showAddChildDialog.value = false
    childFormRef.value.resetFields()

    // 强制重新加载数据
    await loadChildAccounts()
  } catch (error) {
    console.error('创建子女账号失败:', error)
    ElMessage.error('创建子女账号失败')
  } finally {
    addingChild.value = false
  }
}

// 删除老人账号
const deleteElderAccount = async (elder) => {
  try {
    await ElMessageBox.confirm(
      `确定删除老人账号"${elder.name}"吗？删除后将无法恢复。`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )

    console.log('开始删除老人账号:', elder.id)
    await elderApi.deleteElder(elder.id)
    ElMessage.success('老人账号删除成功')

    // 重新加载数据
    await loadElderAccounts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除老人账号失败:', error)
      ElMessage.error('删除老人账号失败: ' + (error.message || '未知错误'))
    }
  }
}

// 查看老人详情
const viewElderDetail = async (elder) => {
  try {
    // 获取老人的完整详细信息，包括房间号等
    const elderDetail = await elderApi.getElder(elder.id)
    selectedElder.value = elderDetail

    // 获取所有绑定关系，筛选出该老人的绑定关系
    const response = await childApi.getContracts()
    if (response && response.length > 0) {
      // 筛选出该老人的绑定关系
      const elderContracts = response.filter(contract => contract.elderId === elder.id)

      // 获取子女详细信息
      const childrenDetails = []

      for (const contract of elderContracts) {
        try {
          const childResponse = await childApi.getChild(contract.childId)
          if (childResponse) {
            childrenDetails.push({
              name: childResponse.name,
              phone: childResponse.phone
            })
          }
        } catch (error) {
          console.error('获取子女详情失败:', error)
        }
      }

      elderChildren.value = childrenDetails
    } else {
      elderChildren.value = []
    }

    showElderDetailDialog.value = true
  } catch (error) {
    console.error('查看老人详情失败:', error)
    ElMessage.error('查看老人详情失败')
  }
}

// 删除子女账号
const deleteChildAccount = async (child) => {
  try {
    await ElMessageBox.confirm(
      `确定删除子女账号"${child.name}"吗？删除后将无法恢复。`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )

    console.log('开始删除子女账号:', child.id)
    await childApi.deleteChild(child.id)
    ElMessage.success('子女账号删除成功')

    // 重新加载数据
    await loadChildAccounts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除子女账号失败:', error)
      ElMessage.error('删除子女账号失败: ' + (error.message || '未知错误'))
    }
  }
}

// 管理绑定
const manageBinding = async (child) => {
  bindingChild.value = { ...child }

  // 获取可绑定的老人（排除已绑定的）
  availableElders.value = elderAccounts.value.filter(elder =>
    !bindingChild.value.boundElders.some(bound => bound.id === elder.id)
  )

  selectedElderId.value = null
  showBindingDialog.value = true
}

// 绑定老人
const bindElder = async () => {
  try {
    binding.value = true

    await childApi.createContract({
      childId: bindingChild.value.id,
      elderId: selectedElderId.value
    })

    ElMessage.success('绑定成功')

    // 重新加载绑定关系
    await loadChildAccounts()
    await loadContracts()

    // 更新当前管理的子女信息
    const updatedChild = childAccounts.value.find(c => c.id === bindingChild.value.id)
    if (updatedChild) {
      const newElder = elderAccounts.value.find(e => e.id === selectedElderId.value)
      if (newElder) {
        updatedChild.boundElders.push({
          id: newElder.id,
          name: newElder.name
        })
      }
    }

    selectedElderId.value = null
  } catch (error) {
    console.error('绑定失败:', error)
    ElMessage.error('绑定失败')
  } finally {
    binding.value = false
  }
}

// 解绑老人
const unbindElder = async (elderId) => {
  try {
    await childApi.deleteContract(bindingChild.value.id, elderId)
    ElMessage.success('解绑成功')

    // 重新加载绑定关系
    await loadChildAccounts()
    await loadContracts()

    // 更新当前管理的子女信息
    bindingChild.value.boundElders = bindingChild.value.boundElders.filter(elder => elder.id !== elderId)
  } catch (error) {
    console.error('解绑失败:', error)
    ElMessage.error('解绑失败')
  }
}

// 新增绑定关系
const addBinding = async () => {
  try {
    await bindingFormRef.value.validate()
    addingBinding.value = true

    await childApi.createContract({
      childId: bindingForm.value.childId,
      elderId: bindingForm.value.elderId
    })

    ElMessage.success('绑定关系创建成功')

    showAddBindingDialog.value = false
    bindingFormRef.value.resetFields()
    await loadContracts()
    await loadChildAccounts()
  } catch (error) {
    console.error('创建绑定关系失败:', error)
    ElMessage.error('创建绑定关系失败')
  } finally {
    addingBinding.value = false
  }
}

// 删除绑定关系
const deleteContract = async (contract) => {
  try {
    await ElMessageBox.confirm(
      `确定解绑"${contract.elderName}"和"${contract.childName}"的绑定关系吗？`,
      '确认解绑',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )

    await childApi.deleteContract(contract.childId, contract.elderId)
    ElMessage.success('解绑成功')

    await loadContracts()
    await loadChildAccounts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('解绑失败:', error)
      ElMessage.error('解绑失败')
    }
  }
}

// 辅助方法
const getAvatarColor = (name: string) => {
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#B37FEB', '#FF6B6B', '#4ECDC4']
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}

const getAgeTagType = (age: number) => {
  if (age < 70) return 'success'
  if (age < 80) return 'warning'
  return 'danger'
}

const formatDate = (dateString: string) => {
  if (!dateString) return '未知时间'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 生命周期
onMounted(() => {
  loadCommunities()
  // 不要自动加载网格群，等用户选择社区后再加载
  loadElderAccounts()
  loadChildAccounts()
  loadContracts()
})
</script>

<style scoped>
.account-contract {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.account-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.balance {
  color: #67c23a;
  font-weight: bold;
}

.account-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.account-code,
.account-password {
  display: flex;
  align-items: center;
}

/* 新增样式 */
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-weight: 500;
  color: #303133;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}

.phone-info,
.time-info {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 14px;
}

.location-info {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 14px;
}

.room-info {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 14px;
}

.bound-elders-display {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: center;
  max-width: 180px;
}

.account-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  width: 100%;
  min-width: 200px;
}

.account-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.account-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.account-icon {
  color: #409EFF;
}

.account-title {
  color: #606266;
}

.account-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.account-field {
  display: flex;
  align-items: center;
  gap: 8px;
}

.field-label {
  font-size: 12px;
  color: #909399;
  min-width: 40px;
  text-align: right;
}

.field-value {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

.gender-display {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

.full-width-tag {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: center;
  min-width: 120px;
}

.account-code .el-tag,
.account-password .el-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  border-radius: 4px;
}

/* 表格行样式优化 */
:deep(.el-table__row) {
  transition: all 0.3s ease;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa !important;
  transform: scale(1.01);
}

/* 标签样式优化 */
:deep(.el-tag) {
  border-radius: 6px;
  font-weight: 500;
}

:deep(.el-tag--primary) {
  background: linear-gradient(135deg, #409eff20, #409eff10);
  border-color: #409eff50;
  color: #409eff;
}

:deep(.el-tag--success) {
  background: linear-gradient(135deg, #67c23a20, #67c23a10);
  border-color: #67c23a50;
  color: #67c23a;
}

:deep(.el-tag--warning) {
  background: linear-gradient(135deg, #e6a23c20, #e6a23c10);
  border-color: #e6a23c50;
  color: #e6a23c;
}

:deep(.el-tag--danger) {
  background: linear-gradient(135deg, #f56c6c20, #f56c6c10);
  border-color: #f56c6c50;
  color: #f56c6c;
}

:deep(.el-tag--info) {
  background: linear-gradient(135deg, #90939920, #90939910);
  border-color: #90939950;
  color: #909399;
}

/* 头像样式优化 */
:deep(.el-avatar) {
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 按钮样式优化 */
:deep(.el-button--danger.is-plain) {
  border-color: #f56c6c80;
  color: #f56c6c;
  background: #f56c6c10;
}

:deep(.el-button--danger.is-plain:hover) {
  background: #f56c6c20;
  border-color: #f56c6c;
}

.binding-info {
  margin-bottom: 20px;
}

.child-info-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.child-info-card h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 2px solid #409EFF;
  padding-bottom: 8px;
}

.child-info-card p {
  margin: 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.child-info-card strong {
  color: #606266;
  min-width: 60px;
}

.elder-binding-info h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 2px solid #67C23A;
  padding-bottom: 8px;
}

.bound-elders {
  margin-top: 10px;
  min-height: 40px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px dashed #dcdfe6;
}

.bound-elders .el-tag {
  margin-right: 8px;
  margin-bottom: 8px;
  padding: 6px 12px;
  font-weight: 500;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.bound-elders .el-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.add-binding {
  margin-top: 20px;
}

.add-binding h4 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 2px solid #E6A23C;
  padding-bottom: 8px;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }

  .header-actions {
    flex-direction: column;
    gap: 5px;
  }

  .section-header {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
}
</style>
