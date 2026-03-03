<template>
  <div class="control-container">
    <div class="main-content">
      <!-- Left: Video Area (Optional) -->
      <div v-show="showVideo" class="video-section">
        <div class="video-placeholder">
          <div class="live-tag">LIVE</div>
          <p>直播画面预览 (房间: {{ roomId }})</p>
          <p class="sub-text">此处接入流媒体播放器</p>
        </div>
        <div class="control-bar">
          <el-button type="danger" plain size="small">断开连接</el-button>
          <el-button type="warning" plain size="small">禁言全员</el-button>
          <div class="stats">
            <span>在线: {{ onlineCount }}</span>
            <span>热度: {{ hotValue }}</span>
          </div>
        </div>
      </div>

      <!-- Right: Chat Area (Vertical Scroll) -->
      <div class="chat-section" :class="{ 'full-width': !showVideo }">
        <div class="chat-header">
          <div class="header-left">
            <span>实时弹幕</span>
            <el-switch
              v-model="showVideo"
              active-text="显示画面"
              inactive-text="隐藏画面"
              inline-prompt
              style="margin-left: 15px"
            />
          </div>
          <div class="header-actions">
            <el-button size="small" @click="goSessions" type="primary" plain>会话时长</el-button>
            <el-button size="small" @click="goLogs" plain>操作日志</el-button>
            <!-- <el-button 
              :type="isSimulating ? 'danger' : 'success'" 
              size="small" 
              @click="toggleSimulation"
            >
              {{ isSimulating ? '停止模拟' : '开始模拟' }}
            </el-button> -->
            <el-button link type="primary" size="small" @click="clearChat">清屏</el-button>
          </div>
        </div>

        <!-- Filter Bar -->
        <div class="filter-bar">
          <div class="filter-tabs">
            <div 
              class="filter-tab" 
              :class="{ active: activeTab === 'all' }"
              @click="activeTab = 'all'"
            >
              全部
            </div>
            <div 
              class="filter-tab" 
              :class="{ active: activeTab === 'chat' }"
              @click="activeTab = 'chat'"
            >
              聊天
            </div>
            <div 
              class="filter-tab" 
              :class="{ active: activeTab === 'gift' }"
              @click="activeTab = 'gift'"
            >
              礼物
            </div>
            <div 
              class="filter-tab" 
              :class="{ active: activeTab === 'join' }"
              @click="activeTab = 'join'"
            >
              进场
            </div>
            <div 
              class="filter-tab" 
              :class="{ active: activeTab === 'interaction' }"
              @click="activeTab = 'interaction'"
            >
              互动
            </div>
          </div>
        </div>
        
        <div 
          class="chat-list" 
          ref="chatListRef" 
          @scroll="handleScroll"
          @wheel="handleWheel"
          @touchstart="handleWheel({ deltaY: -1 })" 
        >
          <div 
            v-for="(msg, index) in filteredMessages" 
            :key="msg.id || index" 
            class="chat-item"
            :class="{ 'active': activeMsgId === msg.id }"
            @click="toggleSelected(msg.id)"
          >
            <!-- Time -->
            <span class="msg-time">{{ formatTime(msg.timestamp) }}</span>
            
            <!-- Message Type Badge/Icon -->
            <span class="msg-type-badge" :class="msg.type">{{ getMsgTypeLabel(msg.type) }}</span>
            
            <!-- User Name -->
            <span v-if="msg.userId" class="user-name">{{ msg.userId }}:</span>
            
            <!-- Content -->
            <span class="message-content" :class="msg.type">
              {{ formatContent(msg) }}
            </span>

            <!-- Action Bar (Expandable) -->
            <div v-if="activeMsgId === msg.id" class="msg-action-bar" @click.stop>
              <el-button 
                size="small" 
                :type="isVoiceReplying && activeMsgId === msg.id ? 'danger' : 'primary'" 
                round 
                @click="handleVoiceReply(msg)"
              >
                {{ isVoiceReplying && activeMsgId === msg.id ? '⏹ 停止回复' : '🎤 语音回复' }}
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- New Message Notification -->
        <div v-if="!isAtBottom && unreadCount > 0" class="new-msg-toast" @click="scrollToBottom">
          ⬇️ {{ unreadCount > 99 ? '99+' : unreadCount }} 条新消息
        </div>

        <div class="chat-input-area">
          <el-input 
            v-model="inputMessage" 
            placeholder="发送弹幕..." 
            @keyup.enter="sendMessage"
            class="msg-input"
          >
            <template #prepend>
              <el-popover
                placement="top-start"
                :width="300"
                trigger="click"
                popper-class="emoji-popover"
              >
                <template #reference>
                  <el-button class="emoji-btn">
                    <span style="font-size: 18px;">😊</span>
                  </el-button>
                </template>
                <div class="emoji-grid">
                  <span 
                    v-for="emoji in emojiList" 
                    :key="emoji" 
                    class="emoji-item"
                    @click="insertEmoji(emoji)"
                  >
                    {{ emoji }}
                  </span>
                </div>
              </el-popover>
            </template>
            <template #append>
              <el-button @click="sendMessage">发送</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import zegoClient from '@/utils/zegoClient'

