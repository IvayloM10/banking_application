package com.example.banking_application.services;

import com.example.banking_application.models.dtos.CardDto;
import com.example.banking_application.models.dtos.UserLoginDto;
import com.example.banking_application.models.dtos.UserRegisterDto;
import com.example.banking_application.models.entities.Card;
import com.example.banking_application.models.entities.User;

public interface UserService {
    boolean register(UserRegisterDto userRegisterDto);

    boolean login(UserLoginDto userLoginDto);


    User getCurrentUser(Long id);

    void createCardAndAccountForUser(Long userId, CardDto carddto);
}
