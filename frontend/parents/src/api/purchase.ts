import request from '@/utils/request'

// 代购请求DTO
export interface PurchaseRequest {
  elderId: number
  content: string
  estimatedAmount?: number
}

// 代购响应DTO
export interface PurchaseResponse {
  id: number
  elderId: number
  content: string
  estimatedAmount: number
  actualAmount: number
  status: string
  createdAt: string
}

// 创建代购请求
export const createPurchaseRequest = (data: PurchaseRequest) => {
  return request({
    url: '/purchase',
    method: 'POST',
    data
  })
}

// 获取待处理代购请求列表
export const getPendingRequests = () => {
  return request({
    url: '/purchase/pending',
    method: 'GET'
  })
}

// 获取老人代购记录
export const getRequestsByElderId = (elderId: number) => {
  return request({
    url: `/purchase/elder/${elderId}`,
    method: 'GET'
  })
}

// 确认代购请求
export const confirmPurchase = (requestId: number, actualAmount: number) => {
  return request({
    url: `/purchase/${requestId}/confirm`,
    method: 'POST',
    data: { actualAmount }
  })
}

// 拒绝代购请求
export const rejectPurchase = (requestId: number, reason?: string) => {
  return request({
    url: `/purchase/${requestId}/reject`,
    method: 'POST',
    data: { reason }
  })
}
