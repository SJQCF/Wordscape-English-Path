<!--定义js，控制组件的行为-->
<script setup>
import { ref, onMounted, computed } from 'vue'
import { getCurrentUserId } from '@/utils/auth.js'
import { ArrowLeft, Headset, Star, StarFilled } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { favoriteWord, cancelFavoriteWord, checkFavoriteWord } from '@/api/word.js'
import { useTheme } from '@/composables/useTheme'

const { initTheme } = useTheme()

// 获取路由实例和当前路由
const router = useRouter()
const route = useRoute()

// 返回上一页
const goBack = () => {
  // 检查当前是否在复习结果页面
  if (showReviewResults.value) {
    // 如果是在复习结果页面，返回首页并刷新数据
    router.push({ path: '/study', query: { refresh: 'true' } })
  } else {
    // 否则正常返回上一页
    router.back()
  }
}

// 用户输入的单词数据
const userWord = ref({
  id: Date.now(), // 临时ID
  word: '',
  phonetic: '',
  definition: ''
})
// 单词列表
const words = ref([])
// 当前单词索引
const currentWordIndex = ref(0)
// 是否为复习模式
const isReviewMode = ref(false)
// 是否为学习模式
const isStudyMode = ref(false)
// 加载状态
const loading = ref(false)
// 错误信息
const error = ref('')
// 收藏状态
const isFavorite = ref(false)
// 收藏加载状态
const favoriting = ref(false)

// 复习结果
const reviewResults = ref([])
// 是否显示复习结果
const showReviewResults = ref(false)
// 当前选中的结果索引
const selectedResultIndex = ref(null)
// 返回首页
const goToHome = () => {
  router.push({ path: '/study', query: { refresh: 'true' } })
}
// 重新复习
const reviewAgain = () => {
  // 重置复习结果
  reviewResults.value = []
  showReviewResults.value = false
  selectedResultIndex.value = null
  // 重新开始复习
  showWord(0)
}

// 收藏/取消收藏单词
const handleFavorite = async () => {
  if (!userWord.value.word) return
  
  favoriting.value = true
  try {
    // 假设用户ID为1
    const userId = getCurrentUserId()
    const wordId = userWord.value.id
    let result
    
    if (isFavorite.value) {
      // 当前已收藏，调用取消收藏API
      result = await cancelFavoriteWord(userId, wordId)
    } else {
      // 当前未收藏，调用收藏API
      result = await favoriteWord(userId, wordId)
    }
    
    if (result.code) {
      // 更新本地收藏状态
      isFavorite.value = !isFavorite.value
    } else {
      console.error(isFavorite.value ? '取消收藏单词失败:' : '收藏单词失败:', result.msg)
    }
  } catch (err) {
    console.error(isFavorite.value ? '取消收藏单词失败:' : '收藏单词失败:', err)
  } finally {
    favoriting.value = false
  }
}

// 播放发音
const playPronunciation = () => {
  // 这里可以实现发音功能，例如使用Web Speech API
  if (userWord.value.word) {
    console.log('Play pronunciation for:', userWord.value.word)
  }
}

// 检查单词是否已收藏
const checkWordFavoriteStatus = async (wordId) => {
  if (!wordId) return
  
  try {
    // 假设用户ID为1
    const userId = getCurrentUserId()
    const result = await checkFavoriteWord(userId, wordId)
    
    if (result.code) {
      isFavorite.value = Boolean(result.data)
    }
  } catch (err) {
    console.error('检查单词收藏状态失败:', err)
    // 如果检查失败，默认设置为未收藏
    isFavorite.value = false
  }
}

// 重置表单
const resetForm = () => {
  userWord.value = {
    id: Date.now(),
    word: '',
    phonetic: '',
    definition: ''
  }
  isFavorite.value = false
}

// 显示指定索引的单词
const showWord = (index) => {
  if (words.value[index]) {
    currentWordIndex.value = index
    userWord.value = {
      id: words.value[index].id,
      word: '', // 学习或复习模式下清空单词，让用户输入
      phonetic: words.value[index].phonetic || words.value[index].phoneticSymbol || words.value[index].yinbiao || '',
      definition: words.value[index].definition || words.value[index].chinese || words.value[index].zhongwen || ''
    }
    // 检查单词的收藏状态
    checkWordFavoriteStatus(userWord.value.id)
  }
}

