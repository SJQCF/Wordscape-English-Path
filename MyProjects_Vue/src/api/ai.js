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