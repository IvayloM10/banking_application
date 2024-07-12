package com.example.banking_application.services;

import com.example.banking_application.models.entities.Card;
import com.example.banking_application.models.entities.User;

public interface CardService {


    Card UserCard(User loggedUser);
}
