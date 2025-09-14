package com.petcare.portal.dtos.AdoptionListingsDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionListingsAiResponse {
    private String message;
    private List<AdoptionListingsResponse> adoption;
    
}
