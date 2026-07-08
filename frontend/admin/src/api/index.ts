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

// 管理员接口
export const adminApi = {
  // 获取统计信息
  getStatistics: () => {
    return request.get('/admin/statistics')
  },
  
  // 获取所有用户账户
  getAccounts: (params?: {
    page?: number
    size?: number
    userType?: string
    status?: string
  }) => {
    return request.get('/admin/accounts', { params })
  },
  
  // 创建用户账户
  createAccount: (data: any) => {
    return request.post('/admin/accounts', data)
  },
  
  // 更新用户账户
  updateAccount: (id: number, data: any) => {
    return request.put(`/admin/accounts/${id}`, data)
  },
  
  // 删除用户账户
  deleteAccount: (id: number) => {
    return request.delete(`/admin/accounts/${id}`)
  }
}

// 社区管理接口
export const communityApi = {
  // 获取社区列表
  getCommunities: () => {
    return request.get('/community')
  },
  
  // 创建社区
  createCommunity: (data: { name: string; address?: string }) => {
    return request.post('/community', data)
  },
  
  // 更新社区
  updateCommunity: (communityId: number, data: { name: string; address: string }) => {
    return request.post(`/community/${communityId}/update`, data)
  },
  
  // 删除社区
  deleteCommunity: (communityId: number) => {
    return request.delete(`/community/${communityId}`)
  },
  
  // 获取网格群列表
  getGridGroups: (communityId: number) => {
    return request.get(`/community/${communityId}/grid-groups`)
  },
  
  // 创建网格群
  createGridGroup: (data: { name: string; unitNumber?: string; communityId: number }) => {
    return request.post(`/community/${data.communityId}/grid-groups`, {
      name: data.name,
      unitNumber: data.unitNumber
    })
  },
  
  // 更新网格群
  updateGridGroup: (groupId: number, data: { name: string; unitNumber: string }) => {
    return request.post(`/community/grid-groups/${groupId}/update`, data)
  },
  
  // 删除网格群
  deleteGridGroup: (groupId: number) => {
    return request.delete(`/community/grid-groups/${groupId}`)
  },
  
  // 获取网格群单元列表
  getGridGroupUnits: (gridGroupId: number) => {
    return request.get(`/grid-group-units/grid-group/${gridGroupId}`)
  },
  
  // 创建网格群单元
  createGridGroupUnit: (data: { gridGroupId: number; unitNumber: string }) => {
    return request.post('/grid-group-units', data)
  },
  
  // 删除网格群单元
  deleteGridGroupUnit: (unitId: number) => {
    return request.delete(`/grid-group-units/${unitId}`)
  },
  
  // 批量创建网格群单元
  createGridGroupUnitsBatch: (units: { gridGroupId: number; unitNumber: string }[]) => {
    return request.post('/grid-group-units/batch', units)
  }
}

// 老人管理接口
export const elderApi = {
  // 创建老人
  createElder: (data: {
    name: string
    age: number
    gender?: string
    gridGroupId: number
    unitNumber?: string
    roomNumber?: string
    phone?: string
  }) => {
    return request.post('/elders', data)
  },
  
  // 获取老人信息
  getElder: (elderId: number) => {
    return request.get(`/elders/${elderId}`)
  },
  
  // 更新老人信息
  updateElder: (elderId: number, data: {
    name?: string
    age?: number
    gender?: string
    gridGroupId?: number
    unitNumber?: string
    roomNumber?: string
    phone?: string
  }) => {
    return request.put(`/elders/${elderId}`, data)
  },
  
  // 删除老人
  deleteElder: (elderId: number) => {
    return request.delete(`/elders/${elderId}`)
  },
  
  // 检查老人今日刷脸状态
  checkTodayFaceStatus: (elderId: number) => {
    return request.get(`/face-check/elder/${elderId}/today-status`)
  },
  
  // 根据网格群获取老人列表
  getEldersByGridGroup: (gridGroupId: number) => {
    return request.get(`/elders/grid-group/${gridGroupId}`)
  },
  
  // 根据单元获取老人列表
  getEldersByGridGroupUnit: (gridGroupUnitId: number) => {
    return request.get(`/elders/grid-group-unit/${gridGroupUnitId}`)
  },
  
  // 获取所有老人
  getAllElders: () => {
    return request.get('/elders')
  },
  
  // 获取异常老人列表
  getAbnormalElders: () => {
    return request.get('/elders/abnormal')
  },
  
  // 获取网格群异常老人
  getAbnormalEldersByGridGroup: (gridGroupId: number) => {
    return request.get(`/elders/abnormal/grid-group/${gridGroupId}`)
  },
  
  // 更新老人密码
  updatePassword: (elderId: number, newPassword: string) => {
    return request.put(`/elders/${elderId}/password`, { password: newPassword })
  }
}

