<template>
  <div class="login-container">
    <div class="login-content">
      <div class="brand-section">
        <h1>Rila Server <span class="enterprise-badge">MAX</span></h1>
        <p class="brand-slogan">企业级全栈流媒体中台</p>
        <div class="feature-list">
          <div class="feature-item">
            <div class="icon-box">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="text-content">
              <strong>100路超清推流</strong>
              <span>支持 4K/8K HDR 画质</span>
            </div>
          </div>
          <div class="feature-item">
            <div class="icon-box">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="text-content">
              <strong>集群协同作业</strong>
              <span>支持 20+ 设备分布式部署</span>
            </div>
          </div>
          <div class="feature-item">
            <div class="icon-box">
              <el-icon><DataLine /></el-icon>
            </div>
            <div class="text-content">
              <strong>SLA 99.99%</strong>
              <span>企业级稳定性保障</span>
            </div>
          </div>
          <div class="feature-item">
            <div class="icon-box">
              <el-icon><Service /></el-icon>
            </div>
            <div class="text-content">
              <strong>专属技术支持</strong>
              <span>7x24小时 1对1 服务</span>
            </div>
          </div>
        </div>
      </div>
      
      <el-card class="login-card">
        <div class="header">
          <div class="logo-wrapper">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <h2>企业登录</h2>
          <p class="subtitle">ENTERPRISE WORKSPACE</p>
        </div>
        <el-form :model="form" label-position="top" size="large" class="login-form">
          <el-form-item label="企业ID">
            <el-input v-model="form.userId" placeholder="请输入企业ID" :prefix-icon="User" />
          </el-form-item>
          <el-form-item label="访问密钥">
            <el-input v-model="form.password" type="password" placeholder="请输入密钥" :prefix-icon="Key" show-password />
          </el-form-item>
          <el-form-item label="频道节点">
            <el-input v-model="form.roomId" placeholder="请输入频道号" :prefix-icon="DataAnalysis" />
          </el-form-item>
          <el-form-item>
            <el-button class="submit-btn" type="danger" @click="handleLogin" :loading="loading">
              安全登录
            </el-button>
            <div class="form-footer">
              <el-button link class="back-btn" @click="$router.push('/login')">
                <el-icon><Back /></el-icon> 切换身份
              </el-button>
              <el-button link class="help-btn">找回密钥</el-button>
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
import { User, Key, DataAnalysis, Back, Monitor, Connection, DataLine, Service, OfficeBuilding } from '@element-plus/icons-vue'

const router = useRouter()
const form = ref({
  userId: '',
  password: '',
  roomId: '1001'
})
const loading = ref(false)

const handleLogin = async () => {
  if (!form.value.userId || !form.value.password) {
    ElMessage.warning('请输入完整登录信息')
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
      localStorage.setItem('user_type', 'enterprise')
      ElMessage.success('企业工作台登录成功')
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
  background: linear-gradient(135deg, #000000 0%, #1a1a2e 100%);
  position: relative;
  overflow: hidden;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
}

/* Subtle background texture */
.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 30px 30px;
  opacity: 0.5;
}

.login-content {
  display: flex;
  align-items: center;
  gap: 100px;
  z-index: 1;
  padding: 40px;
  max-width: 1200px;
  width: 100%;
}

.brand-section {
  flex: 1;
  color: #fff;
}

.brand-section h1 {
  font-size: 3.5rem;
  margin: 0 0 10px;
  letter-spacing: -1px;
  text-shadow: 0 4px 20px rgba(0,0,0,0.5);
}

.enterprise-badge {
  background: linear-gradient(135deg, #7000ff, #00f0ff);
  font-size: 1rem;
  padding: 4px 12px;
  border-radius: 4px;
  vertical-align: middle;
  font-weight: 800;
  letter-spacing: 1px;
  box-shadow: 0 0 15px rgba(112, 0, 255, 0.4);
}

.brand-slogan {
  font-size: 1.4rem;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 50px;
  font-weight: 300;
  letter-spacing: 2px;
  border-left: 4px solid #7000ff;
  padding-left: 20px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 20px;
  background: rgba(255, 255, 255, 0.05);
  padding: 20px 25px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateX(10px);
  border-color: rgba(0, 240, 255, 0.3);
}

.icon-box {
  width: 48px;
  height: 48px;
  background: rgba(112, 0, 255, 0.1);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #00f0ff;
  font-size: 24px;
}

.text-content {
  display: flex;
  flex-direction: column;
}

.text-content strong {
  font-size: 1.1rem;
  color: #fff;
  margin-bottom: 4px;
}

.text-content span {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.5);
}

.login-card {
  width: 440px;
  background: rgba(30, 30, 35, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 10px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.logo-wrapper {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #2c3e50, #000);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  box-shadow: 0 10px 20px rgba(0,0,0,0.3);
  color: #7000ff;
  font-size: 30px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.header h2 {
  font-size: 1.8rem;
  color: #fff;
  margin: 0 0 5px;
}

.subtitle {
  color: rgba(255, 255, 255, 0.4);
  font-size: 0.8rem;
  letter-spacing: 3px;
  font-weight: 600;
}

.login-form :deep(.el-input__wrapper) {
  background-color: rgba(0, 0, 0, 0.3);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #7000ff inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #00f0ff inset;
}

.login-form :deep(.el-input__inner) {
  color: #fff;
}

.login-form :deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.7);
}

.submit-btn {
  width: 100%;
  font-size: 1.1rem;
  padding: 22px 0;
  border-radius: 8px;
  background: linear-gradient(to right, #7000ff, #00f0ff);
  border: none;
  transition: all 0.3s;
  margin-top: 10px;
  font-weight: 600;
  letter-spacing: 1px;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(112, 0, 255, 0.3);
}

.form-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 25px;
}

.back-btn {
  color: rgba(255, 255, 255, 0.5);
}
.back-btn:hover {
  color: #fff;
}

.help-btn {
  color: #00f0ff;
}

@media (max-width: 1100px) {
  .login-content {
    flex-direction: column;
    gap: 60px;
    align-items: center;
  }
  .brand-section {
    text-align: center;
  }
  .feature-list {
    max-width: 600px;
    margin: 0 auto;
  }
  .brand-slogan {
    border-left: none;
    border-bottom: 4px solid #7000ff;
    padding-left: 0;
    padding-bottom: 10px;
    display: inline-block;
  }
  .feature-item {
    justify-content: center;
    text-align: left;
  }
}
</style>