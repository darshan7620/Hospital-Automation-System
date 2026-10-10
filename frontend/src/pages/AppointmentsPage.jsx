import { useState } from 'react'
import {
  cancelAppointment,
  createAppointment,
  fetchAppointment,
  fetchAppointments,
  updateAppointment,
} from '../api/appointmentApi'
import { fetchDoctors } from '../api/doctorApi'
import { fetchPatients } from '../api/patientApi'
import { Alert } from '../components/common/Alert'
import { ConfirmDialog } from '../components/common/ConfirmDialog'
import {
  EmptyState,
  ErrorState,
  LoadingState,
} from '../components/common/StateBlocks'
import {
  BanIcon,
  EditIcon,
  EyeIcon,
  PlusIcon,
} from '../components/common/icons'
import { useFetch } from '../hooks/useFetch'
import {
  allowedNextStatuses,
  APPOINTMENT_STATUSES,
  formatDateTime,
  formatTime,
  statusClass,
  statusLabel,
} from '../lib/format'
import { AppointmentDetailModal } from './AppointmentDetailModal'
import { AppointmentFormModal } from './AppointmentFormModal'
import './appointments.css'

const PAGE_SIZE = 10

function SortableTh({ label, field, sort, onSort, thClass = '' }) {
  const active = sort.field === field
  const directionLabel = active
    ? sort.direction === 'asc'
      ? 'ascending'
      : 'descending'
    : 'none'
  return (
    <th scope="col" className={thClass} aria-sort={directionLabel}>
      <button
        type="button"
        className="data-table__sort"
        onClick={() => onSort(field)}
      >
        {label}
        {active ? (sort.direction === 'asc' ? ' ↑' : ' ↓') : ''}
      </button>
    </th>
  )
}

function canBeCancelled(status) {
  return allowedNextStatuses(status).includes('CANCELLED')
}

