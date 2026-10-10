import { useState } from 'react'
import {
  fetchDashboardSummary,
  fetchTodayAppointments,
  fetchUpcomingAppointments,
} from '../api/dashboardApi'
import { StatCard, StatusBadge } from '../components/common/StatCard'
import {
  EmptyState,
  ErrorState,
  LoadingState,
} from '../components/common/StateBlocks'
import { useFetch } from '../hooks/useFetch'
import {
  APPOINTMENT_STATUSES,
  formatDateTime,
  formatTimeRange,
  statusLabel,
} from '../lib/format'
import './dashboard.css'

const PAGE_SIZE = 10

function SummarySection({ summary }) {
  if (summary.loading) {
    return (
      <section className="dashboard__stats" aria-label="Loading summary">
        <LoadingState label="Loading summary…" />
      </section>
    )
  }

  if (summary.error) {
    return (
      <section className="dashboard__stats" aria-label="Summary error">
        <ErrorState
          title="Could not load the dashboard summary"
          error={summary.error}
          onRetry={summary.reload}
        />
      </section>
    )
  }

  const data = summary.data
  const byStatus = new Map(
    (data.appointmentsByStatus || []).map((entry) => [entry.status, entry.count]),
  )
  const counts = APPOINTMENT_STATUSES.map((status) => ({
    status,
    count: byStatus.get(status) || 0,
  }))
  const maxCount = Math.max(1, ...counts.map((entry) => entry.count))
  const total = counts.reduce((sum, entry) => sum + entry.count, 0)

  return (
    <>
      <section className="dashboard__stats" aria-label="Summary counts">
        <StatCard
          label="Active patients"
          value={data.totalActivePatients}
          accent="accent"
        />
        <StatCard
          label="Active doctors"
          value={data.totalActiveDoctors}
          accent="accent"
        />
        <StatCard
          label="Active departments"
          value={data.totalActiveDepartments}
          accent="accent"
        />
        <StatCard label="Today's appointments" value={data.todayAppointments} />
        <StatCard label="Upcoming appointments" value={data.upcomingAppointments} />
      </section>

      <section className="card dashboard__panel" aria-label="Appointments by status">
        <header className="panel__header">
          <h2>Appointments by status</h2>
          <span className="panel__meta">
            {total} total · generated {formatDateTime(data.generatedAt)}
          </span>
        </header>
        <ul className="status-breakdown">
          {counts.map((entry) => (
            <li key={entry.status} className="status-breakdown__row">
              <span className="status-breakdown__label">
                {statusLabel(entry.status)}
              </span>
              <span className="status-breakdown__track" aria-hidden="true">
                <span
                  className={`status-breakdown__bar status-breakdown__bar--${entry.status.toLowerCase()}`}
                  style={{ width: `${(entry.count / maxCount) * 100}%` }}
                />
              </span>
              <span className="status-breakdown__count">{entry.count}</span>
            </li>
          ))}
        </ul>
      </section>
    </>
  )
}

