package com.petcare.portal.controllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcare.portal.dtos.contactDtos.ContactRequest;
import com.petcare.portal.services.ContactEmailService;

@WebMvcTest(ContactController.class)
public class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContactEmailService contactEmailService;

    @Test
    public void testSendContactForm_Success() throws Exception {
        // Given
        ContactRequest contactRequest = new ContactRequest(
            "John Doe",
            "john@example.com",
            "General Inquiry",
            "I need help with my pet's health."
        );

        // When & Then
        mockMvc.perform(post("/api/contact/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        // Verify that the email service was called
        verify(contactEmailService, times(1)).sendContactEmail(contactRequest);
    }

    @Test
    public void testSendContactForm_ValidationError() throws Exception {
        // Given - missing required fields
        ContactRequest invalidRequest = new ContactRequest("", "", "", "");

        // When & Then
        mockMvc.perform(post("/api/contact/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
