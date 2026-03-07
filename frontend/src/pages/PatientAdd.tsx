import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import PatientForm from '../components/PatientForm'
import { createPatient } from '../api/patientApi'
import type { PatientFormValues } from '../types/patient'
import type { AxiosError } from 'axios'

interface ApiErrorResponse {
  errors?: Record<string, string>
}

export default function PatientAdd() {
  const navigate = useNavigate()
  const [submitting, setSubmitting] = useState<boolean>(false)
  const [serverError, setServerError] = useState<string | null>(null)

  async function handleSubmit(data: PatientFormValues) {
    setSubmitting(true)
    setServerError(null)
    try {
      await createPatient(data)
      navigate('/patients')
    } catch (err: unknown) {
      const axiosErr = err as AxiosError<ApiErrorResponse>
      const errs = axiosErr.response?.data?.errors
      setServerError(errs ? Object.values(errs).join(', ') : 'Failed to create patient.')
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
