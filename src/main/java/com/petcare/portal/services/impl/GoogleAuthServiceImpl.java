package com.petcare.portal.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcare.portal.entities.Customer;
import com.petcare.portal.repositories.CustomerRepository;
import com.petcare.portal.services.GoogleAuthService;

@Service
public class GoogleAuthServiceImpl implements GoogleAuthService {

    @Value("${google.client.id:}")
    private String googleClientId;

    @Autowired
    private CustomerRepository customerRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Customer authenticateGoogleUser(String idToken) {
        try {
            // Verify token với Google
            JsonNode tokenInfo = getGoogleTokenInfo(idToken);
            if (tokenInfo == null || !tokenInfo.has("email")) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }

            String email = tokenInfo.get("email").asText();
            String firstName = tokenInfo.has("given_name") ? tokenInfo.get("given_name").asText() : "";
            String lastName = tokenInfo.has("family_name") ? tokenInfo.get("family_name").asText() : "";
            String googleId = tokenInfo.get("sub").asText();

            // Tìm user existing hoặc tạo mới
            Customer customer = customerRepository.findByEmail(email);
            
            if (customer == null) {
                // Tạo customer mới cho Google user
                customer = new Customer();
                customer.setEmail(email);
                customer.setFirstName(firstName);
                customer.setLastName(lastName);
                customer.setOauthProvider("google");
                customer.setOauthProviderId(googleId);
                customer.setIsOauthUser(true);
                customer.setIsActive(true); // OAuth users auto active
                customer = customerRepository.save(customer);
            } else {
                // Update OAuth info cho existing user (nếu chưa có)
                if (customer.getOauthProvider() == null) {
                    customer.setOauthProvider("google");
                    customer.setOauthProviderId(googleId);
                    customer.setIsOauthUser(true);
                    customer = customerRepository.save(customer);
                }
            }

            return customer;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to authenticate Google user: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyGoogleToken(String idToken) {
        try {
            JsonNode tokenInfo = getGoogleTokenInfo(idToken);
            return tokenInfo != null && tokenInfo.has("email");
        } catch (Exception e) {
            return false;
        }
    }

    private JsonNode getGoogleTokenInfo(String idToken) {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode tokenInfo = objectMapper.readTree(response.getBody());
                
                // Verify audience (client ID)
                if (googleClientId != null && !googleClientId.isEmpty()) {
                    String aud = tokenInfo.get("aud").asText();
                    if (!googleClientId.equals(aud)) {
                        return null; // Invalid audience
                    }
                }
                
                return tokenInfo;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
