package com.analyio.analyiobackend.dto.auth;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RegisterCompanyUserRequest {
    private String username;
    private String password;
    private String role;
    private String email;
    private Long companyId;
    private Optional<String> streetAddress;
    private Optional<String> city;
    private Optional<String> state;
    private Optional<String> zipCode;
    private Optional<String> country;
    private Optional<String> phoneNumber;

    // Additional fields can be added as needed
    
}
