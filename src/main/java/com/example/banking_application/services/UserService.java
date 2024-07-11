package com.example.banking_application.services;

import com.example.banking_application.models.dtos.UserLoginDto;
import com.example.banking_application.models.dtos.UserRegisterDto;

public interface UserService {
    boolean register(UserRegisterDto userRegisterDto);

    boolean login(UserLoginDto userLoginDto);
}
