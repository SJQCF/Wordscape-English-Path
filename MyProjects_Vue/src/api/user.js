import request from '@/utils/axios'

//获取用户类型
export const getUserType = (userId) => {
  return request.get('/user/type/' + userId)
}

//获取用户个人签名
export const getUserSignature = (userId) => {
  return request.get('/user/signature/' + userId)
}

//获取用户名字
export const getUserName = (userId) => {
  return request.get('/user/name/' + userId)
}

//更改用户类型
export const updateUserType = (userId, typeChoice) => {
  return request.post('/user/type/' + userId + '?typeChoice=' + typeChoice)
}

// 获取用户学习天数
export const getStudyDays = (userId) => {
  return request.get('/user/studyDays/' + userId)
}

// 获取用户总学习天数
export const getTotalStudyDays = (userId) => {
  return request.get('/user/totalStudyDays/' + userId)
}

// 获取用户头像
export const getUserAvatar = (userId) => {
  return request.get('/user/avatar/' + userId)
}

// 更改用户名字
export const updateUserName = (userId, name) => {
  return request.post('/user/name/' + userId + '?name=' + name)
}

// 获取用户个性签名
export const updateUserSignature = (userId, signature) => {
  return request.post('/user/signature/' + userId + '?signature=' + signature)
}

// 更改用户头像
export const updateUserAvatar = (userId, file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/user/avatar/' + userId, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 查询用户是否是黑夜模式
export const isNightMode = (userId) => {
  return request.get('/user/isNightMode/' + userId)
}

// 更改用户黑夜模式
export const updateIsNightMode = (userId, isNightMode) => {
  return request.post('/user/isNightMode/' + userId + '?isNightMode=' + isNightMode)
}

// 查询用户前一天是否学习
export const isStudyYesterday = (userId) => {
  return request.get('/user/isStudyYesterday/' + userId)
}

// 用户连续学习天数归零
export const resetStudyDays = (userId) => {
  return request.post('/user/resetStudyDays/' + userId)
}

// 查询用户当前密码
export const getPassword = (userId) => {
  return request.get('/user/password/' + userId)
}

// 更改用户密码
export const updatePassword = (userId, password) => {
  return request.post('/user/password/' + userId + '?password=' + password)
}