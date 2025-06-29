package com.greenkitchen.portal.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.services.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

  @Autowired
  private JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Value("${app.frontend.url}")
  private String frontendUrl;

  @Override
  public void sendVerificationEmail(String toEmail, String verifyToken) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromEmail);
    message.setTo(toEmail);
    message.setSubject("Green Kitchen - Email Verification");

    String verifyUrl = frontendUrl + "/verify-email?email=" + toEmail + "&token=" + verifyToken;
    String body = String.format(
        """
        Hello,

        Thank you for registering with Green Kitchen!

        Please click the link below to verify your email address:
        %s

        This verification link will expire in 24 hours.

        If you didn't create an account, please ignore this email.

        Best regards,
        Green Kitchen Team
        """,
        
        verifyUrl
    );

    message.setText(body);
    mailSender.send(message);
  }

}
