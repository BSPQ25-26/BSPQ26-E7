import { httpClient } from './http/client'
import type { Equipment, EquipmentStatus } from '../shared/types/domain'

interface SaveEquipmentPayload {
  name: string
  type: string
  status: EquipmentStatus
}

export const equipmentService = {
  getAll(): Promise<Equipment[]> {
    return httpClient.get<Equipment[]>('/equipment')
  },

  create(payload: SaveEquipmentPayload): Promise<Equipment> {
    return httpClient.post<Equipment, SaveEquipmentPayload>('/equipment', payload)
  },

  update(id: number, payload: SaveEquipmentPayload): Promise<Equipment> {
    return httpClient.put<Equipment, SaveEquipmentPayload>(`/equipment/${id}`, payload)
  },

  reserve(id: number): Promise<Equipment> {
    return httpClient.put<Equipment, Record<string, never>>(`/equipment/${id}/reserve`, {})
  },

  remove(id: number): Promise<void> {
    return httpClient.delete(`/equipment/${id}`)
  },
}

export type { SaveEquipmentPayload }
