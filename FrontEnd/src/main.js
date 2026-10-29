import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/theme.css'
import { useTheme } from './composables/useTheme'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
// import './assets/main.css'

const app = createApp(App)

// 初始化主题
const { initTheme } = useTheme()
initTheme()

app.use(ElementPlus)
app.use(router)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')