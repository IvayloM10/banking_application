package com.example.banking_application.services;

import com.example.banking_application.models.dtos.UserLoginDto;

public interface AdministrationService {
    void initialize();


    boolean loginAdmin(UserLoginDto userLoginDto);
}
