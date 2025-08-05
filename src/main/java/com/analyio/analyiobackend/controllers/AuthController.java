package com.analyio.analyiobackend.controllers;

import java.util.Map;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.analyio.analyiobackend.dto.LoginRequest;
import com.analyio.analyiobackend.dto.RegisterCompanyFirstTime;
import com.analyio.analyiobackend.dto.ResetPasswordRequest;
import com.analyio.analyiobackend.dto.UpdateUserRequest;
import com.analyio.analyiobackend.jpa.Entities.UserJpa;
import com.analyio.analyiobackend.services.AuthService;
import com.analyio.analyiobackend.services.CleanupService;
import com.analyio.analyiobackend.services.DatabaseService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CleanupService cleanupService;
    private final DatabaseService databaseService;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterCompanyFirstTime request) {
        try{
            if(validateEmail(request.getSuperManagerEmail()) && validatePassword(request.getSuperManagerPassword())){
                UserJpa user = authService.signUpCompanyUserFlow(request);
                authService.sendAccountValidationEmail(user.getEmail());
                return ResponseEntity.ok(user);
            }
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body("Invalid email or password format. Please ensure your email is valid and your password meets the required criteria." +e.getMessage());
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
        
    @GetMapping("/validate")
    public ResponseEntity<?> validateEmailAccount(@RequestParam String email) {
        try {
            UserJpa user = authService.validateUserEmail(email);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to validate email: " + e.getMessage());
        }
    }


    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
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
    @GetMapping("/add-team-member")
    @PreAuthorize("hasRole('COMPANY_MANAGER')")
    public ResponseEntity<?> addTeamMember(@RequestParam String email, HttpServletRequest request) {
        authService.teamMemberLogin(email, extractAccessToken(request));
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/update-account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserJpa> updateAccountDetails(@RequestBody UpdateUserRequest request, HttpServletRequest httpServletRequest) {
        String accessToken = extractAccessToken(httpServletRequest);
        if (accessToken == null) {
            return ResponseEntity.status(401).body(null); // Unauthorized if no access token
        }
        UserJpa updatedUser = authService.updateAccountDetails(accessToken, request);
        return ResponseEntity.ok(updatedUser);
    
}

@GetMapping("/send-reset-password-email")
public ResponseEntity<?> sendPasswordResetEmail(@RequestParam String email) {
    try{
        authService.sendResetPasswordEmail(email);
        return ResponseEntity.ok("Reset password email sent successfully");
    }
    catch (Exception e) {
        return ResponseEntity.badRequest().body("Failed to send reset password email: " + e.getMessage());
    }

}
//development only endpoint
@GetMapping("/validate-reset-code")
public ResponseEntity<?> validateResetCode(@RequestParam String resetCode, @RequestParam String email) {
    try{
        boolean isValid = authService.validateResetPasswordCode(resetCode, email);
        if (isValid) {
            return ResponseEntity.ok("Reset code is valid");
        } else {
            cleanupService.cleanupExpiredResetCodes();
            return ResponseEntity.badRequest().body("Invalid reset code");
        }
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Error validating reset code: " + e.getMessage());
    }




}

@PutMapping("/reset-password")
public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request){
    try{
        authService.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully");
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Failed to reset password: " + e.getMessage());
    }
}

@GetMapping("/add-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public ResponseEntity<?> addAdmin(@RequestParam String email) {
    try {
        authService.sendAdminEmail(email);
        return ResponseEntity.ok().build();
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Failed to add admin: " + e.getMessage());
    }

}


@GetMapping("/CLEAN-DATABASE")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public ResponseEntity<?> cleanDatabase() {
    try {
        databaseService.clearDatabase();
        authService.makeSuperAdmin();
        return ResponseEntity.ok().build();

    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Failed to clean database: " + e.getMessage());
    }

}
}