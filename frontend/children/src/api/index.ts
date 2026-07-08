import request from '@/utils/request'

// 认证相关接口
export const authApi = {
  // 子女登录
  childLogin: (data: { username: string; password: string }) => {
    return request.post('/auth/child-login', data)
  },
  
  // 登出
  logout: () => {
    return request.post('/auth/logout')
  }
}

// 老人子女关系管理接口
export const elderChildApi = {
  // 获取关系列表
  getElderChildren: () => {
    return request.get('/elder-child')
  },
  
  // 获取子女的老人关系列表
  getEldersByChild: (childId: number) => {
    return request.get(`/elder-child/child/${childId}`)
  },
  
  // 创建关系
  createElderChild: (data: { elderId: number; childId: number }) => {
    return request.post('/elder-child', data)
  },
  
  // 删除关系
  deleteElderChild: (id: number) => {
    return request.delete(`/elder-child/${id}`)
  }
}

// 老人相关接口
export const elderApi = {
  // 获取老人信息
  getElder: (elderId: number) => {
    return request.get(`/elders/${elderId}`)
  },
  
  // 获取关联老人列表
  getAssociatedElders: (childId: number) => {
    return request.get(`/elders/child/${childId}`)
  },
  
  // 获取老人状态
  getElderStatus: (elderId: number) => {
    return request.get(`/elders/${elderId}/status`)
  },
  
  // 获取老人账户余额
  getElderBalance: (elderId: number) => {
    return request.get(`/account/elder/${elderId}`)
  },
  
  // 获取评价列表（完全照着老人端写）
  getReviews: (elderId: number) => {
    return request.get(`/review/elder/${elderId}`)
  },
  
  // 提交评价（完全照着老人端写）
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
  
  // 充值
  recharge: (elderId: number, amount: number) => {
    return request.post(`/account/elder/${elderId}/recharge`, null, { 
      params: { amount } 
    })
  },
  
  // 获取账单列表
  getBills: (elderId: number, params?: any) => {
    return request.get(`/account/elder/${elderId}/bills`, { params })
  },
  
  // 导出账单PDF
  exportBills: (elderId: number, params?: any) => {
    return request.get(`/account/elder/${elderId}/bills/export`, { 
      params,
      responseType: 'blob'
    })
  }
}

// 代购服务接口
export const purchaseApi = {
  // 获取关联的老人
  getAssociatedElders: () => {
    return request.get('/purchase/associated-elders')
  },
  
  // 获取老人代购记录
  getRequestsByElder: (elderId: number) => {
    return request.get(`/purchase/elder/${elderId}`)
  },
  
  // 确认代购
  confirmPurchase: (requestId: number, actualAmount: number) => {
    return request.post(`/purchase/${requestId}/confirm`, null, { 
      params: { actualAmount } 
    })
  },
  
  // 拒绝代购
  rejectPurchase: (requestId: number, reason: string) => {
    return request.post(`/purchase/${requestId}/reject`, null, { 
      params: { reason } 
    })
  }
}

// 紧急事件接口
export const emergencyApi = {
  // 获取关联的老人
  getAssociatedElders: () => {
    return request.get('/emergency/associated-elders')
  },
  
  // 获取未处理事件
  getUnhandledEvents: () => {
    return request.get('/emergency/unhandled')
  },
  
  // 获取老人紧急事件
  getEventsByElder: (elderId: number) => {
    return request.get(`/emergency/elder/${elderId}`)
  },
  
  // 处理紧急事件
  handleEvent: (eventId: number) => {
    return request.post(`/emergency/${eventId}/handle`)
  },
  
  // 获取今日刷脸记录
  getTodayFaceChecks: () => {
    return request.get('/face-check/today')
  }
}

// 刷脸记录接口
export const faceCheckApi = {
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
  // 提交评价
  submitReview: (data: {
    reviewId: number
    scores: number[]
    averageScore: number
  }) => {
    return request.post('/review/submit', data)
  },
  
  // 获取老人评价
  getReviewsByElder: (elderId: number) => {
    return request.get(`/review/elder/${elderId}`)
  },
  
  // 获取月度评价
  getReviewsByMonth: (reviewMonth: string) => {
    return request.get(`/review/month/${reviewMonth}`)
  },
  
  // 获取月度平均分
  getAverageScore: (reviewMonth: string) => {
    return request.get(`/review/average-score/${reviewMonth}`)
  },
  
  // 获取评价任务列表
  getReviewTasks: () => {
    return request.get('/review/tasks')
  },
  
  // 获取子女评价任务
  getChildReviews: (childId: number) => {
    return request.get(`/review/child/${childId}`)
  }
}