// Zego Configuration (To be filled by user)
const ZEGO_APP_ID = 1556046237 // 填写你的 AppID
const ZEGO_SERVER_URL = "wss://accesshub-wss.zego.im/accesshub" // 填写你的 Server URL
const ZEGO_TOKEN = "" // 填写你的 Token (测试时可临时生成，生产环境需后台生成)

const router = useRouter()
const roomId = ref(localStorage.getItem('stream_room') || '1001')
const userId = ref(localStorage.getItem('stream_user') || 'admin')
const token = localStorage.getItem('stream_token')
const messages = ref([])
const activeMsgId = ref(null)
const inputMessage = ref('')
const chatListRef = ref(null)
const ws = ref(null)
const onlineCount = ref(0)
const hotValue = ref(0)
const isSimulating = ref(false)
let simulationTimer = null

// Scroll State
const isAtBottom = ref(true)
const unreadCount = ref(0)

// New States
const showVideo = ref(false) // Default hidden as requested
const activeTab = ref('all')
const isVoiceReplying = ref(false)
const currentReplyStreamID = ref(null)

// Zego Init
const initZego = async () => {
  if (!ZEGO_APP_ID || !ZEGO_SERVER_URL) {
    console.warn('Zego AppID or Server URL not configured')
    return
  }
  try {
    await zegoClient.init(ZEGO_APP_ID, ZEGO_SERVER_URL)
    if (ZEGO_TOKEN) {
        await zegoClient.loginRoom(roomId.value, ZEGO_TOKEN, { userID: userId.value, userName: userId.value })
    }
  } catch (e) {
    console.error('Zego init failed', e)
    // ElMessage.error('直播SDK初始化失败，请检查配置') // Optional: suppress if just testing UI
  }
}

// Emoji List
const emojiList = [
  '😀', '😁', '😂', '🤣', '😃', '😄', '😅', '😆', '😉', '😊', '😋', '😎',
  '😍', '😘', '🥰', '😗', '😙', '😚', '🙂', '🤗', '🤩', '🤔', '🤨', '😐',
  '😑', '😶', '🙄', '😏', '😣', '😥', '😮', '🤐', '😯', '😪', '😫', '😴',
  '😌', '😛', '😜', '😝', '🤤', '😒', '😓', '😔', '😕', '🙃', '🤑', '😲',
  '👍', '👎', '👊', '✊', '🤛', '🤜', '🤞', '✌️', '🤟', '🤘', '👌', '👈',
  '👉', '👆', '👇', '☝️', '✋', '🤚', '🖐', '🖖', '👋', '🤙', '💪', '🙏',
  '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔', '❣️', '💕',
  '💞', '💓', '💗', '💖', '💘', '💝', '🔥', '✨', '🌟', '💫', '💥', '💢',
  '💦', '💧', '💤', '👋', '🤚', '🖐', '✋', '🖖', '👌', '🤌', '🤏', '✌️',
  '🤞', '🤟', '🤘', '🤙', '👈', '👉', '👆', '🖕', '👇', '☝️', '👍', '👎',
  '✊', '👊', '🤛', '🤜', '👏', '🙌', '👐', '🤲', '🤝', '🙏', '✍️', '💅',
  '🤳', '💪', '🦾', '🦵', '🦿', '🦶', '👣', '👂', '🦻', '👃', '🫀', '🫁',
  '🧠', '🦷', '🦴', '👀', '👁', '👅', '👄'
]

