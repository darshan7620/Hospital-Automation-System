import { Modal } from '../components/common/Modal'
import { ErrorState, LoadingState } from '../components/common/StateBlocks'
import { formatCurrency, formatDateTime } from '../lib/format'

export function DoctorDetailModal({ state, onClose }) {
  const doctor = state.data

  return (
    <Modal
      title="Doctor details"
      onClose={onClose}
      footer={
        <button type="button" className="btn btn--secondary" onClick={onClose}>
          Close
        </button>
      }
    >
      {state.loading ? <LoadingState label="Loading doctor…" /> : null}

      {state.error ? (
        <ErrorState
          title="Could not load this doctor"
          error={state.error}
          onRetry={state.reload}
        />
      ) : null}

      {!state.loading && !state.error && doctor ? (
        <dl className="detail-list">
          <div className="detail-list__row">
            <dt>Full name</dt>
            <dd>{doctor.fullName}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Specialization</dt>
            <dd>{doctor.specialization}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Department</dt>
            <dd>
              {doctor.departmentName || <span className="detail-muted">—</span>}
              {doctor.departmentId ? (
                <span className="detail-muted"> · ID {doctor.departmentId}</span>
              ) : null}
            </dd>
          </div>
          <div className="detail-list__row">
            <dt>Status</dt>
            <dd>
              <span
                className={`badge ${doctor.active ? 'badge--active' : 'badge--inactive'}`}
              >
                {doctor.active ? 'Active' : 'Inactive'}
              </span>
            </dd>
          </div>
          <div className="detail-list__row">
            <dt>Phone</dt>
            <dd>{doctor.phone || <span className="detail-muted">—</span>}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Email</dt>
            <dd>{doctor.email || <span className="detail-muted">—</span>}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Consultation fee</dt>
            <dd>{formatCurrency(doctor.consultationFee)}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Created</dt>
            <dd>{formatDateTime(doctor.createdAt)}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Last updated</dt>
            <dd>{formatDateTime(doctor.updatedAt)}</dd>
          </div>
          <div className="detail-list__row">
            <dt>ID</dt>
            <dd>{doctor.id}</dd>
          </div>
        </dl>
      ) : null}
    </Modal>
  )
}