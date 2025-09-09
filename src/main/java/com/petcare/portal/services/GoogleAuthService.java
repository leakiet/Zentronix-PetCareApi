package com.petcare.portal.services;

import com.petcare.portal.entities.Customer;

public interface GoogleAuthService {
    Customer authenticateGoogleUser(String idToken);
    boolean verifyGoogleToken(String idToken);
}
