<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>系统设置</span>
            </div>
          </template>
          
          <el-tabs v-model="activeTab" type="border-card">
            <!-- 基本设置 -->
            <el-tab-pane label="基本设置" name="basic">
              <el-form :model="basicSettings" label-width="120px" style="max-width: 600px;">
                <el-form-item label="系统名称">
                  <el-input v-model="basicSettings.systemName" />
                </el-form-item>
                <el-form-item label="系统描述">
                  <el-input v-model="basicSettings.systemDescription" type="textarea" :rows="3" />
                </el-form-item>
                <el-form-item label="管理员邮箱">
                  <el-input v-model="basicSettings.adminEmail" />
                </el-form-item>
                <el-form-item label="系统时区">
                  <el-select v-model="basicSettings.timezone" placeholder="请选择时区">
                    <el-option label="北京时间 (UTC+8)" value="Asia/Shanghai" />
                    <el-option label="东京时间 (UTC+9)" value="Asia/Tokyo" />
                    <el-option label="纽约时间 (UTC-5)" value="America/New_York" />
                    <el-option label="伦敦时间 (UTC+0)" value="Europe/London" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveBasicSettings">保存设置</el-button>
                  <el-button @click="resetBasicSettings">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <!-- 安全设置 -->
            <el-tab-pane label="安全设置" name="security">
              <el-form :model="securitySettings" label-width="120px" style="max-width: 600px;">
                <el-form-item label="密码最小长度">
                  <el-input-number v-model="securitySettings.minPasswordLength" :min="6" :max="20" />
                </el-form-item>
                <el-form-item label="登录失败限制">
                  <el-input-number v-model="securitySettings.maxLoginAttempts" :min="3" :max="10" />
                </el-form-item>
                <el-form-item label="会话超时时间">
                  <el-input-number v-model="securitySettings.sessionTimeout" :min="30" :max="1440" />
                  <span style="margin-left: 10px;">分钟</span>
                </el-form-item>
                <el-form-item label="启用双因子认证">
                  <el-switch v-model="securitySettings.enableTwoFactor" />
                </el-form-item>
                <el-form-item label="强制HTTPS">
                  <el-switch v-model="securitySettings.forceHttps" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveSecuritySettings">保存设置</el-button>
                  <el-button @click="resetSecuritySettings">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <!-- 流媒体设置 -->
            <el-tab-pane label="流媒体设置" name="streaming">
              <el-form :model="streamingSettings" label-width="120px" style="max-width: 600px;">
                <el-form-item label="RTMP端口">
                  <el-input-number v-model="streamingSettings.rtmpPort" :min="1024" :max="65535" />
                </el-form-item>
                <el-form-item label="HTTP端口">
                  <el-input-number v-model="streamingSettings.httpPort" :min="1024" :max="65535" />
                </el-form-item>
                <el-form-item label="最大并发流数">
                  <el-input-number v-model="streamingSettings.maxStreams" :min="1" :max="1000" />
                </el-form-item>
                <el-form-item label="录制存储路径">
                  <el-input v-model="streamingSettings.recordPath" />
                </el-form-item>
                <el-form-item label="启用录制">
                  <el-switch v-model="streamingSettings.enableRecord" />
                </el-form-item>
                <el-form-item label="启用转码">
                  <el-switch v-model="streamingSettings.enableTranscode" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveStreamingSettings">保存设置</el-button>
                  <el-button @click="resetStreamingSettings">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <!-- 系统维护 -->
            <el-tab-pane label="系统维护" name="maintenance">
              <div class="maintenance-section">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-card shadow="never">
                      <template #header>
                        <span>数据库维护</span>
                      </template>
                      <div class="maintenance-item">
                        <p>清理过期的日志数据</p>
                        <el-button type="warning" @click="cleanupLogs">清理日志</el-button>
                      </div>
                      <div class="maintenance-item">
                        <p>优化数据库性能</p>
                        <el-button type="primary" @click="optimizeDatabase">优化数据库</el-button>
                      </div>
                      <div class="maintenance-item">
                        <p>备份数据库</p>
                        <el-button type="success" @click="backupDatabase">创建备份</el-button>
                      </div>
                    </el-card>
                  </el-col>
                  
                  <el-col :span="12">
                    <el-card shadow="never">
                      <template #header>
                        <span>系统信息</span>
                      </template>
                      <div class="system-info">
                        <div class="info-item">
                          <span class="label">系统版本:</span>
                          <span class="value">{{ systemInfo.version }}</span>
                        </div>
                        <div class="info-item">
                          <span class="label">Java版本:</span>
                          <span class="value">{{ systemInfo.javaVersion }}</span>
                        </div>
                        <div class="info-item">
                          <span class="label">运行时间:</span>
                          <span class="value">{{ systemInfo.uptime }}</span>
                        </div>
                        <div class="info-item">
                          <span class="label">内存使用:</span>
                          <span class="value">{{ systemInfo.memoryUsage }}</span>
                        </div>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getSystemSettings, updateSystemSettings, getSystemInfo, cleanupSystemLogs, optimizeSystemDatabase, backupSystemDatabase } from '../api/system';

