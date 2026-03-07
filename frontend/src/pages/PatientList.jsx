import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getPatients } from '../api/patientApi.js'

export default function PatientList() {
  const [patients, setPatients] = useState([])
  const [loading,  setLoading]  = useState(true)
  const [error,    setError]    = useState(null)

  useEffect(() => {
    getPatients()
      .then(res => setPatients(res.data))
      .catch(() => setError('Failed to load patients. Is the backend running?'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p className="text-gray-500 mt-6">Loading patients…</p>
  if (error)   return <p className="text-red-500 mt-6">{error}</p>

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-2xl font-bold text-gray-800">Patients</h1>
        <span className="text-sm text-gray-500">
          {patients.length} record{patients.length !== 1 ? 's' : ''}
        </span>
      </div>

      {patients.length === 0 ? (
        <div className="text-center py-16 text-gray-400">
          <p className="text-lg">No patients yet.</p>
          <Link to="/patients/new" className="text-indigo-600 hover:underline text-sm mt-2 inline-block">
            Add the first patient →
          </Link>
        </div>
      ) : (
        <div className="overflow-x-auto rounded-lg border border-gray-200 shadow-sm">
          <table className="w-full text-sm text-left">
            <thead className="bg-indigo-50 text-indigo-800 uppercase text-xs tracking-wide">
              <tr>
                {['Last Name', 'First Name', 'Date of Birth', 'Gender', 'Address', 'Phone', ''].map(h => (
                  <th key={h} className="px-4 py-3 font-semibold">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {patients.map(p => (
                <tr key={p.id} className="hover:bg-gray-50 transition">
                  <td className="px-4 py-3 font-medium text-gray-900">{p.lastName}</td>
                  <td className="px-4 py-3 text-gray-700">{p.firstName}</td>
                  <td className="px-4 py-3 text-gray-700">{p.dateOfBirth}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-semibold ${
                      p.gender === 'M' ? 'bg-blue-100 text-blue-700' : 'bg-pink-100 text-pink-700'
                    }`}>
                      {p.gender === 'M' ? 'Male' : 'Female'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-gray-600">{p.address || '—'}</td>
                  <td className="px-4 py-3 text-gray-600">{p.phone || '—'}</td>
                  <td className="px-4 py-3">
                    <Link
                      to={`/patients/${p.id}/edit`}
                      className="text-indigo-600 hover:underline font-medium"
                    >
                      Edit
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
