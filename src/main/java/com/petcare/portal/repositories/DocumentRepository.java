package com.petcare.portal.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.petcare.portal.entities.Document;
import com.petcare.portal.entities.Pet;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByPetAndIsDeletedFalseOrderByUploadedAtDesc(Pet pet);
    List<Document> findByPetIdAndIsDeletedFalseOrderByUploadedAtDesc(Long petId);
}
