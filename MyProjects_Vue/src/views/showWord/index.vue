<!--定义js，控制组件的行为-->
<script setup>
import { ref, onMounted } from 'vue'
import { getCurrentUserId } from '@/utils/auth.js'
import { ArrowLeft, Headset, Star, StarFilled } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { getUserStudyWord, getStudyPlan } from '@/api/study'
import { favoriteWord, cancelFavoriteWord, checkFavoriteWord, getWordById, searchWord } from '@/api/word'
import { ElMessage } from 'element-plus'
import { useTheme } from '@/composables/useTheme'

const { initTheme } = useTheme()

// 获取路由实例
const router = useRouter()
// 获取当前路由
const route = useRoute()

// 返回上一页
const goBack = () => {
  router.back()
}

// 单词数据
const words = ref([])
// 当前单词索引
const currentWordIndex = ref(0)
// 当前单词
const currentWord = ref(null)
// 加载状态
const loading = ref(true)
// 错误信息
const error = ref('')
// 收藏状态
const isFavorite = ref(false)
// 收藏加载状态
const favoriting = ref(false)

// 收藏/取消收藏单词
const handleFavorite = async () => {
  if (!currentWord.value) return
  
  favoriting.value = true
  try {
    // 假设用户ID为1
    const userId = getCurrentUserId()
    const wordId = currentWord.value.id
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

// 上一个单词
const prevWord = async () => {
  if (currentWordIndex.value > 0) {
    currentWordIndex.value--
    currentWord.value = words.value[currentWordIndex.value]
    // 检查当前单词的收藏状态
    await checkWordFavoriteStatus(currentWord.value.id)
  }
}

// 下一个单词或结束学习
const handleNextOrFinish = async () => {
  // 检查是否有单词ID参数
  const wordId = route.query.id
  
  if (wordId) {
    // 如果是从单词详情页跳转过来的，返回上一页
    router.back()
  } else if (currentWordIndex.value < words.value.length - 1) {
    // 不是最后一个单词，显示下一个
    currentWordIndex.value++
    currentWord.value = words.value[currentWordIndex.value]
    // 检查当前单词的收藏状态
    await checkWordFavoriteStatus(currentWord.value.id)
  } else {
    // 是最后一个单词，跳转到单词输入页面进行复习
    router.push({
      path: '/inputWord',
      query: {
        words: JSON.stringify(words.value),
        mode: 'study'
      }
    })
  }
}

// 播放发音
const playPronunciation = () => {
  // 这里可以实现发音功能，例如使用Web Speech API
  console.log('Play pronunciation for:', currentWord.value.word)
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

// 获取单词数据
const fetchWords = async () => {
  try {
    loading.value = true
    error.value = ''
    
    // 检查是否有单词ID参数
    const wordId = route.query.id
    const passedWord = route.query.word
    console.log('获取单词数据，wordId:', wordId, 'passedWord:', passedWord)
    
    // 优先使用searchWord API获取单词详情
    if (passedWord) {
      console.log('调用searchWord API，word:', passedWord)
      const response = await searchWord(passedWord, 1)
      console.log('searchWord API响应:', response)
      
      if (response.code && response.data && response.data.length > 0) {
        // 从搜索结果中找到对应ID的单词
        let word = response.data.find(item => item.id == wordId)
        console.log('从搜索结果中找到的单词:', word)
        
        // 如果没找到，使用第一个结果
        if (!word) {
          word = response.data[0]
          console.log('没找到对应ID的单词，使用第一个结果:', word)
        }
        
        words.value = [word]
        currentWord.value = word
        // 检查单词的收藏状态
        await checkWordFavoriteStatus(word.id)
      } else {
        console.log('获取单词详情失败，响应:', response)
        words.value = []
        currentWord.value = null
        error.value = '获取单词详情失败'
      }
    } else if (wordId) {
      // 如果只有单词ID，使用getWordById API获取单词详情
      console.log('调用getWordById API，wordId:', wordId)
      const response = await getWordById(wordId)
      console.log('getWordById API响应:', response)
      
      if (response.code && response.data) {
        let word = response.data
        console.log('获取到的单词数据:', word)
        
        // 确保单词数据结构正确
        if (!word.word && word.name) {
          // 如果API返回的是name字段而不是word字段，进行转换
          word.word = word.name
          console.log('转换后的单词数据:', word)
        }
        
        words.value = [word]
        currentWord.value = word
        // 检查单词的收藏状态
        await checkWordFavoriteStatus(word.id)
      } else {
        console.log('获取单词详情失败，响应:', response)
        words.value = []
        currentWord.value = null
        error.value = '获取单词详情失败'
      }
    } else {
      // 如果没有单词ID，获取单词列表
      const userId = getCurrentUserId()
      
      // 先从本地存储获取学习计划设置
      let studyNum = 10 // 默认值
      const savedPlan = localStorage.getItem('studyPlan')
      if (savedPlan) {
        try {
          const parsedPlan = JSON.parse(savedPlan)
          studyNum = parsedPlan.dailyNewWords || 10
        } catch (error) {
          console.error('解析本地学习计划失败:', error)
        }
      }
      
      // 如果本地存储没有，再从API获取
      if (studyNum === 10) {
        try {
          const planResponse = await getStudyPlan(userId)
          if (planResponse.code && planResponse.data) {
            studyNum = planResponse.data
          }
        } catch (planError) {
          console.error('获取学习计划失败，使用默认值:', planError)
        }
      }
      
      // 使用计划中的数量获取单词
      const response = await getUserStudyWord(userId, studyNum)
      if (response.data && response.data.length > 0) {
        words.value = response.data
        currentWord.value = words.value[0]
        // 检查第一个单词的收藏状态
        await checkWordFavoriteStatus(words.value[0].id)
      } else {
        words.value = []
        currentWord.value = null
        // 弹出提示框，表示目前已学习全部单词
        ElMessage({
          message: '目前已学习全部单词',
          type: 'success',
          duration: 3000
        })
        // 跳转到学习页面
        setTimeout(() => {
          router.push('/study')
        }, 1500)
      }
    }
  } catch (err) {
    console.error('获取单词数据失败:', err)
    error.value = '获取单词数据失败，请稍后重试'
    // 只有当不是从搜索页面跳转过来时，才使用模拟数据
    const wordId = route.query.id
    const passedWord = route.query.word
    if (!wordId) {
      // 使用模拟数据作为备选
      words.value = [
        {
          id: 1,
          word: 'Apple',
          phonetic: '/ˈæpl/',
          definition: '苹果，一种常见的水果',
          example: 'I eat an apple every day.'
        },
        {
          id: 2,
          word: 'Banana',
          phonetic: '/bəˈnɑːnə/',
          definition: '香蕉，一种黄色的水果',
          example: 'Bananas are rich in potassium.'
        },
        {
          id: 3,
          word: 'Orange',
          phonetic: '/ˈɔːrɪndʒ/',
          definition: '橙子，一种橙色的水果',
          example: 'I like to drink orange juice.'
        }
      ]
      currentWord.value = words.value[0]
      // 检查第一个单词的收藏状态
      checkWordFavoriteStatus(words.value[0].id)
    } else if (passedWord) {
      // 如果有传递的单词，使用它作为备选
      const word = {
        id: wordId,
        word: passedWord
      }
      words.value = [word]
      currentWord.value = word
      console.log('使用传递的word参数作为备选:', word)
    }
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取数据
onMounted(() => {
  initTheme()
  fetchWords()
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
          <span class="counter-text">{{ currentWordIndex + 1 }}/{{ words.length }}</span>
        </div>
        <el-button type="text" @click="handleFavorite" class="favorite-button" :loading="favoriting"><el-icon size="32" :class="{ 'is-favorite': isFavorite }"><StarFilled v-if="isFavorite" /><Star v-else /></el-icon></el-button>
      </div>
    </div>
    
    <!-- 单词内容容器 -->
    <div class="word-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-loading type="spinner" color="#409EFF" />
        <p>加载中...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-container">
        <p>{{ error }}</p>
        <el-button type="primary" @click="fetchWords">重试</el-button>
      </div>
      
      <!-- 单词内容 -->
      <template v-else-if="currentWord">
        <!-- 单词标题 -->
        <h1 class="word-title">{{ currentWord.word }}</h1>
        
        <!-- 发音 -->
        <div class="word-phonetic">
          <span class="phonetic-text">{{ currentWord.phonetic }}</span>
          <el-button type="text" @click="playPronunciation" class="pronunciation-button">
            <el-icon size="16" class="phonetic-icon"><Headset /></el-icon>
          </el-button>
        </div>
        
        <!-- 单词定义 -->
        <div class="word-definition">
          <h3 class="section-title">释义</h3>
          <p class="definition-text">{{ currentWord.definition }}</p>
        </div>
        
        <!-- 单词示例 -->
        <div class="word-example">
          <h3 class="section-title">示例</h3>
          <p class="example-text">{{ currentWord.example }}</p>
        </div>
      </template>
      
      <!-- 空状态 -->
      <div v-else class="empty-container">
        <p>暂无单词数据</p>
      </div>
    </div>
    
    <!-- 导航按钮 -->
    <div v-if="!route.query.id" class="word-controls">
      <el-button 
        :disabled="currentWordIndex === 0"
        @click="prevWord"
        :class="['nav-button', { 'disabled-button': currentWordIndex === 0 }]"
      >
        上一个
      </el-button>
      <el-button 
        :disabled="false"
        @click="handleNextOrFinish"
        :class="['nav-button', { 'disabled-button': currentWordIndex === words.length - 1 }]"
      >
        {{ currentWordIndex === words.length - 1 ? '结束' : '下一个' }}
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
  transition: all 0.3s ease;
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
  transition: all 0.3s ease;
}

.is-favorite {
  color: var(--warning-color);
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
  transition: all 0.3s ease;
}

/* 单词内容容器 */
.word-content {
  flex: 1;
  width: 100%;
  max-width: 600px;
  padding: 2rem 1.5rem;
  box-sizing: border-box;
  background-color: var(--bg-secondary);
  border: 1px solid var(--border-color);
  margin: 0 0.8rem;
  margin-top: 40px;
  margin-bottom: 0.8rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  transition: all 0.3s ease;
}

/* 单词标题 */
.word-title {
  font-size: 2rem;
  font-weight: bold;
  color: var(--primary-color);
  margin: 0;
  text-align: center;
  transition: all 0.3s ease;
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
  color: var(--text-tertiary);
  font-style: italic;
  transition: all 0.3s ease;
}

.pronunciation-button {
  padding: 0;
  color: var(--primary-color);
  cursor: pointer;
  transition: all 0.3s ease;
}

.phonetic-icon {
  color: var(--primary-color);
  transition: all 0.3s ease;
}

/* 章节标题 */
.section-title {
  font-size: 1rem;
  font-weight: bold;
  color: var(--text-primary);
  margin: 0 0 0.8rem 0;
  transition: all 0.3s ease;
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
  padding: 0.5rem 0;
  transition: all 0.3s ease;
}

/* 单词示例 */
.word-example {
  margin: 0;
}

.example-text {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
  padding: 1rem;
  background-color: var(--bg-primary);
  border-left: 4px solid var(--primary-color);
  border-radius: 0;
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
  border: 1px solid var(--border-color);
  margin: 0 0.8rem;
  margin-bottom: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
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

.nav-button:active {
  transform: scale(0.98);
}

.disabled-button {
  background-color: rgba(255, 152, 0, 0.3) !important;
  border-color: rgba(255, 152, 0, 0.3) !important;
  color: white !important;
  transition: all 0.3s ease;
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
  
  .word-title {
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
    padding: 0.3rem 0;
  }
  
  .example-text {
    font-size: 0.9rem;
    line-height: 1.5;
    padding: 0.8rem;
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
  
  .header-placeholder {
    width: 36px;
  }
}
</style>