package com.petcare.portal.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.petcare.portal.entities.Image;
import com.petcare.portal.entities.User;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByUserAndIsDeletedFalse(User user);

    List<Image> findByUserAndIsDeletedFalseOrderByUploadedAtDesc(User user);

    @Query("SELECT i FROM Image i WHERE i.user = :user AND i.isDeleted = false ORDER BY i.uploadedAt DESC")
    List<Image> findActiveImagesByUser(@Param("user") User user);

    boolean existsByPublicIdAndIsDeletedFalse(String publicId);
}
