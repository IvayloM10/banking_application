package com.example.banking_application.services;

import com.example.banking_application.models.dtos.CardDto;
import com.example.banking_application.models.dtos.TransactionDto;
import com.example.banking_application.models.dtos.UserLoginDto;
import com.example.banking_application.models.dtos.UserRegisterDto;
import com.example.banking_application.models.entities.Account;
import com.example.banking_application.models.entities.Card;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.models.entities.VirtualCard;

public interface UserService {
    boolean register(UserRegisterDto userRegisterDto);

    boolean login(UserLoginDto userLoginDto);



    User getCurrentUser();

    void createCardAndAccountForUser( CardDto carddto);

    User getUser(String username);

    void makeTransaction(TransactionDto transactionDto);
}
