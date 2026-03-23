import axios from 'axios'
import type { Assessment } from '../types/assessment'

const api = axios.create({ baseURL: '/api/assessment' })

// Attach JWT token from localStorage on every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('medilabo_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export const getAssessment = (patId: number | string) => api.get<Assessment>(`/${patId}`)
