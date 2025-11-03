<template>
  <div class="app-container">
    <!-- 搜索和操作栏 -->
    <div class="filter-container">
      <el-input
        v-model="listQuery.search"
        placeholder="搜索流媒体名称或密钥"
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
        <el-option label="活跃" value="active" />
        <el-option label="非活跃" value="inactive" />
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
        type="primary"
        icon="Plus"
        @click="handleCreate"
      >
        添加流媒体
      </el-button>
      <el-button
        class="filter-item"
        type="success"
        icon="Download"
        @click="handleExport"
      >
        导出
      </el-button>
    </div>

    <!-- 表格 -->
    <el-table
      v-loading="listLoading"
      :data="list"
      element-loading-text="加载中..."
      border
      fit
      highlight-current-row
      style="width: 100%"
    >
      <el-table-column align="center" label="ID" width="80">
        <template #default="scope">
          {{ scope.row.id }}
        </template>
      </el-table-column>
      <el-table-column label="流媒体名称" min-width="150">
        <template #default="scope">
          {{ scope.row.name || scope.row.key }}
        </template>
      </el-table-column>
      <el-table-column label="流密钥" min-width="200">
        <template #default="scope">
          <span class="stream-key">{{ scope.row.key }}</span>
          <el-button
            type="text"
            size="small"
            @click="copyToClipboard(scope.row.key)"
          >
            复制
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="推流地址" min-width="250">
        <template #default="scope">
          <span class="stream-url">{{ getStreamUrl(scope.row.key) }}</span>
          <el-button
            type="text"
            size="small"
            @click="copyToClipboard(getStreamUrl(scope.row.key))"
          >
            复制
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="开始时间" width="180" align="center">
        <template #default="scope">
          <span>{{ formatDate(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="观看人数" width="100" align="center">
        <template #default="scope">
          {{ scope.row.viewerCount || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleUpdate(scope.row)">
            编辑
          </el-button>
          <el-button
            v-if="scope.row.status === 'active'"
            size="small"
            type="warning"
            @click="handleStop(scope.row)"
          >
            停止
          </el-button>
          <el-button
            v-else
            size="small"
            type="success"
            @click="handleStart(scope.row)"
          >
            启动
          </el-button>
          <el-button
            size="small"
            type="danger"
            @click="handleDelete(scope.row)"
          >
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

    <!-- 添加/编辑对话框 -->
    <el-dialog
      :title="dialogStatus === 'create' ? '添加流媒体' : '编辑流媒体'"
      v-model="dialogFormVisible"
      width="500px"
    >
      <el-form
        ref="dataForm"
        :rules="rules"
        :model="temp"
        label-position="left"
        label-width="100px"
        style="width: 400px; margin-left: 50px;"
      >
        <el-form-item label="流媒体名称" prop="name">
          <el-input v-model="temp.name" placeholder="请输入流媒体名称" />
        </el-form-item>
        <el-form-item label="流密钥" prop="key">
          <el-input v-model="temp.key" placeholder="请输入流密钥" />
          <el-button
            type="text"
            size="small"
            @click="generateStreamKey"
          >
            生成随机密钥
          </el-button>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="temp.description"
            type="textarea"
            placeholder="请输入描述"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="temp.status" placeholder="请选择状态">
            <el-option label="活跃" value="active" />
            <el-option label="非活跃" value="inactive" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">
            取消
          </el-button>
          <el-button
            type="primary"
            @click="dialogStatus === 'create' ? createData() : updateData()"
          >
            确认
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getStreams, createStream, updateStream, deleteStream, startStream, stopStream } from '../api/stream';
import { ElMessage, ElMessageBox } from 'element-plus';
import Pagination from '../components/Pagination.vue';

export default {
  name: 'StreamManagement',
  components: {
    Pagination
  },
  data() {
    return {
      list: [],
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        search: '',
        status: ''
      },
      dialogFormVisible: false,
      dialogStatus: '',
      temp: {
        id: undefined,
        name: '',
        key: '',
        description: '',
        status: 'inactive'
      },
      rules: {
        name: [{ required: true, message: '流媒体名称不能为空', trigger: 'blur' }],
        key: [{ required: true, message: '流密钥不能为空', trigger: 'blur' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.listLoading = true;
      getStreams(this.listQuery).then(response => {
        this.list = response.data.items || Object.values(response.data) || [];
        this.total = response.data.total || this.list.length;
        this.listLoading = false;
      }).catch(() => {
        this.listLoading = false;
      });
    },
    handleFilter() {
      this.listQuery.page = 1;
      this.getList();
    },
    handleCreate() {
      this.resetTemp();
      this.dialogStatus = 'create';
      this.dialogFormVisible = true;
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate();
      });
    },
    handleUpdate(row) {
      this.temp = Object.assign({}, row);
      this.dialogStatus = 'update';
      this.dialogFormVisible = true;
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate();
      });
    },
    handleDelete(row) {
      ElMessageBox.confirm('此操作将永久删除该流媒体, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteStream(row.id).then(() => {
          ElMessage.success('删除成功');
          this.getList();
        });
      });
    },
    handleStart(row) {
      startStream(row.id).then(() => {
        ElMessage.success('启动成功');
        this.getList();
      });
    },
    handleStop(row) {
      stopStream(row.id).then(() => {
        ElMessage.success('停止成功');
        this.getList();
      });
    },
    handleExport() {
      ElMessage.info('导出功能开发中...');
    },
    createData() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          createStream(this.temp).then(() => {
            this.list.unshift(this.temp);
            this.dialogFormVisible = false;
            ElMessage.success('创建成功');
            this.getList();
          });
        }
      });
    },
    updateData() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          const tempData = Object.assign({}, this.temp);
          updateStream(tempData.id, tempData).then(() => {
            const index = this.list.findIndex(v => v.id === this.temp.id);
            this.list.splice(index, 1, this.temp);
            this.dialogFormVisible = false;
            ElMessage.success('更新成功');
            this.getList();
          });
        }
      });
    },
    resetTemp() {
      this.temp = {
        id: undefined,
        name: '',
        key: '',
        description: '',
        status: 'inactive'
      };
    },
    generateStreamKey() {
      const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
      let result = '';
      for (let i = 0; i < 16; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length));
      }
      this.temp.key = result;
    },
    getStreamUrl(key) {
      return `rtmp://localhost:1935/live/${key}`;
    },
    copyToClipboard(text) {
      navigator.clipboard.writeText(text).then(() => {
        ElMessage.success('已复制到剪贴板');
      }).catch(() => {
        ElMessage.error('复制失败');
      });
    },
    getStatusType(status) {
      const statusMap = {
        active: 'success',
        inactive: 'info'
      };
      return statusMap[status] || 'info';
    },
    getStatusText(status) {
      const statusMap = {
        active: '活跃',
        inactive: '非活跃'
      };
      return statusMap[status] || status;
    },
    formatDate(date) {
      if (!date) return '-';
      return new Date(date).toLocaleString('zh-CN');
    }
  }
};
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.filter-container {
  padding-bottom: 10px;
}

.filter-container .filter-item {
  display: inline-block;
  vertical-align: middle;
  margin-bottom: 10px;
  margin-right: 10px;
}

.stream-key, .stream-url {
  font-family: monospace;
  font-size: 12px;
  color: #666;
}

.dialog-footer {
  text-align: right;
}
</style>