<!--定义js，控制组件的行为-->
<script setup>
import { ref, onMounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'

const { initTheme } = useTheme()

// 获取路由实例
const router = useRouter()

// 返回上一页
const goBack = () => {
  router.back()
}

// 每日学习计划设置
const studyPlan = ref({
  dailyNewWords: 10, // 每日新学单词数
  dailyReviewWords: 10 // 每日复习单词数
})

// 保存状态
const saving = ref(false)
// 保存成功提示
const saveSuccess = ref(false)

// 保存学习计划设置
const saveStudyPlan = async () => {
  saving.value = true
  try {
    // 验证输入
    if (studyPlan.value.dailyNewWords < 1 || studyPlan.value.dailyReviewWords < 1) {
      alert('学习和复习数量不能小于1')
      return
    }
    
    // 保存到本地存储
    localStorage.setItem('studyPlan', JSON.stringify(studyPlan.value))
    
    // 显示保存成功提示
    saveSuccess.value = true
    setTimeout(() => {
      saveSuccess.value = false
    }, 2000)
  } catch (error) {
    console.error('保存学习计划失败:', error)
    alert('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

// 组件挂载时加载保存的设置
onMounted(() => {
  initTheme()
  // 从本地存储加载学习计划设置
  const savedPlan = localStorage.getItem('studyPlan')
  if (savedPlan) {
    try {
      studyPlan.value = JSON.parse(savedPlan)
    } catch (error) {
      console.error('解析学习计划失败:', error)
    }
  }
})
</script>

<!--定义html，控制组件的结构-->
<template>
  <div class="study-plan-container">
    <!-- 头部导航 -->
    <div class="header">
      <el-button type="text" @click="goBack" class="back-button">
        <el-icon size="24" class="back-icon"><ArrowLeft /></el-icon>
      </el-button>
      <h1 class="page-title">学习计划</h1>
      <div class="header-placeholder"></div>
    </div>
    
    <!-- 内容卡片 -->
    <div class="content-card">
      <!-- 每日新学单词设置 -->
      <div class="setting-item">
        <div class="setting-label">每日新学单词</div>
        <div class="setting-control">
          <el-input-number 
            v-model="studyPlan.dailyNewWords" 
            :min="1" 
            :max="100" 
            :step="1"
            size="large"
            class="number-input"
          />
          <span class="setting-unit">词</span>
        </div>
      </div>
      
      <!-- 每日复习单词设置 -->
      <div class="setting-item">
        <div class="setting-label">每日复习单词</div>
        <div class="setting-control">
          <el-input-number 
            v-model="studyPlan.dailyReviewWords" 
            :min="1" 
            :max="200" 
            :step="1"
            size="large"
            class="number-input"
          />
          <span class="setting-unit">词</span>
        </div>
      </div>
      
      <!-- 保存按钮 -->
      <div class="save-button-container">
        <el-button 
          type="primary" 
          size="large" 
          @click="saveStudyPlan" 
          :loading="saving"
          class="save-button"
        >
          保存设置
        </el-button>
      </div>
      
      <!-- 保存成功提示 -->
      <div v-if="saveSuccess" class="success-message">
        保存成功！
      </div>
    </div>
    
    <!-- 说明文字 -->
    <div class="description">
      <p>设置每日学习目标，帮助你更有计划地学习英语。</p>
      <p>建议每日新学单词数不超过20个，以确保学习效果。</p>
    </div>
  </div>
</template>

<!--定义css，控制组件的样式-->
<style scoped>
/* 整体容器 */
.study-plan-container {
  width: 100%;
  min-height: 100vh;
  padding: 1rem;
  box-sizing: border-box;
  background-color: var(--bg-primary);
  display: flex;
  flex-direction: column;
  align-items: center;
  /* 适配刘海屏 */
  padding-top: env(safe-area-inset-top);
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
  max-width: 400px;
  margin-bottom: 2rem;
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
  cursor: pointer;
  transition: color 0.3s ease, background-color 0.3s ease;
}

.back-button:hover {
  background-color: transparent;
}

.back-icon {
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.header-placeholder {
  width: 40px;
}

/* 内容卡片 */
.content-card {
  width: 100%;
  max-width: 400px;
  background-color: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 2rem;
  margin-bottom: 1.5rem;
  transition: all 0.3s ease;
}

/* 设置项 */
.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.setting-label {
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--text-primary);
  flex: 1;
  transition: all 0.3s ease;
}

.setting-control {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.number-input {
    width: 140px;
  }
  
  /* 增大数字输入框的+和-按钮之间的间隔 */
  :deep(.el-input-number__decrease),
  :deep(.el-input-number__increase) {
    width: 30px;
  }
  
  :deep(.el-input-number__input) {
    width: 80px;
    text-align: center;
    background-color: var(--bg-primary) !important;
    color: var(--text-primary) !important;
    border-color: var(--border-color) !important;
  }

.setting-unit {
  font-size: 1rem;
  color: var(--text-secondary);
  white-space: nowrap;
  transition: all 0.3s ease;
}

/* 保存按钮 */
.save-button-container {
  margin-top: 2.5rem;
  margin-bottom: 1rem;
}

.save-button {
  width: 100%;
  height: 50px;
  font-size: 1rem;
  font-weight: 500;
  border-radius: 12px;
}

/* 保存成功提示 */
.success-message {
  text-align: center;
  color: var(--success-color);
  font-size: 0.9rem;
  margin-top: 1rem;
  padding: 0.5rem;
  background-color: rgba(139, 195, 74, 0.1);
  border-radius: 8px;
  animation: fadeInOut 2s ease-in-out;
  transition: all 0.3s ease;
}

@keyframes fadeInOut {
  0% { opacity: 0; transform: translateY(-10px); }
  20% { opacity: 1; transform: translateY(0); }
  80% { opacity: 1; transform: translateY(0); }
  100% { opacity: 0; transform: translateY(-10px); }
}

/* 说明文字 */
.description {
  width: 100%;
  max-width: 400px;
  padding: 1rem;
  background-color: rgba(255, 152, 0, 0.1);
  border-radius: 12px;
  margin-bottom: 2rem;
  transition: all 0.3s ease;
}

.description p {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin: 0.5rem 0;
  line-height: 1.5;
  transition: all 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .study-plan-container {
    padding: 0.8rem;
  }
  
  .header {
    margin-bottom: 1.5rem;
  }
  
  .content-card {
    padding: 1.5rem;
    border-radius: 12px;
  }
  
  .setting-item {
    margin-bottom: 1.5rem;
  }
  
  .setting-label {
    font-size: 1rem;
  }
  
  .number-input {
    width: 100px;
  }
  
  .save-button {
    height: 45px;
    font-size: 0.95rem;
  }
  
  .description {
    padding: 0.8rem;
    font-size: 0.85rem;
  }
  
  .page-title {
    font-size: 1rem;
  }
  
  .back-button,
  .header-placeholder {
    width: 36px;
    height: 36px;
  }
}

@media (max-width: 360px) {
  .content-card {
    padding: 1.2rem;
  }
  
  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.8rem;
  }
  
  .setting-control {
    width: 100%;
    justify-content: space-between;
  }
  
  .number-input {
    width: 80px;
  }
}
</style>