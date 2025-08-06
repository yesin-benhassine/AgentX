package com.analyio.analyiobackend.dto.authentication;

import lombok.Builder;
import lombok.Data;


@Data
@Builder

public class ClientRegistrationRequest {

    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber; 
    
}
