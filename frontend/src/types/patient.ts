export interface Patient {
  id: number
  lastName: string
  firstName: string
  dateOfBirth: string // ISO date string: 'YYYY-MM-DD'
  gender: 'M' | 'F'
  address?: string
  phone?: string
  createdAt?: string
}

export interface PatientFormValues {
  lastName: string
  firstName: string
  dateOfBirth: string
  gender: 'M' | 'F' | ''
  address?: string
  phone?: string
}
