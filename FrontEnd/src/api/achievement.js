import request from '@/utils/axios'

//获取全部成就信息
export const getAchievement = (pageNum) => {
  return request.get('/achievement/list?pageNum=' + pageNum)
}

//获取用户成就信息
export const getUserAchievement = (userId) => {
  return request.get('/achievement/getUserAchievement/' + userId)
}

//获取用户获取成就时间
export const getObtainedTime = (userId,achievementId) => {
  return request.get('/achievement/getUserAchievementTime/' + userId + '?achievementId=' + achievementId)
}