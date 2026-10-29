import request from '@/utils/axios'

// // 查询单词列表
// export const getWords = () => {
//     return request.get('/word/list')
// }

//收藏单词
export const favoriteWord = (userId, wordId) => {
  return request.post('/word/star/' + userId + '?wordId=' + wordId)
}

//取消收藏单词
export const cancelFavoriteWord = (userId, wordId) => {
  return request.post('/word/cancelStar/' + userId + '?wordId=' + wordId)
}

//获取用户是否收藏单词
export const checkFavoriteWord = (userId, wordId) => {
  return request.get('/word/checkStar/' + userId + '?wordId=' + wordId)
}

//获取用户收藏的单词
export const getUserFavoriteWords = (userId, pageNum) => {
  return request.get('/word/findStarByUserId/' + userId + '?pageNum=' + pageNum)
}

//获取用户已学习的单词
export const getUserStudiedWords = (userId, pageNum,type) => {
  return request.get('/word/studiedWords/' + userId + '?pageNum=' + pageNum + '&type=' + type)
}

//根据id查询单词
export const getWordById = (wordId) => {
  return request.get('/word/getWordById?wordId=' + wordId)
}

//查询单词
export const searchWord = (word,pageNum) => {
  return request.get('/word/find?word=' + word + '&pageNum=' + pageNum)
}
