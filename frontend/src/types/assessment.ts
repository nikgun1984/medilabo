export interface Assessment {
  patId: number
  patientFullName: string
  age: number
  gender: string
  riskLevel: 'None' | 'Borderline' | 'InDanger' | 'EarlyOnset'
  triggerCount: number
  triggersFound: string[]
}
