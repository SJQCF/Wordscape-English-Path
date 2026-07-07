<template>
  <div class="scene-dialog-container">
    <!-- 顶部导航 -->
    <div class="header">
      <el-button type="text" @click="goBack" class="back-button">
        <el-icon size="24"><ArrowLeft /></el-icon>
      </el-button>
      <h1 class="page-title">场景对话</h1>
      <div class="header-placeholder"></div>
    </div>
    
    <!-- 3D场景容器 -->
    <div class="scene-container" ref="sceneContainer"></div>
    
    <!-- 对话界面 -->
    <div class="dialogue-interface">
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
      
      <!-- 提示文字 -->
      <div class="hint-text">{{ hintText }}</div>
      
      <!-- 底部按钮 -->
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
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ArrowLeft, Mic, Close } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import * as THREE from 'three'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { startVoiceChat as apiStartVoiceChat, endVoiceChat as apiEndVoiceChat } from '@/api/ai'
import { prepareMicAudioContext, startBrowserVoicePipeline } from '@/utils/browserVoiceChat'

// 路由实例
const router = useRouter()
const route = useRoute()

// 场景容器引用
const sceneContainer = ref(null)

// 对话功能状态
const sessionId = ref('')
const isRecording = ref(false)
const loading = ref(false)
const sessionStatus = ref('idle') // idle, active, ended
const dialogueList = ref([])
const hintText = ref('点击开始按钮与服务员对话')

// Three.js 相关变量
let scene = null
let camera = null
let renderer = null
let animationId = null
let headGroup = null

// 触摸事件相关变量
let touchStartX = 0
let touchStartY = 0
let cameraRotationX = 0
let cameraRotationY = 0
const rotationSpeed = 0.01 // 旋转速度

/** 停止浏览器麦克风与语音 WebSocket */
let voicePipelineStop = null

// 返回按钮
const goBack = async () => {
  if (sessionId.value || voicePipelineStop) {
    await endVoiceChatInternal()
  }
  router.back()
}

