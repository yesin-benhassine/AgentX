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

import com.analyio.analyiobackend.jpa.models.adminusers.AdminUser;
import com.analyio.analyiobackend.jpa.models.companyusers.CompanyUser;
import com.analyio.analyiobackend.jpa.repos.AdminUserRepo;
import com.analyio.analyiobackend.jpa.repos.CompanyUserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminUserRepo adminRepo;
    private final CompanyUserRepo companyRepo;




    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        /*admin users and company users are two seperate entities
         * therefore the solution to identify the lookup is to append a prefix to the email
         * e.g. "admin:john@company.com" for admin users and "company:john@company.com" for company users
         * this way we can determine which repository to query based on the prefix
         */
        
         if (username.startsWith("admin:")){
            Optional<AdminUser> adminUserOpt = adminRepo.findByEmail(username);
            if(adminUserOpt.isPresent()){
                AdminUser adminUser = adminUserOpt.get();
                return createUserDetails(adminUser.getEmail(), adminUser.getPassword(), "ROLE_" + adminUser.getRole().name());
            } else {
                throw new UsernameNotFoundException("Admin user not found: " + username);
            }
         } else if (username.startsWith("company:")){
            Optional<CompanyUser> companyUserOpt = companyRepo.findByEmail(username);
            if(companyUserOpt.isPresent()){
                CompanyUser companyUser = companyUserOpt.get();
                // Add business logic validation
                if (!companyUser.isActive() || companyUser.isDeleted()) {
                    throw new UsernameNotFoundException("User account is not active: " + username);
                }
                return createUserDetails(companyUser.getEmail(), companyUser.getPassword(), "ROLE_" + companyUser.getRole().name());
            } else {
                throw new UsernameNotFoundException("Company user not found: " + username);
            }
         }
         throw new UsernameNotFoundException("User not found: " + username);
    }

    private UserDetails createUserDetails(String email, String password, String role) {
        return User.builder()
                .username(email) // Spring Security still uses 'username' but we pass email
                .password(password)
                .authorities(getAuthorities(role))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return List.of(new SimpleGrantedAuthority(role));
    }
}
