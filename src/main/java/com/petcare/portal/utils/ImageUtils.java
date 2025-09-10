package com.petcare.portal.utils;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.petcare.portal.services.CloudinaryService;


@Component
public class ImageUtils {
  @Autowired
  private CloudinaryService cloudinaryService;
  
  public String uploadImage(MultipartFile file) {
    try {
      Map result = cloudinaryService.upload(file);
      return (String) result.get("url");
    } catch (Exception e) {
      throw new RuntimeException("Image upload failed: " + e.getMessage());
    }
  }

  public void deleteImage(String imageUrl) {
    try {
      cloudinaryService.delete(imageUrl);
    } catch (Exception e) {
      throw new RuntimeException("Image delete failed: " + e.getMessage());
    }
  }
}