// 计算正确和错误的数量
const correctCount = computed(() => {
  return reviewResults.value.filter(result => result.correct).length
})

const errorCount = computed(() => {
  return reviewResults.value.filter(result => !result.correct).length
})

// 下一个单词
const nextWord = () => {
  if (currentWordIndex.value < words.value.length - 1) {
    showWord(currentWordIndex.value + 1);
  } else if (isReviewMode.value || isStudyMode.value) {
    // 复习模式或学习模式下，最后一个单词完成后显示结果
    showReviewResults.value = true;
  }
}

// 上一个单词
const prevWord = () => {
  if (currentWordIndex.value > 0) {
    showWord(currentWordIndex.value - 1)
  }
}

// 保存单词
const saveWord = async () => {
  if (!userWord.value.word) {
    error.value = '请输入单词'
    return
  }
  
  error.value = ''
  
  // 检查是否为复习模式或学习模式
  if ((isReviewMode.value || isStudyMode.value) && words.value[currentWordIndex.value]) {
    try {
      // 假设用户ID为1
      const userId = getCurrentUserId()
      const currentWord = words.value[currentWordIndex.value]
      const correctAnswer = currentWord.word
      const wordId = currentWord.id
      
      // 检查用户输入是否正确
      const isCorrect = userWord.value.word.toLowerCase() === correctAnswer.toLowerCase()
      
      // 记录结果
      reviewResults.value.push({
        id: wordId,
        word: correctAnswer,
        userInput: userWord.value.word,
        correct: isCorrect,
        phonetic: currentWord.phonetic || '',
        definition: currentWord.definition || ''
      })
      
      // 导入API
      const { checkUserInput, reviewCompleteWord, completeStudyWord } = await import('@/api/study')
      // 调用检查用户输入的API
      const response = await checkUserInput(userId, userWord.value.word, correctAnswer, wordId)
      
      // 根据模式调用不同的API
      if (isCorrect) {
        if (isReviewMode.value) {
          await reviewCompleteWord(userId, wordId)
          console.log('复习完成:', wordId)
        } else if (isStudyMode.value) {
          await completeStudyWord(userId, wordId)
          console.log('学习完成:', wordId)
        }
      }
    } catch (error) {
      console.error('检查用户输入失败:', error)
    }
  }
  
  // 如果是复习模式或学习模式，保存后自动跳转到下一个单词
  if (isReviewMode.value || isStudyMode.value) {
    nextWord()
  }
}

// 组件挂载时初始化
onMounted(() => {
  // 初始化主题
  initTheme()
  
  // 检查路由参数中是否有单词数据
  if (route.query.words) {
    try {
      const wordsData = JSON.parse(route.query.words)
      if (Array.isArray(wordsData) && wordsData.length > 0) {
        words.value = wordsData
        isReviewMode.value = route.query.mode === 'review'
        isStudyMode.value = route.query.mode === 'study'
        // 显示第一个单词
        showWord(0)
      }
    } catch (err) {
      console.error('解析单词数据失败:', err)
    }
  } else if (route.query.word) {
    try {
      const wordData = JSON.parse(route.query.word)
      if (wordData) {
        userWord.value = {
          id: wordData.id || Date.now(),
          word: wordData.word || '',
          phonetic: wordData.phonetic || '',
          definition: wordData.definition || ''
        }
        // 检查单词的收藏状态
        if (userWord.value.id) {
          checkWordFavoriteStatus(userWord.value.id)
        }
      }
    } catch (err) {
      console.error('解析单词数据失败:', err)
    }
  }
})
</script>

