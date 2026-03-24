package com.lablend.backend.repository;

import com.lablend.backend.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link Equipment} persistence operations.
 */
@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
}