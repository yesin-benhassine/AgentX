package com.analyio.analyiobackend.dto.auth;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RegisterAdminUserRequest {
    private String username; 
    private String password; 
    
    private String email;
    private Optional<String> address;
    private Optional<String> city;
    private Optional<String> state;
    private Optional<String> zipCode;
    private Optional<String> country;
    private Optional<String> phoneNumber;
    
}
