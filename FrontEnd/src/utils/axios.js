import axios from 'axios'
import { useRouter } from 'vue-router'

// 创建axios实例
const request = axios.create({
  baseURL: '/api', // api的base_url
  timeout: 60000 // 请求超时时间
})

// 获取token
const getToken = () => {
  return localStorage.getItem('token')
}

// 设置token
const setToken = (token) => {
  localStorage.setItem('token', token)
}

// 清除token
const removeToken = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加token到请求头
    const token = getToken()
    if (token) {
      config.headers.token = token
    }
    return config
  },
  error => {
    // 处理请求错误
    console.log(error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 处理响应数据
    const res = response.data
    return res
  },
  error => {
    // 处理响应错误
    console.log('err' + error)
    
    // 处理401错误
    if (error.response && error.response.status === 401) {
      // 清除token
      removeToken()
      
      // 跳转到登录页
      const router = useRouter()
      router.push('/login')
    }
    
    return Promise.reject(error)
  }
)

export { getToken, setToken, removeToken }
export default request