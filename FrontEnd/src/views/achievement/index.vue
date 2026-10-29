<template>
  <div class="achievement-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索成就..."
        prefix-icon="Search"
        clearable
        class="search-input"
      />
    </div>
    
    <!-- 内部滚动容器 -->
    <div class="achievement-scroll-container">
      <!-- 成就列表 -->
      <div class="achievement-list">
        <div
          v-for="(achievement, index) in filteredAchievements"
          :key="achievement.id"
          class="achievement-card"
          :class="{ 'achievement-obtained': isAchievementObtained(achievement.id) }"
          :style="{ animationDelay: (index * 0.1) + 's' }"
          @click="openAchievementDialog(achievement)"
        >
          <div class="achievement-image">
            <el-image
              :src="achievement.badgeIcon || achievement.image || 'https://picsum.photos/seed/achievement' + achievement.id + '/200/200'"
              :alt="achievement.name"
              fit="cover"
              :fallback="'/default-achievement.png'"
            />
          </div>
          <div class="achievement-info">
            <div class="achievement-name">
              {{ achievement.name }}
            </div>
          </div>
        </div>
        
        <!-- 加载更多指示器 -->
        <div v-if="loadingMore" class="loading-more">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        
        <!-- 无更多数据提示 -->
        <div v-if="!loading && !loadingMore && achievements.length > 0 && !hasMore" class="no-more">
          <p>已加载全部成就</p>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="!loading && filteredAchievements.length === 0" class="empty-state">
        <el-icon size="48">
          <StarFilled />
        </el-icon>
        <p>暂无成就</p>
      </div>
      
      <!-- 成就详情弹窗 -->
      <el-dialog
        v-model="showAchievementDialog"
        :title="selectedAchievement?.name || '成就详情'"
        width="80%"
        center
      >
        <div class="achievement-dialog-content">
          <div class="achievement-dialog-image">
            <el-image
              :src="selectedAchievement?.badgeIcon || selectedAchievement?.image || 'https://picsum.photos/seed/achievement' + selectedAchievement?.id + '/200/200'"
              :alt="selectedAchievement?.name"
              fit="cover"
              :fallback="'/default-achievement.png'"
            />
          </div>
          <div class="achievement-dialog-info">
            <div class="achievement-dialog-description">
              {{ selectedAchievement?.description || '暂无描述' }}
            </div>
            <div class="achievement-dialog-status">
              <span :class="{ 'status-obtained': isAchievementObtained(selectedAchievement?.id) }">
                {{ isAchievementObtained(selectedAchievement?.id) ? '已获得' : '未获得' }}
              </span>
            </div>
            <div v-if="isAchievementObtained(selectedAchievement?.id)" class="achievement-dialog-time">
              <span v-if="!loadingObtainedTime">
                获取时间: {{ obtainedTime || '未知' }}
              </span>
              <span v-else>
                加载中...
              </span>
            </div>
          </div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCurrentUserId } from '@/utils/auth.js'
import { StarFilled, Loading } from '@element-plus/icons-vue'
import { getAchievement, getUserAchievement, getObtainedTime } from '@/api/achievement'
import { useTheme } from '@/composables/useTheme'

// 成就数据
const achievements = ref([])
// 用户已获得的成就ID列表
const userAchievementIds = ref([])
// 加载状态
const loading = ref(true)
// 加载更多状态
const loadingMore = ref(false)
// 搜索查询
const searchQuery = ref('')
// 当前页码
const currentPage = ref(1)
// 是否还有更多数据
const hasMore = ref(true)
// 成就详情弹窗
const showAchievementDialog = ref(false)
// 当前选中的成就
const selectedAchievement = ref(null)
// 成就获取时间
const obtainedTime = ref('')
// 获取时间加载状态
const loadingObtainedTime = ref(false)

