<template>
  <div class="app-container">
    <!-- 搜索和操作栏 -->
    <div class="filter-container">
      <el-input
        v-model="listQuery.search"
        placeholder="搜索应用名称"
        style="width: 200px;"
        class="filter-item"
        @keyup.enter="handleFilter"
      />
      <el-select
        v-model="listQuery.status"
        placeholder="状态"
        clearable
        style="width: 120px"
        class="filter-item"
      >
        <el-option label="运行中" value="running" />
        <el-option label="已停止" value="stopped" />
        <el-option label="错误" value="error" />
      </el-select>
      <el-button
        class="filter-item"
        type="primary"
        icon="Search"
        @click="handleFilter"
      >
        搜索
      </el-button>
      <el-button
        class="filter-item"
        style="margin-left: 10px;"
        type="primary"
        icon="Plus"
        @click="handleCreate"
      >
        添加应用
      </el-button>
      <el-button
        class="filter-item"
        type="success"
        icon="Refresh"
        @click="handleRefresh"
      >
        刷新
      </el-button>
    </div>

    <!-- 应用表格 -->
    <el-table
      v-loading="listLoading"
      :data="list"
      element-loading-text="Loading"
      border
      fit
      highlight-current-row
      style="width: 100%;"
    >
      <el-table-column align="center" label="ID" width="80">
        <template v-slot="scope">
          {{ scope.row.id }}
        </template>
      </el-table-column>
      <el-table-column label="应用名称" width="150">
        <template v-slot="scope">
          <span class="link-type" @click="handleUpdate(scope.row)">{{ scope.row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="描述" width="200">
        <template v-slot="scope">
          <span>{{ scope.row.description || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="版本" width="100" align="center">
        <template v-slot="scope">
          <el-tag size="small">{{ scope.row.version }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template v-slot="scope">
          <el-tag :type="statusFilter(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="端口" width="80" align="center">
        <template v-slot="scope">
          <span>{{ scope.row.port || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="CPU使用率" width="100" align="center">
        <template v-slot="scope">
          <el-progress
            :percentage="scope.row.cpuUsage || 0"
            :color="getProgressColor(scope.row.cpuUsage || 0)"
            :stroke-width="6"
            :show-text="false"
          />
          <span style="margin-left: 8px;">{{ scope.row.cpuUsage || 0 }}%</span>
        </template>
      </el-table-column>
      <el-table-column label="内存使用" width="120" align="center">
        <template v-slot="scope">
          <span>{{ formatMemory(scope.row.memoryUsage) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="160" align="center">
        <template v-slot="scope">
          <span>{{ formatDate(scope.row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="250" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button
            v-if="scope.row.status === 'stopped'"
            size="small"
            type="success"
            @click="handleStart(scope.row)"
          >
            启动
          </el-button>
          <el-button
            v-if="scope.row.status === 'running'"
            size="small"
            type="warning"
            @click="handleStop(scope.row)"
          >
            停止
          </el-button>
          <el-button
            size="small"
            type="info"
            @click="handleRestart(scope.row)"
          >
            重启
          </el-button>
          <el-button type="primary" size="small" @click="handleUpdate(scope.row)">
            编辑
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="listQuery.page"
      :limit.sync="listQuery.limit"
      @pagination="getList"
    />

    <!-- 应用编辑对话框 -->
    <el-dialog :title="textMap[dialogStatus]" v-model="dialogFormVisible" width="600px">
      <el-form
        ref="dataForm"
        :rules="rules"
        :model="temp"
        label-position="left"
        label-width="100px"
        style="width: 500px; margin-left:50px;"
      >
        <el-form-item label="应用名称" prop="name">
          <el-input v-model="temp.name" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="temp.description"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 4}"
            placeholder="请输入应用描述"
          />
        </el-form-item>
        <el-form-item label="版本" prop="version">
          <el-input v-model="temp.version" placeholder="例如: 1.0.0" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="temp.port" :min="1" :max="65535" />
        </el-form-item>
        <el-form-item label="启动命令" prop="startCommand">
          <el-input v-model="temp.startCommand" placeholder="例如: npm start" />
        </el-form-item>
        <el-form-item label="工作目录" prop="workingDirectory">
          <el-input v-model="temp.workingDirectory" placeholder="例如: /app" />
        </el-form-item>
        <el-form-item label="环境变量">
          <el-input
            v-model="temp.environment"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 6}"
            placeholder="KEY1=value1&#10;KEY2=value2"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="temp.status" class="filter-item" placeholder="请选择">
            <el-option label="运行中" value="running" />
            <el-option label="已停止" value="stopped" />
            <el-option label="错误" value="error" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">
            取消
          </el-button>
          <el-button type="primary" @click="dialogStatus === 'create' ? createData() : updateData()">
            确认
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getApps, createApp, updateApp, deleteApp, startApp, stopApp, restartApp } from '../api/app';
import Pagination from '../components/Pagination.vue';

// 数据
const list = ref([]);
const total = ref(0);
const listLoading = ref(true);
const dialogFormVisible = ref(false);
const dialogStatus = ref('');
const textMap = {
  update: '编辑应用',
  create: '创建应用'
};

// 查询参数
const listQuery = ref({
  page: 1,
  limit: 20,
  search: '',
  status: ''
});

// 表单数据
const temp = ref({
  id: undefined,
  name: '',
  description: '',
  version: '1.0.0',
  port: 3000,
  startCommand: '',
  workingDirectory: '',
  environment: '',
  status: 'stopped'
});

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口号', trigger: 'blur' }],
  startCommand: [{ required: true, message: '请输入启动命令', trigger: 'blur' }]
};

// 方法
const getList = async () => {
  listLoading.value = true;
  try {
    const response = await getApps(listQuery.value);
    list.value = response.data.items || response.data;
    total.value = response.data.total || list.value.length;
  } catch (error) {
    console.error('获取应用列表失败:', error);
    ElMessage.error('获取应用列表失败');
  } finally {
    listLoading.value = false;
  }
};

const handleFilter = () => {
  listQuery.value.page = 1;
  getList();
};

const handleRefresh = () => {
  getList();
};

const handleCreate = () => {
  resetTemp();
  dialogStatus.value = 'create';
  dialogFormVisible.value = true;
  nextTick(() => {
    // 清除表单验证
  });
};

const handleUpdate = (row) => {
  temp.value = Object.assign({}, row);
  dialogStatus.value = 'update';
  dialogFormVisible.value = true;
  nextTick(() => {
    // 清除表单验证
  });
};

const handleDelete = (row) => {
  ElMessageBox.confirm('此操作将永久删除该应用, 是否继续?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteApp(row.id);
      ElMessage.success('删除成功');
      getList();
    } catch (error) {
      ElMessage.error('删除失败');
    }
  });
};

const handleStart = async (row) => {
  try {
    await startApp(row.id);
    ElMessage.success('应用启动成功');
    getList();
  } catch (error) {
    ElMessage.error('应用启动失败');
  }
};

const handleStop = async (row) => {
  try {
    await stopApp(row.id);
    ElMessage.success('应用停止成功');
    getList();
  } catch (error) {
    ElMessage.error('应用停止失败');
  }
};

const handleRestart = async (row) => {
  try {
    await restartApp(row.id);
    ElMessage.success('应用重启成功');
    getList();
  } catch (error) {
    ElMessage.error('应用重启失败');
  }
};

const createData = async () => {
  try {
    await createApp(temp.value);
    ElMessage.success('创建成功');
    dialogFormVisible.value = false;
    getList();
  } catch (error) {
    ElMessage.error('创建失败');
  }
};

const updateData = async () => {
  try {
    await updateApp(temp.value.id, temp.value);
    ElMessage.success('更新成功');
    dialogFormVisible.value = false;
    getList();
  } catch (error) {
    ElMessage.error('更新失败');
  }
};

const resetTemp = () => {
  temp.value = {
    id: undefined,
    name: '',
    description: '',
    version: '1.0.0',
    port: 3000,
    startCommand: '',
    workingDirectory: '',
    environment: '',
    status: 'stopped'
  };
};

const statusFilter = (status) => {
  const statusMap = {
    running: 'success',
    stopped: 'info',
    error: 'danger'
  };
  return statusMap[status];
};

const getStatusText = (status) => {
  const statusMap = {
    running: '运行中',
    stopped: '已停止',
    error: '错误'
  };
  return statusMap[status] || status;
};

const getProgressColor = (percentage) => {
  if (percentage < 50) return '#67c23a';
  if (percentage < 80) return '#e6a23c';
  return '#f56c6c';
};

const formatMemory = (memory) => {
  if (!memory) return '-';
  if (memory < 1024) return `${memory}MB`;
  return `${(memory / 1024).toFixed(1)}GB`;
};

const formatDate = (date) => {
  if (!date) return '-';
  return new Date(date).toLocaleString('zh-CN');
};

// 生命周期
onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>
.app-container {
  .filter-container {
    padding: 20px 0;
    
    .filter-item {
      display: inline-block;
      vertical-align: middle;
      margin-bottom: 10px;
      margin-right: 10px;
    }
  }

  .link-type {
    color: #409EFF;
    cursor: pointer;
    
    &:hover {
      color: #66b1ff;
    }
  }
}
</style>