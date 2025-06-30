package com.greenkitchen.portal.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.greenkitchen.portal.entities.OtpRecords;

@Repository
public interface OtpRecordsRepository extends JpaRepository<OtpRecords, Long> {
    OtpRecords findByEmailAndOtpCode(String email, String otpCode);
    
    // Mark all previous OTPs as used for an email
    @Modifying
    @Transactional
    @Query("UPDATE OtpRecords o SET o.isUsed = true WHERE o.email = :email AND o.isUsed = false")
    void markAllOtpAsUsedByEmail(@Param("email") String email);
}
