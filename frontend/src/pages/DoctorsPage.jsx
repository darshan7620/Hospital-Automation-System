import { useEffect, useState } from 'react'
import {
  activateDoctor,
  createDoctor,
  deactivateDoctor,
  fetchDoctor,
  fetchDoctors,
  updateDoctor,
} from '../api/doctorApi'
import { fetchDepartments } from '../api/departmentApi'
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
import { formatCurrency } from '../lib/format'
import { DoctorDetailModal } from './DoctorDetailModal'
import { DoctorFormModal } from './DoctorFormModal'
import './doctors.css'

const PAGE_SIZE = 10
const SEARCH_DEBOUNCE_MS = 300

function ActiveBadge({ active }) {
  return (
    <span className={`badge ${active ? 'badge--active' : 'badge--inactive'}`}>
      {active ? 'Active' : 'Inactive'}
    </span>
  )
}

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

export function DoctorsPage() {
  const [page, setPage] = useState(0)
  const [searchInput, setSearchInput] = useState('')
  const [search, setSearch] = useState('')
  const [specInput, setSpecInput] = useState('')
  const [spec, setSpec] = useState('')
  const [departmentFilter, setDepartmentFilter] = useState('')
  const [activeFilter, setActiveFilter] = useState('')
  const [sort, setSort] = useState({ field: 'fullName', direction: 'asc' })

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
      setSpec(specInput.trim())
      setPage(0)
    }, SEARCH_DEBOUNCE_MS)
    return () => clearTimeout(timer)
  }, [searchInput, specInput])

  const list = useFetch(
    () =>
      fetchDoctors({
        name: search,
        specialization: spec,
        departmentId: departmentFilter,
        active: activeFilter,
        page,
        size: PAGE_SIZE,
        sort: sort.field,
        direction: sort.direction,
      }),
    [search, spec, departmentFilter, activeFilter, page, sort.field, sort.direction],
  )

  const departmentOptions = useFetch(
    () => fetchDepartments({ active: 'true', size: 100 }),
    [],
  )

  const detail = useFetch(
    () => (viewId === null ? Promise.resolve(null) : fetchDoctor(viewId)),
    [viewId],
  )

  const rows = list.data?.content || []
  const departments = departmentOptions.data?.content || []
  const hasFilters =
    search !== '' || spec !== '' || departmentFilter !== '' || activeFilter !== ''

  function changeFilter(setter, value) {
    setter(value)
    setPage(0)
  }

  function clearFilters() {
    setSearchInput('')
    setSearch('')
    setSpecInput('')
    setSpec('')
    setDepartmentFilter('')
    setActiveFilter('')
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
    await createDoctor(values)
    setFormState(null)
    setFeedback({ type: 'success', message: `Doctor "${values.fullName}" created.` })
    list.reload()
  }

  async function handleUpdate(values) {
    await updateDoctor(formState.id, values)
    setFormState(null)
    setFeedback({ type: 'success', message: `Doctor "${values.fullName}" updated.` })
    list.reload()
    if (viewId === formState.id) detail.reload()
  }

  async function handleDeactivate() {
    setConfirmBusy(true)
    setConfirmError(null)
    try {
      await deactivateDoctor(confirmTarget.id)
      setFeedback({
        type: 'success',
        message: `Doctor "${confirmTarget.fullName}" deactivated.`,
      })
      setConfirmTarget(null)
      if (rows.length === 1 && page > 0) {
        setPage(page - 1)
      } else {
        list.reload()
      }
      if (viewId === confirmTarget.id) detail.reload()
    } catch (error) {
      setConfirmError(error?.message || 'Could not deactivate this doctor.')
    } finally {
      setConfirmBusy(false)
    }
  }

  async function handleActivate(doctor) {
    setPendingId(doctor.id)
    try {
      await activateDoctor(doctor.id)
      setFeedback({
        type: 'success',
        message: `Doctor "${doctor.fullName}" reactivated.`,
      })
      list.reload()
      if (viewId === doctor.id) detail.reload()
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error?.message || `Could not reactivate "${doctor.fullName}".`,
      })
    } finally {
      setPendingId(null)
    }
  }

  return (
    <div className="doctors">
      <header className="doctors__intro">
        <div>
          <h2>Doctors</h2>
          <p className="doctors__subtitle">
            {list.data
              ? `${list.data.totalElements} doctor${list.data.totalElements === 1 ? '' : 's'}${hasFilters ? ' matching filters' : ''} · live from the MediCore API`
              : 'Live from the MediCore API'}
          </p>
        </div>
        <button
          type="button"
          className="btn btn--primary"
          onClick={() => setFormState({ mode: 'create' })}
        >
          <PlusIcon />
          New doctor
        </button>
      </header>

      {feedback ? (
        <Alert type={feedback.type} onDismiss={() => setFeedback(null)}>
          {feedback.message}
        </Alert>
      ) : null}

      <section className="card doctors__toolbar" aria-label="Search and filters">
        <div className="form-field doctors__search">
          <label className="form-field__label" htmlFor="doctor-search">
            Search by name
          </label>
          <input
            id="doctor-search"
            className="form-input"
            type="search"
            placeholder="e.g. Dr. Smith"
            value={searchInput}
            onChange={(event) => setSearchInput(event.target.value)}
          />
        </div>
        <div className="form-field doctors__search">
          <label className="form-field__label" htmlFor="doctor-specialization">
            Specialization
          </label>
          <input
            id="doctor-specialization"
            className="form-input"
            type="search"
            placeholder="e.g. Cardiology"
            value={specInput}
            onChange={(event) => setSpecInput(event.target.value)}
          />
        </div>
        <div className="form-field">
          <label className="form-field__label" htmlFor="doctor-department">
            Department
          </label>
          <select
            id="doctor-department"
            className="form-input"
            value={departmentFilter}
            onChange={(event) => changeFilter(setDepartmentFilter, event.target.value)}
          >
            <option value="">All departments</option>
            {departments.map((department) => (
              <option key={department.id} value={department.id}>
                {department.name}
              </option>
            ))}
          </select>
        </div>
        <div className="form-field">
          <label className="form-field__label" htmlFor="doctor-status">
            Status
          </label>
          <select
            id="doctor-status"
            className="form-input"
            value={activeFilter}
            onChange={(event) => changeFilter(setActiveFilter, event.target.value)}
          >
            <option value="">All</option>
            <option value="true">Active</option>
            <option value="false">Inactive</option>
          </select>
        </div>
        {hasFilters ? (
          <button
            type="button"
            className="btn btn--secondary doctors__clear"
            onClick={clearFilters}
          >
            Clear filters
          </button>
        ) : null}
      </section>

      <section className="card doctors__panel" aria-label="Doctor list">
        {list.loading ? <LoadingState label="Loading doctors…" /> : null}

        {list.error ? (
          <ErrorState
            title="Could not load doctors"
            error={list.error}
            onRetry={list.reload}
          />
        ) : null}

        {!list.loading && !list.error && rows.length === 0 ? (
          hasFilters ? (
            <EmptyState
              title="No doctors match your filters"
              message="Try different search terms or filters, or clear them."
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
              title="No doctors yet"
              message="Create the first doctor to get started."
              action={
                <button
                  type="button"
                  className="btn btn--primary"
                  onClick={() => setFormState({ mode: 'create' })}
                >
                  <PlusIcon />
                  New doctor
                </button>
              }
            />
          )
        ) : null}

        {!list.loading && !list.error && rows.length > 0 ? (
          <>
            <div className="table-wrap">
              <table className="data-table doctors-table">
                <thead>
                  <tr>
                    <SortableTh label="Name" field="fullName" sort={sort} onSort={handleSort} />
                    <SortableTh
                      label="Specialization"
                      field="specialization"
                      sort={sort}
                      onSort={handleSort}
                    />
                    <th scope="col">Department</th>
                    <SortableTh
                      label="Fee"
                      field="consultationFee"
                      sort={sort}
                      onSort={handleSort}
                      thClass="doctors-table__fee"
                    />
                    <th scope="col">Status</th>
                    <th scope="col">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((doctor) => (
                    <tr key={doctor.id}>
                      <td>
                        <span className="cell-primary">{doctor.fullName}</span>
                        {doctor.email ? (
                          <span className="cell-secondary">{doctor.email}</span>
                        ) : null}
                      </td>
                      <td>
                        <span className="cell-primary">{doctor.specialization}</span>
                        {doctor.phone ? (
                          <span className="cell-secondary">{doctor.phone}</span>
                        ) : null}
                      </td>
                      <td className="doctors-table__department">
                        {doctor.departmentName || (
                          <span className="cell-secondary">—</span>
                        )}
                        <span className="cell-secondary">ID {doctor.departmentId}</span>
                      </td>
                      <td className="doctors-table__fee">
                        {formatCurrency(doctor.consultationFee)}
                      </td>
                      <td>
                        <ActiveBadge active={doctor.active} />
                      </td>
                      <td>
                        <div className="doctors-table__actions">
                          <button
                            type="button"
                            className="btn btn--secondary btn--icon"
                            title="View details"
                            aria-label={`View ${doctor.fullName}`}
                            onClick={() => setViewId(doctor.id)}
                          >
                            <EyeIcon />
                          </button>
                          <button
                            type="button"
                            className="btn btn--secondary btn--icon"
                            title="Edit"
                            aria-label={`Edit ${doctor.fullName}`}
                            onClick={() =>
                              setFormState({ mode: 'edit', id: doctor.id, doctor })
                            }
                          >
                            <EditIcon />
                          </button>
                          {doctor.active ? (
                            <button
                              type="button"
                              className="btn btn--secondary btn--icon"
                              title="Deactivate"
                              aria-label={`Deactivate ${doctor.fullName}`}
                              disabled={pendingId === doctor.id}
                              onClick={() => {
                                setConfirmError(null)
                                setConfirmTarget(doctor)
                              }}
                            >
                              <BanIcon />
                            </button>
                          ) : (
                            <button
                              type="button"
                              className="btn btn--secondary btn--icon"
                              title="Reactivate"
                              aria-label={`Reactivate ${doctor.fullName}`}
                              disabled={pendingId === doctor.id}
                              onClick={() => handleActivate(doctor)}
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
        <DoctorFormModal
          doctor={formState.mode === 'edit' ? formState.doctor : null}
          onSubmit={formState.mode === 'edit' ? handleUpdate : handleCreate}
          onClose={() => setFormState(null)}
        />
      ) : null}

      {viewId !== null ? (
        <DoctorDetailModal state={detail} onClose={() => setViewId(null)} />
      ) : null}

      {confirmTarget ? (
        <ConfirmDialog
          title="Deactivate doctor"
          message={`Deactivate "${confirmTarget.fullName}"? They will no longer be available for new appointments.`}
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