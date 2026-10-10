import { useState } from 'react'
import { fetchDepartments } from '../api/departmentApi'
import { Modal } from '../components/common/Modal'
import { useFetch } from '../hooks/useFetch'

const LIMITS = { fullName: 150, specialization: 100, phone: 30, email: 150 }

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function validate(values) {
  const errors = {}
  const fullName = values.fullName.trim()
  if (!fullName) errors.fullName = 'Full name is required.'
  else if (fullName.length > LIMITS.fullName)
    errors.fullName = `Full name must be at most ${LIMITS.fullName} characters.`

  const specialization = values.specialization.trim()
  if (!specialization) errors.specialization = 'Specialization is required.'
  else if (specialization.length > LIMITS.specialization)
    errors.specialization = `Specialization must be at most ${LIMITS.specialization} characters.`

  if (values.phone.trim().length > LIMITS.phone)
    errors.phone = `Phone must be at most ${LIMITS.phone} characters.`

  const email = values.email.trim()
  if (email.length > LIMITS.email)
    errors.email = `Email must be at most ${LIMITS.email} characters.`
  else if (email && !EMAIL_PATTERN.test(email)) errors.email = 'Enter a valid email address.'

  const fee = values.consultationFee.trim()
  if (!fee) {
    errors.consultationFee = 'Consultation fee is required.'
  } else {
    const number = Number(fee)
    if (!Number.isFinite(number)) errors.consultationFee = 'Enter a valid amount.'
    else if (number < 0) errors.consultationFee = 'Fee must be 0 or greater.'
    else {
      const [integerPart, fractionPart = ''] = fee.split('.')
      if (integerPart.length > 8)
        errors.consultationFee = 'Fee must be at most 8 integer digits.'
      else if (fractionPart.length > 2)
        errors.consultationFee = 'Fee supports at most 2 decimal places.'
    }
  }

  if (!values.departmentId) errors.departmentId = 'Select a department.'
  return errors
}

