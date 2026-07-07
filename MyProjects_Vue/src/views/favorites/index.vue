<template>
  <div class="favorites-container">
    <!-- 顶部固定区域 -->
    <div class="top-sticky-section">
      <!-- 头部导航 -->
      <div class="course-header">
        <el-button type="text" @click="goBack" class="back-button">
          <el-icon size="24"><ArrowLeft /></el-icon>
        </el-button>
        <h1 class="page-title">我的收藏</h1>
        <div class="header-placeholder"></div>
      </div>
    </div>
    
    <!-- 内部滚动容器 -->
    <div class="favorites-scroll-container">
      <!-- 收藏列表 -->
      <div class="favorites-list">
        <div
          v-for="collection in collections"
          :key="collection.id"
          class="favorites-card"
          @click="handleCollectionClick(collection)"
        >
          <div class="favorites-image">
            <el-image
              :src="collection.icon"
              :alt="collection.name"
              fit="cover"
              :fallback="'/default-collection.png'"
            />

          </div>
          <div class="favorites-info">
            <div class="favorites-name">
              {{ collection.name }}
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="collections.length === 0" class="empty-state">
        <el-icon size="48">
          <StarFilled />
        </el-icon>
        <p>暂无收藏</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ArrowLeft, StarFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'

const { initTheme } = useTheme()

onMounted(() => {
  initTheme()
})

// 获取路由实例
const router = useRouter()

// 返回上一页
const goBack = () => {
  router.back()
}

// 处理收藏卡片点击事件
const handleCollectionClick = (collection) => {
  if (collection.name === '课程') {
    // 跳转到课程页面，并传递参数showFavorites=true
    router.push({ path: '/course', query: { showFavorites: true } })
  } else if (collection.name === '单词本') {
    // 跳转到收藏单词页面
    router.push('/favoriteWords')
  } else if (collection.name === '听力') {
    // 跳转到听力页面，并传递参数showFavorites=true
    router.push({ path: '/listening', query: { showFavorites: true } })
  }
}

// 收藏分类数据 - 删除小讲堂文章
const collections = ref([
  { id: 1, name: '单词本', icon: 'https://sjqcf-web-project01.oss-cn-beijing.aliyuncs.com/52kqiPIAcVDZ04lzGUxku.jpeg' },
  { id: 2, name: '课程', icon: 'https://sjqcf-web-project01.oss-cn-beijing.aliyuncs.com/R-C%20%281%29.jpg' },
  { id: 3, name: '听力', icon: 'https://sjqcf-web-project01.oss-cn-beijing.aliyuncs.com/t0153e8edf9ebb69b13.jpg' }
])
</script>

<style scoped>
/* 主容器 */
.favorites-container {
  width: 100%;
  height: 100vh;
  box-sizing: border-box;
  background-color: var(--bg-primary);
  /* 适配刘海屏 */
  padding-left: env(safe-area-inset-left);
  padding-right: env(safe-area-inset-right);
  /* 确保主容器完全不滚动 */
  overflow: hidden;
  /* 固定定位，确保不会出现外部滚动 */
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  transition: all 0.3s ease;
}

/* 顶部固定区域 - 使用fixed定位确保完全固定 */
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



/* 内部滚动容器 - 核心：确保这是唯一可以滚动的元素 */
.favorites-scroll-container {
  /* 核心：设置明确的高度和位置，确保可以滚动 */
  position: fixed;
  top: 70px; /* 进一步减小与头部的距离 */
  left: 0;
  right: 0;
  bottom: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  /* 增加底部内边距，确保可以看到所有收藏 */
  padding-bottom: 5rem;
  /* 确保触摸事件能正常触发 */
  touch-action: pan-y;
  pointer-events: auto;
  /* 添加左右内边距 */
  padding-left: 1rem;
  padding-right: 1rem;
  /* 添加顶部内边距 */
  padding-top: 0.3rem;
  /* 优化滚动条样式 */
  scrollbar-width: thin;
  scrollbar-color: #dcdfe6 #f5f7fa;
}

