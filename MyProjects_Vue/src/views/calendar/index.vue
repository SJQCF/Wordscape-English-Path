<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ArrowLeft, ArrowLeft as ArrowPrev, ArrowRight as ArrowNext } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getCalendarData } from '@/api/calendar.js'
import { useTheme } from '@/composables/useTheme'

const { initTheme } = useTheme()

// 获取路由实例
const router = useRouter()

// 返回上一页
const goBack = () => {
  router.back()
}

// 当前月份
const currentMonth = ref(new Date())
// 学习数据
const studyData = ref([])
// 加载状态
const loading = ref(false)
// 错误信息
const error = ref('')

// 获取学习数据
const fetchStudyData = async () => {
  try {
    loading.value = true
    error.value = ''
    const year = currentMonth.value.getFullYear()
    const month = currentMonth.value.getMonth() + 1 // API需要的是1-12月
    
    // 假设用户ID为1
    const response = await getCalendarData(1, month, year)
    
    // 检查API返回的数据格式（根据您提供的实际数据格式）
    if (response && response.code === 1 && Array.isArray(response.data)) {
      // 转换API返回的数据格式
      studyData.value = response.data.map(item => ({
        // 创建完整的日期对象
        date: new Date(year, month - 1, item.day),
        // 映射字段名
        studyCount: item.studyWordsNum,
        reviewCount: item.reviewWordsNum,
        // 保留原始数据
        ...item
      }))
      console.log('API数据获取成功:', studyData.value)
    } else {
      console.log('API返回数据为空或格式不正确，使用模拟数据')
      // API返回数据为空或格式不正确，使用模拟数据
      studyData.value = generateMockStudyData(year, month)
    }
  } catch (err) {
    console.error('获取学习数据失败:', err)
    error.value = '获取学习数据失败，已使用模拟数据'
    // 使用模拟数据作为备选
    const year = currentMonth.value.getFullYear()
    const month = currentMonth.value.getMonth() + 1
    studyData.value = generateMockStudyData(year, month)
  } finally {
    loading.value = false
  }
}

// 生成模拟学习数据（兼容实际API格式）
const generateMockStudyData = (year, month) => {
  const mockData = []
  const daysInMonth = new Date(year, month, 0).getDate()
  
  // 随机生成一些学习记录（70%的天数有学习记录）
  for (let i = 1; i <= daysInMonth; i++) {
    if (Math.random() > 0.3) {
      // 模拟API返回的数据格式
      mockData.push({
        day: i,
        studyWordsNum: Math.floor(Math.random() * 30) + 5, // 5-34个新学单词
        reviewWordsNum: Math.floor(Math.random() * 50) + 10, // 10-59个复习单词
        // 同时添加处理逻辑需要的字段
        date: new Date(year, month - 1, i),
        studyCount: Math.floor(Math.random() * 30) + 5,
        reviewCount: Math.floor(Math.random() * 50) + 10
      })
    }
  }
  
  // 确保今天有学习记录
  const today = new Date()
  if (today.getFullYear() === year && today.getMonth() + 1 === month) {
    const todayData = mockData.find(item => item.day === today.getDate())
    
    if (!todayData) {
      const studyNum = Math.floor(Math.random() * 30) + 5
      const reviewNum = Math.floor(Math.random() * 50) + 10
      mockData.push({
        day: today.getDate(),
        studyWordsNum: studyNum,
        reviewWordsNum: reviewNum,
        date: today,
        studyCount: studyNum,
        reviewCount: reviewNum
      })
    }
  }
  
  console.log('生成模拟数据:', mockData)
  return mockData
}

// 当前选中的日期
const selectedDate = ref(null)

