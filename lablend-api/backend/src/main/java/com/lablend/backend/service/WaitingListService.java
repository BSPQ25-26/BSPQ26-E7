package com.lablend.backend.service;

import com.lablend.backend.entity.WaitingList;
import java.util.List;

public interface WaitingListService {
    WaitingList addToQueue(Long userId, Long equipmentId);
    WaitingList getNextInLine(Long equipmentId);
    void removeFromQueue(Long userId, Long equipmentId);
    List<WaitingList> getQueueForEquipment(Long equipmentId);
}