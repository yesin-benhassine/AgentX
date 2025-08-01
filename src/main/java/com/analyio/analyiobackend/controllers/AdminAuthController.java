package com.analyio.analyiobackend.controllers;

import java.util.Map;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.analyio.analyiobackend.dto.auth.LoginRequest;
import com.analyio.analyiobackend.dto.auth.RegisterAdminUserRequest;
import com.analyio.analyiobackend.services.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/auth/admin/")
@AllArgsConstructor
public class AdminAuthController {

    private final AuthService authService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<?> registerAdminUser(@RequestBody RegisterAdminUserRequest request) {
        if(request.getUsername() == null || request.getPassword() == null || request.getEmail() == null) {
            throw new IllegalArgumentException("Username, password, and email are required.");
        }
        try {
            return ResponseEntity.ok(authService.registerAdminUser(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error registering admin user: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginAdminUser(@RequestBody LoginRequest request, HttpServletResponse response) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        try {
            // Get the cookies from the service
            Map<String, ResponseCookie> cookies = authService.loginAdminUser(request);
            
            // Set the cookies in the HTTP response
            response.addHeader("Set-Cookie", cookies.get("access_token").toString());
            response.addHeader("Set-Cookie", cookies.get("refresh_token").toString());
            
            // Return a success response (without exposing token values)
            return ResponseEntity.ok().body(Map.of(
                "message", "Login successful",
                "userType", "admin"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error logging in: " + e.getMessage());
        }
    }

    @GetMapping("/make-super-admin")
    public void makeSuperAdmin() throws Exception {
        authService.makeSuperAdmin();
    }
}   