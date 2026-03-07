import axios from 'axios'

const api = axios.create({ baseURL: '/api/demographics' })

export const getPatients   = ()         => api.get('/patients')
export const getPatient    = (id)       => api.get(`/patients/${id}`)
export const createPatient = (data)     => api.post('/patients', data)
export const updatePatient = (id, data) => api.put(`/patients/${id}`, data)
