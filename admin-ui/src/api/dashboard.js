import request from './request';

// 获取仪表盘统计数据
export function getDashboardStats() {
  return request({
    url: '/admin/dashboard/stats',
    method: 'get'
  });
}

// 获取系统状态
export function getSystemStatus() {
  return request({
    url: '/admin/system/status',
    method: 'get'
  });
}

// 获取最近活动
export function getRecentActivities(params) {
  return request({
    url: '/admin/activities/recent',
    method: 'get',
    params
  });
}

// 获取在线用户数
export function getOnlineUsers() {
  return request({
    url: '/admin/users/online',
    method: 'get'
  });
}

// 获取活跃流媒体数
export function getActiveStreams() {
  return request({
    url: '/admin/streams/active',
    method: 'get'
  });
}