const startVoiceSession = async () => {
  let mic = null
  try {
    loading.value = true
    // 必须在 await 后端之前完成麦克风 + AudioContext.resume，否则 Chrome 会保持 suspended，不上传任何 PCM
    hintText.value = '正在请求麦克风...'
    mic = await prepareMicAudioContext()

    hintText.value = '正在连接语音服务...'
    const sessionConfig = {
      mode: 'browser',
      botName: '服务员',
      systemRole: '你是一名酒店前台服务员，负责接待客人，提供入住、退房等服务。',
      speakingStyle: '友好、专业',
      speaker: 'zh_male_yunzhou_jupiter_bigtts'
    }

    const result = await apiStartVoiceChat(sessionConfig)
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
    hintText.value = '对话中（语音经服务器转发），点击结束停止'
    isRecording.value = true
  } catch (error) {
    console.error('开始语音会话失败:', error)
    hintText.value = error?.message || '无法启动语音，请检查 HTTPS、麦克风权限与网络'
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

const endVoiceChatInternal = async () => {
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
    const result = await apiEndVoiceChat(sid)
    if (result.code) {
      sessionStatus.value = 'ended'
      hintText.value = '语音会话已结束'
      if (result.data && result.data.response) {
        dialogueList.value.push({
          role: 'ai',
          content: result.data.response
        })
      }
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

const endVoiceChat = async () => {
  await endVoiceChatInternal()
}

// 切换录音状态
const toggleRecording = async () => {
  if (isRecording.value) {
    await endVoiceChat()
  } else {
    // 不添加录音提示，直接开始录音
    await startVoiceSession()
  }
}

// 结束会话
const endSession = async () => {
  await endVoiceChatInternal()
  sessionStatus.value = 'idle'
  dialogueList.value = []
  hintText.value = '点击开始按钮与服务员对话'
}



// 初始化3D场景
const initScene = () => {
  // 创建场景
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0xffffff) // 白色背景，减少与模型的对比

  // 创建相机
  camera = new THREE.PerspectiveCamera(
    55,
    sceneContainer.value.clientWidth / sceneContainer.value.clientHeight,
    0.1,
    1000
  )
  
  // 固定相机位置
  camera.position.set(0, -0.3, 1.8) // 调整位置，更高一些，更好地看到地板
  camera.lookAt(0, 0, 0) // 初始朝向，看向地板和吧台

  // 渲染器
  try {
    renderer = new THREE.WebGLRenderer({ 
      antialias: true,
      alpha: false,
      logarithmicDepthBuffer: true // 改善深度缓冲精度
    })
  } catch (error) {
    console.error('WebGL初始化失败:', error)
    // 显示错误信息
    const errorMessage = document.createElement('div')
    errorMessage.style.position = 'absolute'
    errorMessage.style.top = '50%'
    errorMessage.style.left = '50%'
    errorMessage.style.transform = 'translate(-50%, -50%)'
    errorMessage.style.padding = '20px'
    errorMessage.style.backgroundColor = '#fff'
    errorMessage.style.border = '1px solid #ccc'
    errorMessage.style.borderRadius = '8px'
    errorMessage.style.textAlign = 'center'
    errorMessage.style.maxWidth = '80%'
    errorMessage.innerHTML = `
      <h3>WebGL初始化失败</h3>
      <p>您的浏览器不支持WebGL或WebGL已被禁用。</p>
      <p>请尝试以下解决方案：</p>
      <ul style="text-align: left; margin: 10px 0;">
        <li>更新您的浏览器到最新版本</li>
        <li>启用浏览器的WebGL功能</li>
        <li>检查您的设备是否支持WebGL</li>
      </ul>
    `
    sceneContainer.value.appendChild(errorMessage)
    return
  }
  renderer.setSize(sceneContainer.value.clientWidth, sceneContainer.value.clientHeight)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2)) // 限制像素比，平衡质量和性能
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap // 更柔和的阴影
  // 移除sRGBEncoding以避免构建错误
  // renderer.outputEncoding = THREE.sRGBEncoding // 改善颜色显示
  renderer.toneMapping = THREE.ACESFilmicToneMapping // 电影级色调映射
  renderer.toneMappingExposure = 1.0
  sceneContainer.value.appendChild(renderer.domElement)

  // --- 灯光系统 ---
  // 环境光
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6) // 调整环境光强度
  scene.add(ambientLight)

  // 主方向光
  const mainLight = new THREE.DirectionalLight(0xffffff, 1.0) // 进一步降低亮度
  mainLight.position.set(5, 10, 15) // 调整灯光位置，减少直射
  mainLight.castShadow = true
  mainLight.shadow.mapSize.width = 4096 // 增加阴影贴图分辨率
  mainLight.shadow.mapSize.height = 4096
  mainLight.shadow.camera.near = 0.1
  mainLight.shadow.camera.far = 50
  scene.add(mainLight)
  


  // 补光
  const fillLight = new THREE.DirectionalLight(0xffffff, 0.7)
  fillLight.position.set(-5, 10, -5)
  scene.add(fillLight)

  // 正面光
  const frontLight = new THREE.DirectionalLight(0xffffff, 0.5)
  frontLight.position.set(0, 5, 10)
  scene.add(frontLight)

  // --- 地板 ---
  // 首先使用默认地板，确保场景中有地板
  const floorGeometry = new THREE.PlaneGeometry(30, 30)
  const floorMaterial = new THREE.MeshStandardMaterial({ 
    color: 0x4a5568, // 深灰色，更明显
    metalness: 0.1,
    roughness: 0.8
  })
  const floor = new THREE.Mesh(floorGeometry, floorMaterial)
  floor.rotation.x = -Math.PI / 2
  floor.position.y = -1.0 // 提高地板位置
  floor.receiveShadow = true
  scene.add(floor)
  console.log('添加默认地板')
  console.log('默认地板位置:', floor.position)
  console.log('默认地板旋转:', floor.rotation)
  
  // 尝试加载新的地板模型
  const floorLoader = new GLTFLoader()
  const floorPath = '/models/73a52053360823e8385079ad6bcac158.glb' // 使用目录中实际存在的地板模型文件
  
  console.log('尝试加载地板模型:', floorPath)
  
  floorLoader.load(
    floorPath,
    (floorGltf) => {
      console.log('地板模型加载成功:', floorPath)
      const newFloor = floorGltf.scene
      
      // 移除默认地板
      scene.remove(floor)
      console.log('移除默认地板')
      
      // 调整地板位置和缩放
      newFloor.position.set(0, -1.0, 0) // 提高地板位置
      newFloor.scale.set(5, 1.1, 10) // 增大地板尺寸
      newFloor.rotation.y = 0 // 调整旋转角度
      
      console.log('新地板位置:', newFloor.position)
      console.log('新地板缩放:', newFloor.scale)
      console.log('新地板旋转:', newFloor.rotation)
      
      // 启用阴影
      newFloor.receiveShadow = true
      newFloor.traverse((child) => {
        if (child.isMesh) {
          child.receiveShadow = true
          child.castShadow = false
          console.log('地板子对象:', child.name, '类型:', child.type)
        }
      })
      
      // 将地板添加到场景
      scene.add(newFloor)
      console.log('新地板添加到场景成功')
    },
    (xhr) => console.log('地板模型加载进度:', (xhr.loaded / xhr.total * 100) + '%'),
    (error) => {
      console.error('地板模型加载失败:', floorPath, error)
      console.log('继续使用默认地板')
    }
  )

  // --- 加载新的吧台模型 ---
  const loader = new GLTFLoader()
  const modelPath = '/models/f641dcc8da7a97f4ae02d5c5aff2bab7.glb'
  
  console.log('尝试加载模型:', modelPath)
  
  loader.load(
    modelPath,
    (gltf) => {
      console.log('模型加载成功:', modelPath)
      const barCounter = gltf.scene
      
      // 调整位置和缩放
        barCounter.position.set(0, -1, 0.5) // 调整模型位置，确保在地板上方
        barCounter.scale.set(1.5, 1.5, 1.5) // 增大模型尺寸
        console.log('模型位置:', barCounter.position)
        console.log('模型缩放后尺寸:', barCounter.scale)
      
      // 启用阴影并优化材质
      barCounter.castShadow = true
      barCounter.traverse((child) => {
        if (child.isMesh) {
          child.castShadow = true
          child.receiveShadow = true
          // 优化材质属性，减少条纹效果
          if (child.material) {
            if (Array.isArray(child.material)) {
              // 处理材质数组
              child.material.forEach(material => {
                if (material.isMeshStandardMaterial) {
                  material.metalness = 0.2
                  material.roughness = 0.7
                  material.flatShading = false
                  material.needsUpdate = true
                }
              })
            } else if (child.material.isMeshStandardMaterial) {
              // 处理单个材质
              child.material.metalness = 0.2
              child.material.roughness = 0.7
              child.material.flatShading = false
              child.material.needsUpdate = true
            }
          }
        }
      })
      
      // 调整模型旋转，确保正面朝向相机
      barCounter.rotation.y = 0 // 不旋转，保持原始朝向
      console.log('模型旋转:', barCounter.rotation)
      
      scene.add(barCounter)
      console.log('模型添加到场景成功')
          
          // 加载墙壁模型，围成一个房间
          const wallLoader = new GLTFLoader()
          const wallPath = '/models/c784621ed484d7846cb6b31c77a32f48.glb' // 墙壁模型路径
          
          console.log('尝试加载墙壁模型:', wallPath)
          
          wallLoader.load(
            wallPath,
            (wallGltf) => {
              console.log('墙壁模型加载成功:', wallPath)
              
              // 创建四个墙壁，每个墙壁可以单独调整大小和位置
              const walls = [
                {
                  // 前墙（朝向相机）
                  position: new THREE.Vector3(0, -1, -0.25), // X, Y, Z 位置
                  rotation: new THREE.Euler(0, 0, 0), // X, Y, Z 旋转
                  scale: new THREE.Vector3(3.5, 2, 1.5) // X, Y, Z 缩放
                },
                {
                  // 后墙
                  position: new THREE.Vector3(0, -1, 3), // X, Y, Z 位置
                  rotation: new THREE.Euler(0, Math.PI, 0), // X, Y, Z 旋转
                  scale: new THREE.Vector3(3.5, 2, 1.5) // X, Y, Z 缩放
                },
                {
                  // 左墙
                  position: new THREE.Vector3(-1.5, -1, 1), // X, Y, Z 位置
                  rotation: new THREE.Euler(0, Math.PI/2, 0), // X, Y, Z 旋转
                  scale: new THREE.Vector3(4.5, 2, 1.5) // X, Y, Z 缩放
                },
                {
                  // 右墙
                  position: new THREE.Vector3(1.5, -1, 1), // X, Y, Z 位置
                  rotation: new THREE.Euler(0, -Math.PI/2, 0), // X, Y, Z 旋转
                  scale: new THREE.Vector3(4.5, 2, 1.5) // X, Y, Z 缩放
                }
              ]
              
              // 添加每个墙壁到场景
              walls.forEach((wallConfig, index) => {
                const wall = wallGltf.scene.clone()
                wall.position.copy(wallConfig.position)
                wall.rotation.copy(wallConfig.rotation)
                wall.scale.copy(wallConfig.scale)
                
                // 启用阴影
                wall.castShadow = true
                wall.receiveShadow = true
                wall.traverse((child) => {
                  if (child.isMesh) {
                    child.castShadow = true
                    child.receiveShadow = true
                  }
                })
                
                scene.add(wall)
                console.log(`墙壁${index + 1}添加到场景成功`)
                console.log(`  位置:`, wall.position)
                console.log(`  旋转:`, wall.rotation)
                console.log(`  缩放:`, wall.scale)
              })
              
              console.log('\n=== 墙壁调整说明 ===')
              console.log('要调整墙壁的大小和位置，请修改上面walls数组中对应墙壁的参数:')
              console.log('1. position: new THREE.Vector3(X, Y, Z) - 调整墙壁位置')
              console.log('2. rotation: new THREE.Euler(X, Y, Z) - 调整墙壁旋转')
              console.log('3. scale: new THREE.Vector3(X, Y, Z) - 调整墙壁大小')
              console.log('\n例如：要把前墙变高，可以修改scale的Y值；要把左墙移近，可以修改position的X值')
              
              console.log('所有墙壁添加到场景成功')
              
              // 加载服务员模型
              const waiterLoader = new GLTFLoader()
              const waiterPath = '/models/c26ad99823a5cfa326b50c3810ccf695.glb' // 服务员模型路径
              
              console.log('尝试加载服务员模型:', waiterPath)
              
              waiterLoader.load(
                waiterPath,
                (waiterGltf) => {
                  console.log('服务员模型加载成功:', waiterPath)
                  const waiter = waiterGltf.scene
                  
                  // 调整服务员位置、大小和旋转
                  waiter.position.set(-0, -0.65, 0.35) // 放在吧台左侧
                  waiter.scale.set(0.5, 0.5, 0.5) // 调整合适的尺寸
                  
                  console.log('服务员位置:', waiter.position)
                  console.log('服务员缩放:', waiter.scale)
                  console.log('服务员旋转:', waiter.rotation)
                  
                  // 启用阴影
                  waiter.castShadow = true
                  waiter.receiveShadow = true
                  waiter.traverse((child) => {
                    if (child.isMesh) {
                      child.castShadow = true
                      child.receiveShadow = true
                    }
                  })
                  
                  // 将服务员添加到场景
                  scene.add(waiter)
                  console.log('服务员添加到场景成功')
                },
                (xhr) => console.log('服务员模型加载进度:', (xhr.loaded / xhr.total * 100) + '%'),
                (error) => {
                  console.error('服务员模型加载失败:', waiterPath, error)
                }
              )
            },
            (xhr) => console.log('墙壁模型加载进度:', (xhr.loaded / xhr.total * 100) + '%'),
            (error) => {
              console.error('墙壁模型加载失败:', wallPath, error)
            }
          )
    },
    (xhr) => console.log((xhr.loaded / xhr.total * 100) + '% loaded'),
    (error) => {
      console.error('模型加载失败:', modelPath, error)
    }
  )

  // 初始相机朝向
  cameraRotationX = 0 // 初始X轴旋转
  cameraRotationY = 0 // 初始Y轴旋转
  updateCameraRotation() // 更新相机旋转
  console.log('相机位置:', camera.position)
  console.log('初始相机旋转:', camera.rotation)

  // 开始动画
  animate()
}