// 生成日历数据
const calendarData = computed(() => {
  const year = currentMonth.value.getFullYear()
  const month = currentMonth.value.getMonth()
  
  // 获取当月第一天
  const firstDay = new Date(year, month, 1)
  // 获取当月最后一天
  const lastDay = new Date(year, month + 1, 0)
  
  // 当月天数
  const daysInMonth = lastDay.getDate()
  // 当月第一天是星期几（0=周日, 1=周一, ..., 6=周六）
  const firstDayOfWeek = firstDay.getDay()
  
  // 计算当月最大学习和复习数量（用于颜色深浅计算）
  const maxStudyCount = Math.max(...studyData.value.map(item => item.studyCount || 0), 1)
  const maxReviewCount = Math.max(...studyData.value.map(item => item.reviewCount || 0), 1)
  
  const calendar = []
  
  // 添加上个月的日期
  // 星期标题顺序：MON, TUE, WED, THU, FRI, SAT, SUN
  // 需要计算在当月第一天前需要显示多少天上个月的日期
  let daysToAddBefore = firstDayOfWeek
  if (firstDayOfWeek === 0) {
    // 当月第一天是周日，需要添加上个月的6天（MON-SAT）
    daysToAddBefore = 6
  } else {
    // 当月第一天不是周日，需要添加上个月的(firstDayOfWeek - 1)天
    daysToAddBefore = firstDayOfWeek - 1
  }
  
  for (let i = daysToAddBefore; i > 0; i--) {
    const prevDate = new Date(year, month, -i + 1)
    calendar.push({
      date: prevDate,
      day: prevDate.getDate(),
      isCurrentMonth: false,
      isStudied: false,
      isToday: false,
      studyCount: 0,
      reviewCount: 0,
      intensity: 0 // 颜色强度（0-1）
    })
  }
  
  // 添加当月的日期
  for (let i = 1; i <= daysInMonth; i++) {
    const currentDate = new Date(year, month, i)
    const today = new Date()
    
    // 改进的当天日期判断逻辑
    const isToday = currentDate.getDate() === today.getDate() &&
                    currentDate.getMonth() === today.getMonth() &&
                    currentDate.getFullYear() === today.getFullYear()
    
    // 添加调试信息
    console.log(`检查日期 ${year}-${month+1}-${i} 是否为今天:`, isToday)
    
    // 查找当天的学习数据
    const dayData = studyData.value.find(item => {
      // 方法1：使用day字段直接匹配（更高效）
      if (item.day) {
        return item.day === i
      }
      // 方法2：使用完整日期匹配（兼容模拟数据和API数据）
      const itemDate = item.date instanceof Date ? item.date : new Date(item.date)
      if (isNaN(itemDate.getTime())) {
        console.warn('无效的日期:', item.date)
        return false
      }
      return itemDate.getDate() === i && 
             itemDate.getMonth() === month && 
             itemDate.getFullYear() === year
    })
    
    // 添加调试信息
    if (dayData) {
      console.log(`找到第${i}天的学习数据:`, dayData)
    } else {
      console.log(`未找到第${i}天的学习数据`)
    }
    
    // 计算颜色强度（综合学习和复习数量）
    const studyIntensity = dayData?.studyCount ? (dayData.studyCount / maxStudyCount) * 0.6 : 0
    const reviewIntensity = dayData?.reviewCount ? (dayData.reviewCount / maxReviewCount) * 0.4 : 0
    const totalIntensity = studyIntensity + reviewIntensity
    
    calendar.push({
      date: currentDate,
      day: i,
      isCurrentMonth: true,
      isStudied: !!dayData,
      isToday: isToday,
      studyCount: dayData?.studyCount || 0,
      reviewCount: dayData?.reviewCount || 0,
      intensity: totalIntensity // 颜色强度（0-1）
    })
  }
  
  // 添加下个月的日期，补满6行
  const remainingDays = 42 - calendar.length
  for (let i = 1; i <= remainingDays; i++) {
    const nextDate = new Date(year, month + 1, i)
    calendar.push({
      date: nextDate,
      day: nextDate.getDate(),
      isCurrentMonth: false,
      isStudied: false,
      isToday: false,
      studyCount: 0,
      reviewCount: 0,
      intensity: 0
    })
  }
  
  return calendar
})

