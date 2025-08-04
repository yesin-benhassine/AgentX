package com.analyio.analyiobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResetPasswordRequest {
    private String email;
    private String resetCode;
    private String newPassword;
}
