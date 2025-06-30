package com.greenkitchen.portal.services;

import com.greenkitchen.portal.entities.Customer;

public interface GoogleAuthService {
    Customer authenticateGoogleUser(String idToken);
    boolean verifyGoogleToken(String idToken);
}
