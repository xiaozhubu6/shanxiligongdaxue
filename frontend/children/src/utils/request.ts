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
    // 检查是否是包装的响应格式
    const { data } = response
    
    // 如果data有code属性，说明是包装的响应
    if (data && typeof data === 'object' && 'code' in data) {
      const { code, message, data: responseData } = data
      
      if (code === 200) {
        return responseData
      } else {
        ElMessage.error(message || '请求失败')
        return Promise.reject(new Error(message || '请求失败'))
      }
    } else {
      // 直接返回数据（如List<Elder>等）
      return data
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
