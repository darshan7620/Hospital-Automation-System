import { api, toQueryString } from '../lib/api'

export function fetchDepartments({ name = '', active = '', page = 0, size = 10 } = {}) {
  return api.get(`/departments${toQueryString({ name, active, page, size })}`)
}

export function fetchDepartment(id) {
  return api.get(`/departments/${id}`)
}

export function createDepartment(body) {
  return api.post('/departments', body)
}

export function updateDepartment(id, body) {
  return api.put(`/departments/${id}`, body)
}

export function deactivateDepartment(id) {
  return api.delete(`/departments/${id}`)
}

export function activateDepartment(id) {
  return api.post(`/departments/${id}/activate`)
}
