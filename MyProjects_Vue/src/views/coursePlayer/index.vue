<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { getVideoUrl, Courses } from '@/api/course'
import { ElMessage } from 'element-plus'
import { useTheme } from '@/composables/useTheme'

// 初始化主题
const { initTheme } = useTheme();

// 视频元素引用
const videoRef = ref(null);
// 原始屏幕方向
const originalOrientation = ref(null);

// 获取路由实例
const router = useRouter()
// 获取当前路由
const route = useRoute()

// 课程数据
const course = ref(null)
// 视频地址
const videoUrl = ref('')
// 加载状态
const loading = ref(true)
// 错误信息
const error = ref('')
// 其他课程数据
const otherCourses = ref([])

// 返回上一页
const goBack = () => {
  router.back()
}

// 获取课程视频地址
const fetchVideoUrl = async () => {
  try {
    loading.value = true
    error.value = ''
    
    // 获取路由参数中的课程ID
    const courseId = route.params.id || route.query.id
    if (!courseId) {
      throw new Error('缺少课程ID')
    }
    
    // 获取视频地址
    const response = await getVideoUrl(courseId)
    if (response.code && response.data) {
      videoUrl.value = response.data
    } else {
      throw new Error('获取视频地址失败')
    }
    
    // 获取其他课程数据
    await fetchOtherCourses()
  } catch (err) {
    console.error('获取视频地址失败:', err)
    error.value = err.message || '获取视频地址失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 获取其他课程数据
const fetchOtherCourses = async () => {
  try {
    // 获取课程列表
    const response = await Courses(1)
    if (response.code && response.data) {
      // 过滤掉当前课程
      const courseId = route.params.id || route.query.id
      console.log('当前课程ID:', courseId);
      console.log('获取到的课程列表:', response.data);
      // 使用严格比较，确保类型一致
      otherCourses.value = response.data.filter(c => String(c.id) !== String(courseId))
      console.log('过滤后的其他课程:', otherCourses.value);
    }
  } catch (err) {
    console.error('获取其他课程失败:', err)
  }
}

// 切换到其他课程
const switchToCourse = (courseId) => {
  console.log('切换到课程:', courseId);
  // 确保courseId是字符串类型
  const id = String(courseId);
  console.log('跳转到课程详情页:', `/course/${id}`);
  // 跳转到课程详情页
  router.push(`/course/${id}`);
}

// 处理全屏事件
const handleFullscreenChange = () => {
  if (document.fullscreenElement === videoRef.value || 
      document.webkitFullscreenElement === videoRef.value || 
      document.mozFullScreenElement === videoRef.value || 
      document.msFullscreenElement === videoRef.value) {
    // 进入全屏，尝试横屏
    try {
      if (screen.orientation) {
        originalOrientation.value = screen.orientation.angle;
        // 尝试锁定横屏，支持不同的横屏方向
        screen.orientation.lock('landscape').catch(err => {
          // 如果锁定失败，尝试其他横屏方向
          if (err) {
            console.log('无法锁定屏幕方向:', err);
            // 尝试锁定特定的横屏方向
            try {
              screen.orientation.lock('landscape-primary').catch(() => {
                console.log('无法锁定landscape-primary');
                // 尝试另一种横屏方向
                screen.orientation.lock('landscape-secondary').catch(() => {
                  console.log('无法锁定landscape-secondary');
                });
              });
            } catch (e) {
              console.log('尝试锁定屏幕方向失败:', e);
            }
          }
        });
      } else if (screen.lockOrientation) {
        // 旧版API兼容
        originalOrientation.value = window.orientation;
        screen.lockOrientation('landscape');
      } else if (screen.mozLockOrientation) {
        // Firefox 兼容
        originalOrientation.value = window.orientation;
        screen.mozLockOrientation('landscape');
      } else if (screen.msLockOrientation) {
        // IE 兼容
        originalOrientation.value = window.orientation;
        screen.msLockOrientation('landscape');
      }
    } catch (err) {
      console.log('屏幕方向控制失败:', err);
    }
  } else {
    // 退出全屏，恢复原始方向
    try {
      if (screen.orientation) {
        screen.orientation.unlock();
      } else if (screen.unlockOrientation) {
        // 旧版API兼容
        screen.unlockOrientation();
      } else if (screen.mozUnlockOrientation) {
        // Firefox 兼容
        screen.mozUnlockOrientation();
      } else if (screen.msUnlockOrientation) {
        // IE 兼容
        screen.msUnlockOrientation();
      }
    } catch (err) {
      console.log('恢复屏幕方向失败:', err);
    }
  }
};

// 组件挂载时获取数据
onMounted(() => {
  initTheme();
  fetchVideoUrl();
  
  // 添加全屏事件监听
  if (videoRef.value) {
    document.addEventListener('fullscreenchange', handleFullscreenChange);
    document.addEventListener('webkitfullscreenchange', handleFullscreenChange);
    document.addEventListener('mozfullscreenchange', handleFullscreenChange);
    document.addEventListener('MSFullscreenChange', handleFullscreenChange);
  }
});

// 组件卸载时移除事件监听
onUnmounted(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange);
  document.removeEventListener('webkitfullscreenchange', handleFullscreenChange);
  document.removeEventListener('mozfullscreenchange', handleFullscreenChange);
  document.removeEventListener('MSFullscreenChange', handleFullscreenChange);
  
  // 确保退出全屏时恢复屏幕方向
  try {
    if (screen.orientation) {
      screen.orientation.unlock();
    }
  } catch (err) {
    console.log('恢复屏幕方向失败:', err);
  }
});
</script>

