import request from './request';

export function getApps(params) {
  return request({
    url: '/api/apps',
    method: 'get',
    params
  });
}

export function createApp(data) {
  return request({
    url: '/api/apps',
    method: 'post',
    data
  });
}

export function updateApp(id, data) {
  return request({
    url: `/api/apps/${id}`,
    method: 'put',
    data
  });
}

export function deleteApp(id) {
  return request({
    url: `/api/apps/${id}`,
    method: 'delete'
  });
}

export function getAppById(id) {
  return request({
    url: `/api/apps/${id}`,
    method: 'get'
  });
}

export function startApp(id) {
  return request({
    url: `/api/apps/${id}/start`,
    method: 'post'
  });
}

export function stopApp(id) {
  return request({
    url: `/api/apps/${id}/stop`,
    method: 'post'
  });
}

export function restartApp(id) {
  return request({
    url: `/api/apps/${id}/restart`,
    method: 'post'
  });
}

export function searchApps(params) {
  return request({
    url: '/api/apps/search-simple',
    method: 'get',
    params
  });
}