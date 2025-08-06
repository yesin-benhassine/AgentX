package com.analyio.analyiobackend.dto.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidateResetCode{
    private String email;
    private String resetCode;





}