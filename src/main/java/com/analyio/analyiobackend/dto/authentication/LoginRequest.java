package com.analyio.analyiobackend.dto.authentication;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    
}
