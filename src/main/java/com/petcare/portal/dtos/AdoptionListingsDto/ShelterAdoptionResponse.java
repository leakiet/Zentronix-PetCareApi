package com.petcare.portal.dtos.AdoptionListingsDto;

import com.petcare.portal.entities.Address;

import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShelterAdoptionResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String companyName;
    private String phone;
    private String email;
    private String gender;
    @Embedded
    private Address address;
}
