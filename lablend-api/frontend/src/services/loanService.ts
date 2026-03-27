import { httpClient } from './http/client'
import type { Loan, LoanStatus } from '../shared/types/domain'

interface CreateLoanPayload {
  userId: number
  equipmentId: number
}

interface UpdateLoanPayload {
  userId: number
  equipmentId: number
  status: LoanStatus
}

export const loanService = {
  getAll(): Promise<Loan[]> {
    return httpClient.get<Loan[]>('/loans')
  },

  create(payload: CreateLoanPayload): Promise<Loan> {
    return httpClient.post<Loan, CreateLoanPayload>('/loans', payload)
  },

  update(id: number, payload: UpdateLoanPayload): Promise<Loan> {
    return httpClient.put<Loan, UpdateLoanPayload>(`/loans/${id}`, payload)
  },

  remove(id: number): Promise<void> {
    return httpClient.delete(`/loans/${id}`)
  },
}

export type { CreateLoanPayload, UpdateLoanPayload }
