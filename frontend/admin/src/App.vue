<template>
  <!-- 登录页面单独显示，不包含侧边栏 -->
  <div v-if="$route.path === '/login'" class="login-page">
    <RouterView />
  </div>

  <!-- 主应用布局，包含侧边栏 -->
  <el-container v-else class="app-container">
    <!-- 侧边栏 -->
    <el-aside width="250px" class="sidebar">
      <div class="logo">
        <h2>养老管理系统</h2>
      </div>

      <el-menu
        :default-active="$route.path"
        router
        class="menu"
      >
        <el-menu-item index="/community">
          <el-icon><School /></el-icon>
          <span>社区管理</span>
        </el-menu-item>

        <el-menu-item index="/account">
          <el-icon><UserFilled /></el-icon>
          <span>账号与签约</span>
        </el-menu-item>

        <el-menu-item index="/account-stats">
          <el-icon><Money /></el-icon>
          <span>账户统计</span>
        </el-menu-item>

        <el-menu-item index="/review">
          <el-icon><Star /></el-icon>
          <span>服务评价</span>
        </el-menu-item>

        <el-menu-item index="/emergency">
          <el-icon><Warning /></el-icon>
          <span>紧急事件</span>
        </el-menu-item>

        <el-menu-item index="/purchase">
          <el-icon><ShoppingCart /></el-icon>
          <span>代购管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/community' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ getPageTitle() }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ userStore.userName || '管理员' }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 页面内容 -->
      <el-main class="main-content">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  School,
  UserFilled,
  Money,
  ArrowDown,
  Warning,
  ShoppingCart,
  Star
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const getPageTitle = () => {
  const path = route.path
  switch (path) {
    case '/community': return '社区管理'
    case '/account': return '账号与签约'
    case '/account-stats': return '账户统计'
    case '/review': return '服务评价'
    case '/emergency': return '紧急事件'
    case '/purchase': return '代购管理'
    default: return '首页'
  }
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人信息功能开发中')
      break
    case 'logout':
      userStore.clearUser()
      router.push('/login')
      ElMessage.success('已退出登录')
      break
  }
}

onMounted(() => {
  userStore.initUser()
})
</script>

<style>
/* 全局强制禁止水平滚动 */
html, body {
  height: 100%;
  width: 100%;
  overflow: hidden;
  margin: 0;
  padding: 0;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  overflow-x: hidden !important;
}

.app-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

.sidebar {
  background-color: #304156;
  color: #fff;
  flex-shrink: 0;
  width: 250px;
  min-width: 250px;
  overflow: hidden;
  position: relative;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding-left: 20px;
  background: white;
  border-bottom: 1px solid #434a50;
}

.logo h2 {
  margin: 0;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.menu {
  border: none;
  background: white;
  height: calc(100vh - 60px);
  overflow: hidden;
}

.menu :deep(.el-menu-item) {
  color: #ffffff !important;
  padding-left: 20px !important;
  justify-content: flex-start;
  text-align: left;
}

.menu :deep(.el-menu-item .el-icon) {
  margin-right: 8px;
  color: #ffffff ;
}

.menu :deep(.el-menu-item:hover) {
  background-color: #263445;
  color: #ffffff !important;
}

.menu :deep(.el-menu-item.is-active) {
  background-color: #409eff;
  color: #ffffff !important;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
  height: 60px;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
}

.user-info:hover {
  color: #409eff;
}

.main-content {
  background: #f0f2f5;
  padding: 20px;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden !important;
  height: calc(100vh - 60px);
  position: relative;
}

.login-page {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

/* 强制禁止Element Plus组件的水平滚动 */
.el-table {
  overflow-x: hidden !important;
  max-width: 100%;
}

.el-table .el-table__body-wrapper {
  overflow-x: hidden !important;
}

.el-card {
  overflow-x: hidden !important;
  max-width: 100%;
}

.el-dialog {
  overflow-x: hidden !important;
}

.el-dialog__wrapper {
  overflow-x: hidden !important;
}

.el-scrollbar__wrap {
  overflow-x: hidden !important;
}

.el-select-dropdown {
  overflow-x: hidden !important;
}

.el-popper {
  overflow-x: hidden !important;
}
</style>
