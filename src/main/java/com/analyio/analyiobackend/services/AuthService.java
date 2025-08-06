package com.analyio.analyiobackend.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.analyio.analyiobackend.dto.authentication.ClientRegistrationRequest;
import com.analyio.analyiobackend.dto.authentication.RegisterCompanyFirstTime;
import com.analyio.analyiobackend.dto.authentication.ResetPasswordRequest;
import com.analyio.analyiobackend.dto.authentication.TokenPair;
import com.analyio.analyiobackend.dto.authentication.UpdateUserRequest;
import com.analyio.analyiobackend.jpa.Entities.Company;
import com.analyio.analyiobackend.jpa.Entities.Location;
import com.analyio.analyiobackend.jpa.Entities.Role;
import com.analyio.analyiobackend.jpa.Entities.UserJpa;
import com.analyio.analyiobackend.jpa.Entities.UserResetPasswordCode;
import com.analyio.analyiobackend.jpa.repos.CompanyRepo;
import com.analyio.analyiobackend.jpa.repos.UserRepo;
import com.analyio.analyiobackend.jpa.repos.UserResetPasswordCodeRepo;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final UserRepo userRepo; 
    private final EmailService emailService;
    private final CompanyRepo companyRepo;
    private final UserResetPasswordCodeRepo userResetPasswordCodeRepo;
    public Map<String, ResponseCookie> authenticate(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        


        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = jwtService.generateAccessToken(authentication);
        String refreshToken = jwtService.generateRefreshToken(authentication);
        
        TokenPair tokenPair = new TokenPair(accessToken, refreshToken);
        Map<String, ResponseCookie> cookies = new HashMap<>();
        cookies.put("access_token", jwtService.createAuthCookie(tokenPair));
        cookies.put("refresh_token", jwtService.createRefreshCookie(tokenPair));
        return cookies;
    }


public UserJpa signUpCompanyUserFlow(RegisterCompanyFirstTime request){
    // First, create and save the company
    Company company = Company.builder()
        .name(request.getName())
        .superManagerEmail(request.getSuperManagerEmail())
        .superManagerPhone(request.getSuperManagerPhone())
        .build();
    
    // Save the company first to get a managed entity
    Company savedCompany = companyRepo.save(company);
    
    // Then create the user
    UserJpa user = UserJpa.builder()
        .email(request.getSuperManagerEmail())
        .username(request.getSuperManagerUsername())
        .firstName(request.getSuperManagerFirstName())
        .lastName(request.getSuperManagerLastName())
        .password(passwordEncoder.encode(request.getSuperManagerPassword()))
        .isActive(false)
        .phoneNumber(request.getSuperManagerPhone())
        .role(Role.COMPANY_MANAGER)
        .company(savedCompany)  // Use the saved company
        .build();
    
    if(request.getLocationData() != null && request.getLocationData().isPresent()) {
        Location location = Location.builder()
            .address(request.getLocationData().get().getAddress())
            .city(request.getLocationData().get().getCity())
            .state(request.getLocationData().get().getState())
            .zipCode(request.getLocationData().get().getZipCode())
            .country(request.getLocationData().get().getCountry())
            .build();
        user.setLocation(location);
    }

    return userRepo.save(user);
}



public ResponseCookie refreshToken(String refreshToken) {
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

        return jwtService.createAuthCookie(tokenPair);
        
    } catch (Exception e) {
        throw new RuntimeException("Token refresh failed: " + e.getMessage());
    }
}



