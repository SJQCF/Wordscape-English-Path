<template>
  <div class="ai-container">
    <div class="ai-header">
      <h2>AI功能</h2>
    </div>
    <div class="ai-content">
      <div class="ai-items">
        <!-- AI识图 -->
        <div class="ai-item" @click="startAIImage">
          <div class="item-icon">
            <span class="emoji-icon">📷</span>
          </div>
          <div class="item-info">
            <h3>AI识图</h3>
            <p>上传图片，AI帮你识别内容</p>
          </div>
        </div>
        
        <!-- AI写作 -->
        <div class="ai-item" @click="startAIWriting">
          <div class="item-icon">
            <span class="emoji-icon">✍️</span>
          </div>
          <div class="item-info">
            <h3>AI写作</h3>
            <p>生成英文文本，提升写作效率</p>
          </div>
        </div>
        
        <!-- AI翻译 -->
        <div class="ai-item" @click="startAITranslate">
          <div class="item-icon">
            <span class="emoji-icon">🌐</span>
          </div>
          <div class="item-info">
            <h3>AI翻译</h3>
            <p>翻译文本，中英互译</p>
          </div>
        </div>
        
        <!-- AI识别题目 -->
        <div class="ai-item" @click="startAIQuestionRecognition">
          <div class="item-icon">
            <span class="emoji-icon">📚</span>
          </div>
          <div class="item-info">
            <h3>AI识别题目</h3>
            <p>上传题目图片，AI帮你解答</p>
          </div>
        </div>
        
        <!-- 场景对话 -->
        <div class="ai-item" @click="startSceneDialog">
          <div class="item-icon">
            <span class="emoji-icon">💬</span>
          </div>
          <div class="item-info">
            <h3>场景对话</h3>
            <p>在3D场景中练习英语口语</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 场景选择对话框 -->
    <div v-if="dialogVisible" class="scene-dialog-overlay" @click="cancelScene">
      <div class="scene-dialog-content" @click.stop>
        <div class="scene-dialog-header">
          <h3 class="scene-dialog-title">选择练习场景</h3>
          <button class="scene-dialog-close" @click="cancelScene">×</button>
        </div>
        <div class="scene-dialog-body">
          <div class="scene-selector">
            <div 
              v-for="scene in scenes" 
              :key="scene.value"
              :class="['scene-card', scene.value + '-scene', { 'scene-card-selected': selectedScene === scene.value }]"
              @click="selectedScene = scene.value"
            >
              <div class="scene-icon">
                <span class="icon-emoji">{{ scene.icon }}</span>
              </div>
              <div class="scene-info">
                <h4 class="scene-title">{{ scene.label }}</h4>
                <p class="scene-description">{{ scene.description }}</p>
              </div>
              <div class="scene-radio">
                <div :class="['radio-button', { 'radio-button-selected': selectedScene === scene.value }]">
                  <div v-if="selectedScene === scene.value" class="radio-dot"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="scene-dialog-footer">
          <button class="footer-btn cancel-btn" @click="cancelScene">取消</button>
          <button class="footer-btn confirm-btn" @click="confirmScene">开始练习</button>
        </div>
      </div>
    </div>
    
    <!-- 自定义场景配置对话框 -->
    <div v-if="customSceneDialogVisible" class="scene-dialog-overlay" @click="cancelCustomScene">
      <div class="scene-dialog-content" @click.stop>
        <div class="scene-dialog-header">
          <h3 class="scene-dialog-title">配置自定义场景</h3>
          <button class="scene-dialog-close" @click="cancelCustomScene">×</button>
        </div>
        <div class="scene-dialog-body">
          <div class="custom-scene-form">
            <div class="form-item">
              <label class="form-label">场景名称</label>
              <input 
                type="text" 
                v-model="customSceneConfig.sceneName" 
                class="form-input"
                placeholder="请输入场景名称，例如：咖啡店"
              >
            </div>
            <div class="form-item">
              <label class="form-label">场景描述</label>
              <textarea 
                v-model="customSceneConfig.sceneDescription" 
                class="form-textarea"
                placeholder="请输入场景描述，例如：你是一名咖啡店服务员，负责接待客人，推荐饮品，处理订单。"
                rows="3"
              ></textarea>
            </div>

          </div>
        </div>
        <div class="scene-dialog-footer">
          <button class="footer-btn cancel-btn" @click="cancelCustomScene">取消</button>
          <button class="footer-btn confirm-btn" @click="confirmCustomScene">开始练习</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import { ElMessage } from 'element-plus'
