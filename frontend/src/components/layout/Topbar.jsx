import { useLocation } from 'react-router-dom'
import { MenuIcon } from '../common/icons'

const TITLES = [
  ['/departments', 'Departments'],
  ['/appointments', 'Appointments'],
  ['/dashboard', 'Dashboard'],
  ['/doctors', 'Doctors'],
  ['/patients', 'Patients'],
]

function titleForPath(pathname) {
  const match = TITLES.find(([prefix]) => pathname.startsWith(prefix))
  return match ? match[1] : 'MediCore'
}

export function Topbar({ onMenuClick }) {
  const { pathname } = useLocation()
  const title = titleForPath(pathname)

  return (
    <header className="topbar">
      <button
        type="button"
        className="topbar__menu"
        onClick={onMenuClick}
        aria-label="Open navigation"
      >
        <MenuIcon />
      </button>
      <h1 className="topbar__title">{title}</h1>
      <span className="topbar__meta">UTC time zone · demo data only</span>
    </header>
  )
}
