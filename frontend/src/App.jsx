import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from './components/layout/AppLayout'
import { DashboardPage } from './pages/DashboardPage'
import { DepartmentsPage } from './pages/DepartmentsPage'
import { DoctorsPage } from './pages/DoctorsPage'
import { NotFoundPage, PlaceholderPage } from './pages/PlaceholderPage'

export default function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/departments" element={<DepartmentsPage />} />
        <Route path="/doctors" element={<DoctorsPage />} />
        <Route
          path="/patients"
          element={
            <PlaceholderPage
              title="Patients"
              description="Register patients, maintain their records, and search the registry."
              capabilities={[
                'Patient search by name and phone',
                'Registration form with server-side patient number generation',
                'Detail view with full record (list view stays summary-only)',
              ]}
            />
          }
        />
        <Route
          path="/appointments"
          element={
            <PlaceholderPage
              title="Appointments"
              description="Book appointments and manage the daily schedule."
              capabilities={[
                'Booking form with doctor and patient selection',
                'Schedule list with date, doctor, patient, and status filters',
                'Reschedule and status actions with conflict feedback',
              ]}
            />
          }
        />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
