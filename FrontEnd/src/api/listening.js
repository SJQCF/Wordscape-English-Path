import request from '@/utils/axios'

//获取全部听力信息
export const getListening = (pageNum) => {
  return request.get('/listening/list?pageNum=' + pageNum)
}

//根据类型获取听力信息
export const getListeningByType = (type,pageNum) => {
  return request.get('/listening/listByType?type=' + type + '&pageNum=' + pageNum)
}

//根据id获取听力信息
export const getListeningById = (id) => {
  return request.get('/listening/getById?id=' + id)
}

//查询用户收藏的听力音频的基础信息
export const getUserFavoriteListening = (userId, pageNum) => {
  return request.get('/listening/getFavoriteAudios/' + userId + '?pageNum=' + pageNum)
}

//收藏听力音频
export const favoriteListening = (userId, audioId) => {
  return request.post('/listening/favorite/' + userId + '?audioId=' + audioId)
}

//取消收藏听力音频
export const cancelFavoriteListening = (userId, audioId) => {
  return request.post('/listening/cancelFavorite/' + userId + '?audioId=' + audioId)
}

//查看用户是否收藏了听力音频
export const checkFavoriteListening = (userId, audioId) => {
  return request.get('/listening/checkFavorite/' + userId + '?audioId=' + audioId)
}