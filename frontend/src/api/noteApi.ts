import axios from 'axios'
import type { Note, NoteRequest } from '../types/note'

const api = axios.create({ baseURL: '/api/notes' })

export const getNotesByPatient = (patId: number | string) =>
  api.get<Note[]>(`/patient/${patId}`)

export const createNote = (data: NoteRequest) => api.post<Note>('', data)

export const deleteNote = (id: string) => api.delete(`/${id}`)
