import { defineStore } from 'pinia'
import { elderApi, accountApi } from '@/api'

interface User {
  id: number
  name: string
  role: string
  token: string
  elderId?: number
  faceData?: string
}

interface ElderInfo {
  id: number
  name: string
  age: number | string
  gender: string
  status: string
  gridGroupId: number | string
  gridGroupName: string
  createdAt: string | null
  updatedAt: string | null
  balance?: number
}

export const useUserStore = defineStore('user', {
  state: (): { user: User | null; elderInfo: ElderInfo | null } => ({
    user: null,
    elderInfo: null
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.user?.token,
    userRole: (state) => state.user?.role,
    userName: (state) => state.user?.name || state.elderInfo?.name,
    elderId: (state) => state.elderInfo?.id
  },
  
  actions: {
    setUser(user: User) {
      this.user = user
      localStorage.setItem('token', user.token)
      localStorage.setItem('user', JSON.stringify(user))
    },
    
    setElderInfo(elderInfo: ElderInfo) {
      this.elderInfo = elderInfo
      localStorage.setItem('elderInfo', JSON.stringify(elderInfo))
    },
    
    clearUser() {
      this.user = null
      this.elderInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('elderInfo')
    },
    
    initUser() {
      const token = localStorage.getItem('token')
      const userStr = localStorage.getItem('user')
      const elderInfoStr = localStorage.getItem('elderInfo')
      
      if (token && userStr) {
        try {
          const user = JSON.parse(userStr)
          this.user = { ...user, token }
          
          // 如果有token，重新获取最新的老人信息
          if (user.elderId) {
            this.loadElderInfo().catch(error => {
              console.error('初始化时加载老人信息失败:', error)
            })
          }
        } catch (error) {
          console.error('解析用户信息失败:', error)
          this.clearUser()
        }
      }
      
      if (elderInfoStr) {
        try {
          const elderInfo = JSON.parse(elderInfoStr)
          this.elderInfo = elderInfo
        } catch (error) {
          console.error('解析老人信息失败:', error)
          this.elderInfo = null
        }
      }
    },
    
    async loadElderInfo() {
      try {
        // 从用户信息中获取elderId
        const userStr = localStorage.getItem('user')
        if (userStr) {
          const user = JSON.parse(userStr)
          const elderId = user.elderId
          if (!elderId) {
            console.error('无法获取老人ID')
            return
          }
          
          // 同时获取老人信息和账户余额
          const [elderData, balanceData] = await Promise.all([
            elderApi.getElder(elderId) as any,
            accountApi.getBalance(elderId) as any
          ])
          
          // 更新老人信息，包含余额
          const updatedElderInfo: ElderInfo = {
            id: elderData.id || 0,
            name: elderData.name || '',
            age: elderData.age || 0,
            gender: elderData.gender || '',
            status: elderData.status || '',
            gridGroupId: elderData.gridGroupId || 0,
            gridGroupName: elderData.gridGroupName || '',
            createdAt: elderData.createdAt || '',
            updatedAt: elderData.updatedAt || '',
            balance: balanceData || 0
          }
          
          this.setElderInfo(updatedElderInfo)
          console.log('老人信息加载成功:', updatedElderInfo)
          
          // 同时更新localStorage中的用户信息，确保余额同步
          const updatedUser = { ...user, balance: balanceData }
          localStorage.setItem('user', JSON.stringify(updatedUser))
        }
      } catch (error) {
        console.error('加载老人信息失败:', error)
        throw error
      }
    },
    
    // 强制刷新余额
    async refreshBalance() {
      try {
        const userStr = localStorage.getItem('user')
        if (userStr) {
          const user = JSON.parse(userStr)
          const elderId = user.elderId
          if (!elderId) {
            console.error('无法获取老人ID')
            return
          }
          
          // 只获取余额
          const balanceData = await accountApi.getBalance(elderId) as any
          
          // 更新老人信息中的余额
          if (this.elderInfo) {
            this.elderInfo.balance = balanceData || 0
            localStorage.setItem('elderInfo', JSON.stringify(this.elderInfo))
          }
          
          // 更新用户信息中的余额
          const updatedUser = { ...user, balance: balanceData }
          localStorage.setItem('user', JSON.stringify(updatedUser))
          
          console.log('余额刷新成功:', balanceData)
          return balanceData
        }
      } catch (error) {
        console.error('刷新余额失败:', error)
        throw error
      }
    }
  }
})
