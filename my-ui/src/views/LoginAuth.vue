<template>
  <div class="login-container">
    <div class="login-content">
      <div class="brand-section">
        <h1>Rila Server <span class="pro-badge">PLUS</span></h1>
        <p class="brand-slogan">专业级直播流媒体工作台</p>
        <div class="feature-list">
          <div class="feature-item">
            <el-icon><DataLine /></el-icon>
            <span>20路高清推流</span>
          </div>
          <div class="feature-item">
            <el-icon><Platform /></el-icon>
            <span>5台设备协同</span>
          </div>
          <div class="feature-item">
            <el-icon><Cpu /></el-icon>
            <span>优先计算资源</span>
          </div>
        </div>
      </div>
      
      <el-card class="login-card">
        <div class="header">
          <h2>授权登录</h2>
          <p class="subtitle">AUTHORIZED ACCESS</p>
        </div>
        <el-form :model="form" label-position="top" size="large">
          <el-form-item label="授权账号">
            <el-input v-model="form.userId" placeholder="请输入授权账号" :prefix-icon="UserFilled" />
          </el-form-item>
          <el-form-item label="访问令牌">
            <el-input v-model="form.password" type="password" placeholder="请输入访问令牌" :prefix-icon="Key" show-password />
          </el-form-item>
          <el-form-item label="专属频道">
            <el-input v-model="form.roomId" placeholder="请输入频道ID" :prefix-icon="Share" />
          </el-form-item>
          <el-form-item>
            <el-button class="submit-btn" type="warning" @click="handleLogin" :loading="loading">
              验证并登录
            </el-button>
            <div class="form-footer">
              <el-button link class="back-btn" @click="$router.push('/login')">
                <el-icon><Back /></el-icon> 切换身份
              </el-button>
              <el-button link class="help-btn">申请授权</el-button>
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
import { UserFilled, Key, Share, Back, DataLine, Platform, Cpu } from '@element-plus/icons-vue'

const router = useRouter()
const form = ref({
  userId: '',
  password: '',
  roomId: '1001'
})
const loading = ref(false)

const handleLogin = async () => {
  if (!form.value.userId || !form.value.password) {
    ElMessage.warning('请输入授权账号和令牌')
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
      localStorage.setItem('user_type', 'auth')
      ElMessage.success('授权验证成功')
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
  background: linear-gradient(135deg, #2c3e50 0%, #4b6cb7 100%);
  /* Overlay with a subtle pattern or texture could go here */
  position: relative;
  overflow: hidden;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
}

/* Abstract geometric shapes for background interest */
.login-container::before {
  content: '';
  position: absolute;
  top: -10%;
  right: -10%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(255,255,255,0.05) 0%, rgba(255,255,255,0) 70%);
  border-radius: 50%;
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
  text-shadow: 0 4px 10px rgba(0,0,0,0.3);
}

.pro-badge {
  background: linear-gradient(90deg, #f0932b, #f9ca24);
  color: #2c3e50;
  font-size: 1rem;
  padding: 4px 12px;
  border-radius: 6px;
  vertical-align: middle;
  font-weight: 800;
  box-shadow: 0 2px 10px rgba(240, 147, 43, 0.4);
}

.brand-slogan {
  font-size: 1.2rem;
  opacity: 0.9;
  margin-bottom: 40px;
  font-weight: 300;
  letter-spacing: 1px;
  border-left: 4px solid #f9ca24;
  padding-left: 15px;
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
  background: rgba(44, 62, 80, 0.6);
  padding: 15px 25px;
  border-radius: 8px;
  border-left: 3px solid #f9ca24;
  backdrop-filter: blur(5px);
  transition: transform 0.3s;
}

.feature-item:hover {
  transform: translateX(10px);
  background: rgba(44, 62, 80, 0.8);
}

.feature-item .el-icon {
  font-size: 1.4rem;
  color: #f9ca24;
}

.login-card {
  width: 420px;
  background: rgba(255, 255, 255, 0.98);
  border: none;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  padding: 15px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
  border-bottom: 2px solid #f5f7fa;
  padding-bottom: 20px;
}

.header h2 {
  font-size: 1.8rem;
  color: #2c3e50;
  margin: 0 0 5px;
}

.subtitle {
  color: #e67e22;
  font-size: 0.85rem;
  letter-spacing: 3px;
  font-weight: 600;
  text-transform: uppercase;
}

.submit-btn {
  width: 100%;
  font-size: 1.1rem;
  padding: 22px 0;
  border-radius: 6px;
  background: linear-gradient(to right, #f0932b, #ffbe76);
  border: none;
  transition: all 0.3s;
  margin-top: 15px;
  color: #fff;
  font-weight: 600;
  letter-spacing: 1px;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(240, 147, 43, 0.3);
  background: linear-gradient(to right, #e67e22, #f39c12);
}

.form-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 25px;
  padding-top: 15px;
  border-top: 1px solid #f5f7fa;
}

.back-btn {
  color: #95a5a6;
}
.back-btn:hover {
  color: #2c3e50;
}

.help-btn {
  color: #e67e22;
  font-weight: 500;
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
  .brand-slogan {
    border-left: none;
    border-bottom: 3px solid #f9ca24;
    padding-left: 0;
    padding-bottom: 10px;
  }
  .feature-list {
    display: none;
  }
  .feature-item {
    border-left: none;
    border-bottom: 3px solid #f9ca24;
  }
}
</style>
