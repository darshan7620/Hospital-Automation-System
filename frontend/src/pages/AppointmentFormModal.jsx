import { useState } from 'react'
import { fetchDoctors } from '../api/doctorApi'
import { fetchPatients } from '../api/patientApi'
import { Modal } from '../components/common/Modal'
import { useFetch } from '../hooks/useFetch'
import {
  allowedNextStatuses,
  isTerminalStatus,
  statusLabel,
} from '../lib/format'

const LIMITS = { reason: 500 }

function splitInstant(isoInstant) {
  const date = new Date(isoInstant)
  const pad = (value) => String(value).padStart(2, '0')
  return {
    date: `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`,
    time: `${pad(date.getHours())}:${pad(date.getMinutes())}`,
  }
}

function toInstant(date, time) {
  const [year, month, day] = date.split('-').map(Number)
  const [hour, minute] = time.split(':').map(Number)
  return new Date(year, month - 1, day, hour, minute, 0, 0).toISOString()
}

function todayLocalString() {
  const now = new Date()
  const pad = (value) => String(value).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
}

function validate(values) {
  const errors = {}

  if (!values.patientId) errors.patientId = 'Select a patient.'
  if (!values.doctorId) errors.doctorId = 'Select a doctor.'
  if (!values.date) errors.date = 'Date is required.'
  if (!values.startTime) errors.startTime = 'Start time is required.'
  if (!values.endTime) errors.endTime = 'End time is required.'

  if (values.date && values.startTime && values.endTime) {
    const start = new Date(toInstant(values.date, values.startTime))
    const end = new Date(toInstant(values.date, values.endTime))
    if (end <= start) errors.endTime = 'End time must be after start time.'
  }

  if (values.reason.trim().length > LIMITS.reason)
    errors.reason = `Reason must be at most ${LIMITS.reason} characters.`

  return errors
}

