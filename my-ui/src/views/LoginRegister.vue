<template>
  <div class="login-container">
    <div class="login-content">
      <div class="brand-section">
        <h1>Rila Server <span class="lite-badge">LITE</span></h1>
        <p class="brand-slogan">轻量级个人直播解决方案</p>
        <div class="feature-list">
          <div class="feature-item">
            <el-icon><VideoCamera /></el-icon>
            <span>1路标准推流</span>
          </div>
          <div class="feature-item">
            <el-icon><Monitor /></el-icon>
            <span>多设备拉流</span>
          </div>
          <div class="feature-item">
            <el-icon><Connection /></el-icon>
            <span>免费社区支持</span>
          </div>
        </div>
      </div>
      
      <el-card class="login-card">
        <div class="header">
          <h2>用户登录</h2>
          <p class="subtitle">REGISTER LOGIN</p>
        </div>
        <el-form :model="form" label-position="top" size="large">
          <el-form-item label="用户名">
            <el-input v-model="form.userId" placeholder="请输入用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item label="房间号">
            <el-input v-model="form.roomId" placeholder="请输入房间号" :prefix-icon="House" />
          </el-form-item>
          <el-form-item>
            <el-button class="submit-btn" type="primary" @click="handleLogin" :loading="loading">
              立即登录
            </el-button>
            <div class="form-footer">
              <el-button link class="back-btn" @click="$router.push('/login')">
                <el-icon><Back /></el-icon> 切换身份
              </el-button>
              <el-button link class="help-btn">注册账号</el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { User, Lock, House, Back, VideoCamera, Monitor, Connection } from '@element-plus/icons-vue'

const router = useRouter()
const form = ref({
  userId: '',
  password: '',
  roomId: '1001'
})
const loading = ref(false)

const handleLogin = async () => {
  if (!form.value.userId || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  loading.value = true
  try {
    const res = await axios.post('/ws/login', {
      userId: form.value.userId,
      password: form.value.password,
      role: 'controller',
      roomId: form.value.roomId
    })
    
    const token = res.data.token
    if (token) {
      localStorage.setItem('stream_token', token)
      localStorage.setItem('stream_user', form.value.userId)
      localStorage.setItem('stream_room', form.value.roomId)
      localStorage.setItem('user_type', 'register')
      ElMessage.success('登录成功')
      router.push('/control')
    } else {
      ElMessage.error('登录失败: 未获取到Token')
    }
  } catch (err) {
    console.error(err)
    const errorMsg = err.response?.data?.message || err.response?.data?.error || '登录请求失败'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(120deg, #2980b9, #8e44ad);
  background-size: 200% 200%;
  animation: gradientBG 15s ease infinite;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
  overflow: hidden;
  position: relative;
}

@keyframes gradientBG {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.login-content {
  display: flex;
  align-items: center;
  gap: 80px;
  z-index: 1;
  padding: 20px;
}

.brand-section {
  color: #fff;
  text-align: left;
  max-width: 400px;
}

.brand-section h1 {
  font-size: 3rem;
  margin-bottom: 10px;
  text-shadow: 0 4px 10px rgba(0,0,0,0.2);
}

.lite-badge {
  background: #2ecc71;
  font-size: 1rem;
  padding: 4px 10px;
  border-radius: 6px;
  vertical-align: middle;
  text-shadow: none;
}

.brand-slogan {
  font-size: 1.2rem;
  opacity: 0.9;
  margin-bottom: 40px;
  font-weight: 300;
  letter-spacing: 1px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 1.1rem;
  background: rgba(255, 255, 255, 0.1);
  padding: 12px 20px;
  border-radius: 10px;
  backdrop-filter: blur(5px);
  transition: transform 0.3s;
}

.feature-item:hover {
  transform: translateX(10px);
  background: rgba(255, 255, 255, 0.2);
}

.feature-item .el-icon {
  font-size: 1.4rem;
}

.login-card {
  width: 400px;
  background: rgba(255, 255, 255, 0.95);
  border: none;
  border-radius: 16px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  padding: 10px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h2 {
  font-size: 1.8rem;
  color: #2c3e50;
  margin: 0 0 5px;
}

.subtitle {
  color: #7f8c8d;
  font-size: 0.9rem;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.submit-btn {
  width: 100%;
  font-size: 1.1rem;
  padding: 22px 0;
  border-radius: 8px;
  background: linear-gradient(to right, #2980b9, #3498db);
  border: none;
  transition: all 0.3s;
  margin-top: 10px;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(41, 128, 185, 0.4);
}

.form-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}

.back-btn {
  color: #7f8c8d;
}
.back-btn:hover {
  color: #2980b9;
}

.help-btn {
  color: #3498db;
}

@media (max-width: 900px) {
  .login-content {
    flex-direction: column;
    gap: 40px;
  }
  .brand-section {
    text-align: center;
    align-items: center;
  }
  .feature-list {
    display: none; /* Hide features on mobile to save space */
  }
}
</style>
