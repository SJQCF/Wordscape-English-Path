import { createRouter, createWebHistory } from 'vue-router' 
 import StudyView from '@/views/study/index.vue' 
 import AIView from '@/views/AI/index.vue' 
 import AchievementView from '@/views/achievement/index.vue' 
 import UserView from '@/views/user/index.vue' 
 import LoginView from '@/views/login/index.vue' 
 import ShopView from '@/views/shop/index.vue'
// 导入token相关方法
import { getToken } from '@/utils/axios.js' 
// 导入LayoutView组件
import LayoutView from '@/views/layout/index.vue'
// 导入SwitchView组件
import SwitchView from '@/views/switch/index.vue'
// 导入CalendarView组件
import CalendarView from '@/views/calendar/index.vue'
// 导入BooksView组件
import BooksView from '@/views/books/index.vue'
// 导入FavoritesView组件
import FavoritesView from '@/views/favorites/index.vue'
// 导入CourseView组件
import CourseView from '@/views/course/index.vue'
// 导入ShowWordView组件
import ShowWordView from '@/views/showWord/index.vue'
// 导入FavoriteWordsView组件
import FavoriteWordsView from '@/views/favoriteWords/index.vue'
// 导入BookDetailView组件
import BookDetailView from '@/views/book/index.vue'
// 导入AllSudiedView组件
import AllSudiedView from '@/views/allSudied/index.vue'
// 导入InputWordView组件
import InputWordView from '@/views/inputWord/index.vue'
// 导入StudyPlanView组件
import StudyPlanView from '@/views/studyPlan/index.vue'
// 导入CourseDetailView组件
import CourseDetailView from '@/views/courseDetail/index.vue'
// 导入MyCoursesView组件
import MyCoursesView from '@/views/myCourses/index.vue'
// 导入CoursePlayerView组件
import CoursePlayerView from '@/views/coursePlayer/index.vue'
// 导入AIImageView组件
import AIImageView from '@/views/aiImage/index.vue'
// 导入AIWritingView组件
import AIWritingView from '@/views/aiWriting/index.vue'
// 导入AIResultView组件
import AIResultView from '@/views/aiResult/index.vue'
// 导入SearchView组件
import SearchView from '@/views/search/index.vue'
// 导入AISpeakingView组件
import AISpeakingView from '@/views/aiSpeaking/index.vue'
// 导入DarkModeView组件
import DarkModeView from '@/views/darkMode/index.vue'
// 导入ListeningView组件
import ListeningView from '@/views/listening/index.vue'
// 导入ListeningDetailView组件
import ListeningDetailView from '@/views/listening/detail.vue'
// 导入AITranslateView组件
import AITranslateView from '@/views/aiTranslate/index.vue'
// 导入AIQuestionRecognitionView组件
import AIQuestionRecognitionView from '@/views/aiQuestionRecognition/index.vue'
// 导入SceneDialogView组件
import SceneDialogView from '@/views/sceneDialog/index.vue'
// 导入AIChatView组件
import AIChatView from '@/views/aiChat/index.vue'

const router = createRouter({ 
  history: createWebHistory(import.meta.env.BASE_URL), 
  routes: [ 
    { 
      path: '/', 
      name: 'home', 
      component: LayoutView, 
      redirect: '/study', // 添加重定向，默认进入学习页面
      children:[ 
        {path: '/study',name: 'study',component: StudyView}, 
        {path: '/shop',name: 'shop',component: ShopView}, 
        {path: '/AI',name: 'AI',component: AIView}, 
        {path: '/achievement',name: 'achievement',component: AchievementView}, 
        {path: '/user',name: 'user',component: UserView}, 
        { path: '/switch',name: 'switch',component: SwitchView}, 
  ] 
},
// 全屏页面路由（不包含在Layout中）
{ path: '/calendar',name: 'calendar',component: CalendarView},
{ path: '/books',name: 'books',component: BooksView},
{ path: '/favorites',name: 'favorites',component: FavoritesView},
{ path: '/course',name: 'course',component: CourseView},
{ path: '/showWord',name: 'showWord',component: ShowWordView},
{ path: '/favoriteWords',name: 'favoriteWords',component: FavoriteWordsView},
{ path: '/book/:id',name: 'bookDetail',component: BookDetailView},
{ path: '/allSudied',name: 'allSudied',component: AllSudiedView},
{ path: '/inputWord',name: 'inputWord',component: InputWordView},
{ path: '/studyPlan',name: 'studyPlan',component: StudyPlanView},
{ path: '/course/:id',name: 'courseDetail',component: CourseDetailView},
{ path: '/myCourses',name: 'myCourses',component: MyCoursesView},
{ path: '/coursePlayer/:id',name: 'coursePlayer',component: CoursePlayerView},
{ path: '/aiImage',name: 'aiImage',component: AIImageView},
{ path: '/aiWriting',name: 'aiWriting',component: AIWritingView},
{ path: '/aiResult',name: 'aiResult',component: AIResultView},
{ path: '/search',name: 'search',component: SearchView},
{ path: '/aiSpeaking',name: 'aiSpeaking',component: AISpeakingView},
{ path: '/darkMode',name: 'darkMode',component: DarkModeView},
{ path: '/listening',name: 'listening',component: ListeningView},
{ path: '/listening/:id',name: 'listeningDetail',component: ListeningDetailView},
{ path: '/aiTranslate',name: 'aiTranslate',component: AITranslateView},
{ path: '/aiQuestionRecognition',name: 'aiQuestionRecognition',component: AIQuestionRecognitionView},
{ path: '/sceneDialog',name: 'sceneDialog',component: SceneDialogView},
{ path: '/aiChat',name: 'aiChat',component: AIChatView},
    { 
      path: '/login', 
      name: 'login', 
      component: LoginView, 
      meta: { requiresAuth: false } // 添加meta信息
    }, 
    { 
      path: '/:pathMatch(.*)*', // 404路由
      redirect: '/' // 重定向到首页
    } ,
    {
  path: '/similarQuestions',
  name: 'similarQuestions',
  component: () => import('../views/similarQuestions/index.vue')
}
  ] 
 })

// 路由守卫
router.beforeEach((to, from, next) => {
  // 检查是否需要认证
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)
  
  // 获取token
  const token = getToken()
  
  // 如果需要认证且没有token，跳转到登录页
  if (requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
