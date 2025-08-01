package com.analyio.analyiobackend.controllers;

import java.util.Map;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.analyio.analyiobackend.services.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class RefreshController {
    
    private final AuthService authService;
    
    @GetMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        try {
            String refreshToken = extractRefreshToken(request);
            
            if (refreshToken == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Refresh token not found"));
            }
            
            // Generate new token pair using the refresh token
            Map<String, ResponseCookie> cookies = authService.refreshToken(refreshToken);
            
            // Set the new cookies in the response
            return ResponseEntity.ok()
                    .header("Set-Cookie", cookies.get("access_token").toString())
                    .header("Set-Cookie", cookies.get("refresh_token").toString())
                    .body(Map.of(
                        "message", "Token refreshed successfully"
                    ));
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Token refresh failed: " + e.getMessage()));
        }
    }
    
    /**
     * Extract refresh token from request - checks both Authorization header and cookies
     */
    private String extractRefreshToken(HttpServletRequest request) {
        // First, try to get refresh token from Authorization header
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // If no Authorization header, try to get refresh token from cookies
        return getTokenFromCookie(request, "refresh_token");
    }
    
    /**
     * Extract token from cookies
     */
    private String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}