// 账户管理接口
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
  getBills: (elderId: number) => {
    return request.get(`/account/elder/${elderId}/bills`)
  },
  
  // 获取账户统计
  getAccountStats: (timeFilter?: string) => {
    const params = timeFilter ? { timeFilter } : {}
    return request.get('/account-stats', { params })
  }
}

// 老人子女关系管理接口
export const elderChildApi = {
  // 检查老人是否有签约关系
  checkElderContract: (elderId: number) => {
    return request.get(`/elder-child/check/${elderId}`)
  },
  
  // 创建老人子女关系
  createElderChild: (data: { elderId: number; childId: number }) => {
    return request.post('/elder-child', data)
  },
  
  // 删除老人子女关系
  deleteElderChild: (id: number) => {
    return request.delete(`/elder-child/${id}`)
  },
  
  // 获取所有老人子女关系
  getElderChildren: () => {
    return request.get('/elder-child')
  },
  
  // 获取老人的所有子女关系
  getChildrenByElder: (elderId: number) => {
    return request.get(`/elder-child/elder/${elderId}`)
  }
}

// 子女管理接口
export const childApi = {
  // 获取所有子女
  getChildren: () => {
    // TODO: 实现后端API
    return request.get('/child')
  },
  
  // 创建子女账号
  createChild: (data: { name: string; phone: string; password: string }) => {
    // TODO: 实现后端API
    return request.post('/child', data)
  },
  
  // 删除子女账号
  deleteChild: (childId: number) => {
    // TODO: 实现后端API
    return request.delete(`/child/${childId}`)
  },
  
  // 根据ID获取子女
  getChild: (childId: number) => {
    return request.get(`/child/${childId}`)
  },
  
  // 获取绑定关系
  getContracts: () => {
    return request.get('/elder-child')
  },
  
  // 根据子女ID获取绑定关系
  getContractsByChild: (childId: number) => {
    return request.get(`/elder-child/child/${childId}`)
  },
  
  // 创建绑定关系
  createContract: (data: { childId: number; elderId: number }) => {
    return request.post('/elder-child', data)
  },
  
  // 删除绑定关系
  deleteContract: (childId: number, elderId: number) => {
    // 先查询该子女和老人的关系ID，然后删除
    return request.get(`/elder-child/child/${childId}`).then((response: any) => {
      const relations = response || []
      const relation = relations.find((r: any) => r.elderId === elderId)
      if (relation) {
        return request.delete(`/elder-child/${relation.id}`)
      }
      throw new Error('未找到绑定关系')
    })
  }
}

// 代购服务接口
export const purchaseApi = {
  // 获取统计信息
  getStatistics: () => {
    return request.get('/purchase/statistics')
  },
  
  // 创建代购请求
  createRequest: (data: {
    elderId: number
    content: string
    estimatedAmount?: number
  }) => {
    return request.post('/purchase', data)
  },
  
  // 获取待处理请求
  getPendingRequests: () => {
    return request.get('/purchase/pending')
  },
  
  // 获取所有请求
  getAllRequests: () => {
    return request.get('/purchase/all')
  },
  
  // 获取老人代购记录
  getRequestsByElder: (elderId: number) => {
    return request.get(`/purchase/elder/${elderId}`)
  },
  
  // 确认代购
  confirmPurchase: (requestId: number, data: { actualAmount: number; remark?: string }) => {
    return request.put(`/purchase/${requestId}/confirm`, data)
  },
  
  // 拒绝代购
  rejectPurchase: (requestId: number, reason?: string) => {
    return request.put(`/purchase/${requestId}/reject`, { reason })
  }
}

// 账户统计接口
export const accountStatsApi = {
  // 获取账户统计
  getAccountStats: (timeFilter?: string) => {
    const params = timeFilter ? `?timeFilter=${timeFilter}` : ''
    return request.get(`/account-stats${params}`)
  },
  
  // 获取汇总统计
  getAccountSummary: (timeFilter?: string) => {
    const params = timeFilter ? `?timeFilter=${timeFilter}` : ''
    return request.get(`/account-stats/summary${params}`)
  }
}

