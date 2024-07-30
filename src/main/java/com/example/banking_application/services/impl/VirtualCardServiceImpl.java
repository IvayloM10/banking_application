package com.example.banking_application.services.impl;

import com.example.banking_application.models.entities.User;
import com.example.banking_application.models.entities.VirtualCard;
import com.example.banking_application.repositories.UserRepository;
import com.example.banking_application.repositories.VirtualCardRepository;
import com.example.banking_application.services.VirtualCardService;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class VirtualCardServiceImpl implements VirtualCardService {

    private final VirtualCardRepository virtualCardRepository;

    private final UserRepository userRepository;

    public VirtualCardServiceImpl(VirtualCardRepository virtualCardRepository, UserRepository userRepository) {
        this.virtualCardRepository = virtualCardRepository;

        this.userRepository = userRepository;
    }

    @Override
    public VirtualCard UserVirtualCard(User loggedUser) {
        return this.virtualCardRepository.findByCardHolder(loggedUser);
    }

    @Override
    public void generateNewNumber(String username) {
        User currentUser = this.userRepository.findByUsername(username).orElse(null);
        VirtualCard currentUserVirtualCard = currentUser.getVirtualCard();
        String newNumber = generateCardNumber();

        currentUserVirtualCard.setCardNumber(newNumber);
        this.virtualCardRepository.save(currentUserVirtualCard);
    }

    public String generateCardNumber() {
        String newNumber ="VCARD" + new Random().nextInt(99999999);
        while(this.virtualCardRepository.findByCardNumber(newNumber).isPresent()){
            newNumber = generateCardNumber();
        }
        return newNumber;
    }
}
