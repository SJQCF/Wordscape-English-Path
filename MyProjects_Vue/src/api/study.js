import request from '@/utils/axios'

// 获取用户今日学习单词数
export const getTodayNewWordsNum = (userId) => {
  return request.get('/study/todayNewWordsNum/' + userId)
}

// 获取用户今日复习单词数
export const getTodayReviewWordsNum = (userId) => {
  return request.get('/study/todayReviewWordsNum/' + userId)
}

// 获取用户已学单词数
export const getStudiedWordsNum = (userId) => {
  return request.get('/study/studiedWordsNum/' + userId)
}

// 查询用户学习的单词
export const getUserStudyWord = (userId,num) => {
  return request.get('/study/studyWord/' + userId + '?num=' + num)
}

//用户完成学习单词
export const completeStudyWord = (userId,wordId) => {
  return request.post('/study/completeWord/' + userId + '?wordId=' + wordId)
}

//检查用户输入
export const checkUserInput = (userId,userInput,correctAnswer,wordId) => {
  return request.post('/study/checkWord/' + userId + '?userInput=' + userInput + '&correctAnswer=' + correctAnswer + '&wordId=' + wordId)
}

//用户完成复习单词
export const reviewCompleteWord = (userId,wordId) => {
  return request.post('/study/reviewComplete/' + userId + '?wordId=' + wordId)
}

//获取用户复习单词
export const getUserReviewWord = (userId,num) => {
  return request.get('/study/reviewWords/' + userId + '?num=' + num)
}

//获取用户复习单词数量
const getReviewWordsNum = (userId) => {
  return request.get('/study/reviewWordsNum/' + userId)
}

//获取用户计划每日学习单词数量
export const getStudyPlan = (userId) => {
  return request.get('/study/planWords/' + userId)
}

//获取用户计划每日复习单词数量
export const getReviewStudyPlan = (userId) => {
  return request.get('/study/planReviewWords/' + userId)
}

//更新用户计划每日学习单词数量
export const updateStudyPlan = (userId,plan) => {
  return request.post('/study/planWords/' + userId + '?plan=' + plan)
}

//更改用户计划每日复习单词数量
export const changeStudyPlan = (userId,plan) => {
  return request.post('/study/planReviewWords/' + userId + '?plan=' + plan)
}