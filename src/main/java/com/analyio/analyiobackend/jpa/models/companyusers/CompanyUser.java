package com.analyio.analyiobackend.jpa.models.companyusers;

import java.time.LocalDateTime;

import com.analyio.analyiobackend.jpa.models.company.Company;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="company_users")
public class CompanyUser {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    

    @Column(name="username", nullable = false, unique = true)
    private String username;
    @Column(name="password", nullable = false)
    private String password;
    @Column(name="role", nullable = false)
    @Enumerated(EnumType.STRING)
    private CompanyRole role;
    @Column(name="email", nullable = false, unique = true)
    private String email;
    
    @ManyToOne
    @JoinColumn(name="company_id", nullable = false)
    private Company company;


    @Column(name="is_active", nullable = false)
    private boolean isActive;

    @Column(name="is_deleted", nullable = false)
    private boolean isDeleted;


    @Column(name="is_verified", nullable = false)
    private boolean isVerified;

    @Column(name="street_address", nullable = true)
    private String streetAddress;
    @Column(name="city", nullable = true)
    private String city;
    @Column(name="state", nullable = true)
    private String state;
    @Column(name="zip_code", nullable = true)
    private String zipCode;
    @Column(name="country", nullable = true)
    private String country;
    @Column(name="phone_number", nullable = true)
    private String phoneNumber;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
        this.isDeleted = false;
        this.isVerified = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
