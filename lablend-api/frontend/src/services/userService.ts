import { httpClient } from './http/client'
import type { User, UserRole } from '../shared/types/domain'

interface SaveUserPayload {
  name: string
  email: string
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
}

export type { SaveUserPayload }
