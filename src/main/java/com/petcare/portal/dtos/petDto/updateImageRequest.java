package com.petcare.portal.dtos.petDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class updateImageRequest {

    @NotBlank(message = "Image URL is required")
    private String image;

}