// 处理日期点击事件
const handleDayClick = (day) => {
  selectedDate.value = day
}

// 格式化日期显示
const formatDate = (date) => {
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

// 监听月份变化，重新获取数据
watch(currentMonth, () => {
  fetchStudyData()
})

// 月份切换
const prevMonth = () => {
  currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() - 1, 1)
}

const nextMonth = () => {
  currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + 1, 1)
}

// 月份标题
const monthTitle = computed(() => {
  return currentMonth.value.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long' })
})

// 获取学习天数统计
const totalDays = computed(() => {
  const year = currentMonth.value.getFullYear()
  const month = currentMonth.value.getMonth()
  return studyData.value.filter(item => {
    const itemDate = new Date(item.date)
    return itemDate.getMonth() === month && 
           itemDate.getFullYear() === year
  }).length
})

// 组件挂载时获取数据
onMounted(() => {
  initTheme()
  fetchStudyData()
})
</script>

<template>
  <div class="calendar-container">
    <!-- 返回按钮 -->
    <div class="header">
      <el-button type="text" @click="goBack" class="back-button">
        <el-icon size="24" class="back-icon"><ArrowLeft /></el-icon>
      </el-button>
      <h1 class="page-title">学习日历</h1>
      <div class="header-placeholder"></div>
    </div>
    
    <!-- 日历卡片 -->
    <div class="calendar-card">
      <!-- 月份导航 -->
      <div class="month-navigation">
        <el-button type="text" @click="prevMonth" class="month-nav-button">
          <el-icon size="20"><ArrowPrev /></el-icon>
        </el-button>
        <h2 class="month-title">{{ monthTitle }}</h2>
        <el-button type="text" @click="nextMonth" class="month-nav-button">
          <el-icon size="20"><ArrowNext /></el-icon>
        </el-button>
      </div>
      
      <!-- 星期标题 -->
      <div class="weekdays">
        <div class="weekday-item">MON</div>
        <div class="weekday-item">TUE</div>
        <div class="weekday-item">WED</div>
        <div class="weekday-item">THU</div>
        <div class="weekday-item">FRI</div>
        <div class="weekday-item">SAT</div>
        <div class="weekday-item">SUN</div>
      </div>
      
      <!-- 日期网格 -->
      <div class="calendar-grid">
        <div 
          v-for="(day, index) in calendarData" 
          :key="index" 
          class="calendar-day"
          :class="{
            'other-month': !day.isCurrentMonth,
            'studied-day': day.isStudied,
            'today': day.isToday,
            'selected-day': selectedDate && selectedDate.date.toDateString() === day.date.toDateString()
          }"
          :style="{
            // 移除当天日期的特殊背景色
            backgroundColor: day.isCurrentMonth && day.intensity > 0 
              ? `rgba(64, 158, 255, ${0.2 + day.intensity * 0.6})` 
              : 'transparent'
          }"
          @click="handleDayClick(day)"
        >
          <div class="day-number">{{ day.day }}</div>
        </div>
      </div>
      
      <!-- 选中日期详情 -->
      <div v-if="selectedDate" class="selected-day-details">
        <h3 class="details-title">{{ formatDate(selectedDate.date) }}</h3>
        <div class="details-content">
          <div v-if="selectedDate.studyCount > 0 || selectedDate.reviewCount > 0" class="study-stats">
            <div class="stat-item">
              <div class="stat-label">新学单词</div>
              <div class="stat-value study-value">{{ selectedDate.studyCount }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">复习单词</div>
              <div class="stat-value review-value">{{ selectedDate.reviewCount }}</div>
            </div>
          </div>
          <div v-else class="no-study-data">
            <p>当天没有学习记录</p>
          </div>
        </div>
      </div>
      
      <!-- 加载和错误状态 -->
      <div v-if="loading" class="loading-container">
        <el-loading type="spinner" color="#FF9800" />
        <p>加载中...</p>
      </div>
    <!-- 错误状态 -->
      <div v-else-if="error" class="error-container">
        <p>{{ error }}</p>
        <el-button type="primary" @click="fetchStudyData" style="background-color: #1E88E5; border-color: #1E88E5;">重试</el-button>
      </div>
      
      <!-- 学习统计 -->
    <div class="sign-in-stats">
      <div class="stat-item">
        <div class="stat-label">共学习</div>
        <div class="stat-value">{{ totalDays }}天</div>
      </div>
    </div>
    </div>
  </div>
</template>

<style scoped>
/* 全屏容器 */
.calendar-container {
  width: 100%;
  height: 100vh;
  padding: 1rem;
  box-sizing: border-box;
  background-color: var(--bg-primary);
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
  /* 适配刘海屏 */
  padding-top: env(safe-area-inset-top);
  padding-left: env(safe-area-inset-left);
  padding-right: env(safe-area-inset-right);
  /* 确保在手机上不出现双滚动条 */
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  transition: all 0.3s ease;
}

/* 头部 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: 400px;
  margin-bottom: 1.5rem;
}

.page-title {
  font-size: 0.9rem;
  font-weight: bold;
  color: var(--text-primary);
  margin: 0;
  transition: all 0.3s ease;
}

.back-button {
  display: flex;
  align-items: center;
  color: var(--text-primary);
  padding: 0;
  font-size: 1rem;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  justify-content: center;
  transition: all 0.3s ease;
}

.back-icon {
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.header-placeholder {
  width: 40px;
}

/* 日历卡片 */
.calendar-card {
  width: 100%;
  max-width: 400px;
  background-color: var(--bg-secondary);
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: all 0.3s ease;
}

/* 月份导航 */
.month-navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding: 0 0.5rem;
}

