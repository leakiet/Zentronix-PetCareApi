package com.greenkitchen.portal.services;

public interface EmailService {
    
    void sendVerificationEmail(String toEmail, String verifyToken);

}
