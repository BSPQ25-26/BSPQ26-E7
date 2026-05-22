import { httpClient } from './http/client'
import type { User, UserRole } from '../shared/types/domain'

interface SaveUserPayload {
  name: string
  email: string
  password?: string
  role: UserRole
}

export const userService = {
  getAll(): Promise<User[]> {
    return httpClient.get<User[]>('/users')
  },

  create(payload: SaveUserPayload): Promise<User> {
    return httpClient.post<User, SaveUserPayload>('/users', payload)
  },

  update(id: number, payload: SaveUserPayload): Promise<User> {
    return httpClient.put<User, SaveUserPayload>(`/users/${id}`, payload)
  },

  remove(id: number): Promise<void> {
    return httpClient.delete(`/users/${id}`)
  },

  getFlagged(): Promise<User[]> {
    return httpClient.get<User[]>('/users/dashboard/flagged')
  },

  unblock(id: number): Promise<void> {
    return httpClient.put<void, Record<string, never>>(`/users/${id}/unblock`, {})
  },
  
  block(id: number): Promise<void> {
    return httpClient.put<void, Record<string, never>>(`/users/${id}/block`, {})
  },
}

export type { SaveUserPayload }
