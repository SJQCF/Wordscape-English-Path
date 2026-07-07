<script setup>
import { ref, onMounted, computed, nextTick } from 'vue' // 确保导入onMounted和computed
import { useRouter } from 'vue-router'
import {
  Histogram,
  Calendar,
  Reading,
  Document,
  StarFilled,
  Collection,
  Timer,
  ArrowRight,
  Upload,
  Camera,
  Picture,
  Moon,
  SwitchButton
} from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme';

import { useStudyData } from '@/composables/useStudyData' // 导入学习数据组合式函数
import { useUserData } from '@/composables/useUserData' // 导入用户数据组合式函数
import { updateUserName, updateUserSignature, updateUserAvatar } from '@/api/user' // 导入更新用户信息的API函数
import { getCurrentUserId } from '@/utils/auth.js'
import { ElMessage, ElMessageBox } from 'element-plus'

// 使用组合式函数 - 共享学习天数和用户类型
const { studyDays, totalStudyDays, userType } = useStudyData()

// 使用组合式函数 - 获取用户数据
const { userSignature, userName, userAvatar, refresh: refreshUserData } = useUserData()

// 编辑状态管理
const editingName = ref(false)
const editingSignature = ref(false)
const newName = ref('')
const newSignature = ref('')
const saving = ref(false)
// 输入框引用
const nameInputRef = ref(null)
const signatureInputRef = ref(null)

// 头像相关状态
const showAvatarDialog = ref(false)
const showAvatarOptions = ref(false)
const avatarFile = ref(null)
const cameraInputRef = ref(null)
const galleryInputRef = ref(null)

// 开始编辑名字
const startEditName = () => {
  newName.value = userName.value
  editingName.value = true
  
  // 确保输入框获得焦点
  nextTick(() => {
    if (nameInputRef.value) {
      nameInputRef.value.focus()
    }
  })
}

// 开始编辑签名
const startEditSignature = () => {
  newSignature.value = userSignature.value
  editingSignature.value = true
  
  // 确保输入框获得焦点
  nextTick(() => {
    if (signatureInputRef.value) {
      signatureInputRef.value.focus()
    }
  })
}

// 保存新名字
const saveNewName = async () => {
  if (!newName.value.trim()) {
    // 空值时不保存，直接取消编辑
    editingName.value = false
    return
  }
  
  saving.value = true
  // 从登录信息获取用户ID
  const userId = getCurrentUserId()
  
  try {
    const result = await updateUserName(userId, newName.value.trim())
    if (result.code) {
      // 更新成功后刷新用户数据
      await refreshUserData()
      editingName.value = false
    }
  } catch (error) {
    console.error('更新用户名字失败:', error)
  } finally {
    saving.value = false
  }
}

// 保存新签名
const saveNewSignature = async () => {
  saving.value = true
  // 从登录信息获取用户ID
  const userId = getCurrentUserId()
  
  try {
    const result = await updateUserSignature(userId, newSignature.value)
    if (result.code) {
      // 更新成功后刷新用户数据
      await refreshUserData()
      editingSignature.value = false
    }
  } catch (error) {
    console.error('更新用户签名失败:', error)
  } finally {
    saving.value = false
  }
}

// 学习类型配置 - 根据ID定义不同学习内容
const studyTypeConfig = {
  1: { title: '高中'},
  2: { title: '四级'},
  3: { title: '六级'},
  4: { title: '商务'},
  5: { title: '出国'}
}

// 当前学习类型的配置 - 使用computed响应式更新
const currentStudyConfig = computed(() => studyTypeConfig[userType.value] || studyTypeConfig[3])

// 初始化主题
const { initTheme } = useTheme()

// 路由实例
const router = useRouter()

// 钩子函数
onMounted(() => {
  // 数据已经通过组合式函数自动加载，这里可以添加其他初始化逻辑
  initTheme()
})



// 手动刷新数据的方法
const refreshData = () => {
  // 如果需要手动刷新，可以调用组合式函数提供的refresh方法
  // refresh()
}

// 打开头像预览弹窗
const openAvatarDialog = () => {
  showAvatarDialog.value = true
}

// 关闭头像预览弹窗
const closeAvatarDialog = () => {
  showAvatarDialog.value = false
  showAvatarOptions.value = false
}

// 显示头像选择选项
const showAvatarSelectOptions = () => {
  showAvatarOptions.value = true
}

// 触发相机拍照
const triggerCamera = () => {
  cameraInputRef.value.click()
}

