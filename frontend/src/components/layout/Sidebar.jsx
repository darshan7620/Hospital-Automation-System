import { NavLink } from 'react-router-dom'
import {
  AppointmentIcon,
  CloseIcon,
  DashboardIcon,
  DepartmentIcon,
  DoctorIcon,
  PatientIcon,
} from '../common/icons'

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', icon: <DashboardIcon /> },
  { to: '/departments', label: 'Departments', icon: <DepartmentIcon /> },
  { to: '/doctors', label: 'Doctors', icon: <DoctorIcon /> },
  { to: '/patients', label: 'Patients', icon: <PatientIcon /> },
  { to: '/appointments', label: 'Appointments', icon: <AppointmentIcon /> },
]

export function Sidebar({ open, onClose }) {
  return (
    <>
      <aside className={`sidebar${open ? ' sidebar--open' : ''}`}>
        <div className="sidebar__brand">
          <span className="sidebar__logo" aria-hidden="true">
            M
          </span>
          <span className="sidebar__name">MediCore</span>
          <button
            type="button"
            className="sidebar__close"
            onClick={onClose}
            aria-label="Close navigation"
          >
            <CloseIcon />
          </button>
        </div>

        <nav className="sidebar__nav" aria-label="Main navigation">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `sidebar__link${isActive ? ' sidebar__link--active' : ''}`
              }
              onClick={onClose}
            >
              <span className="sidebar__link-icon">{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <p className="sidebar__footer">Hospital Management System</p>
      </aside>
      {open ? (
        <div className="sidebar-overlay" onClick={onClose} aria-hidden="true" />
      ) : null}
    </>
  )
}
