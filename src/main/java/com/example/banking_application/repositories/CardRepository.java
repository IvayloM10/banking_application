package com.example.banking_application.repositories;

import com.example.banking_application.models.entities.Card;
import com.example.banking_application.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card,Long>{

    Card findByCardHolder(User user);

}