// 更新相机旋转
const updateCameraRotation = () => {
  // 直接设置相机的旋转
  camera.rotation.y = cameraRotationY // 水平旋转
  camera.rotation.x = cameraRotationX // 垂直旋转
  
  // 限制X轴旋转范围，避免过度倾斜
  camera.rotation.x = Math.max(-Math.PI/2, Math.min(Math.PI/2, camera.rotation.x))
  
  //console.log('相机位置:', camera.position)
  //console.log('相机旋转:', camera.rotation)
  //console.log('旋转参数:', cameraRotationX, cameraRotationY)
}

// 动画循环
const animate = () => {
  animationId = requestAnimationFrame(animate)
  
  // 更新相机移动
  updateCameraMovement()
  
  renderer.render(scene, camera)
}

// 处理窗口大小变化
const handleResize = () => {
  if (!camera || !renderer || !sceneContainer.value) return
  
  camera.aspect = sceneContainer.value.clientWidth / sceneContainer.value.clientHeight
  camera.updateProjectionMatrix()
  renderer.setSize(sceneContainer.value.clientWidth, sceneContainer.value.clientHeight)
}

// 触摸/鼠标事件相关变量
let isDragging = false

// 键盘事件相关变量
const moveSpeed = 0.05 // 移动速度
let keysPressed = {} // 记录按下的键

