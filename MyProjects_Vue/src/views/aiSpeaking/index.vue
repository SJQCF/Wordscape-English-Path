<template>
  <div class="ai-speaking-container">
    <!-- 头部导航 -->
    <div class="header">
      <el-button type="text" @click="goBack" class="back-button">
        <el-icon size="24"><ArrowLeft /></el-icon>
      </el-button>
      <h1 class="page-title">口语练习</h1>
      <div class="header-placeholder"></div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 渐变圆形 -->
      <div :class="['gradient-circle', { 'recording': isRecording }]"></div>
      

      
      <!-- 对话内容 -->
      <div v-if="dialogueList.length > 0" class="dialogue-container">
        <div 
          v-for="(item, index) in dialogueList" 
          :key="index"
          :class="['dialogue-item', item.role]"
        >
          <div class="dialogue-content">{{ item.content }}</div>
        </div>
      </div>
    </div>
    
    <!-- 底部按钮区域 -->
    <div class="bottom-buttons">
      <!-- 话筒按钮 -->
      <div class="button-item">
        <el-button 
          :type="isRecording ? 'warning' : 'primary'" 
          circle 
          size="large" 
          class="mic-button"
          @click="toggleRecording"
          :loading="loading"
        >
          <el-icon size="32"><Mic /></el-icon>
        </el-button>
        <span class="button-label">{{ isRecording ? '结束' : '开始' }}</span>
      </div>
      
      <!-- 叉按钮 -->
      <div class="button-item">
        <el-button 
          type="danger" 
          circle 
          size="large" 
          class="cancel-button"
          @click="endSession"
          :disabled="!sessionId"
        >
          <el-icon size="32"><Close /></el-icon>
        </el-button>
        <span class="button-label">取消</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ArrowLeft, Mic, Close } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import request from '@/utils/axios'
import { prepareMicAudioContext, startBrowserVoicePipeline } from '@/utils/browserVoiceChat'

// 初始化主题
const { initTheme } = useTheme()

// 获取路由实例
const router = useRouter()
const route = useRoute()

// 状态管理
const sessionId = ref('')
const isRecording = ref(false)
const loading = ref(false)
const sessionStatus = ref('idle') // idle, active, ended
const dialogueList = ref([])
const hintText = ref('')

/** 浏览器语音管道停止函数 */
let voicePipelineStop = null

// 计算属性
const sessionStatusText = computed(() => {
  const statusMap = {
    idle: '未开始',
    active: '进行中',
    ended: '已结束'
  }
  return statusMap[sessionStatus.value] || '未知'
})

const sessionStatusClass = computed(() => {
  return `status-${sessionStatus.value}`
})

// API调用函数
const startVoiceChat = async () => {
  let mic = null
  try {
    loading.value = true
    hintText.value = '正在请求麦克风...'
    mic = await prepareMicAudioContext()

    hintText.value = '正在连接语音服务...'
    const sessionConfig = {
      mode: 'browser',
      botName: '口语老师',
      systemRole: '你是一位专业的英语口语老师，帮助学生练习日常对话。你的第一句话是\'May I help you?\'',
      speakingStyle: '语速适中，发音清晰，耐心友好',
      speaker: 'zh_male_yunzhou_jupiter_bigtts'
    }
    const result = await request.post('/ai/startVoiceChat', sessionConfig)
    if (!result.code) {
      hintText.value = result.msg || '开始语音会话失败'
      mic.stream.getTracks().forEach((t) => t.stop())
      try {
        mic.audioCtx.close()
      } catch {
        /* ignore */
      }
      return
    }

    sessionId.value = result.data.sessionId
    sessionStatus.value = 'active'

    const { stop } = await startBrowserVoicePipeline(sessionId.value, mic)
    voicePipelineStop = stop
    hintText.value = '对话中，点击结束停止'
    isRecording.value = true
  } catch (error) {
    console.error('开始语音会话失败:', error)
    hintText.value = error?.message || '无法启动语音'
    sessionId.value = ''
    sessionStatus.value = 'idle'
    isRecording.value = false
    if (mic?.stream) {
      mic.stream.getTracks().forEach((t) => t.stop())
    }
    if (mic?.audioCtx) {
      try {
        mic.audioCtx.close()
      } catch {
        /* ignore */
      }
    }
  } finally {
    loading.value = false
  }
}

const endVoiceChat = async () => {
  if (voicePipelineStop) {
    try {
      voicePipelineStop()
    } catch (e) {
      console.warn(e)
    }
    voicePipelineStop = null
  }

  const sid = sessionId.value
  if (!sid) {
    isRecording.value = false
    return
  }

  try {
    loading.value = true
    const result = await request.post(`/ai/endVoiceChat/${sid}`)
    if (result.code) {
      sessionStatus.value = 'ended'
      isRecording.value = false
    } else {
      hintText.value = result.msg || '结束语音会话失败'
    }
  } catch (error) {
    console.error('结束语音会话失败:', error)
    hintText.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
    sessionId.value = ''
    isRecording.value = false
  }
}

