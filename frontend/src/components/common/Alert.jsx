import { CloseIcon } from './icons'

export function Alert({ type = 'info', children, onDismiss = null }) {
  const role = type === 'error' ? 'alert' : 'status'
  return (
    <div className={`alert alert--${type}`} role={role}>
      <span className="alert__text">{children}</span>
      {onDismiss ? (
        <button
          type="button"
          className="alert__dismiss"
          onClick={onDismiss}
          aria-label="Dismiss message"
        >
          <CloseIcon />
        </button>
      ) : null}
    </div>
  )
}
