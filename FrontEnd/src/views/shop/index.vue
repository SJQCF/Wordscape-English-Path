<template>
  <div class="shop-container">
    <div class="shop-header">
      <h2>商店</h2>
    </div>
    <div class="shop-content">
      <div class="shop-items">
        <!-- 学习资料 - 可点击的卡片 -->
        <router-link to="/books?from=shop" class="shop-item-link">
          <div class="shop-item">
            <div class="item-icon">
              <span class="emoji-icon">📚</span>
            </div>
            <div class="item-info">
              <h3>学习资料</h3>
              <p>丰富的英语学习资料</p>
            </div>
          </div>
        </router-link>
        
        <!-- 高级课程 -->
        <router-link to="/course" class="shop-item-link">
          <div class="shop-item">
            <div class="item-icon">
              <span class="emoji-icon">🎓</span>
            </div>
            <div class="item-info">
              <h3>高级课程</h3>
              <p>专业的英语课程</p>
            </div>
          </div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useTheme } from '@/composables/useTheme'

// 初始化主题
const { initTheme } = useTheme()

// 组件挂载时初始化主题
import { onMounted } from 'vue'
onMounted(() => {
  initTheme()
})
</script>

<style scoped>
.shop-container {
  width: 100%;
  padding: 1rem;
  box-sizing: border-box;
  min-height: 100vh;
  background: linear-gradient(180deg, #ffffff 0%, #e6f0ff 50%, #4a6fa5 100%);
  color: var(--text-primary);
  transition: all 0.3s ease;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 3rem; /* 为底部导航栏留出空间 */
  height: 100vh;
  margin: 0;
}

/* 黑夜模式样式 */
html.dark .shop-container {
  background: linear-gradient(180deg, #1a1a1a 0%, #2d2d2d 50%, #121212 100%);
  color: var(--text-primary);
}

.shop-header {
  text-align: center;
  margin-bottom: 2rem;
  margin-top: 80px;
}

.shop-header h2 {
  margin: 0;
  color: var(--primary-color);
  font-size: 1.5rem;
  font-weight: bold;
}

.shop-content {
  width: 100%;
  box-sizing: border-box;
}

.shop-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 16px;
  margin: 0 auto;
  width: 100%;
  max-width: 500px;
  box-sizing: border-box;
}

.shop-item {
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
}

.shop-item:active {
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

.item-info p {
  margin: 0 0 0.5rem;
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

/* 路由链接样式 - 确保与普通按钮保持一致 */
.shop-item-link {
  display: block;
  text-decoration: none;
  color: inherit;
  width: 100%;
  box-sizing: border-box;
  opacity: 0;
  transform: translateY(30px);
  animation: shopItemFadeIn 0.5s ease-out forwards;
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
}

/* 响应式设计 - 超小屏手机 */
@media (max-width: 320px) {
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
}

/* 动画效果 */
@keyframes shopItemFadeIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 商店项目动画延迟 */
.shop-item-link {
  animation: shopItemFadeIn 0.5s ease-out forwards;
}

.shop-item-link:nth-child(1) {
  animation-delay: 0.1s;
}

.shop-item-link:nth-child(2) {
  animation-delay: 0.2s;
}
</style>