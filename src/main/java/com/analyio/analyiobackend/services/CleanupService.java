package com.analyio.analyiobackend.services;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analyio.analyiobackend.jpa.repos.UserResetPasswordCodeRepo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class CleanupService {

    private final UserResetPasswordCodeRepo userResetPasswordCodeRepo;

    /**
     * Runs every 5 minutes to clean up expired reset password codes
     * Cron expression: 0 0/5 * * * * (every 5 minutes)
     * Maximum delay after expiration: 5 minutes
     */
    @Scheduled(cron = "0 0/5 * * * *")
    @Transactional
    public void cleanupExpiredResetCodes() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Starting cleanup of expired reset codes at {}", now);
        
        try {
            userResetPasswordCodeRepo.deleteByExpirationDateBefore(now);
            log.info("Successfully cleaned up expired reset codes");
        } catch (Exception e) {
            log.error("Failed to cleanup expired reset codes: {}", e.getMessage(), e);
        }
    }

    /**
     * Alternative: Run every 15 minutes
     * @Scheduled(fixedRate = 900000) // 15 minutes in milliseconds
     */
    
    /**
     * Alternative: Run every hour at minute 0
     * @Scheduled(cron = "0 0 * * * *")
     */
}
