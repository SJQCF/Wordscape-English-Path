<template>
  <div class="ai-result-container">
    <!-- 顶部导航 -->
    <div class="header">
      <el-button type="text" @click="goBack" class="back-button">
        <el-icon size="24"><ArrowLeft /></el-icon>
      </el-button>
      <h1 class="page-title">识别结果</h1>
      <div class="header-placeholder"></div>
    </div>
    
    <!-- 识别结果内容 -->
    <div class="result-content">
      
      <!-- 识别内容 -->
      <div class="content-container">
        <h2>识别内容</h2>
        <div class="content-box">
          <!-- 物体识别结果 -->
          <div v-if="parsedResult.length > 0" class="objects-list">
            <div v-for="(item, index) in parsedResult" :key="index" class="object-item">
              <div class="object-info">
                <span class="object-chinese">{{ item.中文 }}</span>
                <span class="object-english">{{ item.英文 }}</span>
              </div>
              <el-button type="primary" circle size="large" class="search-button" @click="goToWordSearch(item.英文)">
                <el-icon size="24"><Right /></el-icon>
              </el-button>
            </div>
          </div>
          
          <!-- 题目识别结果 -->
          <div v-else-if="questionContent" class="question-result">
            <!-- 题目部分 -->
            <div class="question-section">
              <div class="section-title question-title">
                题目
              </div>
              <div class="question-content">
                {{ questionContent }}
              </div>
            </div>
            
            <!-- 答案部分 -->
            <div class="answer-section" v-if="answer">
              <div class="section-title">答案</div>
              <div class="answer-content">
                {{ answer }}
              </div>
            </div>
            
            <!-- 解析部分 -->
            <div class="analysis-section" v-if="analysis">
              <div class="section-title">解析</div>
              <div class="analysis-content">
                {{ analysis }}
              </div>
            </div>
            
            <!-- 类似练习题按钮 -->
            <div v-if="similarQuestions && similarQuestionsList.length > 0" class="similar-questions-button">
              <el-button 
                type="primary" 
                round 
                @click="goToSimilarQuestions"
                class="similar-questions-btn"
              >
                查看类似练习题
              </el-button>
            </div>
          </div>
          
          <!-- 原始结果 -->
          <p v-else-if="result" class="result-text">{{ result }}</p>
          <p v-else class="loading-text">加载中...</p>
        </div>
      </div>
      

      
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" round @click="goBack">返回</el-button>
        <el-button type="info" round @click="goToAiImage">重新识别</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ArrowLeft, PictureFilled, Right, Plus } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import { ElMessage } from 'element-plus'

// 初始化主题
const { initTheme } = useTheme()

// 获取路由实例
const router = useRouter()
// 获取当前路由
const route = useRoute()

// 识别结果
const image = ref('')
const result = ref('')
const parsedResult = ref([])

// 题目识别结果
const questionContent = ref('')
const answer = ref('')
const analysis = ref('')
const similarQuestions = ref('')
const showSimilarQuestions = ref(false)

// 类似练习题相关
const similarQuestionsList = ref([])
const currentQuestionIndex = ref(0)
const userAnswer = ref('')
const showCorrectAnswer = ref(false)

// 处理返回按钮
const goBack = () => {
  router.back()
}

// 重新识别
const goToAiImage = () => {
  router.push('/aiImage')
}

// 跳转到场景对话
const goToSceneDialog = () => {
  router.push('/sceneDialog')
}

// 跳转到单词查询页面
const goToWordSearch = (word) => {
  router.push({
    path: '/search',
    query: {
      q: word
    }
  })
}

// 跳转到类似练习题页面
const goToSimilarQuestions = () => {
  router.push({
    path: '/similarQuestions',
    query: {
      questions: JSON.stringify(similarQuestionsList.value)
    }
  })
}



