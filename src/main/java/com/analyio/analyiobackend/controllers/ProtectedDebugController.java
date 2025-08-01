package com.analyio.analyiobackend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/debug")
public class ProtectedDebugController {
    @GetMapping("/")
    public String getMethodName() {
        return "Protected debug endpoint accessed with param: ";
    }
    
    
}


