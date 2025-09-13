package com.petcare.portal.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.petcare.portal.entities.Image;
import com.petcare.portal.entities.User;
import com.petcare.portal.repositories.ImageRepository;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.repositories.PetRepository;
import com.petcare.portal.dtos.imageDtos.ImageResponse;
import com.petcare.portal.dtos.imageDtos.ImageUploadRequest;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public ImageResponse uploadImage(Long userId, MultipartFile file, ImageUploadRequest request) {
        try {
            // Find user
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            // Upload to Cloudinary
            Map<String, Object> uploadResult = cloudinaryService.upload(file);

            // Create Image entity
            Image image = new Image();
            image.setName(request.getName() != null ? request.getName() : file.getOriginalFilename());
            image.setUrl((String) uploadResult.get("url"));
            image.setPublicId((String) uploadResult.get("public_id"));
            image.setFormat((String) uploadResult.get("format"));
            image.setSize(((Number) uploadResult.get("bytes")).longValue());
            image.setUploadedAt(LocalDateTime.now());
            image.setUser(user);
            image.setIsDeleted(false);

            // Save to database
            Image savedImage = imageRepository.save(image);

            // Convert to response
            return convertToResponse(savedImage);

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    public void deleteImage(Long userId, Long imageId) {
        try {
            // Find user
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            // Find image
            Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));

            // Check if image belongs to user
            if (!image.getUser().getId().equals(userId)) {
                throw new RuntimeException("Image does not belong to user");
            }

            // Check if image is already deleted
            if (image.getIsDeleted()) {
                throw new RuntimeException("Image is already deleted");
            }

            // Check if image is being used by any pet
            if (isImageInUse(image.getUrl())) {
                throw new RuntimeException("Image is currently being used by a pet and cannot be deleted");
            }

            // Delete from Cloudinary
            cloudinaryService.delete(image.getUrl());

            // Soft delete from database
            image.setIsDeleted(true);
            image.setDeletedAt(LocalDateTime.now());
            imageRepository.save(image);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image: " + e.getMessage(), e);
        }
    }

    public boolean isImageInUse(String imageUrl) {
        return petRepository.existsByImageAndIsDeletedFalse(imageUrl);
    }

    public List<ImageResponse> getUserImages(Long userId) {
        try {
            // Find user
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            // Get user's active images
            List<Image> images = imageRepository.findByUserAndIsDeletedFalseOrderByUploadedAtDesc(user);

            // Convert to response
            return images.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to get user images: " + e.getMessage(), e);
        }
    }

    public ImageResponse getImageById(Long imageId) {
        try {
            Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));

            if (image.getIsDeleted()) {
                throw new RuntimeException("Image is deleted");
            }

            return convertToResponse(image);

        } catch (Exception e) {
            throw new RuntimeException("Failed to get image: " + e.getMessage(), e);
        }
    }

    private ImageResponse convertToResponse(Image image) {
        ImageResponse response = new ImageResponse();
        response.setId(image.getId());
        response.setName(image.getName());
        response.setUrl(image.getUrl());
        response.setPublicId(image.getPublicId());
        response.setFormat(image.getFormat());
        response.setSize(image.getSize());
        response.setUploadedAt(image.getUploadedAt());
        response.setUserId(image.getUser().getId());
        response.setUserName(image.getUser().getFirstName() + " " + image.getUser().getLastName());
        return response;
    }
}
