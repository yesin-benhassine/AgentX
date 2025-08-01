package com.analyio.analyiobackend.controllers;

import java.util.Map;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.analyio.analyiobackend.dto.auth.LoginRequest;
import com.analyio.analyiobackend.dto.auth.RegisterCompanyUserRequest;
import com.analyio.analyiobackend.jpa.models.company.Company;
import com.analyio.analyiobackend.jpa.models.companyusers.CompanyUser;
import com.analyio.analyiobackend.services.AuthService;
import com.analyio.analyiobackend.services.CompanyService;
import com.analyio.analyiobackend.services.JwtService;

import lombok.AllArgsConstructor;


@RestController
@RequestMapping("/api/auth/company/")
@AllArgsConstructor
public class CompanyAuthController {

    private final CompanyService companyService;
    private final AuthService authService;
    private final JwtService jwtService;




    @PostMapping("/register")
    public ResponseEntity<?> registerCompanyUser (@RequestBody RegisterCompanyUserRequest request) {
        // Validate the request data
        if (request.getUsername() == null || request.getPassword() == null || request.getEmail() == null || request.getCompanyId() == null) {
            throw new IllegalArgumentException("Username, password, email, and company ID are required.");
        }

        // Register the company user
        CompanyUser companyUser = authService.registerCompanyUser(request);


        return ResponseEntity.ok(RegisterCompanyUserRequest.builder()
                .username(companyUser.getUsername())
                .email(companyUser.getEmail())
                .role(companyUser.getRole().name())
                .companyId(companyUser.getCompany().getId())
                
                .build());

        
    }

    @GetMapping("/create-company")
    public ResponseEntity<?> createCompany(@RequestParam String name, @RequestParam String email) {
        
        // Validate the request data
        if (name == null || email == null) {
            throw new IllegalArgumentException("Company name and email are required.");
        }

        // Create the company
        return ResponseEntity.ok(companyService.createCompany(Company.builder()
                .name(name)
                .email(email)
                .build()));
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginCompanyUser(@RequestBody LoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        try{
            // Get the cookies from the service
            Map<String, ResponseCookie> cookies = authService.loginCompanyUser(request);
            
            // Return a success response (without exposing token values)
            return ResponseEntity.ok().header("Set-Cookie", cookies.get("access_token").toString())
                    .header("Set-Cookie", cookies.get("refresh_token").toString())
                    .body(Map.of(
                        "message", "Login successful",
                        "userType", "company"
                    ));
                    
        

        
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }


    @GetMapping("/verify")
    public ResponseEntity<?> verifyCompanyUser(@RequestParam String token) {

        return ResponseEntity.ok(jwtService.decodeToken(token));
    }


    
    
    
    
}
