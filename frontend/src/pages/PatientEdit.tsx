import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import PatientForm from '../components/PatientForm'
import { getPatient, updatePatient } from '../api/patientApi'
import type { Patient, PatientFormValues } from '../types/patient'
import type { AxiosError } from 'axios'

interface ApiErrorResponse {
  errors?: Record<string, string>
}

export default function PatientEdit() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [patient, setPatient] = useState<Patient | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [submitting, setSubmitting] = useState<boolean>(false)
  const [serverError, setServerError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    getPatient(id)
      .then((res) => setPatient(res.data))
      .catch(() => setServerError('Patient not found.'))
      .finally(() => setLoading(false))
  }, [id])

  async function handleSubmit(data: PatientFormValues) {
    if (!id) return
    setSubmitting(true)
    setServerError(null)
    try {
      await updatePatient(id, data)
      navigate('/patients')
    } catch (err: unknown) {
      const axiosErr = err as AxiosError<ApiErrorResponse>
      const errs = axiosErr.response?.data?.errors
      setServerError(errs ? Object.values(errs).join(', ') : 'Failed to update patient.')
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
          <PatientForm
            defaultValues={patient as unknown as PatientFormValues}
            onSubmit={handleSubmit}
            submitting={submitting}
          />
        )}
      </div>
    </div>
  )
}
