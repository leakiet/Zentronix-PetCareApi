package com.petcare.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.petcare.portal.entities.HealthRecord;
import java.util.List;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    
    @Query("SELECT hr FROM HealthRecord hr WHERE hr.pet.id = :petId AND hr.isDeleted = false ORDER BY hr.visitDate DESC")
    List<HealthRecord> findByPetIdAndNotDeleted(@Param("petId") Long petId);
    
    @Query("SELECT hr FROM HealthRecord hr WHERE hr.isDeleted = false ORDER BY hr.visitDate DESC")
    List<HealthRecord> findAllNotDeleted();
    
    @Query("SELECT hr FROM HealthRecord hr WHERE hr.id = :id AND hr.isDeleted = false")
    HealthRecord findByIdAndNotDeleted(@Param("id") Long id);
}
