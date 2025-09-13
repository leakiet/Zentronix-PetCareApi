package com.petcare.portal.dtos.imageDtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {
    private Long id;
    private String name;
    private String url;
    private String publicId;
    private String format;
    private Long size;
    private LocalDateTime uploadedAt;
    private Long userId;
    private String userName;
}
