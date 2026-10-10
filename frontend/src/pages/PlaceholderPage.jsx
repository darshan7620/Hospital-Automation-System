import { Link } from 'react-router-dom'
import './pages.css'

export function PlaceholderPage({ title, description, capabilities = [] }) {
  return (
    <div className="placeholder-page">
      <section className="card placeholder-page__card">
        <span className="placeholder-page__tag">Coming in Phase 7</span>
        <h2>{title}</h2>
        <p className="placeholder-page__description">{description}</p>
        {capabilities.length > 0 ? (
          <>
            <h3>Planned capabilities</h3>
            <ul className="placeholder-page__list">
              {capabilities.map((capability) => (
                <li key={capability}>{capability}</li>
              ))}
            </ul>
          </>
        ) : null}
        <p className="placeholder-page__note">
          The backend API for this module already exists and is documented in the
          project README.
        </p>
        <Link className="btn btn--primary" to="/dashboard">
          Back to dashboard
        </Link>
      </section>
    </div>
  )
}

export function NotFoundPage() {
  return (
    <div className="placeholder-page">
      <section className="card placeholder-page__card">
        <span className="placeholder-page__tag">404</span>
        <h2>Page not found</h2>
        <p className="placeholder-page__description">
          The page you requested does not exist.
        </p>
        <Link className="btn btn--primary" to="/dashboard">
          Back to dashboard
        </Link>
      </section>
    </div>
  )
}