// 触发相册选择
const triggerGallery = () => {
  galleryInputRef.value.click()
}

// 处理文件选择
const handleAvatarChange = async (event) => {
  const file = event.target.files[0]
  if (file) {
    // 检查文件类型
    if (!file.type.startsWith('image/')) {
      ElMessage.error('请选择图片文件')
      return
    }
    
    // 检查文件大小
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error('图片大小不能超过5MB')
      return
    }
    
    avatarFile.value = file
    
    try {
      // 从登录信息获取用户ID
      const userId = getCurrentUserId()
      
      const result = await updateUserAvatar(userId, file)
      if (result.code) {
        // 更新成功后，刷新用户数据
        await refreshUserData()
        ElMessage.success('头像已更新')
        closeAvatarDialog()
      } else {
        ElMessage.error('头像更新失败')
      }
    } catch (error) {
      console.error('更新头像失败:', error)
      ElMessage.error('头像更新失败')
    }
    
    // 重置input，以便下次选择
    event.target.value = ''
  }
}

// 处理退出登录
const handleLogout = () => {
  try {
    // 清除本地存储中的用户信息和token
    localStorage.removeItem('userInfo')
    localStorage.removeItem('token')
    
    // 跳转到登录页面
    router.push('/login')
    
    ElMessage.success('退出登录成功')
  } catch (error) {
    console.error('退出登录失败:', error)
    ElMessage.error('退出登录失败')
  }
}
</script>

