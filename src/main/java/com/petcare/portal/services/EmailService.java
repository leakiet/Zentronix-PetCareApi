package com.petcare.portal.services;

import com.petcare.portal.entities.Order;

public interface EmailService {
    
    void sendVerificationEmail(String toEmail, String verifyToken);
    void sendOtpEmail(String toEmail, String otpCode);
    void sendOrderConfirmationEmail(String toEmail, Order order);

}
