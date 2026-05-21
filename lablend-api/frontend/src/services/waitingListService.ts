import { httpClient } from './http/client'
import type { WaitingList } from '../shared/types/domain'

interface JoinQueuePayload {
  userId: number
  equipmentId: number
}

export const waitingListService = {
  join(payload: JoinQueuePayload): Promise<WaitingList> {
    return httpClient.post<WaitingList, JoinQueuePayload>('/waiting-list/join', payload)
  },

  getNextInLine(equipmentId: number): Promise<WaitingList> {
    return httpClient.get<WaitingList>(`/waiting-list/next/${equipmentId}`)
  },

  removeFromQueue(userId: number, equipmentId: number): Promise<void> {
    return httpClient.delete(`/waiting-list/remove?userId=${userId}&equipmentId=${equipmentId}`)
  },

  getQueueForEquipment(equipmentId: number): Promise<WaitingList[]> {
    return httpClient.get<WaitingList[]>(`/waiting-list/queue/${equipmentId}`)
  }
}

export type { JoinQueuePayload }