import request from './request';

export function login(data) {
  return request({
    url: '/api/admin/login',
    method: 'post',
    data,
  });
}