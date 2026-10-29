<template>
  <div class="chat-page">
    <!-- 顶部导航 -->
    <header class="chat-header">
      <button class="icon-btn" aria-label="返回" @click="goBack">
        <el-icon :size="20"><ArrowLeft /></el-icon>
      </button>
      <div class="header-main">
        <div class="header-title">AI 客服</div>
        <div class="header-sub" :class="{ offline: agentOffline }">
          <span class="dot"></span>
          {{ agentOffline ? '连接异常' : '在线，随时为你解答' }}
        </div>
      </div>
      <button class="icon-btn" aria-label="历史对话" title="历史对话" @click="openHistory">
        <el-icon :size="20"><Clock /></el-icon>
      </button>
      <button class="icon-btn" aria-label="新建对话" title="新建对话" @click="newConversation">
        <el-icon :size="20"><Plus /></el-icon>
      </button>
    </header>

    <!-- 模型选择 -->
    <div class="chat-toolbar">
      <el-select
        v-model="providerId"
        class="provider-select"
        size="small"
        placeholder="选择 AI"
        :loading="providersLoading"
        :disabled="providersLoading"
      >
        <el-option
          v-for="p in providers"
          :key="p.id"
          :label="p.model ? `${p.displayName} · ${p.model}` : p.displayName"
          :value="p.id"
        />
      </el-select>
      <div v-if="selectedSupportsThinking" class="thinking-toggle">
        <el-switch v-model="deepThinking" size="small" />
        <span @click="deepThinking = !deepThinking">深度思考</span>
      </div>
    </div>

    <!-- 消息区 -->
    <main ref="scrollRef" class="chat-body">
      <!-- 空状态 -->
      <div v-if="messages.length === 0" class="welcome">
        <div class="welcome-avatar">
          <el-icon :size="30"><ChatDotRound /></el-icon>
        </div>
        <h3>你好，我是你的英语学习助手</h3>
        <p>可以问我词汇用法、听力资源或备考课程，我会帮你从词库、课程和听力库里查找答案。</p>
        <div class="suggestions">
          <button v-for="s in suggestions" :key="s" class="suggestion" @click="useSuggestion(s)">
            {{ s }}
          </button>
        </div>
      </div>

      <!-- 消息列表 -->
      <template v-else>
        <div
          v-for="m in messages"
          :key="m.id"
          :class="['msg-row', m.role === 'user' ? 'is-user' : 'is-ai']"
        >
          <div v-if="m.role !== 'user'" class="avatar">
            <el-icon :size="18"><ChatDotRound /></el-icon>
          </div>
          <div
            :class="[
              'bubble',
              m.role === 'user' ? 'bubble-user' : 'bubble-ai',
              { 'bubble-error': m.error }
            ]"
          >
            {{ m.content }}
          </div>
        </div>

        <!-- 正在输入 -->
        <div v-if="sending" class="msg-row is-ai">
          <div class="avatar">
            <el-icon :size="18"><ChatDotRound /></el-icon>
          </div>
          <div class="bubble bubble-ai typing">
            <span class="typing-dot"></span>
            <span class="typing-dot"></span>
            <span class="typing-dot"></span>
          </div>
        </div>
      </template>
    </main>

    <!-- 输入区 -->
    <footer class="chat-footer">
      <div class="input-wrap">
        <el-input
          v-model="inputText"
          class="chat-input"
          type="textarea"
          resize="none"
          :autosize="{ minRows: 1, maxRows: 4 }"
          placeholder="输入你的问题，Enter 发送，Shift + Enter 换行"
          @keydown="onKeydown"
        />
        <button
          class="send-btn"
          aria-label="发送"
          :disabled="sending || !inputText.trim()"
          @click="send"
        >
          <el-icon :size="20"><Promotion /></el-icon>
        </button>
      </div>
    </footer>

    <!-- 历史对话面板 -->
    <div v-if="historyVisible" class="history-mask" @click="historyVisible = false"></div>
    <aside class="history-panel" :class="{ open: historyVisible }">
      <div class="history-head">
        <span class="history-head-title">历史对话</span>
        <button class="icon-btn" aria-label="新建对话" title="新建对话" @click="newConversation">
          <el-icon :size="18"><Plus /></el-icon>
        </button>
      </div>
      <div v-if="historyLoading" class="history-empty">加载中…</div>
      <div v-else-if="conversations.length === 0" class="history-empty">还没有历史对话</div>
      <ul v-else class="history-list">
        <li
          v-for="item in conversations"
          :key="item.sessionId"
          :class="['history-item', { active: item.sessionId === sessionId }]"
          @click="selectConversation(item)"
        >
          <div class="history-item-body">
            <div class="history-item-title">{{ item.title || '新对话' }}</div>
            <div class="history-item-time">{{ formatTime(item.updatedAt) }}</div>
          </div>
          <button
            class="history-item-del"
            aria-label="删除"
            title="删除"
            @click.stop="deleteConversation(item)"
          >
            <el-icon :size="15"><Delete /></el-icon>
          </button>
        </li>
      </ul>
    </aside>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, Promotion, Delete, ChatDotRound, Clock } from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme'
