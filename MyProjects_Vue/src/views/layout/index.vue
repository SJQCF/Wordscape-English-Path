<!--定义js，控制组件的行为--> 
<script setup> 
   import CommonButton from '@/views/button/common-button.vue'
   import { HomeFilled, More, StarFilled, UserFilled, ShoppingCart, Medal } from '@element-plus/icons-vue'
   import { RouterLink, useRoute, onBeforeRouteUpdate } from 'vue-router';
   import { ref, onMounted } from 'vue';
   import { useTheme } from '@/composables/useTheme';
   
   // 获取当前路由信息
   const route = useRoute();
   
   // 定义响应式变量存储当前选中的按钮文字
   const currentTitle = ref('学习');
   
   // 路由路径到中文名称的映射
   const routeToTitle = {
     '/study': '学习',
     '/shop': '商店',
     '/AI': 'AI',
     '/achievement': '成就',
     '/user': '我的',
     '/': '学习' // 添加根路径映射，默认显示"学习"
   };
   
   // 更新标题函数
   const updateTitle = (toPath = route.path) => {
     // 处理路径，确保能正确匹配
     const path = toPath.split('?')[0].split('#')[0].replace(/\/$/, '') || '/';
     // 查找对应的标题
     const title = routeToTitle[path];
     if (title) {
       currentTitle.value = title;
     }
   };
   
   // 初始化主题
   const { initTheme } = useTheme()
   
   // 组件挂载时初始化标题和主题
   onMounted(() => {
     updateTitle();
     initTheme();
   });
   
   // 路由切换前更新标题
   onBeforeRouteUpdate((to) => {
     updateTitle(to.path);
   });
   
   // 判断按钮是否激活的函数
   const isActive = (path) => {
     return route.path === path;
   };
</script> 
 
<!--定义html，控制组件的结构--> 
<template> 
  <div class="common-layout"> 
    <el-container> 
      <!-- 只有当路由不是/switch时显示头部 -->
      <el-header v-if="route.path !== '/switch'" class="header">{{ currentTitle }}</el-header> 
      <el-main>
        <router-view></router-view>
      </el-main> 
      <!-- 只有当路由不是/switch时显示底部导航 -->
      <el-footer v-if="route.path !== '/switch'" class="footer">
        <div class="footer-nav-container">
          <!-- 学习按钮 -->
          <div class="footer-item">
            <router-link to="/study" class="button-router-link">
              <common-button :button-props="{
                type: 'primary',
                plain: true,
                icon: HomeFilled,
                link: true,
                active: isActive('/study'),
                // 移除文字和圆圈轮廓
              }" />
            </router-link>
          </div>
          <!-- 商店按钮 -->
          <div class="footer-item">
            <router-link to="/shop" class="button-router-link">
              <common-button :button-props="{
                type: 'primary',
                plain: true,
                icon: ShoppingCart,
                link: true,
                active: isActive('/shop'),
                // 移除文字和圆圈轮廓
              }" />
            </router-link>
          </div>
          <!-- AI按钮 -->
          <div class="footer-item ai-item">
            <router-link to="/AI" class="button-router-link">
              <common-button :button-props="{
                type: 'primary',
                plain: true,
                text: 'AI',
                link: true,
                active: isActive('/AI'),
                // 移除图标，保留文字
              }" />
            </router-link>
          </div>
          <!-- 成就按钮 -->
          <div class="footer-item">
            <router-link to="/achievement" class="button-router-link">
              <common-button :button-props="{
                type: 'primary',
                plain: true,
                icon: Medal,
                link: true,
                active: isActive('/achievement'),
                // 移除文字和圆圈轮廓
              }" />
            </router-link>
          </div>
          <!-- 我的按钮 -->
          <div class="footer-item">
            <router-link to="/user" class="button-router-link">
              <common-button :button-props="{
                type: 'primary',
                plain: true,
                icon: UserFilled,
                link: true,
                active: isActive('/user'),
                // 移除文字和圆圈轮廓
              }" />
            </router-link>
          </div>
        </div>
      </el-footer> 
    </el-container> 
  </div> 
</template> 
 
