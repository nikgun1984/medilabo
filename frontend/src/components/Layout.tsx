import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Layout() {
  const { pathname } = useLocation()
  const { username, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <nav className="bg-indigo-700 text-white shadow-md">
        <div className="max-w-5xl mx-auto px-4 h-14 flex items-center justify-between">
          <Link to="/patients" className="text-xl font-bold tracking-wide">
            🏥 Medilabo
          </Link>
          <div className="flex items-center gap-4">
            <Link
              to="/patients/new"
              className="bg-white text-indigo-700 font-semibold text-sm px-4 py-1.5 rounded-full hover:bg-indigo-50 transition"
            >
              + Add Patient
            </Link>
            <span className="text-indigo-200 text-sm hidden sm:inline">
              {username}
            </span>
            <button
              onClick={handleLogout}
              className="text-indigo-200 hover:text-white text-sm font-medium transition"
            >
              Logout
            </button>
          </div>
        </div>
      </nav>

      <div className="max-w-5xl mx-auto w-full px-4 py-2 text-sm text-gray-500">
        {pathname === '/patients' && 'Patients'}
        {pathname === '/patients/new' && (
          <>
            <Link to="/patients" className="text-indigo-600 hover:underline">
              Patients
            </Link>{' '}
            / Add
          </>
        )}
        {pathname.includes('/view') && (
          <>
            <Link to="/patients" className="text-indigo-600 hover:underline">
              Patients
            </Link>{' '}
            / View
          </>
        )}
        {pathname.includes('/edit') && (
          <>
            <Link to="/patients" className="text-indigo-600 hover:underline">
              Patients
            </Link>{' '}
            / Edit
          </>
        )}
      </div>

      <main className="max-w-5xl mx-auto w-full px-4 pb-10 flex-1">
        <Outlet />
      </main>
    </div>
  )
}
