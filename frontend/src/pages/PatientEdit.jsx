import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import PatientForm from '../components/PatientForm.jsx'
import { getPatient, updatePatient } from '../api/patientApi.js'

export default function PatientEdit() {
  const { id }   = useParams()
  const navigate = useNavigate()
  const [patient,     setPatient]     = useState(null)
  const [loading,     setLoading]     = useState(true)
  const [submitting,  setSubmitting]  = useState(false)
  const [serverError, setServerError] = useState(null)

  useEffect(() => {
    getPatient(id)
      .then(res => setPatient(res.data))
      .catch(() => setServerError('Patient not found.'))
      .finally(() => setLoading(false))
  }, [id])

  async function handleSubmit(data) {
    setSubmitting(true)
    setServerError(null)
    try {
      await updatePatient(id, data)
      navigate('/patients')
    } catch (err) {
      setServerError(
        err.response?.data?.errors
          ? Object.values(err.response.data.errors).join(', ')
          : 'Failed to update patient.'
      )
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <p className="text-gray-500 mt-6">Loading…</p>

  return (
    <div className="max-w-2xl">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Edit Patient</h1>

      {serverError && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 text-sm rounded-md">
          {serverError}
        </div>
      )}

      <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-6">
        {patient && (
          <PatientForm defaultValues={patient} onSubmit={handleSubmit} submitting={submitting} />
        )}
      </div>
    </div>
  )
}