// 处理触摸开始事件
const handleTouchStart = (e) => {
  e.preventDefault()
  const touch = e.touches[0]
  touchStartX = touch.clientX
  touchStartY = touch.clientY
  console.log('触摸开始:', touchStartX, touchStartY)
}

// 处理触摸移动事件
const handleTouchMove = (e) => {
  e.preventDefault()
  if (!e.touches[0]) return
  
  const touch = e.touches[0]
  const deltaX = touch.clientX - touchStartX
  const deltaY = touch.clientY - touchStartY
  
  console.log('触摸移动:', deltaX, deltaY)
  
  // 更新相机旋转 - 修正方向
  cameraRotationY += deltaX * rotationSpeed // 向右移动 → 相机向右旋转
  cameraRotationX += deltaY * rotationSpeed // 向上移动 → 相机向上旋转
  
  // 限制X轴旋转范围，避免过度倾斜
  cameraRotationX = Math.max(-Math.PI/2, Math.min(Math.PI/2, cameraRotationX))
  
  // 更新相机旋转
  updateCameraRotation()
  
  // 更新触摸起始位置
  touchStartX = touch.clientX
  touchStartY = touch.clientY
}

// 处理鼠标按下事件
const handleMouseDown = (e) => {
  isDragging = true
  touchStartX = e.clientX
  touchStartY = e.clientY
  console.log('鼠标按下:', touchStartX, touchStartY)
}

