package com.analyio.analyiobackend.controllers;

import java.util.Map;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.analyio.analyiobackend.dto.LoginRequest;
import com.analyio.analyiobackend.dto.RegisterCompanyFirstTime;
import com.analyio.analyiobackend.jpa.Entities.UserJpa;
import com.analyio.analyiobackend.services.AuthService;
import com.analyio.analyiobackend.services.EmailService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final EmailService emailService;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterCompanyFirstTime request) {
        try{
            if(validateEmail(request.getSuperManagerEmail()) && validatePassword(request.getSuperManagerPassword())){
                UserJpa user = authService.signUpCompanyUserFlow(request);
                return ResponseEntity.ok(user);
            }
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body("Invalid email or password format. Please ensure your email is valid and your password meets the required criteria.");
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest)  {
        try {
            Map<String, ResponseCookie> cookies = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());

            // Set cookies in response headers
            ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();
            for (ResponseCookie cookie : cookies.values()) {
                responseBuilder.header("Set-Cookie", cookie.toString());
            }
            
            return responseBuilder.body("Login successful");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid email or password");
        
        }
    }
        


    @GetMapping("/me")
    public ResponseEntity<UserJpa> getCurrentLoggedInUser(HttpServletRequest request){
        String accessToken=new String(); 
        if (request.getCookies() == null) {
            return ResponseEntity.status(401).body(null); // Unauthorized if no cookies present
        }
            for (Cookie cookie: request.getCookies()){
                if (cookie.getName().equals("access_token")) {
                    accessToken = cookie.getValue();
                    break;
                }

            }

        if (accessToken == null) {
            return ResponseEntity.status(401).body(null); 
        }else{
        try{
            UserJpa user = authService.getAuthenticatedUser(accessToken);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(null); 
        }}




    }


            @GetMapping("/refresh")
        public ResponseEntity<?> refresh(HttpServletRequest request){
            String refreshToken = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if (cookie.getName().equals("refresh_token")) {
                        refreshToken = cookie.getValue();
                        break;
                    }
                }
            }

            if (refreshToken == null) {
                return ResponseEntity.status(401).body("Refresh token not found");
            }

            try {
                ResponseCookie cookie = authService.refreshToken(refreshToken);


                ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();
                responseBuilder.header("Set-Cookie", cookie.toString());
                return responseBuilder.body("Tokens refreshed successfully");
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Failed to refresh tokens: " + e.getMessage());
            }
            
        }







    private boolean  validateEmail(String email){
        return ( !email.isEmpty() && email.indexOf("@")!=-1 && email.indexOf(".")!=-1 && email.indexOf(".")>email.indexOf("@"));
    }
    private boolean validatePassword(String password){
        if (password == null || password.length() < 8) {
            return false;
        }
        
        // Check for at least one uppercase letter
        boolean hasUpper = password.matches(".*[A-Z].*");
        // Check for at least one lowercase letter
        boolean hasLower = password.matches(".*[a-z].*");
        // Check for at least one digit
        boolean hasDigit = password.matches(".*\\d.*");
        // Check for at least one special character
        boolean hasSymbol = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        
        return hasUpper && hasLower && hasDigit && hasSymbol && password.length() >= 8;
    }

    private String extractAccessToken(HttpServletRequest request) {
        String accessToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("access_token")) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }
        return accessToken;
    }
    @PostMapping("/add-team-member")
    @PreAuthorize("hasRole('COMPANY_MANAGER')")
    public ResponseEntity<?> addTeamMember(@RequestParam String email, HttpServletRequest request) {
        authService.teamMemberLogin(email, extractAccessToken(request));
        return ResponseEntity.ok().build();
    }}