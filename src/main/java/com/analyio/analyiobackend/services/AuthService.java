package com.analyio.analyiobackend.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.analyio.analyiobackend.dto.TokenPair;
import com.analyio.analyiobackend.dto.auth.LoginRequest;
import com.analyio.analyiobackend.dto.auth.RegisterAdminUserRequest;
import com.analyio.analyiobackend.dto.auth.RegisterCompanyUserRequest;
import com.analyio.analyiobackend.jpa.models.adminusers.AdminRole;
import com.analyio.analyiobackend.jpa.models.adminusers.AdminUser;
import com.analyio.analyiobackend.jpa.models.company.Company;
import com.analyio.analyiobackend.jpa.models.companyusers.CompanyRole;
import com.analyio.analyiobackend.jpa.models.companyusers.CompanyUser;
import com.analyio.analyiobackend.jpa.repos.AdminUserRepo;
import com.analyio.analyiobackend.jpa.repos.CompanyRepo;
import com.analyio.analyiobackend.jpa.repos.CompanyUserRepo;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthService {

    private final CompanyUserRepo companyUserRepo;
    private final CompanyRepo companyRepo;
    private final AdminUserRepo adminUserRepo;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Transactional
    public CompanyUser registerCompanyUser(RegisterCompanyUserRequest request){
        // First, fetch the company by ID
        Company company = companyRepo.findById(request.getCompanyId())
            .orElseThrow(() -> new RuntimeException("Company not found with ID: " + request.getCompanyId()));
        
        CompanyUser companyUser = new CompanyUser();
        companyUser.setUsername(request.getUsername());
        companyUser.setPassword(passwordEncoder.encode(request.getPassword()));
        companyUser.setRole(CompanyRole.valueOf(request.getRole().toUpperCase()));
        companyUser.setEmail("company:" + request.getEmail()); // Storing with prefix
        companyUser.setCompany(company); 
        
        // Set additional fields if present
        request.getStreetAddress().ifPresent(companyUser::setStreetAddress);
        request.getCity().ifPresent(companyUser::setCity);
        request.getState().ifPresent(companyUser::setState);
        request.getZipCode().ifPresent(companyUser::setZipCode);
        request.getCountry().ifPresent(companyUser::setCountry);
        request.getPhoneNumber().ifPresent(companyUser::setPhoneNumber);

        return companyUserRepo.save(companyUser);
    }    

    @Transactional 
    public AdminUser registerAdminUser(RegisterAdminUserRequest request){
        AdminUser adminUser = 
        AdminUser.builder().username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(AdminRole.ADMIN)
                .email("admin:" + request.getEmail()) 
                .address(request.getAddress().orElse(null))
                .city(request.getCity().orElse(null))
                .state(request.getState().orElse(null))
                .zipCode(request.getZipCode().orElse(null))
                .country(request.getCountry().orElse(null))
                .phoneNumber(request.getPhoneNumber().orElse(null))
                .build();

        return adminUserRepo.save(adminUser);
    }

    public Map<String, ResponseCookie> loginCompanyUser(LoginRequest request){
        String companyEmail = "company:" + request.getEmail();
        
        // Find user by prefixed email
        CompanyUser user = companyUserRepo.findByEmail(companyEmail)
            .orElseThrow(() -> new RuntimeException("Company user not found with email: " + request.getEmail()));

        // Check if user is active BEFORE authentication
        if (!user.isActive() || user.isDeleted()) {
            throw new RuntimeException("User account is not active: " + request.getEmail());
        }

        // Authenticate using the prefixed email (this will call CustomUserDetailsService)
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                companyEmail, // This should match what's stored in DB
                request.getPassword()
            )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        TokenPair tokenPair = jwtService.generateTokenPair(authentication);

        Map<String, ResponseCookie> cookies = new HashMap<>();
        cookies.put("access_token", jwtService.createAuthCookie(tokenPair));
        cookies.put("refresh_token", jwtService.createRefreshCookie(tokenPair));
        return cookies;
    }

public Map<String, ResponseCookie> loginAdminUser(LoginRequest request){
    String adminEmail = "admin:" + request.getEmail();
    
    // Find user by prefixed email
    AdminUser user = adminUserRepo.findByEmail(adminEmail)
        .orElseThrow(() -> new RuntimeException("Admin user not found with email: " + request.getEmail()));

    // Authenticate using the prefixed email
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            adminEmail, // This should match what's stored in DB
            request.getPassword()
        )
    );
    SecurityContextHolder.getContext().setAuthentication(authentication);

    TokenPair tokenPair = jwtService.generateTokenPair(authentication);
    Map<String, ResponseCookie> cookies = new HashMap<>();
    
    cookies.put("access_token", jwtService.createAuthCookie(tokenPair));
    cookies.put("refresh_token", jwtService.createRefreshCookie(tokenPair));
      
    return cookies; 
}




    @Transactional 
    public void makeSuperAdmin() throws Exception{
        String email = "admin@admin.admin";
        String password = "admin123";
        String username = "superadmin";

        if(adminUserRepo.existsByEmail("admin:" + email)){
            throw new Exception("Super admin already exists");
        }
        

        adminUserRepo.save(AdminUser.builder()
                .email("admin:" + email)
                .password(passwordEncoder.encode(password))
                .username(username)
                .role(AdminRole.SUPER_ADMIN)
                .build());



    }


public Map<String, ResponseCookie> refreshToken(String refreshToken) {
    try {
        // First, check if it's a refresh token
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Token is not a refresh token");
        }

        // Extract username from the refresh token
        String username = jwtService.extractUsernameFromToken(refreshToken);
        if (username == null) {
            throw new RuntimeException("Unable to extract username from refresh token");
        }

        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        // Validate the refresh token with user details
        if (!jwtService.isValidToken(refreshToken, userDetails)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Generate new access and refresh tokens
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
        
        TokenPair tokenPair = jwtService.generateTokenPair(authentication);

        Map<String, ResponseCookie> cookies = new HashMap<>();
        cookies.put("access_token", jwtService.createAuthCookie(tokenPair));
        cookies.put("refresh_token", jwtService.createRefreshCookie(tokenPair));
        
        return cookies;
        
    } catch (Exception e) {
        throw new RuntimeException("Token refresh failed: " + e.getMessage());
    }
}
}