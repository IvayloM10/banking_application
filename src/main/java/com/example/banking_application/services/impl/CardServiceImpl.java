package com.example.banking_application.services.impl;

import com.example.banking_application.models.entities.Card;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.repositories.CardRepository;
import com.example.banking_application.services.CardService;
import org.springframework.stereotype.Service;

@Service
public class CardServiceImpl implements CardService {
    private CardRepository cardRepository;


    public CardServiceImpl(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public Card UserCard(User user){
       return this.cardRepository.findByCardHolder(user);
    }
}
