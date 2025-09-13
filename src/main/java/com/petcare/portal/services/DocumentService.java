package com.petcare.portal.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.petcare.portal.entities.Document;
import com.petcare.portal.entities.Pet;
import com.petcare.portal.repositories.DocumentRepository;
import com.petcare.portal.repositories.PetRepository;
import com.petcare.portal.dtos.documentDtos.DocumentResponse;
import com.petcare.portal.dtos.documentDtos.DocumentUploadRequest;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public DocumentResponse uploadDocument(Long petId, MultipartFile file, DocumentUploadRequest request) {
        try {
            // Find pet
            Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + petId));

            // Validate file type
            if (!isValidDocumentType(file)) {
                throw new RuntimeException("Invalid file type. Only PDF, DOC, and DOCX files are allowed.");
            }

            // Upload to Cloudinary
            Map<String, Object> uploadResult = cloudinaryService.upload(file);

            // Create Document entity
            Document document = new Document();
            document.setName(request.getName() != null ? request.getName() : file.getOriginalFilename());
            document.setUrl((String) uploadResult.get("url"));
            document.setPublicId((String) uploadResult.get("public_id"));
            document.setFormat((String) uploadResult.get("format"));
            document.setSize(((Number) uploadResult.get("bytes")).longValue());
            document.setUploadedAt(LocalDateTime.now());
            document.setPet(pet);
            document.setIsDeleted(false);

            // Save to database
            Document savedDocument = documentRepository.save(document);

            // Convert to response
            return convertToResponse(savedDocument);

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload document: " + e.getMessage(), e);
        }
    }

    public void deleteDocument(Long petId, Long documentId) {
        try {
            // Find pet
            Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + petId));

            // Find document
            Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

            // Check if document belongs to pet
            if (!document.getPet().getId().equals(petId)) {
                throw new RuntimeException("Document does not belong to pet");
            }

            // Check if document is already deleted
            if (document.getIsDeleted()) {
                throw new RuntimeException("Document is already deleted");
            }

            // Delete from Cloudinary
            cloudinaryService.delete(document.getUrl());

            // Soft delete from database
            document.setIsDeleted(true);
            document.setDeletedAt(LocalDateTime.now());
            documentRepository.save(document);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete document: " + e.getMessage(), e);
        }
    }

    public List<DocumentResponse> getPetDocuments(Long petId) {
        try {
            // Find pet
            Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + petId));

            // Get pet's active documents
            List<Document> documents = documentRepository.findByPetAndIsDeletedFalseOrderByUploadedAtDesc(pet);

            // Convert to response
            return documents.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to get pet documents: " + e.getMessage(), e);
        }
    }

    public DocumentResponse getDocumentById(Long documentId) {
        try {
            Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

            if (document.getIsDeleted()) {
                throw new RuntimeException("Document is deleted");
            }

            return convertToResponse(document);

        } catch (Exception e) {
            throw new RuntimeException("Failed to get document: " + e.getMessage(), e);
        }
    }

    public String getDocumentDownloadUrl(Long documentId) {
        try {
            Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

            if (document.getIsDeleted()) {
                throw new RuntimeException("Document is deleted");
            }

            // Use the stored URL and add attachment flag for download
            String url = document.getUrl();
            if (url.contains("?")) {
                return url + "&fl=attachment";
            } else {
                return url + "?fl=attachment";
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to get document download URL: " + e.getMessage(), e);
        }
    }

    private boolean isValidDocumentType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
            contentType.equals("application/pdf") ||
            contentType.equals("application/msword") ||
            contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
            contentType.equals("application/vnd.ms-excel") ||
            contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        );
    }

    private DocumentResponse convertToResponse(Document document) {
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setName(document.getName());
        response.setUrl(document.getUrl());
        response.setPublicId(document.getPublicId());
        response.setFormat(document.getFormat());
        response.setSize(document.getSize());
        response.setUploadedAt(document.getUploadedAt());
        response.setPetId(document.getPet().getId());
        response.setPetName(document.getPet().getPetName());
        return response;
    }
}
