package com.example.banking_application.services.impl;

import com.example.banking_application.inits.Initializer;
import com.example.banking_application.services.AdministrationService;
import com.example.banking_application.services.BranchService;
import com.example.banking_application.services.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InitializerTest {
    @Mock
    private AdministrationService administrationService;

    @Mock
    private BranchService branchService;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private Initializer initializer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRunWhenExchangeRatesNotInitialized() throws Exception {
        // Arrange
        when(exchangeRateService.hasInitializedExRates()).thenReturn(false);


        // Act
        initializer.run();

        // Assert
        verify(exchangeRateService).updateRates(any());
        verify(administrationService).initialize();
        verify(branchService).initialize();
    }

    @Test
    void testRunWhenExchangeRatesAlreadyInitialized() throws Exception {
        // Arrange
        when(exchangeRateService.hasInitializedExRates()).thenReturn(true);

        // Act
        initializer.run();

        // Assert
        verify(exchangeRateService, never()).updateRates(any());
        verify(administrationService).initialize();
        verify(branchService).initialize();
    }
}
