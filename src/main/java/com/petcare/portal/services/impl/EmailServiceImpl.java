package com.petcare.portal.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.petcare.portal.services.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

  @Autowired
  private JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Value("${app.frontend.url}")
  private String frontendUrl;

  @Override
  @Async
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

  @Override
  @Async
  public void sendOtpEmail(String toEmail, String otpCode) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromEmail);
    message.setTo(toEmail);
    message.setSubject("Green Kitchen - Password Reset OTP Code");

    String body = String.format(
        """
        Hello,

        You have requested to reset your password for your Green Kitchen account.

        Your OTP code is: %s

        This code will expire in 5 minutes.

        If you didn't request a password reset, please ignore this email or contact support if you're concerned about security.

        For security reasons, do not share this code with anyone.

        Best regards,
        Green Kitchen Team
        """,
        otpCode
    );

    message.setText(body);
    mailSender.send(message);
  }

}
