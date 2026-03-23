import axios from 'axios'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  username: string
  message: string
}

const api = axios.create({ baseURL: '/auth' })

export const login = (data: LoginRequest) => api.post<LoginResponse>('/login', data)
