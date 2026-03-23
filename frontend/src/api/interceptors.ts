import axios from 'axios'

/**
 * Shared axios interceptors for JWT authentication.
 * Call setupInterceptors() once at app startup.
 */
export function setupInterceptors() {
  // Request interceptor: attach JWT token
  axios.interceptors.request.use((config) => {
    const token = localStorage.getItem('medilabo_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  })

  // Response interceptor: redirect to login on 401
  axios.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        // Don't redirect if we're already on the login page or calling /auth/login
        const isLoginRequest = error.config?.url?.includes('/auth/login')
        if (!isLoginRequest) {
          localStorage.removeItem('medilabo_token')
          localStorage.removeItem('medilabo_user')
          window.location.href = '/login'
        }
      }
      return Promise.reject(error)
    },
  )
}
