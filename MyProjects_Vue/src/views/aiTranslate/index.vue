<template>
  <div class="ai-translate-container">
    <!-- 头部导航 -->
    <div class="course-header">
      <el-button type="text" @click="goBack" class="back-button">
        <el-icon size="24"><ArrowLeft /></el-icon>
      </el-button>
      <div class="header-title">AI翻译</div>
      <div class="header-right"></div>
    </div>
    
    <!-- 翻译内容 -->
    <div class="translate-content">
      <!-- 输入区域 -->
      <div class="input-section">
        <h3 class="section-title">输入文本</h3>
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="6"
          placeholder="请输入要翻译的文本..."
          class="translate-input"
        />
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-section">
        <el-button 
          type="primary" 
          @click="translate" 
          :loading="loading"
          class="translate-button"
        >
          翻译
        </el-button>
      </div>
      
      <!-- 输出区域 -->
      <div class="output-section" v-if="outputText">
        <div class="section-header">
          <h3 class="section-title">翻译结果</h3>
          <div class="header-actions">
            <el-button 
              type="text" 
              @click="copyResult" 
              class="copy-button"
              title="复制翻译结果"
            >
              <el-icon size="16"><DocumentCopy /></el-icon>
              <span>复制</span>
            </el-button>
          </div>
        </div>
        
        <!-- 翻译结果 -->
        <div class="translate-output">
          {{ outputText }}
        </div>
      </div>
      
      <!-- 占位符 -->
      <div class="output-section placeholder-section" v-else>
        <div class="section-header">
          <h3 class="section-title">翻译结果</h3>
        </div>
        <div class="translate-output placeholder">
          翻译结果将显示在这里...
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ArrowLeft, DocumentCopy, Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { translateText } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { useTheme } from '@/composables/useTheme'

const { initTheme } = useTheme()

// 路由实例
const router = useRouter()

// 输入文本
const inputText = ref('')
// 输出文本
const outputText = ref('')
// 加载状态
const loading = ref(false)



// 返回上一页
const goBack = () => {
  router.back()
}

// 翻译文本
const translate = async () => {
  if (!inputText.value.trim()) {
    ElMessage.warning('请输入要翻译的文本')
    return
  }
  
  loading.value = true
  
  try {
    console.log('开始翻译...')
    console.log('输入文本:', inputText.value)
    console.log('输入文本类型:', typeof inputText.value)
    console.log('输入文本长度:', inputText.value.length)
    const response = await translateText(inputText.value)
    console.log('响应数据:', response)
    
    if (response.code) {
      // 提取有效部分
      let extractedText = ''
      
      if (typeof response.data === 'string') {
        console.log('响应数据是字符串:', response.data)
        // 尝试从字符串中提取text部分
        // 方案1：使用正则表达式匹配text='开始到annotations之前的内容
        const textMatch = response.data.match(/text='([\s\S]+?)'\s*,\s*annotations/)
        console.log('正则匹配结果:', textMatch)
        
        if (textMatch && textMatch[1]) {
          extractedText = textMatch[1]
          console.log('使用正则提取的文本:', extractedText)
        } else {
          // 方案2：直接查找text='和annotations之间的内容
          const startIndex = response.data.indexOf("text='")
          const endIndex = response.data.indexOf("', annotations")
          console.log('startIndex:', startIndex, 'endIndex:', endIndex)
          
          if (startIndex !== -1 && endIndex !== -1) {
            extractedText = response.data.substring(startIndex + 6, endIndex)
            console.log('使用字符串索引提取的文本:', extractedText)
          } else {
            // 方案3：尝试从整个字符串中提取所有可能的文本内容
            const textStart = response.data.indexOf("text='")
            console.log('textStart:', textStart)
            
            if (textStart !== -1) {
              // 找到text='后的所有内容，直到遇到下一个以', 开头的部分
              let textContent = response.data.substring(textStart + 6)
              // 找到第一个以', 开头的位置
              const textEnd = textContent.indexOf("', ")
              console.log('textEnd:', textEnd)
              
              if (textEnd !== -1) {
                textContent = textContent.substring(0, textEnd)
              }
              extractedText = textContent
              console.log('使用备选方案提取的文本:', extractedText)
            } else {
              // 方案4：直接显示整个响应数据
              extractedText = response.data
              console.log('使用整个响应数据:', extractedText)
            }
          }
        }
      } else if (Array.isArray(response.data) && response.data.length > 0) {
        // 处理数组类型的返回数据
        const firstItem = response.data[0]
        if (firstItem && firstItem.content && firstItem.content.length > 0) {
          const contentItem = firstItem.content[0]
          if (contentItem && contentItem.text) {
            extractedText = contentItem.text
          } else {
            extractedText = JSON.stringify(response.data)
          }
        } else {
          extractedText = JSON.stringify(response.data)
        }
      } else if (response.data && response.data.content && response.data.content.length > 0) {
        // 处理对象类型的返回数据
        extractedText = response.data.content[0].text
      } else {
        extractedText = JSON.stringify(response.data)
      }
      
      // 存储完整文本
      outputText.value = extractedText
      console.log('最终提取的文本:', extractedText)
    } else {
      console.error('翻译失败:', response)
      ElMessage.error('翻译失败，请稍后重试')
    }
  } catch (error) {
    console.error('翻译失败:', error)
    ElMessage.error('翻译失败，请稍后重试')
  } finally {
    loading.value = false
  }
}



