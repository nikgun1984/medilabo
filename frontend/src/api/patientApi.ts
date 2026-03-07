import axios from 'axios'
import type { Patient, PatientFormValues } from '../types/patient'

const api = axios.create({ baseURL: '/api/demographics' })

export const getPatients = () => api.get<Patient[]>('/patients')
export const getPatient = (id: number | string) => api.get<Patient>(`/patients/${id}`)
export const createPatient = (data: PatientFormValues) => api.post<Patient>('/patients', data)
export const updatePatient = (id: number | string, data: PatientFormValues) =>
  api.put<Patient>(`/patients/${id}`, data)
