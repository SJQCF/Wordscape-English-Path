<template>
  <div class="ai-writing-container">
    <!-- 头部导航 -->
    <div class="header">
      <el-button type="text" @click="goBack" class="back-button">
        <el-icon size="24"><ArrowLeft /></el-icon>
      </el-button>
      <h1 class="page-title">AI写作</h1>
      <div class="header-placeholder"></div>
    </div>
    
    <!-- 聊天内容区域 -->
    <div class="chat-container" ref="chatContainer">
      <!-- 消息列表 -->
      <div class="message-list">
        <!-- 系统消息 -->
        <div class="message-item system-message">
          <div class="message-content">
            <p>你好！我是你的AI写作助手，有什么可以帮你写的吗？</p>
          </div>
        </div>
        
        <!-- 消息对 -->
        <div v-for="(pair, index) in messagePairs" :key="index" class="message-pair">
          <!-- 用户消息 -->
          <div class="message-item user">
            <div class="user-message-wrapper">
              <div class="message-content">
                <p v-if="pair.userMessage.content">{{ pair.userMessage.content }}</p>
                <div v-if="pair.userMessage.image" class="message-image">
                  <el-image :src="pair.userMessage.image" fit="cover" />
                </div>
              </div>
              <div class="user-avatar">
              <el-avatar size="40" :src="userAvatar" />
            </div>
            </div>
            <div class="message-time">{{ pair.userMessage.time }}</div>
          </div>
          
          <!-- AI回复 -->
          <div v-if="pair.aiResponse" class="message-item ai-message">
            <div class="message-content">
              <p>{{ pair.aiResponse.content }}</p>
              <div v-if="pair.aiResponse.hasExample" class="example-section">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="pair.showExample = !pair.showExample"
                  class="example-button"
                >
                  {{ pair.showExample ? '隐藏范文' : '查看范文' }}
                </el-button>
                <div v-if="pair.showExample" class="example-content">
                  <p>{{ pair.aiResponse.example }}</p>
                  <div v-if="pair.aiResponse.hasTranslation" class="translation-content">
                    <p>{{ pair.aiResponse.translation }}</p>
                  </div>
                </div>
              </div>
            </div>
            <div class="message-time">{{ pair.aiResponse.time }}</div>
          </div>
          
          <!-- 加载状态 -->
          <div v-if="pair.loading" class="message-item ai-message">
            <div class="message-content">
              <div class="loading-indicator">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>AI正在思考...</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 输入区域 -->
    <div class="input-container">
      <!-- 图片选择按钮 -->
      <div class="image-button" @click="showImagePicker">
        <el-icon size="28"><Camera /></el-icon>
      </div>
      
      <!-- 输入框 -->
      <div class="input-wrapper">
        <!-- 图片预览 -->
        <div v-if="tempImage" class="image-preview">
          <el-image :src="tempImage" fit="contain" class="preview-image" />
          <el-button type="text" class="remove-image" @click="tempImage = null">
            <el-icon size="20"><Close /></el-icon>
          </el-button>
        </div>
        
        <el-input
          v-model="inputMessage"
          placeholder="发消息或按住说话..."
          @keyup.enter="sendMessage"
          class="message-input"
          :rows="2"
          type="textarea"
        />
      </div>
      

      
      <!-- 发送按钮 -->
      <el-button type="primary" round @click="sendMessage" class="send-button">
        <el-icon size="20"><ArrowRight /></el-icon>
      </el-button>
    </div>
    
    <!-- 图片选择弹窗 -->
    <el-dialog
      v-model="imagePickerVisible"
      title="选择图片"
      width="80%"
      destroy-on-close
    >
      <div class="image-picker-options">
        <el-button type="primary" round @click="takePhoto" class="picker-button">
          <el-icon size="24"><Camera /></el-icon>
          <span>拍摄照片</span>
        </el-button>
        <el-button type="primary" round @click="chooseImage" class="picker-button">
          <el-icon size="24"><Picture /></el-icon>
          <span>从相册选择</span>
        </el-button>
      </div>
      <input
        type="file"
        ref="fileInput"
        accept="image/*"
        style="display: none"
        @change="handleImageSelect"
      />
      <input
        type="file"
        ref="cameraInput"
        accept="image/*"
        capture="camera"
        style="display: none"
        @change="handleImageSelect"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ArrowLeft, Camera, Loading, Picture, ArrowRight, Close } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserAvatar } from '@/api/user.js'
