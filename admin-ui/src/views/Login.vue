<template>
  <div class="login-container">
    <div class="login-wrapper">
      <!-- 左侧装饰区域 -->
      <div class="login-left">
        <div class="login-bg">
          <div class="login-bg-content">
            <h2>欢迎使用管理系统</h2>
            <p>安全、高效、智能的管理平台</p>
            <div class="feature-list">
               <div class="feature-item">
                 <el-icon><Setting /></el-icon>
                 <span>安全可靠</span>
               </div>
               <div class="feature-item">
                 <el-icon><VideoPlay /></el-icon>
                 <span>快速响应</span>
               </div>
               <div class="feature-item">
                 <el-icon><Grid /></el-icon>
                 <span>用户友好</span>
               </div>
             </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录区域 -->
      <div class="login-right">
        <div class="login-form-container">
          <div class="login-header">
            <img :src="logoUrl" class="login-logo" alt="logo" />
            <h3 class="login-title">管理员登录</h3>
            <p class="login-subtitle">请选择登录方式</p>
          </div>

          <!-- 登录方式切换 -->
          <div class="login-tabs">
            <div 
              class="tab-item" 
              :class="{ active: activeTab === 'password' }"
              @click="activeTab = 'password'"
            >
              <el-icon><Key /></el-icon>
              <span>密码登录</span>
            </div>
            <div 
              class="tab-item" 
              :class="{ active: activeTab === 'sms' }"
              @click="activeTab = 'sms'"
            >
              <el-icon><Message /></el-icon>
              <span>验证码</span>
            </div>
            <div 
              class="tab-item" 
              :class="{ active: activeTab === 'qr' }"
              @click="activeTab = 'qr'"
            >
              <el-icon><View /></el-icon>
              <span>扫码登录</span>
            </div>
          </div>

          <!-- 密码登录表单 -->
          <div v-show="activeTab === 'password'" class="login-form-panel">
            <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" class="login-form">
              <el-form-item prop="username">
                <el-input
                  v-model="passwordForm.username"
                  placeholder="请输入用户名/邮箱/手机号"
                  size="large"
                  prefix-icon="User"
                />
              </el-form-item>

              <el-form-item prop="password">
                <el-input
                  v-model="passwordForm.password"
                  :type="passwordVisible ? 'text' : 'password'"
                  placeholder="请输入密码"
                  size="large"
                  prefix-icon="Setting"
                  @keyup.enter="handlePasswordLogin"
                >
                  <template #suffix>
                    <el-icon class="password-toggle" @click="togglePassword">
                      <component :is="passwordVisible ? Hide : View" />
                    </el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <div class="login-options">
                <el-checkbox v-model="rememberMe">记住我</el-checkbox>
                <el-link type="primary" @click="showForgotPassword = true">忘记密码？</el-link>
              </div>

              <el-button 
                :loading="loading" 
                type="primary" 
                size="large"
                class="login-btn"
                @click="handlePasswordLogin"
              >
                登录
              </el-button>
            </el-form>
          </div>

          <!-- 验证码登录表单 -->
          <div v-show="activeTab === 'sms'" class="login-form-panel">
            <el-form ref="smsFormRef" :model="smsForm" :rules="smsRules" class="login-form">
              <el-form-item prop="phone">
                <el-input
                  v-model="smsForm.phone"
                  placeholder="请输入手机号"
                  size="large"
                  prefix-icon="Connection"
                />
              </el-form-item>

              <el-form-item prop="code">
                <div class="sms-input-group">
                  <el-input
                     v-model="smsForm.code"
                     placeholder="请输入验证码"
                     size="large"
                     :prefix-icon="Grid"
                     @keyup.enter="handleSmsLogin"
                   />
                   <el-button 
                    :disabled="smsCountdown > 0" 
                    @click="sendSmsCode"
                    class="sms-btn"
                  >
                    {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
                  </el-button>
                </div>
              </el-form-item>

              <el-button 
                :loading="loading" 
                type="primary" 
                size="large"
                class="login-btn"
                @click="handleSmsLogin"
              >
                登录
              </el-button>
            </el-form>
          </div>

          <!-- 扫码登录 -->
          <div v-show="activeTab === 'qr'" class="login-form-panel">
            <div class="qr-login-container">
              <div class="qr-code-wrapper">
                <div class="qr-code" ref="qrCodeRef">
                  <!-- 这里可以集成真实的二维码生成库 -->
                  <div class="qr-placeholder">
                    <el-icon size="80"><View /></el-icon>
                    <p>请使用手机扫描二维码</p>
                  </div>
                </div>
                <div class="qr-status" v-if="qrStatus">
                  <el-icon><SuccessFilled /></el-icon>
                  <span>{{ qrStatus }}</span>
                </div>
              </div>
              <div class="qr-tips">
                <p>使用微信或企业微信扫码登录</p>
                <el-button text @click="refreshQrCode">
                  <el-icon><ArrowDown /></el-icon>
                  刷新二维码
                </el-button>
              </div>
            </div>
          </div>

          <!-- 第三方登录 -->
          <div class="social-login">
            <div class="divider">
              <span>其他登录方式</span>
            </div>
            <div class="social-buttons">
              <el-button class="social-btn wechat" @click="handleWechatLogin">
                <svg class="social-icon" viewBox="0 0 1024 1024">
                  <path d="M693.12 344.32c-12.8 0-25.6 1.28-37.76 3.84 33.28-155.52 201.6-271.36 384-271.36 159.36 0 298.24 89.6 359.68 220.16-71.68-35.84-155.52-56.32-243.84-56.32-182.4 0-350.72 115.84-462.08 103.68z" fill="#00C800"/>
                  <path d="M693.12 344.32c-12.8 0-25.6 1.28-37.76 3.84 33.28-155.52 201.6-271.36 384-271.36 159.36 0 298.24 89.6 359.68 220.16-71.68-35.84-155.52-56.32-243.84-56.32-182.4 0-350.72 115.84-462.08 103.68z" fill="#00C800"/>
                </svg>
                微信登录
              </el-button>
              <el-button class="social-btn qq" @click="handleQQLogin">
                <svg class="social-icon" viewBox="0 0 1024 1024">
                  <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z" fill="#1296DB"/>
                </svg>
                QQ登录
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 忘记密码对话框 -->
    <el-dialog v-model="showForgotPassword" title="找回密码" width="400px">
      <el-form :model="forgotForm" :rules="forgotRules" ref="forgotFormRef">
        <el-form-item label="邮箱/手机号" prop="contact">
          <el-input
            v-model="forgotForm.contact"
            placeholder="请输入邮箱或手机号"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="sms-input-group">
            <el-input 
              v-model="forgotForm.code" 
              placeholder="请输入验证码"
              :prefix-icon="Grid"
            />
            <el-button :disabled="forgotCountdown > 0" @click="sendForgotCode">
              {{ forgotCountdown > 0 ? `${forgotCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForgotPassword = false">取消</el-button>
        <el-button type="primary" @click="handleForgotPassword">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { 
  User, Setting, Grid, VideoPlay, Connection,
  View, Hide, Expand, Fold, ArrowDown 
} from '@element-plus/icons-vue'
import logoUrl from '@/assets/logo.png'

const router = useRouter()
const userStore = useUserStore()

// 当前激活的登录方式
const activeTab = ref('password')

// 密码登录表单
const passwordForm = ref({
  username: '',
  password: ''
})

const passwordRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 验证码登录表单
const smsForm = ref({
  phone: '',
  code: ''
})

const smsRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// 忘记密码表单
const forgotForm = ref({
  contact: '',
  code: ''
})

const forgotRules = {
  contact: [{ required: true, message: '请输入邮箱或手机号', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// 表单引用
const passwordFormRef = ref()
const smsFormRef = ref()
const forgotFormRef = ref()
const qrCodeRef = ref()

// 状态变量
const passwordVisible = ref(false)
const loading = ref(false)
const rememberMe = ref(false)
const showForgotPassword = ref(false)
const smsCountdown = ref(0)
const forgotCountdown = ref(0)
const qrStatus = ref('')

// 定时器
let smsTimer = null
let forgotTimer = null
let qrTimer = null

// 密码显示/隐藏切换
const togglePassword = () => {
  passwordVisible.value = !passwordVisible.value
}

// 密码登录
const handlePasswordLogin = async () => {
  if (!passwordFormRef.value) return
  
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    // 这里应该调用实际的登录API
    await userStore.login(passwordForm.value)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    ElMessage.error('登录失败，请检查用户名和密码')
    console.error('Login failed:', error)
  } finally {
    loading.value = false
  }
}

// 验证码登录
const handleSmsLogin = async () => {
  if (!smsFormRef.value) return
  
  const valid = await smsFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    // 这里应该调用实际的验证码登录API
    // await userStore.smsLogin(smsForm.value)
    ElMessage.success('验证码登录成功')
    router.push('/')
  } catch (error) {
    ElMessage.error('验证码登录失败')
    console.error('SMS login failed:', error)
  } finally {
    loading.value = false
  }
}

// 发送短信验证码
const sendSmsCode = async () => {
  if (!smsForm.value.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }

  if (!/^1[3-9]\d{9}$/.test(smsForm.value.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }

  try {
    // 这里应该调用实际的发送验证码API
    // await api.sendSmsCode(smsForm.value.phone)
    ElMessage.success('验证码已发送')
    
    // 开始倒计时
    smsCountdown.value = 60
    smsTimer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) {
        clearInterval(smsTimer)
        smsTimer = null
      }
    }, 1000)
  } catch (error) {
    ElMessage.error('验证码发送失败')
    console.error('Send SMS failed:', error)
  }
}

// 发送忘记密码验证码
const sendForgotCode = async () => {
  if (!forgotForm.value.contact) {
    ElMessage.warning('请先输入邮箱或手机号')
    return
  }

  try {
    // 这里应该调用实际的发送验证码API
    // await api.sendForgotCode(forgotForm.value.contact)
    ElMessage.success('验证码已发送')
    
    // 开始倒计时
    forgotCountdown.value = 60
    forgotTimer = setInterval(() => {
      forgotCountdown.value--
      if (forgotCountdown.value <= 0) {
        clearInterval(forgotTimer)
        forgotTimer = null
      }
    }, 1000)
  } catch (error) {
    ElMessage.error('验证码发送失败')
    console.error('Send forgot code failed:', error)
  }
}

// 处理忘记密码
const handleForgotPassword = async () => {
  if (!forgotFormRef.value) return
  
  const valid = await forgotFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    // 这里应该调用实际的重置密码API
    // await api.resetPassword(forgotForm.value)
    ElMessage.success('密码重置链接已发送到您的邮箱/手机')
    showForgotPassword.value = false
    forgotForm.value = { contact: '', code: '' }
  } catch (error) {
    ElMessage.error('密码重置失败')
    console.error('Reset password failed:', error)
  }
}

// 刷新二维码
const refreshQrCode = () => {
  qrStatus.value = ''
  // 这里应该调用实际的生成二维码API
  // generateQrCode()
  ElMessage.info('二维码已刷新')
}

// 微信登录
const handleWechatLogin = () => {
  // 这里应该调用实际的微信登录API
  ElMessage.info('微信登录功能开发中...')
}

// QQ登录
const handleQQLogin = () => {
  // 这里应该调用实际的QQ登录API
  ElMessage.info('QQ登录功能开发中...')
}

// 模拟二维码扫描状态检查
const checkQrStatus = () => {
  if (activeTab.value === 'qr') {
    // 这里应该调用实际的二维码状态检查API
    // 模拟状态变化
    setTimeout(() => {
      if (Math.random() > 0.7) {
        qrStatus.value = '扫描成功，请在手机上确认登录'
      }
    }, 3000)
  }
}

// 组件挂载时的初始化
onMounted(() => {
  // 监听二维码状态
  qrTimer = setInterval(checkQrStatus, 2000)
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (smsTimer) {
    clearInterval(smsTimer)
  }
  if (forgotTimer) {
    clearInterval(forgotTimer)
  }
  if (qrTimer) {
    clearInterval(qrTimer)
  }
})
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-wrapper {
  width: 100%;
  max-width: 1200px;
  height: 600px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  display: flex;
  overflow: hidden;
}

/* 左侧装饰区域 */
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-bg {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.login-bg::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="white" opacity="0.1"/><circle cx="75" cy="75" r="1" fill="white" opacity="0.1"/><circle cx="50" cy="10" r="1" fill="white" opacity="0.1"/><circle cx="10" cy="50" r="1" fill="white" opacity="0.1"/><circle cx="90" cy="30" r="1" fill="white" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  opacity: 0.3;
}

.login-bg-content {
  text-align: center;
  color: white;
  z-index: 1;
  position: relative;
}

.login-bg-content h2 {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 16px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.login-bg-content p {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 40px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  opacity: 0.9;
}

.feature-item .el-icon {
  font-size: 20px;
}

/* 右侧登录区域 */
.login-right {
  flex: 1;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-form-container {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-logo {
  width: 60px;
  height: 60px;
  margin-bottom: 20px;
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 登录方式切换 */
.login-tabs {
  display: flex;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 4px;
  margin-bottom: 30px;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #606266;
}

.tab-item:hover {
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
}

.tab-item.active {
  background: white;
  color: #409eff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.tab-item .el-icon {
  font-size: 16px;
}

/* 登录表单面板 */
.login-form-panel {
  margin-bottom: 30px;
}

.login-form .el-form-item {
  margin-bottom: 20px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
}

.password-toggle {
  cursor: pointer;
  color: #c0c4cc;
  transition: color 0.3s;
}

.password-toggle:hover {
  color: #409eff;
}

/* 验证码输入组 */
.sms-input-group {
  display: flex;
  gap: 12px;
}

.sms-input-group .el-input {
  flex: 1;
}

.sms-btn {
  width: 120px;
  flex-shrink: 0;
}

/* 二维码登录 */
.qr-login-container {
  text-align: center;
}

.qr-code-wrapper {
  margin-bottom: 20px;
}

.qr-code {
  width: 200px;
  height: 200px;
  margin: 0 auto 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.qr-placeholder {
  text-align: center;
  color: #909399;
}

.qr-placeholder p {
  margin: 12px 0 0 0;
  font-size: 14px;
}

.qr-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #67c23a;
  font-size: 14px;
}

.qr-tips {
  color: #909399;
  font-size: 14px;
}

.qr-tips p {
  margin: 0 0 12px 0;
}

/* 第三方登录 */
.social-login {
  margin-top: 30px;
}

.divider {
  position: relative;
  text-align: center;
  margin-bottom: 20px;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e4e7ed;
}

.divider span {
  background: white;
  padding: 0 16px;
  color: #909399;
  font-size: 12px;
}

.social-buttons {
  display: flex;
  gap: 12px;
}

.social-btn {
  flex: 1;
  height: 40px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
}

.social-btn.wechat {
  background: #07c160;
  border-color: #07c160;
  color: white;
}

.social-btn.wechat:hover {
  background: #06ad56;
  border-color: #06ad56;
}

.social-btn.qq {
  background: #1296db;
  border-color: #1296db;
  color: white;
}

.social-btn.qq:hover {
  background: #0e7bc4;
  border-color: #0e7bc4;
}

.social-icon {
  width: 18px;
  height: 18px;
}

/* 忘记密码对话框 */
.el-dialog .sms-input-group {
  display: flex;
  gap: 12px;
}

.el-dialog .sms-input-group .el-input {
  flex: 1;
}

.el-dialog .sms-input-group .el-button {
  width: 100px;
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    padding: 10px;
  }
  
  .login-wrapper {
    height: auto;
    min-height: 500px;
    flex-direction: column;
  }
  
  .login-left {
    min-height: 200px;
  }
  
  .login-bg-content h2 {
    font-size: 24px;
  }
  
  .feature-list {
    flex-direction: row;
    justify-content: center;
    gap: 30px;
  }
  
  .login-right {
    padding: 40px 30px;
  }
  
  .login-tabs {
    flex-direction: column;
    gap: 4px;
  }
  
  .tab-item {
    justify-content: flex-start;
    padding: 12px 16px;
  }
  
  .social-buttons {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .login-right {
    padding: 30px 20px;
  }
  
  .qr-code {
    width: 160px;
    height: 160px;
  }
}
</style>