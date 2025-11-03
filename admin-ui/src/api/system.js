import request from './request'

// 获取系统设置
export function getSystemSettings() {
  return request({
    url: '/admin/system/settings',
    method: 'get'
  })
}

// 更新系统设置
export function updateSystemSettings(data) {
  return request({
    url: '/admin/system/settings',
    method: 'put',
    data
  })
}

// 获取系统信息
export function getSystemInfo() {
  return request({
    url: '/admin/system/info',
    method: 'get'
  })
}

// 清理系统日志
export function cleanupSystemLogs() {
  return request({
    url: '/admin/system/cleanup-logs',
    method: 'post'
  })
}

// 优化数据库
export function optimizeSystemDatabase() {
  return request({
    url: '/admin/system/optimize-database',
    method: 'post'
  })
}

// 备份数据库
export function backupSystemDatabase() {
  return request({
    url: '/admin/system/backup-database',
    method: 'post'
  })
}

// 重启系统
export function restartSystem() {
  return request({
    url: '/admin/system/restart',
    method: 'post'
  })
}

// 获取系统日志
export function getSystemLogs(params) {
  return request({
    url: '/admin/system/logs',
    method: 'get',
    params
  })
}