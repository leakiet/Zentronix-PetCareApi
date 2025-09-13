package com.petcare.portal.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.petcare.portal.dtos.documentDtos.DocumentResponse;
import com.petcare.portal.dtos.documentDtos.DocumentUploadRequest;
import com.petcare.portal.services.DocumentService;

@RestController
@RequestMapping("/apis/v1/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload/{petId}")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @PathVariable Long petId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name) {

        try {
            DocumentUploadRequest request = new DocumentUploadRequest();
            request.setName(name);

            DocumentResponse document = documentService.uploadDocument(petId, file, request);
            return ResponseEntity.ok(document);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{petId}/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long petId,
            @PathVariable Long documentId) {

        try {
            documentService.deleteDocument(petId, documentId);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<DocumentResponse>> getPetDocuments(@PathVariable Long petId) {
        try {
            List<DocumentResponse> documents = documentService.getPetDocuments(petId);
            return ResponseEntity.ok(documents);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> getDocumentById(@PathVariable Long documentId) {
        try {
            DocumentResponse document = documentService.getDocumentById(documentId);
            return ResponseEntity.ok(document);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/download/{documentId}")
    public ResponseEntity<String> downloadDocument(@PathVariable Long documentId) {
        try {
            String downloadUrl = documentService.getDocumentDownloadUrl(documentId);
            return ResponseEntity.ok(downloadUrl);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
