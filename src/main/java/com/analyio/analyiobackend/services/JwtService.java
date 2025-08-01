package com.analyio.analyiobackend.services;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.analyio.analyiobackend.dto.TokenPair;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtService {
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;
    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpirationMs;




    //generate access token
    public String generateAccessToken(Authentication authentication){
        return generateToken(authentication, jwtExpirationMs, new HashMap<>());
    }


    private SecretKey getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //generate refresh token


    public String generateRefreshToken(Authentication authentication){

        Map<String, String> claims = new HashMap<>();
        claims.put("tokenType","refresh");
        return generateToken(authentication, refreshExpirationMs, claims);
    }


    private String generateToken(Authentication authentication, long expiryMs, Map<String, String> claims){
        UserDetails userPrincipal =(UserDetails) authentication.getPrincipal();
        return Jwts.builder().subject(userPrincipal.getUsername()).claims(claims).issuedAt(new Date()).expiration(new Date(new Date().getTime()+expiryMs)).signWith(getSignInKey()).compact();
    }

        //validate token

    public boolean isValidToken(String token, UserDetails userDetails){
        final String username = extractUsernameFromToken(token);

        if(!username.equals(userDetails.getUsername())){
            return false;
        }
        try{
            Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token);
                    return true;
        }catch(SignatureException e){
            log.error("Invalid JWT signature: {}", e.getMessage());
            return false;

        }catch(MalformedJwtException e){
                log.error("Invalid JWT token: {}", e.getMessage());
            return false;

        }catch(ExpiredJwtException e){
            log.error("JWT token is expired:{}", e.getMessage());
            return false;

        }catch(UnsupportedJwtException e){
            log.error("JWT token is unsupported:{}", e.getMessage());
            return false;

        }
        catch(IllegalArgumentException e){
            log.error("JWT claims string is empty:{}", e.getMessage());
            return false;

        }
    }

    public String extractUsernameFromToken(String token){
        return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload().getSubject();
    }



    //validate if token is refresh token
    public boolean isRefreshToken(String token){
        Claims claims = Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
        return "refresh".equals(claims.get("tokenType"));
    }

    public TokenPair generateTokenPair(Authentication authentication) {
        String access = generateAccessToken(authentication);
        String refresh = generateRefreshToken(authentication);
        return new TokenPair(access, refresh);
    }

    public String decodeToken(String token) {
        try {
            return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload().toString();
        } catch (Exception e) {
            log.error("Failed to decode JWT token: {}", e.getMessage());
            return null;
        }
    }

public ResponseCookie createAuthCookie(TokenPair tokenPair) {
    return ResponseCookie.from("access_token", tokenPair.getAccessToken())
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(jwtExpirationMs / 1000)
            .build();
}

public ResponseCookie createRefreshCookie(TokenPair tokenPair) {
    return ResponseCookie.from("refresh_token", tokenPair.getRefreshToken())
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(refreshExpirationMs / 1000)
            .build();
}
    


}