// 获取成就数据
const fetchAchievements = async (isLoadMore = false) => {
  try {
    if (isLoadMore) {
      loadingMore.value = true
    } else {
      loading.value = true
      currentPage.value = 1
      achievements.value = []
    }
    
    // 获取全部成就信息
    const achievementResponse = await getAchievement(currentPage.value)
    if (achievementResponse.code && achievementResponse.data) {
      const newAchievements = achievementResponse.data
      if (isLoadMore) {
        achievements.value = [...achievements.value, ...newAchievements]
      } else {
        achievements.value = newAchievements
      }
      
      // 检查是否还有更多数据
      hasMore.value = newAchievements.length > 0
      if (hasMore.value) {
        currentPage.value++
      }
    } else {
      hasMore.value = false
    }
    
    // 只在首次加载时获取用户成就信息
    if (!isLoadMore) {
      // 获取用户成就信息
      const userId = getCurrentUserId()
      const userAchievementResponse = await getUserAchievement(userId)
      if (userAchievementResponse.code && userAchievementResponse.data) {
        userAchievementIds.value = userAchievementResponse.data
      }
    }
  } catch (error) {
    console.error('获取成就数据失败:', error)
    hasMore.value = false
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 检查成就是否已获得
const isAchievementObtained = (achievementId) => {
  return userAchievementIds.value.includes(achievementId)
}

// 格式化时间函数
const formatTime = (timeString) => {
  if (!timeString) return ''
  // 将ISO格式的时间转换为更友好的格式
  return timeString.replace('T', ' ')
}

// 打开成就详情弹窗
const openAchievementDialog = async (achievement) => {
  selectedAchievement.value = achievement
  obtainedTime.value = ''
  
  // 如果成就是已获得的，获取获取时间
  if (isAchievementObtained(achievement.id)) {
    loadingObtainedTime.value = true
    try {
      const userId = getCurrentUserId()
      const response = await getObtainedTime(userId, achievement.id)
      if (response.code && response.data) {
        obtainedTime.value = formatTime(response.data)
      }
    } catch (error) {
      console.error('获取成就获取时间失败:', error)
    } finally {
      loadingObtainedTime.value = false
    }
  }
  
  showAchievementDialog.value = true
}

// 关闭成就详情弹窗
const closeAchievementDialog = () => {
  showAchievementDialog.value = false
  selectedAchievement.value = null
}

// 过滤后的成就列表
const filteredAchievements = computed(() => {
  let result = achievements.value
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(achievement => 
      achievement.name.toLowerCase().includes(query)
    )
  }
  
  return result
})

// 初始化主题
const { initTheme } = useTheme()

// 组件挂载时获取数据
onMounted(() => {
  fetchAchievements()
  
  // 添加滚动事件监听
  const scrollContainer = document.querySelector('.achievement-scroll-container')
  if (scrollContainer) {
    scrollContainer.addEventListener('scroll', handleScroll)
  }
  
  // 初始化主题
  initTheme()
})

// 处理滚动事件
const handleScroll = () => {
  const scrollContainer = document.querySelector('.achievement-scroll-container')
  if (!scrollContainer) return
  
  const { scrollTop, scrollHeight, clientHeight } = scrollContainer
  
  // 当滚动到距离底部100px时加载更多
  if (scrollHeight - scrollTop - clientHeight < 100 && !loading.value && !loadingMore.value && hasMore.value) {
    fetchAchievements(true)
  }
}
</script>

<style scoped>
/* 主容器 */
.achievement-container {
  padding: 0;
  width: 100%;
  height: 100vh;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  /* 确保容器不会产生自己的滚动条 */
  overflow: hidden;
  background: linear-gradient(180deg, #ffffff 0%, #e6f0ff 50%, #4a6fa5 100%);
  color: var(--text-primary);
  transition: all 0.3s ease;
  margin: 0;
}

/* 黑夜模式样式 */
html.dark .achievement-container {
  background: linear-gradient(180deg, #1a1a1a 0%, #2d2d2d 50%, #121212 100%);
  color: var(--text-primary);
}

/* 搜索栏样式 */
.search-bar {
  padding: 0 1rem 1rem;
  background-color: var(--bg-primary);
  z-index: 10;
  border-bottom: 1px solid var(--border-color);
  width: 100%;
  box-sizing: border-box;
  /* 确保搜索栏固定在顶部 */
  flex-shrink: 0;
  height: 60px;
  transition: all 0.3s ease;
}

.search-input {
  width: 100%;
  border-radius: 20px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  background-color: var(--bg-secondary) !important;
  color: var(--text-primary) !important;
  border: 1px solid var(--border-color) !important;
  transition: all 0.3s ease;
}

/* 确保在黑夜模式下搜索框显示正确的背景色 */
:deep(.el-input__wrapper) {
  background-color: var(--bg-secondary) !important;
  box-shadow: none !important;
  border: 1px solid var(--border-color) !important;
}

:deep(.el-input__inner) {
  color: var(--text-primary) !important;
}

:deep(.el-input__prefix-inner) {
  color: var(--text-tertiary) !important;
}

/* 内部滚动容器 - 核心：确保这是唯一可以滚动的元素 */
.achievement-scroll-container {
  /* 核心：设置明确的高度，确保可以滚动 */
  height: calc(100vh - 140px); /* 调整高度计算，确保有足够空间 */
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  /* 增加底部内边距，确保可以看到所有成就 */
  padding-bottom: 5rem;
  /* 确保触摸事件能正常触发 */
  touch-action: pan-y;
  pointer-events: auto;
  /* 确保滚动容器占满可用空间 */
  flex: 1;
  /* 添加一些内边距 */
  padding-left: 0.5rem;
  padding-right: 0.5rem;
}

/* 确保滚动条在所有浏览器中都能正常工作 */
.achievement-scroll-container {
  scrollbar-width: thin;
  scrollbar-color: var(--border-color) transparent;
}

/* 滚动条样式 - 确保内部滚动条可见 */
.achievement-scroll-container::-webkit-scrollbar {
  width: 6px;
}

.achievement-scroll-container::-webkit-scrollbar-track {
  background: transparent;
}

.achievement-scroll-container::-webkit-scrollbar-thumb {
  background-color: var(--border-color);
  border-radius: 3px;
}

/* 成就列表样式 */
.achievement-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr); /* 两列布局 */
  gap: 1rem;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
  padding: 0.5rem;
  /* 确保列表内容能正常扩展，但不会被拉伸 */
  overflow: visible;
  /* 移除min-height，防止卡片被拉伸 */
  /* 添加额外的底部空间 */
  margin-bottom: 2rem;
}

/* 成就卡片样式 */
.achievement-card {
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  background-color: var(--bg-secondary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  position: relative;
  /* 只设置固定高度，不设置min-height，避免被拉伸 */
  height: 180px;
  /* 默认灰度效果 */
  filter: grayscale(100%);
  opacity: 0;
  transform: translateY(30px);
  animation: achievementFadeIn 0.5s ease-out forwards;
}

/* 已获得成就的样式 */
.achievement-card.achievement-obtained {
  filter: grayscale(0%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}

.achievement-card:active {
  transform: scale(0.98);
}

/* 成就图片容器 */
.achievement-image {
  width: 100%;
  aspect-ratio: 1 / 1;
  position: relative;
  background-color: var(--bg-tertiary);
  transition: all 0.3s ease;
}

.achievement-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.achievement-card:active .achievement-image img {
  transform: scale(1.05);
}

/* 成就徽章 */
.achievement-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.achievement-badge .el-icon {
  color: #e6a23c;
  font-size: 16px;
}

/* 成就信息区域 */
.achievement-info {
  padding: 0.8rem;
  display: flex;
  flex-direction: column;
  /* 确保信息区域不会被拉伸 */
  justify-content: center;
  height: 70px;
}

/* 成就名称样式 */
.achievement-name {
  font-size: 1rem;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  text-align: center;
  min-height: 2.8rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 0;
  color: var(--text-tertiary);
  width: 100%;
}

.empty-state .el-icon {
  margin-bottom: 1rem;
  color: #e6a23c;
}

.empty-state p {
  margin-bottom: 1.5rem;
}

/* 加载更多指示器 */
.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem 0;
  color: var(--text-tertiary);
  gap: 0.5rem;
  grid-column: 1 / -1;
}

/* 无更多数据提示 */
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem 0;
  color: var(--text-tertiary);
  grid-column: 1 / -1;
}

/* 成就详情弹窗样式 */
.achievement-dialog-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
  padding: 1rem;
  background-color: var(--bg-secondary);
  border-radius: 8px;
  transition: all 0.3s ease;
}

