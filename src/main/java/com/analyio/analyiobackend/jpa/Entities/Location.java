package com.analyio.analyiobackend.jpa.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "address", nullable = true)
    private String address;
    @Column(name = "city", nullable = true)
    private String city;
    @Column(name = "state", nullable = true)
    private String state;
    @Column(name = "country", nullable = true)
    private String country;
    @Column(name = "zip_code", nullable = true)
    private String zipCode;

    
}