/* 移除滚动条样式，因为不再需要滚动 */
/* 
.favorites-scroll-container {
  scrollbar-width: thin;
  scrollbar-color: #dcdfe6 transparent;
}

.favorites-scroll-container::-webkit-scrollbar {
  width: 6px;
}

.favorites-scroll-container::-webkit-scrollbar-track {
  background: transparent;
}

.favorites-scroll-container::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}
*/

/* 收藏列表样式 */
.favorites-list {
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

/* 收藏卡片样式 */
.favorites-card {
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  background-color: var(--bg-secondary);
  border: 1px solid var(--border-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  position: relative;
  /* 只设置固定高度，不设置min-height，避免被拉伸 */
  height: 180px;
  /* 确保没有星星图标显示 */
}

/* 隐藏可能存在的星星图标 */
.favorites-card .el-icon[class*="star"],
.favorites-card [class*="star"] {
  display: none !important;
}

.favorites-card:active {
  transform: scale(0.98);
}

/* 收藏图片容器 */
.favorites-image {
  width: 100%;
  aspect-ratio: 1 / 1;
  position: relative;
  background-color: var(--bg-primary);
  transition: all 0.3s ease;
}

.favorites-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.favorites-card:active .favorites-image img {
  transform: scale(1.05);
}



/* 收藏信息区域 */
.favorites-info {
  padding: 0.8rem;
  display: flex;
  flex-direction: column;
  /* 确保信息区域不会被拉伸 */
  justify-content: center;
  height: 70px;
}

/* 收藏名称样式 */
.favorites-name {
  font-size: 0.9rem;
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
  transition: all 0.3s ease;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 0;
  color: #909399;
  width: 100%;
}

.empty-state .el-icon {
  margin-bottom: 1rem;
  color: var(--warning-color);
  transition: all 0.3s ease;
}

.empty-state p {
  margin-bottom: 1.5rem;
  color: var(--text-tertiary);
  transition: all 0.3s ease;
}

/* 响应式设计 */

/* 平板设备 */
@media (min-width: 768px) and (max-width: 1024px) {
  .favorites-list {
    grid-template-columns: repeat(2, 1fr);
    gap: 1.2rem;
    padding: 1.5rem;
  }
  
  .favorites-name {
    font-size: 1rem;
  }
}

/* 大屏手机 */
@media (max-width: 767px) {
  .favorites-list {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.8rem;
    padding: 1rem;
  }
  
  .favorites-card {
    border-radius: 10px;
    min-height: 160px;
  }
  
  .favorites-info {
    padding: 0.6rem;
  }
  
  .favorites-name {
    font-size: 0.85rem;
    min-height: 2.4rem;
  }
}

/* 小屏手机 */
@media (max-width: 480px) {
  .favorites-list {
    grid-template-columns: repeat(2, 1fr); /* 保持两列布局 */
    gap: 0.6rem;
    padding: 0.8rem;
  }
  
  
  
  .favorites-card {
    border-radius: 10px;
    min-height: 140px;
  }
  
  .favorites-info {
    padding: 0.5rem;
  }
  
  .favorites-name {
    font-size: 0.8rem;
    min-height: 2.2rem;
  }
  

}

/* 超小屏手机 */
@media (max-width: 320px) {
  .favorites-list {
    gap: 0.5rem;
    padding: 0.5rem;
  }
  
  .favorites-card {
    min-height: 120px;
  }
  
  .favorites-info {
    padding: 0.4rem;
  }
  
  .favorites-name {
    font-size: 0.75rem;
  }
}

/* 确保所有子元素都不会产生自己的滚动条 */
.favorites-container *:not(.favorites-scroll-container) {
  overflow: hidden !important;
}

/* 确保列表项不会产生自己的滚动条 */
.favorites-card,
.favorites-image,
.favorites-info {
  overflow: hidden;
}
</style>