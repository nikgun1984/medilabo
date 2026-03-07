import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import PatientForm from '../components/PatientForm.jsx'
import { createPatient } from '../api/patientApi.js'

export default function PatientAdd() {
  const navigate = useNavigate()
  const [submitting,   setSubmitting]   = useState(false)
  const [serverError,  setServerError]  = useState(null)

  async function handleSubmit(data) {
    setSubmitting(true)
    setServerError(null)
    try {
      await createPatient(data)
      navigate('/patients')
    } catch (err) {
      setServerError(
        err.response?.data?.errors
          ? Object.values(err.response.data.errors).join(', ')
          : 'Failed to create patient.'
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-2xl">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Add Patient</h1>

      {serverError && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 text-sm rounded-md">
          {serverError}
        </div>
      )}

      <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-6">
        <PatientForm onSubmit={handleSubmit} submitting={submitting} />
      </div>
    </div>
  )
}