export function DoctorFormModal({ doctor, onSubmit, onClose }) {
  const isEdit = Boolean(doctor)
  const [values, setValues] = useState({
    fullName: doctor?.fullName ?? '',
    specialization: doctor?.specialization ?? '',
    phone: doctor?.phone ?? '',
    email: doctor?.email ?? '',
    consultationFee: doctor ? String(doctor.consultationFee) : '',
    departmentId: doctor ? String(doctor.departmentId) : '',
  })
  const [fieldErrors, setFieldErrors] = useState({})
  const [formError, setFormError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  const departmentOptions = useFetch(
    () => fetchDepartments({ active: 'true', size: 100 }),
    [],
  )

  const activeDepartments = departmentOptions.data?.content || []
  const currentDepartment = isEdit
    ? { id: doctor.departmentId, name: doctor.departmentName }
    : null
  const departments =
    currentDepartment &&
    !activeDepartments.some((department) => department.id === currentDepartment.id)
      ? [currentDepartment, ...activeDepartments]
      : activeDepartments

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
        fullName: values.fullName.trim(),
        specialization: values.specialization.trim(),
        phone: values.phone.trim() || null,
        email: values.email.trim() || null,
        consultationFee: Number(values.consultationFee.trim()),
        departmentId: Number(values.departmentId),
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

  const departmentOptionsFailed =
    !departmentOptions.loading && departmentOptions.error
  const noActiveDepartments =
    !departmentOptions.loading &&
    !departmentOptions.error &&
    activeDepartments.length === 0

  return (
    <Modal
      title={isEdit ? 'Edit doctor' : 'New doctor'}
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
            form="doctor-form"
            disabled={submitting}
          >
            {submitting ? 'Saving…' : isEdit ? 'Save changes' : 'Create doctor'}
          </button>
        </>
      }
    >
      <form id="doctor-form" onSubmit={handleSubmit} noValidate>
        <div className="form-stack">
          {formError ? (
            <div className="alert alert--error" role="alert">
              {formError}
            </div>
          ) : null}

          <div className="form-field">
            <label className="form-field__label" htmlFor="doctor-full-name">
              Full name <span className="form-field__required">*</span>
            </label>
            <input
              id="doctor-full-name"
              className={`form-input${fieldErrors.fullName ? ' form-input--invalid' : ''}`}
              type="text"
              maxLength={LIMITS.fullName}
              value={values.fullName}
              onChange={(event) => update('fullName', event.target.value)}
              aria-invalid={Boolean(fieldErrors.fullName)}
              aria-describedby={
                fieldErrors.fullName ? 'doctor-full-name-error' : undefined
              }
            />
            {fieldErrors.fullName ? (
              <p className="form-field__error" id="doctor-full-name-error">
                {fieldErrors.fullName}
              </p>
            ) : (
              <p className="form-field__hint">
                {values.fullName.length}/{LIMITS.fullName} characters
              </p>
            )}
          </div>

          <div className="form-field">
            <label className="form-field__label" htmlFor="doctor-specialization">
              Specialization <span className="form-field__required">*</span>
            </label>
            <input
              id="doctor-specialization"
              className={`form-input${fieldErrors.specialization ? ' form-input--invalid' : ''}`}
              type="text"
              maxLength={LIMITS.specialization}
              value={values.specialization}
              onChange={(event) => update('specialization', event.target.value)}
              aria-invalid={Boolean(fieldErrors.specialization)}
              aria-describedby={
                fieldErrors.specialization ? 'doctor-specialization-error' : undefined
              }
            />
            {fieldErrors.specialization ? (
              <p className="form-field__error" id="doctor-specialization-error">
                {fieldErrors.specialization}
              </p>
            ) : (
              <p className="form-field__hint">
                {values.specialization.length}/{LIMITS.specialization} characters
              </p>
            )}
          </div>

          <div className="form-field">
            <label className="form-field__label" htmlFor="doctor-phone">
              Phone
            </label>
            <input
              id="doctor-phone"
              className={`form-input${fieldErrors.phone ? ' form-input--invalid' : ''}`}
              type="tel"
              maxLength={LIMITS.phone}
              value={values.phone}
              onChange={(event) => update('phone', event.target.value)}
              aria-invalid={Boolean(fieldErrors.phone)}
              aria-describedby={fieldErrors.phone ? 'doctor-phone-error' : undefined}
            />
            {fieldErrors.phone ? (
              <p className="form-field__error" id="doctor-phone-error">
                {fieldErrors.phone}
              </p>
            ) : (
              <p className="form-field__hint">
                {values.phone.length}/{LIMITS.phone} characters · optional
              </p>
            )}
          </div>

          <div className="form-field">
            <label className="form-field__label" htmlFor="doctor-email">
              Email
            </label>
            <input
              id="doctor-email"
              className={`form-input${fieldErrors.email ? ' form-input--invalid' : ''}`}
              type="email"
              maxLength={LIMITS.email}
              value={values.email}
              onChange={(event) => update('email', event.target.value)}
              aria-invalid={Boolean(fieldErrors.email)}
              aria-describedby={fieldErrors.email ? 'doctor-email-error' : undefined}
            />
            {fieldErrors.email ? (
              <p className="form-field__error" id="doctor-email-error">
                {fieldErrors.email}
              </p>
            ) : (
              <p className="form-field__hint">
                {values.email.length}/{LIMITS.email} characters · optional · must be unique
              </p>
            )}
          </div>

          <div className="form-field">
            <label className="form-field__label" htmlFor="doctor-fee">
              Consultation fee <span className="form-field__required">*</span>
            </label>
            <input
              id="doctor-fee"
              className={`form-input${fieldErrors.consultationFee ? ' form-input--invalid' : ''}`}
              type="number"
              min="0"
              step="0.01"
              inputMode="decimal"
              value={values.consultationFee}
              onChange={(event) => update('consultationFee', event.target.value)}
              aria-invalid={Boolean(fieldErrors.consultationFee)}
              aria-describedby={
                fieldErrors.consultationFee ? 'doctor-fee-error' : undefined
              }
            />
            {fieldErrors.consultationFee ? (
              <p className="form-field__error" id="doctor-fee-error">
                {fieldErrors.consultationFee}
              </p>
            ) : (
              <p className="form-field__hint">e.g. 150.00 · 0 or greater</p>
            )}
          </div>

          <div className="form-field">
            <label className="form-field__label" htmlFor="doctor-department">
              Department <span className="form-field__required">*</span>
            </label>
            <select
              id="doctor-department"
              className={`form-input${fieldErrors.departmentId ? ' form-input--invalid' : ''}`}
              value={values.departmentId}
              onChange={(event) => update('departmentId', event.target.value)}
              aria-invalid={Boolean(fieldErrors.departmentId)}
              aria-describedby={
                fieldErrors.departmentId ? 'doctor-department-error' : undefined
              }
            >
              <option value="">Select a department…</option>
              {departments.map((department) => (
                <option key={department.id} value={department.id}>
                  {department.name}
                  {currentDepartment && department.id === currentDepartment.id && !department.active
                    ? ' (inactive)'
                    : ''}
                </option>
              ))}
            </select>
            {fieldErrors.departmentId ? (
              <p className="form-field__error" id="doctor-department-error">
                {fieldErrors.departmentId}
              </p>
            ) : (
              <p className="form-field__hint">
                Only active departments can be assigned to a doctor.
              </p>
            )}
            {departmentOptionsFailed ? (
              <p className="form-field__error">
                Could not load departments.{' '}
                <button
                  type="button"
                  className="btn btn--secondary"
                  onClick={departmentOptions.reload}
                >
                  Retry
                </button>
              </p>
            ) : null}
          </div>

          {noActiveDepartments && !isEdit ? (
            <div className="alert alert--info" role="status">
              No active departments are available. Create and activate a department
              before adding doctors.
            </div>
          ) : null}
        </div>
      </form>
    </Modal>
  )
}