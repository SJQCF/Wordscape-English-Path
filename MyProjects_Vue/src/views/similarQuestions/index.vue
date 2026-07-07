<template>
  <div class="similar-questions-container">
    <!-- 顶部导航 -->
    <div class="header">
      <el-button type="text" @click="goBack" class="back-button">
        <el-icon size="24"><ArrowLeft /></el-icon>
      </el-button>
      <h1 class="page-title">类似练习题</h1>
      <div class="header-placeholder"></div>
    </div>
    
    <!-- 题目内容 -->
    <div class="questions-content">
      <!-- 题目导航 -->
      <div class="question-navigation">
        <span class="question-counter">
          {{ currentQuestionIndex + 1 }} / {{ similarQuestionsList.length }}
        </span>
        <div class="nav-buttons">
          <el-button 
            type="info" 
            round 
            @click="prevQuestion"
            :disabled="currentQuestionIndex === 0"
            size="small"
          >
            上一题
          </el-button>
          <el-button 
            type="info" 
            round 
            @click="nextQuestion"
            :disabled="currentQuestionIndex === similarQuestionsList.length - 1"
            size="small"
          >
            下一题
          </el-button>
        </div>
      </div>
      
      <!-- 当前题目 -->
      <div v-if="similarQuestionsList[currentQuestionIndex]" class="similar-questions-content">
        <!-- 题目内容 -->
        <div class="similar-question-text">
          <div class="question-title">{{ similarQuestionsList[currentQuestionIndex].question }}</div>
          <div class="question-options" v-if="similarQuestionsList[currentQuestionIndex].options.length > 0">
            <div 
              v-for="(option, index) in similarQuestionsList[currentQuestionIndex].options" 
              :key="index"
              class="option-item"
            >
              {{ option }}
            </div>
          </div>
        </div>
        
        <!-- 用户答案输入 -->
        <div v-if="!showCorrectAnswer" class="user-answer-section">
          <el-input
            v-model="userAnswer"
            type="textarea"
            :rows="3"
            placeholder="请输入你的答案 (例如: A)"
            class="user-answer-input"
          />
          <el-button 
            type="primary" 
            round 
            @click="checkAnswer"
            class="check-answer-btn"
          >
            提交答案
          </el-button>
        </div>
        
        <!-- 正确答案 -->
        <div v-else class="correct-answer-section">
          <div class="answer-section">
            <div class="section-title">正确答案</div>
            <div class="answer-content">
              {{ similarQuestionsList[currentQuestionIndex].answer }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import { ElMessage } from 'element-plus'

// 初始化主题
const { initTheme } = useTheme()

// 获取路由实例
const router = useRouter()
const route = useRoute()

// 类似练习题相关
const similarQuestionsList = ref([])
const currentQuestionIndex = ref(0)
const userAnswer = ref('')
const showCorrectAnswer = ref(false)

// 返回上一页
const goBack = () => {
  router.back()
}

// 检查答案
const checkAnswer = () => {
  if (userAnswer.value.trim()) {
    showCorrectAnswer.value = true
  } else {
    ElMessage({
      message: '请输入答案',
      type: 'warning',
      duration: 2000
    })
  }
}

// 下一题
const nextQuestion = () => {
  if (currentQuestionIndex.value < similarQuestionsList.value.length - 1) {
    currentQuestionIndex.value++
    userAnswer.value = ''
    showCorrectAnswer.value = false
  }
}

// 上一题
const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
    userAnswer.value = ''
    showCorrectAnswer.value = false
  }
}

// 组件挂载时获取数据
onMounted(() => {
  // 初始化主题
  initTheme()
  
  // 从路由参数中获取类似练习题数据
  if (route.query.questions) {
    try {
      similarQuestionsList.value = JSON.parse(route.query.questions)
      console.log('类似练习题数据:', similarQuestionsList.value)
    } catch (error) {
      console.error('解析类似练习题数据失败:', error)
      ElMessage({
        message: '加载类似练习题失败',
        type: 'error',
        duration: 2000
      })
      router.back()
    }
  } else {
    ElMessage({
      message: '没有类似练习题数据',
      type: 'warning',
      duration: 2000
    })
    router.back()
  }
})
</script>

<style scoped>
/* 主容器 */
.similar-questions-container {
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
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}

/* 顶部导航 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: calc(env(safe-area-inset-top) + 0.5rem) 1rem 1rem;
  background-color: var(--bg-primary);
  box-shadow: none;
  transition: all 0.3s ease;
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

/* 题目内容 */
.questions-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 1rem;
  overflow-y: auto;
  min-height: 0; /* 确保flex子元素可以正确收缩 */
}

.question-navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.8rem 1rem;
  background-color: var(--bg-primary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  margin-bottom: 1rem;
}

.question-counter {
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.nav-buttons {
  display: flex;
  gap: 0.5rem;
}

.similar-questions-content {
  font-size: 1rem;
  line-height: 1.6;
  color: var(--text-primary);
  white-space: pre-wrap;
  word-break: break-word;
  padding: 1rem;
  background-color: var(--bg-primary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.similar-question-text {
  margin-bottom: 1rem;
}

.question-title {
  font-weight: 500;
  margin-bottom: 1rem;
}

.question-options {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.option-item {
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--border-color);
}

.option-item:last-child {
  border-bottom: none;
}

.user-answer-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 1rem;
}

.user-answer-input {
  width: 100%;
}

.check-answer-btn {
  align-self: flex-start;
}

.correct-answer-section {
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 答案部分 */
.answer-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.answer-content {
  font-size: 1rem;
  line-height: 1.6;
  color: var(--text-primary);
  white-space: pre-wrap;
  word-break: break-word;
  padding: 1rem;
  background-color: var(--bg-primary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .questions-content {
    padding: 0.8rem;
  }
  
  .similar-questions-content {
    padding: 1.2rem;
  }
}
</style>