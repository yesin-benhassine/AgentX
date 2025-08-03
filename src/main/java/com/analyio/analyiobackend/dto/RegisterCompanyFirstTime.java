package com.analyio.analyiobackend.dto;

import java.util.Optional;

import lombok.Data;

@Data
public class RegisterCompanyFirstTime {

    public String name;
    public String superManagerEmail;
    public String superManagerPhone;
    public String superManagerUsername;
    public String superManagerPassword;
    public String superManagerFirstName;
    public String superManagerLastName;
    public Optional<ExtraLocationData> locationData;
    
    

    
}
