import { defineStore } from 'pinia'

interface User {
  id: number
  name: string
  role: string
  token: string
  gridGroupId?: number
  gridGroupName?: string
}

export const useUserStore = defineStore('user', {
  state: (): { user: User | null } => ({
    user: null
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.user?.token,
    userRole: (state) => state.user?.role,
    userName: (state) => state.user?.name
  },
  
  actions: {
    setUser(user: User) {
      this.user = user
      localStorage.setItem('token', user.token)
      localStorage.setItem('user', JSON.stringify(user))
    },
    
    clearUser() {
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },
    
    initUser() {
      const token = localStorage.getItem('token')
      const userStr = localStorage.getItem('user')
      
      if (token && userStr) {
        try {
          const user = JSON.parse(userStr)
          this.user = { ...user, token }
        } catch (error) {
          console.error('解析用户信息失败:', error)
          this.clearUser()
        }
      }
    }
  }
})
