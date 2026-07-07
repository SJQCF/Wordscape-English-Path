<script setup>
import { ref, onMounted } from 'vue'
import { getCurrentUserId } from '@/utils/auth.js'
import { Reading, ArrowLeft, Loading } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { updateUserType, getUserType } from '@/api/user'
import { clearStudyDataCache } from '@/composables/useStudyData'
import { useTheme } from '@/composables/useTheme'

// 获取路由实例
const router = useRouter()

// 返回上一页
const goBack = () => {
  router.back()
}

// 当前选中的学习内容选项
const selectedOption = ref('') // 初始化为空，不显示默认选中
const loading = ref(true) // 初始化为true，显示加载状态

// 学习类型ID到名称的映射
const studyTypeIdMap = {
  1: '高中英语',
  2: '四级英语',
  3: '六级英语',
  4: '商务英语',
  5: '出国英语'
}

// 初始化主题
const { initTheme } = useTheme()

// 获取当前用户的学习类型
onMounted(async () => {
  loading.value = true
  // 初始化主题
  initTheme()
  // 实际开发中，userId应从登录信息获取，现在暂时使用固定值1
  const userId = getCurrentUserId()
  
  try {
    // 获取用户当前学习类型
    const result = await getUserType(userId)
    if (result.code && result.data) {
      // 根据用户类型ID获取对应的学习类型名称
      const userType = result.data
      selectedOption.value = studyTypeIdMap[userType] || '四级英语'
    }
  } catch (error) {
    console.error('获取用户学习类型失败:', error)
  } finally {
    loading.value = false
  }
})

// 选择学习内容
const selectOption = async (option) => {
  selectedOption.value = option
  
  // 实际开发中，userId应从登录信息获取，现在暂时使用固定值1
  const userId = getCurrentUserId()
  // 根据选择的学习内容映射到对应的ID
  const studyTypeMap = {
    '高中英语': 1,
    '四级英语': 2,
    '六级英语': 3,
    '商务英语': 4,
    '出国英语': 5
  }
  // 获取对应的学习类型ID
  const userTypeId = studyTypeMap[option]
  
  try {
      // 调用API更新用户学习类型
      const result = await updateUserType(userId, userTypeId)
      if (result.code) {
        // 更新成功，可以添加提示
        console.log('学习内容更新成功')
        // 清除学习数据缓存，确保下次获取最新数据
        clearStudyDataCache()
      }
    } catch (error) {
      // 处理错误
      console.error('学习内容更新失败:', error)
    }
}
</script>

<template>
  <div class="switch-container">
  <!-- 加载遮罩 -->
  <div v-if="loading" class="loading-overlay">
    <div class="loading-content">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <span class="loading-text">加载中...</span>
    </div>
  </div>
  <div v-if="loading" class="loading-overlay">
    <el-loading fullscreen text="加载中..." />
  </div>
    <!-- 返回按钮 -->
    <div class="back-button-container">
      <el-button type="text" @click="goBack" class="back-button">
        <el-icon size="20" class="back-icon"><ArrowLeft /></el-icon>
        <span>返回</span>
      </el-button>
    </div>
    
    <h2 class="switch-title">选择学习内容</h2>
    <div class="switch-options">
      <div class="option-item">
        <el-button 
          plain
          class="option-button"
          :class="{ 'selected': selectedOption === '高中英语' }"
          @click="selectOption('高中英语')"
          :disabled="loading"
        >高中英语</el-button>
      </div>
      <div class="option-item">
        <el-button 
          plain
          class="option-button"
          :class="{ 'selected': selectedOption === '四级英语' }"
          @click="selectOption('四级英语')"
          :disabled="loading"
        >四级英语</el-button>
      </div>
      <div class="option-item">
        <el-button 
          plain
          class="option-button"
          :class="{ 'selected': selectedOption === '六级英语' }"
          @click="selectOption('六级英语')"
          :disabled="loading"
        >六级英语</el-button>
      </div>
      <div class="option-item">
        <el-button 
          plain
          class="option-button"
          :class="{ 'selected': selectedOption === '商务英语' }"
          @click="selectOption('商务英语')"
          :disabled="loading"
        >商务英语</el-button>
      </div>
      <div class="option-item">
        <el-button 
          plain
          class="option-button"
          :class="{ 'selected': selectedOption === '出国英语' }"
          @click="selectOption('出国英语')"
          :disabled="loading"
        >出国英语</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.switch-container {
  width: 100%;
  height: 100vh;
  padding: 1.5rem;
  box-sizing: border-box;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
  /* 适配刘海屏 */
  padding-top: env(safe-area-inset-top);
  padding-left: env(safe-area-inset-left);
  padding-right: env(safe-area-inset-right);
  transition: all 0.3s ease;
}