<template>
  <div class="course-player-container">
    <!-- 顶部固定区域 -->
    <div class="top-sticky-section">
      <!-- 头部导航 -->
      <div class="course-header">
        <el-button type="text" @click="goBack" class="back-button">
          <el-icon size="24"><ArrowLeft /></el-icon>
        </el-button>
        <h1 class="page-title">课程学习</h1>
        <div class="header-placeholder"></div>
      </div>
    </div>
    
    <!-- 视频播放区域 -->
    <div class="video-container">
      <div v-if="loading" class="loading-container">
        <el-loading type="spinner" color="#409EFF" />
        <p>加载中...</p>
      </div>
      
      <div v-else-if="error" class="error-container">
        <p>{{ error }}</p>
        <el-button type="primary" @click="fetchVideoUrl">重试</el-button>
      </div>
      
      <div v-else-if="videoUrl" class="video-player">
        <video
          ref="videoRef"
          :src="videoUrl"
          controls
          autoplay
          muted
          playsinline
          webkit-playsinline
          x5-playsinline
          x5-video-player-type="h5"
          x5-video-player-fullscreen="true"
          x5-video-orientation="landscape"
          class="video-element"
        >
          您的浏览器不支持视频播放
        </video>
      </div>
    </div>
    
    <!-- 课程信息区域 -->
    <div class="course-info-container">
      <h2 class="course-title">课程名称</h2>
      <div class="course-meta">
        <span class="meta-item">讲师：张老师</span>
        <span class="meta-item">时长：120分钟</span>
      </div>
      <div class="course-description">
        <h3 class="section-title">课程简介</h3>
        <p class="description-content">本课程适合零基础学习者，从音标开始，逐步掌握英语基础语法和常用词汇，为后续学习打下坚实基础。</p>
      </div>
    </div>
    
    <!-- 其他课程列表 -->
    <div class="other-courses-container">
      <h3 class="section-title">其他课程</h3>
      <div class="other-courses-list">
        <div
          v-for="otherCourse in otherCourses"
          :key="otherCourse.id"
          class="other-course-item"
          @click="switchToCourse(otherCourse.id)"
          style="cursor: pointer;"
        >
          <div class="other-course-image">
            <el-image
              :src="otherCourse.coverImage"
              :alt="otherCourse.title"
              fit="cover"
              :fallback="'/default-course.png'"
            />
          </div>
          <div class="other-course-info">
            <h4 class="other-course-name">{{ otherCourse.title }}</h4>
            <div class="other-course-meta">
              <span class="meta-item">{{ otherCourse.teacher || '未知讲师' }}</span>
              <span class="meta-item">{{ otherCourse.duration ? otherCourse.duration + '分钟' : '未知时长' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 主容器 */
.course-player-container {
  width: 100%;
  min-height: 100vh;
  box-sizing: border-box;
  background-color: var(--bg-primary);
  transition: all 0.3s ease;
  /* 适配刘海屏 */
  padding-left: env(safe-area-inset-left);
  padding-right: env(safe-area-inset-right);
}

/* 顶部固定区域 */
.top-sticky-section {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: var(--bg-primary);
  z-index: 100;
  padding: env(safe-area-inset-top) 1rem 1rem;
  /* 移除阴影，保持视觉一致性 */
  box-shadow: none;
  transition: all 0.3s ease;
}

/* 头部导航 */
.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  box-sizing: border-box;
  padding: 0.5rem 0;
}

.page-title {
  font-size: 1.2rem;
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

.header-placeholder {
  width: 40px;
}

/* 视频播放区域 */
.video-container {
  padding-top: 70px;
  width: 100%;
  background-color: black;
  position: relative;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 6rem 0;
  gap: 1rem;
  width: 100%;
  color: white;
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
  gap: 1rem;
  width: 100%;
  color: white;
}

.video-player {
  width: 100%;
  position: relative;
  padding-top: 56.25%; /* 16:9 比例 */
}

.video-element {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 全屏样式 */
.video-element:-webkit-full-screen {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background-color: black;
}

.video-element:fullscreen {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background-color: black;
}

.video-element:-webkit-full-screen video {
  object-fit: contain;
}

.video-element:fullscreen video {
  object-fit: contain;
}

/* 移动设备全屏优化 */
@media (max-width: 768px) {
  .video-element {
    object-fit: contain;
  }
  
  /* 确保视频容器在移动设备上正确显示 */
  .video-player {
    padding-top: 56.25%; /* 16:9 比例 */
  }
  
  /* 全屏模式下的移动设备样式 */
  .video-element:-webkit-full-screen {
    background-color: black;
  }
  
  .video-element:fullscreen {
    background-color: black;
  }
  
  /* 适配横屏模式 */
  @media (orientation: landscape) {
    .video-element:-webkit-full-screen {
      width: 100vw;
      height: 100vh;
    }
    
    .video-element:fullscreen {
      width: 100vw;
      height: 100vh;
    }
  }
}

/* 课程信息区域 */
.course-info-container {
  padding: 1.5rem;
  background-color: var(--bg-secondary);
  margin-bottom: 1rem;
  transition: all 0.3s ease;
}

.course-title {
  font-size: 1.4rem;
  font-weight: bold;
  color: var(--text-primary);
  margin: 0 0 1rem 0;
  transition: all 0.3s ease;
}

.course-meta {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.meta-item {
  font-size: 0.9rem;
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.course-description {
  margin-top: 1rem;
}

.section-title {
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--text-primary);
  margin: 0 0 1rem 0;
  transition: all 0.3s ease;
}

.description-content {
  font-size: 0.95rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
  transition: all 0.3s ease;
}

/* 其他课程列表 */
.other-courses-container {
  padding: 1.5rem;
  background-color: var(--bg-secondary);
  margin-bottom: 2rem;
  transition: all 0.3s ease;
}

.other-courses-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.other-course-item {
  display: flex;
  align-items: center;
  padding: 1rem;
  border-radius: 12px;
  background-color: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  cursor: pointer;
}

.other-course-item:active {
  transform: scale(0.98);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.other-course-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  margin-right: 1rem;
  flex-shrink: 0;
}

.other-course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.other-course-info {
  flex: 1;
  min-width: 0;
}

.other-course-name {
  font-size: 1rem;
  font-weight: 500;
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: all 0.3s ease;
}

.other-course-meta {
  display: flex;
  gap: 1rem;
}

.other-course-meta .meta-item {
  font-size: 0.8rem;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .course-info-container {
    padding: 1rem;
  }
  
  .course-title {
    font-size: 1.2rem;
  }
  
  .course-meta {
    gap: 1rem;
  }
  
  .other-courses-container {
    padding: 1rem;
  }
  
  .other-course-item {
    padding: 0.8rem;
  }
  
  .other-course-image {
    width: 70px;
    height: 70px;
  }
  
  .other-course-name {
    font-size: 0.9rem;
  }
}
</style>