import { getCurrentUserId } from '@/utils/auth.js'
import { createArticle, createArticleWithPicture } from '@/api/ai.js'
import { useTheme } from '@/composables/useTheme'

// 获取路由实例
const router = useRouter()

// 返回上一页
const goBack = () => {
  router.back()
}

// 聊天容器引用
const chatContainer = ref(null)

// 消息列表 - 改为一问一答的结构
const messagePairs = ref([])

// 输入消息
const inputMessage = ref('')

// 加载状态
const loading = ref(false)

// 临时存储选中的图片
const tempImage = ref(null)
// 临时存储选中的文件对象
const tempFile = ref(null)

// 用户头像
const userAvatar = ref('https://picsum.photos/seed/user/200/200')

// 获取用户头像
const fetchUserAvatar = async () => {
  try {
    // 优先从缓存中获取
    const cachedAvatar = localStorage.getItem('userAvatar')
    if (cachedAvatar) {
      userAvatar.value = cachedAvatar
      return
    }
    
    // 从登录信息获取用户ID
    const userId = getCurrentUserId()
    const response = await getUserAvatar(userId)
    if (response && response.code === 1 && response.data) {
      userAvatar.value = response.data
      // 缓存头像
      localStorage.setItem('userAvatar', response.data)
    }
  } catch (error) {
    console.error('获取用户头像失败:', error)
    // 保持默认头像
  }
}

// 图片选择弹窗
const imagePickerVisible = ref(false)
const fileInput = ref(null)
const cameraInput = ref(null)

// 显示图片选择器
const showImagePicker = () => {
  imagePickerVisible.value = true
}

// 拍摄照片
const takePhoto = () => {
  imagePickerVisible.value = false
  cameraInput.value.click()
}

// 从相册选择
const chooseImage = () => {
  imagePickerVisible.value = false
  fileInput.value.click()
}

