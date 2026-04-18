package com.lablend.backend.repository;

import com.lablend.backend.entity.WaitingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link WaitingList} persistence operations.
 * Includes custom queries to manage the queue logic (FIFO).
 */
@Repository
public interface WaitingListRepository extends JpaRepository<WaitingList, Long> {   
    List<WaitingList> findByEquipmentIdOrderByRequestDateAsc(Long equipmentId);
    Optional<WaitingList> findFirstByEquipmentIdOrderByRequestDateAsc(Long equipmentId);
    boolean existsByUserIdAndEquipmentId(Long userId, Long equipmentId);
    void deleteByUserIdAndEquipmentId(Long userId, Long equipmentId);
}