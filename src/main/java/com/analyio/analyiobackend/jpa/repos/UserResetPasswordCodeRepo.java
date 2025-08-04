package com.analyio.analyiobackend.jpa.repos;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.analyio.analyiobackend.jpa.Entities.UserResetPasswordCode;

public interface UserResetPasswordCodeRepo extends JpaRepository<UserResetPasswordCode, Long> {
    // Custom query to find a reset password code by its resetCode
    Optional<UserResetPasswordCode> findByResetCode(String resetCode);
    
    // Custom query to find a reset password code by its user ID
    Optional<UserResetPasswordCode> findByUserId(Long userId);

    boolean existsByResetCode(String resetCode);
    
    // Delete expired reset codes
    @Modifying
    @Query("DELETE FROM UserResetPasswordCode u WHERE u.expirationDate < ?1")
    void deleteByExpirationDateBefore(LocalDateTime dateTime);
    
}