.switch-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
  text-align: center;
  transition: all 0.3s ease;
}

/* 返回按钮样式 */
.back-button-container {
  position: absolute;
  top: 1rem;
  left: 1rem;
  z-index: 10;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 1rem;
  color: var(--primary-color);
  padding: 0.5rem;
  transition: all 0.3s ease;
}

.back-icon {
  margin-right: 0.2rem;
}

.switch-options {
  width: 100%;
  max-width: 400px;
  display: flex;
  flex-direction: column;
  gap: 2.5rem;
  flex: 1;
  justify-content: center;
}

.option-item {
  width: 100%;
}

.option-button {
  width: 100%;
  height: auto;
  padding: 1.2rem;
  font-size: 1.1rem;
  font-weight: 500;
  border-radius: 12px !important;
  transition: all 0.3s ease;
  border: none !important;
  background: none !important;
  outline: none !important;
  box-shadow: none !important;
}

/* 移除Element Plus按钮的所有默认样式 */
.option-item :deep(.el-button) {
  border-radius: 12px !important;
  padding: 1.2rem !important;
  border: 2px solid transparent !important;
  background-color: #f5f7fa !important;
  color: #606266 !important;
  font-size: 1.1rem !important;
  font-weight: 500 !important;
  transition: all 0.3s ease !important;
  box-shadow: none !important;
  min-width: unset !important;
}

/* 未选中按钮样式 */
.option-item :deep(.el-button:not(.selected)) {
  background-color: var(--bg-secondary) !important;
  border-color: var(--border-color) !important;
  color: var(--text-secondary) !important;
  border-radius: 12px !important;
  transition: all 0.3s ease !important;
}

/* 选中按钮样式 */
.option-item :deep(.el-button.selected) {
  background-color: var(--primary-color) !important;
  border-color: var(--primary-color) !important;
  color: white !important;
  /* 移除放大效果 */
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3) !important;
  border-radius: 12px !important;
  border: 2px solid var(--primary-color) !important;
  transition: all 0.3s ease !important;
}

/* 按钮点击效果 */
.option-item :deep(.el-button:active) {
  transform: scale(0.98) !important;
}

/* 移除按钮悬停效果 */
.option-button:hover {
  transform: none;
}

/* 未选中按钮的悬停效果 */
.option-button:not(.selected):hover {
  background-color: var(--bg-secondary);
  border-color: var(--border-color);
  color: var(--text-secondary);
}
/* 响应式设计 - 平板设备 */
@media (max-width: 1024px) {
  .switch-title {
    font-size: 1.4rem;
    margin-bottom: 1.2rem;
  }
  
  .switch-options {
    gap: 2rem;
    max-width: 350px;
  }
  
  .option-button {
    font-size: 1rem;
    padding: 1rem;
  }
}

/* 响应式设计 - 小型平板/大屏手机 */
@media (max-width: 768px) {
  .switch-title {
    font-size: 1.3rem;
    margin-bottom: 1rem;
  }
  
  .switch-options {
    gap: 1.8rem;
    max-width: 320px;
  }
  
  .option-button {
    font-size: 1rem;
    padding: 1rem;
  }
  
  .back-button {
    font-size: 0.95rem;
    padding: 0.4rem;
  }
}

/* 响应式设计 - 标准手机 */
@media (max-width: 480px) {
  .switch-container {
    padding: 1rem;
  }
  
  .switch-title {
    font-size: 1.2rem;
    margin-bottom: 0.8rem;
  }
  
  .switch-options {
    gap: 1.5rem;
    max-width: 100%;
  }
  
  .option-button {
    font-size: 0.95rem;
    padding: 0.9rem;
  }
  
  .back-button {
    font-size: 0.9rem;
    padding: 0.3rem;
  }
  
  .back-button-container {
    top: 0.8rem;
    left: 0.8rem;
  }
}

/* 响应式设计 - 小屏手机 */
@media (max-width: 360px) {
  .switch-container {
    padding: 0.8rem;
  }
  
  .switch-title {
    font-size: 1.1rem;
    margin-bottom: 0.6rem;
  }
  
  .switch-options {
    gap: 1.2rem;
  }
  
  .option-button {
    font-size: 0.9rem;
    padding: 0.8rem;
  }
  
  .back-button {
    font-size: 0.85rem;
    padding: 0.2rem;
  }
  
  .back-button-container {
    top: 0.6rem;
    left: 0.6rem;
  }
}

/* 响应式设计 - 横屏模式 */
@media (max-height: 500px) {
  .switch-options {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: center;
    gap: 1rem;
  }
  
  .option-item {
    width: calc(50% - 0.5rem);
  }
  
  .switch-title {
    margin-bottom: 0.5rem;
  }
}
</style>