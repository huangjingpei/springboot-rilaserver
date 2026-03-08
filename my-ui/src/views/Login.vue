<template>
  <div class="portal-container">
    <div class="portal-header">
      <h1>RILA STREAMING SERVER</h1>
      <p class="subtitle">高性能 · 低延迟 · 全场景直播解决方案</p>
    </div>
    
    <div class="cards-wrapper">
      <!-- Register User -->
      <div class="plan-card register-card" @click="$router.push('/login/register')">
        <div class="card-header">
          <div class="icon-wrapper">
            <el-icon><User /></el-icon>
          </div>
          <h3>普通用户</h3>
          <p class="plan-type">REGISTER</p>
        </div>
        <div class="card-body">
          <ul class="feature-list">
            <li><el-icon><Check /></el-icon> 1路推流通道</li>
            <li><el-icon><Check /></el-icon> 1台推流设备</li>
            <li><el-icon><Check /></el-icon> 基础画质支持</li>
            <li><el-icon><Check /></el-icon> 社区技术支持</li>
          </ul>
        </div>
        <div class="card-footer">
          <el-button round class="action-btn">立即体验</el-button>
        </div>
      </div>

      <!-- Auth User -->
      <div class="plan-card auth-card" @click="$router.push('/login/auth')">
        <div class="popular-tag">RECOMMENDED</div>
        <div class="card-header">
          <div class="icon-wrapper">
            <el-icon><Trophy /></el-icon>
          </div>
          <h3>授权用户</h3>
          <p class="plan-type">PROFESSIONAL</p>
        </div>
        <div class="card-body">
          <ul class="feature-list">
            <li><el-icon><Check /></el-icon> 20路推流通道</li>
            <li><el-icon><Check /></el-icon> 5台推流设备</li>
            <li><el-icon><Check /></el-icon> 高清画质 (1080P)</li>
            <li><el-icon><Check /></el-icon> 优先技术支持</li>
          </ul>
        </div>
        <div class="card-footer">
          <el-button type="warning" round class="action-btn">进入工作台</el-button>
        </div>
      </div>

      <!-- Enterprise User -->
      <div class="plan-card enterprise-card" @click="$router.push('/login/enterprise')">
        <div class="card-header">
          <div class="icon-wrapper">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <h3>企业用户</h3>
          <p class="plan-type">ENTERPRISE</p>
        </div>
        <div class="card-body">
          <ul class="feature-list">
            <li><el-icon><Check /></el-icon> 100路推流通道</li>
            <li><el-icon><Check /></el-icon> 20台推流设备</li>
            <li><el-icon><Check /></el-icon> 4K/8K 超高清</li>
            <li><el-icon><Check /></el-icon> 专属客户经理</li>
          </ul>
        </div>
        <div class="card-footer">
          <el-button type="danger" round class="action-btn">企业登录</el-button>
        </div>
      </div>
    </div>
    
    <div class="portal-footer">
      <p>&copy; 2025 Rila Streaming Server. All Rights Reserved.</p>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { User, Trophy, OfficeBuilding, Check } from '@element-plus/icons-vue'

const router = useRouter()
</script>

<style scoped>
.portal-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
  color: #fff;
}

.portal-header {
  text-align: center;
  margin-bottom: 60px;
  animation: fadeInDown 1s ease-out;
}

.portal-header h1 {
  font-size: 3.5rem;
  margin: 0 0 15px;
  letter-spacing: 4px;
  background: linear-gradient(to right, #fff, #a5a5a5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 10px 20px rgba(0,0,0,0.3);
}

.subtitle {
  font-size: 1.2rem;
  color: rgba(255,255,255,0.7);
  letter-spacing: 2px;
  font-weight: 300;
}

.cards-wrapper {
  display: flex;
  gap: 30px;
  align-items: stretch; /* Make cards same height */
  justify-content: center;
  flex-wrap: nowrap; /* Force single row */
  max-width: 1400px;
  width: 100%;
  padding: 0 40px;
}

.plan-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 40px 30px;
  flex: 1; /* Distribute space evenly */
  min-width: 300px;
  max-width: 400px;
  height: auto; /* Let content or align-items determine height */
  min-height: 480px;
  display: flex;
  flex-direction: column;
  transition: all 0.4s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.plan-card:hover {
  transform: translateY(-15px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.4);
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.3);
}

/* Card Specific Styles */
.register-card .icon-wrapper { color: #409eff; background: rgba(64, 158, 255, 0.1); }
.register-card:hover { border-top: 4px solid #409eff; }

.auth-card {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(230, 162, 60, 0.3);
  z-index: 1;
}
.auth-card .icon-wrapper { color: #e6a23c; background: rgba(230, 162, 60, 0.1); }
.auth-card:hover { border-top: 4px solid #e6a23c; }

.enterprise-card .icon-wrapper { color: #7000ff; background: rgba(112, 0, 255, 0.1); }
.enterprise-card:hover { border-top: 4px solid #7000ff; }

.popular-tag {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  background: #e6a23c;
  color: #fff;
  padding: 5px 15px;
  border-bottom-left-radius: 10px;
  border-bottom-right-radius: 10px;
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 1px;
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
}

.icon-wrapper {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 28px;
  transition: all 0.3s;
}

.plan-card:hover .icon-wrapper {
  transform: scale(1.1);
}

h3 {
  font-size: 1.5rem;
  margin: 0 0 5px;
  color: #fff;
}

.plan-type {
  font-size: 0.9rem;
  color: rgba(255,255,255,0.5);
  letter-spacing: 2px;
  font-weight: 600;
}

.card-body {
  flex: 1;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
  text-align: left;
}

.feature-list li {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(255,255,255,0.8);
  font-size: 0.95rem;
}

.feature-list li .el-icon {
  color: #67c23a;
}

.card-footer {
  margin-top: 20px;
  text-align: center;
}

.action-btn {
  width: 80%;
  height: 40px;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.3s;
}

.register-card .action-btn { background: transparent; border: 1px solid #fff; color: #fff; }
.register-card .action-btn:hover { background: #fff; color: #000; }

.auth-card .action-btn { box-shadow: 0 4px 15px rgba(230, 162, 60, 0.4); }
.auth-card .action-btn:hover { transform: scale(1.05); }

.enterprise-card .action-btn { background: linear-gradient(45deg, #7000ff, #00f0ff); border: none; color: #fff; }
.enterprise-card .action-btn:hover { box-shadow: 0 4px 15px rgba(112, 0, 255, 0.4); }

.portal-footer {
  margin-top: 60px;
  color: rgba(255,255,255,0.3);
  font-size: 0.9rem;
}

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-30px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 1024px) {
  .cards-wrapper {
    flex-direction: column;
    align-items: center;
    height: auto;
  }
  .plan-card {
    width: 100%;
    max-width: 500px;
    min-height: auto;
  }
}
</style>
