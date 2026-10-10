import { useEffect, useState } from 'react'
import {
  activatePatient,
  createPatient,
  deactivatePatient,
  fetchPatient,
  fetchPatients,
  updatePatient,
} from '../api/patientApi'
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
import { PatientDetailModal } from './PatientDetailModal'
import { PatientFormModal } from './PatientFormModal'
import './patients.css'

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

export function PatientsPage() {
  const [page, setPage] = useState(0)
  const [nameInput, setNameInput] = useState('')
  const [nameSearch, setNameSearch] = useState('')
  const [phoneInput, setPhoneInput] = useState('')
  const [phoneSearch, setPhoneSearch] = useState('')
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
      setNameSearch(nameInput.trim())
      setPhoneSearch(phoneInput.trim())
      setPage(0)
    }, SEARCH_DEBOUNCE_MS)
    return () => clearTimeout(timer)
  }, [nameInput, phoneInput])

  const list = useFetch(
    () =>
      fetchPatients({
        name: nameSearch,
        phone: phoneSearch,
        active: activeFilter,
        page,
        size: PAGE_SIZE,
        sort: sort.field,
        direction: sort.direction,
      }),
    [nameSearch, phoneSearch, activeFilter, page, sort.field, sort.direction],
  )

  const detail = useFetch(
    () => (viewId === null ? Promise.resolve(null) : fetchPatient(viewId)),
    [viewId],
  )

  const rows = list.data?.content || []
  const hasFilters =
    nameSearch !== '' || phoneSearch !== '' || activeFilter !== ''

  function changeFilter(setter, value) {
    setter(value)
    setPage(0)
  }

  function clearFilters() {
    setNameInput('')
    setNameSearch('')
    setPhoneInput('')
    setPhoneSearch('')
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
    await createPatient(values)
    setFormState(null)
    setFeedback({ type: 'success', message: `Patient "${values.fullName}" registered.` })
    list.reload()
  }

  async function handleUpdate(values) {
    await updatePatient(formState.id, values)
    setFormState(null)
    setFeedback({ type: 'success', message: `Patient "${values.fullName}" updated.` })
    list.reload()
    if (viewId === formState.id) detail.reload()
  }

  async function handleDeactivate() {
    setConfirmBusy(true)
    setConfirmError(null)
    try {
      await deactivatePatient(confirmTarget.id)
      setFeedback({
        type: 'success',
        message: `Patient "${confirmTarget.fullName}" deactivated.`,
      })
      setConfirmTarget(null)
      if (rows.length === 1 && page > 0) {
        setPage(page - 1)
      } else {
        list.reload()
      }
      if (viewId === confirmTarget.id) detail.reload()
    } catch (error) {
      setConfirmError(error?.message || 'Could not deactivate this patient.')
    } finally {
      setConfirmBusy(false)
    }
  }

  async function handleActivate(patient) {
    setPendingId(patient.id)
    try {
      await activatePatient(patient.id)
      setFeedback({
        type: 'success',
        message: `Patient "${patient.fullName}" reactivated.`,
      })
      list.reload()
      if (viewId === patient.id) detail.reload()
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error?.message || `Could not reactivate "${patient.fullName}".`,
      })
    } finally {
      setPendingId(null)
    }
  }

  return (
    <div className="patients">
      <header className="patients__intro">
        <div>
          <h2>Patients</h2>
          <p className="patients__subtitle">
            {list.data
              ? `${list.data.totalElements} patient${list.data.totalElements === 1 ? '' : 's'}${hasFilters ? ' matching filters' : ''} · live from the MediCore API`
              : 'Live from the MediCore API'}
          </p>
        </div>
        <button
          type="button"
          className="btn btn--primary"
          onClick={() => setFormState({ mode: 'create' })}
        >
          <PlusIcon />
          Register patient
        </button>
      </header>

      {feedback ? (
        <Alert type={feedback.type} onDismiss={() => setFeedback(null)}>
          {feedback.message}
        </Alert>
      ) : null}

      <section className="card patients__toolbar" aria-label="Search and filters">
        <div className="form-field patients__search">
          <label className="form-field__label" htmlFor="patient-search">
            Search by name
          </label>
          <input
            id="patient-search"
            className="form-input"
            type="search"
            placeholder="e.g. Jane Doe"
            value={nameInput}
            onChange={(event) => setNameInput(event.target.value)}
          />
        </div>
        <div className="form-field patients__search">
          <label className="form-field__label" htmlFor="patient-phone">
            Phone
          </label>
          <input
            id="patient-phone"
            className="form-input"
            type="search"
            placeholder="e.g. +1 555 0100"
            value={phoneInput}
            onChange={(event) => setPhoneInput(event.target.value)}
          />
        </div>
        <div className="form-field">
          <label className="form-field__label" htmlFor="patient-status">
            Status
          </label>
          <select
            id="patient-status"
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
            className="btn btn--secondary patients__clear"
            onClick={clearFilters}
          >
            Clear filters
          </button>
        ) : null}
      </section>

      <section className="card patients__panel" aria-label="Patient list">
        {list.loading ? <LoadingState label="Loading patients…" /> : null}

        {list.error ? (
          <ErrorState
            title="Could not load patients"
            error={list.error}
            onRetry={list.reload}
          />
        ) : null}

        {!list.loading && !list.error && rows.length === 0 ? (
          hasFilters ? (
            <EmptyState
              title="No patients match your filters"
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
              title="No patients yet"
              message="Register the first patient to get started."
              action={
                <button
                  type="button"
                  className="btn btn--primary"
                  onClick={() => setFormState({ mode: 'create' })}
                >
                  <PlusIcon />
                  Register patient
                </button>
              }
            />
          )
        ) : null}

        {!list.loading && !list.error && rows.length > 0 ? (
          <>
            <div className="table-wrap">
              <table className="data-table patients-table">
                <thead>
                  <tr>
                    <SortableTh
                      label="Patient number"
                      field="patientNumber"
                      sort={sort}
                      onSort={handleSort}
                      thClass="patients-table__number"
                    />
                    <SortableTh
                      label="Name"
                      field="fullName"
                      sort={sort}
                      onSort={handleSort}
                    />
                    <SortableTh
                      label="Phone"
                      field="phone"
                      sort={sort}
                      onSort={handleSort}
                    />
                    <th scope="col">Status</th>
                    <th scope="col">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((patient) => (
                    <tr key={patient.id}>
                      <td className="patients-table__number">
                        <span className="cell-primary">{patient.patientNumber}</span>
                      </td>
                      <td>
                        <span className="cell-primary">{patient.fullName}</span>
                      </td>
                      <td>
                        <span className="cell-primary">{patient.phone}</span>
                      </td>
                      <td>
                        <ActiveBadge active={patient.active} />
                      </td>
                      <td>
                        <div className="patients-table__actions">
                          <button
                            type="button"
                            className="btn btn--secondary btn--icon"
                            title="View details"
                            aria-label={`View ${patient.fullName}`}
                            onClick={() => setViewId(patient.id)}
                          >
                            <EyeIcon />
                          </button>
                          <button
                            type="button"
                            className="btn btn--secondary btn--icon"
                            title="Edit"
                            aria-label={`Edit ${patient.fullName}`}
                            onClick={() =>
                              setFormState({ mode: 'edit', id: patient.id, patient })
                            }
                          >
                            <EditIcon />
                          </button>
                          {patient.active ? (
                            <button
                              type="button"
                              className="btn btn--secondary btn--icon"
                              title="Deactivate"
                              aria-label={`Deactivate ${patient.fullName}`}
                              disabled={pendingId === patient.id}
                              onClick={() => {
                                setConfirmError(null)
                                setConfirmTarget(patient)
                              }}
                            >
                              <BanIcon />
                            </button>
                          ) : (
                            <button
                              type="button"
                              className="btn btn--secondary btn--icon"
                              title="Reactivate"
                              aria-label={`Reactivate ${patient.fullName}`}
                              disabled={pendingId === patient.id}
                              onClick={() => handleActivate(patient)}
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
        <PatientFormModal
          patient={formState.mode === 'edit' ? formState.patient : null}
          onSubmit={formState.mode === 'edit' ? handleUpdate : handleCreate}
          onClose={() => setFormState(null)}
        />
      ) : null}

      {viewId !== null ? (
        <PatientDetailModal state={detail} onClose={() => setViewId(null)} />
      ) : null}

      {confirmTarget ? (
        <ConfirmDialog
          title="Deactivate patient"
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