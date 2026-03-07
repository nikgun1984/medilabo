import { Outlet, Link, useLocation } from 'react-router-dom'

export default function Layout() {
  const { pathname } = useLocation()

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <nav className="bg-indigo-700 text-white shadow-md">
        <div className="max-w-5xl mx-auto px-4 h-14 flex items-center justify-between">
          <Link to="/patients" className="text-xl font-bold tracking-wide">
            🏥 Medilabo
          </Link>
          <Link
            to="/patients/new"
            className="bg-white text-indigo-700 font-semibold text-sm px-4 py-1.5 rounded-full hover:bg-indigo-50 transition"
          >
            + Add Patient
          </Link>
        </div>
      </nav>

      <div className="max-w-5xl mx-auto w-full px-4 py-2 text-sm text-gray-500">
        {pathname === '/patients' && 'Patients'}
        {pathname === '/patients/new' && (
          <><Link to="/patients" className="text-indigo-600 hover:underline">Patients</Link> / Add</>
        )}
        {pathname.includes('/edit') && (
          <><Link to="/patients" className="text-indigo-600 hover:underline">Patients</Link> / Edit</>
        )}
      </div>

      <main className="max-w-5xl mx-auto w-full px-4 pb-10 flex-1">
        <Outlet />
      </main>
    </div>
  )
}
