import request from './request';

export function getStreams(params) {
  return request({
    url: '/admin/streams',
    method: 'get',
    params
  });
}

export function createStream(data) {
  return request({
    url: '/admin/streams',
    method: 'post',
    data
  });
}

export function updateStream(id, data) {
  return request({
    url: `/admin/streams/${id}`,
    method: 'put',
    data
  });
}

export function deleteStream(id) {
  return request({
    url: `/admin/streams/${id}`,
    method: 'delete'
  });
}

export function getStreamById(id) {
  return request({
    url: `/admin/streams/${id}`,
    method: 'get'
  });
}

export function startStream(id) {
  return request({
    url: `/admin/streams/${id}/start`,
    method: 'post'
  });
}

export function stopStream(id) {
  return request({
    url: `/admin/streams/${id}/stop`,
    method: 'post'
  });
}