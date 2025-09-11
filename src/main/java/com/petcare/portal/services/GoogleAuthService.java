package com.petcare.portal.services;

import com.petcare.portal.entities.User;

public interface GoogleAuthService {
    User authenticateGoogleUser(String idToken);
    boolean verifyGoogleToken(String idToken);
}
