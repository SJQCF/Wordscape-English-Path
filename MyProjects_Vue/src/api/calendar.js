import request from '@/utils/axios'

// 获取日历数据
export const getCalendarData = (userId, month, year) => {
  return request.get(`/calendar/${userId}?month=${month}&year=${year}`);
}