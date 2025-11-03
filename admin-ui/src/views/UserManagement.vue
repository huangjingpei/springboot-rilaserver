<template>
  <div class="app-container">
    <!-- 搜索和操作栏 -->
    <div class="filter-container">
      <el-input
        v-model="listQuery.search"
        placeholder="搜索用户名或邮箱"
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
        <el-option label="禁用" value="inactive" />
        <el-option label="封禁" value="banned" />
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
        添加用户
      </el-button>
      <el-button
        class="filter-item"
        type="warning"
        icon="Download"
        @click="handleDownload"
      >
        导出
      </el-button>
    </div>

    <!-- 用户表格 -->
    <el-table
      v-loading="listLoading"
      :data="list"
      element-loading-text="Loading"
      border
      fit
      highlight-current-row
      style="width: 100%;"
    >
      <el-table-column align="center" label="ID" width="95">
        <template v-slot="scope">
          {{ scope.row.id }}
        </template>
      </el-table-column>
      <el-table-column label="姓名" width="150">
        <template v-slot="scope">
          {{ scope.row.firstName }} {{ scope.row.lastName }}
        </template>
      </el-table-column>
      <el-table-column label="邮箱" width="200" align="center">
        <template v-slot="scope">
          <span>{{ scope.row.email }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110" align="center">
        <template v-slot="scope">
          <el-tag :type="statusFilter(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="最后登录" width="180">
        <template v-slot="scope">
          <span>{{ formatDate(scope.row.lastLoginAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="注册时间" width="180">
        <template v-slot="scope">
          <span>{{ formatDate(scope.row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button type="primary" size="small" @click="handleUpdate(scope.row)">
            编辑
          </el-button>
          <el-button
            v-if="scope.row.status !== 'banned'"
            size="small"
            type="danger"
            @click="handleModifyStatus(scope.row, 'banned')"
          >
            封禁
          </el-button>
          <el-button
            v-else
            size="small"
            type="success"
            @click="handleModifyStatus(scope.row, 'active')"
          >
            解封
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

    <!-- 用户编辑对话框 -->
    <el-dialog :title="textMap[dialogStatus]" v-model="dialogFormVisible">
      <el-form
        ref="dataForm"
        :rules="rules"
        :model="temp"
        label-position="left"
        label-width="100px"
        style="width: 400px; margin-left:50px;"
      >
        <el-form-item label="姓" prop="firstName">
          <el-input v-model="temp.firstName" />
        </el-form-item>
        <el-form-item label="名" prop="lastName">
          <el-input v-model="temp.lastName" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="temp.email" />
        </el-form-item>
        <el-form-item v-if="dialogStatus === 'create'" label="密码" prop="password">
          <el-input v-model="temp.password" type="password" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="temp.status" class="filter-item" placeholder="请选择">
            <el-option label="活跃" value="active" />
            <el-option label="禁用" value="inactive" />
            <el-option label="封禁" value="banned" />
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
import { getUsers, createUser, updateUser, deleteUser } from '../api/user';
import Pagination from '../components/Pagination.vue';

// 数据
const list = ref([]);
const total = ref(0);
const listLoading = ref(true);
const dialogFormVisible = ref(false);
const dialogStatus = ref('');
const textMap = {
  update: '编辑用户',
  create: '创建用户'
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
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  status: 'active'
});

// 表单验证规则
const rules = {
  firstName: [{ required: true, message: '请输入姓', trigger: 'blur' }],
  lastName: [{ required: true, message: '请输入名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

// 方法
const getList = async () => {
  listLoading.value = true;
  try {
    const response = await getUsers(listQuery.value);
    list.value = response.data.items || response.data;
    total.value = response.data.total || list.value.length;
  } catch (error) {
    console.error('获取用户列表失败:', error);
    ElMessage.error('获取用户列表失败');
  } finally {
    listLoading.value = false;
  }
};

const handleFilter = () => {
  listQuery.value.page = 1;
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
  ElMessageBox.confirm('此操作将永久删除该用户, 是否继续?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id);
      ElMessage.success('删除成功');
      getList();
    } catch (error) {
      ElMessage.error('删除失败');
    }
  });
};

const handleModifyStatus = async (row, status) => {
  try {
    await updateUser(row.id, { ...row, status });
    ElMessage.success('状态修改成功');
    row.status = status;
  } catch (error) {
    ElMessage.error('状态修改失败');
  }
};

const createData = async () => {
  try {
    await createUser(temp.value);
    ElMessage.success('创建成功');
    dialogFormVisible.value = false;
    getList();
  } catch (error) {
    ElMessage.error('创建失败');
  }
};

const updateData = async () => {
  try {
    await updateUser(temp.value.id, temp.value);
    ElMessage.success('更新成功');
    dialogFormVisible.value = false;
    getList();
  } catch (error) {
    ElMessage.error('更新失败');
  }
};

const handleDownload = () => {
  ElMessage.info('导出功能开发中...');
};

const resetTemp = () => {
  temp.value = {
    id: undefined,
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    status: 'active'
  };
};

const statusFilter = (status) => {
  const statusMap = {
    active: 'success',
    inactive: 'warning',
    banned: 'danger'
  };
  return statusMap[status];
};

const getStatusText = (status) => {
  const statusMap = {
    active: '活跃',
    inactive: '禁用',
    banned: '封禁'
  };
  return statusMap[status] || status;
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