function TodaySection({ today, page, onPageChange }) {
  const data = today.data
  const rows = data?.content || []
  const from = data && data.totalElements > 0 ? data.page * data.size + 1 : 0
  const to = data ? from + rows.length - 1 : 0

  return (
    <section className="card dashboard__panel" aria-label="Today's appointments">
      <header className="panel__header">
        <h2>Today&apos;s appointments</h2>
        {data ? (
          <span className="panel__meta">
            {data.totalElements === 0
              ? 'none scheduled'
              : `${from}–${to} of ${data.totalElements}`}
          </span>
        ) : null}
      </header>

      {today.loading ? <LoadingState label="Loading today's appointments…" /> : null}

      {today.error ? (
        <ErrorState
          title="Could not load today's appointments"
          error={today.error}
          onRetry={today.reload}
        />
      ) : null}

      {!today.loading && !today.error && rows.length === 0 ? (
        <EmptyState
          title="No appointments today"
          message="There are no appointments scheduled for the current UTC day."
        />
      ) : null}

      {!today.loading && !today.error && rows.length > 0 ? (
        <>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th scope="col">Time</th>
                  <th scope="col">Patient</th>
                  <th scope="col">Doctor</th>
                  <th scope="col">Department</th>
                  <th scope="col">Status</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((appointment) => (
                  <tr key={appointment.id}>
                    <td>{formatTimeRange(appointment.startTime, appointment.endTime)}</td>
                    <td>
                      <span className="cell-primary">{appointment.patientName}</span>
                      <span className="cell-secondary">{appointment.patientNumber}</span>
                    </td>
                    <td>{appointment.doctorName}</td>
                    <td>{appointment.departmentName}</td>
                    <td>
                      <StatusBadge status={appointment.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {data && data.totalPages > 1 ? (
            <footer className="pagination">
              <button
                type="button"
                className="btn btn--secondary"
                disabled={page === 0}
                onClick={() => onPageChange(page - 1)}
              >
                Previous
              </button>
              <span className="pagination__info">
                Page {data.page + 1} of {data.totalPages}
              </span>
              <button
                type="button"
                className="btn btn--secondary"
                disabled={data.last}
                onClick={() => onPageChange(page + 1)}
              >
                Next
              </button>
            </footer>
          ) : null}
        </>
      ) : null}
    </section>
  )
}

function UpcomingSection({ upcoming }) {
  const rows = upcoming.data || []

  return (
    <section className="card dashboard__panel" aria-label="Upcoming appointments">
      <header className="panel__header">
        <h2>Upcoming appointments</h2>
        {upcoming.data ? (
          <span className="panel__meta">{rows.length} next</span>
        ) : null}
      </header>

      {upcoming.loading ? <LoadingState label="Loading upcoming appointments…" /> : null}

      {upcoming.error ? (
        <ErrorState
          title="Could not load upcoming appointments"
          error={upcoming.error}
          onRetry={upcoming.reload}
        />
      ) : null}

      {!upcoming.loading && !upcoming.error && rows.length === 0 ? (
        <EmptyState
          title="No upcoming appointments"
          message="No scheduled or confirmed appointments are waiting in the queue."
        />
      ) : null}

      {!upcoming.loading && !upcoming.error && rows.length > 0 ? (
        <ul className="upcoming-list">
          {rows.map((appointment) => (
            <li key={appointment.id} className="upcoming-list__item">
              <div className="upcoming-list__time">
                {formatDateTime(appointment.startTime)}
              </div>
              <div className="upcoming-list__who">
                <span className="cell-primary">{appointment.patientName}</span>
                <span className="cell-secondary">
                  {appointment.doctorName} · {appointment.departmentName}
                </span>
              </div>
              <StatusBadge status={appointment.status} />
            </li>
          ))}
        </ul>
      ) : null}
    </section>
  )
}

export function DashboardPage() {
  const [page, setPage] = useState(0)

  const summary = useFetch(() => fetchDashboardSummary(), [])
  const today = useFetch(
    () => fetchTodayAppointments({ page, size: PAGE_SIZE }),
    [page],
  )
  const upcoming = useFetch(() => fetchUpcomingAppointments({ limit: 10 }), [])

  const refreshAll = () => {
    summary.reload()
    today.reload()
    upcoming.reload()
  }

  return (
    <div className="dashboard">
      <header className="dashboard__intro">
        <div>
          <h2>Hospital overview</h2>
          <p className="dashboard__subtitle">
            Live figures from the MediCore API
            {summary.data?.date ? ` · ${summary.data.date} (UTC)` : ''}
          </p>
        </div>
        <button type="button" className="btn btn--secondary" onClick={refreshAll}>
          Refresh
        </button>
      </header>

      <SummarySection summary={summary} />

      <div className="dashboard__grid">
        <TodaySection today={today} page={page} onPageChange={setPage} />
        <UpcomingSection upcoming={upcoming} />
      </div>

      <p className="dashboard__footnote">
        API timestamps are UTC; times are displayed in your local time zone.
      </p>
    </div>
  )
}