// 解析JSON结果
const parseResult = (resultData) => {
  try {
    console.log('原始结果数据:', resultData);
    
    // 处理不同类型的输入
    let resultStr = resultData;
    if (typeof resultStr !== 'string') {
      resultStr = JSON.stringify(resultStr);
    }
    
    console.log('转换后的字符串:', resultStr);
    
    // 尝试1: 直接解析整个字符串
    try {
      const parsed = JSON.parse(resultStr);
      console.log('直接解析结果:', parsed);
      if (parsed.objects && Array.isArray(parsed.objects)) {
        console.log('直接解析成功，返回objects数组');
        return parsed.objects;
      }
    } catch (e) {
      console.log('直接解析失败，尝试其他方法');
    }
    
    // 尝试2: 寻找包含"objects"的部分
    const objectsIndex = resultStr.indexOf('"objects"');
    if (objectsIndex !== -1) {
      // 向前寻找开始的{
      let startIndex = objectsIndex;
      let braceCount = 0;
      let foundStart = false;
      
      for (let i = objectsIndex; i >= 0; i--) {
        if (resultStr[i] === '}') {
          braceCount++;
        } else if (resultStr[i] === '{') {
          if (braceCount === 0) {
            startIndex = i;
            foundStart = true;
            break;
          } else {
            braceCount--;
          }
        }
      }
      
      if (foundStart) {
        // 向后寻找结束的}
        let endIndex = objectsIndex;
        braceCount = 0;
        let foundEnd = false;
        
        for (let i = objectsIndex; i < resultStr.length; i++) {
          if (resultStr[i] === '{') {
            braceCount++;
          } else if (resultStr[i] === '}') {
            braceCount--;
            if (braceCount === -1) {
              endIndex = i + 1;
              foundEnd = true;
              break;
            }
          }
        }
        
        if (foundEnd) {
          const jsonStr = resultStr.substring(startIndex, endIndex);
          console.log('提取的JSON字符串:', jsonStr);
          try {
            const parsed = JSON.parse(jsonStr);
            console.log('解析后的对象:', parsed);
            if (parsed.objects && Array.isArray(parsed.objects)) {
              console.log('提取解析成功，返回objects数组');
              return parsed.objects;
            }
          } catch (parseError) {
            console.error('解析提取的JSON失败:', parseError);
          }
        }
      }
    }
    
    // 尝试3: 使用正则表达式
    try {
      // 匹配包含objects的JSON结构
      const regex = /\{[\s\S]*?"objects"\s*:\s*\[[\s\S]*?\][\s\S]*?\}/;
      const match = resultStr.match(regex);
      if (match) {
        const jsonStr = match[0];
        console.log('正则提取的JSON字符串:', jsonStr);
        const parsed = JSON.parse(jsonStr);
        if (parsed.objects && Array.isArray(parsed.objects)) {
          console.log('正则解析成功，返回objects数组');
          return parsed.objects;
        }
      }
    } catch (regexError) {
      console.error('正则提取失败:', regexError);
    }
    
    // 尝试4: 手动提取objects数组
    try {
      // 寻找"objects": [的位置
      const objectsStart = resultStr.indexOf('"objects": [');
      if (objectsStart !== -1) {
        // 寻找对应的结束]
        let endIndex = objectsStart;
        let bracketCount = 1;
        
        for (let i = objectsStart + 10; i < resultStr.length; i++) {
          if (resultStr[i] === '[') {
            bracketCount++;
          } else if (resultStr[i] === ']') {
            bracketCount--;
            if (bracketCount === 0) {
              endIndex = i + 1;
              break;
            }
          }
        }
        
        if (endIndex > objectsStart) {
          const objectsArrayStr = resultStr.substring(objectsStart + 10, endIndex - 1);
          console.log('手动提取的objects数组字符串:', objectsArrayStr);
          try {
            const objectsArray = JSON.parse('[' + objectsArrayStr + ']');
            if (Array.isArray(objectsArray)) {
              console.log('手动提取解析成功，返回objects数组');
              return objectsArray;
            }
          } catch (e) {
            console.error('手动提取解析失败:', e);
          }
        }
      }
    } catch (manualError) {
      console.error('手动提取失败:', manualError);
    }
    
    // 尝试5: 直接搜索中文和英文键值对
    try {
      const objects = [];
      // 匹配中文和英文键值对
      const regex = /"中文"\s*:\s*"([^"]+)"[\s\S]*?"英文"\s*:\s*"([^"]+)"/g;
      let match;
      while ((match = regex.exec(resultStr)) !== null) {
        objects.push({
          "中文": match[1],
          "英文": match[2]
        });
      }
      if (objects.length > 0) {
        console.log('直接匹配键值对成功，返回objects数组');
        return objects;
      }
    } catch (e) {
      console.error('直接匹配键值对失败:', e);
    }
    
    console.log('所有解析方法都失败，返回空数组');
    return [];
  } catch (error) {
    console.error('解析结果失败:', error);
    return [];
  }
}