<!--定义html，控制组件的结构-->
<template>
  <div class="word-show-container">
    <!-- 顶部固定区域 -->
    <div class="top-sticky-section">
      <!-- 头部导航 -->
      <div class="course-header">
        <el-button type="text" @click="goBack" class="back-button">
          <el-icon size="24"><ArrowLeft /></el-icon>
        </el-button>
        <div class="word-counter">
          <span class="counter-text">
            {{ showReviewResults ? '复习结果' : (isStudyMode ? '单词学习' : (isReviewMode ? '单词复习' : '单词输入')) }}
            <span v-if="!showReviewResults && (isReviewMode || isStudyMode) && words.length > 0" class="word-index">({{ currentWordIndex + 1 }}/{{ words.length }})</span>
          </span>
        </div>
        <el-button v-if="!showReviewResults" type="text" @click="handleFavorite" class="favorite-button" :loading="favoriting" :disabled="!userWord.word"><el-icon size="32" :class="{ 'is-favorite': isFavorite }"><StarFilled v-if="isFavorite" /><Star v-else /></el-icon></el-button>
      </div>
    </div>
    
    <!-- 单词内容容器 -->
      <div class="word-content" v-if="!showReviewResults">
      <!-- 错误状态 -->
      <div v-if="error" class="error-container">
        <p>{{ error }}</p>
      </div>
      
      <!-- 单词内容 -->
      <!-- 单词标题（输入框） -->
      <h1 class="word-title">
        <el-input 
          v-model="userWord.word" 
          :placeholder="isReviewMode ? '请输入单词' : '请输入单词'"
          size="large"
          class="word-input"
          style="width: 100%"
        />
      </h1>
      
      <!-- 发音 -->
      <div class="word-phonetic">
        <span class="phonetic-text" v-if="userWord.phonetic">{{ userWord.phonetic }}</span>
        <span class="phonetic-text" v-else>无音标</span>
        <el-button type="text" @click="playPronunciation" class="pronunciation-button" :disabled="!userWord.word">
          <el-icon size="16" class="phonetic-icon"><Headset /></el-icon>
        </el-button>
      </div>
      
      <!-- 单词定义 -->
      <div class="word-definition">
        <h3 class="section-title">释义</h3>
        <p class="definition-text">{{ userWord.definition || '无释义' }}</p>
      </div>
       
    </div>
    
    <!-- 复习结果界面 -->
      <div class="word-content" v-else>
      <h1 class="review-result-title">复习结果</h1>
      
      <!-- 总体统计 -->
      <div class="review-stats">
        <div class="stat-item correct">
          <span class="stat-label">正确</span>
          <span class="stat-value">{{ correctCount }}</span>
        </div>
        <div class="stat-item error">
          <span class="stat-label">错误</span>
          <span class="stat-value">{{ errorCount }}</span>
        </div>
        <div class="stat-item total">
          <span class="stat-label">总计</span>
          <span class="stat-value">{{ reviewResults.length }}</span>
        </div>
      </div>
      
      <!-- 题目状态圆圈 -->
      <div class="result-circles">
        <div 
          v-for="(result, index) in reviewResults" 
          :key="result.id" 
          class="result-circle" 
          :class="{ 'correct': result.correct, 'error': !result.correct, 'selected': selectedResultIndex === index }"
          @click="selectedResultIndex = selectedResultIndex === index ? null : index"
        >
          <span class="circle-number">{{ index + 1 }}</span>
        </div>
      </div>
      
      <!-- 选中题目详情 -->
      <div class="selected-result" v-if="selectedResultIndex !== null && reviewResults[selectedResultIndex]">
        <div class="result-item" :class="{ 'correct': reviewResults[selectedResultIndex].correct, 'error': !reviewResults[selectedResultIndex].correct }">
          <div class="result-header">
            <span class="result-word">{{ reviewResults[selectedResultIndex].word }}</span>
            <span class="result-status">{{ reviewResults[selectedResultIndex].correct ? '正确' : '错误' }}</span>
          </div>
          <div class="result-info" v-if="!reviewResults[selectedResultIndex].correct">
            <span class="result-label">你的输入:</span>
            <span class="result-input">{{ reviewResults[selectedResultIndex].userInput }}</span>
          </div>
          <div class="result-phonetic" v-if="reviewResults[selectedResultIndex].phonetic">
            {{ reviewResults[selectedResultIndex].phonetic }}
          </div>
          <div class="result-definition" v-if="reviewResults[selectedResultIndex].definition">
            {{ reviewResults[selectedResultIndex].definition }}
          </div>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="review-actions">
        <el-button @click="reviewAgain" class="action-button">
          重新复习
        </el-button>
        <el-button type="primary" @click="goToHome" class="action-button">
          返回首页
        </el-button>
      </div>
    </div>
    
    <!-- 导航按钮 -->
    <div class="word-controls" v-if="!showReviewResults">
      <el-button 
        v-if="isReviewMode || isStudyMode"
        @click="prevWord"
        :disabled="currentWordIndex === 0"
        class="nav-button"
        :class="{ 'disabled-button': currentWordIndex === 0 }"
      >
        上一个
      </el-button>
      <el-button 
        v-else
        @click="resetForm"
        class="nav-button"
      >
        重置
      </el-button>
      <el-button 
        @click="saveWord"
        class="nav-button"
      >
        {{ isReviewMode || isStudyMode ? '下一个' : '保存' }}
      </el-button>
    </div>
  </div>
