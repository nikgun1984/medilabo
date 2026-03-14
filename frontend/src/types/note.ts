export interface Note {
  id: string
  patId: number
  patient: string
  note: string
  createdAt: string
  updatedAt: string
}

export interface NoteRequest {
  patId: number
  patient: string
  note: string
}
