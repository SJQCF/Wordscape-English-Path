<template>
  <div class="login-container">
    <!-- 头部标题 -->
    <div class="login-header">
      <h1 class="app-name">言境英途</h1>
      <p class="app-slogan">提升英语能力，开启学习之旅</p>
    </div>
    
    <!-- 登录表单 -->
    <div class="login-form">
      <!-- 登录方式切换 -->
      <div class="login-tabs">
        <div 
          class="tab-item" 
          :class="{ active: loginType === 'code' }"
          @click="loginType = 'code'"
        >
          验证码登录
        </div>
        <div 
          class="tab-item" 
          :class="{ active: loginType === 'password' }"
          @click="loginType = 'password'"
        >
          密码登录
        </div>
      </div>
      
      <!-- 表单内容 -->
      <div class="form-content">
        <!-- 邮箱输入 -->
        <div class="form-item">
          <el-input
            v-model="loginData.email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
            class="login-input"
          />
        </div>
        
        <!-- 密码输入 -->
        <div class="form-item" v-if="loginType === 'password'">
          <el-input
            v-model="loginData.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            class="login-input"
          />
        </div>
        
        <!-- 验证码输入 -->
        <div class="form-item" v-else>
          <div class="code-input-wrapper">
            <el-input
              v-model="loginData.code"
              placeholder="请输入验证码"
              prefix-icon="Message"
              class="code-input"
            />
            <el-button 
              type="primary" 
              :disabled="countdown > 0"
              @click="sendCode"
              class="code-button"
            >
              {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
            </el-button>
          </div>
        </div>
        
        <!-- 登录按钮 -->
        <el-button 
          type="primary" 
          :loading="loading"
          @click="handleLogin"
          class="login-button"
        >
          {{ loginType === 'code' ? '注册/登录' : '登录' }}
        </el-button>
        
        <!-- 辅助链接 -->
        <div class="login-links">
          <span @click="forgotPassword" class="link-item">忘记密码</span>
          <span @click="register" class="link-item">注册账号</span>
        </div>
      </div>
    </div>
    
    <!-- 密码更改弹窗 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="更改密码"
      :width="windowWidth > 768 ? '30%' : '80%'"
      :close-on-click-modal="false"
    >
      <el-form ref="passwordFormRef" :model="{}" label-width="80px">
        <el-form-item label="新密码">
          <el-input
            v-model="newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closePasswordDialog">取消</el-button>
          <el-button type="primary" @click="handlePasswordChange" :loading="passwordLoading">
            确认更改
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElDialog, ElForm, ElFormItem, ElInput, ElButton } from 'element-plus'
import { loginByPassword, loginByEmail, sendCode as sendEmailCode } from '@/api/login.js'
import { isStudyYesterday, resetStudyDays, getPassword, updatePassword } from '@/api/user.js'
import { setToken } from '@/utils/axios.js'
import { useTheme } from '@/composables/useTheme.js'

// 路由实例
const router = useRouter()
// 获取当前路由
const route = useRoute()

// 登录类型：password 或 code
const loginType = ref('password')
// 是否是忘记密码流程
const isForgotPassword = ref(false)

// 登录数据
const loginData = reactive({
  email: '',
  password: '',
  code: ''
})

// 加载状态
const loading = ref(false)



// 验证码倒计时
const countdown = ref(0)

// 密码更改相关
const showPasswordDialog = ref(false)
const newPassword = ref('')
const confirmPassword = ref('')
const passwordLoading = ref(false)
const passwordFormRef = ref(null)

// 窗口宽度，用于响应式布局
const windowWidth = ref(window.innerWidth)

// 主题相关
const { initTheme: initUserTheme } = useTheme()

// 监听窗口大小变化
window.addEventListener('resize', () => {
  windowWidth.value = window.innerWidth
})

