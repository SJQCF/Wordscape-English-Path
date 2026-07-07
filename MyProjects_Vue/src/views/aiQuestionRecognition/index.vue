<template>
  <div class="ai-image-container">
    <!-- 顶部固定区域 -->
    <div class="top-sticky-section">
      <!-- 头部导航 -->
      <div class="course-header">
        <el-button type="text" @click="goBack" class="back-button">
          <el-icon size="24"><ArrowLeft /></el-icon>
        </el-button>
        <h1 class="page-title">AI识别题目</h1>
        <div class="header-placeholder"></div>
      </div>
    </div>
    
    <!-- 选择方式 -->
    <div class="select-options">
      <div class="option-card" @click="startCamera">
        <div class="option-icon">
          <span class="emoji-icon">📷</span>
        </div>
        <h3>拍摄照片</h3>
        <p>使用相机拍摄题目进行识别</p>
      </div>
      
      <div class="option-card" @click="selectFromGallery">
        <div class="option-icon">
          <span class="emoji-icon">🖼️</span>
        </div>
        <h3>从相册选择</h3>
        <p>从手机相册中选择图片进行识别</p>
      </div>
    </div>
    
    <!-- 隐藏的文件上传input，用于调用相机 -->
    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      capture="environment"
      class="hidden-file-input"
      @change="handleImageUpload"
    />
    
    <!-- 隐藏的文件上传input，用于从相册选择 -->
    <input
      ref="galleryInput"
      type="file"
      accept="image/*"
      class="hidden-file-input"
      @change="handleImageUpload"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ArrowLeft, Delete } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getAnswerWithQuestion } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { useTheme } from '@/composables/useTheme'

const { initTheme } = useTheme()

// 路由实例
const router = useRouter()

// 状态管理
const fileInput = ref(null)
const galleryInput = ref(null)
const selectedImage = ref('')
const recognitionResult = ref('')
const loading = ref(false)

// 返回上一页
const goBack = () => {
  router.back()
}

// 开始相机拍摄
const startCamera = () => {
  // 触发文件选择，调用相机
  fileInput.value.click()
}

// 从相册选择
const selectFromGallery = () => {
  // 触发文件选择，从相册选择
  galleryInput.value.click()
}

// 处理图片上传
const handleImageUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    // 预览图片
    const reader = new FileReader()
    reader.onload = (e) => {
      selectedImage.value = e.target.result
    }
    reader.readAsDataURL(file)
    
    // 调用 AI 识别 API
    recognizeQuestion(file)
    
    // 重置input，以便下次选择
    event.target.value = ''
  }
}

// 识别题目
const recognizeQuestion = async (file) => {
  loading.value = true
  // 显示加载提示
  const loadingInstance = ElMessage({
    message: '正在识别...',
    type: 'info',
    duration: 0, // 不自动关闭
    showClose: false
  })
  
  try {
    console.log('开始发送请求...')
    const response = await getAnswerWithQuestion(file)
    console.log('请求成功，响应:', response)
    if (response.code === 1 && response.data) {
      recognitionResult.value = response.data
      // 关闭加载提示
      loadingInstance.close()
      ElMessage({
        message: '识别成功',
        type: 'success',
        duration: 1000
      })
      // 过滤结果数据中的图片数据
      let filteredResult = recognitionResult.value;
      if (typeof filteredResult === 'string') {
        // 过滤掉字符串中的图片数据
        filteredResult = filteredResult.replace(/data:image\/[a-zA-Z0-9+\/=]+/g, '[图片]');
      } else if (typeof filteredResult === 'object') {
        // 对于对象，需要递归过滤
        filteredResult = JSON.parse(JSON.stringify(filteredResult).replace(/data:image\/[a-zA-Z0-9+\/=]+/g, '[图片]'));
      }
      
      // 存储图片数据到sessionStorage
      sessionStorage.setItem('aiResultImage', selectedImage.value);
      // 跳转到结果页面，只传递过滤后的结果数据
      setTimeout(() => {
        router.push({
          path: '/aiResult',
          query: {
            result: JSON.stringify(filteredResult)
          }
        })
      }, 1000)
    } else {
      // 关闭加载提示
      loadingInstance.close()
      ElMessage({
        message: '识别失败，请重试',
        type: 'error',
        duration: 2000
      })
    }
  } catch (error) {
    // 关闭加载提示
    loadingInstance.close()
    console.error('识别失败:', error)
    console.error('错误详情:', error.response)
    ElMessage({
      message: '识别失败，请检查网络连接',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

// 组件挂载时初始化
onMounted(() => {
  initTheme()
})
</script>

<style scoped>
/* 主容器 */
.ai-image-container {
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

/* 选择方式容器 */
.select-options {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 2rem;
  padding: 1rem;
  margin-top: 80px;
}

/* 选项卡片 */
.option-card {
  width: 100%;
  max-width: 300px;
  padding: 2rem;
  background-color: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.option-card:active {
  transform: translateY(2px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

/* 选项图标 */
.option-icon {
  margin-bottom: 1rem;
  transition: all 0.3s ease;
}

.emoji-icon {
  font-size: 2.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f0f7ff 0%, #e6f0ff 100%);
  margin: 0 auto 1rem;
}

.option-card h3 {
  margin: 0 0 0.3rem;
  color: #333333;
  font-size: 1.1rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

.option-card p {
  margin: 0;
  color: #666666;
  font-size: 0.9rem;
  line-height: 1.4;
  transition: all 0.3s ease;
}

/* 隐藏的文件输入 */
.hidden-file-input {
  display: none;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .select-options {
    padding: 1rem;
    gap: 1.5rem;
    margin-top: 80px;
  }
  
  .option-card {
    padding: 1.5rem;
  }
  
  /* 调整头部导航 */
  .top-sticky-section {
    padding: env(safe-area-inset-top) 0.8rem 0.8rem;
  }
  
  .course-header {
    padding: 0.5rem 0;
  }
  
  .page-title {
    font-size: 1rem;
  }
  
  .back-button {
    width: 40px;
    height: 40px;
  }
  
  .header-placeholder {
    width: 40px;
  }
}

</style>