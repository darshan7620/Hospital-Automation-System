import { api, toQueryString } from '../lib/api'

export function fetchPatients({
  name = '',
  phone = '',
  active = '',
  page = 0,
  size = 10,
  sort = 'fullName',
  direction = 'asc',
} = {}) {
  return api.get(
    `/patients${toQueryString({
      name,
      phone,
      active,
      page,
      size,
      sort,
      direction,
    })}`,
  )
}

export function fetchPatient(id) {
  return api.get(`/patients/${id}`)
}

export function createPatient(body) {
  return api.post('/patients', body)
}

export function updatePatient(id, body) {
  return api.put(`/patients/${id}`, body)
}

export function deactivatePatient(id) {
  return api.delete(`/patients/${id}`)
}

export function activatePatient(id) {
  return api.post(`/patients/${id}/activate`)
}