public UserJpa teamMemberLogin(String email, String accessToken){
    UserJpa currentUser = getAuthenticatedUser(accessToken);
    String randomGeneratedPassword = generateRandomPassword();

    emailService.sendEmail(
        email,
        "Welcome to analyio!",
        "You've been added as a team member by " + currentUser.getUsername() + ". Your temporary password is: " + randomGeneratedPassword
        +" to validate your account please click the link below: http://localhost:8080/api/auth/validate?email=" + email

    );    

    // Get a fresh managed Company entity from the database
    Long companyId = currentUser.getCompany().getId();
    Company company = companyRepo.findById(companyId)
        .orElseThrow(() -> new RuntimeException("Company not found"));

    UserJpa newUser = UserJpa.builder()
        .email(email)
        .password(passwordEncoder.encode(randomGeneratedPassword))
        .role(Role.COMPANY_USER)
        .company(company)  // Set the managed Company entity directly
        .isActive(true)
        .isDeleted(false)
        .build();

    return userRepo.save(newUser);
}

public UserJpa updateAccountDetails(String accessToken, UpdateUserRequest request){
    UserJpa user = getAuthenticatedUser(accessToken);

    if (request.getUsername() != null) {
        user.setUsername(request.getUsername());
    }
    if (request.getFirstName() != null) {
        user.setFirstName(request.getFirstName());
    }
    if (request.getLastName() != null) {
        user.setLastName(request.getLastName());
    }
    if (request.getPhoneNumber() != null) {
        user.setPhoneNumber(request.getPhoneNumber());
    }
    if(request.getLocationData() != null && request.getLocationData().isPresent()) {
        Location location = Location.builder()
            .address(request.getLocationData().get().getAddress())
            .city(request.getLocationData().get().getCity())
            .state(request.getLocationData().get().getState())
            .zipCode(request.getLocationData().get().getZipCode())
            .country(request.getLocationData().get().getCountry())
            .build();
        user.setLocation(location);
    }

    return userRepo.save(user);
}

public UserJpa getAuthenticatedUser(String access_token){
    String email = jwtService.extractUsernameFromToken(access_token);
    if (email == null) {
        throw new RuntimeException("Unable to extract email from access token");
    }

    return userRepo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
}

private String generateRandomPassword() {
    SecureRandom random = new SecureRandom();
    String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String lowerCase = "abcdefghijklmnopqrstuvwxyz";
    String digits = "0123456789";
    String symbols = "!@#$%^&*(),.?\":{}|<>";
    String allChars = upperCase + lowerCase + digits + symbols;
    
    StringBuilder password = new StringBuilder();
    
    // Ensure at least one character from each category
    password.append(upperCase.charAt(random.nextInt(upperCase.length())));
    password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
    password.append(digits.charAt(random.nextInt(digits.length())));
    password.append(symbols.charAt(random.nextInt(symbols.length())));
    
    // Fill the rest with random characters (minimum 8 total)
    for (int i = 4; i < 12; i++) {
        password.append(allChars.charAt(random.nextInt(allChars.length())));
    }
    
    // Shuffle the password to randomize the order
    char[] chars = password.toString().toCharArray();
    for (int i = chars.length - 1; i > 0; i--) {
        int j = random.nextInt(i + 1);
        char temp = chars[i];
        chars[i] = chars[j];
        chars[j] = temp;
    }
    
    return new String(chars);
}


public void sendResetPasswordEmail(String email){
    UserJpa user = userRepo.findByEmail(email).orElseThrow(
        () -> new RuntimeException("User not found with email: " + email)
    );
    
    // Delete any existing reset codes for this user first
    userResetPasswordCodeRepo.findByUserId(user.getId())
        .ifPresent(existingCode -> userResetPasswordCodeRepo.delete(existingCode));
    
    String resetCode = jwtService.generateResetPasswordCode();

    // Ensure code is unique
    while(userResetPasswordCodeRepo.existsByResetCode(resetCode)) {
        resetCode = jwtService.generateResetPasswordCode();
    }

    UserResetPasswordCode resetPasswordCode = new UserResetPasswordCode();
    resetPasswordCode.setResetCode(resetCode);
    resetPasswordCode.setUser(user);
    userResetPasswordCodeRepo.save(resetPasswordCode);

    emailService.sendEmail(
        email,
        "Reset Password",
        "Your reset password code is: " + resetCode
    );
}
public boolean validateResetPasswordCode(String resetCode, String email) {
    return userResetPasswordCodeRepo.findByResetCode(resetCode)
        .map(code -> !code.getExpirationDate().isBefore(LocalDateTime.now()) && code.getUser().getEmail().equals(email))
        .orElse(false);
}

