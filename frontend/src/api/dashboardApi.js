import { api, toQueryString } from '../lib/api'

export function fetchDashboardSummary() {
  return api.get('/dashboard/summary')
}

export function fetchTodayAppointments({ page = 0, size = 20 } = {}) {
  return api.get(`/dashboard/appointments/today${toQueryString({ page, size })}`)
}

export function fetchUpcomingAppointments({ limit = 10 } = {}) {
  return api.get(`/dashboard/appointments/upcoming${toQueryString({ limit })}`)
}