// Computed
const filteredMessages = computed(() => {
  // Always show system messages
  if (activeTab.value === 'all') {
    return messages.value
  }
  
  return messages.value.filter(m => {
    if (m.type === 'system') return true
    
    if (activeTab.value === 'interaction') {
      return m.type === 'share' || m.type === 'like'
    }
    
    return m.type === activeTab.value
  })
})

// Helpers
const getMsgTypeLabel = (type) => {
  const map = {
    join: '来了',
    chat: '说',
    gift: '送礼',
    share: '分享',
    like: '点赞',
    system: '系统'
  }
  return map[type] || '消息'
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const h = date.getHours().toString().padStart(2, '0')
  const m = date.getMinutes().toString().padStart(2, '0')
  const s = date.getSeconds().toString().padStart(2, '0')
  return `${h}:${m}:${s}`
}

const formatContent = (msg) => {
  if (msg.type === 'join') return '进入了直播间'
  if (msg.type === 'like') return '点赞了主播'
  if (msg.type === 'share') return '分享了直播间'
  if (msg.type === 'gift') return `送出了 ${msg.content}`
  return msg.content
}

const goSessions = () => {
  router.push('/sessions')
}
const goLogs = () => {
  router.push('/logs')
}

const insertEmoji = (emoji) => {
  inputMessage.value += emoji
}

const connectWebSocket = () => {
  if (!token) {
    ElMessage.error('未登录')
    router.push('/login')
    return
  }

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  const url = `${protocol}//${host}/ws?token=${encodeURIComponent(token)}&roomId=${roomId.value}&deviceId=control_panel_${Date.now()}`

  ws.value = new WebSocket(url)

  ws.value.onopen = () => {
    console.log('WS Connected')
    ElMessage.success('连接直播间成功')
    addSystemMessage('已连接到直播间 ' + roomId.value)
  }

  ws.value.onmessage = (event) => {
    console.log('WS Message Received (Raw):', event.data)
    try {
      const data = JSON.parse(event.data)
      console.log('Parsed WS Data:', data)
      handleMessage(data)
    } catch (e) {
      console.error('Parse error', e)
    }
  }

  ws.value.onclose = () => {
    console.log('WS Closed')
    addSystemMessage('连接已断开')
  }

  ws.value.onerror = (err) => {
    console.error('WS Error', err)
    ElMessage.error('连接发生错误')
  }
}

const handleMessage = (data) => {

  // Handle different message types based on C++ logic structure
  if (data.type && data.subType) {
    handleBulletMessage(data)
  } else if (data.type && data.action) {
    handleUserActionMessage(data)
  } else if (data.type && data.users) {
    handleUsersListMessage(data)
  } else if (data.type === 'system') {
    // Keep system message handling as fallback or explicit check
    addSystemMessage(data.message)
  } else {
    console.warn('Unhandled message structure:', data)
  }
}


const handleBulletMessage = (data) => {
  if (data.type !== 'bullet') return

  const subType = data.subType
  // Assuming 'content' is already an object or parsed data
  // In Vue, data.content is accessible directly
  
  if (subType === 'danmaku' || subType === 'chat' || subType === 'gift' || subType === 'like' || subType === 'share') {
      // Map legacy types to 'danmaku' handler logic or handle directly
      // For now, let's treat chat/gift/like/share as 'danmaku' for display
      handleDanmakuMessage(data.content, data.assignedId, data)
  } else if (subType === 'remotectrl') {
      // Handle remote control
  } else if (subType === 'setting') {
      // Handle settings
  }
}

