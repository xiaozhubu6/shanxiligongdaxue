<template>
  <div class="app-container">
    <!-- 登录页面布局 -->
    <template v-if="$route.meta.layout === 'login'">
      <RouterView />
    </template>
    
    <!-- 主应用布局 -->
    <template v-else>
      <div class="main-layout">
        <!-- 顶部导航 -->
        <div class="header">
          <div class="header-left">
            <h1>养老管理系统 - 子女端</h1>
          </div>
          
          <div class="header-right">
            <span class="welcome-text">欢迎，{{ userInfo.name || '用户' }}登录！</span>
            <el-button type="danger" size="small" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出系统
            </el-button>
          </div>
        </div>
        
        <!-- 主内容区 -->
        <div class="main-content-wrapper">
          <!-- 侧边栏 -->
          <div class="sidebar">
            <div class="sidebar-menu">
              <div class="menu-item" :class="{ active: $route.path === '/evaluation' }" @click="navigateTo('/evaluation')">
                <el-icon><Star /></el-icon>
                <span>服务评价</span>
              </div>
              <div class="menu-item" :class="{ active: $route.path === '/recharge' }" @click="navigateTo('/recharge')">
                <el-icon><Money /></el-icon>
                <span>账户充值</span>
              </div>
              <div class="menu-item" :class="{ active: $route.path === '/bills' }" @click="navigateTo('/bills')">
                <el-icon><Document /></el-icon>
                <span>账单查看</span>
              </div>
              <div class="menu-item" :class="{ active: $route.path === '/alerts' }" @click="navigateTo('/alerts')">
                <el-icon><Warning /></el-icon>
                <span>异常监控</span>
              </div>
            </div>
          </div>
          
          <!-- 主内容区 -->
          <div class="main-content">
            <RouterView />
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document, Monitor, Money, Star, SwitchButton, Warning } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

// 用户信息
const userInfo = ref({
  name: '',
  id: null
})

// 退出系统
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出系统吗？',
      '退出确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    // 清除本地存储
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    
    // 调用退出API
    try {
      await import('@/api').then(api => api.authApi.logout())
    } catch (error) {
      console.error('退出API调用失败:', error)
    }
    
    // 跳转到登录页
    router.push('/login')
    ElMessage.success('退出成功')
    
  } catch (error) {
    // 用户取消退出
  }
}

// 导航方法
const navigateTo = (path) => {
  console.log('导航到:', path)
  try {
    router.push(path)
  } catch (error) {
    console.error('导航失败:', error)
    ElMessage.error('页面跳转失败')
  }
}

// 加载用户信息
const loadUserInfo = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      const userData = JSON.parse(userStr)
      console.log('加载的用户数据:', userData)
      
      // 设置用户信息，优先使用name字段
      userInfo.value = {
        id: userData.id || null,
        name: userData.name || '用户',
        username: userData.username || '',
        userType: userData.userType || '',
        phone: userData.phone || '',
        age: userData.age || null,
        gender: userData.gender || '',
        balance: userData.balance || 0
      }
      
      console.log('设置的用户信息:', userInfo.value)
    } catch (error) {
      console.error('解析用户信息失败:', error)
      userInfo.value = {
        name: '用户',
        id: null
      }
    }
  } else {
    console.log('未找到用户信息，使用默认值')
    userInfo.value = {
      name: '用户',
      id: null
    }
  }
}

onMounted(() => {
  console.log('App mounted')
  console.log('Current route:', route.path)
  loadUserInfo()
  
  // 监听localStorage变化，实现多标签页同步
  window.addEventListener('storage', (e) => {
    if (e.key === 'user') {
      console.log('检测到用户信息变化，重新加载')
      loadUserInfo()
    }
  })
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  width: 100%;
  overflow: hidden;
  margin: 0;
  padding: 0;
}

#app {
  width: 100vw;
  height: 100vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

.app-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

.main-layout {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.header {
  background: white;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
  flex-shrink: 0;
}

.header-left h1 {
  margin: 0;
  color: #333;
  font-size: 20px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.welcome-text {
  color: #333;
  font-size: 16px;
  font-weight: 500;
}

.user-info {
  display: flex;
  align-items: center;
  color: #333;
  font-size: 16px;
}

.main-content {
  background: #f0f2f5;
  padding: 20px;
  margin: 0;
  overflow-y: auto;
  flex: 1;
}

.main-content-wrapper {
  display: flex;
  flex: 1;
  width: 100%;
  height: 100%;
}

.sidebar {
  width: 240px;
  background: white;
  border-right: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.sidebar-menu {
  padding: 20px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: #606266;
  text-decoration: none;
  transition: all 0.3s;
  margin-bottom: 4px;
  cursor: pointer;
}

.menu-item:hover {
  background: #f5f7fa;
  color: #409eff;
}

.menu-item.active {
  background: #ecf5ff;
  color: #409eff;
  border-right: 3px solid #409eff;
}

.menu-item .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 0 15px;
  }
  
  .header-left h1 {
    font-size: 18px;
  }
  
  .header-right {
    gap: 10px;
  }
  
  .welcome-text {
    font-size: 14px;
  }
  
  .sidebar {
    width: 200px;
  }
  
  .menu-item {
    padding: 10px 15px;
    font-size: 14px;
  }
  
  .main-content {
    padding: 15px;
  }
}

@media (max-width: 1024px) {
}
</style>