// 发送验证码
const sendCode = async () => {
  if (!loginData.email) {
    ElMessage.warning('请输入邮箱')
    return
  }
  
  // 邮箱格式验证
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(loginData.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }
  
  // 立即开始倒计时
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
  
  try {
    // 调用发送验证码API
    const response = await sendEmailCode(loginData.email)
    
    if (response.code === 1) {
      ElMessage.success('验证码已发送到您的邮箱')
    } else {
      ElMessage.error('验证码发送失败，请稍后重试')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error('验证码发送失败，请稍后重试')
  }
}

// 处理登录
const handleLogin = async () => {
  // 表单验证
  if (!loginData.email) {
    ElMessage.warning('请输入邮箱')
    return
  }
  
  // 邮箱格式验证
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(loginData.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }
  
  if (loginType.value === 'password' && !loginData.password) {
    ElMessage.warning('请输入密码')
    return
  }
  
  if (loginType.value === 'code' && !loginData.code) {
    ElMessage.warning('请输入验证码')
    return
  }
  

  
  loading.value = true
  
  try {
    let response
    if (loginType.value === 'password') {
      // 密码登录
      response = await loginByPassword(loginData)
    } else {
      // 邮箱验证码登录或注册
      response = await loginByEmail(loginData)
    }
    
    if (response.code === 1 && response.data) {
      // 登录成功，存储token和用户信息
      const { token, id, username } = response.data
      console.log('登录成功，用户信息:', { token, id, username })
      
      setToken(token)
      localStorage.setItem('userInfo', JSON.stringify({ id, username }))
      
      // 检查用户前一天是否学习（所有登录方式都检查）
      if (id) {
        console.log('登录成功，开始检测用户学习状态')
        // 检查用户前一天是否学习
        try {
          const studyResponse = await isStudyYesterday(id)
          console.log('学习状态检查结果:', studyResponse)
          if (studyResponse.code === 1 && !studyResponse.data) {
            // 如果前一天没有学习，重置连续学习天数
            await resetStudyDays(id)
            console.log('用户前一天没有学习，连续学习天数已归零')
          }
        } catch (studyError) {
          console.error('检查学习状态失败:', studyError)
        }
        
        // 检查是否是忘记密码流程，或者密码是否为默认密码"1"（仅验证码登录时）
        if (loginType.value === 'code') {
          try {
            console.log('调用getPassword API，userId:', id)
            const passwordResponse = await getPassword(id)
            console.log('密码检查结果:', passwordResponse)
            console.log('密码值:', passwordResponse.data)
            
            // 即使服务器返回异常，也尝试检查密码
            if (isForgotPassword.value || (passwordResponse.code === 1 && passwordResponse.data === '1')) {
              // 如果是忘记密码流程或密码为默认密码，显示密码更改弹窗
              console.log('忘记密码流程或密码为默认密码，显示更改弹窗')
              showPasswordDialog.value = true
              // 不立即跳转，等用户更改密码后再跳转
              return // 终止后续执行
            } else {
              console.log('密码不是默认密码且不是忘记密码流程，不显示更改弹窗')
              // 密码不是默认密码，直接跳转
              ElMessage.success('登录成功')
              // 登录成功后初始化用户主题设置
              await initUserTheme()
              router.push('/study')
            }
          } catch (passwordError) {
            console.error('检查密码状态失败:', passwordError)
            // 检查失败，直接跳转
            ElMessage.success('登录成功')
            // 登录成功后初始化用户主题设置
            await initUserTheme()
            router.push('/study')
          }
        } else {
          // 密码登录，直接跳转
          console.log('密码登录，直接跳转')
          ElMessage.success('登录成功')
          // 登录成功后初始化用户主题设置
          await initUserTheme()
          router.push('/study')
        }
      } else {
        console.log('无用户ID，跳过检测')
        // 无用户ID，直接跳转
        ElMessage.success('登录成功')
        // 登录成功后初始化用户主题设置
        await initUserTheme()
        router.push('/study')
      }
    } else {
      ElMessage.error(response.msg || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 忘记密码
const forgotPassword = () => {
  // 跳转到验证码登录模式
  loginType.value = 'code'
  // 标记为忘记密码流程
  isForgotPassword.value = true
  ElMessage.info('请使用验证码登录来重置密码')
}

// 注册账号
const register = () => {
  // 直接切换到验证码登录模式
  loginType.value = 'code'
  // 跳转到登录页面并设置为验证码登录（邮箱登录）
  router.push({
    path: '/login',
    query: { type: 'code' }
  })
}

// 处理密码更改
const handlePasswordChange = async () => {
  // 表单验证
  if (!newPassword.value) {
    ElMessage.warning('请输入新密码')
    return
  } else if (newPassword.value.length < 6) {
    ElMessage.warning('密码长度必须大于等于6位')
    return
  } else if (newPassword.value !== confirmPassword.value) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  passwordLoading.value = true
  
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo'))
    const response = await updatePassword(userInfo.id, newPassword.value)
    
    if (response.code === 1) {
      ElMessage.success('密码更改成功，正在跳转...')
      showPasswordDialog.value = false
      // 重置表单
      newPassword.value = ''
      confirmPassword.value = ''
      // 登录成功后初始化用户主题设置
      await initUserTheme()
      // 延迟跳转，让用户有时间看到提示
      setTimeout(() => {
        router.push('/study')
      }, 1000)
    } else {
      ElMessage.error('密码更改失败')
    }
  } catch (error) {
    console.error('密码更改失败:', error)
    ElMessage.error('密码更改失败，请稍后重试')
  } finally {
    passwordLoading.value = false
  }
}

// 关闭密码更改弹窗
const closePasswordDialog = async () => {
  showPasswordDialog.value = false
  // 重置表单
  newPassword.value = ''
  confirmPassword.value = ''
  // 登录成功后初始化用户主题设置
  await initUserTheme()
  // 跳转到首页
  router.push('/study')
}

// 组件挂载时检查路由参数
onMounted(() => {
  // 检查路由参数，如果有type参数且值为'code'，则切换到验证码登录
  if (route.query.type === 'code') {
    loginType.value = 'code'
  }
})
</script>

<style scoped>
.login-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 2rem;
  box-sizing: border-box;
}

.login-header {
  text-align: center;
  margin-bottom: 3rem;
}

.app-name {
  font-size: 2rem;
  font-weight: bold;
  color: #1E88E5;
  margin: 0 0 0.5rem 0;
}

.app-slogan {
  font-size: 1rem;
  color: #666;
  margin: 0;
}

.login-form {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.15);
  padding: 2rem;
  box-sizing: border-box;
}

.login-tabs {
  display: flex;
  margin-bottom: 2rem;
  border-bottom: 1px solid #e0e0e0;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 0.8rem 0;
  cursor: pointer;
  color: #666;
  font-size: 1rem;
  transition: all 0.3s ease;
  border-bottom: 2px solid transparent;
}

.tab-item.active {
  color: #1E88E5;
  border-bottom-color: #1E88E5;
  font-weight: 500;
}

.form-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-item {
  width: 100%;
}

.login-input {
  width: 100%;
  height: 50px;
  border-radius: 8px;
}

.code-input-wrapper {
  display: flex;
  gap: 1rem;
}

.code-input {
  flex: 1;
  height: 50px;
  border-radius: 8px;
}

.code-button {
  width: 120px;
  height: 50px;
  border-radius: 8px;
}

.agreement {
  font-size: 0.85rem;
}

.login-button {
  width: 100%;
  height: 50px;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  background: #1E88E5;
  border-color: #1E88E5;
}

.login-button:hover {
  background: #1976D2;
  border-color: #1976D2;
}

.login-links {
  display: flex;
  justify-content: space-between;
  margin-top: 1rem;
}

.link-item {
  font-size: 0.85rem;
  color: #1E88E5;
  cursor: pointer;
  transition: color 0.3s ease;
}

.link-item:hover {
  color: #1976D2;
  text-decoration: underline;
}

/* 响应式设计 */
@media screen and (max-width: 480px) {
  .login-container {
    padding: 1rem;
  }
  
  .login-form {
    padding: 1.5rem;
  }
  
  .app-name {
    font-size: 1.5rem;
  }
  
  .login-button {
    height: 48px;
  }
  
  .login-input,
  .code-input,
  .code-button {
    height: 48px;
  }
}
</style>