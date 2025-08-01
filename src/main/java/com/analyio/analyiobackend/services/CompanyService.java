package com.analyio.analyiobackend.services;

import org.springframework.stereotype.Service;

import com.analyio.analyiobackend.jpa.models.company.Company;
import com.analyio.analyiobackend.jpa.repos.CompanyRepo;

import lombok.AllArgsConstructor;



@Service
@AllArgsConstructor
public class CompanyService {
    private final CompanyRepo companyRepo;





    public Company createCompany(Company company) {
        if (companyRepo.existsByName(company.getName())) {
            throw new IllegalArgumentException("Company with this name already exists");
        }
        return companyRepo.save(company);
    }
    
}