import {
  chatWithAgent,
  listProviders,
  clearAgentConversation,
  listAgentConversations,
  getAgentConversationMessages
} from '@/api/ai'

const router = useRouter()
const { initTheme } = useTheme()

const SESSION_KEY = 'aiChatSessionId'
// 进入对话页时默认选中的 AI
const DEFAULT_PROVIDER_ID = 'deepseek'

const suggestions = [
  '查一下 hear 的意思和用法',
  '有适合四级备考的课程吗',
  '推荐一些听力练习资源'
]

const messages = ref([])
const inputText = ref('')
const sending = ref(false)
const sessionId = ref('')
const scrollRef = ref(null)

const providers = ref([])
const providersLoading = ref(false)
const providerId = ref('')
const deepThinking = ref(false)
const agentOffline = ref(false)

// 历史对话
const historyVisible = ref(false)
const historyLoading = ref(false)
const conversations = ref([])

let messageSeed = 0
const nextId = () => `${Date.now()}-${messageSeed++}`

const selectedProvider = computed(() =>
  providers.value.find((p) => p.id === providerId.value)
)

const selectedSupportsThinking = computed(
  // 后端 ProviderInfo 暴露的字段名是 thinking（不是 hasThinking）
  () => !!selectedProvider.value?.thinking
)

// 只保留支持工具调用的 AI，学习规划 Agent 依赖工具能力
const supportsTools = (provider) =>
  Array.isArray(provider?.capabilities) &&
  provider.capabilities.some((c) => String(c).toUpperCase() === 'TOOLS')

const loadProviders = async () => {
  providersLoading.value = true
  try {
    const res = await listProviders()
    if (res && res.code === 1 && Array.isArray(res.data)) {
      providers.value = res.data.filter(supportsTools)
      agentOffline.value = false
      // 默认选中 DeepSeek；若不可用则退回列表中的第一个
      if (!providers.value.some((p) => p.id === providerId.value)) {
        const preferred = providers.value.find((p) => p.id === DEFAULT_PROVIDER_ID)
        providerId.value = preferred ? preferred.id : (providers.value[0]?.id || '')
      }
    }
  } catch (error) {
    console.error('获取 AI 列表失败:', error)
    agentOffline.value = true
  } finally {
    providersLoading.value = false
  }
}

