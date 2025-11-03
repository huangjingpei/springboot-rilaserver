import request from './request';

// 连接测试
export function testConnection(data) {
  return request({
    url: '/api/network/test-connection',
    method: 'post',
    data
  });
}

// Ping测试
export function testPing(data) {
  return request({
    url: '/api/network/ping',
    method: 'post',
    data
  });
}

// 获取网络状态
export function getNetworkStatus() {
  return request({
    url: '/api/network/status',
    method: 'get'
  });
}

// 端口扫描
export function scanPorts(data) {
  return request({
    url: '/api/network/scan-ports',
    method: 'post',
    data
  });
}