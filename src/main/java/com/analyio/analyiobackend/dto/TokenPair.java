package com.analyio.analyiobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class TokenPair {

    private String accessToken;
    private String refreshToken;
}