export function AppointmentFormModal({ appointment, onSubmit, onClose }) {
  const isEdit = Boolean(appointment)
  const terminal = isEdit && isTerminalStatus(appointment.status)
  const initialStart = isEdit ? splitInstant(appointment.startTime) : null

  const [values, setValues] = useState({
    patientId: appointment ? String(appointment.patientId) : '',
    doctorId: appointment ? String(appointment.doctorId) : '',
    date: initialStart ? initialStart.date : todayLocalString(),
    startTime: initialStart ? initialStart.time : '09:00',
    endTime: isEdit ? splitInstant(appointment.endTime).time : '09:30',
    reason: appointment?.reason ?? '',
    status: '',
  })
  const [fieldErrors, setFieldErrors] = useState({})
  const [formError, setFormError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  const patientOptions = useFetch(
    () => fetchPatients({ active: 'true', size: 100 }),
    [],
  )
  const doctorOptions = useFetch(
    () => fetchDoctors({ active: 'true', size: 100 }),
    [],
  )

  const activePatients = patientOptions.data?.content || []
  const activeDoctors = doctorOptions.data?.content || []

  const currentPatient = isEdit
    ? { id: appointment.patientId, fullName: appointment.patientName, patientNumber: appointment.patientNumber }
    : null
  const currentDoctor = isEdit
    ? { id: appointment.doctorId, fullName: appointment.doctorName }
    : null

  const patients =
    currentPatient && !activePatients.some((patient) => patient.id === currentPatient.id)
      ? [currentPatient, ...activePatients]
      : activePatients
  const doctors =
    currentDoctor && !activeDoctors.some((doctor) => doctor.id === currentDoctor.id)
      ? [currentDoctor, ...activeDoctors]
      : activeDoctors

  const nextStatuses = isEdit ? allowedNextStatuses(appointment.status) : []

  function update(field, value) {
    setValues((prev) => ({ ...prev, [field]: value }))
    if (fieldErrors[field]) {
      setFieldErrors((prev) => ({ ...prev, [field]: undefined }))
    }
  }

  async function handleSubmit(event) {
    event.preventDefault()
    const errors = validate(values)
    setFieldErrors(errors)
    setFormError(null)
    if (Object.keys(errors).length > 0) return

    setSubmitting(true)
    try {
      await onSubmit({
        patientId: Number(values.patientId),
        doctorId: Number(values.doctorId),
        startTime: toInstant(values.date, values.startTime),
        endTime: toInstant(values.date, values.endTime),
        reason: values.reason.trim() || null,
        ...(isEdit && values.status ? { status: values.status } : {}),
      })
    } catch (error) {
      const mapped = {}
      if (Array.isArray(error?.fieldErrors)) {
        for (const entry of error.fieldErrors) {
          mapped[entry.field] = entry.message
        }
      }
      setFieldErrors(mapped)
      if (Object.keys(mapped).length === 0 || error?.status !== 400) {
        setFormError(error?.message || 'Something went wrong.')
      }
      setSubmitting(false)
    }
  }

  const patientsFailed = !patientOptions.loading && patientOptions.error
  const doctorsFailed = !doctorOptions.loading && doctorOptions.error
  const noActivePatients =
    !patientOptions.loading && !patientOptions.error && activePatients.length === 0
  const noActiveDoctors =
    !doctorOptions.loading && !doctorOptions.error && activeDoctors.length === 0

  return (
    <Modal
      title={isEdit ? 'Edit appointment' : 'Book appointment'}
      onClose={submitting ? () => {} : onClose}
      footer={
        <>
          <button
            type="button"
            className="btn btn--secondary"
            onClick={onClose}
            disabled={submitting}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="btn btn--primary"
            form="appointment-form"
            disabled={submitting}
          >
            {submitting ? 'Saving…' : isEdit ? 'Save changes' : 'Book appointment'}
          </button>
        </>
      }
    >
      <form id="appointment-form" onSubmit={handleSubmit} noValidate>
        <div className="form-stack">
          {formError ? (
            <div className="alert alert--error" role="alert">
              {formError}
            </div>
          ) : null}

          {terminal ? (
            <div className="alert alert--info" role="status">
              This appointment is {appointment.status.toLowerCase()}. Terminal
              appointments cannot be rescheduled or moved to another status; only
              the reason can be edited.
            </div>
          ) : null}

          <div className="form-field">
            <label className="form-field__label" htmlFor="appointment-patient">
              Patient <span className="form-field__required">*</span>
            </label>
            <select
              id="appointment-patient"
              className={`form-input${fieldErrors.patientId ? ' form-input--invalid' : ''}`}
              value={values.patientId}
              onChange={(event) => update('patientId', event.target.value)}
              disabled={terminal}
              aria-invalid={Boolean(fieldErrors.patientId)}
              aria-describedby={
                fieldErrors.patientId ? 'appointment-patient-error' : undefined
              }
            >
              <option value="">Select a patient…</option>
              {patients.map((patient) => (
                <option key={patient.id} value={patient.id}>
                  {patient.patientNumber} — {patient.fullName}
                </option>
              ))}
            </select>
            {fieldErrors.patientId ? (
              <p className="form-field__error" id="appointment-patient-error">
                {fieldErrors.patientId}
              </p>
            ) : (
              <p className="form-field__hint">Only active patients can be booked.</p>
            )}
            {patientsFailed ? (
              <p className="form-field__error">
                Could not load patients.{' '}
                <button
                  type="button"
                  className="btn btn--secondary"
                  onClick={patientOptions.reload}
                >
                  Retry
                </button>
              </p>
            ) : null}
          </div>

          <div className="form-field">
            <label className="form-field__label" htmlFor="appointment-doctor">
              Doctor <span className="form-field__required">*</span>
            </label>
            <select
              id="appointment-doctor"
              className={`form-input${fieldErrors.doctorId ? ' form-input--invalid' : ''}`}
              value={values.doctorId}
              onChange={(event) => update('doctorId', event.target.value)}
              disabled={terminal}
              aria-invalid={Boolean(fieldErrors.doctorId)}
              aria-describedby={
                fieldErrors.doctorId ? 'appointment-doctor-error' : undefined
              }
            >
              <option value="">Select a doctor…</option>
              {doctors.map((doctor) => (
                <option key={doctor.id} value={doctor.id}>
                  {doctor.fullName}
                  {doctor.specialization ? ` — ${doctor.specialization}` : ''}
                </option>
              ))}
            </select>
            {fieldErrors.doctorId ? (
              <p className="form-field__error" id="appointment-doctor-error">
                {fieldErrors.doctorId}
              </p>
            ) : (
              <p className="form-field__hint">
                A slot is refused with HTTP 409 when the doctor already has an
                active appointment overlapping it.
              </p>
            )}
            {doctorsFailed ? (
              <p className="form-field__error">
                Could not load doctors.{' '}
                <button
                  type="button"
                  className="btn btn--secondary"
                  onClick={doctorOptions.reload}
                >
                  Retry
                </button>
              </p>
            ) : null}
          </div>

          <div className="form-field">
            <label className="form-field__label" htmlFor="appointment-date">
              Date <span className="form-field__required">*</span>
            </label>
            <input
              id="appointment-date"
              className={`form-input${fieldErrors.date ? ' form-input--invalid' : ''}`}
              type="date"
              value={values.date}
              onChange={(event) => update('date', event.target.value)}
              disabled={terminal}
              aria-invalid={Boolean(fieldErrors.date)}
              aria-describedby={
                fieldErrors.date ? 'appointment-date-error' : 'appointment-date-hint'
              }
            />
            {fieldErrors.date ? (
              <p className="form-field__error" id="appointment-date-error">
                {fieldErrors.date}
              </p>
            ) : (
              <p className="form-field__hint" id="appointment-date-hint">
                Times are entered in your local time zone and stored as UTC by the
                API.
              </p>
            )}
          </div>

          <div className="appointments-form__times">
            <div className="form-field">
              <label className="form-field__label" htmlFor="appointment-start">
                Start <span className="form-field__required">*</span>
              </label>
              <input
                id="appointment-start"
                className={`form-input${fieldErrors.startTime ? ' form-input--invalid' : ''}`}
                type="time"
                value={values.startTime}
                onChange={(event) => update('startTime', event.target.value)}
                disabled={terminal}
                aria-invalid={Boolean(fieldErrors.startTime)}
                aria-describedby={
                  fieldErrors.startTime ? 'appointment-start-error' : undefined
                }
              />
              {fieldErrors.startTime ? (
                <p className="form-field__error" id="appointment-start-error">
                  {fieldErrors.startTime}
                </p>
              ) : null}
            </div>

            <div className="form-field">
              <label className="form-field__label" htmlFor="appointment-end">
                End <span className="form-field__required">*</span>
              </label>
              <input
                id="appointment-end"
                className={`form-input${fieldErrors.endTime ? ' form-input--invalid' : ''}`}
                type="time"
                value={values.endTime}
                onChange={(event) => update('endTime', event.target.value)}
                disabled={terminal}
                aria-invalid={Boolean(fieldErrors.endTime)}
                aria-describedby={
                  fieldErrors.endTime ? 'appointment-end-error' : undefined
                }
              />
              {fieldErrors.endTime ? (
                <p className="form-field__error" id="appointment-end-error">
                  {fieldErrors.endTime}
                </p>
              ) : null}
            </div>
          </div>

          {isEdit && !terminal ? (
            <div className="form-field">
              <label className="form-field__label" htmlFor="appointment-status">
                Status
              </label>
              <select
                id="appointment-status"
                className="form-input"
                value={values.status}
                onChange={(event) => update('status', event.target.value)}
                aria-describedby="appointment-status-hint"
              >
                <option value="">
                  Keep current status ({appointment.status.toLowerCase()})
                </option>
                {nextStatuses.map((status) => (
                  <option key={status} value={status}>
                    {statusLabel(status)}
                  </option>
                ))}
              </select>
              <p className="form-field__hint" id="appointment-status-hint">
                The API rejects transitions outside the allowed status flow with
                HTTP 409.
              </p>
            </div>
          ) : null}

          <div className="form-field">
            <label className="form-field__label" htmlFor="appointment-reason">
              Reason
            </label>
            <textarea
              id="appointment-reason"
              className={`form-input${fieldErrors.reason ? ' form-input--invalid' : ''}`}
              maxLength={LIMITS.reason}
              rows={3}
              value={values.reason}
              onChange={(event) => update('reason', event.target.value)}
              aria-invalid={Boolean(fieldErrors.reason)}
              aria-describedby={
                fieldErrors.reason ? 'appointment-reason-error' : 'appointment-reason-hint'
              }
            />
            {fieldErrors.reason ? (
              <p className="form-field__error" id="appointment-reason-error">
                {fieldErrors.reason}
              </p>
            ) : (
              <p className="form-field__hint" id="appointment-reason-hint">
                {values.reason.length}/{LIMITS.reason} characters · optional
              </p>
            )}
          </div>

          {isEdit && noActivePatients ? (
            <div className="alert alert--info" role="status">
              No active patients are available. Reactivate a patient before
              reassigning this appointment.
            </div>
          ) : null}

          {isEdit && noActiveDoctors ? (
            <div className="alert alert--info" role="status">
              No active doctors are available. Reactivate a doctor before
              reassigning this appointment.
            </div>
          ) : null}

          {!isEdit && (noActivePatients || noActiveDoctors) ? (
            <div className="alert alert--info" role="status">
              Booking needs at least one active patient and one active doctor.
              Create or reactivate them first.
            </div>
          ) : null}
        </div>
      </form>
    </Modal>
  )
}
