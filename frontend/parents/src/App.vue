<template>
  <div class="app-container">
    <!-- 页面内容 - 铺满整个屏幕 -->
    <RouterView />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(() => {
  userStore.initUser()
})
</script>

<style>
/* 全局隐藏滚动条 */
* {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

*::-webkit-scrollbar {
  display: none; /* Chrome, Safari and Opera */
}

body, html {
  overflow: hidden;
  margin: 0;
  padding: 0;
}
</style>

<style scoped>
.app-container {
  width: 100vw;
  height: 100vh;
  background: #f5f7fa;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}
</style>
