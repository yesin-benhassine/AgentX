package com.analyio.analyiobackend.jpa.models.adminusers;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "admin_users")
public class AdminUser {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id; 


    @Column(name="username", nullable = false, unique = true)
    private String username;

    @Column(name="password", nullable = false)
    private String password;


    @Column(name="role", nullable = false)
    @Enumerated(EnumType.STRING)
    private AdminRole role;

    @Column(name="email", nullable = false, unique = true)
    private String email;
    

    @Column(name="created_at", nullable = false)
    private String createdAt;

    @Column(name="updated_at", nullable = false)
    private String updatedAt;


    @Column(name="address", nullable = true)
    private String address;
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

    @PrePersist

    public void prePersist() {
        this.createdAt = java.time.LocalDateTime.now().toString();
        this.updatedAt = java.time.LocalDateTime.now().toString();
    }
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = java.time.LocalDateTime.now().toString();
    }
}
