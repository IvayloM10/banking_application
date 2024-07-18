package com.example.banking_application.repositories;

import com.example.banking_application.models.entities.User;
import com.example.banking_application.models.entities.VirtualCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VirtualCardRepository extends JpaRepository<VirtualCard, Long> {
    VirtualCard findByCardHolder(User user);


    Optional<VirtualCard> findByCardNumber(String cardNumber);
}
