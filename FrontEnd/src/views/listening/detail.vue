<script setup>
import { ref, onMounted } from 'vue'
import { getCurrentUserId } from '@/utils/auth.js'
import { ArrowLeft, Star, StarFilled } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { getListeningById, favoriteListening, cancelFavoriteListening, checkFavoriteListening } from '@/api/listening.js'
import { ElMessage } from 'element-plus'

// 获取路由实例和当前路由
const router = useRouter()
const route = useRoute()

// 返回上一页
const goBack = () => {
  router.back()
}

// 听力详情
const listeningDetail = ref(null)
// 加载状态
const loading = ref(false)
// 错误信息
const error = ref('')
// 播放状态
const isPlaying = ref(false)
// 当前播放时间
const currentTime = ref(0)
// 音频总时长
const duration = ref(0)
// 收藏状态
const isFavorite = ref(false)
// 收藏加载状态
const favoriteLoading = ref(false)

// 音频元素引用
const audioElement = ref(null)

// 获取听力详情
const fetchListeningDetail = async () => {
  const id = route.params.id
  if (!id) {
    error.value = '无效的听力ID'
    return
  }

  try {
    loading.value = true
    error.value = ''
    const response = await getListeningById(id)
    
    if (response && response.code === 1 && response.data) {
      listeningDetail.value = response.data
      // 格式化音频时长
      duration.value = response.data.duration
      // 检查收藏状态
      await checkFavorite()
    } else {
      error.value = '获取听力详情失败'
    }
  } catch (err) {
    console.error('获取听力详情失败:', err)
    error.value = '获取听力详情失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 播放/暂停音频
const togglePlay = () => {
  if (!audioElement.value) return
  
  if (isPlaying.value) {
    audioElement.value.pause()
  } else {
    audioElement.value.play()
  }
  isPlaying.value = !isPlaying.value
}

// 监听音频时间更新
const handleTimeUpdate = () => {
  if (audioElement.value) {
    currentTime.value = Math.floor(audioElement.value.currentTime)
  }
}

// 监听音频播放结束
const handleEnded = () => {
  isPlaying.value = false
  currentTime.value = 0
}

// 监听音频加载完成
const handleLoadedMetadata = () => {
  if (audioElement.value) {
    duration.value = Math.floor(audioElement.value.duration)
  }
}

// 拖动进度条
const handleProgressChange = (value) => {
  if (audioElement.value) {
    audioElement.value.currentTime = value
    currentTime.value = value
  }
}

// 格式化时间
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 检查是否收藏
const checkFavorite = async () => {
  if (!listeningDetail.value) return
  
  try {
    const userId = getCurrentUserId()
    const response = await checkFavoriteListening(userId, listeningDetail.value.id)
    if (response && response.code === 1) {
      isFavorite.value = response.data
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 切换收藏状态
const toggleFavorite = async () => {
  if (!listeningDetail.value || favoriteLoading.value) return
  
  favoriteLoading.value = true
  try {
    const userId = getCurrentUserId()
    let response
    
    if (isFavorite.value) {
      // 取消收藏
      response = await cancelFavoriteListening(userId, listeningDetail.value.id)
    } else {
      // 收藏
      response = await favoriteListening(userId, listeningDetail.value.id)
    }
    
    if (response && response.code === 1) {
      isFavorite.value = !isFavorite.value
      ElMessage({
        message: isFavorite.value ? '收藏成功' : '取消收藏成功',
        type: 'success',
        duration: 1000
      })
    } else {
      ElMessage({
        message: isFavorite.value ? '取消收藏失败' : '收藏失败',
        type: 'error',
        duration: 2000
      })
    }
  } catch (error) {
    console.error('操作收藏失败:', error)
    ElMessage({
      message: '操作失败，请稍后重试',
      type: 'error',
      duration: 2000
    })
  } finally {
    favoriteLoading.value = false
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchListeningDetail()
})
</script>

<template>
  <div class="listening-detail-container">
    <!-- 头部导航 -->
    <div class="header">
      <el-button type="text" @click="goBack" class="back-button">
        <el-icon size="24"><ArrowLeft /></el-icon>
      </el-button>
      <h1 class="page-title">听力详情</h1>
      <el-button 
        type="text" 
        @click="toggleFavorite" 
        :loading="favoriteLoading"
        class="favorite-button"
      >
        <el-icon size="32">
          <component :is="isFavorite ? 'StarFilled' : 'Star'" />
        </el-icon>
      </el-button>
    </div>
    
    <!-- 内容区域 -->
    <div class="content-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-loading type="spinner" color="#FF9800" />
        <p>加载中...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-container">
        <p>{{ error }}</p>
        <el-button type="primary" @click="fetchListeningDetail" style="background-color: #1E88E5; border-color: #1E88E5;">重试</el-button>
      </div>
      
      <!-- 听力详情 -->
      <div v-else-if="listeningDetail" class="listening-detail">
        <!-- 音频信息 -->
        <div class="audio-info">
          <h2 class="audio-title">{{ listeningDetail.title }}</h2>
          <div class="audio-meta">
            <span class="audio-duration">时长: {{ formatTime(listeningDetail.duration) }}</span>
            <span class="audio-category">分类: {{ 
              listeningDetail.category === 1 ? '高中听力' : 
              listeningDetail.category === 2 ? '四级听力' : 
              listeningDetail.category === 3 ? '六级听力' : 
              listeningDetail.category === 4 ? '新闻报道' : 
              listeningDetail.category === 5 ? '英文歌曲' : 
              listeningDetail.category === 6 ? '日常对话' : '其他'
            }}</span>
          </div>
        </div>
        
        <!-- 音频播放器 -->
        <div class="audio-player">
          <audio
            ref="audioElement"
            :src="listeningDetail.audioUrl"
            @timeupdate="handleTimeUpdate"
            @ended="handleEnded"
            @loadedmetadata="handleLoadedMetadata"
            class="audio-element"
          ></audio>
          
          <div class="player-controls">
            <el-button 
              type="primary" 
              round 
              class="play-pause-button"
              @click="togglePlay"
            >
              {{ isPlaying ? '暂停' : '播放' }}
            </el-button>
            
            <div class="progress-container">
              <span class="time-current">{{ formatTime(currentTime) }}</span>
              <el-slider
                v-model="currentTime"
                :min="0"
                :max="duration"
                @change="handleProgressChange"
                :format-tooltip="formatTime"
                class="progress-slider"
              ></el-slider>
              <span class="time-total">{{ formatTime(duration) }}</span>
            </div>
          </div>
        </div>
        
        <!-- 听力原文 -->
        <div class="transcript-container">
          <h3 class="transcript-title">听力原文</h3>
          <div class="transcript-content">
            {{ listeningDetail.transcript || '暂无听力原文' }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 全屏容器 */
.listening-detail-container {
  width: 100%;
  min-height: 100vh;
  box-sizing: border-box;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  padding: env(safe-area-inset-top) 1rem env(safe-area-inset-bottom);
  transition: all 0.3s ease;
}

/* 头部导航 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: 600px;
  margin: 0 auto 2rem;
  box-sizing: border-box;
  padding: 1rem 0;
}

.page-title {
  font-size: 1.1rem;
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

.favorite-button {
  display: flex;
  align-items: center;
  color: var(--text-primary);
  padding: 0;
  font-size: 1rem;
  border-radius: 50%;
  width: 56px;
  height: 56px;
  justify-content: center;
  transition: all 0.3s ease;
}

.favorite-button .el-icon {
  color: #FF9800;
}

.favorite-button .el-icon.is-checked {
  color: #FF9800;
}

/* 内容容器 */
.content-container {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  box-sizing: border-box;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 0;
  gap: 1rem;
  width: 100%;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

/* 错误状态 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 0;
  gap: 1rem;
  width: 100%;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.error-container .el-button {
  margin-top: 0.5rem;
  background-color: #FF9800 !important;
  border-color: #FF9800 !important;
}

/* 听力详情 */
.listening-detail {
  width: 100%;
}

/* 音频信息 */
.audio-info {
  background-color: var(--bg-secondary);
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.audio-title {
  font-size: 1.2rem;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 1rem;
  line-height: 1.4;
  transition: all 0.3s ease;
}

.audio-meta {
  display: flex;
  gap: 1.5rem;
  font-size: 0.9rem;
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

/* 音频播放器 */
.audio-player {
  background-color: var(--bg-secondary);
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.audio-element {
  display: none;
}

.player-controls {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.play-pause-button {
  align-self: center;
  padding: 0.8rem 2rem;
  font-size: 1.1rem;
  font-weight: 500;
  background-color: #FF9800 !important;
  border-color: #FF9800 !important;
}

.progress-container {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.time-current,
.time-total {
  font-size: 0.85rem;
  color: var(--text-secondary);
  min-width: 45px;
  text-align: center;
  transition: all 0.3s ease;
}

.progress-slider {
  flex: 1;
}

/* 听力原文 */
.transcript-container {
  background-color: var(--bg-secondary);
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.transcript-title {
  font-size: 1.1rem;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 1rem;
  transition: all 0.3s ease;
}

.transcript-content {
  font-size: 0.95rem;
  line-height: 1.6;
  color: var(--text-primary);
  white-space: pre-wrap; /* 保留原文中的换行 */
  transition: all 0.3s ease;
}

/* 自定义滑块样式 */
:deep(.el-slider__runway) {
  background-color: var(--bg-primary) !important;
}

:deep(.el-slider__bar) {
  background-color: #FF9800 !important;
}

:deep(.el-slider__button) {
  border-color: #FF9800 !important;
}



/* 响应式设计 */
@media (max-width: 480px) {
  .listening-detail-container {
    padding: env(safe-area-inset-top) 0.5rem env(safe-area-inset-bottom);
  }
  
  .header {
    padding: 0.8rem 0;
    margin-bottom: 1.5rem;
  }
  
  .audio-info,
  .audio-player,
  .transcript-container {
    padding: 1.2rem;
    margin-bottom: 1.2rem;
  }
  
  .audio-title {
    font-size: 1.1rem;
  }
  
  .play-pause-button {
    padding: 0.7rem 1.8rem;
    font-size: 1rem;
  }
  
  .progress-container {
    gap: 0.8rem;
  }
}

@media (max-width: 320px) {
  .audio-info,
  .audio-player,
  .transcript-container {
    padding: 1rem;
  }
  
  .audio-title {
    font-size: 1rem;
  }
  
  .play-pause-button {
    padding: 0.6rem 1.5rem;
    font-size: 0.95rem;
  }
}
</style>