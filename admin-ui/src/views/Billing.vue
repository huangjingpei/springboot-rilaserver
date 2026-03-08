<template>
  <div class="page">
    <div class="header">
      <h2>账单与结算</h2>
      <div class="ops">
        <el-date-picker v-model="month" type="month" placeholder="选择月份" @change="fetchData" />
        <el-button type="primary" @click="fetchData">刷新</el-button>
      </div>
    </div>
    <el-alert title="账单数据接口待接入，当前为占位页面" type="info" show-icon style="margin-bottom: 12px" />
    <el-table :data="rows" style="width: 100%">
      <el-table-column prop="user" label="用户" width="140" />
      <el-table-column prop="month" label="月份" width="120" />
      <el-table-column prop="liveSeconds" label="推流时长(秒)" width="140" />
      <el-table-column prop="packageHours" label="套餐内(小时)" width="140" />
      <el-table-column prop="overHours" label="超出(小时)" width="120" />
      <el-table-column prop="amount" label="应付(¥)" width="120" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === '已结算' ? 'success' : 'warning'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const rows = ref([])
const month = ref('')

const fetchData = () => {
  rows.value = [
    { user: 'userA', month: '2026-03', liveSeconds: 36000, packageHours: 10, overHours: 0, amount: 0, status: '已结算' },
    { user: 'userB', month: '2026-03', liveSeconds: 90000, packageHours: 20, overHours: 5, amount: 150, status: '待结算' }
  ]
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
