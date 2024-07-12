package com.example.banking_application.services;


import com.example.banking_application.models.entities.User;
import com.example.banking_application.models.entities.VirtualCard;

public interface VirtualCardService {
    VirtualCard UserVirtualCard(User loggedUser);

}
