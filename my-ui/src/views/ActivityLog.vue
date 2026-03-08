<template>
  <div class="page">
    <div class="header">
      <h2>操作与状态日志</h2>
      <el-button type="primary" @click="fetchData" :loading="loading">刷新</el-button>
    </div>
    <el-table :data="rows" style="width: 100%" v-loading="loading" empty-text="暂无日志">
      <el-table-column prop="changeTime" label="时间" width="180" />
      <el-table-column prop="configId" label="配置ID" width="100" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="row.newStatus ? 'success' : 'danger'">
            {{ row.newStatus ? '开始' : '结束' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="changeReason" label="原因" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const rows = ref([])
const userId = localStorage.getItem('stream_user') || ''

const fetchData = async () => {
  if (!userId) {
    ElMessage.error('未获取到用户ID')
    return
  }
  loading.value = true
  try {
    const res = await axios.get('/api/live/status/client-logs', {
      params: { clientId: userId }
    })
    rows.value = res.data?.logs || []
  } catch (e) {
    ElMessage.error('获取日志失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page {
  padding: 16px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
</style>