.month-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  transition: all 0.3s ease;
}

.month-nav-button {
  color: var(--text-primary);
  padding: 0.5rem;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.month-nav-button:hover {
  background-color: transparent;
  color: var(--text-primary);
}

/* 星期标题 */
.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 0;
  margin-bottom: 1.5rem;
}

.weekday-item {
  text-align: center;
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--text-secondary);
  padding: 0.5rem 0;
  transition: all 0.3s ease;
}

/* 日期网格 */
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  padding: 0 0.2rem;
  min-height: 350px;
}

.calendar-day {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.3s ease;
  min-height: 32px;
  font-size: 0.85rem;
}

.other-month {
  color: var(--text-tertiary);
}

.studied-day {
  background-color: rgba(64, 158, 255, 0.2);
}

.today {
  /* 移除当天日期的特殊样式 */
  font-weight: 500;
}

.selected-day {
  border: 2px solid #409EFF;
  transform: scale(1.1);
  z-index: 10;
}

.day-number {
  font-size: 0.9rem;
  font-weight: 500;
}

/* 选中日期详情 */
.selected-day-details {
  background-color: var(--bg-secondary);
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-top: 1rem;
  transition: all 0.3s ease;
}

.details-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
  text-align: center;
  transition: all 0.3s ease;
}

.details-content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80px;
}

