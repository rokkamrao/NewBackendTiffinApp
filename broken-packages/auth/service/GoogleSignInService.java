package com.tiffin.api.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
public class GoogleSignInService {

    @Value("${google.client.id:YOUR_GOOGLE_CLIENT_ID}")
    private String googleClientId;

    public Map<String, Object> verifyToken(String idTokenString) {
        log.info("[GoogleSignInService] Verifying Google ID token");

        // TODO: Add Google API client library dependency and implement proper token verification
        // For now, returning a mock payload for development
        // In production, this should verify the token against Google's servers
        
        if (idTokenString == null || idTokenString.trim().isEmpty()) {
            log.error("[GoogleSignInService] Empty or null token provided");
            return null;
        }

        try {
            // Mock verification for development - replace with actual Google API verification
            Map<String, Object> payload = new HashMap<>();
            payload.put("email", "test@example.com");
            payload.put("name", "Test User");
            payload.put("sub", "google_user_id_" + System.currentTimeMillis());
            
            log.info("[GoogleSignInService] Mock Google ID token verification successful");
            return payload;
            
        } catch (Exception e) {
            log.error("[GoogleSignInService] Error verifying Google ID token", e);
            return null;
        }
    }
}