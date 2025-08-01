package com.analyio.analyiobackend.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.analyio.analyiobackend.services.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        
        // Skip JWT processing for public auth endpoints
        if (path.equals("/api/auth/admin/login") || 
            path.equals("/api/auth/company/login") || 
            path.equals("/api/auth/admin/make-super-admin") ||
            path.equals("/api/auth/refresh")) {  // Add refresh endpoint as public
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = getJwtFromRequest(request);
        final String username;

        // If no JWT token found in either header or cookies, continue without authentication
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract username from token
            username = jwtService.extractUsernameFromToken(jwt);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.isValidToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log the error but don't block the request - let it continue without authentication
            // This allows the endpoint to handle the invalid token appropriately
            System.err.println("Error processing JWT token: " + e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from request - checks both Authorization header and cookies
     * For protected endpoints, only get access tokens
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // First, try to get token from Authorization header
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // For all protected endpoints, only get access token from cookies
        // Refresh token handling is done separately in the refresh controller
        return getJwtFromCookie(request, "access_token");
    }

    /**
     * Extract JWT token from cookies
     */
    private String getJwtFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}