package com.petcare.portal.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.petcare.portal.dtos.contactDtos.ContactRequest;
import com.petcare.portal.services.ContactEmailService;

@Service
public class ContactEmailServiceImpl implements ContactEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    @Async
    public void sendContactEmail(ContactRequest contactRequest) {
        // Send email to admin/support team
        sendEmailToAdmin(contactRequest);

        // Optional: Send confirmation email to user
        sendConfirmationEmailToUser(contactRequest);
    }

    private void sendEmailToAdmin(ContactRequest contactRequest) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(fromEmail); // Send to admin/support email
        message.setSubject("Fur Shield Contact Form: " + contactRequest.getSubject());

        String body = String.format(
            """
            New contact form submission from Fur Shield website:

            From: %s (%s)
            Subject: %s

            Message:
            %s

            ---
            This email was sent from the Fur Shield contact form.
            """,
            contactRequest.getName(),
            contactRequest.getEmail(),
            contactRequest.getSubject(),
            contactRequest.getMessage()
        );

        message.setText(body);
        mailSender.send(message);
    }

    private void sendConfirmationEmailToUser(ContactRequest contactRequest) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(contactRequest.getEmail());
        message.setSubject("Thank you for contacting Fur Shield");

        String body = String.format(
            """
            Dear %s,

            Thank you for contacting Fur Shield!

            We have received your message and will get back to you within 24 hours.

            Your message details:
            Subject: %s
            Message: %s

            If you have any urgent concerns, please call our emergency hotline at +84 123 456 789.

            Best regards,
            Fur Shield Team
            %s
            """,
            contactRequest.getName(),
            contactRequest.getSubject(),
            contactRequest.getMessage(),
            frontendUrl
        );

        message.setText(body);
        mailSender.send(message);
    }
}