<!--定义css，控制组件的样式--> 
<style scoped> 
/* 基础样式设置 - 确保不产生外部滑动栏 */
html, body {
  margin: 0;
  padding: 0;
  font-size: 16px;
  height: 100%;
  width: 100%;
  overflow: hidden !important; /* 防止页面整体滚动 */
}
 
/* 整体布局 */
.common-layout {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden !important; /* 防止页面整体滚动 */
}

/* 黑夜模式样式 */
html.dark .common-layout {
  background: linear-gradient(180deg, #1a1a1a 0%, #2d2d2d 50%, #121212 100%);
}

/* 黑夜模式下的底部导航 */
html.dark .footer {
  background: linear-gradient(180deg, rgba(30, 30, 30, 0.8) 0%, rgba(18, 18, 18, 1) 100%);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

/* 黑夜模式下的按钮 */
html.dark .footer-item:not(.ai-item) :deep(.el-button) {
  color: rgba(255, 255, 255, 0.6);
}

html.dark .footer-item.ai-item :deep(.el-button) {
  color: rgba(255, 255, 255, 0.6);
}

/* 黑夜模式下的激活按钮 */
html.dark .footer-item:not(.ai-item) :deep(.el-button.is-active) {
  color: #ffffff !important;
}

html.dark .footer-item.ai-item :deep(.el-button.is-active) {
  color: #ffffff !important;
}
 
/* 头部样式 */
.header {
  flex: 0 0 auto; /* 不允许头部伸缩 */
  height: 3%; /* 将高度调整为屏幕的3% */
  min-height: 28px; /* 设置最小高度，确保在小屏幕上也有足够的点击区域 */
  max-height: 36px; /* 设置最大高度，防止在大屏幕上过高 */
  background-color: var(--bg-primary); /* 使用主题变量 */
  color: var(--text-primary); /* 使用主题变量 */
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
  font-weight: bold;
  line-height: 1.5;
  box-sizing: border-box;
  padding: 0 1rem;
  /* 适配刘海屏 */
  padding-top: env(safe-area-inset-top);
  border-bottom: 1px solid var(--border-color); /* 使用主题变量 */
}
 
/* 主体内容 */
.el-main {
  flex: 1;
  padding: 0;
  overflow: hidden !important; /* 确保不产生外部滑动栏 */
  box-sizing: border-box;
  min-height: 0; /* 确保flex项目可以正确收缩 */
}
 
/* 底部导航 */
 .footer {
   display: flex;
   height: 10%; 
   background: linear-gradient(180deg, rgba(107, 140, 206, 0.8) 0%, rgba(107, 140, 206, 1) 100%); /* 更浅的蓝色 */
   padding: 0 0.5rem 0.2rem;
   box-sizing: border-box;
   align-items: center;
   justify-content: center;
   position: fixed; /* 固定在底部 */
   bottom: 0; /* 距离底部0 */
   left: 0; /* 距离左侧0 */
   right: 0; /* 距离右侧0 */
   overflow: hidden;
   width: 100%;
   z-index: 100; /* 确保在所有内容之上 */
   border-top: 1px solid rgba(255, 255, 255, 0.3); /* 半透明白色边框 */
 }
 
/* 底部导航项容器 */
 .footer-nav-container {
   display: flex;
   align-items: center;
   justify-content: space-around;
   width: 100%;
   max-width: 600px;
   height: 100%;
   padding-bottom: 0.2rem;
 }

/* 底部导航项 */
 .footer-item {
   display: flex;
   align-items: center;
   justify-content: center;
   margin: 0 0.5rem;
   height: 100%;
   flex: 1;
   max-width: 60px;
   transition: all 0.3s ease;
 }

/* RouterLink 样式 */
 .router-link,
 .button-router-link {
   display: flex;
   align-items: center;
   justify-content: center;
   height: 100%;
   width: 100%;
   flex: 1;
   text-decoration: none;
   color: inherit;
 }

/* 确保router-link不影响按钮样式 */
.button-router-link {
  padding: 0;
  margin: 0;
  background: none;
  border: none;
  cursor: pointer;
}

/* 确保router-link内部的按钮保持原有样式 */
.button-router-link > * {
  width: 100%;
  height: 100%;
}

/* 移除router-link的默认a标签样式 */
.button-router-link:link,
.button-router-link:visited,
.button-router-link:hover,
.button-router-link:active {
  text-decoration: none;
  color: inherit;
  transform: none;
}

/* 底部导航项点击效果 */
 .footer-item:active {
   transform: scale(0.95);
 }

/* 确保按钮适配底部空间 - 非AI按钮 */
 .footer-item:not(.ai-item) :deep(.el-button) {
   width: 90%;
   max-width: 75px;
   height: auto;
   aspect-ratio: 1 / 1;
   justify-content: center;
   display: flex;
   flex-direction: column;
   align-items: center;
   border-radius: 0; /* 去掉圆圈轮廓 */
   font-size: 1.7rem; /* 再略微增大字体大小 */
   padding: 0.5rem;
   margin: 0;
   min-width: 55px;
   max-height: calc(100% - 0.2rem);
   box-sizing: border-box;
   position: relative;
   bottom: 0;
   transition: all 0.3s ease;
   border: none;
   background: transparent;
   color: rgba(255, 255, 255, 0.7); /* 半透明白色 */
 }

/* AI按钮样式 */
.footer-item.ai-item :deep(.el-button) {
  width: 90%;
  max-width: 75px;
  height: auto;
  aspect-ratio: 1 / 1;
  justify-content: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-radius: 0;
  font-size: 1.1rem;
  padding: 0.5rem;
  margin: 0;
  min-width: 55px;
  max-height: calc(100% - 0.2rem);
  box-sizing: border-box;
  position: relative;
  bottom: 0;
  transition: all 0.3s ease;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.7); /* 半透明白色 */
}

 /* 非AI按钮激活状态样式 */
 .footer-item:not(.ai-item) :deep(.el-button.is-active) {
   background-color: transparent !important;
   color: #ffffff !important; /* 白色图标 */
   border-color: transparent !important;
   transform: scale(1.1); /* 图标放大 */
   box-shadow: none;
   filter: brightness(1.2); /* 高亮效果 */
 }

/* AI按钮激活状态样式 */
.footer-item.ai-item :deep(.el-button.is-active) {
  background-color: transparent !important;
  color: #ffffff !important; /* 白色文字 */
  border-color: transparent !important;
  font-weight: bold;
  transform: scale(1.05);
  box-shadow: none;
}

/* 按钮按下动画 */
.footer-item :deep(.el-button:active) {
  transform: scale(0.95);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .header {
    height: 5%; /* 在小屏幕上保持5%高度 */
    font-size: 0.9rem;
  }
  
  .footer {
    height: 7%; /* 在小屏幕上增加底部高度 */
  }


  .common-layout {
    height: 98vh;
  }
}
@media (max-width: 480px) {
   /* 手机设备保持5%高度 */
   .header {
     height: 5%;
   }
   
   .footer {
     height: 9%;
   }
   
   .el-main {
     height: 84%;
   }
   
   /* 调整按钮间距和大小 */
   .footer {
     padding: 0.3rem;
   }
   
   .footer-item {
     margin: 0 0.3rem;
   }
   
   .footer-item:not(.ai-item) :deep(.el-button) {
     max-width: 55px;
     font-size: 1.4rem; /* 略微增大手机端图标大小 */
   }
   
   .footer-item.ai-item :deep(.el-button) {
     font-size: 0.8rem;
     max-width: 50px;
   }
  }
   /* 超小屏幕优化 */
   @media (max-width: 320px) {
     .footer-item :deep(.el-button) {
       font-size: 0.5rem;
       max-width: 45px;
     }
   }
   
   /* 大屏幕手机优化（如iPhone 14 Pro Max） */
   @media (min-width: 428px) {
     /* 调小底部栏高度 */
     .footer {
       height: 8%;
     }
     
     .el-main {
       height: 82%;
     }
     
     /* 调整按钮大小以适应较小的底部栏 */
     .footer-item :deep(.el-button) {
       font-size: 0.6rem;
       max-width: 60px;
       min-width: 45px;
       max-height: calc(8vh - 0.4rem);
     }
   }
</style>