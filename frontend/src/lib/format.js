export const APPOINTMENT_STATUSES = [
  'SCHEDULED',
  'CONFIRMED',
  'COMPLETED',
  'CANCELLED',
  'NO_SHOW',
]

const STATUS_LABELS = {
  SCHEDULED: 'Scheduled',
  CONFIRMED: 'Confirmed',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
  NO_SHOW: 'No show',
}

const STATUS_CLASS_NAMES = {
  SCHEDULED: 'badge--scheduled',
  CONFIRMED: 'badge--confirmed',
  COMPLETED: 'badge--completed',
  CANCELLED: 'badge--cancelled',
  NO_SHOW: 'badge--no-show',
}

export function statusLabel(status) {
  return STATUS_LABELS[status] || status || 'Unknown'
}

const ALLOWED_TRANSITIONS = {
  SCHEDULED: ['CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW'],
  CONFIRMED: ['COMPLETED', 'CANCELLED', 'NO_SHOW'],
  COMPLETED: [],
  CANCELLED: [],
  NO_SHOW: [],
}

export function allowedNextStatuses(status) {
  return ALLOWED_TRANSITIONS[status] || []
}

export function isTerminalStatus(status) {
  return allowedNextStatuses(status).length === 0
}

export function statusClass(status) {
  return STATUS_CLASS_NAMES[status] || 'badge--default'
}

const dateTimeFormatter = new Intl.DateTimeFormat(undefined, {
  year: 'numeric',
  month: 'short',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
})

const dateFormatter = new Intl.DateTimeFormat(undefined, {
  year: 'numeric',
  month: 'short',
  day: '2-digit',
})

const timeFormatter = new Intl.DateTimeFormat(undefined, {
  hour: '2-digit',
  minute: '2-digit',
})

export function formatDateTime(isoInstant) {
  if (!isoInstant) return '—'
  const date = new Date(isoInstant)
  return Number.isNaN(date.getTime()) ? '—' : dateTimeFormatter.format(date)
}

export function formatDate(isoDate) {
  if (!isoDate) return '—'
  const date = new Date(`${isoDate}T00:00:00Z`)
  return Number.isNaN(date.getTime()) ? isoDate : dateFormatter.format(date)
}

export function formatTime(isoInstant) {
  if (!isoInstant) return '—'
  const date = new Date(isoInstant)
  return Number.isNaN(date.getTime()) ? '—' : timeFormatter.format(date)
}

export function formatTimeRange(startTime, endTime) {
  return `${formatTime(startTime)} – ${formatTime(endTime)}`
}

const currencyFormatter = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

export function formatCurrency(value) {
  if (value === undefined || value === null || value === '') return '—'
  const number = Number(value)
  return Number.isNaN(number) ? '—' : currencyFormatter.format(number)
}
