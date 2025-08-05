package com.analyio.analyiobackend.services;

import org.springframework.stereotype.Service;

import com.analyio.analyiobackend.jpa.repos.CompanyRepo;
import com.analyio.analyiobackend.jpa.repos.UserRepo;
import com.analyio.analyiobackend.jpa.repos.UserResetPasswordCodeRepo;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DatabaseService {
    private final UserResetPasswordCodeRepo userResetPasswordCodeRepo;
    private final UserRepo userRepo;
    private final CompanyRepo companyRepo;

    @Transactional
    public void clearDatabase() {
        userResetPasswordCodeRepo.deleteAll();
        userRepo.deleteAll();
        companyRepo.deleteAll();
    }
    
}