export function AppointmentsPage() {
  const [page, setPage] = useState(0)
  const [dateFilter, setDateFilter] = useState('')
  const [doctorFilter, setDoctorFilter] = useState('')
  const [patientFilter, setPatientFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [sort, setSort] = useState({ field: 'startTime', direction: 'asc' })

  const [formState, setFormState] = useState(null)
  const [viewId, setViewId] = useState(null)
  const [confirmTarget, setConfirmTarget] = useState(null)
  const [confirmBusy, setConfirmBusy] = useState(false)
  const [confirmError, setConfirmError] = useState(null)
  const [feedback, setFeedback] = useState(null)

  const list = useFetch(
    () =>
      fetchAppointments({
        date: dateFilter,
        doctorId: doctorFilter,
        patientId: patientFilter,
        status: statusFilter,
        page,
        size: PAGE_SIZE,
        sort: sort.field,
        direction: sort.direction,
      }),
    [dateFilter, doctorFilter, patientFilter, statusFilter, page, sort.field, sort.direction],
  )

  const doctorOptions = useFetch(
    () => fetchDoctors({ active: 'true', size: 100 }),
    [],
  )
  const patientOptions = useFetch(
    () => fetchPatients({ active: 'true', size: 100 }),
    [],
  )

  const detail = useFetch(
    () => (viewId === null ? Promise.resolve(null) : fetchAppointment(viewId)),
    [viewId],
  )

  const rows = list.data?.content || []
  const doctors = doctorOptions.data?.content || []
  const patients = patientOptions.data?.content || []
  const hasFilters =
    dateFilter !== '' || doctorFilter !== '' || patientFilter !== '' || statusFilter !== ''

  function changeFilter(setter, value) {
    setter(value)
    setPage(0)
  }

  function clearFilters() {
    setDateFilter('')
    setDoctorFilter('')
    setPatientFilter('')
    setStatusFilter('')
    setPage(0)
  }

  function handleSort(field) {
    setSort((prev) =>
      prev.field === field
        ? { field, direction: prev.direction === 'asc' ? 'desc' : 'asc' }
        : { field, direction: 'asc' },
    )
    setPage(0)
  }

  async function handleCreate(values) {
    await createAppointment(values)
    setFormState(null)
    setFeedback({ type: 'success', message: 'Appointment booked.' })
    list.reload()
  }

  async function handleUpdate(values) {
    await updateAppointment(formState.id, values)
    setFormState(null)
    setFeedback({ type: 'success', message: 'Appointment updated.' })
    list.reload()
    if (viewId === formState.id) detail.reload()
  }

  async function handleCancel() {
    setConfirmBusy(true)
    setConfirmError(null)
    try {
      await cancelAppointment(confirmTarget.id)
      setFeedback({
        type: 'success',
        message: `Appointment #${confirmTarget.id} cancelled.`,
      })
      setConfirmTarget(null)
      list.reload()
      if (viewId === confirmTarget.id) detail.reload()
    } catch (error) {
      setConfirmError(error?.message || 'Could not cancel this appointment.')
    } finally {
      setConfirmBusy(false)
    }
  }

  return (
    <div className="appointments">
      <header className="appointments__intro">
        <div>
          <h2>Appointments</h2>
          <p className="appointments__subtitle">
            {list.data
              ? `${list.data.totalElements} appointment${list.data.totalElements === 1 ? '' : 's'}${hasFilters ? ' matching filters' : ''} · live from the MediCore API`
              : 'Live from the MediCore API'}
          </p>
        </div>
        <button
          type="button"
          className="btn btn--primary"
          onClick={() => setFormState({ mode: 'create' })}
        >
          <PlusIcon />
          New appointment
        </button>
      </header>

      {feedback ? (
        <Alert type={feedback.type} onDismiss={() => setFeedback(null)}>
          {feedback.message}
        </Alert>
      ) : null}

      <section className="card appointments__toolbar" aria-label="Filters">
        <div className="form-field">
          <label className="form-field__label" htmlFor="appointment-filter-date">
            Date
          </label>
          <input
            id="appointment-filter-date"
            className="form-input"
            type="date"
            value={dateFilter}
            onChange={(event) => changeFilter(setDateFilter, event.target.value)}
          />
        </div>
        <div className="form-field">
          <label className="form-field__label" htmlFor="appointment-filter-doctor">
            Doctor
          </label>
          <select
            id="appointment-filter-doctor"
            className="form-input"
            value={doctorFilter}
            onChange={(event) => changeFilter(setDoctorFilter, event.target.value)}
          >
            <option value="">All doctors</option>
            {doctors.map((doctor) => (
              <option key={doctor.id} value={doctor.id}>
                {doctor.fullName}
              </option>
            ))}
          </select>
        </div>
        <div className="form-field">
          <label className="form-field__label" htmlFor="appointment-filter-patient">
            Patient
          </label>
          <select
            id="appointment-filter-patient"
            className="form-input"
            value={patientFilter}
            onChange={(event) => changeFilter(setPatientFilter, event.target.value)}
          >
            <option value="">All patients</option>
            {patients.map((patient) => (
              <option key={patient.id} value={patient.id}>
                {patient.patientNumber} — {patient.fullName}
              </option>
            ))}
          </select>
        </div>
        <div className="form-field">
          <label className="form-field__label" htmlFor="appointment-filter-status">
            Status
          </label>
          <select
            id="appointment-filter-status"
            className="form-input"
            value={statusFilter}
            onChange={(event) => changeFilter(setStatusFilter, event.target.value)}
          >
            <option value="">All statuses</option>
            {APPOINTMENT_STATUSES.map((status) => (
              <option key={status} value={status}>
                {statusLabel(status)}
              </option>
            ))}
          </select>
        </div>
        {hasFilters ? (
          <button
            type="button"
            className="btn btn--secondary appointments__clear"
            onClick={clearFilters}
          >
            Clear filters
          </button>
        ) : null}
        <p className="appointments__hint">
          The date filter matches a UTC calendar day: <code>startTime</code> within
          [00:00Z, next-day 00:00Z). Row times are displayed in your local time
          zone.
        </p>
      </section>

      <section className="card appointments__panel" aria-label="Appointment list">
        {list.loading ? <LoadingState label="Loading appointments…" /> : null}

        {list.error ? (
          <ErrorState
            title="Could not load appointments"
            error={list.error}
            onRetry={list.reload}
          />
        ) : null}

        {!list.loading && !list.error && rows.length === 0 ? (
          hasFilters ? (
            <EmptyState
              title="No appointments match your filters"
              message="Try a different date, doctor, patient or status, or clear the filters."
              action={
                <button
                  type="button"
                  className="btn btn--secondary"
                  onClick={clearFilters}
                >
                  Clear filters
                </button>
              }
            />
          ) : (
            <EmptyState
              title="No appointments yet"
              message="Book the first appointment to get started."
              action={
                <button
                  type="button"
                  className="btn btn--primary"
                  onClick={() => setFormState({ mode: 'create' })}
                >
                  <PlusIcon />
                  New appointment
                </button>
              }
            />
          )
        ) : null}

        {!list.loading && !list.error && rows.length > 0 ? (
          <>
            <div className="table-wrap">
              <table className="data-table appointments-table">
                <thead>
                  <tr>
                    <SortableTh
                      label="Start"
                      field="startTime"
                      sort={sort}
                      onSort={handleSort}
                      thClass="appointments-table__time"
                    />
                    <SortableTh
                      label="End"
                      field="endTime"
                      sort={sort}
                      onSort={handleSort}
                      thClass="appointments-table__time"
                    />
                    <th scope="col">Patient</th>
                    <th scope="col">Doctor</th>
                    <SortableTh label="Status" field="status" sort={sort} onSort={handleSort} />
                    <th scope="col">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((appointment) => (
                    <tr key={appointment.id}>
                      <td className="appointments-table__time">
                        {formatDateTime(appointment.startTime)}
                      </td>
                      <td className="appointments-table__time">
                        {formatTime(appointment.endTime)}
                      </td>
                      <td>
                        <span className="cell-primary">{appointment.patientName}</span>
                        <span className="cell-secondary">
                          {appointment.patientNumber}
                        </span>
                      </td>
                      <td>
                        <span className="cell-primary">{appointment.doctorName}</span>
                        {appointment.departmentName ? (
                          <span className="cell-secondary">
                            {appointment.departmentName}
                          </span>
                        ) : null}
                      </td>
                      <td>
                        <span className={`badge ${statusClass(appointment.status)}`}>
                          {statusLabel(appointment.status)}
                        </span>
                      </td>
                      <td>
                        <div className="appointments-table__actions">
                          <button
                            type="button"
                            className="btn btn--secondary btn--icon"
                            title="View details"
                            aria-label={`View appointment ${appointment.id}`}
                            onClick={() => setViewId(appointment.id)}
                          >
                            <EyeIcon />
                          </button>
                          <button
                            type="button"
                            className="btn btn--secondary btn--icon"
                            title="Edit or reschedule"
                            aria-label={`Edit appointment ${appointment.id}`}
                            onClick={() =>
                              setFormState({
                                mode: 'edit',
                                id: appointment.id,
                                appointment,
                              })
                            }
                          >
                            <EditIcon />
                          </button>
                          {canBeCancelled(appointment.status) ? (
                            <button
                              type="button"
                              className="btn btn--secondary btn--icon"
                              title="Cancel appointment"
                              aria-label={`Cancel appointment ${appointment.id}`}
                              onClick={() => {
                                setConfirmError(null)
                                setConfirmTarget(appointment)
                              }}
                            >
                              <BanIcon />
                            </button>
                          ) : null}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {list.data && list.data.totalPages > 1 ? (
              <footer className="pagination">
                <button
                  type="button"
                  className="btn btn--secondary"
                  disabled={page === 0}
                  onClick={() => setPage(page - 1)}
                >
                  Previous
                </button>
                <span className="pagination__info">
                  Page {list.data.page + 1} of {list.data.totalPages}
                </span>
                <button
                  type="button"
                  className="btn btn--secondary"
                  disabled={list.data.last}
                  onClick={() => setPage(page + 1)}
                >
                  Next
                </button>
              </footer>
            ) : null}
          </>
        ) : null}
      </section>

      {formState ? (
        <AppointmentFormModal
          appointment={formState.mode === 'edit' ? formState.appointment : null}
          onSubmit={formState.mode === 'edit' ? handleUpdate : handleCreate}
          onClose={() => setFormState(null)}
        />
      ) : null}

      {viewId !== null ? (
        <AppointmentDetailModal state={detail} onClose={() => setViewId(null)} />
      ) : null}

      {confirmTarget ? (
        <ConfirmDialog
          title="Cancel appointment"
          message={`Cancel the appointment for "${confirmTarget.patientName}" with "${confirmTarget.doctorName}" on ${formatDateTime(confirmTarget.startTime)}? This cannot be undone.`}
          confirmLabel="Cancel appointment"
          busy={confirmBusy}
          error={confirmError}
          onConfirm={handleCancel}
          onCancel={() => setConfirmTarget(null)}
        />
      ) : null}
    </div>
  )
}
