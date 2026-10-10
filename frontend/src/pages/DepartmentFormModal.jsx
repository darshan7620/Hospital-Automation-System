import { useState } from 'react'
import { Modal } from '../components/common/Modal'

const LIMITS = { name: 100, description: 255, location: 150 }

function validate(values) {
  const errors = {}
  const name = values.name.trim()
  if (!name) errors.name = 'Name is required.'
  else if (name.length > LIMITS.name)
    errors.name = `Name must be at most ${LIMITS.name} characters.`
  if (values.description.length > LIMITS.description)
    errors.description = `Description must be at most ${LIMITS.description} characters.`
  if (values.location.length > LIMITS.location)
    errors.location = `Location must be at most ${LIMITS.location} characters.`
  return errors
}

export function DepartmentFormModal({ department, onSubmit, onClose }) {
  const isEdit = Boolean(department)
  const [values, setValues] = useState({
    name: department?.name ?? '',
    description: department?.description ?? '',
    location: department?.location ?? '',
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
        name: values.name.trim(),
        description: values.description.trim(),
        location: values.location.trim(),
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
      title={isEdit ? 'Edit department' : 'New department'}
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
            form="department-form"
            disabled={submitting}
          >
            {submitting ? 'Saving…' : isEdit ? 'Save changes' : 'Create department'}
          </button>
        </>
      }
    >
      <form id="department-form" onSubmit={handleSubmit} noValidate>
        <div className="form-stack">
          {formError ? (
            <div className="alert alert--error" role="alert">
              {formError}
            </div>
          ) : null}

          <div className="form-field">
            <label className="form-field__label" htmlFor="department-name">
              Name <span className="form-field__required">*</span>
            </label>
            <input
              id="department-name"
              className={`form-input${fieldErrors.name ? ' form-input--invalid' : ''}`}
              type="text"
              maxLength={LIMITS.name}
              value={values.name}
              onChange={(event) => update('name', event.target.value)}
              aria-invalid={Boolean(fieldErrors.name)}
              aria-describedby={fieldErrors.name ? 'department-name-error' : undefined}
            />
            {fieldErrors.name ? (
              <p className="form-field__error" id="department-name-error">
                {fieldErrors.name}
              </p>
            ) : (
              <p className="form-field__hint">
                {values.name.length}/{LIMITS.name} characters · must be unique
              </p>
            )}
          </div>

          <div className="form-field">
            <label className="form-field__label" htmlFor="department-description">
              Description
            </label>
            <textarea
              id="department-description"
              className={`form-input${fieldErrors.description ? ' form-input--invalid' : ''}`}
              maxLength={LIMITS.description}
              value={values.description}
              onChange={(event) => update('description', event.target.value)}
              aria-invalid={Boolean(fieldErrors.description)}
              aria-describedby={
                fieldErrors.description ? 'department-description-error' : undefined
              }
            />
            {fieldErrors.description ? (
              <p className="form-field__error" id="department-description-error">
                {fieldErrors.description}
              </p>
            ) : (
              <p className="form-field__hint">
                {values.description.length}/{LIMITS.description} characters
              </p>
            )}
          </div>

          <div className="form-field">
            <label className="form-field__label" htmlFor="department-location">
              Location
            </label>
            <input
              id="department-location"
              className={`form-input${fieldErrors.location ? ' form-input--invalid' : ''}`}
              type="text"
              maxLength={LIMITS.location}
              value={values.location}
              onChange={(event) => update('location', event.target.value)}
              aria-invalid={Boolean(fieldErrors.location)}
              aria-describedby={
                fieldErrors.location ? 'department-location-error' : undefined
              }
            />
            {fieldErrors.location ? (
              <p className="form-field__error" id="department-location-error">
                {fieldErrors.location}
              </p>
            ) : (
              <p className="form-field__hint">
                {values.location.length}/{LIMITS.location} characters
              </p>
            )}
          </div>
        </div>
      </form>
    </Modal>
  )
}