const getSessionStatus = async () => {
  if (!sessionId.value) return
  
  try {
    const result = await request.get(`/api/ai/getSessionStatus/${sessionId.value}`)
    if (result.code) {
      sessionStatus.value = result.data.status
    }
  } catch (error) {
    console.error('获取会话状态失败:', error)
  }
}

// 切换录音状态
const toggleRecording = async () => {
  if (isRecording.value) {
    await endVoiceChat()
  } else {
    await startVoiceChat()
  }
}

// 结束会话
const endSession = async () => {
  await endVoiceChat()
  sessionStatus.value = 'idle'
  dialogueList.value = []
  hintText.value = ''
  isRecording.value = false
}

// 组件挂载时初始化主题
onMounted(() => {
  initTheme()
  
  // 检查路由参数中是否有会话ID（从其他页面跳转过来的场景）
  const routeSessionId = route.query.sessionId
  if (routeSessionId) {
    sessionId.value = routeSessionId
    sessionStatus.value = 'active'
    isRecording.value = true // 设置为已开始状态，按钮显示"结束"
    hintText.value = '会话已创建，点击结束按钮可关闭会话'
  }
})

onUnmounted(() => {
  if (voicePipelineStop) {
    try {
      voicePipelineStop()
    } catch {
      /* ignore */
    }
    voicePipelineStop = null
  }
})

// 返回上一页
const goBack = async () => {
  if (sessionId.value || voicePipelineStop) {
    await endVoiceChat()
  }
  router.back()
}
</script>

<style scoped>
/* 整体容器 */
.ai-speaking-container {
  width: 100%;
  height: 100vh;
  box-sizing: border-box;
  background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-tertiary) 100%);
  color: var(--text-primary);
  /* 适配刘海屏 */
  padding-left: env(safe-area-inset-left);
  padding-right: env(safe-area-inset-right);
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
  background: transparent;
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

/* 主要内容区域 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  gap: 2rem;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  position: relative;
}

/* 会话信息 */
.session-info {
  background-color: rgba(255, 255, 255, 0.1);
  padding: 1rem;
  border-radius: 8px;
  width: 100%;
  max-width: 400px;
  text-align: center;
  position: absolute;
  top: 2rem;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1;
}

.session-status {
  margin-bottom: 0.5rem;
  font-size: 1rem;
}

.status-label {
  color: var(--text-secondary);
}

.status-value {
  font-weight: 500;
}

.status-active {
  color: var(--success-color);
}

.status-idle {
  color: var(--text-secondary);
}

.status-ended {
  color: var(--danger-color);
}

.session-id {
  font-size: 0.8rem;
  color: var(--text-tertiary);
  word-break: break-all;
}

/* 对话容器 */
.dialogue-container {
  width: 100%;
  max-width: 400px;
  margin-top: 1rem;
  padding: 1rem;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.dialogue-item {
  margin-bottom: 0.8rem;
  max-width: 80%;
  padding: 0.8rem;
  border-radius: 12px;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dialogue-item.user {
  align-self: flex-end;
  background-color: var(--primary-color);
  color: white;
  border-bottom-right-radius: 4px;
  margin-left: auto;
}

.dialogue-item.ai {
  align-self: flex-start;
  background-color: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-bottom-left-radius: 4px;
}

.dialogue-content {
  font-size: 0.9rem;
  line-height: 1.4;
}

/* 渐变圆形 */
.gradient-circle {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color) 0%, rgba(255, 152, 0, 0.7) 100%);
  box-shadow: 0 10px 30px rgba(255, 152, 0, 0.3);
  animation: pulse 3s ease-in-out infinite;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 录音状态的圆形 */
.gradient-circle.recording {
  animation: pulse 1s ease-in-out infinite, rotate 3s linear infinite;
  box-shadow: 0 0 20px var(--primary-color), 0 0 40px var(--primary-color);
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 脉冲动画 */
@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 提示文字 */
.hint-text {
  font-size: 1rem;
  color: var(--text-secondary);
  text-align: center;
  margin-top: 1rem;
}



/* 底部按钮区域 */
.bottom-buttons {
  display: flex;
  justify-content: center;
  gap: 2rem;
  padding: 2rem;
  padding-bottom: calc(2rem + env(safe-area-inset-bottom));
}

.button-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.button-label {
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-top: 0.5rem;
}

.button-label {
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-top: 0.5rem;
}

.mic-button {
  width: 80px;
  height: 80px;
  background-color: var(--primary-color);
  border: none;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
  transition: all 0.3s ease;
}

.mic-button:hover {
  background-color: var(--primary-color);
  transform: none;
}

.cancel-button {
  width: 80px;
  height: 80px;
  background-color: #f56c6c;
  border: none;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
  transition: all 0.3s ease;
}

.cancel-button:hover {
  background-color: #f56c6c;
  transform: none;
}

/* 响应式设计 */
@media screen and (max-width: 480px) {
  .gradient-circle {
    width: 180px;
    height: 180px;
  }
  
  .mic-button,
  .cancel-button {
    width: 70px;
    height: 70px;
  }
  
  .bottom-buttons {
    gap: 1.5rem;
    padding: 1.5rem;
  }
}
</style>