const handleDanmakuMessage = (content, assignedId, rawData) => {
    // Check if content is array or object
    if (Array.isArray(content)) {
        content.forEach(item => {
            processDanmaItem(assignedId, item, rawData)
        })
    } else if (typeof content === 'object' && content !== null) {
        processDanmaItem(assignedId, content, rawData)
    } else {
        // Fallback for simple string content (legacy support)
        // If content is string, wrap it in an object structure if possible, or handle gracefully
        processDanmaItem(assignedId, { content: content, type: 'chat', name: 'Unknown' }, rawData)
    }
}

const processDanmaItem = (assignedId, item, rawData) => {
    // C++ Logic Mapping:
    // 1. Extract type string (jsonObject["type"])
    const typeStr = item.type || 'chat'
    
    // 2. Map to frontend types
    // MESSAGE_TYPE_MAP equivalent
    let msgType = 'chat'
    const lowerType = typeStr.toLowerCase()
    
    if (lowerType === 'chat') {
        msgType = 'chat'
    } else if (lowerType === 'gift') {
        msgType = 'gift'
    } else if (lowerType === 'member' || lowerType === 'join') {
        msgType = 'join'
    } else if (lowerType === 'like') {
        msgType = 'like'
    } else if (lowerType === 'share') {
        msgType = 'share'
    } else {
        // Unknown type, treat as chat or log warning
        console.warn('Unknown message type:', typeStr)
        // Default to chat to ensure visibility, or return to ignore
        msgType = 'chat' 
    }

    // 3. Check content existence
    // if (!jsonObject.contains("content") || jsonObject["content"].is_null()) return;
    if (item.content === undefined || item.content === null) {
        console.warn('Message content is null or missing for type:', typeStr)
        return
    }

    // 4. Create DanmaItem (createDanmaItem equivalent)
    /*
        name: jsonObject["name"]
        content: jsonObject["content"]
        platform: jsonObject["platform"]
        liveId: jsonObject["liveId"]
        userId: jsonObject["userId"]
    */
    
    const msgItem = { 
      type: msgType, 
      userId: item.userId || rawData.userId || '0', 
      username: item.name || '匿名用户',
      avatar: item.avatar || '', // C++ sets this to empty string currently
      content: item.content,
      platform: item.platform || 'unknown',
      liveId: item.liveId || '',
      role: rawData.role || 'USER',
      timestamp: Date.now(),
      id: Date.now() + Math.random().toString(36).substr(2, 9),
      assignedId: assignedId
    }

    pushMessage(msgItem)
}

const pushMessage = (msgItem) => {
    messages.value.push(msgItem)
    // Limit global message count
    if (messages.value.length > 1000) {
      messages.value.shift()
    }
    
    nextTick(() => {
      if (isAtBottom.value) {
        scrollToBottom()
      } else {
        unreadCount.value++
      }
    })
}

const handleUserActionMessage = (data) => {
  const action = data.action
  const role = data.role || '用户'
  const assignedId = data.assignedId || '?'
  
  // Update online count based on action
  if (action === 'join') {
    onlineCount.value++
    
    // Create a join message for the chat
    const joinMsg = {
      type: 'join',
      userId: data.userId || `User${assignedId}`,
      username: `用户${assignedId}`,
      content: '加入直播间',
      timestamp: Date.now(),
      id: Date.now() + Math.random().toString(36).substr(2, 9),
      role: role
    }
    pushMessage(joinMsg)
    
  } else if (action === 'leave') {
    if (onlineCount.value > 0) {
      onlineCount.value--
    }
    // Optional: Show leave message
    // addSystemMessage(`用户${assignedId} 离开了直播间`)
  }
}

