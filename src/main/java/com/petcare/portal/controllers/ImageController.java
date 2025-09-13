package com.petcare.portal.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.petcare.portal.services.ImageService;
import com.petcare.portal.dtos.imageDtos.ImageResponse;
import com.petcare.portal.dtos.imageDtos.ImageUploadRequest;

@RestController
@RequestMapping("/apis/v1/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<ImageResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam("userId") Long userId) {

        try {
            // Create upload request
            ImageUploadRequest request = new ImageUploadRequest();
            request.setName(name);

            // Upload image
            ImageResponse response = imageService.uploadImage(userId, file, request);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<ImageResponse>> getUserImages(@RequestParam("userId") Long userId) {
        try {
            // Get user's images
            List<ImageResponse> images = imageService.getUserImages(userId);

            return ResponseEntity.ok(images);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<ImageResponse> getImage(@PathVariable("imageId") Long imageId) {
        try {
            ImageResponse image = imageService.getImageById(imageId);
            return ResponseEntity.ok(image);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }

        @DeleteMapping("/{imageId}")
    public ResponseEntity<String> deleteImage(@PathVariable("imageId") Long imageId, @RequestParam("userId") Long userId) {
        try {
            // Delete image
            imageService.deleteImage(userId, imageId);

            return ResponseEntity.ok("Image deleted successfully");

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to delete image");
        }
    }

    @GetMapping("/check-usage/{imageId}")
    public ResponseEntity<Boolean> checkImageUsage(@PathVariable("imageId") Long imageId, @RequestParam("userId") Long userId) {
        try {
            // Get image
            ImageResponse image = imageService.getImageById(imageId);

            // Check if image belongs to user
            if (!image.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
            }

            // Check if image is being used
            boolean isInUse = imageService.isImageInUse(image.getUrl());

            return ResponseEntity.ok(isInUse);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }

}
