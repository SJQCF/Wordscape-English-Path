<template>
  <!-- 如果没有传入任何按钮属性，显示完整示例 -->
  <div v-if="!buttonProps" class="button-example">
    <div class="button-row">
      <el-button>Default</el-button>
      <el-button type="primary">Primary</el-button>
      <el-button type="success">Success</el-button>
      <el-button type="info">Info</el-button>
      <el-button type="warning">Warning</el-button>
      <el-button type="danger">Danger</el-button>
    </div>

    <div class="button-row">
      <el-button plain>Plain</el-button>
      <el-button type="primary" plain>Primary</el-button>
      <el-button type="success" plain>Success</el-button>
      <el-button type="info" plain>Info</el-button>
      <el-button type="warning" plain>Warning</el-button>
      <el-button type="danger" plain>Danger</el-button>
    </div>

    <div class="button-row">
      <el-button round>Round</el-button>
      <el-button type="primary" round>Primary</el-button>
      <el-button type="success" round>Success</el-button>
      <el-button type="info" round>Info</el-button>
      <el-button type="warning" round>Warning</el-button>
      <el-button type="danger" round>Danger</el-button>
    </div>

    <div class="button-row">
      <el-button :icon="Search" circle />
      <el-button type="primary" :icon="Edit" circle />
      <el-button type="success" :icon="Check" circle />
      <el-button type="info" :icon="Message" circle />
      <el-button type="warning" :icon="Star" circle />
      <el-button type="danger" :icon="Delete" circle />
    </div>
  </div>
  <!-- 如果传入了按钮属性，只显示一个按钮 -->
  <el-button 
    v-else 
    :type="buttonProps.type" 
    :plain="buttonProps.plain" 
    :round="buttonProps.round" 
    :circle="buttonProps.circle" 
    :icon="buttonProps.icon" 
    :size="buttonProps.size" 
    :disabled="buttonProps.disabled" 
    :class="{ 'is-active': buttonProps.active }" 
    @click="$emit('click')"
  >
    {{ buttonProps.text }}
  </el-button>
</template>

<script lang="ts" setup>
import {
  Check,
  Delete,
  Edit,
  Message,
  Search,
  Star,
} from '@element-plus/icons-vue'

// 定义按钮属性类型
interface ButtonProps {
  type?: 'default' | 'primary' | 'success' | 'info' | 'warning' | 'danger'
  plain?: boolean
  round?: boolean
  circle?: boolean
  icon?: any
  text?: string
  size?: 'large' | 'default' | 'small'
  disabled?: boolean
  active?: boolean // 添加激活状态属性
}

// 接收按钮属性
const props = defineProps<{
  buttonProps?: ButtonProps
}>()

// 定义事件
defineEmits<{
  (e: 'click'): void
}>()
</script>

<style scoped>
.button-example {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.button-row {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: center;
}

.button-row > * {
  margin: 0;
}
</style>