<template>
  <div class="user-container">
    <!-- 用户信息头部 -->
    <div class="user-header">
      <div class="user-avatar" @click="openAvatarDialog">
        <el-avatar :size="80" :src="userAvatar || 'https://picsum.photos/id/1005/200/200'">
          {{ userName.charAt(0) || '未知' }}
        </el-avatar>
      </div>
      <div class="user-info">
        <!-- 名字编辑 -->
        <div class="user-name-container">
          <div v-if="!editingName" class="user-name" @click="startEditName">
            {{ userName || '点击设置名字' }}
          </div>
          <div v-else class="user-name-edit">
            <el-input 
              ref="nameInputRef"
              v-model="newName" 
              placeholder="请输入新名字"
              size="small"
              @keyup.enter="saveNewName"
              @blur="saveNewName"
            />
          </div>
        </div>
        
        <!-- 签名编辑 -->
        <div class="user-signature-container">
          <div v-if="!editingSignature" class="user-id" @click="startEditSignature">
            {{ userSignature || '点击设置个性签名' }}
          </div>
          <div v-else class="user-signature-edit">
            <el-input 
              ref="signatureInputRef"
              v-model="newSignature" 
              placeholder="请输入个性签名"
              size="small"
              @keyup.enter="saveNewSignature"
              @blur="saveNewSignature"
            />
          </div>
        </div>
        
        <div class="user-level">{{ currentStudyConfig.title }}</div>
      </div>
    </div>
    
    <!-- 学习统计 -->
    <div class="study-stats">
      <div class="stats-item">
        <el-icon size="24" class="stats-icon">
          <Histogram />
        </el-icon>
        <div class="stats-text">累计已学 {{ totalStudyDays }}天，坚持连续学习 {{ studyDays }} 天</div>
      </div>
    </div>
    
    <!-- 功能列表 -->
    <div class="function-list">
      <!-- 我的日历 -->
      <router-link to="/calendar" class="function-item-link">
        <div class="function-item">
          <el-icon size="20" class="function-icon">
            <Calendar />
          </el-icon>
          <span class="function-text">学习日历</span>
          <el-icon size="16" class="function-arrow">
            <ArrowRight />
          </el-icon>
        </div>
      </router-link>
      
      <!-- 我的学习 -->
      <router-link to="/myCourses" class="function-item-link">
        <div class="function-item no-border-bottom">
          <el-icon size="20" class="function-icon">
            <Collection />
          </el-icon>
          <span class="function-text">我的课程</span>
          <el-icon size="16" class="function-arrow">
            <ArrowRight />
          </el-icon>
        </div>
      </router-link>
      
      <!-- 我的词汇量 -->
      <router-link to="/allSudied" class="function-item-link">
        <div class="function-item no-border">
          <el-icon size="20" class="function-icon">
            <Document />
          </el-icon>
          <span class="function-text">全部已学</span>
          <el-icon size="16" class="function-arrow">
            <ArrowRight />
          </el-icon>
        </div>
      </router-link>
      
      <!-- 学习计划 -->
      <router-link to="/studyPlan" class="function-item-link">
        <div class="function-item no-border-top no-border-bottom">
          <el-icon size="20" class="function-icon">
            <Timer />
          </el-icon>
          <span class="function-text">学习计划</span>
          <el-icon size="16" class="function-arrow">
            <ArrowRight />
          </el-icon>
        </div>
      </router-link>
      
      <!-- 我的收藏 -->
      <router-link to="/favorites" class="function-item-link">
        <div class="function-item">
          <el-icon size="20" class="function-icon">
            <StarFilled />
          </el-icon>
          <span class="function-text">我的收藏</span>
          <el-icon size="16" class="function-arrow">
            <ArrowRight />
          </el-icon>
        </div>
      </router-link>
      
      <!-- 我的图书 -->
      <router-link to="/books?from=user" class="function-item-link">
        <div class="function-item">
          <el-icon size="20" class="function-icon">
            <Reading />
          </el-icon>
          <span class="function-text">我的图书</span>
          <el-icon size="16" class="function-arrow">
            <ArrowRight />
          </el-icon>
        </div>
      </router-link>
      
      <!-- 黑夜模式 -->
      <router-link to="/darkMode" class="function-item-link">
        <div class="function-item">
          <el-icon size="20" class="function-icon">
            <Moon />
          </el-icon>
          <span class="function-text">黑夜模式</span>
          <el-icon size="16" class="function-arrow">
            <ArrowRight />
          </el-icon>
        </div>
      </router-link>
      
      <!-- 退出登录 -->
      <div class="function-item-link" @click="handleLogout">
        <div class="function-item">
          <el-icon size="20" class="function-icon">
            <SwitchButton />
          </el-icon>
          <span class="function-text">退出登录</span>
          <el-icon size="16" class="function-arrow">
            <ArrowRight />
          </el-icon>
        </div>
      </div>
    </div>
    
    <!-- 头像预览弹窗 -->
    <el-dialog
      v-model="showAvatarDialog"
      title="头像设置"
      width="80%"
      center
    >
      <div class="avatar-dialog-content">
        <!-- 放大的头像 -->
        <div class="avatar-preview">
          <el-avatar :size="200" :src="userAvatar || 'https://picsum.photos/id/1005/200/200'">
            {{ userName.charAt(0) || '未知' }}
          </el-avatar>
        </div>
        
        <!-- 更换头像按钮 -->
        <el-button
          v-if="!showAvatarOptions"
          type="primary"
          @click="showAvatarSelectOptions"
          class="change-avatar-button"
          icon="Upload"
        >
          更换头像
        </el-button>
        
        <!-- 头像选择选项 -->
        <div v-else class="avatar-options">
          <el-button
            type="primary"
            @click="triggerCamera"
            class="avatar-option-button"
          >
            <el-icon><Camera /></el-icon>
            拍照
          </el-button>
          <el-button
            type="primary"
            @click="triggerGallery"
            class="avatar-option-button"
          >
            <el-icon><Picture /></el-icon>
            从相册选择
          </el-button>
          <el-button
            @click="showAvatarOptions = false"
            class="avatar-option-button"
          >
            取消
          </el-button>
        </div>
        
        <!-- 隐藏的文件输入 -->
        <input
          ref="cameraInputRef"
          type="file"
          accept="image/*"
          capture="camera"
          class="hidden-file-input"
          @change="handleAvatarChange"
        />
        <input
          ref="galleryInputRef"
          type="file"
          accept="image/*"
          class="hidden-file-input"
          @change="handleAvatarChange"
        />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 整体容器 */
