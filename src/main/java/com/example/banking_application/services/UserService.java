package com.example.banking_application.services;

import com.example.banking_application.models.dtos.CardDto;
import com.example.banking_application.models.dtos.TransactionDto;
import com.example.banking_application.models.dtos.UserLoginDto;
import com.example.banking_application.models.dtos.UserRegisterDto;
import com.example.banking_application.models.entities.*;
import org.springframework.stereotype.Service;


public interface UserService {
    boolean register(UserRegisterDto userRegisterDto);

    boolean login(UserLoginDto userLoginDto);



    User getCurrentUser(String username);

    void createCardAndAccountForUser( CardDto carddto);

    User getUser(String username);

    void makeTransaction(TransactionDto transactionDto,String username);

    void handleRegularTransaction(Account senderAccount, String receiverCardNumber, Transaction transaction);

    void logout();

}
