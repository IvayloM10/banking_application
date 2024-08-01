package com.example.banking_application.services.impl;

import com.example.banking_application.models.entities.Card;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.repositories.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CardServiceImplTest {
    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUserCard() {
        // Arrange
        User user = new User();
        Card expectedCard = new Card();

        when(cardRepository.findByCardHolder(user)).thenReturn(expectedCard);

        // Act
        Card actualCard = cardService.UserCard(user);

        // Assert
        assertEquals(expectedCard, actualCard);
        verify(cardRepository, times(1)).findByCardHolder(user);
    }
}
