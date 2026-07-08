import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      redirect: '/community'
    },
    {
      path: '/community',
      name: 'community',
      component: () => import('../views/CommunityManagementView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/account',
      name: 'account',
      component: () => import('../views/AccountContractView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/account-stats',
      name: 'account-stats',
      component: () => import('../views/AccountStatsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/review',
      name: 'review',
      component: () => import('../views/ServiceReviewView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/emergency',
      name: 'emergency',
      component: () => import('../views/EmergencyView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/purchase',
      name: 'purchase',
      component: () => import('../views/PurchaseView.vue'),
      meta: { requiresAuth: true }
    }
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    next('/community')
  } else {
    next()
  }
})

export default router