// 解析类似练习题为单独的题目数组
const parseSimilarQuestions = (similarQuestionsText) => {
  try {
    console.log('开始解析类似练习题:', similarQuestionsText);
    
    const questions = [];
    const lines = similarQuestionsText.split('\n');
    let currentQuestion = null;
    let currentLines = [];
    
    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim();
      
      // 检查是否是新题目的开始
      if (line.match(/^\d+\.\s*The word/)) {
        // 如果有当前题目，先保存
        if (currentQuestion) {
          questions.push(currentQuestion);
        }
        
        // 开始新题目
        currentQuestion = {
          question: line,
          options: [],
          answer: '',
          analysis: ''
        };
        currentLines = [line];
      } 
      // 检查是否是选项行
      else if (currentQuestion && line.match(/^[A-D]\.\s*/)) {
        currentQuestion.options.push(line);
        currentLines.push(line);
      }
      // 检查是否是答案行
      else if (currentQuestion && line.match(/^答案：/)) {
        currentQuestion.answer = line.replace('答案：', '').trim();
        currentLines.push(line);
      }
      // 检查是否是解析行
      else if (currentQuestion && line.match(/^解析：/)) {
        currentQuestion.analysis = line.replace('解析：', '').trim();
        currentLines.push(line);
      }
      // 其他行，添加到当前题目
      else if (currentQuestion && line) {
        currentLines.push(line);
      }
    }
    
    // 保存最后一个题目
    if (currentQuestion) {
      questions.push(currentQuestion);
    }
    
    similarQuestionsList.value = questions;
    console.log('解析完成的类似练习题:', similarQuestionsList.value);
  } catch (error) {
    console.error('解析类似练习题失败:', error);
  }
}

// 解析题目结果
const parseQuestionResult = (resultData) => {
  try {
    console.log('开始解析题目结果:', resultData);
    
    // 处理不同类型的输入
    let resultStr = resultData;
    if (typeof resultStr !== 'string') {
      resultStr = JSON.stringify(resultStr);
    }
    
    console.log('转换后的题目字符串:', resultStr);
    
    // 尝试提取text字段内容
    let extractedText = resultStr;
    
    // 方案1：使用正则表达式提取text字段
    const textMatch = resultStr.match(/text='([\s\S]+?)'\s*,\s*annotations/);
    if (textMatch && textMatch[1]) {
      extractedText = textMatch[1];
      console.log('使用正则提取的text:', extractedText);
    } else {
      // 方案2：直接查找text=和annotations之间的内容
      const startIndex = resultStr.indexOf("text='");
      const endIndex = resultStr.indexOf("', annotations");
      if (startIndex !== -1 && endIndex !== -1) {
        extractedText = resultStr.substring(startIndex + 6, endIndex);
        console.log('使用字符串索引提取的text:', extractedText);
      } else {
        // 方案3：尝试查找text=后的内容
        const textStart = resultStr.indexOf("text='");
        if (textStart !== -1) {
          let textContent = resultStr.substring(textStart + 6);
          const textEnd = textContent.indexOf("', ");
          if (textEnd !== -1) {
            textContent = textContent.substring(0, textEnd);
          }
          extractedText = textContent;
          console.log('使用备选方案提取的text:', extractedText);
        }
      }
    }
    
    // 过滤掉文本中的图片数据
    extractedText = extractedText.replace(/data:image\/[a-zA-Z0-9+\/=]+/g, '[图片]');
    console.log('过滤后的文本:', extractedText);
    
    // 重置结构化数据
    questionContent.value = ''
    answer.value = ''
    analysis.value = ''
    similarQuestions.value = ''
    similarQuestionsList.value = []
    currentQuestionIndex.value = 0
    userAnswer.value = ''
    showCorrectAnswer.value = false
    
    // 按换行符分割文本
    const lines = extractedText.split('\n')
    console.log('分割后的行:', lines)
    
    // 遍历行
    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim()
      console.log('当前行:', line)
      
      // 检查是否是原题目部分
      if (line.includes('【原题目】')) {
        // 收集题目内容，直到遇到解析
        let questionLines = []
        for (let j = i; j < lines.length; j++) {
          const qLine = lines[j].trim()
          if (qLine.includes('【解析】')) {
            break
          }
          questionLines.push(lines[j])
        }
        questionContent.value = questionLines.join('\n')
        console.log('题目内容:', questionContent.value)
      } 
      // 检查是否是答案部分
      else if (line.includes('【答案】')) {
        // 提取答案内容
        answer.value = line.replace('【答案】', '').trim()
        console.log('答案内容:', answer.value)
      } 
      // 检查是否是解析部分
      else if (line.includes('【解析】')) {
        // 收集解析内容，直到遇到答案或类似练习题
        let analysisLines = []
        for (let j = i; j < lines.length; j++) {
          const aLine = lines[j].trim()
          if (aLine.includes('【答案】') || aLine.includes('【类似练习题】')) {
            break
          }
          analysisLines.push(lines[j])
        }
        analysis.value = analysisLines.join('\n')
        console.log('解析内容:', analysis.value)
      }
      // 检查是否是类似练习题部分
      else if (line.includes('【类似练习题】')) {
        // 收集类似练习题内容，直到文本结束
        let similarLines = []
        for (let j = i; j < lines.length; j++) {
          // 过滤掉包含base64图片数据的行
          if (!lines[j].includes('data:image/')) {
            similarLines.push(lines[j])
          }
        }
        similarQuestions.value = similarLines.join('\n')
        console.log('类似练习题内容:', similarQuestions.value)
        
        // 解析类似练习题为单独的题目数组
        parseSimilarQuestions(similarQuestions.value)
      }

    }
    
    // 如果没有结构化数据，将整个文本作为题目内容
    if (!questionContent.value) {
      questionContent.value = extractedText
      console.log('没有结构化数据，使用整个文本作为题目内容')
    }
    
    console.log('解析完成，题目结构化数据:', {
      questionContent: questionContent.value,
      answer: answer.value,
      analysis: analysis.value,
      similarQuestions: similarQuestions.value,
      similarQuestionsList: similarQuestionsList.value
    })
  } catch (error) {
    console.error('解析题目结果失败:', error);
    // 如果解析失败，将整个结果作为题目内容
    questionContent.value = typeof resultData === 'string' ? resultData : JSON.stringify(resultData);
  }
}

