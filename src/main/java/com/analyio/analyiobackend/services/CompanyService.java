package com.analyio.analyiobackend.services;

import org.springframework.stereotype.Service;

import com.analyio.analyiobackend.jpa.Entities.Company;
import com.analyio.analyiobackend.jpa.repos.CompanyRepo;

import lombok.AllArgsConstructor;



@Service
@AllArgsConstructor
public class CompanyService {
    private final CompanyRepo companyRepo;





    public Company createCompany(String name, String superManagerEmail, String superManagerPhone) {
        Company company = new Company();
        company.setName(name);
        company.setSuperManagerEmail(superManagerEmail);
        company.setSuperManagerPhone(superManagerPhone);
        return companyRepo.save(company);
    }



    
}
