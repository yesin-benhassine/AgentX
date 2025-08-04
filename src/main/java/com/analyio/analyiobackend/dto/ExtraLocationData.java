package com.analyio.analyiobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExtraLocationData {
    

    public String address; 
    public String city;
    public String state;
    public String country;
    public String zipCode;
}
