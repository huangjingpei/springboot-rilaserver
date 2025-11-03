import request from './request';

export function getUsers(params) {
  return request({
    url: '/admin/all-users',
    method: 'get',
    params
  });
}

export function createUser(data) {
  return request({
    url: '/admin/users',
    method: 'post',
    data
  });
}

export function updateUser(id, data) {
  return request({
    url: `/admin/users/${id}`,
    method: 'put',
    data
  });
}

export function deleteUser(id) {
  return request({
    url: `/admin/users/${id}`,
    method: 'delete'
  });
}

export function getUserById(id) {
  return request({
    url: `/admin/users/${id}`,
    method: 'get'
  });
}