.study-stats {
  display: flex;
  gap: 2rem;
  justify-content: center;
  width: 100%;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-label {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
  transition: all 0.3s ease;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.study-value {
  color: #67C23A;
}

.review-value {
  color: #E6A23C;
}

.no-study-data {
  color: var(--text-tertiary);
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

/* 学习标签 */
.study-tags {
  position: absolute;
  bottom: -25px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 4px;
  flex-direction: column;
  align-items: center;
}

.tag {
  font-size: 0.7rem;
  padding: 2px 6px;
  border-radius: 10px;
  color: white;
  white-space: nowrap;
}

.study-tag {
  background-color: #67C23A;
}

.review-tag {
  background-color: #E6A23C;
}



/* 加载和错误状态 */
.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 0;
  gap: 1rem;
  margin-bottom: 1rem;
}

.error-container p {
  color: var(--danger-color);
  margin: 0;
  transition: all 0.3s ease;
}



/* 学习统计 */
.sign-in-stats {
  display: flex;
  justify-content: center;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-label {
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-bottom: 0.3rem;
  transition: all 0.3s ease;
}

.stat-value {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .calendar-container {
    padding: 0.8rem;
  }
  
  .header {
    margin-bottom: 1rem;
  }
  
  .calendar-card {
    padding: 1rem;
    border-radius: 12px;
  }
  
  .month-navigation {
    margin-bottom: 1rem;
  }
  
  .month-title {
    font-size: 1.1rem;
  }
  
  .weekdays {
    margin-bottom: 0.8rem;
    gap: 0.4rem;
    padding: 0 0.1rem;
  }
  
  .weekday-item {
    font-size: 0.7rem;
    padding: 0.2rem 0;
  }
  
  .calendar-grid {
    gap: 0.4rem;
    margin-bottom: 1rem;
    padding: 0 0.1rem;
    min-height: 300px;
  }
  
  .calendar-day {
    min-height: 28px;
    font-size: 0.8rem;
  }
  
  .day-number {
    font-size: 0.8rem;
  }
  
  .sign-in-stats {
    padding-top: 1rem;
  }
  
  .stat-label {
    font-size: 0.75rem;
  }
  
  .stat-value {
    font-size: 1.1rem;
  }
}

@media (max-width: 360px) {
  .calendar-container {
    padding: 0.6rem;
  }
  
  .calendar-card {
    padding: 0.8rem;
    border-radius: 10px;
  }
  
  .month-title {
    font-size: 1rem;
  }
  
  .weekdays {
    gap: 0.3rem;
    padding: 0;
  }
  
  .weekday-item {
    font-size: 0.65rem;
  }
  
  .calendar-grid {
    gap: 0.3rem;
    padding: 0;
    min-height: 280px;
  }
  
  .calendar-day {
    min-height: 25px;
    font-size: 0.75rem;
  }
  
  .day-number {
    font-size: 0.75rem;
  }
  
  .stat-label {
    font-size: 0.7rem;
  }
  
  .stat-value {
    font-size: 1rem;
  }
}

@media (max-width: 320px) {
  .calendar-container {
    padding: 0.5rem;
  }
  
  .weekdays {
    gap: 0.2rem;
  }
  
  .calendar-grid {
    gap: 0.2rem;
    min-height: 250px;
  }
  
  .calendar-day {
    min-height: 22px;
    font-size: 0.7rem;
  }
  
  .day-number {
    font-size: 0.7rem;
  }
}

/* 平板设备优化 */
@media (min-width: 768px) and (max-width: 1024px) {
  .calendar-container {
    padding: 2rem;
  }
  
  .calendar-card {
    max-width: 500px;
    padding: 2rem;
    border-radius: 20px;
  }
  
  .month-title {
    font-size: 1.3rem;
  }
  
  .weekday-item {
    font-size: 0.9rem;
    padding: 0.6rem 0;
  }
  
  .calendar-grid {
    gap: 1.2rem;
  }
  
  .calendar-day {
    min-height: 42px;
  }
  
  .day-number {
    font-size: 1rem;
  }
  

  
  .stat-label {
    font-size: 0.9rem;
  }
  
  .stat-value {
    font-size: 1.3rem;
  }
}
/* 深色模式 */
.dark-mode .calendar-card {
  background-color: var(--bg-secondary);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.dark-mode .weekday-item {
  color: var(--text-secondary);
}

.dark-mode .day-number {
  color: var(--text-primary);
}

.dark-mode .other-month {
  color: var(--text-tertiary);
}

.dark-mode .selected-day-details {
  background-color: var(--bg-secondary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.dark-mode .details-title {
  color: var(--text-primary);
}

.dark-mode .stat-label {
  color: var(--text-secondary);
}

.dark-mode .stat-value {
  color: #FF9800;
}

.dark-mode .no-study-data {
  color: var(--text-tertiary);
}
</style>