package com.petcare.portal.dtos.documentDtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private Long id;
    private String name;
    private String url;
    private String publicId;
    private String format;
    private Long size;
    private LocalDateTime uploadedAt;
    private Long petId;
    private String petName;
}