</template>

<!--定义css，控制组件的样式-->
<style scoped>
/* 整体容器 */
.word-show-container {
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
  align-items: center;
  transition: all 0.3s ease;
}

/* 复习结果标题 */
.review-result-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--text-primary);
  text-align: center;
  margin-bottom: 2rem;
}

/* 复习统计 */
.review-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 2rem;
  padding: 1rem;
  background-color: var(--bg-secondary);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-item {
  text-align: center;
  padding: 0.8rem;
  border-radius: 8px;
  min-width: 80px;
}

.stat-item.correct {
  background-color: #f0f9eb;
  color: #67c23a;
}

.stat-item.error {
  background-color: #fef0f0;
  color: #f56c6c;
}

.stat-item.total {
  background-color: var(--bg-tertiary);
  color: var(--primary-color);
}

.stat-label {
  display: block;
  font-size: 0.9rem;
  margin-bottom: 0.3rem;
}

.stat-value {
  display: block;
  font-size: 1.5rem;
  font-weight: bold;
}

/* 结果圆圈 */
.result-circles {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin: 2rem 0;
  justify-content: center;
}

.result-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
}

.result-circle.correct {
  background-color: #67c23a;
  color: white;
}

.result-circle.error {
  background-color: #f56c6c;
  color: white;
}

.result-circle:hover {
  transform: none;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.result-circle.selected {
  transform: scale(1.1);
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.1);
}

.circle-number {
  font-size: 1.2rem;
  font-weight: bold;
}

/* 选中结果详情 */
.selected-result {
  margin: 2rem 0;
  background-color: var(--bg-secondary);
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.result-item {
  background-color: var(--bg-secondary);
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border-left: 4px solid;
  transition: all 0.3s ease;
}

.result-item.correct {
  border-left-color: #67c23a;
}

.result-item.error {
  border-left-color: #f56c6c;
}

.result-header {
  display: flex;
  align-items: center;
  margin-bottom: 0.8rem;
}

.result-word {
  font-weight: bold;
  font-size: 1.1rem;
  flex: 1;
}

.result-status {
  font-size: 0.9rem;
  padding: 0.2rem 0.8rem;
  border-radius: 12px;
  font-weight: bold;
}

.result-item.correct .result-status {
  background-color: #f0f9eb;
  color: #67c23a;
}

.result-item.error .result-status {
  background-color: #fef0f0;
  color: #f56c6c;
}

.result-info {
  margin-bottom: 0.5rem;
  padding: 0.5rem;
  background-color: var(--bg-primary);
  border-radius: 4px;
  transition: all 0.3s ease;
}

.result-label {
  font-size: 0.9rem;
  color: var(--text-tertiary);
  margin-right: 0.5rem;
}

.result-input {
  font-size: 0.9rem;
  color: var(--danger-color);
  font-weight: bold;
}

.result-phonetic {
  font-size: 0.9rem;
  color: var(--text-tertiary);
  font-style: italic;
  margin-bottom: 0.5rem;
}

.result-definition {
  font-size: 0.95rem;
  color: var(--text-secondary);
  line-height: 1.4;
}

/* 复习操作按钮 */
.review-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

.action-button {
  flex: 1;
  height: 48px;
  font-size: 1rem;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.action-button:first-child {
  background-color: var(--bg-secondary);
  border-color: var(--border-color);
  color: var(--text-secondary);
}

.action-button:active {
  transform: scale(0.98);
}

/* 顶部固定区域 */
.top-sticky-section {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: var(--bg-primary);
  z-index: 100;
  padding: env(safe-area-inset-top) 1rem 0;
  /* 移除阴影，保持视觉一致性 */
  box-shadow: none;
  height: auto;
  min-height: 50px;
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

.favorite-button {
  display: flex;
  align-items: center;
  color: var(--text-tertiary);
  padding: 0.5rem;
  font-size: 1rem;
  border-radius: 50%;
  width: 48px;
  height: 48px;
  justify-content: center;
  cursor: pointer;
  transition: color 0.3s ease, background-color 0.3s ease;
}

.favorite-button:hover {
  background-color: transparent;
}

.favorite-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.is-favorite {
  color: #e6a23c;
  transition: all 0.3s ease;
}

/* 单词计数器 */
.word-counter {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.counter-text {
  font-size: 1.1rem;
  font-weight: bold;
  color: var(--text-primary);
}

/* 单词内容容器 */
.word-content {
  flex: 1;
  width: 100%;
  max-width: 600px;
  padding: 2rem 1.5rem;
  box-sizing: border-box;
  background-color: var(--bg-secondary);
  margin: 0 0.8rem;
  margin-top: 40px;
  margin-bottom: 0.8rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  transition: all 0.3s ease;
}

/* 复习结果标题 */
.review-result-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--text-primary);
  text-align: center;
  margin: 0 auto 2rem;
}

/* 复习结果标题 */
.review-result-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin: 0 auto 2rem;
}

/* 复习结果标题 */
.review-result-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin: 0 auto 2rem;
}

