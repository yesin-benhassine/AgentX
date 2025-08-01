package com.analyio.analyiobackend.jpa.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyio.analyiobackend.jpa.models.adminusers.AdminRole;
import com.analyio.analyiobackend.jpa.models.adminusers.AdminUser;

public interface AdminUserRepo extends JpaRepository<AdminUser, Long> {
    
    Optional<AdminUser> findByUsername(String username);
    
    Optional<AdminUser> findByEmail(String email);
    
    List<AdminUser> findAllByRole(AdminRole role);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
}
