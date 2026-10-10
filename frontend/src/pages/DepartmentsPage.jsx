import { useEffect, useState } from 'react'
import {
  activateDepartment,
  createDepartment,
  deactivateDepartment,
  fetchDepartment,
  fetchDepartments,
  updateDepartment,
} from '../api/departmentApi'
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
  RefreshIcon,
} from '../components/common/icons'
import { useFetch } from '../hooks/useFetch'
import { DepartmentDetailModal } from './DepartmentDetailModal'
import { DepartmentFormModal } from './DepartmentFormModal'
import './departments.css'

const PAGE_SIZE = 10
const SEARCH_DEBOUNCE_MS = 300

function ActiveBadge({ active }) {
  return (
    <span className={`badge ${active ? 'badge--active' : 'badge--inactive'}`}>
      {active ? 'Active' : 'Inactive'}
    </span>
  )
}

export function DepartmentsPage() {
  const [page, setPage] = useState(0)
  const [searchInput, setSearchInput] = useState('')
  const [search, setSearch] = useState('')
  const [activeFilter, setActiveFilter] = useState('')

  const [formState, setFormState] = useState(null)
  const [viewId, setViewId] = useState(null)
  const [confirmTarget, setConfirmTarget] = useState(null)
  const [confirmBusy, setConfirmBusy] = useState(false)
  const [confirmError, setConfirmError] = useState(null)
  const [pendingId, setPendingId] = useState(null)
  const [feedback, setFeedback] = useState(null)

  useEffect(() => {
    const timer = setTimeout(() => {
      setSearch(searchInput.trim())
      setPage(0)
    }, SEARCH_DEBOUNCE_MS)
    return () => clearTimeout(timer)
  }, [searchInput])

  const list = useFetch(
    () => fetchDepartments({ name: search, active: activeFilter, page, size: PAGE_SIZE }),
    [search, activeFilter, page],
  )

  const detail = useFetch(
    () => (viewId === null ? Promise.resolve(null) : fetchDepartment(viewId)),
    [viewId],
  )

  const rows = list.data?.content || []
  const hasFilters = search !== '' || activeFilter !== ''

  function changeFilter(value) {
    setActiveFilter(value)
    setPage(0)
  }

  function clearFilters() {
    setSearchInput('')
    setSearch('')
    setActiveFilter('')
    setPage(0)
  }

  async function handleCreate(values) {
    await createDepartment(values)
    setFormState(null)
    setFeedback({ type: 'success', message: `Department "${values.name}" created.` })
    list.reload()
  }

  async function handleUpdate(values) {
    await updateDepartment(formState.id, values)
    setFormState(null)
    setFeedback({ type: 'success', message: `Department "${values.name}" updated.` })
    list.reload()
    if (viewId === formState.id) detail.reload()
  }

  async function handleDeactivate() {
    setConfirmBusy(true)
    setConfirmError(null)
    try {
      await deactivateDepartment(confirmTarget.id)
      setFeedback({
        type: 'success',
        message: `Department "${confirmTarget.name}" deactivated.`,
      })
      setConfirmTarget(null)
      if (rows.length === 1 && page > 0) {
        setPage(page - 1)
      } else {
        list.reload()
      }
      if (viewId === confirmTarget.id) detail.reload()
    } catch (error) {
      setConfirmError(error?.message || 'Could not deactivate this department.')
    } finally {
      setConfirmBusy(false)
    }
  }

  async function handleActivate(department) {
    setPendingId(department.id)
    try {
      await activateDepartment(department.id)
      setFeedback({
        type: 'success',
        message: `Department "${department.name}" reactivated.`,
      })
      list.reload()
      if (viewId === department.id) detail.reload()
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error?.message || `Could not reactivate "${department.name}".`,
      })
    } finally {
      setPendingId(null)
    }
  }

  return (
    <div className="departments">
      <header className="departments__intro">
        <div>
          <h2>Departments</h2>
          <p className="departments__subtitle">
            {list.data
              ? `${list.data.totalElements} department${list.data.totalElements === 1 ? '' : 's'}${hasFilters ? ' matching filters' : ''} · live from the MediCore API`
              : 'Live from the MediCore API'}
          </p>
        </div>
        <button
          type="button"
          className="btn btn--primary"
          onClick={() => setFormState({ mode: 'create' })}
        >
          <PlusIcon />
          New department
        </button>
      </header>

      {feedback ? (
        <Alert type={feedback.type} onDismiss={() => setFeedback(null)}>
          {feedback.message}
        </Alert>
      ) : null}

      <section className="card departments__toolbar" aria-label="Search and filters">
        <div className="form-field departments__search">
          <label className="form-field__label" htmlFor="department-search">
            Search by name
          </label>
          <input
            id="department-search"
            className="form-input"
            type="search"
            placeholder="e.g. Cardiology"
            value={searchInput}
            onChange={(event) => setSearchInput(event.target.value)}
          />
        </div>
        <div className="form-field">
          <label className="form-field__label" htmlFor="department-status">
            Status
          </label>
          <select
            id="department-status"
            className="form-input"
            value={activeFilter}
            onChange={(event) => changeFilter(event.target.value)}
          >
            <option value="">All</option>
            <option value="true">Active</option>
            <option value="false">Inactive</option>
          </select>
        </div>
        {hasFilters ? (
          <button
            type="button"
            className="btn btn--secondary departments__clear"
            onClick={clearFilters}
          >
            Clear filters
          </button>
        ) : null}
      </section>

      <section className="card departments__panel" aria-label="Department list">
        {list.loading ? <LoadingState label="Loading departments…" /> : null}

        {list.error ? (
          <ErrorState
            title="Could not load departments"
            error={list.error}
            onRetry={list.reload}
          />
        ) : null}

        {!list.loading && !list.error && rows.length === 0 ? (
          hasFilters ? (
            <EmptyState
              title="No departments match your filters"
              message="Try a different name or status, or clear the filters."
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
              title="No departments yet"
              message="Create the first department to get started."
              action={
                <button
                  type="button"
                  className="btn btn--primary"
                  onClick={() => setFormState({ mode: 'create' })}
                >
                  <PlusIcon />
                  New department
                </button>
              }
            />
          )
        ) : null}

        {!list.loading && !list.error && rows.length > 0 ? (
          <>
            <div className="table-wrap">
              <table className="data-table departments-table">
                <thead>
                  <tr>
                    <th scope="col">Name</th>
                    <th scope="col">Description</th>
                    <th scope="col">Status</th>
                    <th scope="col">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((department) => (
                    <tr key={department.id}>
                      <td>
                        <span className="cell-primary">{department.name}</span>
                        {department.location ? (
                          <span className="cell-secondary">{department.location}</span>
                        ) : null}
                      </td>
                      <td className="departments-table__description">
                        {department.description || (
                          <span className="cell-secondary">—</span>
                        )}
                      </td>
                      <td>
                        <ActiveBadge active={department.active} />
                      </td>
                      <td>
                        <div className="departments-table__actions">
                          <button
                            type="button"
                            className="btn btn--secondary btn--icon"
                            title="View details"
                            aria-label={`View ${department.name}`}
                            onClick={() => setViewId(department.id)}
                          >
                            <EyeIcon />
                          </button>
                          <button
                            type="button"
                            className="btn btn--secondary btn--icon"
                            title="Edit"
                            aria-label={`Edit ${department.name}`}
                            onClick={() =>
                              setFormState({ mode: 'edit', id: department.id, department })
                            }
                          >
                            <EditIcon />
                          </button>
                          {department.active ? (
                            <button
                              type="button"
                              className="btn btn--secondary btn--icon"
                              title="Deactivate"
                              aria-label={`Deactivate ${department.name}`}
                              disabled={pendingId === department.id}
                              onClick={() => {
                                setConfirmError(null)
                                setConfirmTarget(department)
                              }}
                            >
                              <BanIcon />
                            </button>
                          ) : (
                            <button
                              type="button"
                              className="btn btn--secondary btn--icon"
                              title="Reactivate"
                              aria-label={`Reactivate ${department.name}`}
                              disabled={pendingId === department.id}
                              onClick={() => handleActivate(department)}
                            >
                              <RefreshIcon />
                            </button>
                          )}
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
        <DepartmentFormModal
          department={formState.mode === 'edit' ? formState.department : null}
          onSubmit={formState.mode === 'edit' ? handleUpdate : handleCreate}
          onClose={() => setFormState(null)}
        />
      ) : null}

      {viewId !== null ? (
        <DepartmentDetailModal state={detail} onClose={() => setViewId(null)} />
      ) : null}

      {confirmTarget ? (
        <ConfirmDialog
          title="Deactivate department"
          message={`Deactivate "${confirmTarget.name}"? It will no longer be assignable to doctors or new appointments.`}
          confirmLabel="Deactivate"
          busy={confirmBusy}
          error={confirmError}
          onConfirm={handleDeactivate}
          onCancel={() => setConfirmTarget(null)}
        />
      ) : null}
    </div>
  )
}