// 账单接口
export const billApi = {
  // 获取老人账单
  getBillsByElder: (elderId: number) => {
    return request.get(`/bills/elder/${elderId}`)
  },
  
  // 筛选账单
  getBillsByFilter: (params: {
    elderId?: number
    startDate?: string
    endDate?: string
    billType?: string
  }) => {
    const queryParams = new URLSearchParams()
    if (params.elderId) queryParams.append('elderId', params.elderId.toString())
    if (params.startDate) queryParams.append('startDate', params.startDate)
    if (params.endDate) queryParams.append('endDate', params.endDate)
    if (params.billType) queryParams.append('billType', params.billType)
    
    return request.get(`/bills/filter?${queryParams}`)
  },
  
  // 获取账单统计
  getBillStatistics: (elderId: number) => {
    return request.get(`/bills/statistics/${elderId}`)
  }
}

// 紧急事件接口
export const emergencyApi = {
  // 获取统计信息
  getStatistics: () => {
    return request.get('/emergency/statistics')
  },
  
  // 创建紧急事件
  createEvent: (data: {
    elderId: number
    eventType?: string
    description?: string
  }) => {
    return request.post('/emergency', data)
  },
  
  // 获取未处理事件
  getUnhandledEvents: () => {
    return request.get('/emergency/unhandled')
  },
  
  // 获取所有事件
  getAllEvents: () => {
    return request.get('/emergency/all')
  },
  
  // 获取已处理事件
  getHandledEvents: () => {
    return request.get('/emergency/handled')
  },
  
  // 处理未刷脸异常
  handleFaceCheckException: (elderId: number) => {
    return request.post('/emergency/handle-face-check-exception', {
      elderId
    })
  },
  
  // 创建重新打卡记录
  createRecheckRecord: (data: {
    elderId: number
    result: string
    photoData?: string
    adminAssisted?: boolean
    originalRecordId?: number
    recheckReason?: string
  }) => {
    return request.post('/emergency/recheck-record', data)
  },
  
  // 获取所有老人
  getAllElders: () => {
    return request.get('/elders')
  },
  
  // 获取所有网格群
  getAllGridGroups: () => {
    return request.get('/community/grid-groups')
  },
  
  // 获取今日打卡记录
  getTodayFaceChecks: () => {
    return request.get('/face-check/today')
  },
  
  // 获取未刷脸异常
  getAbnormalToday: () => {
    return request.get('/face-check/abnormal-today')
  },
  
  // 处理紧急事件
  handleEvent: (eventId: number) => {
    return request.post(`/emergency/${eventId}/handle`)
  }
}

// 刷脸记录接口
export const faceCheckApi = {
  // 记录刷脸
  recordFaceCheck: (elderId: number, result: string) => {
    return request.post(`/face-check/elder/${elderId}`, { result })
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
  },
  
  // 获取老人所有打卡照片
  getElderPhotos: (elderId: number) => {
    return request.get(`/face-check/elder/${elderId}/photos`)
  },
  
  // 获取老人的紧急事件
  getElderEvents: (elderId: number) => {
    return request.get(`/emergency/elder/${elderId}`)
  }
}

// 服务评价接口
export const reviewApi = {
  // 创建评价
  createReview: (data: {
    elderId: number
    childId?: number
    score: number
    comment?: string
    reviewMonth?: string
  }) => {
    return request.post('/review', data)
  },
  
  // 获取所有评价任务
  getReviewTasks: (params?: {
    status?: string
    type?: string
    month?: string
  }) => {
    return request.get('/review/tasks', { params })
  },
  
  // 推送月度评价任务
  pushMonthlyTasks: (data?: { reviewMonth?: string }) => {
    return request.post('/review/push-tasks', data)
  },
  
  // 推送老人评价任务
  pushElderTasks: (data?: { reviewMonth?: string }) => {
    return request.post('/review/push-elder-tasks', data)
  },
  
  // 推送子女评价任务
  pushChildTasks: (data?: { reviewMonth?: string }) => {
    return request.post('/review/push-child-tasks', data)
  },
  
  // 获取评价统计
  getReviewStats: (reviewMonth?: string) => {
    const url = reviewMonth ? `/review/statistics/${reviewMonth}` : '/review/statistics/current'
    return request.get(url)
  },
  
  // 获取老人评价
  getReviewsByElder: (elderId: number) => {
    return request.get(`/review/elder/${elderId}`)
  },
  
  // 完成评价任务
  completeReviewTask: (reviewId: number, reviewType: string) => {
    return request.post('/review/complete-review', { reviewId, reviewType })
  },
  
  // 获取子女评价
  getReviewsByChild: (childId: number) => {
    return request.get(`/review/child/${childId}`)
  },
  
  // 获取月度评价
  getReviewsByMonth: (reviewMonth: string) => {
    return request.get(`/review/month/${reviewMonth}`)
  },
  
  // 获取月度平均分
  getAverageScore: (reviewMonth: string) => {
    return request.get(`/review/average-score/${reviewMonth}`)
  }
}
