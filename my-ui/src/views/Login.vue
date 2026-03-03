<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>场控登录</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.userId" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="房间号">
          <el-input v-model="form.roomId" placeholder="请输入要管理的房间号" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

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
    // Matches C++ RilaHttpClient::LoginWebsocketTask behavior
    const res = await axios.post('/ws/login', {
      userId: form.value.userId,
      password: form.value.password,
      role: 'controller',
      roomId: form.value.roomId
    })
    
    // WebSocketAuthController returns { token: "...", roomId: "..." }
    const token = res.data.token
    if (token) {
      localStorage.setItem('stream_token', token)
      localStorage.setItem('stream_user', form.value.userId)
      localStorage.setItem('stream_room', form.value.roomId)
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
  height: 100vh;
  background-color: #000;
}
.login-card {
  width: 400px;
  background-color: #1e1e1e;
  border: 1px solid #333;
}
h2 {
  text-align: center;
  color: #fff;
  margin-bottom: 20px;
}
:deep(.el-input__wrapper) {
  background-color: #333;
  box-shadow: none;
}
:deep(.el-input__inner) {
  color: #fff;
}
</style>
