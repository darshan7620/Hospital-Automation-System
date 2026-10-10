import { api, toQueryString } from '../lib/api'

export function fetchDoctors({
  name = '',
  specialization = '',
  departmentId = '',
  active = '',
  page = 0,
  size = 10,
  sort = 'fullName',
  direction = 'asc',
} = {}) {
  return api.get(
    `/doctors${toQueryString({
      name,
      specialization,
      departmentId,
      active,
      page,
      size,
      sort,
      direction,
    })}`,
  )
}

export function fetchDoctor(id) {
  return api.get(`/doctors/${id}`)
}

export function createDoctor(body) {
  return api.post('/doctors', body)
}

export function updateDoctor(id, body) {
  return api.put(`/doctors/${id}`, body)
}

export function deactivateDoctor(id) {
  return api.delete(`/doctors/${id}`)
}

export function activateDoctor(id) {
  return api.post(`/doctors/${id}/activate`)
}