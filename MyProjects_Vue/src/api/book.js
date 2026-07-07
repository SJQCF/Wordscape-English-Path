import request from '@/utils/axios'

// 获取用户收藏的书籍
export const getUserFavoriteBooks = (userId) =>
  request.get('/book/getFavoriteBooks/' + userId)

//获取书籍列表
export const getBook = (pageNum) =>
  request.get('/book/getBooks?pageNum=' + pageNum)

// 获取书籍详情
export const getBookDetail = (bookId) =>
  request.get('/book/getBookById/' + bookId)

// 取消收藏书籍
export const removeBookFromFavorites = (userId, bookId) =>
  request.post('/book/cancelFavoriteBook/' + userId + '?bookId=' + bookId)

// 检查书籍是否被收藏
export const checkBookIsFavorite = (userId, bookId) =>
  request.get('/book/checkFavoriteBook/' + userId + '?bookId=' + bookId)

// 收藏书籍
export const favoriteBook = (userId, bookId) =>
  request.post('/book/favoriteBook/' + userId + '?bookId=' + bookId)