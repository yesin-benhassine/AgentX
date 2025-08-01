package com.analyio.analyiobackend.jpa.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyio.analyiobackend.jpa.models.companyusers.CompanyRole;
import com.analyio.analyiobackend.jpa.models.companyusers.CompanyUser;

public interface CompanyUserRepo extends JpaRepository<CompanyUser, Long> {
    
    Optional<CompanyUser> findByUsername(String username);
    
    Optional<CompanyUser> findByEmail(String email);
    
    List<CompanyUser> findAllByRole(CompanyRole role);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
}
