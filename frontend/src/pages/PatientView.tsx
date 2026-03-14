import { useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getPatient } from '../api/patientApi'
import { createNote, getNotesByPatient } from '../api/noteApi'
import type { Patient } from '../types/patient'
import type { Note } from '../types/note'

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

function formatDateTime(iso: string) {
  return new Date(iso).toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export default function PatientView() {
  const { id } = useParams<{ id: string }>()

  const [patient, setPatient] = useState<Patient | null>(null)
  const [notes, setNotes] = useState<Note[]>([])
  const [loadingPatient, setLoadingPatient] = useState(true)
  const [loadingNotes, setLoadingNotes] = useState(true)
  const [patientError, setPatientError] = useState<string | null>(null)

  const [noteText, setNoteText] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)
  const textareaRef = useRef<HTMLTextAreaElement>(null)

  // Load patient info
  useEffect(() => {
    if (!id) return
    getPatient(id)
      .then((res) => setPatient(res.data))
      .catch(() => setPatientError('Failed to load patient.'))
      .finally(() => setLoadingPatient(false))
  }, [id])

  // Load notes
  useEffect(() => {
    if (!id) return
    getNotesByPatient(id)
      .then((res) => setNotes(res.data))
      .catch(() => setNotes([]))
      .finally(() => setLoadingNotes(false))
  }, [id])

  // Auto-resize textarea as user types
  const handleNoteChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setNoteText(e.target.value)
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto'
      textareaRef.current.style.height = `${textareaRef.current.scrollHeight}px`
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!noteText.trim() || !patient) return
    setSubmitting(true)
    setSubmitError(null)
    try {
      const res = await createNote({
        patId: patient.id,
        patient: patient.lastName,
        note: noteText,
      })
      setNotes((prev) => [res.data, ...prev])
      setNoteText('')
      if (textareaRef.current) textareaRef.current.style.height = 'auto'
    } catch {
      setSubmitError('Failed to save note. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  if (loadingPatient) {
    return <p className="text-gray-500 mt-6 animate-pulse">Loading patient…</p>
  }

  if (patientError || !patient) {
    return <p className="text-red-500 mt-6">{patientError ?? 'Patient not found.'}</p>
  }

  return (
    <div className="space-y-8 mt-2">

      {/* ── Patient Info Card ──────────────────────────────────────────── */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
        {/* Card header */}
        <div className="bg-indigo-600 px-6 py-4 flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold text-white">
              {patient.firstName} {patient.lastName}
            </h1>
            <p className="text-indigo-200 text-sm mt-0.5">Patient #{patient.id}</p>
          </div>
          <span
            className={`px-3 py-1 rounded-full text-sm font-semibold ${
              patient.gender === 'M'
                ? 'bg-blue-100 text-blue-800'
                : 'bg-pink-100 text-pink-800'
            }`}
          >
            {patient.gender === 'M' ? '♂ Male' : '♀ Female'}
          </span>
        </div>

        {/* Card body */}
        <div className="px-6 py-5 grid grid-cols-2 sm:grid-cols-4 gap-4">
          <InfoField label="Date of Birth" value={formatDate(patient.dateOfBirth)} />
          <InfoField label="Phone" value={patient.phone ?? '—'} />
          <InfoField label="Address" value={patient.address ?? '—'} className="col-span-2" />
        </div>

        {/* Card footer */}
        <div className="px-6 py-3 bg-gray-50 border-t border-gray-100 flex gap-3">
          <Link
            to={`/patients/${patient.id}/edit`}
            className="text-sm font-medium text-indigo-600 hover:text-indigo-800 hover:underline"
          >
            ✏️ Edit patient
          </Link>
          <span className="text-gray-300">|</span>
          <Link
            to="/patients"
            className="text-sm font-medium text-gray-500 hover:text-gray-700 hover:underline"
          >
            ← Back to list
          </Link>
        </div>
      </div>

      {/* ── Add Note ──────────────────────────────────────────────────── */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-3">Add Observation Note</h2>
        <form onSubmit={handleSubmit} className="space-y-3">
          <textarea
            ref={textareaRef}
            value={noteText}
            onChange={handleNoteChange}
            rows={4}
            placeholder="Record your observations here…&#10;&#10;Original formatting (line breaks, indentation) will be preserved exactly as entered."
            className="w-full resize-none rounded-lg border border-gray-300 px-4 py-3 text-sm text-gray-800
                       placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-indigo-400
                       focus:border-transparent font-mono leading-relaxed transition overflow-hidden"
            style={{ minHeight: '120px' }}
          />
          {submitError && <p className="text-red-500 text-sm">{submitError}</p>}
          <div className="flex items-center justify-between">
            <p className="text-xs text-gray-400">
              {noteText.length > 0 ? `${noteText.length} characters` : 'No character limit'}
            </p>
            <button
              type="submit"
              disabled={submitting || !noteText.trim()}
              className="bg-indigo-600 text-white text-sm font-semibold px-5 py-2 rounded-lg
                         hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
            >
              {submitting ? 'Saving…' : 'Save Note'}
            </button>
          </div>
        </form>
      </div>

      {/* ── Notes History ─────────────────────────────────────────────── */}
      <div>
        <h2 className="text-lg font-semibold text-gray-800 mb-4">
          Medical History
          {!loadingNotes && (
            <span className="ml-2 text-sm font-normal text-gray-400">
              {notes.length} note{notes.length !== 1 ? 's' : ''}
            </span>
          )}
        </h2>

        {loadingNotes ? (
          <div className="space-y-3">
            {[1, 2].map((i) => (
              <div key={i} className="bg-white rounded-xl border border-gray-200 p-5 animate-pulse">
                <div className="h-3 bg-gray-200 rounded w-32 mb-3" />
                <div className="h-3 bg-gray-100 rounded w-full mb-2" />
                <div className="h-3 bg-gray-100 rounded w-4/5" />
              </div>
            ))}
          </div>
        ) : notes.length === 0 ? (
          <div className="bg-white rounded-xl border border-dashed border-gray-300 p-10 text-center text-gray-400">
            <p className="text-4xl mb-2">📋</p>
            <p className="font-medium">No notes yet</p>
            <p className="text-sm mt-1">Add the first observation note above.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {notes.map((n, idx) => (
              <div
                key={n.id}
                className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden"
              >
                {/* Note header */}
                <div className="flex items-center justify-between px-5 py-3 bg-gray-50 border-b border-gray-100">
                  <div className="flex items-center gap-2">
                    <span className="w-6 h-6 rounded-full bg-indigo-100 text-indigo-700 text-xs font-bold flex items-center justify-center">
                      {notes.length - idx}
                    </span>
                    <span className="text-xs font-medium text-gray-500 uppercase tracking-wide">
                      Visit note
                    </span>
                  </div>
                  <div className="text-right">
                    <p className="text-xs text-gray-500">{formatDateTime(n.createdAt)}</p>
                    {n.updatedAt !== n.createdAt && (
                      <p className="text-xs text-gray-400 italic">
                        edited {formatDateTime(n.updatedAt)}
                      </p>
                    )}
                  </div>
                </div>

                {/* Note body — whitespace-pre-wrap preserves original formatting */}
                <div className="px-5 py-4">
                  <p className="text-sm text-gray-800 whitespace-pre-wrap font-mono leading-relaxed">
                    {n.note}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

// ── Small helper component ──────────────────────────────────────────────────
function InfoField({
  label,
  value,
  className = '',
}: {
  label: string
  value: string
  className?: string
}) {
  return (
    <div className={className}>
      <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-0.5">{label}</p>
      <p className="text-sm text-gray-800 font-medium">{value}</p>
    </div>
  )
}
