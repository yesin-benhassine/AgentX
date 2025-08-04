package com.analyio.analyiobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidateResetCode{
    private String email;
    private String resetCode;





}