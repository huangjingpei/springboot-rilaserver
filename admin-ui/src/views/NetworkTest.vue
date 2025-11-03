<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>网络测试</span>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>
              <span>连接测试</span>
            </template>
            <el-form :model="testForm" label-width="100px">
              <el-form-item label="目标地址">
                <el-input v-model="testForm.target" placeholder="请输入IP地址或域名" />
              </el-form-item>
              <el-form-item label="端口">
                <el-input-number v-model="testForm.port" :min="1" :max="65535" />
              </el-form-item>
              <el-form-item label="超时时间">
                <el-input-number v-model="testForm.timeout" :min="1" :max="60" />
                <span style="margin-left: 10px;">秒</span>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="runConnectTest" :loading="testing">
                  开始测试
                </el-button>
                <el-button @click="clearResults">清空结果</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>
              <span>Ping测试</span>
            </template>
            <el-form :model="pingForm" label-width="100px">
              <el-form-item label="目标地址">
                <el-input v-model="pingForm.target" placeholder="请输入IP地址或域名" />
              </el-form-item>
              <el-form-item label="次数">
                <el-input-number v-model="pingForm.count" :min="1" :max="10" />
              </el-form-item>
              <el-form-item>
                <el-button type="success" @click="runPingTest" :loading="pinging">
                  开始Ping
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="24">
          <el-card shadow="never">
            <template #header>
              <span>测试结果</span>
            </template>
            <div class="test-results">
              <el-timeline v-if="results.length > 0">
                <el-timeline-item
                  v-for="(result, index) in results"
                  :key="index"
                  :timestamp="result.timestamp"
                  :type="result.success ? 'success' : 'danger'"
                >
                  <div class="result-item">
                    <div class="result-title">{{ result.title }}</div>
                    <div class="result-content">{{ result.content }}</div>
                    <div v-if="result.details" class="result-details">
                      <pre>{{ result.details }}</pre>
                    </div>
                  </div>
                </el-timeline-item>
              </el-timeline>
              <el-empty v-else description="暂无测试结果" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { testConnection, testPing } from '../api/network';

const testForm = ref({
  target: 'localhost',
  port: 8080,
  timeout: 5
});

const pingForm = ref({
  target: 'localhost',
  count: 4
});

const testing = ref(false);
const pinging = ref(false);
const results = ref([]);

const runConnectTest = async () => {
  if (!testForm.value.target) {
    ElMessage.warning('请输入目标地址');
    return;
  }
  
  testing.value = true;
  const timestamp = new Date().toLocaleString();
  
  try {
    const response = await testConnection({
      target: testForm.value.target,
      port: testForm.value.port,
      timeout: testForm.value.timeout
    });
    
    results.value.unshift({
      timestamp,
      title: `连接测试 - ${testForm.value.target}:${testForm.value.port}`,
      content: response.data.success ? '连接成功' : '连接失败',
      success: response.data.success,
      details: response.data.message || response.data.details
    });
    
    ElMessage.success('测试完成');
  } catch (error) {
    results.value.unshift({
      timestamp,
      title: `连接测试 - ${testForm.value.target}:${testForm.value.port}`,
      content: '测试失败',
      success: false,
      details: error.message || '网络错误'
    });
    
    ElMessage.error('测试失败');
  } finally {
    testing.value = false;
  }
};

const runPingTest = async () => {
  if (!pingForm.value.target) {
    ElMessage.warning('请输入目标地址');
    return;
  }
  
  pinging.value = true;
  const timestamp = new Date().toLocaleString();
  
  try {
    const response = await testPing({
      target: pingForm.value.target,
      count: pingForm.value.count
    });
    
    results.value.unshift({
      timestamp,
      title: `Ping测试 - ${pingForm.value.target}`,
      content: response.data.success ? 'Ping成功' : 'Ping失败',
      success: response.data.success,
      details: response.data.output || response.data.message
    });
    
    ElMessage.success('Ping完成');
  } catch (error) {
    results.value.unshift({
      timestamp,
      title: `Ping测试 - ${pingForm.value.target}`,
      content: 'Ping失败',
      success: false,
      details: error.message || '网络错误'
    });
    
    ElMessage.error('Ping失败');
  } finally {
    pinging.value = false;
  }
};

const clearResults = () => {
  results.value = [];
  ElMessage.info('结果已清空');
};
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

.test-results {
  max-height: 400px;
  overflow-y: auto;
}

.result-item {
  margin-bottom: 10px;
}

.result-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.result-content {
  margin-bottom: 5px;
}

.result-details {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
}

.result-details pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>