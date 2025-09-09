package com.petcare.portal.services;

public interface EmailService {
    
    void sendVerificationEmail(String toEmail, String verifyToken);
    void sendOtpEmail(String toEmail, String otpCode);

}