const scrollToBottom = async () => {
  await nextTick()
  const el = scrollRef.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

const persist = () => {
  try {
    if (sessionId.value) {
      localStorage.setItem(SESSION_KEY, sessionId.value)
    } else {
      localStorage.removeItem(SESSION_KEY)
    }
  } catch (error) {
    console.warn('会话缓存写入失败:', error)
  }
}

// 后端消息记录 -> 界面消息结构，非 USER 的一律当 AI 回复展示
const toMessages = (rows) =>
  (rows || []).map((row) => ({
    id: nextId(),
    role: String(row.messageType).toUpperCase() === 'USER' ? 'user' : 'assistant',
    content: row.content || ''
  }))

// 消息以数据库为准，这里只恢复上次的会话 ID；读不到就当作新会话
const restoreSession = async () => {
  let cached = ''
  try {
    cached = localStorage.getItem(SESSION_KEY) || ''
  } catch (error) {
    console.warn('会话缓存读取失败:', error)
  }
  if (!cached) {
    return
  }

  try {
    const res = await getAgentConversationMessages(cached)
    if (res && res.code === 1 && Array.isArray(res.data) && res.data.length > 0) {
      sessionId.value = cached
      messages.value = toMessages(res.data)
      await scrollToBottom()
    } else {
      localStorage.removeItem(SESSION_KEY)
    }
  } catch (error) {
    console.warn('恢复历史会话失败:', error)
    localStorage.removeItem(SESSION_KEY)
  }
}

const onKeydown = (event) => {
  if (event.key === 'Enter' && !event.shiftKey && !event.isComposing) {
    event.preventDefault()
    send()
  }
}

const useSuggestion = (text) => {
  inputText.value = text
  send()
}

const send = async () => {
  const text = inputText.value.trim()
  if (!text || sending.value) {
    return
  }

  messages.value.push({ id: nextId(), role: 'user', content: text })
  inputText.value = ''
  sending.value = true
  await scrollToBottom()

  try {
    const res = await chatWithAgent({
      sessionId: sessionId.value || undefined,
      message: text,
      providerId: providerId.value || undefined,
      deepThinking: deepThinking.value ? true : undefined
    })

    if (res && res.code === 1 && res.data) {
      sessionId.value = res.data.sessionId || sessionId.value
      messages.value.push({
        id: nextId(),
        role: 'assistant',
        content: res.data.answer || '（AI 没有返回内容）'
      })
      agentOffline.value = false
    } else {
      messages.value.push({
        id: nextId(),
        role: 'assistant',
        content: (res && res.msg) || '抱歉，AI 暂时无法回复，请稍后再试。',
        error: true
      })
    }
  } catch (error) {
    console.error('AI 对话失败:', error)
    agentOffline.value = true
    messages.value.push({
      id: nextId(),
      role: 'assistant',
      content: '网络异常，暂时联系不上 AI 客服，请检查网络后重试。',
      error: true
    })
  } finally {
    sending.value = false
    persist()
    await scrollToBottom()
  }
}

// 新建对话：只切换到一个空会话，服务端历史仍然保留
const newConversation = () => {
  if (sending.value) {
    return
  }
  sessionId.value = ''
  messages.value = []
  inputText.value = ''
  persist()
  historyVisible.value = false
  ElMessage.success('已开启新对话')
}

const loadConversations = async () => {
  historyLoading.value = true
  try {
    const res = await listAgentConversations()
    if (res && res.code === 1 && Array.isArray(res.data)) {
      conversations.value = res.data
    }
  } catch (error) {
    console.error('获取历史对话失败:', error)
  } finally {
    historyLoading.value = false
  }
}

const openHistory = async () => {
  historyVisible.value = true
  await loadConversations()
}

// 打开一条历史对话：消息从数据库读回来，后续发送会带着同一个 sessionId 继续上下文
const selectConversation = async (item) => {
  if (sending.value) {
    return
  }
  try {
    const res = await getAgentConversationMessages(item.sessionId)
    if (res && res.code === 1 && Array.isArray(res.data)) {
      sessionId.value = item.sessionId
      messages.value = toMessages(res.data)
      inputText.value = ''
      persist()
      historyVisible.value = false
      await scrollToBottom()
    } else {
      ElMessage.error((res && res.msg) || '加载对话失败')
    }
  } catch (error) {
    console.error('加载对话失败:', error)
    ElMessage.error('加载对话失败')
  }
}

const deleteConversation = async (item) => {
  try {
    await ElMessageBox.confirm('删除后无法恢复，确定删除这条对话吗？', '删除对话', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (error) {
    // 用户取消
    return
  }

  try {
    const res = await clearAgentConversation(item.sessionId)
    if (res && res.code === 1) {
      conversations.value = conversations.value.filter((c) => c.sessionId !== item.sessionId)
      if (sessionId.value === item.sessionId) {
        sessionId.value = ''
        messages.value = []
        inputText.value = ''
        persist()
      }
      ElMessage.success('已删除')
    } else {
      ElMessage.error((res && res.msg) || '删除失败')
    }
  } catch (error) {
    console.error('删除对话失败:', error)
    ElMessage.error('删除失败')
  }
}

const formatTime = (value) => {
  if (!value) {
    return ''
  }
  const date = new Date(String(value).replace(' ', 'T'))
  if (Number.isNaN(date.getTime())) {
    return ''
  }
  const pad = (n) => String(n).padStart(2, '0')
  if (date.toDateString() === new Date().toDateString()) {
    return `${pad(date.getHours())}:${pad(date.getMinutes())}`
  }
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  initTheme()
  restoreSession()
  loadProviders()
})
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100vh;
  height: 100dvh;
  overflow: hidden;
  background:
    linear-gradient(180deg, var(--bg-secondary) 0%, var(--bg-primary) 60%);
  color: var(--text-primary);
}

/* 顶部导航 */
.chat-header {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.7rem 0.9rem;
  padding-top: calc(0.7rem + env(safe-area-inset-top));
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
}

.header-main {
  flex: 1;
  min-width: 0;
  text-align: center;
}

.header-title {
  font-size: 1.05rem;
  font-weight: 600;
  line-height: 1.3;
}

.header-sub {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
  font-size: 0.72rem;
  color: var(--text-secondary);
}

.header-sub .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--success-color);
}

.header-sub.offline {
  color: var(--danger-color);
}

.header-sub.offline .dot {
  background: var(--danger-color);
}

.icon-btn {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--text-primary);
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.icon-btn:hover {
  background: var(--bg-tertiary);
}

/* 模型选择 */
.chat-toolbar {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0.9rem;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
}

.provider-select {
  flex: 1;
  max-width: 260px;
}

.thinking-toggle {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.8rem;
  color: var(--text-secondary);
  white-space: nowrap;
}

.thinking-toggle span {
  cursor: pointer;
}

