<template>
  <div class="page">
    <div class="header">
      <h2>直播会话时长</h2>
      <div class="ops">
        <el-button type="primary" @click="fetchData" :loading="loading">刷新</el-button>
        <el-select v-model="days" style="width: 120px" @change="fetchData">
          <el-option :value="1" label="近1天" />
          <el-option :value="7" label="近7天" />
          <el-option :value="30" label="近30天" />
        </el-select>
      </div>
    </div>
    <el-table :data="rows" style="width: 100%" v-loading="loading" empty-text="暂无会话">
      <el-table-column prop="configId" label="配置ID" width="100" />
      <el-table-column prop="startTime" label="开始时间" width="180" />
      <el-table-column prop="endTime" label="结束时间" width="180" />
      <el-table-column label="时长">
        <template #default="{ row }">
          {{ formatDuration(row.durationSeconds) }}
        </template>
      </el-table-column>
    </el-table>
  </div>
  </template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const rows = ref([])
const days = ref(7)
const userId = localStorage.getItem('stream_user') || ''

const formatDuration = (s) => {
  const sec = Number(s || 0)
  const h = Math.floor(sec / 3600)
  const m = Math.floor((sec % 3600) / 60)
  const r = sec % 60
  const pad = (n) => n.toString().padStart(2, '0')
  return `${pad(h)}:${pad(m)}:${pad(r)}`
}

const fetchData = async () => {
  if (!userId) {
    ElMessage.error('未获取到用户ID')
    return
  }
  loading.value = true
  try {
    const res = await axios.get('/api/live/sessions', {
      params: { clientId: userId, days: days.value }
    })
    rows.value = res.data?.sessions || []
  } catch (e) {
    ElMessage.error('获取会话失败')
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
.ops {
  display: flex;
  gap: 8px;
}
</style>
