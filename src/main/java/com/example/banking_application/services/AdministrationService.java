package com.example.banking_application.services;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.dtos.UserLoginDto;
import com.example.banking_application.models.entities.Administrator;

public interface AdministrationService {
    void initialize();


    boolean loginAdmin(UserLoginDto userLoginDto);

    Administrator getCurrentAdmin(String id);

    CurrentUser getCurrentUser();

    void approveTransaction(Long id,String username);

    void rejectTransaction(Long id, String username);

}
