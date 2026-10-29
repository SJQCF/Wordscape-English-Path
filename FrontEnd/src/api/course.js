import request from '@/utils/axios'

// 获取课程列表
export const Courses = (pageNum) => {
  return request.get('/course/courses?pageNum=' + pageNum)
}

// 获取用户收藏的课程
export const favoritesCourses = (userId,pageNum) => {
  return request.get('/course/favoriteCoursesByUserId/' + userId + '?pageNum=' + pageNum)
}

// 根据课程ID获取课程详情
export const courseDetail = (courseId) => {
  return request.get('/course/getCourseById?courseId=' + courseId)
}

// 收藏课程
export const favoriteCourse = (userId, courseId) => {
  return request.post('/course/favoriteCourse/' + userId + '?courseId=' + courseId)
}

// 取消收藏课程
export const cancelFavoriteCourse = (userId, courseId) => {
  return request.post('/course/cancelFavoriteCourse/' + userId + '?courseId=' + courseId)
}

//检查用户是否收藏了课程
export const checkFavoriteCourse = (userId, courseId) => {
  return request.get('/course/checkFavoriteCourse/' + userId + '?courseId=' + courseId)
}

// 购买课程
export const buyCourse = (userId, courseId) => {
  return request.post('/course/buyCourse/' + userId + '?courseId=' + courseId)
}

// 检查用户是否已购买课程
export const checkBoughtCourse = (userId, courseId) => {
  return request.get('/course/checkBoughtCourse/' + userId + '?courseId=' + courseId)
}

// 获取用户已购买的课程
export const getBoughtCourse = (userId, pageNum) => {
  return request.get('/course/getBoughtCourse/' + userId + '?pageNum=' + pageNum)
}

//获取视频地址
export const getVideoUrl = (courseId) => {
  return request.get('/course/getCourseVideo?courseId=' + courseId)
}