package com.petcare.portal.services;

import com.petcare.portal.dtos.contactDtos.ContactRequest;

public interface ContactEmailService {

    void sendContactEmail(ContactRequest contactRequest);
}
