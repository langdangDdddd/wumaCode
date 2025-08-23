import axios, { AxiosRequestConfig, AxiosResponse } from 'axios'

// 創建 axios 實例
const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 請求攔截器
instance.interceptors.request.use(
  (config) => {
    // 在這裡可以添加認證 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 響應攔截器
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    return response.data
  },
  (error) => {
    // 統一錯誤處理
    console.error('請求錯誤:', error)
    return Promise.reject(error)
  }
)

// 導出請求函數
export const request = async <T = any>(config: AxiosRequestConfig): Promise<T> => {
  return instance.request(config)
}

export default instance