.user-container {
  padding: 1rem;
  min-height: 100vh;
  background: linear-gradient(180deg, #ffffff 0%, #e6f0ff 50%, #4a6fa5 100%);
  color: var(--text-primary);
  transition: all 0.3s ease;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 5rem; /* 增加底部空间，确保所有功能项都能被看到 */
  height: 100vh;
  box-sizing: border-box;
  margin: 0;
}

/* 黑夜模式样式 */
html.dark .user-container {
  background: linear-gradient(180deg, #1a1a1a 0%, #2d2d2d 50%, #121212 100%);
  color: var(--text-primary);
}

/* 用户信息头部 */
.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

/* 用户头像 */
.user-avatar {
  margin-right: 1.5rem;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.user-avatar:hover {
  transform: none;
}

.user-avatar .el-avatar {
  border: 2px solid #e6f7ff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 头像弹窗样式 */
.avatar-dialog-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rem;
  padding: 1rem;
}

.avatar-preview {
  margin: 1rem 0;
}

.change-avatar-button {
  margin-top: 1rem;
  width: 80%;
}

.avatar-options {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  width: 100%;
  align-items: stretch;
  margin-top: 1rem;
  padding: 0 10%;
}

.avatar-option-button {
  width: 100% !important;
  min-width: 100% !important;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  box-sizing: border-box;
  margin: 0 auto !important;
}

.hidden-file-input {
  display: none;
}

/* 用户信息 */
.user-info {
  flex: 1;
}

.user-name-container, .user-signature-container {
  position: relative;
  margin-bottom: 0.3rem;
}

.user-name {
  font-size: 1.3rem;
  font-weight: bold;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.user-name:hover {
  color: var(--text-primary);
  text-decoration: none;
}

.user-id {
  font-size: 0.85rem;
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.user-id:hover {
  color: var(--text-secondary);
  text-decoration: none;
}

.user-name-edit, .user-signature-edit {
  width: 100%;
}

.user-name-edit .el-input, .user-signature-edit .el-input {
  width: 100%;
}

.user-name-edit .el-input__inner, .user-signature-edit .el-input__inner {
  font-size: inherit;
  font-weight: inherit;
  color: inherit;
}

.user-level {
  font-size: 0.9rem;
  color: var(--primary-color);
  background-color: var(--level-bg, rgba(64, 158, 255, 0.1));
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  display: inline-block;
}

/* 学习统计 */
.study-stats {
  background-color: var(--bg-secondary);
  border-radius: 12px;
  padding: 1.2rem;
  margin-bottom: 1.5rem;
  transition: all 0.3s ease;
}

.stats-item {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.stats-icon {
  color: var(--icon-color, #409EFF);
}

.stats-text {
  font-size: 1rem;
  color: var(--text-primary);
  font-weight: 500;
}

/* 功能列表 */
.function-list {
  background-color: transparent;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.function-item {
  display: flex;
  align-items: center;
  padding: 1.2rem 1rem;
  border-bottom: 1px solid var(--border-color);
  background-color: var(--bg-primary);
  margin-bottom: 0.5rem;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.function-item:last-child {
  border-bottom: none;
}

.function-item:active {
  background-color: var(--bg-tertiary);
}

/* 移除特定功能项的边框 */
.function-item.no-border {
  border-bottom: none;
}

.function-item.no-border-top {
  border-top: none;
}

.function-item.no-border-bottom {
  border-bottom: none;
}

/* 路由链接样式 */
.function-item-link {
  display: block;
  text-decoration: none;
  color: inherit;
  width: 100%;
}

.function-icon {
  margin-right: 1rem;
  color: var(--icon-color, #409EFF);
  width: 20px;
}

.function-text {
  flex: 1;
  font-size: 1rem;
  color: var(--text-primary);
}

.function-arrow {
  color: var(--text-tertiary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-container {
    padding: 1rem;
    padding-bottom: 5rem; /* 增加底部空间，确保所有功能项都能被看到 */
    height: 100vh;
  }
  
  .user-name {
    font-size: 1.2rem;
  }
  
  .stats-text {
    font-size: 0.95rem;
  }
}

@media (max-width: 480px) {
  .user-container {
    padding: 0.8rem;
    padding-bottom: 5rem; /* 增加底部空间，确保所有功能项都能被看到 */
    height: 100vh;
  }
  
  .user-header {
    margin-bottom: 1.5rem;
    padding-bottom: 1rem;
  }
  
  .user-avatar {
    margin-right: 1rem;
  }
  
  .user-avatar .el-avatar {
    size: 60px;
  }
  
  .avatar-dialog-content {
    gap: 1.5rem;
  }
  
  .change-avatar-button {
    width: 90%;
  }
  
  .avatar-options {
    padding: 0 5%;
  }
  
  .avatar-option-button {
    width: 100% !important;
    min-width: 100% !important;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    box-sizing: border-box;
    margin: 0 auto !important;
  }
  
  .avatar-options .el-button {
    width: 100% !important;
    min-width: 100% !important;
    box-sizing: border-box !important;
    margin: 0 auto !important;
  }
  
  .user-name {
    font-size: 1.1rem;
  }
  
  .user-id {
    font-size: 0.8rem;
  }
  
  .user-level {
    font-size: 0.8rem;
  }
  
  .stats-text {
    font-size: 0.9rem;
  }
  
  .function-item {
    padding: 1rem 0.8rem;
  }
}
/* 深色模式下的图标颜色 */
.dark-mode .stats-icon,
.dark-mode .function-icon {
  --icon-color: #FF9800;
}

.dark-mode .user-level {
  --level-bg: rgba(255, 152, 0, 0.1);
}

/* 退出登录对话框样式 */
:deep(.logout-dialog) {
  max-width: 90%;
  box-sizing: border-box;
}

@media (max-width: 480px) {
  :deep(.logout-dialog) {
    max-width: 95%;
  }
}

/* 退出登录对话框样式 */
.logout-dialog {
  max-width: 90% !important;
  width: 300px !important;
}

@media (max-width: 480px) {
  .logout-dialog {
    max-width: 95% !important;
    width: 280px !important;
  }
  
  .logout-dialog .el-message-box__content {
    font-size: 14px !important;
  }
  
  .logout-dialog .el-message-box__btns {
    display: flex;
    justify-content: space-between;
  }
  
  .logout-dialog .el-button {
    flex: 1;
    margin: 0 5px;
  }
}

/* 退出登录对话框样式 */
.logout-dialog {
  max-width: 90% !important;
  width: 300px !important;
}

@media (max-width: 480px) {
  .logout-dialog {
    max-width: 95% !important;
    width: 280px !important;
  }
  
  .logout-dialog .el-message-box__content {
    font-size: 14px !important;
  }
  
  .logout-dialog .el-message-box__btns {
    display: flex;
    justify-content: space-between;
  }
  
  .logout-dialog .el-button {
    flex: 1;
    margin: 0 5px;
  }
}

/* 退出登录对话框样式 */
.logout-dialog {
  max-width: 90% !important;
  width: 300px !important;
}

@media (max-width: 480px) {
  .logout-dialog {
    max-width: 95% !important;
    width: 280px !important;
  }
  
  .logout-dialog .el-message-box__content {
    font-size: 14px !important;
  }
  
  .logout-dialog .el-message-box__btns {
    display: flex;
    justify-content: space-between;
  }
  
  .logout-dialog .el-button {
    flex: 1;
    margin: 0 5px;
  }
}

/* 退出登录对话框样式 */
.logout-dialog {
  max-width: 90% !important;
  width: 300px !important;
}

@media (max-width: 480px) {
  .logout-dialog {
    max-width: 95% !important;
    width: 280px !important;
  }
  
  .logout-dialog .el-message-box__content {
    font-size: 14px !important;
  }
  
  .logout-dialog .el-message-box__btns {
    display: flex;
    justify-content: space-between;
  }
  
  .logout-dialog .el-button {
    flex: 1;
    margin: 0 5px;
  }
}

/* 退出登录对话框样式 */
.logout-dialog {
  max-width: 90% !important;
  width: 300px !important;
}

@media (max-width: 480px) {
  .logout-dialog {
    max-width: 95% !important;
    width: 280px !important;
  }
  
  .logout-dialog .el-message-box__content {
    font-size: 14px !important;
  }
  
  .logout-dialog .el-message-box__btns {
    display: flex;
    justify-content: space-between;
  }
  
  .logout-dialog .el-button {
    flex: 1;
    margin: 0 5px;
  }
}

/* 退出登录对话框样式 */
.logout-dialog {
  max-width: 90% !important;
  width: 300px !important;
}

@media (max-width: 480px) {
  .logout-dialog {
    max-width: 95% !important;
    width: 280px !important;
  }
  
  .logout-dialog .el-message-box__content {
    font-size: 14px !important;
  }
  
  .logout-dialog .el-message-box__btns {
    display: flex;
    justify-content: space-between;
  }
  
  .logout-dialog .el-button {
    flex: 1;
    margin: 0 5px;
  }
}

/* 退出登录对话框样式 */
.logout-dialog {
  max-width: 90% !important;
  width: 300px !important;
}

@media (max-width: 480px) {
  .logout-dialog {
    max-width: 95% !important;
    width: 280px !important;
  }
  
  .logout-dialog .el-message-box__content {
    font-size: 14px !important;
  }
  
  .logout-dialog .el-message-box__btns {
    display: flex;
    justify-content: space-between;
  }
  
  .logout-dialog .el-button {
    flex: 1;
    margin: 0 5px;
  }
}
</style>