// 处理图片选择
const handleImageSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    // 检查文件大小
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.warning('图片大小不能超过5MB')
      return
    }
    
    // 创建图片URL
    const imageUrl = URL.createObjectURL(file)
    
    // 存储到临时变量，等待用户输入文字
    tempImage.value = imageUrl
    tempFile.value = file
    
    // 提示用户可以输入文字
    ElMessage.success('图片已选择，请输入文字后发送')
  }
  
  // 重置input
  event.target.value = ''
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() && !tempImage.value) return
  
  // 清空输入框
  const message = inputMessage.value.trim()
  inputMessage.value = ''
  
  // 添加到消息对列表
  const newPair = {
    userMessage: {
      content: message,
      image: tempImage.value,
      time: getCurrentTime()
    },
    aiResponse: null,
    loading: true
  }
  
  // 直接添加到数组
  messagePairs.value.push(newPair)
  
  // 清空临时图片和文件
  const file = tempFile.value
  tempImage.value = null
  tempFile.value = null
  
  // 滚动到底部
  scrollToBottom()
  
  // 调用AI创作API
  try {
    console.log('开始调用API...')
    let response
    
    // 根据是否有图片选择不同的API
    if (file) {
      // 调用带图片的创作API
      response = await createArticleWithPicture(file, message)
    } else {
      // 调用普通创作API
      response = await createArticle(message)
    }
    
    console.log('API调用成功:', response)
    
    if (response.code === 1 && response.data) {
      // 提取AI生成的文章内容
      let aiContent = response.data
      console.log('原始AI内容长度:', aiContent.length)
      console.log('原始AI内容:', aiContent)
      
      // 处理返回的结果，提取实际的文章内容
      // 查找text字段
      // 改进正则表达式，处理包含单引号的情况
      const textMatch = aiContent.match(/text='([\s\S]*?)(?='\s*,)/)
      if (textMatch && textMatch[1]) {
        aiContent = textMatch[1]
        console.log('提取后的AI内容长度:', aiContent.length)
        console.log('提取后的AI内容:', aiContent)
      } else {
        // 如果正则匹配失败，尝试直接提取text字段后的内容
        const textStart = aiContent.indexOf("text='")
        if (textStart !== -1) {
          let textEnd = aiContent.indexOf("',", textStart + 6)
          if (textEnd === -1) {
            textEnd = aiContent.indexOf("'}", textStart + 6)
          }
          if (textEnd !== -1) {
            aiContent = aiContent.substring(textStart + 6, textEnd)
            console.log('使用子字符串提取的内容长度:', aiContent.length)
            console.log('使用子字符串提取的内容:', aiContent)
          }
        }
      }
      
      // 分割内容为结构部分、范文部分和翻译部分
      let content = aiContent
      let example = ''
      let translation = ''
      let hasExample = false
      let hasTranslation = false
      
      // 查找范文部分的开始位置（尝试多种格式）
      const examplePatterns = ['### <范文>', '### <范文>', '## 范文', '### 范文']
      let exampleIndex = -1
      let examplePattern = ''
      
      for (const pattern of examplePatterns) {
        exampleIndex = aiContent.indexOf(pattern)
        if (exampleIndex !== -1) {
          examplePattern = pattern
          break
        }
      }
      
      // 查找翻译部分的开始位置
      const translationPatterns = ['## 范文翻译', '### 范文翻译', '翻译：', '参考翻译：', '**翻译**', '### 翻译']
      let translationIndex = -1
      let translationPattern = ''
      
      for (const pattern of translationPatterns) {
        translationIndex = aiContent.indexOf(pattern)
        if (translationIndex !== -1) {
          translationPattern = pattern
          break
        }
      }
      
      if (exampleIndex !== -1) {
        // 提取结构部分
        content = aiContent.substring(0, exampleIndex).trim()
        
        // 提取范文部分
        if (translationIndex !== -1 && translationIndex > exampleIndex) {
          example = aiContent.substring(exampleIndex + examplePattern.length, translationIndex).trim()
          translation = aiContent.substring(translationIndex + translationPattern.length).trim()
          hasTranslation = true
        } else {
          example = aiContent.substring(exampleIndex + examplePattern.length).trim()
          // 检查是否在范文部分末尾包含翻译
          const endTranslationMatch = example.match(/(翻译：|参考翻译：)[\s\S]*$/)
          if (endTranslationMatch) {
            const matchIndex = example.indexOf(endTranslationMatch[0])
            translation = example.substring(matchIndex + endTranslationMatch[1].length).trim()
            example = example.substring(0, matchIndex).trim()
            hasTranslation = true
          }
        }
        
        console.log('最终范文部分:', example)
        console.log('最终翻译部分:', translation)
        hasExample = true
      }
      
      // 移除结构部分中的###标记
      content = content.replace(/^###\s*/gm, '').trim()
      
      console.log('结构部分:', content)
      console.log('范文部分:', example)
      console.log('翻译部分:', translation)
      console.log('是否有范文:', hasExample)
      console.log('是否有翻译:', hasTranslation)
      console.log('范文开始位置:', exampleIndex)
      console.log('翻译开始位置:', translationIndex)
      
      console.log('结构部分:', content)
      console.log('范文部分:', example)
      console.log('是否有范文:', hasExample)
      console.log('范文开始位置:', exampleIndex)
      
      console.log('结构部分:', content)
      console.log('范文部分:', example)
      
      // 创建新的消息对
      const updatedPairs = messagePairs.value.map((pair, index) => {
        if (index === messagePairs.value.length - 1) {
          const newPair = {
            ...pair,
            aiResponse: {
              content: content,
              example: example,
              translation: translation,
              hasExample: hasExample,
              hasTranslation: hasTranslation,
              time: getCurrentTime()
            },
            showExample: false,
            loading: false
          }
          console.log('更新后的消息对:', newPair)
          return newPair
        }
        return pair
      })
      
      // 直接赋值更新
      messagePairs.value = updatedPairs
      console.log('消息对列表:', messagePairs.value)
    } else {
      console.log('API返回错误:', response)
      // 使用Vue的响应式更新方式
      messagePairs.value = messagePairs.value.map((pair, index) => {
        if (index === messagePairs.value.length - 1) {
          return {
            ...pair,
            aiResponse: {
              content: '抱歉，AI创作失败，请稍后重试',
              time: getCurrentTime()
            },
            loading: false
          }
        }
        return pair
      })
    }
  } catch (error) {
    console.error('AI创作失败:', error)
    // 使用Vue的响应式更新方式
    messagePairs.value = messagePairs.value.map((pair, index) => {
      if (index === messagePairs.value.length - 1) {
        return {
          ...pair,
          aiResponse: {
            content: '抱歉，AI创作失败，请稍后重试',
            time: getCurrentTime()
          },
          loading: false
        }
      }
      return pair
    })
  } finally {
    // 滚动到底部
    scrollToBottom()
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

// 获取当前时间
const getCurrentTime = () => {
  const now = new Date()
  const hours = now.getHours().toString().padStart(2, '0')
  const minutes = now.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

// 初始化主题
const { initTheme } = useTheme()

// 组件挂载时滚动到底部
onMounted(() => {
  scrollToBottom()
  // 获取用户头像
  fetchUserAvatar()
  // 初始化主题
  initTheme()
})
</script>

<style scoped>
/* 整体容器 */
.ai-writing-container {
  width: 100%;
  height: 100vh;
  box-sizing: border-box;
  background-color: var(--bg-secondary);
  color: var(--text-primary);
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
  background-color: var(--bg-secondary);
  z-index: 10;
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

/* 聊天容器 */
.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  background-color: var(--bg-secondary);
  max-width: 600px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

/* 确保用户消息在所有设备上都正确显示 */
.user-message-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 0.2rem;
  width: 100%;
  justify-content: flex-end;
  max-width: 95%;
  position: relative;
  right: -25px;
}

/* 消息列表 */
.message-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.message-pair {
  margin-bottom: 1.2rem;
}

/* 消息项 */
.message-item {
  display: flex;
  flex-direction: column;
  max-width: 80%;
}

/* 系统消息 */
.system-message {
  align-self: center;
  max-width: 90%;
}

.system-message .message-content {
  background-color: rgba(255, 152, 0, 0.1);
  color: var(--primary-color);
  border-radius: 12px;
  padding: 0.8rem;
  font-size: 0.9rem;
  text-align: center;
}

/* 用户消息 */
.user {
  align-self: flex-end;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.3rem;
}

.user-message-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 0.5rem;
}

.user .message-content {
  background-color: var(--primary-color);
  color: white;
  border-radius: 12px 12px 0 12px;
  padding: 0.8rem;
  font-size: 0.9rem;
  max-width: calc(100% - 25px);
  position: relative;
  flex: 1;
  word-wrap: break-word;
  white-space: pre-wrap;
  word-break: break-word;
  width: auto !important;
}

.user-avatar {
  flex-shrink: 0;
  align-self: flex-end;
  margin-bottom: 0;
  margin-left: 0 !important;
  width: 40px !important;
  height: 40px !important;
  position: relative;
  right: -15px;
}

.user-avatar .el-avatar {
    width: 40px !important;
    height: 40px !important;
    font-size: 20px !important;
  }

.user .message-time {
  align-self: flex-end;
  margin-top: 0.2rem;
}

/* AI消息 */
.ai-message {
  align-self: flex-start;
}

.ai-message .message-content {
  background-color: var(--bg-primary);
  color: var(--text-primary);
  border-radius: 12px 12px 12px 0;
  padding: 0.8rem;
  font-size: 0.9rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  word-wrap: break-word;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.5;
  border: 1px solid var(--border-color);
}

/* 范文部分 */
.example-section {
  margin-top: 1rem;
}

.example-section .el-button.example-button {
  margin-bottom: 0.8rem !important;
  width: auto !important;
  max-width: 100px !important;
  padding: 0.2rem 0.6rem !important;
  font-size: 0.8rem !important;
  min-width: unset !important;
}

.example-content {
  background-color: rgba(255, 152, 0, 0.05);
  border-left: 3px solid var(--primary-color);
  padding: 0.8rem;
  border-radius: 0 8px 8px 0;
  margin-top: 0.5rem;
  font-size: 0.9rem;
  line-height: 1.5;
}

.translation-content {
  background-color: rgba(0, 122, 255, 0.05);
  border-left: 3px solid #007aff;
  padding: 0.8rem;
  border-radius: 0 8px 8px 0;
  margin-top: 0.8rem;
  font-size: 0.9rem;
  line-height: 1.5;
}

/* 范文部分 */
.example-section {
  margin-top: 1rem;
}

.example-button {
  margin-bottom: 0.8rem;
  width: 100%;
  max-width: 200px;
}

.example-content {
  background-color: rgba(255, 152, 0, 0.05);
  border-left: 3px solid var(--primary-color);
  padding: 0.8rem;
  border-radius: 0 8px 8px 0;
  margin-top: 0.5rem;
  font-size: 0.9rem;
  line-height: 1.5;
}

/* 消息时间 */
.message-time {
  font-size: 0.75rem;
  color: var(--text-tertiary);
  margin-top: 0.3rem;
  align-self: flex-end;
}

.ai-message .message-time {
  align-self: flex-start;
}

/* 消息图片 */
.message-image {
  margin-top: 0.5rem;
  border-radius: 8px;
  overflow: hidden;
}

.message-image img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
}

