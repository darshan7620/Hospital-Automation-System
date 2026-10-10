export function Spinner({ size = 22 }) {
  return (
    <span
      className="spinner"
      style={{ width: size, height: size }}
      role="status"
      aria-label="Loading"
    />
  )
}

export function LoadingState({ label = 'Loading…' }) {
  return (
    <div className="state-block" role="status" aria-live="polite">
      <Spinner />
      <p className="state-block__text">{label}</p>
    </div>
  )
}

export function EmptyState({ title = 'Nothing here yet', message, action = null }) {
  return (
    <div className="state-block">
      <svg
        className="state-block__icon"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.6"
        aria-hidden="true"
      >
        <rect x="3.5" y="4.5" width="17" height="16" rx="2.5" />
        <path d="M3.5 9.5h17M8 3v3M16 3v3" />
        <path d="M9 14.5l2 2 4-4" />
      </svg>
      <p className="state-block__title">{title}</p>
      {message ? <p className="state-block__text">{message}</p> : null}
      {action}
    </div>
  )
}

export function ErrorState({ error, onRetry = null, title = 'Something went wrong' }) {
  const status = error?.status
  return (
    <div className="state-block state-block--error" role="alert">
      <svg
        className="state-block__icon"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.6"
        aria-hidden="true"
      >
        <circle cx="12" cy="12" r="9" />
        <path d="M12 7.5v5.5M12 16.5v.01" />
      </svg>
      <p className="state-block__title">
        {title}
        {status ? <span className="state-block__status">HTTP {status}</span> : null}
      </p>
      <p className="state-block__text">{error?.message || 'Unexpected error.'}</p>
      {onRetry ? (
        <button type="button" className="btn btn--secondary" onClick={onRetry}>
          Retry
        </button>
      ) : null}
    </div>
  )
}
