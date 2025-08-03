package com.analyio.analyiobackend.services;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.analyio.analyiobackend.jpa.Entities.UserJpa;
import com.analyio.analyiobackend.jpa.repos.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepo userRepo; // Added 'final' modifier

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserJpa> userOptional = userRepo.findByEmail(username);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        UserJpa user = userOptional.get();
        String role = user.getRole().name();

        return createUserDetails(user.getEmail(), user.getPassword(), role, user);
    }

    private UserDetails createUserDetails(String email, String password, String role, UserJpa user) {
        return User.builder()
                .username(email)
                .password(password)
                .authorities(getAuthorities(role))
                .accountExpired(false)
                .accountLocked(user.isDeleted()) // Use user's deleted status
                .credentialsExpired(false)
                .disabled(user.isDeleted()) // Disable if user is deleted
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        // Add ROLE_ prefix as expected by Spring Security
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}