/* 消息区 */
.chat-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
  -webkit-overflow-scrolling: touch;
  padding: 1rem 0.9rem;
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  max-width: 100%;
}

.msg-row.is-user {
  flex-direction: row-reverse;
}

.avatar {
  flex: 0 0 auto;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, #4a6fa5 0%, #6b8cce 100%);
}

.bubble {
  max-width: 76%;
  padding: 0.65rem 0.85rem;
  font-size: 0.92rem;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  border-radius: 14px;
}

.bubble-ai {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-top-left-radius: 4px;
  color: var(--text-primary);
}

.bubble-user {
  background: linear-gradient(135deg, #4a6fa5 0%, #6b8cce 100%);
  color: #fff;
  border-top-right-radius: 4px;
}

.bubble-error {
  border-color: var(--danger-color);
  color: var(--danger-color);
}

/* 输入中动画 */
.typing {
  display: flex;
  align-items: center;
  gap: 5px;
  min-height: 22px;
}

.typing-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--text-tertiary);
  animation: typingBlink 1.2s infinite ease-in-out;
}

.typing-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typingBlink {
  0%,
  60%,
  100% {
    opacity: 0.25;
    transform: translateY(0);
  }
  30% {
    opacity: 1;
    transform: translateY(-3px);
  }
}

/* 空状态 */
.welcome {
  margin: auto;
  padding: 1rem;
  max-width: 520px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.6rem;
}

.welcome-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, #4a6fa5 0%, #6b8cce 100%);
  box-shadow: var(--shadow-md);
}

.welcome h3 {
  margin: 0.4rem 0 0;
  font-size: 1.05rem;
  color: var(--text-primary);
}

.welcome p {
  margin: 0;
  font-size: 0.85rem;
  line-height: 1.6;
  color: var(--text-secondary);
}

.suggestions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  width: 100%;
  margin-top: 0.6rem;
}

.suggestion {
  padding: 0.6rem 0.8rem;
  font-size: 0.85rem;
  text-align: left;
  color: var(--text-primary);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.suggestion:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

/* 输入区 */
.chat-footer {
  flex: 0 0 auto;
  padding: 0.6rem 0.9rem;
  padding-bottom: calc(0.6rem + env(safe-area-inset-bottom));
  background: var(--bg-primary);
  border-top: 1px solid var(--border-color);
}

.input-wrap {
  display: flex;
  align-items: flex-end;
  gap: 0.5rem;
}

.chat-input {
  flex: 1;
}

:deep(.chat-input .el-textarea__inner) {
  border-radius: 12px;
  background: var(--bg-secondary);
  color: var(--text-primary);
  box-shadow: none;
  border: 1px solid var(--border-color);
  padding: 0.6rem 0.8rem;
  font-size: 0.92rem;
  line-height: 1.5;
}

:deep(.chat-input .el-textarea__inner:focus) {
  border-color: var(--primary-color);
}

.send-btn {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 50%;
  color: #fff;
  background: linear-gradient(135deg, #4a6fa5 0%, #6b8cce 100%);
  cursor: pointer;
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.send-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.send-btn:not(:disabled):active {
  transform: scale(0.94);
}

/* 历史对话面板 */
.history-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 200;
}

.history-panel {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: min(320px, 85vw);
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
  box-shadow: var(--shadow-lg);
  transform: translateX(-100%);
  transition: transform 0.25s ease;
  z-index: 201;
}

.history-panel.open {
  transform: translateX(0);
}

.history-head {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
  padding: 0.8rem 0.9rem;
  padding-top: calc(0.8rem + env(safe-area-inset-top));
  border-bottom: 1px solid var(--border-color);
}

.history-head-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.history-empty {
  padding: 2rem 1rem;
  text-align: center;
  font-size: 0.85rem;
  color: var(--text-tertiary);
}

.history-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  list-style: none;
  margin: 0;
  padding: 0.5rem;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 0.7rem;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.history-item:hover {
  background: var(--bg-secondary);
}

.history-item.active {
  background: var(--bg-tertiary);
}

.history-item-body {
  flex: 1;
  min-width: 0;
}

.history-item-title {
  font-size: 0.88rem;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-item-time {
  margin-top: 0.15rem;
  font-size: 0.72rem;
  color: var(--text-tertiary);
}

/* 默认半透明可见，触屏设备没有 hover 也能点到 */
.history-item-del {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--text-tertiary);
  cursor: pointer;
  opacity: 0.55;
  transition: all 0.2s ease;
}

.history-item-del:hover {
  color: var(--danger-color);
  background: var(--bg-tertiary);
  opacity: 1;
}

@media (max-width: 480px) {
  .bubble {
    max-width: 82%;
    font-size: 0.9rem;
  }

  .provider-select {
    max-width: none;
  }
}
</style>
