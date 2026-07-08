import request from '@/utils/request'

// 认证相关接口
export const authApi = {
  // 统一登录接口
  login: (data: { username: string; password: string; userType: string }) => {
    return request.post('/auth/login', data)
  },
  
  // 登出
  logout: (data?: { token: string }) => {
    return request.post('/auth/logout', data)
  }
}

// 老人相关接口
export const elderApi = {
  // 获取老人信息
  getElder: (elderId: number) => {
    return request.get(`/elders/${elderId}`)
  },
  
  // 更新老人信息
  updateElder: (elderId: number, data: any) => {
    return request.put(`/elder/${elderId}`, data)
  },
  
  // 获取评价列表
  getReviews: (elderId: number) => {
    return request.get(`/review/elder/${elderId}`)
  },
  
  // 提交评价
  submitReview: (data: any) => {
    return request.post('/review/submit', data)
  }
}

// 账户相关接口
export const accountApi = {
  // 获取账户信息
  getAccount: (elderId: number) => {
    return request.get(`/account/elder/${elderId}`)
  },
  
  // 获取余额
  getBalance: (elderId: number) => {
    return request.get(`/account/elder/${elderId}/balance`)
  },
  
  // 获取账单列表
  getBills: (elderId: number, params?: any) => {
    return request.get(`/account/elder/${elderId}/bills`, { params })
  }
}

// 代购服务接口
export const purchaseApi = {
  // 创建代购请求
  createRequest: (data: {
    elderId: number
    content: string
    estimatedAmount?: number
  }) => {
    return request.post('/purchase', data)
  },
  
  // 获取老人代购记录
  getRequestsByElder: (elderId: number) => {
    return request.get(`/purchase/elder/${elderId}`)
  }
}

// 紧急事件接口
export const emergencyApi = {
  // 创建紧急事件
  createEvent: (data: {
    elderId: number
    eventType?: string
    description?: string
  }) => {
    return request.post('/emergency', data)
  }
}

// 刷脸记录接口
export const faceCheckApi = {
  // 记录刷脸
  recordFaceCheck: (elderId: number, result: string, photoData?: string) => {
    // 临时解决方案：使用表单数据而不是JSON，避免@RequestBody问题
    const formData = new FormData()
    formData.append('result', result)
    if (photoData) {
      formData.append('photoData', photoData)
    }
    
    return request.post(`/face-check/elder/${elderId}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  // 检查今日刷脸状态
  checkTodayStatus: (elderId: number) => {
    return request.get(`/face-check/elder/${elderId}/today-status`)
  },
  
  // 检查重新打卡请求
  checkRecheckRequest: (elderId: number) => {
    return request.get(`/face-check/elder/${elderId}/recheck-status`)
  },
  
  // 获取今日刷脸记录
  getTodayFaceChecks: () => {
    return request.get('/face-check/today')
  },
  
  // 获取老人刷脸记录
  getFaceChecksByElder: (elderId: number) => {
    return request.get(`/face-check/elder/${elderId}`)
  },
  
  // 获取今日未刷脸老人
  getEldersWithoutTodayCheck: () => {
    return request.get('/face-check/abnormal-today')
  }
}

// 服务评价接口
export const reviewApi = {
  // 创建评价
  createReview: (data: {
    elderId: number
    score: number
    comment?: string
    reviewMonth?: string
  }) => {
    return request.post('/review', data)
  },
  
  // 获取老人评价
  getReviewsByElder: (elderId: number) => {
    return request.get(`/review/elder/${elderId}`)
  }
}
