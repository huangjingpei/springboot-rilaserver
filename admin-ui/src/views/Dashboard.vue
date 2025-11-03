<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="panel-group">
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel" @click="handleSetLineChartData('users')">
          <div class="card-panel-icon-wrapper icon-people">
            <el-icon class="card-panel-icon"><User /></el-icon>
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">用户总数</div>
            <div class="card-panel-num">{{ userCount }}</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel" @click="handleSetLineChartData('apps')">
          <div class="card-panel-icon-wrapper icon-message">
            <el-icon class="card-panel-icon"><Grid /></el-icon>
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">应用总数</div>
            <div class="card-panel-num">{{ appCount }}</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel" @click="handleSetLineChartData('streams')">
          <div class="card-panel-icon-wrapper icon-money">
            <el-icon class="card-panel-icon"><VideoPlay /></el-icon>
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">活跃流</div>
            <div class="card-panel-num">{{ streamCount }}</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel" @click="handleSetLineChartData('online')">
          <div class="card-panel-icon-wrapper icon-shopping">
            <el-icon class="card-panel-icon"><Connection /></el-icon>
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">在线用户</div>
            <div class="card-panel-num">{{ onlineCount }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :sm="24" :lg="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
            </div>
          </template>
          <div class="recent-activity">
            <el-timeline>
              <el-timeline-item
                v-for="(activity, index) in recentActivities"
                :key="index"
                :timestamp="activity.timestamp"
                placement="top"
              >
                <el-card>
                  <h4>{{ activity.title }}</h4>
                  <p>{{ activity.description }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :lg="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>系统状态</span>
            </div>
          </template>
          <div class="system-status">
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="status-item">
                  <div class="status-label">CPU使用率</div>
                  <el-progress :percentage="cpuUsage" :color="getProgressColor(cpuUsage)" />
                </div>
              </el-col>
              <el-col :span="12">
                <div class="status-item">
                  <div class="status-label">内存使用率</div>
                  <el-progress :percentage="memoryUsage" :color="getProgressColor(memoryUsage)" />
                </div>
              </el-col>
            </el-row>
            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="12">
                <div class="status-item">
                  <div class="status-label">磁盘使用率</div>
                  <el-progress :percentage="diskUsage" :color="getProgressColor(diskUsage)" />
                </div>
              </el-col>
              <el-col :span="12">
                <div class="status-item">
                  <div class="status-label">网络状态</div>
                  <el-tag :type="networkStatus === 'normal' ? 'success' : 'danger'">
                    {{ networkStatus === 'normal' ? '正常' : '异常' }}
                  </el-tag>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>快速操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/users')">
              <el-icon><User /></el-icon>
              用户管理
            </el-button>
            <el-button type="success" @click="$router.push('/apps')">
              <el-icon><Grid /></el-icon>
              应用管理
            </el-button>
            <el-button type="warning" @click="$router.push('/streams')">
              <el-icon><VideoPlay /></el-icon>
              流媒体管理
            </el-button>
            <el-button type="info" @click="$router.push('/network')">
              <el-icon><Connection /></el-icon>
              网络测试
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { User, Grid, VideoPlay, Connection } from '@element-plus/icons-vue';
import { getDashboardStats, getSystemStatus, getRecentActivities } from '../api/dashboard';
import { ElMessage } from 'element-plus';

// 数据统计
const userCount = ref(0);
const appCount = ref(0);
const streamCount = ref(0);
const onlineCount = ref(0);

// 系统状态
const cpuUsage = ref(45);
const memoryUsage = ref(67);
const diskUsage = ref(32);
const networkStatus = ref('normal');

// 最近活动
const recentActivities = ref([]);

const handleSetLineChartData = (type) => {
  console.log('Chart data type:', type);
};

const getProgressColor = (percentage) => {
  if (percentage < 50) return '#67c23a';
  if (percentage < 80) return '#e6a23c';
  return '#f56c6c';
};

// 加载仪表盘数据
const loadDashboardData = async () => {
  try {
    // 获取统计数据
    const statsResponse = await getDashboardStats();
    if (statsResponse.data) {
      userCount.value = statsResponse.data.userCount || 0;
      appCount.value = statsResponse.data.appCount || 0;
      streamCount.value = statsResponse.data.streamCount || 0;
      onlineCount.value = statsResponse.data.onlineCount || 0;
    }
  } catch (error) {
    console.error('获取统计数据失败:', error);
    // 使用模拟数据作为后备
    userCount.value = 1234;
    appCount.value = 56;
    streamCount.value = 12;
    onlineCount.value = 89;
  }
};

// 加载系统状态
const loadSystemStatus = async () => {
  try {
    const statusResponse = await getSystemStatus();
    if (statusResponse.data) {
      cpuUsage.value = statusResponse.data.cpuUsage || 45;
      memoryUsage.value = statusResponse.data.memoryUsage || 67;
      diskUsage.value = statusResponse.data.diskUsage || 32;
      networkStatus.value = statusResponse.data.networkStatus || 'normal';
    }
  } catch (error) {
    console.error('获取系统状态失败:', error);
    // 保持默认值
  }
};

// 加载最近活动
const loadRecentActivities = async () => {
  try {
    const activitiesResponse = await getRecentActivities({ limit: 5 });
    if (activitiesResponse.data && activitiesResponse.data.length > 0) {
      recentActivities.value = activitiesResponse.data;
    } else {
      // 使用模拟数据
      recentActivities.value = [
        {
          title: '新用户注册',
          description: '用户 john@example.com 完成注册',
          timestamp: '2024-01-15 10:30'
        },
        {
          title: '应用部署',
          description: '应用 MyApp v1.2.0 部署成功',
          timestamp: '2024-01-15 09:15'
        },
        {
          title: '流媒体启动',
          description: '直播流 stream_001 开始推流',
          timestamp: '2024-01-15 08:45'
        }
      ];
    }
  } catch (error) {
    console.error('获取最近活动失败:', error);
    // 使用模拟数据
    recentActivities.value = [
      {
        title: '新用户注册',
        description: '用户 john@example.com 完成注册',
        timestamp: '2024-01-15 10:30'
      },
      {
        title: '应用部署',
        description: '应用 MyApp v1.2.0 部署成功',
        timestamp: '2024-01-15 09:15'
      },
      {
        title: '流媒体启动',
        description: '直播流 stream_001 开始推流',
        timestamp: '2024-01-15 08:45'
      }
    ];
  }
};

// 初始化数据
onMounted(async () => {
  await Promise.all([
    loadDashboardData(),
    loadSystemStatus(),
    loadRecentActivities()
  ]);
});
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;

  .panel-group {
    margin-top: 18px;

    .card-panel-col {
      margin-bottom: 32px;
    }

    .card-panel {
      height: 108px;
      cursor: pointer;
      font-size: 12px;
      position: relative;
      overflow: hidden;
      color: #666;
      background: #fff;
      box-shadow: 4px 4px 40px rgba(0, 0, 0, 0.05);
      border-color: rgba(0, 0, 0, 0.05);
      border-radius: 4px;
      transition: all 0.3s ease-in-out;

      &:hover {
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      }

      .card-panel-icon-wrapper {
        float: left;
        margin: 14px 0 0 14px;
        padding: 16px;
        transition: all 0.38s ease-out;
        border-radius: 6px;

        .card-panel-icon {
          float: left;
          font-size: 48px;
        }
      }

      .icon-people {
        background: #40c9c6;

        .card-panel-icon {
          color: #40c9c6;
        }
      }

      .icon-message {
        background: #36a3f7;

        .card-panel-icon {
          color: #36a3f7;
        }
      }

      .icon-money {
        background: #f4516c;

        .card-panel-icon {
          color: #f4516c;
        }
      }

      .icon-shopping {
        background: #34bfa3;

        .card-panel-icon {
          color: #34bfa3;
        }
      }

      .card-panel-description {
        float: right;
        font-weight: bold;
        margin: 26px;
        margin-left: 0px;

        .card-panel-text {
          line-height: 18px;
          color: rgba(0, 0, 0, 0.45);
          font-size: 16px;
          margin-bottom: 12px;
        }

        .card-panel-num {
          font-size: 20px;
          color: rgba(0, 0, 0, 0.85);
        }
      }
    }
  }

  .box-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .recent-activity {
    max-height: 400px;
    overflow-y: auto;
  }

  .system-status {
    .status-item {
      margin-bottom: 20px;

      .status-label {
        margin-bottom: 8px;
        font-weight: 500;
        color: #606266;
      }
    }
  }

  .quick-actions {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;

    .el-button {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
}

@media (max-width: 1024px) {
  .card-panel-description {
    display: none;
  }

  .card-panel-icon-wrapper {
    float: none !important;
    width: 100%;
    height: 100%;
    margin: 0 !important;

    .svg-icon {
      display: block;
      margin: 14px auto !important;
      float: none !important;
    }
  }
}
</style>