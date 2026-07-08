import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8082/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 检查响应数据格式
    const responseData = response.data
    
    // 如果是直接的数据（如余额API），直接返回
    if (typeof responseData === 'number') {
      return responseData
    }
    
    // 如果是AI聊天响应格式 {message, success}
    if (typeof responseData === 'object' && responseData.message && typeof responseData.success === 'boolean') {
      return responseData
    }
    
    // 如果是直接的对象数据，直接返回
    if (typeof responseData === 'object' && !responseData.code) {
      return responseData
    }
    
    // 如果是标准格式 {code, message, data}
    const { code, message, data } = responseData
    
    if (code === 200) {
      return data
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message || '请求失败'))
    }
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        window.location.href = '/login'
      } else if (status === 403) {
        ElMessage.error('权限不足')
      } else {
        ElMessage.error(data?.message || '网络错误')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    
    return Promise.reject(error)
  }
)

export default request
