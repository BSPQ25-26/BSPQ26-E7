export type EquipmentStatus = 'AVAILABLE' | 'RESERVED' | 'UNDER_MAINTENANCE'

export type LoanStatus = 'ACTIVE' | 'COMPLETED' | 'CANCELLED'

export type UserRole = 'ADMIN' | 'USER'

export interface Equipment {
  id: number
  name: string
  type: string
  status: EquipmentStatus
  version?: number
}

export interface Loan {
  id: number
  userId: number
  equipmentId: number
  loanDate: string
  status: LoanStatus
}

export interface User {
  id: number
  name: string
  email: string
  role: UserRole
}

export interface ApiError {
  message: string
  status: number
}
