package com.analyio.analyiobackend.jpa.models.company;

import java.util.List;

import com.analyio.analyiobackend.jpa.models.companyusers.CompanyUser;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "companies")
public class Company  {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;



    @Column(name="name", nullable = false, unique = true)
    private String name;

    @Column(name="email", nullable = false, unique = true)
    private String email;
    
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CompanyUser> companyUsers;
    
    @Column(name="created_at", nullable = false)
    private String createdAt;

    @Column(name="updated_at", nullable = false)
    private String updatedAt;




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