import { startVoiceChat } from '@/api/ai'

// 获取路由实例
const router = useRouter()

// 开始AI识图
const startAIImage = () => {
  // 跳转到AI识图页面
  router.push('/aiImage')
}



// 开始AI写作
const startAIWriting = () => {
  // 跳转到AI写作页面
  router.push('/aiWriting')
}

// 初始化主题
const { initTheme } = useTheme()



// 开始AI翻译
const startAITranslate = () => {
  // 跳转到AI翻译页面
  router.push('/aiTranslate')
}

// 开始AI识别题目
const startAIQuestionRecognition = () => {
  // 跳转到AI识别题目页面
  router.push('/aiQuestionRecognition')
}

// 场景选择对话框
const dialogVisible = ref(false)
const selectedScene = ref('hotel') // 默认选择酒店场景

// 自定义场景配置对话框
const customSceneDialogVisible = ref(false)
const customSceneConfig = ref({
  sceneName: '',
  sceneDescription: ''
})

// 音色选项
const speakerOptions = [
  {
    value: 'zh_male_yunzhou_jupiter_bigtts',
    label: '男声-云舟',
    description: '默认推荐 ⭐'
  },
  {
    value: 'zh_female_shuangkuai_jupiter_bigtts',
    label: '女声-爽快',
    description: '活泼对话'
  },
  {
    value: 'zh_male_wennuan_jupiter_bigtts',
    label: '男声-温暖',
    description: '温馨场景'
  },
  {
    value: 'zh_female_tianmei_jupiter_bigtts',
    label: '女声-甜美',
    description: '友好交流'
  },
  {
    value: 'zh_male_chengshu_jupiter_bigtts',
    label: '男声-成熟',
    description: '专业场景'
  },
  {
    value: 'zh_female_qingxin_jupiter_bigtts',
    label: '女声-清新',
    description: '轻松对话'
  }
]

// 场景数据
const scenes = [
  {
    value: 'hotel',
    label: '酒店场景',
    description: '练习酒店入住、退房等对话',
    icon: '🏨'
  },
  {
    value: 'airport',
    label: '机场场景',
    description: '练习机场值机、登机等对话',
    icon: '✈️'
  },
  {
    value: 'custom',
    label: '自定义场景',
    description: '根据您的需求创建对话场景',
    icon: '🎨'
  }
]

// 开始场景对话
const startSceneDialog = () => {
  // 显示场景选择对话框
  dialogVisible.value = true
}

// 确认选择场景
const confirmScene = async () => {
  if (selectedScene.value === 'custom') {
    // 自定义场景 - 打开配置对话框
    dialogVisible.value = false
    customSceneDialogVisible.value = true
  } else {
    // 其他场景 - 直接启动会话
    await startSceneSession(selectedScene.value)
  }
}

