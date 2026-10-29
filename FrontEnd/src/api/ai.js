import request from '@/utils/axios'

//ai识图 - 上传图片并获取物体名称和翻译
export const getAnswerByPicture = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/ai/getAnswerByPicture', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

//ai创作文章(无图片)
export const createArticle = (require) => {
  return request.get('/ai/createArticle', {
    params: {
      require: require
    }
  })
}

//ai创作文章(有图片)
export const createArticleWithPicture = (file, require) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('require', require)
  return request.post('/ai/createArticleWithPicture', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

//ai翻译文本
export const translateText = (text) => {
  console.log('API调用参数:', text)
  return request.post('/ai/translateText', null, {
    params: {
      text: text
    }
  })
}

//ai识别题目
export const getAnswerWithQuestion = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/ai/getAnswerWithQuestion', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 启动AI语音会话
 * @param {Object} data - 会话配置
 * @param {string} data.mode - 模式: 'text' 或 'audio'
 * @param {string} data.botName - AI角色名称
 * @param {string} data.systemRole - 系统角色设定
 * @param {string} data.speakingStyle - 说话风格
 * @param {string} data.speaker - TTS音色
 * @returns {Promise}
 */
export function startVoiceChat(data) {
  return request({
    url: '/ai/startVoiceChat',
    method: 'post',
    data
  })
}

/**
 * 结束AI语音会话
 * @param {string} sessionId - 会话ID
 * @returns {Promise}
 */
export function endVoiceChat(sessionId) {
  return request({
    url: `/ai/endVoiceChat/${sessionId}`,
    method: 'post'
  })
}

/**
 * 获取会话状态
 * @param {string} sessionId - 会话ID
 * @returns {Promise}
 */
export function getSessionStatus(sessionId) {
  return request({
    url: `/ai/getSessionStatus/${sessionId}`,
    method: 'get'
  })
}

/**
 * 获取所有活跃会话
 * @returns {Promise}
 */
export function getActiveSessions() {
  return request({
    url: '/ai/getActiveSessions',
    method: 'get'
  })
}

/**
 * 与学习规划 Agent 对话
 * 首次请求可只传 message，服务端会返回 sessionId 用于保留上下文。
 * @param {Object} data
 * @param {string} [data.sessionId] - 上一次响应返回的会话 ID，携带即可保留上下文
 * @param {string} data.message - 用户消息
 * @param {string} [data.providerId] - 指定本次使用的 AI，留空使用默认 AI
 * @param {boolean} [data.deepThinking] - 是否开启深度思考，留空沿用服务端默认
 * @returns {Promise} 返回 { code, msg, data: { sessionId, answer } }
 */
export function chatWithAgent(data) {
  return request({
    url: '/ai/agent/chat',
    method: 'post',
    data
  })
}

/**
 * 获取可切换的 AI 列表（只含 id、名称、模型和能力，不含密钥）
 * @returns {Promise}
 */
export function listProviders() {
  return request({
    url: '/ai/providers',
    method: 'get'
  })
}

/**
 * 清空指定会话的短期记忆
 * @param {string} sessionId - 会话 ID
 * @returns {Promise}
 */
export function clearAgentConversation(sessionId) {
  return request({
    url: `/ai/agent/chat/${sessionId}`,
    method: 'delete'
  })
}

/**
 * 获取当前用户的历史会话列表（按最后活跃时间倒序）
 * @returns {Promise} 返回 [{ sessionId, title, createdAt, updatedAt }]
 */
export function listAgentConversations() {
  return request({
    url: '/ai/agent/conversations',
    method: 'get'
  })
}

/**
 * 读取某个历史会话的全部消息，用于回看并继续对话
 * @param {string} sessionId - 会话 ID
 * @returns {Promise} 返回 [{ messageType, content, createdAt }]
 */
export function getAgentConversationMessages(sessionId) {
  return request({
    url: `/ai/agent/conversations/${sessionId}`,
    method: 'get'
  })
}