/* 弹窗样式 */
:deep(.el-dialog__header) {
  background-color: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

:deep(.el-dialog__title) {
  color: var(--text-primary);
}

:deep(.el-dialog__body) {
  background-color: var(--bg-secondary);
  padding: 0;
}

:deep(.el-dialog) {
  background-color: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
}

:deep(.el-dialog__close) {
  color: var(--text-primary);
}

.achievement-dialog-image {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.achievement-dialog-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.achievement-dialog-info {
  width: 100%;
  text-align: center;
}

.achievement-dialog-description {
  font-size: 1rem;
  color: var(--text-primary);
  line-height: 1.5;
  margin-bottom: 1rem;
  padding: 0 1rem;
}

.achievement-dialog-status {
  display: inline-block;
  padding: 0.3rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

.achievement-dialog-status.status-obtained {
  background-color: rgba(103, 194, 58, 0.1);
  color: #67c23a;
  border: 1px solid #c2e7ff;
}

.achievement-dialog-status:not(.status-obtained) {
  background-color: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
  border: 1px solid #f5d0a9;
}

.achievement-dialog-time {
  margin-top: 1rem;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

/* 响应式设计 */

/* 平板设备 */
@media (min-width: 768px) {
  .achievement-list {
    grid-template-columns: repeat(2, 1fr);
    gap: 1.2rem;
    padding: 1.5rem;
  }
  
  .achievement-name {
    font-size: 1rem;
  }
}

/* 大屏手机 */
@media (max-width: 767px) {
  .achievement-list {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.8rem;
    padding: 1rem;
  }
  
  .achievement-card {
    border-radius: 10px;
    min-height: 160px;
  }
  
  .achievement-info {
    padding: 0.6rem;
  }
  
  .achievement-name {
    font-size: 0.95rem;
    min-height: 2.4rem;
  }
}

/* 小屏手机 */
@media (max-width: 480px) {
  .achievement-list {
    grid-template-columns: repeat(2, 1fr); /* 保持两列布局 */
    gap: 0.6rem;
    padding: 0.8rem;
  }
  
  .search-bar {
    padding: 0 0.8rem 0.8rem;
  }
  
  .achievement-card {
    border-radius: 10px;
    min-height: 140px;
  }
  
  .achievement-info {
    padding: 0.5rem;
  }
  
  .achievement-name {
    font-size: 0.9rem;
    min-height: 2.2rem;
  }
  
  .achievement-badge {
    width: 24px;
    height: 24px;
    top: 6px;
    right: 6px;
  }
  
  .achievement-badge .el-icon {
    font-size: 14px;
  }
}

/* 超小屏手机 */
@media (max-width: 320px) {
  .achievement-list {
    gap: 0.5rem;
    padding: 0.5rem;
  }
  
  .achievement-card {
    min-height: 120px;
  }
  
  .achievement-info {
    padding: 0.4rem;
  }
  
  .achievement-name {
    font-size: 0.85rem;
  }
}

/* 动画效果 */
@keyframes achievementFadeIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 确保所有子元素都不会产生自己的滚动条 */
.achievement-container *:not(.achievement-scroll-container) {
  overflow: hidden !important;
}

/* 确保列表项不会产生自己的滚动条 */
.achievement-card,
.achievement-image,
.achievement-info {
  overflow: hidden;
}
</style>