// 启动场景会话
const startSceneSession = async (sceneType) => {
  try {
    // 准备场景配置数据
    const sceneConfig = {
      mode: 'audio',
      botName: sceneType === 'hotel' ? '酒店前台' : '机场工作人员',
      systemRole: sceneType === 'hotel' ? '你是一名专业的酒店前台工作人员，负责接待客人，办理入住和退房手续，回答客人的问题。' : 
                   '你是一名专业的机场工作人员，负责办理值机、登机手续，回答乘客的问题。',
      speakingStyle: '自然、友好',
      speaker: 'zh_male_yunzhou_jupiter_bigtts'
    }
    
    // 启动AI语音会话
    const response = await startVoiceChat(sceneConfig)
    const sessionId = response.data.sessionId
    
    // 根据选择的场景跳转到不同页面，并传递会话ID
    if (sceneType === 'hotel') {
      // 酒店场景 - 跳转到3D场景页面
      router.push({
        path: '/sceneDialog',
        query: { sessionId }
      })
    } else {
      // 机场场景 - 跳转到AI口语练习页面
      router.push({
        path: '/aiSpeaking',
        query: { 
          scene: sceneType,
          sessionId 
        }
      })
    }
  } catch (error) {
    console.error('启动语音会话失败:', error)
    ElMessage.error('启动语音会话失败，请重试')
  } finally {
    dialogVisible.value = false
  }
}

// 确认自定义场景配置
const confirmCustomScene = async () => {
  if (!customSceneConfig.value.sceneName) {
    ElMessage.warning('请输入场景名称')
    return
  }
  
  try {
    // 准备自定义场景配置数据
    const sceneConfig = {
      mode: 'audio',
      botName: customSceneConfig.value.sceneName,
      systemRole: customSceneConfig.value.sceneDescription || `你是${customSceneConfig.value.sceneName}，能够根据用户的需求进行自然的对话。`,
      speakingStyle: '自然、友好',
      speaker: 'zh_male_yunzhou_jupiter_bigtts'
    }
    
    // 启动AI语音会话
    const response = await startVoiceChat(sceneConfig)
    const sessionId = response.data.sessionId
    
    customSceneDialogVisible.value = false
    
    // 跳转到AI口语练习页面
    router.push({
      path: '/aiSpeaking',
      query: { 
        scene: 'custom',
        sceneName: customSceneConfig.value.sceneName,
        sessionId 
      }
    })
  } catch (error) {
    console.error('启动语音会话失败:', error)
    ElMessage.error('启动语音会话失败，请重试')
  } finally {
    customSceneDialogVisible.value = false
  }
}

// 取消自定义场景配置
const cancelCustomScene = () => {
  customSceneDialogVisible.value = false
  // 重置配置
  customSceneConfig.value = {
    sceneName: '',
    sceneDescription: ''
  }
}

// 取消选择
const cancelScene = () => {
  dialogVisible.value = false
}

// 组件挂载时初始化主题
onMounted(() => {
  initTheme()
})
</script>

<style scoped>
.ai-container {
  padding: 1rem;
  min-height: 100vh;
  background: linear-gradient(180deg, #ffffff 0%, #e6f0ff 50%, #4a6fa5 100%);
  color: var(--text-primary);
  transition: all 0.3s ease;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 3rem; /* 为底部导航栏留出空间 */
  height: 100vh;
  box-sizing: border-box;
  margin: 0;
  width: 100%;
}

/* 黑夜模式样式 */
html.dark .ai-container {
  background: linear-gradient(180deg, #1a1a1a 0%, #2d2d2d 50%, #121212 100%);
  color: var(--text-primary);
}

.ai-header {
  text-align: center;
  margin-bottom: 2rem;
  margin-top: 80px;
}

.ai-header h2 {
  margin: 0;
  color: var(--primary-color);
  font-size: 1.5rem;
  font-weight: bold;
}

.ai-content {
  width: 100%;
  box-sizing: border-box;
}

.ai-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 1rem;
  margin: 0 auto;
  width: 100%;
  max-width: 600px;
  box-sizing: border-box;
}

.ai-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 16px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
  width: 100%;
  min-height: 100px;
  max-width: 100%;
  box-sizing: border-box;
  background-color: var(--bg-secondary);
  cursor: pointer;
  border: 1px solid var(--border-color);
  opacity: 0;
  transform: translateY(30px);
  animation: aiItemFadeIn 0.5s ease-out forwards;
}

.ai-item:active {
  transform: translateY(2px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.item-icon {
  margin-right: 1rem;
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-tertiary) 100%);
}