const handleUsersListMessage = (data) => {
  if (data.users && Array.isArray(data.users)) {
    onlineCount.value = data.users.length
    // We could also store the user list if we needed to display it
    // const usersList = data.users
  }
}

const sendMessage = () => {
  const text = inputMessage.value.trim()
  if (!text) return 
  
  if (!ws.value || ws.value.readyState !== WebSocket.OPEN) {
    ElMessage.warning('连接未就绪')
    return
  }

  // Construct payload
  // Type is always 'bullet', subType is 'chat'
  const msgObj = {
    type: 'bullet',
    subType: 'chat',
    content: text
  }
  
  ws.value.send(JSON.stringify(msgObj))
  
  // Optimistic update
  const msgItem = {
    type: 'chat',
    userId: 'me',
    username: '我',
    avatar: '',
    content: text,
    platform: 'web',
    liveId: roomId.value,
    role: 'ADMIN',
    timestamp: Date.now(),
    id: Date.now() + Math.random().toString(36).substr(2, 9),
    assignedId: 0
  }
  
  pushMessage(msgItem)
  
  inputMessage.value = ''
  
  // Force scroll to bottom for self-sent messages
  isAtBottom.value = true
  unreadCount.value = 0
  scrollToBottom()
}

const addSystemMessage = (text) => {
  messages.value.push({
    type: 'system',
    userId: '系统',
    content: text,
    timestamp: Date.now(),
    id: Date.now() + Math.random().toString(36).substr(2, 9)
  })
  if (isAtBottom.value) {
    scrollToBottom()
  } else {
    unreadCount.value++
  }
}

const toggleSelected = (id) => {
  if (activeMsgId.value === id) {
    activeMsgId.value = null
  } else {
    activeMsgId.value = id
  }
}

const handleVoiceReply = async (msg) => {
  if (isVoiceReplying.value && activeMsgId.value === msg.id) {
     stopVoiceReply()
     return
  }

  if (isVoiceReplying.value) {
    stopVoiceReply()
  }

  if (!ZEGO_APP_ID) {
      ElMessageBox.alert('请先在代码中配置 ZEGO_APP_ID 和 ZEGO_SERVER_URL \n(位于 ControlPanel.vue 头部)', 'SDK 未配置', {
        confirmButtonText: '我知道了'
      })
      return
  }

  try {
      const streamID = `reply_${roomId.value}_${msg.id}`
      await zegoClient.startVoiceReply(streamID)
      isVoiceReplying.value = true
      currentReplyStreamID.value = streamID
      ElMessage.success(`正在语音回复: ${msg.content}`)
  } catch (e) {
      ElMessage.error('语音回复启动失败: ' + e.message)
  }
}

const stopVoiceReply = () => {
    if (currentReplyStreamID.value) {
        zegoClient.stopVoiceReply(currentReplyStreamID.value)
        isVoiceReplying.value = false
        currentReplyStreamID.value = null
        ElMessage.info('已结束语音回复')
    }
}

// Watch scroll to detect bottom
const handleScroll = () => {
  const el = chatListRef.value
  if (!el) return
  
  // Relax threshold slightly to 10px to account for sub-pixel rendering
  const isBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 10
  
  // Only update state if we are truly at bottom
  // If user is scrolling up, the 'wheel' event will catch it faster
  if (isBottom) {
    isAtBottom.value = true
    unreadCount.value = 0
  } else {
    isAtBottom.value = false
  }
}

const handleWheel = (e) => {
  // If user scrolls UP (deltaY < 0), immediately lock auto-scroll
  if (e.deltaY < 0) {
    isAtBottom.value = false
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatListRef.value) {
      chatListRef.value.scrollTop = chatListRef.value.scrollHeight
      // Note: We do NOT force set isAtBottom = true here.
      // We let the scroll event listener confirm it naturally.
      // This prevents race conditions where we think we are at bottom but user is scrolling up.
    }
  })
}

const clearChat = () => {
  messages.value = []
}

