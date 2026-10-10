import { Modal } from '../components/common/Modal'
import { ErrorState, LoadingState } from '../components/common/StateBlocks'
import { formatDate, formatDateTime } from '../lib/format'

export function PatientDetailModal({ state, onClose }) {
  const patient = state.data

  return (
    <Modal
      title="Patient details"
      onClose={onClose}
      footer={
        <button type="button" className="btn btn--secondary" onClick={onClose}>
          Close
        </button>
      }
    >
      {state.loading ? <LoadingState label="Loading patient…" /> : null}

      {state.error ? (
        <ErrorState
          title="Could not load this patient"
          error={state.error}
          onRetry={state.reload}
        />
      ) : null}

      {!state.loading && !state.error && patient ? (
        <dl className="detail-list">
          <div className="detail-list__row">
            <dt>Patient number</dt>
            <dd className="detail-list__number">{patient.patientNumber}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Full name</dt>
            <dd>{patient.fullName}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Date of birth</dt>
            <dd>{formatDate(patient.dateOfBirth)}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Phone</dt>
            <dd>{patient.phone || <span className="detail-muted">—</span>}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Address</dt>
            <dd>{patient.address || <span className="detail-muted">—</span>}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Status</dt>
            <dd>
              <span
                className={`badge ${patient.active ? 'badge--active' : 'badge--inactive'}`}
              >
                {patient.active ? 'Active' : 'Inactive'}
              </span>
            </dd>
          </div>
          <div className="detail-list__row">
            <dt>Registered</dt>
            <dd>{formatDateTime(patient.createdAt)}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Last updated</dt>
            <dd>{formatDateTime(patient.updatedAt)}</dd>
          </div>
          <div className="detail-list__row">
            <dt>ID</dt>
            <dd>{patient.id}</dd>
          </div>
        </dl>
      ) : null}
    </Modal>
  )
}