const activeTab = ref('basic');

const basicSettings = ref({
  systemName: '流媒体管理系统',
  systemDescription: '基于Spring Boot的流媒体管理平台',
  adminEmail: 'admin@example.com',
  timezone: 'Asia/Shanghai'
});

const securitySettings = ref({
  minPasswordLength: 8,
  maxLoginAttempts: 5,
  sessionTimeout: 120,
  enableTwoFactor: false,
  forceHttps: false
});

const streamingSettings = ref({
  rtmpPort: 1935,
  httpPort: 8080,
  maxStreams: 100,
  recordPath: '/var/recordings',
  enableRecord: true,
  enableTranscode: false
});

const systemInfo = ref({
  version: '1.0.0',
  javaVersion: 'OpenJDK 11.0.2',
  uptime: '5天 12小时 30分钟',
  memoryUsage: '512MB / 2GB'
});

const loadSettings = async () => {
  try {
    const response = await getSystemSettings();
    if (response.data) {
      Object.assign(basicSettings.value, response.data.basic || {});
      Object.assign(securitySettings.value, response.data.security || {});
      Object.assign(streamingSettings.value, response.data.streaming || {});
    }
  } catch (error) {
    console.error('加载设置失败:', error);
  }
};

const loadSystemInfo = async () => {
  try {
    const response = await getSystemInfo();
    if (response.data) {
      Object.assign(systemInfo.value, response.data);
    }
  } catch (error) {
    console.error('加载系统信息失败:', error);
  }
};

const saveBasicSettings = async () => {
  try {
    await updateSystemSettings({ basic: basicSettings.value });
    ElMessage.success('基本设置保存成功');
  } catch (error) {
    ElMessage.error('保存失败');
  }
};

const saveSecuritySettings = async () => {
  try {
    await updateSystemSettings({ security: securitySettings.value });
    ElMessage.success('安全设置保存成功');
  } catch (error) {
    ElMessage.error('保存失败');
  }
};

const saveStreamingSettings = async () => {
  try {
    await updateSystemSettings({ streaming: streamingSettings.value });
    ElMessage.success('流媒体设置保存成功');
  } catch (error) {
    ElMessage.error('保存失败');
  }
};

const resetBasicSettings = () => {
  basicSettings.value = {
    systemName: '流媒体管理系统',
    systemDescription: '基于Spring Boot的流媒体管理平台',
    adminEmail: 'admin@example.com',
    timezone: 'Asia/Shanghai'
  };
};

const resetSecuritySettings = () => {
  securitySettings.value = {
    minPasswordLength: 8,
    maxLoginAttempts: 5,
    sessionTimeout: 120,
    enableTwoFactor: false,
    forceHttps: false
  };
};

const resetStreamingSettings = () => {
  streamingSettings.value = {
    rtmpPort: 1935,
    httpPort: 8080,
    maxStreams: 100,
    recordPath: '/var/recordings',
    enableRecord: true,
    enableTranscode: false
  };
};

const cleanupLogs = async () => {
  try {
    await ElMessageBox.confirm('确定要清理过期的日志数据吗？', '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await cleanupSystemLogs();
    ElMessage.success('日志清理完成');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清理失败');
    }
  }
};

const optimizeDatabase = async () => {
  try {
    await ElMessageBox.confirm('数据库优化可能需要一些时间，确定要继续吗？', '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await optimizeSystemDatabase();
    ElMessage.success('数据库优化完成');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('优化失败');
    }
  }
};

const backupDatabase = async () => {
  try {
    await ElMessageBox.confirm('确定要创建数据库备份吗？', '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    });
    
    await backupSystemDatabase();
    ElMessage.success('备份创建完成');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('备份失败');
    }
  }
};

onMounted(() => {
  loadSettings();
  loadSystemInfo();
});
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.maintenance-section {
  padding: 20px 0;
}

.maintenance-item {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.maintenance-item p {
  margin: 0 0 10px 0;
  color: #606266;
}

.system-info {
  padding: 10px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  font-weight: bold;
  color: #606266;
}

.value {
  color: #303133;
}
</style>