const toggleSimulation = () => {
  if (isSimulating.value) {
    stopSimulation()
  } else {
    startSimulation()
  }
}

const startSimulation = () => {
  isSimulating.value = true
  // Generate a message every 10ms (approx)
  // Warning: This is very fast and might lag the browser if left running for too long
  simulationTimer = setInterval(() => {
    generateRandomMessage()
  }, 50) 
}

const stopSimulation = () => {
  isSimulating.value = false
  if (simulationTimer) {
    clearInterval(simulationTimer)
    simulationTimer = null
  }
}

const generateRandomMessage = () => {
  const types = ['join', 'chat', 'gift', 'share', 'like']
  const randomType = types[Math.floor(Math.random() * types.length)]
  const users = ['张三', '李四', '王五', '赵六', '榜一大哥', '纯路人', '小透明', '管理001']
  const randomUser = users[Math.floor(Math.random() * users.length)]
  
  // The 'content' part of the message (The Item)
  // Matching C++ DanmaItem structure
  const itemContent = {
      type: randomType === 'join' ? 'member' : randomType, 
      name: randomUser,
      userId: 'user_' + Math.floor(Math.random() * 10000),
      platform: ['ios', 'android', 'web'][Math.floor(Math.random() * 3)],
      liveId: roomId.value,
      timestamp: Date.now()
  }
  
  // Add specific content based on type
  if (randomType === 'chat') {
      const texts = [
        '主播好帅 😍', 
        '666 🔥', 
        '怎么还不开始？🤔', 
        'BGM是什么？🎵', 
        '哈哈哈哈 😂', 
        '前面的等等我 🏃‍♂️', 
        '有点卡 📶', 
        '清楚多了 ✨', 
        '支持支持 👍',
        '这也太强了吧 🐮🍺',
        '爱了爱了 ❤️'
      ]
      itemContent.content = texts[Math.floor(Math.random() * texts.length)]
  } else if (randomType === 'gift') {
      const gifts = ['火箭 🚀', '跑车 🏎️', '鲜花 💐', '小心心 ❤️', '大啤酒 🍺', '摩天轮 🎡']
      itemContent.content = gifts[Math.floor(Math.random() * gifts.length)]
  } else {
      // For system/event types, content might be empty or specific description
      itemContent.content = randomType 
  }

  // The full message wrapper
  const payload = {
    type: 'bullet',
    subType: 'danmaku', 
    content: itemContent,
    assignedId: Math.floor(Math.random() * 1000),
    role: Math.random() > 0.9 ? 'vip' : 'user'
  }

  handleMessage(payload)
  
  // Limit memory usage
  // Note: pushMessage already handles this limit (1000 items), 
  // but generateRandomMessage was manually slicing.
  // We can remove this manual slice since pushMessage does it.
}

onMounted(() => {
  connectWebSocket()
  initZego()
})

onUnmounted(() => {
  stopSimulation()
  if (ws.value) ws.value.close()
})
</script>

<style scoped>
.control-container {
  width: 100vw;
  height: 100vh;
  background-color: #000;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  box-sizing: border-box;
}

.main-content {
  display: flex;
  width: 100%;
  max-width: 1600px;
  height: 85vh;
  background-color: #161823;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 0 20px rgba(0,0,0,0.5);
}

.video-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #2f3035;
  position: relative;
  transition: all 0.3s;
}

.video-placeholder {
  flex: 1;
  background-color: #000;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #666;
  position: relative;
}

.live-tag {
  position: absolute;
  top: 20px;
  left: 20px;
  background-color: #fe2c55;
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 12px;
}

.control-bar {
  height: 60px;
  background-color: #161823;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 10px;
  border-top: 1px solid #2f3035;
}

.stats {
  margin-left: auto;
  color: #999;
  font-size: 14px;
  display: flex;
  gap: 15px;
}

/* Chat Section */
.msg-time {
  color: #666;
  font-size: 12px;
  margin-right: 8px;
  flex-shrink: 0;
}

