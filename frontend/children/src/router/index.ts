import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginSimple.vue'),
      meta: { requiresAuth: false, layout: 'login' }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      redirect: '/evaluation',
      meta: { requiresAuth: true, layout: 'main' }
    },
    {
      path: '/evaluation',
      name: 'evaluation',
      component: () => import('../views/EvaluationView.vue'),
      meta: { requiresAuth: true, layout: 'main' }
    },
    {
      path: '/recharge',
      name: 'recharge',
      component: () => import('../views/RechargeView.vue'),
      meta: { requiresAuth: true, layout: 'main' }
    },
    {
      path: '/bills',
      name: 'bills',
      component: () => import('../views/BillView.vue'),
      meta: { requiresAuth: true, layout: 'main' }
    },
    {
      path: '/alerts',
      name: 'alerts',
      component: () => import('../views/AlertView.vue'),
      meta: { requiresAuth: true, layout: 'main' }
    }
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  console.log('路由跳转:', from.path, '->', to.path)
  
  // 如果访问登录页，直接放行
  if (to.path === '/login') {
    next()
    return
  }
  
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
      console.log('未登录，重定向到登录页')
      next('/login')
      return
    }
    
    try {
      const user = JSON.parse(userStr)
      console.log('路由守卫检查用户:', user)
      
      // 检查用户数据是否有效
      if (!user || (!user.id && !user.elderId)) {
        console.log('用户信息无效，重定向到登录页')
        localStorage.removeItem('user')
        next('/login')
        return
      }
    } catch (error) {
      console.log('用户信息解析失败，重定向到登录页')
      localStorage.removeItem('user')
      next('/login')
      return
    }
  }
  
  console.log('路由守卫通过，跳转到:', to.path)
  next()
})

export default router
