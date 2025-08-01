package com.analyio.analyiobackend.jpa.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyio.analyiobackend.jpa.models.company.Company;

public interface CompanyRepo extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String name);
    
    boolean existsByName(String name);
    
    
}