/* 单词标题 */
.word-title {
  margin: 0;
  text-align: center;
}

.word-input {
  font-size: 2rem;
  font-weight: bold;
  color: var(--primary-color);
}

/* 确保在黑夜模式下输入框显示正确的背景色 */
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

/* 发音 */
.word-phonetic {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.8rem;
  margin: 0;
}

.phonetic-text {
  font-size: 1rem;
  color: #909399;
  font-style: italic;
}

.pronunciation-button {
  padding: 0;
  color: var(--primary-color);
  cursor: pointer;
}

.pronunciation-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.phonetic-icon {
  color: var(--primary-color);
}

/* 章节标题 */
.section-title {
  font-size: 1rem;
  font-weight: bold;
  color: var(--text-primary);
  margin: 0 0 0.8rem 0;
}

/* 单词定义 */
.word-definition {
  margin: 0;
}

.definition-text {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}



/* 错误状态 */
.error-container {
  padding: 0.4rem 0.6rem;
  background-color: rgba(245, 108, 108, 0.1);
  border: 1px solid rgba(245, 108, 108, 0.3);
  border-radius: 4px;
  color: var(--danger-color);
  margin-bottom: 0.6rem;
  font-size: 0.8rem;
  line-height: 1.4;
  transition: all 0.3s ease;
}

/* 导航按钮 */
.word-controls {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  width: 100%;
  max-width: 600px;
  padding: 1rem;
  box-sizing: border-box;
  background-color: var(--bg-secondary);
  margin: 0 0.8rem;
  margin-bottom: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.nav-button {
  flex: 1;
  height: 48px;
  font-size: 1rem;
  font-weight: 500;
  border-radius: 8px;
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-button:first-child {
  background-color: var(--bg-secondary);
  border-color: var(--border-color);
  color: var(--text-secondary);
}

.nav-button:active {
  transform: scale(0.98);
}

/* 响应式设计 */
@media (max-width: 480px) {
  .word-content {
    padding: 1.5rem 1rem;
    margin: 0 0.5rem;
    margin-top: 40px;
    margin-bottom: 0.5rem;
    gap: 1.2rem;
  }
  
  .word-input {
    font-size: 1.8rem;
  }
  
  .word-phonetic {
    gap: 0.6rem;
  }
  
  .phonetic-text {
    font-size: 0.9rem;
  }
  
  .section-title {
    font-size: 1rem;
    margin-bottom: 0.5rem;
  }
  
  .definition-text {
    font-size: 1rem;
    line-height: 1.5;
  }
  

  
  .word-controls {
    margin: 0 0.5rem;
    margin-bottom: 1rem;
  }
  
  .nav-button {
    height: 44px;
    font-size: 0.9rem;
  }
  
  /* 调整头部导航 */
  .top-sticky-section {
    padding: env(safe-area-inset-top) 0.8rem 0;
  }
  
  .course-header {
    padding: 0.5rem 0;
  }
  
  .back-button,
  .favorite-button {
    width: 36px;
    height: 36px;
  }
  
  .counter-text {
    font-size: 1rem;
  }
  

}
</style>