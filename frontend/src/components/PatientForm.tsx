import { useForm, SubmitHandler } from 'react-hook-form'
import type { PatientFormValues } from '../types/patient'

const FIELD =
  'border border-gray-300 rounded-md px-3 py-2 w-full text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400'
const LABEL = 'block text-sm font-medium text-gray-700 mb-1'
const ERR = 'text-red-500 text-xs mt-1'

interface Props {
  defaultValues?: Partial<PatientFormValues>
  onSubmit: SubmitHandler<PatientFormValues>
  submitting: boolean
}

export default function PatientForm({ defaultValues = {}, onSubmit, submitting }: Props) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<PatientFormValues>({ defaultValues })

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label className={LABEL}>Last Name *</label>
          <input
            className={FIELD}
            {...register('lastName', { required: 'Last name is required' })}
          />
          {errors.lastName && <p className={ERR}>{errors.lastName.message}</p>}
        </div>
        <div>
          <label className={LABEL}>First Name *</label>
          <input
            className={FIELD}
            {...register('firstName', { required: 'First name is required' })}
          />
          {errors.firstName && <p className={ERR}>{errors.firstName.message}</p>}
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label className={LABEL}>Date of Birth *</label>
          <input
            type="date"
            className={FIELD}
            {...register('dateOfBirth', { required: 'Date of birth is required' })}
          />
          {errors.dateOfBirth && <p className={ERR}>{errors.dateOfBirth.message}</p>}
        </div>
        <div>
          <label className={LABEL}>Gender *</label>
          <select className={FIELD} {...register('gender', { required: 'Gender is required' })}>
            <option value="">— select —</option>
            <option value="M">Male</option>
            <option value="F">Female</option>
          </select>
          {errors.gender && <p className={ERR}>{errors.gender.message}</p>}
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label className={LABEL}>
            Address <span className="text-gray-400 font-normal">(optional)</span>
          </label>
          <input className={FIELD} {...register('address')} />
        </div>
        <div>
          <label className={LABEL}>
            Phone <span className="text-gray-400 font-normal">(optional)</span>
          </label>
          <input className={FIELD} {...register('phone')} />
        </div>
      </div>

      <button
        type="submit"
        disabled={submitting}
        className="bg-indigo-600 text-white px-6 py-2 rounded-md text-sm font-semibold hover:bg-indigo-700 disabled:opacity-50 transition"
      >
        {submitting ? 'Saving…' : 'Save Patient'}
      </button>
    </form>
  )
}
