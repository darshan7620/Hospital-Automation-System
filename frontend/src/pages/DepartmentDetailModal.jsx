import { Modal } from '../components/common/Modal'
import { ErrorState, LoadingState } from '../components/common/StateBlocks'
import { formatDateTime } from '../lib/format'

export function DepartmentDetailModal({ state, onClose }) {
  const department = state.data

  return (
    <Modal
      title="Department details"
      onClose={onClose}
      footer={
        <button type="button" className="btn btn--secondary" onClick={onClose}>
          Close
        </button>
      }
    >
      {state.loading ? <LoadingState label="Loading department…" /> : null}

      {state.error ? (
        <ErrorState
          title="Could not load this department"
          error={state.error}
          onRetry={state.reload}
        />
      ) : null}

      {!state.loading && !state.error && department ? (
        <dl className="detail-list">
          <div className="detail-list__row">
            <dt>Name</dt>
            <dd>{department.name}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Status</dt>
            <dd>
              <span
                className={`badge ${department.active ? 'badge--active' : 'badge--inactive'}`}
              >
                {department.active ? 'Active' : 'Inactive'}
              </span>
            </dd>
          </div>
          <div className="detail-list__row">
            <dt>Description</dt>
            <dd>{department.description || <span className="detail-muted">—</span>}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Location</dt>
            <dd>{department.location || <span className="detail-muted">—</span>}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Created</dt>
            <dd>{formatDateTime(department.createdAt)}</dd>
          </div>
          <div className="detail-list__row">
            <dt>Last updated</dt>
            <dd>{formatDateTime(department.updatedAt)}</dd>
          </div>
          <div className="detail-list__row">
            <dt>ID</dt>
            <dd>{department.id}</dd>
          </div>
        </dl>
      ) : null}
    </Modal>
  )
}
