import { statusClass, statusLabel } from '../../lib/format'

export function StatusBadge({ status }) {
  return (
    <span className={`badge ${statusClass(status)}`}>{statusLabel(status)}</span>
  )
}

export function StatCard({ label, value, hint = null, accent = 'neutral' }) {
  return (
    <article className={`stat-card stat-card--${accent}`}>
      <p className="stat-card__label">{label}</p>
      <p className="stat-card__value">{value}</p>
      {hint ? <p className="stat-card__hint">{hint}</p> : null}
    </article>
  )
}
