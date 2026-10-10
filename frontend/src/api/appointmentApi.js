import { api, toQueryString } from '../lib/api'

export function fetchAppointments({
  date = '',
  doctorId = '',
  patientId = '',
  status = '',
  page = 0,
  size = 20,
  sort = 'startTime',
  direction = 'asc',
} = {}) {
  return api.get(
    `/appointments${toQueryString({
      date,
      doctorId,
      patientId,
      status,
      page,
      size,
      sort,
      direction,
    })}`,
  )
}

export function fetchAppointment(id) {
  return api.get(`/appointments/${id}`)
}

export function createAppointment(body) {
  return api.post('/appointments', body)
}

export function updateAppointment(id, body) {
  return api.put(`/appointments/${id}`, body)
}

export function cancelAppointment(id) {
  return api.post(`/appointments/${id}/cancel`)
}
