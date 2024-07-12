package com.example.banking_application.services.impl;

import com.example.banking_application.models.entities.User;
import com.example.banking_application.models.entities.VirtualCard;
import com.example.banking_application.repositories.VirtualCardRepository;
import com.example.banking_application.services.VirtualCardService;
import org.springframework.stereotype.Service;

@Service
public class VirtualCardServiceImpl implements VirtualCardService {

    private VirtualCardRepository virtualCardRepository;

    public VirtualCardServiceImpl(VirtualCardRepository virtualCardRepository) {
        this.virtualCardRepository = virtualCardRepository;
    }

    @Override
    public VirtualCard UserVirtualCard(User loggedUser) {
        return this.virtualCardRepository.findByCardHolder(loggedUser);
    }
}
