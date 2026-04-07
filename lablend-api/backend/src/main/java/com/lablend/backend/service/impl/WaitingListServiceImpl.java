package com.lablend.backend.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lablend.backend.entity.WaitingList;
import com.lablend.backend.repository.WaitingListRepository;
import com.lablend.backend.service.WaitingListService;

import jakarta.transaction.Transactional;

@Service
public class WaitingListServiceImpl implements WaitingListService {

    @Autowired
    private WaitingListRepository waitingListRepository;

    @Override
    public WaitingList addToQueue(Long userId, Long equipmentId) {
        // Validar si ya está en la cola para evitar que un alumno se apunte mil veces
        if (waitingListRepository.existsByUserIdAndEquipmentId(userId, equipmentId)) {
            throw new RuntimeException("Student is already in the queue for this equipment");
        }
        return waitingListRepository.save(new WaitingList(userId, equipmentId));
    }

    @Override
    public List<WaitingList> getQueueForEquipment(Long equipmentId) {
        return waitingListRepository.findByEquipmentIdOrderByRequestDateAsc(equipmentId);
    }

    @Override
    @Transactional
    public void removeFromQueue(Long userId, Long equipmentId) {
        waitingListRepository.deleteByUserIdAndEquipmentId(userId, equipmentId);
    }

    @Override
    public WaitingList getNextInLine(Long equipmentId) {
        return waitingListRepository.findFirstByEquipmentIdOrderByRequestDateAsc(equipmentId)
                .orElse(null); 
    }
}