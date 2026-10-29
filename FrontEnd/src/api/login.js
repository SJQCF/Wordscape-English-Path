import request from '@/utils/axios'

// 密码登录
export const loginByPassword = (loginData) => {
  return request.post('/login/password', loginData)
}

// 验证码登录
export const loginByCode = (loginData) => {
  return request.post('/login/code', loginData)
}

// 邮箱验证码登录或注册
export const loginByEmail = (loginData) => {
  return request.post('/login/email', loginData)
}

// 发送验证码到邮箱
export const sendCode = (email) => {
  return request.post('/login/code', null, {
    params: {
      email: email
    }
  })
}

// 登出
export const logout = () => {
  return request.post('/logout')
}

// 获取当前用户信息
export const getCurrentUser = () => {
  return request.get('/user/current')
}