.emoji-icon {
  font-size: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-info h3 {
  margin: 0 0 0.3rem;
  color: var(--text-primary);
  font-size: 1.1rem;
  font-weight: 600;
  height: auto;
  display: block;
  text-align: left;
}

.item-info p {
  margin: 0;
  color: var(--text-secondary);
  text-align: left;
  height: auto;
  font-size: 0.9rem;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-price {
  font-size: 1.2rem;
  font-weight: bold;
  color: var(--danger-color);
  margin-top: auto;
}

/* 响应式设计 - 平板设备 */
@media (min-width: 768px) and (max-width: 1024px) {
  .shop-items {
    gap: 24px;
    padding: 0 30px;
    max-width: 600px;
  }
  
  .shop-item {
    padding: 24px;
    min-height: 130px;
    border-radius: 18px;
  }
  
  .item-image .el-icon {
    font-size: 52px;
  }
  
  .item-info h3 {
    font-size: 1.2rem;
  }
  
  .item-price {
    font-size: 1.3rem;
  }
}

/* 响应式设计 - 大屏手机 */
@media (max-width: 767px) {
  .shop-items {
    gap: 18px;
    padding: 0 15px;
    max-width: 100%;
  }
  
  .shop-item {
    padding: 18px;
    min-height: 110px;
    border-radius: 14px;
  }
}

/* 响应式设计 - 小屏手机 */
@media (max-width: 480px) {
  .shop-container {
    padding: 0.8rem;
    padding-bottom: 3rem;
  }
  
  .shop-content {
    padding: 0.8rem;
  }
  
  .shop-items {
    gap: 16px;
    padding: 0 12px;
  }
  
  .shop-item {
    padding: 16px;
    min-height: 100px;
    border-radius: 12px;
  }
  
  .item-image .el-icon {
    font-size: 40px;
  }
  
  .item-info h3 {
    font-size: 1rem;
  }
  
  .item-price {
    font-size: 1.1rem;
  }
  
  .item-info p {
    font-size: 0.85rem;
  }
  
  /* 场景对话框响应式 */
  .scene-dialog {
    width: 95% !important;
    max-width: 320px !important;
  }
  
  .scene-card {
    padding: 16px;
    gap: 12px;
    min-height: 80px;
  }
  
  .scene-icon {
    width: 45px;
    height: 45px;
  }
  
  .icon-emoji {
    font-size: 20px;
  }
  
  .scene-title {
    font-size: 1rem;
  }
  
  .scene-description {
    font-size: 0.8rem;
  }
  
  .dialog-footer {
    padding: 16px;
    gap: 12px;
  }
  
  .footer-btn {
    padding: 10px 20px;
  }
}

/* 响应式设计 - 超小屏手机 */
@media (max-width: 320px) {
  .shop-container {
    padding: 0.4rem;
    padding-bottom: 3rem;
  }
  
  .shop-content {
    padding: 0.4rem;
  }
  
  .shop-items {
    gap: 12px;
    padding: 0 10px;
  }
  
  .shop-item {
    padding: 14px;
    min-height: 90px;
  }
  
  .item-image .el-icon {
    font-size: 36px;
  }
  
  .item-info h3 {
    font-size: 0.95rem;
  }
  
  .item-info p {
    font-size: 0.8rem;
  }
}

/* 场景选择对话框 - 自定义实现 */
.scene-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease-out;
}

.scene-dialog-content {
  width: 90%;
  max-width: 400px;
  background: var(--bg-secondary);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.3s ease-out;
}

.scene-dialog-header {
  background: linear-gradient(135deg, #4a6fa5 0%, #6b8cce 100%);
  color: white;
  padding: 24px 24px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.scene-dialog-title {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
  flex: 1;
  text-align: center;
}

.scene-dialog-close {
  background: none;
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.scene-dialog-close:hover {
  background: rgba(255, 255, 255, 0.2);
}

.scene-dialog-body {
  padding: 24px;
  background: var(--bg-tertiary);
  max-height: 70vh;
  overflow-y: auto;
}

.scene-selector {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 自定义场景表单样式 */
.custom-scene-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-primary);
}

.form-input,
.form-textarea {
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 0.9rem;
  background: var(--bg-secondary);
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #4a6fa5;
  box-shadow: 0 0 0 2px rgba(74, 111, 165, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}



.scene-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 16px;
  transition: all 0.3s ease;
  cursor: pointer;
  background: var(--bg-secondary);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  min-height: 80px;
  position: relative;
  border: 1px solid var(--border-color);
}

.scene-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.scene-card-selected {
  background: linear-gradient(135deg, #f0f7ff 0%, #e6f0ff 100%);
  box-shadow: 0 6px 16px rgba(74, 111, 165, 0.2) !important;
}

.hotel-scene {
  border-left: 4px solid #4a6fa5;
}

.airport-scene {
  border-left: 4px solid #667eea;
}

.custom-scene {
  border-left: 4px solid #4caf50;
}

.custom-scene {
  border-left: 4px solid #4caf50;
}

.custom-scene {
  border-left: 4px solid #4caf50;
}

.scene-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.hotel-scene .scene-icon {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
}

.airport-scene .scene-icon {
  background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%);
}

.custom-scene .scene-icon {
  background: linear-gradient(135deg, #e8f5e8 0%, #c8e6c9 100%);
}

.custom-scene .scene-icon {
  background: linear-gradient(135deg, #e8f5e8 0%, #c8e6c9 100%);
}

.custom-scene .scene-icon {
  background: linear-gradient(135deg, #e8f5e8 0%, #c8e6c9 100%);
}

.icon-emoji {
  font-size: 24px;
}

.scene-info {
  flex: 1;
}

.scene-title {
  margin: 0 0 4px;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.scene-description {
  margin: 0;
  font-size: 0.85rem;
  color: var(--text-secondary);
  line-height: 1.4;
}

.scene-radio {
  margin-left: auto;
}

.radio-button {
  width: 20px;
  height: 20px;
  border: 2px solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.radio-button-selected {
  border-color: #4a6fa5;
  background: #4a6fa5;
}

.radio-dot {
  width: 10px;
  height: 10px;
  background: white;
  border-radius: 50%;
  animation: radioPulse 0.3s ease-out;
}

.scene-dialog-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px 24px;
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
}

.footer-btn {
  flex: 1;
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: none;
  cursor: pointer;
  font-size: 0.9rem;
}

.cancel-btn {
  background: #f5f7fa;
  border: 1px solid #e0e0e0;
  color: #666;
}

.cancel-btn:hover {
  background: #e9ecef;
}

.confirm-btn {
  background: linear-gradient(135deg, #4a6fa5 0%, #6b8cce 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(74, 111, 165, 0.3);
}

.confirm-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(74, 111, 165, 0.4);
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes sceneCardFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes aiItemFadeIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes radioPulse {
  from {
    transform: scale(0);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.scene-card {
  animation: sceneCardFadeIn 0.4s ease-out forwards;
}

.scene-card:nth-child(1) {
  animation-delay: 0.1s;
}

.scene-card:nth-child(2) {
  animation-delay: 0.2s;
}

.scene-card:nth-child(3) {
  animation-delay: 0.3s;
}

.scene-card:nth-child(3) {
  animation-delay: 0.3s;
}

.scene-card:nth-child(3) {
  animation-delay: 0.3s;
}

/* AI 项目动画延迟 */
.ai-item:nth-child(1) {
  animation-delay: 0.1s;
}

.ai-item:nth-child(2) {
  animation-delay: 0.2s;
}

.ai-item:nth-child(3) {
  animation-delay: 0.3s;
}

.ai-item:nth-child(4) {
  animation-delay: 0.4s;
}

.ai-item:nth-child(5) {
  animation-delay: 0.5s;
}
</style>