// 处理鼠标移动事件
const handleMouseMove = (e) => {
  if (!isDragging) return
  
  const deltaX = e.clientX - touchStartX
  const deltaY = e.clientY - touchStartY
  
  console.log('鼠标移动:', deltaX, deltaY)
  
  // 更新相机旋转 - 修正方向
  cameraRotationY += deltaX * rotationSpeed // 向右移动 → 相机向右旋转
  cameraRotationX += deltaY * rotationSpeed // 向上移动 → 相机向上旋转
  
  // 限制X轴旋转范围，避免过度倾斜
  cameraRotationX = Math.max(-Math.PI/2, Math.min(Math.PI/2, cameraRotationX))
  
  // 更新相机旋转
  updateCameraRotation()
  
  // 更新触摸起始位置
  touchStartX = e.clientX
  touchStartY = e.clientY
}

// 处理鼠标释放事件
const handleMouseUp = (e) => {
  isDragging = false
  console.log('鼠标释放')
}

// 处理键盘按下事件
const handleKeyDown = (e) => {
  keysPressed[e.key.toLowerCase()] = true
  console.log('键盘按下:', e.key.toLowerCase())
  console.log('当前按下的键:', keysPressed)
}

// 处理键盘释放事件
const handleKeyUp = (e) => {
  keysPressed[e.key.toLowerCase()] = false
  console.log('键盘释放:', e.key.toLowerCase())
  console.log('当前按下的键:', keysPressed)
}

// 更新键盘控制的相机移动
const updateCameraMovement = () => {
  if (!camera) return
  
  // 计算移动方向
  let moveForward = 0
  let moveSideways = 0
  
  if (keysPressed['w']) moveForward += moveSpeed
  if (keysPressed['s']) moveForward -= moveSpeed
  if (keysPressed['a']) moveSideways -= moveSpeed
  if (keysPressed['d']) moveSideways += moveSpeed
  
  console.log('移动方向 - 前/后:', moveForward, '左/右:', moveSideways)
  
  // 应用移动
  if (moveForward !== 0 || moveSideways !== 0) {
    console.log('应用移动')
    // 简单的移动实现，基于世界坐标系
    camera.position.z -= moveForward * moveSpeed * 10 // 向前/向后
    camera.position.x += moveSideways * moveSpeed * 10 // 向左/向右
    
    console.log('相机位置:', camera.position)
  }
}