// 复制翻译结果
const copyResult = () => {
  if (!outputText.value) {
    ElMessage.warning('没有可复制的内容')
    return
  }
  
  // 创建临时文本区域
  const textArea = document.createElement('textarea')
  textArea.value = outputText.value
  document.body.appendChild(textArea)
  textArea.select()
  
  try {
    // 执行复制命令
    document.execCommand('copy')
    ElMessage.success('复制成功')
  } catch (err) {
    ElMessage.error('复制失败，请手动复制')
  } finally {
    // 清理临时元素
    document.body.removeChild(textArea)
  }
}

// 组件挂载时初始化
onMounted(() => {
  initTheme()
})
</script>

<style scoped>
.ai-translate-container {
  width: 100%;
  min-height: 100vh;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  transition: all 0.3s ease;
  padding: 1rem;
  box-sizing: border-box;
}

/* 头部导航 */
.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  margin-bottom: 2rem;
  border-bottom: 1px solid var(--border-color);
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
  background-color: var(--bg-secondary);
}

.header-title {
  font-size: 1.2rem;
  font-weight: bold;
  color: var(--text-primary);
}

.header-right {
  width: 40px;
}

/* 翻译内容 */
.translate-content {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

/* 输入区域 */
.input-section {
  background-color: var(--bg-secondary);
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-title {
  font-size: 1rem;
  font-weight: 500;
  color: var(--text-primary);
  margin: 0;
}

.copy-button {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  color: var(--primary-color);
  font-size: 0.85rem;
  padding: 0.3rem 0.6rem;
  border-radius: 4px;
  transition: all 0.3s ease;
  background-color: var(--bg-tertiary);
  border: 1px solid var(--border-color);
}

.copy-button:hover {
  background-color: var(--bg-secondary);
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.translate-input {
  width: 100%;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background-color: var(--bg-primary);
  color: var(--text-primary);
  padding: 1rem;
  font-size: 1rem;
  resize: none;
  transition: all 0.3s ease;
}

/* 深色模式 */
/* 确保在黑夜模式下输入框显示正确的背景色 */
:deep(.el-textarea__inner) {
  background-color: var(--bg-primary) !important;
  color: var(--text-primary) !important;
  border: 1px solid var(--border-color) !important;
}

/* 确保在黑夜模式下复制按钮显示正确 */
.dark-mode .copy-button {
  background-color: var(--bg-tertiary);
  border-color: var(--border-color);
  color: var(--primary-color);
}

.dark-mode .copy-button:hover {
  background-color: var(--bg-secondary);
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.translate-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(255, 152, 0, 0.1);
}

/* 操作按钮 */
.action-section {
  display: flex;
  justify-content: center;
}

.translate-button {
  padding: 0.8rem 2rem;
  font-size: 1rem;
  font-weight: 500;
  border-radius: 8px;
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
  transition: all 0.3s ease;
}

.translate-button:hover {
  background-color: var(--primary-hover);
  border-color: var(--primary-hover);
}

/* 输出区域 */
.output-section {
  background-color: var(--bg-secondary);
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  min-height: 200px;
}

.placeholder-section {
  min-height: 300px;
  display: flex;
  flex-direction: column;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--border-color);
}

.header-actions {
  display: flex;
  gap: 0.5rem;
}

.copy-button {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  color: var(--primary-color);
  font-size: 0.85rem;
  padding: 0.3rem 0.6rem;
  border-radius: 4px;
  transition: all 0.3s ease;
  background-color: var(--bg-tertiary);
  border: 1px solid var(--border-color);
}

.copy-button:hover {
  background-color: var(--bg-secondary);
  color: var(--primary-color);
  border-color: var(--primary-color);
}

/* 结构化输出 */
.structured-output {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

/* 题目部分 */
.question-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.question-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--border-color);
}

.question-actions {
  display: flex;
  gap: 0.5rem;
}

.add-wrong-button {
  font-size: 0.8rem;
  padding: 0.2rem 0.6rem;
  border-radius: 4px;
}

.view-wrong-button {
  font-size: 0.8rem;
  padding: 0.2rem 0.6rem;
  border-radius: 4px;
  color: var(--text-primary);
}

.question-content {
  font-size: 1rem;
  line-height: 1.6;
  color: var(--text-primary);
  white-space: pre-wrap;
  word-break: break-word;
  padding: 1rem;
  background-color: var(--bg-primary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
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

/* 解析部分 */
.analysis-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.analysis-content {
  font-size: 1rem;
  line-height: 1.6;
  color: var(--text-primary);
  white-space: pre-wrap;
  word-break: break-word;
  padding: 1rem;
  background-color: var(--bg-primary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

/* 占位符 */
.translate-output {
  font-size: 1rem;
  line-height: 1.6;
  color: var(--text-primary);
  white-space: pre-wrap;
  word-break: break-word;
  padding: 1rem;
  background-color: var(--bg-primary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  min-height: 100px;
}

.translate-output.placeholder {
  color: var(--text-tertiary);
  font-style: italic;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 200px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ai-translate-container {
    padding: 0.8rem;
  }
  
  .translate-content {
    gap: 1.5rem;
  }
  
  .input-section,
  .output-section {
    padding: 1.2rem;
  }
  
  .translate-input {
    padding: 0.8rem;
    font-size: 0.9rem;
  }
  
  .translate-button {
    padding: 0.7rem 1.5rem;
    font-size: 0.9rem;
  }
}

@media (max-width: 480px) {
  .ai-translate-container {
    padding: 0.5rem;
  }
  
  .translate-content {
    gap: 1.2rem;
  }
  
  .input-section,
  .output-section {
    padding: 1rem;
  }
  
  .translate-input {
    padding: 0.7rem;
    font-size: 0.85rem;
  }
  
  .translate-button {
    padding: 0.6rem 1.2rem;
    font-size: 0.85rem;
  }
  
  .section-title {
    font-size: 0.9rem;
  }
  
  .translate-output {
    font-size: 0.9rem;
  }
}
</style>