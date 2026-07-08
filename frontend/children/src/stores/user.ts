import { defineStore } from 'pinia'

interface User {
  id: number
  name: string
  role: string
  token: string
  childId?: number
}

interface ElderInfo {
  id: number
  name: string
  age: number
  gender: string
  balance: number
  todayFaceChecked: boolean
  status: string
  gridGroupName?: string
}

export const useUserStore = defineStore('user', {
  state: (): { user: User | null; associatedElders: ElderInfo[] } => ({
    user: null,
    associatedElders: []
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.user?.token,
    userRole: (state) => state.user?.role,
    userName: (state) => state.user?.name,
    childId: (state) => state.user?.childId
  },
  
  actions: {
    setUser(user: User) {
      this.user = user
      localStorage.setItem('token', user.token)
      localStorage.setItem('user', JSON.stringify(user))
    },
    
    setAssociatedElders(elders: ElderInfo[]) {
      this.associatedElders = elders
      localStorage.setItem('associatedElders', JSON.stringify(elders))
    },
    
    clearUser() {
      this.user = null
      this.associatedElders = []
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('associatedElders')
    },
    
    initUser() {
      const token = localStorage.getItem('token')
      const userStr = localStorage.getItem('user')
      const eldersStr = localStorage.getItem('associatedElders')
      
      if (token && userStr) {
        try {
          const user = JSON.parse(userStr)
          this.user = { ...user, token }
        } catch (error) {
          console.error('解析用户信息失败:', error)
          this.clearUser()
        }
      }
      
      if (eldersStr) {
        try {
          const elders = JSON.parse(eldersStr)
          this.associatedElders = elders
        } catch (error) {
          console.error('解析老人信息失败:', error)
        }
      }
    }
  }
})
