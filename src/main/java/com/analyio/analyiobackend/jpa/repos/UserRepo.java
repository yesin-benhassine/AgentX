package com.analyio.analyiobackend.jpa.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyio.analyiobackend.jpa.Entities.UserJpa;

public interface UserRepo extends JpaRepository<UserJpa, Long>{
    // Custom query to find a user by their email
    Optional<UserJpa> findByEmail(String email);

    // Custom query to find a user by their username
    Optional<UserJpa> findByUsername(String username);

    // Custom query to find a user by their first name
    Optional<UserJpa> findByFirstName(String firstName);

    // Custom query to find a user by their last name
    Optional<UserJpa> findByLastName(String lastName);
    
    // Custom query to find a user by their phone number
    Optional<UserJpa> findByPhoneNumber(String phoneNumber);


    boolean existsByEmail(String email);

}