// 组件挂载时获取数据
onMounted(() => {
  // 初始化主题
  initTheme()
  
  // 从路由参数中获取结果数据
  if (route.query.result) {
    try {
      // 尝试解析JSON字符串
      const parsed = JSON.parse(route.query.result)
      result.value = parsed
      console.log('解析后的result:', result.value)
    } catch (error) {
      // 如果解析失败，使用原始字符串
      result.value = route.query.result
      console.log('使用原始字符串:', result.value)
    }
    
    // 解析物体识别结果
    parsedResult.value = parseResult(result.value)
    console.log('解析后的parsedResult:', parsedResult.value)
    
    // 只有当不是物体识别结果时，才尝试解析题目结果
    if (parsedResult.value.length === 0) {
      parseQuestionResult(result.value)
    }
  } else {
    // 没有result参数时的处理
    ElMessage({
      message: '没有识别结果，请重新上传图片',
      type: 'warning',
      duration: 3000
    })
  }
})
</script>

<style scoped>
/* 主容器 */
.ai-result-container {
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

/* 识别结果内容 */
.result-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 1rem;
  overflow-y: auto;
  min-height: 0; /* 确保flex子元素可以正确收缩 */
}



/* 识别内容 */
.content-container {
  margin-bottom: 1.5rem;
}

.content-container h2 {
  font-size: 1.1rem;
  font-weight: bold;
  color: var(--text-primary);
  margin: 0 0 1rem;
  transition: all 0.3s ease;
}

.content-box {
  padding: 1.5rem;
  background-color: var(--bg-secondary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.result-text {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.6;
  font-size: 0.95rem;
  transition: all 0.3s ease;
}

/* 题目识别结果 */
.question-result {
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

/* 类似练习题部分 */
.similar-questions-button {
  margin-top: 1rem;
  text-align: center;
}

.similar-questions-btn {
  width: 100%;
  max-width: 200px;
}

/* 物体列表 */
.objects-list {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.object-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.8rem;
  background-color: var(--bg-primary);
  border-radius: 8px;
  border-left: 4px solid var(--primary-color);
  transition: all 0.3s ease;
}

.object-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.search-button {
  margin-left: 0.8rem;
  width: 40px;
  height: 40px;
}

.object-chinese {
  font-size: 1rem;
  font-weight: 500;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.object-english {
  font-size: 0.9rem;
  color: var(--text-secondary);
  font-style: italic;
  transition: all 0.3s ease;
}

.loading-text {
  margin: 0;
  color: var(--text-tertiary);
  text-align: center;
  padding: 2rem 0;
  transition: all 0.3s ease;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  padding: 1.5rem 0;
  align-items: center;
  flex-shrink: 0; /* 确保按钮区域不会被压缩 */
}



/* 响应式设计 */
@media (max-width: 480px) {
  .result-content {
    padding: 0.8rem;
  }
  
  .result-image {
    max-height: 300px;
    object-fit: contain;
    background-color: #f5f7fa;
  }
  
  .content-box {
    padding: 1.2rem;
  }
  
  .result-text {
    font-size: 0.9rem;
    line-height: 1.5;
  }
  
  .action-buttons {
    gap: 0.8rem;
  }
  
  .action-buttons .el-button {
    width: 100px;
  }
}
</style>