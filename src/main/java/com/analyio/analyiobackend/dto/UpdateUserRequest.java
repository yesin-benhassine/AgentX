package com.analyio.analyiobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UpdateUserRequest {
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    
}