.chat-section {
  width: 400px;
  display: flex;
  flex-direction: column;
  background-color: #161823;
  transition: width 0.3s;
}

.chat-section.full-width {
  width: 100%;
}

.chat-header {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  border-bottom: 1px solid #2f3035;
  color: #fff;
}

.header-left {
  display: flex;
  align-items: center;
}

.filter-bar {
  padding: 10px 15px;
  border-bottom: 1px solid #2f3035;
  background-color: #1a1c29;
}

.filter-tabs {
  display: flex;
  gap: 10px;
}

.filter-tab {
  padding: 4px 12px;
  border-radius: 14px;
  font-size: 13px;
  color: #999;
  cursor: pointer;
  background-color: #252632;
  transition: all 0.2s;
  user-select: none;
}

.filter-tab:hover {
  color: #fff;
  background-color: #33343f;
}

.filter-tab.active {
  color: #fff;
  background-color: #fe2c55;
  font-weight: 500;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  /* Ensure left alignment */
  align-items: flex-start; 
}

.chat-item {
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
  color: #fff;
  text-align: left; /* Explicit left alignment */
  width: 100%;
  display: flex;
  align-items: center;
  flex-wrap: wrap; /* Allow wrapping for action bar */
  cursor: pointer;
  transition: background-color 0.2s;
  padding: 4px;
  border-radius: 4px;
}

.chat-item:hover {
  background-color: rgba(255, 255, 255, 0.05);
}

.chat-item.active {
  background-color: rgba(64, 158, 255, 0.1);
}

.msg-action-bar {
  width: 100%;
  margin-top: 8px;
  padding-left: 60px; /* Indent to align with content roughly */
  display: flex;
  gap: 8px;
}

/* Badges */
.msg-type-badge {
  display: inline-block;
  padding: 0 4px;
  border-radius: 4px;
  font-size: 11px;
  margin-right: 8px;
  flex-shrink: 0;
  height: 18px;
  line-height: 18px;
}

.msg-type-badge.join { background-color: #2f3035; color: #999; }
.msg-type-badge.chat { background-color: rgba(254, 44, 85, 0.1); color: #fe2c55; }
.msg-type-badge.gift { background-color: rgba(255, 165, 0, 0.15); color: #ffa500; }
.msg-type-badge.share { background-color: rgba(32, 214, 248, 0.15); color: #20d6f8; }
.msg-type-badge.like { background-color: rgba(255, 64, 129, 0.15); color: #ff4081; }
.msg-type-badge.system { background-color: #f56c6c; color: #fff; }

.user-name {
  color: #aeb4c0;
  margin-right: 6px;
  font-weight: 500;
  white-space: nowrap;
}

.message-content {
  color: #fff;
}

.message-content.gift { color: #ffa500; }
.message-content.like { color: #ff4081; }

.chat-input-area {
  padding: 15px;
  border-top: 1px solid #2f3035;
  background-color: #161823;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.input-actions {
  display: flex;
}

:deep(.el-input__wrapper) {
  background-color: #252632;
  box-shadow: none;
  border-radius: 20px;
}

:deep(.el-input__inner) {
  color: #fff;
}

/* Custom Scrollbar */
.chat-list::-webkit-scrollbar {
  width: 6px;
}
.chat-list::-webkit-scrollbar-thumb {
  background: #333;
  border-radius: 3px;
}

/* Emoji Grid */
.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 5px;
  max-height: 200px;
  overflow-y: auto;
}

.emoji-item {
  font-size: 20px;
  cursor: pointer;
  text-align: center;
  padding: 5px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.emoji-item:hover {
  background-color: #f0f0f0;
}

/* Ensure emoji button looks good in input prepend */
.emoji-btn {
  padding: 0 10px !important;
}

.new-msg-toast {
  position: absolute;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  background-color: #fe2c55;
  color: #fff;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
  z-index: 10;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from { transform: translate(-50%, 20px); opacity: 0; }
  to { transform: translate(-50%, 0); opacity: 1; }
}
</style>
