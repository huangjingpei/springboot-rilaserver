<template>
  <div class="app-wrapper">
    <div class="sidebar-container" :class="{ collapse: isCollapse }">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="false"
        :router="true"
        mode="vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <div class="sidebar-logo-container">
          <div class="sidebar-logo-link">
            <img src="../assets/logo.png" class="sidebar-logo" alt="logo" />
            <h1 v-show="!isCollapse" class="sidebar-title">Admin Panel</h1>
          </div>
        </div>

        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>

        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>

        <el-menu-item index="/apps">
          <el-icon><Grid /></el-icon>
          <template #title>应用管理</template>
        </el-menu-item>

        <el-menu-item index="/streams">
          <el-icon><VideoPlay /></el-icon>
          <template #title>流媒体管理</template>
        </el-menu-item>

        <el-menu-item index="/network">
          <el-icon><Connection /></el-icon>
          <template #title>网络测试</template>
        </el-menu-item>

        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="main-container" :class="{ collapse: isCollapse }">
      <div class="navbar">
        <div class="navbar-left">
          <el-button
            type="text"
            @click="toggleSideBar"
            class="hamburger-container"
          >
            <el-icon><Expand v-if="isCollapse" /><Fold v-else /></el-icon>
          </el-button>
          <el-breadcrumb class="app-breadcrumb" separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbList" :key="item.path">
              {{ item.meta?.title || item.name }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="navbar-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <span class="username">管理员</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <div class="app-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../store/user';
import {
  House,
  User,
  Grid,
  VideoPlay,
  Connection,
  Setting,
  Expand,
  Fold,
  ArrowDown
} from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const isCollapse = ref(false);

const activeMenu = computed(() => {
  const { path } = route;
  return path;
});

const breadcrumbList = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title);
  return matched;
});

const toggleSideBar = () => {
  isCollapse.value = !isCollapse.value;
};

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout();
    router.push('/login');
  } else if (command === 'profile') {
    // 处理个人资料
    console.log('个人资料');
  }
};
</script>

<style lang="scss" scoped>
.app-wrapper {
  position: relative;
  height: 100vh;
  width: 100%;
  display: flex;

  .sidebar-container {
    width: 210px;
    height: 100vh;
    position: fixed;
    font-size: 0px;
    top: 0;
    bottom: 0;
    left: 0;
    z-index: 1001;
    overflow: hidden;
    background-color: #304156;
    transition: width 0.28s;

    &.collapse {
      width: 64px;
    }

    .sidebar-logo-container {
      position: relative;
      width: 100%;
      height: 50px;
      line-height: 50px;
      background: #2b2f3a;
      text-align: center;
      overflow: hidden;

      .sidebar-logo-link {
        height: 100%;
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: center;

        .sidebar-logo {
          width: 32px;
          height: 32px;
          vertical-align: middle;
          margin-right: 12px;
        }

        .sidebar-title {
          display: inline-block;
          margin: 0;
          color: #fff;
          font-weight: 600;
          line-height: 50px;
          font-size: 14px;
          font-family: Avenir, Helvetica Neue, Arial, Helvetica, sans-serif;
          vertical-align: middle;
        }
      }
    }

    .el-menu {
      border: none;
      height: calc(100vh - 50px);
      width: 100% !important;
      padding-top: 10px;
      
      .el-menu-item {
        height: 50px;
        line-height: 50px;
        margin: 5px 10px;
        border-radius: 6px;
        transition: all 0.3s;
        
        &:hover {
          background-color: rgba(255, 255, 255, 0.1) !important;
        }
        
        &.is-active {
          background-color: #409EFF !important;
          color: #fff !important;
        }
        
        .el-icon {
          margin-right: 8px;
          font-size: 16px;
        }
      }
    }
  }

  .main-container {
    min-height: 100vh;
    transition: margin-left 0.28s, width 0.28s;
    margin-left: 210px;
    width: calc(100% - 210px);
    position: relative;
    display: flex;
    flex-direction: column;

    &.collapse {
      margin-left: 64px;
      width: calc(100% - 64px);
    }

    .navbar {
      height: 70px;
      overflow: hidden;
      position: relative;
      background: #fff;
      box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 24px;

      .navbar-left {
        display: flex;
        align-items: center;

        .hamburger-container {
          line-height: 70px;
          height: 100%;
          float: left;
          cursor: pointer;
          transition: background 0.3s;
          -webkit-tap-highlight-color: transparent;
          margin-right: 20px;
          padding: 0 8px;
          border-radius: 4px;

          &:hover {
            background: rgba(0, 0, 0, 0.025);
          }
        }

        .app-breadcrumb {
          display: inline-block;
          font-size: 14px;
          line-height: 70px;
          margin-left: 8px;
        }
      }

      .navbar-right {
        .el-dropdown-link {
          cursor: pointer;
          color: #409eff;
          display: flex;
          align-items: center;
          height: 40px;
          padding: 0 12px;
          border-radius: 6px;
          transition: background-color 0.3s;

          &:hover {
            background-color: rgba(64, 158, 255, 0.1);
          }

          .username {
            margin-left: 8px;
            margin-right: 4px;
            font-size: 14px;
            font-weight: 500;
          }
        }
      }
    }

    .app-main {
      min-height: calc(100vh - 70px);
      width: 100%;
      position: relative;
      overflow: auto;
      padding: 24px;
      background-color: #f0f2f5;
    }
  }
}

// 响应式处理
@media (max-width: 768px) {
  .app-wrapper {
    .sidebar-container {
      transition: transform 0.28s;
      width: 210px !important;
    }

    .main-container {
      margin-left: 0 !important;
      width: 100% !important;
    }
  }
}
</style>