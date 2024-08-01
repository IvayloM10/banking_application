package com.example.banking_application.services.impl;

import com.example.banking_application.models.entities.User;
import com.example.banking_application.models.entities.VirtualCard;
import com.example.banking_application.repositories.UserRepository;
import com.example.banking_application.repositories.VirtualCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VirtualCardServiceImplTest {

    private static final String USERNAME = "testUser";
    @Mock
    private VirtualCardRepository mockVirtualCardRepository;

    @Mock
    private UserRepository mockUserRepository;

    @InjectMocks
    private VirtualCardServiceImpl virtualCardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUserVirtualCard() {
        // Arrange- create user and add when to make the cardholder be the user created when calling
        User user = new User();
        VirtualCard expectedCard = new VirtualCard();

        when(mockVirtualCardRepository.findByCardHolder(user)).thenReturn(expectedCard);

        // Act - call mehtod
        VirtualCard actualCard = virtualCardService.UserVirtualCard(user);

        // Assert
        assertEquals(expectedCard, actualCard);
        verify(mockVirtualCardRepository, times(1)).findByCardHolder(user);
    }

    @Test
    void testGenerateNewNumber() {
        // Arrange
        String username = USERNAME;
        User user = mock(User.class);
        VirtualCard virtualCard = new VirtualCard();
        String initialNumber = "VCARD12345678";
        virtualCard.setCardNumber(initialNumber);

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(user.getVirtualCard()).thenReturn(virtualCard);

        // Act - call method
        virtualCardService.generateNewNumber(username);

        // Assert
        verify(mockUserRepository, times(1)).findByUsername(username);
        verify(mockVirtualCardRepository, times(1)).save(virtualCard);
        assertNotEquals(initialNumber, virtualCard.getCardNumber());
    }
}



