package com.analyio.analyiobackend.jpa.Entities;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="users")
public class UserJpa {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "username", unique = true, nullable = true)
    private String username;
    @Column(name="first_name", nullable = true)
    private String firstName;
    @Column(name="last_name", nullable = true)
    private String lastName;
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name="is_active", nullable = false)
    private boolean isActive;

    @Column(name="is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name="role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
    

    @ManyToOne(optional = true)
    @JoinColumn(name="company_id", nullable = true)
    private Company company;

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name="location_id", nullable = true)
    private Location location;
    @Column(name="phone_number", nullable = true)
    private String phoneNumber;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;




    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.isActive = true;
        this.isDeleted = false;  // 
    }
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();   }
        
}