/* 加载指示器 */
.loading-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* 输入容器 */
.input-container {
  display: flex;
  align-items: flex-end;
  gap: 0.8rem;
  padding: 1rem;
  padding-bottom: 2.5rem !important;
  background-color: var(--bg-primary);
  border-top: 1px solid var(--border-color);
  max-width: 600px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
  padding-left: calc(1rem + env(safe-area-inset-left));
  padding-right: calc(1rem + env(safe-area-inset-right));
  padding-bottom: calc(2.5rem + env(safe-area-inset-bottom)) !important;
  transition: all 0.3s ease;
}

.input-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.image-preview {
  position: relative;
  width: 100%;
  max-height: 150px;
  border-radius: 8px;
  overflow: hidden;
  background-color: var(--bg-secondary);
}

.preview-image {
  width: 100%;
  max-height: 150px;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.remove-image:hover {
  background-color: rgba(0, 0, 0, 0.7);
}

.input-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.image-preview {
  position: relative;
  width: 100%;
  max-height: 150px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f5f7fa;
}

.preview-image {
  width: 100%;
  height: auto;
  max-height: 150px;
  object-fit: contain;
}

.remove-image {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.remove-image:hover {
  background-color: transparent;
  color: var(--text-secondary);
}

/* 图片按钮 */
.image-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  color: var(--text-secondary);
  background-color: var(--bg-tertiary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.image-button:hover {
  background-color: transparent;
  border-color: var(--border-color);
  color: var(--text-secondary);
}

/* 消息输入框 */
.message-input {
  flex: 1;
  border-radius: 20px;
  min-height: 60px;
  resize: vertical;
}

.message-input :deep(.el-textarea__inner) {
  resize: vertical;
}

.message-input :deep(.el-textarea__inner) {
  border-radius: 20px;
  min-height: 60px !important;
  max-height: 150px !important;
  resize: vertical !important;
  font-size: 1.2rem !important;
  line-height: 60px !important;
  padding: 0 16px !important;
  text-align: left !important;
  vertical-align: middle !important;
  word-wrap: break-word !important;
  white-space: pre-wrap !important;
  word-break: break-word !important;
  height: 60px !important;
}

.message-input :deep(.el-textarea) {
  margin-bottom: 0;
  display: flex;
  align-items: center;
}

.message-input :deep(.el-textarea) {
  margin-bottom: 0;
}



/* 发送按钮 */
.send-button {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

/* 图片选择弹窗 */
.image-picker-options {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: 1rem;
  width: 100%;
  box-sizing: border-box;
}

.picker-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.8rem;
  padding: 1.2rem;
  font-size: 1rem;
  width: 100%;
  height: 60px;
  box-sizing: border-box;
  border-radius: 8px;
  margin: 0 auto;
  max-width: 90%;
}

@media screen and (max-width: 480px) {
  .picker-button {
    height: 56px !important;
    padding: 1rem !important;
    font-size: 1rem !important;
    max-width: 100% !important;
  }
  
  .image-picker-options {
    padding: 1rem !important;
  }
}

/* 响应式设计 */
@media screen and (max-width: 480px) {
  .chat-container {
    padding: 0.8rem;
  }
  
  .message-item {
    max-width: 95% !important;
  }
  
  .user .message-content {
    max-width: calc(100% - 30px) !important;
    width: auto !important;
    flex: 1 !important;
  }
  
  .user-message-wrapper {
    max-width: 95% !important;
    width: 100% !important;
    justify-content: flex-end !important;
  }
  
  .user {
    width: 100% !important;
    align-items: flex-end !important;
  }
  
  .user-avatar {
    flex-shrink: 0 !important;
    margin-left: 0 !important;
    width: 32px !important;
    height: 32px !important;
  }
  
  .chat-container {
    padding: 0.8rem !important;
    width: 100% !important;
  }
  
  .message-list {
    width: 100% !important;
  }
  
  .message-content {
    padding: 0.7rem;
    font-size: 0.85rem;
  }
  
  .input-container {
    padding: 0.8rem !important;
    padding-bottom: 2rem !important;
    gap: 0.6rem;
    padding-bottom: calc(2rem + env(safe-area-inset-bottom)) !important;
  }
  
  .image-button {
    width: 48px;
    height: 48px;
  }
  

  
  .send-button {
    width: 48px;
    height: 48px;
  }
  
  .message-input {
    flex: 1;
    min-height: 48px !important;
    max-height: 48px !important;
    height: 48px !important;
  }
  
  .message-input :deep(.el-textarea__inner) {
    min-height: 48px !important;
    max-height: 120px !important;
    font-size: 1.1rem !important;
    line-height: 1.3 !important;
    padding: 10px 14px !important;
    text-align: left !important;
    vertical-align: top !important;
    word-wrap: break-word !important;
    white-space: pre-wrap !important;
    word-break: break-word !important;
    height: 48px !important;
    display: block !important;
  }
  
  .message-input :deep(.el-textarea) {
    height: 48px !important;
    min-height: 48px !important;
    max-height: 48px !important;
  }
  
  .message-input :deep(.el-textarea) {
    margin-bottom: 0 !important;
    display: block !important;
    min-height: 48px !important;
    max-height: 120px !important;
  }
  
  .input-container {
    align-items: flex-end;
  }
  
  .message-input :deep(.el-textarea__inner) {
    resize: none !important;
  }
  
  .user {
    gap: 0.2rem;
  }
  
  .user-message-wrapper {
    gap: 0.2rem !important;
    width: 100%;
    justify-content: flex-end;
  }
  
  .user .message-content {
    max-width: calc(100% - 40px) !important;
    padding: 0.7rem;
    width: auto !important;
  }
  
  .user-message-wrapper {
    width: 100% !important;
    justify-content: flex-end !important;
  }
  
  .message-item {
    max-width: 85% !important;
  }
  
  .ai-message .message-content {
    max-width: 100% !important;
    width: auto !important;
  }
  
  .user-avatar {
    flex-shrink: 0;
  }
  
  .user-avatar .el-avatar {
    width: 28px;
    height: 28px;
  }
}
</style>