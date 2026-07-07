<template>
  <div class="dark-mode-container">
    <!-- 头部导航 -->
    <div class="header">
      <el-button type="text" @click="goBack" class="back-button">
        <el-icon size="24"><ArrowLeft /></el-icon>
      </el-button>
      <h1 class="page-title">黑夜模式</h1>
      <div class="header-placeholder"></div>
    </div>
    
    <!-- 主要内容 -->
    <div class="content">
      <div class="mode-switch">
        <span class="mode-label">黑夜模式</span>
        <el-switch 
          v-model="isDarkMode" 
          size="large" 
          active-text="开启" 
          inactive-text="关闭" 
          @change="handleSwitchChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useTheme } from '@/composables/useTheme'

// 获取路由实例
const router = useRouter()

// 使用主题钩子
const { isDarkMode, initTheme, toggleTheme } = useTheme()

// 返回上一页
const goBack = () => {
  router.back()
}

// 切换黑夜模式
const handleSwitchChange = async (newValue) => {
  try {
    await toggleTheme(newValue)
  } catch (error) {
    console.error('更新黑夜模式失败:', error)
    ElMessage.error('更新失败，请稍后重试')
  }
}

// 组件挂载时初始化主题
onMounted(() => {
  initTheme()
})
</script>

<style scoped>
/* 整体容器 */
.dark-mode-container {
  width: 100%;
  min-height: 100vh;
  box-sizing: border-box;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  /* 适配刘海屏 */
  padding-left: env(safe-area-inset-left);
  padding-right: env(safe-area-inset-right);
  transition: all 0.3s ease;
}

/* 头部导航 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  box-sizing: border-box;
  padding: 1rem 0;
  background-color: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 10;
  transition: all 0.3s ease;
}

.page-title {
  font-size: 1.1rem;
  font-weight: bold;
  color: var(--text-primary);
  margin: 0;
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
}

.header-placeholder {
  width: 40px;
}

/* 主要内容 */
.content {
  padding: 2rem 1.5rem;
  max-width: 600px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

/* 模式切换 */
.mode-switch {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 0;
  border-bottom: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.mode-switch:last-child {
  border-bottom: none;
}

.mode-label {
  font-size: 1rem;
  color: var(--text-primary);
  font-weight: 500;
}

/* 响应式设计 */
@media screen and (max-width: 480px) {
  .content {
    padding: 1.5rem 1rem;
  }
  
  .mode-switch {
    padding: 1.2rem 0;
  }
  
  .mode-label {
    font-size: 0.95rem;
  }
}
</style>