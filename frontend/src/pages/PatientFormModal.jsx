import { useState } from 'react'
import { Modal } from '../components/common/Modal'

const LIMITS = { fullName: 150, phone: 30, address: 255 }
const PHONE_PATTERN = /^[0-9+()\-\s]{7,30}$/
const MAX_BIRTH_DATE = localDateString(new Date())

function localDateString(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function validate(values) {
  const errors = {}
  const fullName = values.fullName.trim()
  if (!fullName) errors.fullName = 'Full name is required.'
  else if (fullName.length > LIMITS.fullName)
    errors.fullName = `Full name must be at most ${LIMITS.fullName} characters.`

  const today = localDateString(new Date())
  if (!values.dateOfBirth) {
    errors.dateOfBirth = 'Date of birth is required.'
  } else if (values.dateOfBirth >= today) {
    errors.dateOfBirth = 'Date of birth must be in the past.'
  }

  const phone = values.phone.trim()
  if (!phone) errors.phone = 'Phone is required.'
  else if (phone.length > LIMITS.phone)
    errors.phone = `Phone must be at most ${LIMITS.phone} characters.`
  else if (!PHONE_PATTERN.test(phone))
    errors.phone = 'Phone must contain 7 to 30 digits and may include +, -, spaces and parentheses.'

  if (values.address.length > LIMITS.address)
    errors.address = `Address must be at most ${LIMITS.address} characters.`

  return errors
}

export function PatientFormModal({ patient, onSubmit, onClose }) {
  const isEdit = Boolean(patient)
  const [values, setValues] = useState({
    fullName: patient?.fullName ?? '',
    dateOfBirth: patient?.dateOfBirth ?? '',
    phone: patient?.phone ?? '',
    address: patient?.address ?? '',
  })
  const [fieldErrors, setFieldErrors] = useState({})
  const [formError, setFormError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

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
        dateOfBirth: values.dateOfBirth,
        phone: values.phone.trim(),
        address: values.address.trim() || null,
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

  return (
    <Modal
      title={isEdit ? 'Edit patient' : 'Register patient'}
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
            form="patient-form"
            disabled={submitting}
          >
            {submitting ? 'Saving…' : isEdit ? 'Save changes' : 'Register patient'}
          </button>
        </>
      }
    >
      <form id="patient-form" onSubmit={handleSubmit} noValidate>
        <div className="form-stack">
          {formError ? (
            <div className="alert alert--error" role="alert">
              {formError}
            </div>
          ) : null}

          {isEdit ? (
            <div className="alert alert--info" role="status">
              Patient number {patient.patientNumber} — assigned by the server.
            </div>
          ) : (
            <div className="alert alert--info" role="status">
              Patient number is assigned automatically by the server after
              registration.
            </div>
          )}

          <div className="form-field">
            <label className="form-field__label" htmlFor="patient-full-name">
              Full name <span className="form-field__required">*</span>
            </label>
            <input
              id="patient-full-name"
              className={`form-input${fieldErrors.fullName ? ' form-input--invalid' : ''}`}
              type="text"
              maxLength={LIMITS.fullName}
              value={values.fullName}
              onChange={(event) => update('fullName', event.target.value)}
              aria-invalid={Boolean(fieldErrors.fullName)}
              aria-describedby={
                fieldErrors.fullName ? 'patient-full-name-error' : undefined
              }
            />
            {fieldErrors.fullName ? (
              <p className="form-field__error" id="patient-full-name-error">
                {fieldErrors.fullName}
              </p>
            ) : (
              <p className="form-field__hint">
                {values.fullName.length}/{LIMITS.fullName} characters
              </p>
            )}
          </div>

          <div className="form-field">
            <label className="form-field__label" htmlFor="patient-dob">
              Date of birth <span className="form-field__required">*</span>
            </label>
            <input
              id="patient-dob"
              className={`form-input${fieldErrors.dateOfBirth ? ' form-input--invalid' : ''}`}
              type="date"
              max={MAX_BIRTH_DATE}
              value={values.dateOfBirth}
              onChange={(event) => update('dateOfBirth', event.target.value)}
              aria-invalid={Boolean(fieldErrors.dateOfBirth)}
              aria-describedby={
                fieldErrors.dateOfBirth ? 'patient-dob-error' : undefined
              }
            />
            {fieldErrors.dateOfBirth ? (
              <p className="form-field__error" id="patient-dob-error">
                {fieldErrors.dateOfBirth}
              </p>
            ) : (
              <p className="form-field__hint">Must be in the past</p>
            )}
          </div>

          <div className="form-field">
            <label className="form-field__label" htmlFor="patient-phone">
              Phone <span className="form-field__required">*</span>
            </label>
            <input
              id="patient-phone"
              className={`form-input${fieldErrors.phone ? ' form-input--invalid' : ''}`}
              type="tel"
              maxLength={LIMITS.phone}
              value={values.phone}
              onChange={(event) => update('phone', event.target.value)}
              aria-invalid={Boolean(fieldErrors.phone)}
              aria-describedby={fieldErrors.phone ? 'patient-phone-error' : undefined}
            />
            {fieldErrors.phone ? (
              <p className="form-field__error" id="patient-phone-error">
                {fieldErrors.phone}
              </p>
            ) : (
              <p className="form-field__hint">
                {values.phone.length}/{LIMITS.phone} characters · digits and +, -,
                spaces, parentheses
              </p>
            )}
          </div>

          <div className="form-field">
            <label className="form-field__label" htmlFor="patient-address">
              Address
            </label>
            <textarea
              id="patient-address"
              className={`form-input${fieldErrors.address ? ' form-input--invalid' : ''}`}
              maxLength={LIMITS.address}
              rows={3}
              value={values.address}
              onChange={(event) => update('address', event.target.value)}
              aria-invalid={Boolean(fieldErrors.address)}
              aria-describedby={
                fieldErrors.address ? 'patient-address-error' : undefined
              }
            />
            {fieldErrors.address ? (
              <p className="form-field__error" id="patient-address-error">
                {fieldErrors.address}
              </p>
            ) : (
              <p className="form-field__hint">
                {values.address.length}/{LIMITS.address} characters · optional
              </p>
            )}
          </div>
        </div>
      </form>
    </Modal>
  )
}