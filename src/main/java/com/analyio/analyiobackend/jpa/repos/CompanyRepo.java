package com.analyio.analyiobackend.jpa.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyio.analyiobackend.jpa.Entities.Company;



public interface  CompanyRepo extends JpaRepository<Company, Long> {

    // Custom query to find a company by its name
    Optional<Company> findByName(String name);

    // Custom query to find a company by its super manager email
    Optional<Company> findBySuperManagerEmail(String email);

    // Custom query to find a company by its super manager phone
    Optional<Company> findBySuperManagerPhone(String phone);
    
    
}