public UserJpa resetPassword(ResetPasswordRequest request){
    String resetCode = request.getResetCode();
    String email = request.getEmail();
    String newPassword = request.getNewPassword();
    UserResetPasswordCode resetPasswordCode = userResetPasswordCodeRepo.findByResetCode(resetCode)
        .orElseThrow(() -> new RuntimeException("Invalid reset code"));

    if (resetPasswordCode.getExpirationDate().isBefore(LocalDateTime.now())) {
        userResetPasswordCodeRepo.delete(resetPasswordCode);
        throw new RuntimeException("Reset code has expired");
        
    }

    UserJpa user = resetPasswordCode.getUser();
    if (!user.getEmail().equals(email)) {
        throw new RuntimeException("Email does not match the user associated with the reset code");
    }
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepo.save(user);

    // Delete the reset code after successful password reset
    userResetPasswordCodeRepo.delete(resetPasswordCode);

    return user;
}


@PostConstruct
public void makeSuperAdmin(){
    if (!userRepo.existsByEmail("admin@analyio.backend")) {
        UserJpa superAdmin = UserJpa.builder()
            .email("admin@analyio.backend")
            .password(passwordEncoder.encode("SuperAdminPassword123!"))
            .role(Role.SUPER_ADMIN)
            .build();
        userRepo.save(superAdmin);
    }
}

public void sendAdminEmail(String email){
    String randomPassword = generateRandomPassword();
    String subject = "Welcome to Analyio";
    String body = 
    """
    Hello, welcome to Analyio! Please change your password after logging in for the first time. Your autogenerated password is: """ + randomPassword + """
    To activate your admin account, please click the link below:
    http://localhost:8080/api/auth/validate?email=""" + email; 
    emailService.sendEmail(email, subject, body);
    UserJpa newAdminUser = UserJpa.builder()
        .email(email)
        .password(passwordEncoder.encode(randomPassword))
        .role(Role.ADMIN)
        .isActive(false)
        .isDeleted(false)
        .build();
    
    userRepo.save(newAdminUser);
}

public void sendAccountValidationEmail(String email) {
    String subject = "Account Validation";
    String body = """
                  Please validate your account by clicking the link below:
                  http://localhost:8080/api/auth/validate?email=""" + email;

    emailService.sendEmail(email, subject, body);


}

public UserJpa registerClient(ClientRegistrationRequest request, String accessToken){
    UserJpa currentUser = getAuthenticatedUser(accessToken);
    String randomGeneratedPassword = generateRandomPassword();
    Company currentUserCompany = currentUser.getCompany();
    UserJpa newClient = UserJpa.builder()
        .email(request.getEmail())
        .username(request.getUsername())
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .password(passwordEncoder.encode(randomGeneratedPassword))
        .isActive(false)
        .phoneNumber(request.getPhoneNumber())
        .role(Role.CLIENT)
        .company(currentUserCompany)  // Use the current user's company
        .build();
    
    emailService.sendEmail(
        request.getEmail(),
        "Welcome to Analyio!",
        "You've been added as a client by " + currentUser.getUsername() + ". Your temporary password is: " + randomGeneratedPassword
        +" to validate your account please click the link below: http://localhost:8080/api/auth/validate?email=" + request.getEmail()
    );
    return userRepo.save(newClient);
    
}

public UserJpa validateUserEmail(String email) {    
Optional<UserJpa> userOptional = userRepo.findByEmail(email);
    if (userOptional.isEmpty()) {
        throw new RuntimeException("User not found with email: " + email);
    }
    UserJpa user = userOptional.get();
    user.setActive(true);
    return userRepo.save(user);
}
}