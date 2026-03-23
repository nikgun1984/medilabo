import axios from 'axios'
import type { Patient, PatientFormValues } from '../types/patient'

const api = axios.create({ baseURL: '/api/demographics' })

// Attach JWT token from localStorage on every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('medilabo_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export const getPatients = () => api.get<Patient[]>('/patients')
export const getPatient = (id: number | string) => api.get<Patient>(`/patients/${id}`)
export const createPatient = (data: PatientFormValues) => api.post<Patient>('/patients', data)
export const updatePatient = (id: number | string, data: PatientFormValues) =>
  api.put<Patient>(`/patients/${id}`, data)