// 组件挂载时初始化
onMounted(() => {
  // 检查路由参数中是否有会话ID
  const routeSessionId = route.query.sessionId
  if (routeSessionId) {
    sessionId.value = routeSessionId
    sessionStatus.value = 'active'
    isRecording.value = true // 设置为已开始状态，按钮显示"结束"
    hintText.value = '会话已创建，点击结束按钮可关闭会话'
  }
  
  initScene()
  window.addEventListener('resize', handleResize)
  
  // 添加键盘事件监听器
  window.addEventListener('keydown', handleKeyDown)
  window.addEventListener('keyup', handleKeyUp)
  
  // 添加触摸事件监听器
  setTimeout(() => {
    const container = sceneContainer.value
    if (container) {
      console.log('添加触摸事件监听器')
      container.addEventListener('touchstart', handleTouchStart, { passive: false })
      container.addEventListener('touchmove', handleTouchMove, { passive: false })
      // 同时添加鼠标事件，方便桌面测试
      container.addEventListener('mousedown', handleMouseDown)
      container.addEventListener('mousemove', handleMouseMove)
      container.addEventListener('mouseup', handleMouseUp)
    }
  }, 100)
})

// 组件卸载时清理
onUnmounted(() => {
  if (voicePipelineStop) {
    try {
      voicePipelineStop()
    } catch (e) {
      /* ignore */
    }
    voicePipelineStop = null
  }
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', handleResize)
  
  // 移除键盘事件监听器
  window.removeEventListener('keydown', handleKeyDown)
  window.removeEventListener('keyup', handleKeyUp)
  
  // 移除触摸和鼠标事件监听器
  const container = sceneContainer.value
  if (container) {
    container.removeEventListener('touchstart', handleTouchStart)
    container.removeEventListener('touchmove', handleTouchMove)
    container.removeEventListener('mousedown', handleMouseDown)
    container.removeEventListener('mousemove', handleMouseMove)
    container.removeEventListener('mouseup', handleMouseUp)
  }
  
  if (renderer) {
    renderer.dispose()
  }
})
</script>

<style scoped>
/* 主容器 */
.scene-dialog-container {
  width: 100%;
  height: 100vh;
  box-sizing: border-box;
  background-color: var(--bg-primary);
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

/* 顶部导航 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: calc(env(safe-area-inset-top) + 0.5rem) 1rem 1rem;
  background-color: var(--bg-primary);
  box-shadow: none;
  transition: all 0.3s ease;
}

.page-title {
  font-size: 1.2rem;
  font-weight: bold;
  color: var(--text-primary);
  margin: 0;
  transition: all 0.3s ease;
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
  transition: all 0.3s ease;
}

.header-placeholder {
  width: 40px;
}

/* 3D场景容器 */
.scene-container {
  flex: 1;
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  margin: 0 1rem;
}

/* 对话界面 */
.dialogue-interface {
  background-color: rgba(255, 255, 255, 0.95);
  border-top-left-radius: 20px;
  border-top-right-radius: 20px;
  padding: 1.5rem;
  padding-bottom: calc(1.5rem + env(safe-area-inset-bottom));
  box-shadow: 0 -5px 20px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  animation: slideUp 0.3s ease;
}

/* 对话容器 */
.dialogue-container {
  width: 100%;
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 1rem;
  padding: 1rem;
  background-color: rgba(240, 242, 245, 0.8);
  border-radius: 12px;
}

.dialogue-item {
  margin-bottom: 0.8rem;
  max-width: 80%;
  padding: 0.8rem;
  border-radius: 12px;
  animation: slideIn 0.3s ease;
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
  background-color: white;
  border: 1px solid var(--border-color);
  border-bottom-left-radius: 4px;
}

.dialogue-content {
  font-size: 0.9rem;
  line-height: 1.4;
}

/* 提示文字 */
.hint-text {
  font-size: 1rem;
  color: var(--text-secondary);
  text-align: center;
  margin-bottom: 1.5rem;
}

/* 底部按钮区域 */
.bottom-buttons {
  display: flex;
  justify-content: center;
  gap: 2rem;
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

.mic-button {
  width: 70px;
  height: 70px;
  background-color: var(--primary-color);
  border: none;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
  transition: all 0.3s ease;
}

.cancel-button {
  width: 70px;
  height: 70px;
  background-color: #f56c6c;
  border: none;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
  transition: all 0.3s ease;
}

/* 动画 */
@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
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

/* 响应式设计 */
@media (max-width: 480px) {
  .scene-container {
    margin: 0 0.8rem;
  }
  
  .dialogue-interface {
    padding: 1rem;
    padding-bottom: calc(1rem + env(safe-area-inset-bottom));
  }
  
  .mic-button,
  .cancel-button {
    width: 60px;
    height: 60px;
  }
  
  .bottom-buttons {
    gap: 1.5rem;
  }
}
</style>