package com.petcare.portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.petcare.portal.dtos.contactDtos.ContactRequest;
import com.petcare.portal.services.ContactEmailService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/apis/v1/contact")
@CrossOrigin(origins = "${app.frontend.url:http://localhost:3000}")
public class ContactController {

    @Autowired
    private ContactEmailService contactEmailService;

    @PostMapping("/send")
    public ResponseEntity<?> sendContactForm(@Valid @RequestBody ContactRequest contactRequest) {
        try {
            // Send contact email asynchronously
            contactEmailService.sendContactEmail(contactRequest);

            return ResponseEntity.ok().body(new ContactResponse(
                "success",
                "Your message has been sent successfully! We'll get back to you within 24 hours.",
                contactRequest.getName()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ContactResponse(
                "error",
                "Sorry, there was an error sending your message. Please try again or contact us directly.",
                contactRequest.getName()
            ));
        }
    }

    // Response DTO class
    public static class ContactResponse {
        private String status;
        private String message;
        private String name;

        public ContactResponse(String status, String message, String name) {
            this.status = status;
            this.message = message;
            this.name = name;
        }

        // Getters
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public String getName() { return name; }
    }
}
