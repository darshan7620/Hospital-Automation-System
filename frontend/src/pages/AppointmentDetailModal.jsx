import { Modal } from '../components/common/Modal'
import { ErrorState, LoadingState } from '../components/common/StateBlocks'
import {
  formatDateTime,
  statusClass,
  statusLabel,
} from '../lib/format'

function formatDuration(appointment) {
  const start = new Date(appointment.startTime).getTime()
  const end = new Date(appointment.endTime).getTime()
  if (Number.isNaN(start) || Number.isNaN(end) || end <= start) return null
  const minutes = Math.round((end - start) / 60000)
  if (minutes % 60 === 0) {
    const hours = minutes / 60
    return `${hours} hour${hours === 1 ? '' : 's'}`
  }
  return `${minutes} minutes`
}

export function AppointmentDetailModal({ state, onClose }) {
  const appointment = state.data
  const duration = appointment ? formatDuration(appointment) : null

  return (
    <Modal
      title="Appointment details"
      onClose={onClose}
      footer={
        <button type="button" className="btn btn--secondary" onClick={onClose}>
          Close
        </button>
      }
    >
      {state.loading ? <LoadingState label="Loading appointment…" /> : null}

      {state.error ? (
        <ErrorState
          title="Could not load this appointment"
          error={state.error}
          onRetry={state.reload}
        />
      ) : null}

      {!state.loading && !state.error && appointment ? (
        <dl className="detail-list">
          <div className="detail-list__row">
            <dt>Status</dt>
            <dd>
              <span className={`badge ${statusClass(appointment.status)}`}>
                {statusLabel(appointment.status)}
              </span>
            </dd>
          </div>
          <div className="detail-list__row">
            <dt>Patient</dt>
            <dd>
              {appointment.patientName}
              <span className="detail-muted"> · {appointment.patientNumber}</span>
            </dd>
          </div>
          <div className="detail-list__row">
            <dt>Doctor</dt>
            <dd>{appointment.doctorName}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Department</dt>
            <dd>{appointment.departmentName || <span className="detail-muted">—</span>}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Start</dt>
            <dd>{formatDateTime(appointment.startTime)}</dd>
          </div>
          <div className="detail-list__row">
            <dt>End</dt>
            <dd>
              {formatDateTime(appointment.endTime)}
              {duration ? <span className="detail-muted"> · {duration}</span> : null}
            </dd>
          </div>
          <div className="detail-list__row">
            <dt>Reason</dt>
            <dd>{appointment.reason || <span className="detail-muted">—</span>}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Created</dt>
            <dd>{formatDateTime(appointment.createdAt)}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Last updated</dt>
            <dd>{formatDateTime(appointment.updatedAt)}</dd>
          </div>
          <div className="detail-list__row">
            <dt>ID</dt>
            <dd>{appointment.id}</dd>
          </div>
        </dl>
      ) : null}

      {!state.loading && !state.error && appointment ? (
        <p className="detail-muted" style={{ fontSize: '0.78rem' }}>
          Times are stored in UTC by the API and displayed in your local time zone.
        </p>
      ) : null}
    </Modal>
  )
}
