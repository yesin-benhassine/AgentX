package com.analyio.analyiobackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinalizeTeamMemberAccount {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    
}
