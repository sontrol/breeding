<template>
  <div class="chat-container">
    <el-card class="chat-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Monitor /></el-icon> DeepSeek 智能养殖助手</span>
          <el-tag type="info" size="small">基于RBAC严格数据隔离</el-tag>
        </div>
      </template>

      <div class="chat-window" ref="chatWindow">
        <div v-for="(msg, index) in messageList" :key="index" :class="['message-item', msg.role === 'user' ? 'is-user' : 'is-ai']">
          <div class="avatar">
            <el-avatar :icon="msg.role === 'user' ? 'UserFilled' : 'Platform'" :class="msg.role" />
          </div>
          <div class="content">
            <div class="text" v-html="formatContent(msg.content)"></div>
          </div>
        </div>
        <div v-if="loading" class="message-item is-ai">
          <div class="avatar"><el-avatar icon="Platform" class="ai" /></div>
          <div class="content">
            <div class="text typing">正在分析您的权限和数据，请稍候...</div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="请向AI提问（如：目前有多少头动物？疫苗库存情况如何？）"
          @keyup.enter.native.prevent="sendMessage"
        />
        <el-button type="primary" class="send-btn" :loading="loading" @click="sendMessage" icon="Position">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

interface Message {
  role: 'user' | 'ai'
  content: string
}

const inputMessage = ref('')
const loading = ref(false)
const chatWindow = ref<HTMLElement | null>(null)
const messageList = ref<Message[]>([
  { role: 'ai', content: '您好！我是基于DeepSeek的智能养殖助手。我只能访问并为您解答您拥有权限的数据信息。请问有什么我可以帮您的？' }
])

const scrollToBottom = () => {
  nextTick(() => {
    if (chatWindow.value) {
      chatWindow.value.scrollTop = chatWindow.value.scrollHeight
    }
  })
}

const formatContent = (content: string) => {
  return content.replace(/\n/g, '<br/>')
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  
  const currentMsg = inputMessage.value
  messageList.value.push({ role: 'user', content: currentMsg })
  inputMessage.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const res: any = await request.post('/ai/chat', { message: currentMsg })
    if (res.code === 200) {
      messageList.value.push({ role: 'ai', content: res.data })
    } else {
      messageList.value.push({ role: 'ai', content: `[系统提示] ${res.msg}` })
    }
  } catch (error: any) {
    messageList.value.push({ role: 'ai', content: '[系统提示] 网络或接口请求异常，请检查后端服务。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.chat-container {
  padding: 20px;
  height: calc(100vh - 100px);
  box-sizing: border-box;
}
.chat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}
:deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
.chat-window {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f7fa;
}
.message-item {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}
.message-item.is-user {
  flex-direction: row-reverse;
}
.avatar {
  margin: 0 15px;
}
.avatar .user {
  background-color: #409EFF;
}
.avatar .ai {
  background-color: #67C23A;
}
.content .text {
  max-width: 600px;
  padding: 12px 16px;
  border-radius: 8px;
  line-height: 1.5;
  word-wrap: break-word;
}
.is-user .content .text {
  background-color: #409EFF;
  color: #fff;
  border-top-right-radius: 2px;
}
.is-ai .content .text {
  background-color: #fff;
  color: #303133;
  border-top-left-radius: 2px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
.typing {
  color: #909399 !important;
  font-style: italic;
}
.input-area {
  padding: 15px;
  background-color: #fff;
  border-top: 1px solid #ebeef5;
  display: flex;
  align-items: flex-end;
  gap: 15px;
}
.send-btn {
  height: 75px;
  width: 100px;
}
</style>
