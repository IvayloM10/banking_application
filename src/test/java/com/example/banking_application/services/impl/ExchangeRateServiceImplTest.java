package com.example.banking_application.services.impl;

import com.example.banking_application.config.ForexConfigurations;
import com.example.banking_application.models.entities.ExchangeRate;
import com.example.banking_application.repositories.ExchangeRateRepository;
import org.hibernate.ObjectNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;



import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateServiceImplTest {

    private static final class TestRates {
        // SUD -> base
        // CUR1 -> 4
        // CUR2 -> 0.5

        private static final String BASE_CURRENCY = "SUD";

        private static final ExchangeRate CUR1 = createExchangeRate("CUR1", new BigDecimal("4"));
        private static final ExchangeRate CUR2 = createExchangeRate("CUR2", new BigDecimal("0.5"));

        private static ExchangeRate createExchangeRate(String currency, BigDecimal rate) {
            ExchangeRate exchangeRate = new ExchangeRate();
            exchangeRate.setCurrency(currency);
            exchangeRate.setRate(rate);
            return exchangeRate;
        }
    }

    private ExchangeRateServiceImpl toTest;

    @Mock
    private ExchangeRateRepository mockRepository;

    @BeforeEach
    void setUp() {
        ForexConfigurations forexConfigurations = new ForexConfigurations();
        forexConfigurations.setBase(TestRates.BASE_CURRENCY);
        this.toTest = new ExchangeRateServiceImpl(
                this.mockRepository,
                null,
                forexConfigurations);
    }

    // 1 SUD ->   4 CUR1
    // 1 SUD -> 0.5 CUR2

    @ParameterizedTest(name = "Converting {2} {0} to {1}. Expected {3}")
    @CsvSource(
            textBlock = """
          SUD, CUR1, 1, 4.00
          SUD, CUR1, 2, 8.00
          SUD, SUD,  1, 1
          CUR1,CUR2, 1, 0.12
          CUR2,CUR1, 1, 8.00
          LBD, LBD, 1, 1
          """
    )
    void testConvert(String from,
                     String to,
                     BigDecimal amount,
                     BigDecimal expected) {

        initExRates();

        BigDecimal result = this.toTest.convert(from, to, amount);
        Assertions.assertEquals(expected, result);
    }

    @Test
    void testApiObjectNotFoundException() {
        Assertions.assertThrows(ObjectNotFoundException.class,
                () -> this.toTest.convert("NO_EXIST_1", "NOT_EXIST_2", BigDecimal.ONE)
        );
    }

    private void initExRates() {
        Mockito.lenient().when(this.mockRepository.findByCurrency(TestRates.CUR1.getCurrency()))
                .thenReturn(Optional.of(TestRates.CUR1));
        Mockito.lenient().when(this.mockRepository.findByCurrency(TestRates.CUR2.getCurrency()))
                .thenReturn(Optional.of(TestRates.CUR2));
    }

    @Test
    void testHasInitializedExRates() {
        when(this.mockRepository.count()).thenReturn(0L);
        Assertions.assertFalse(this.toTest.hasInitializedExRates());

        when(this.mockRepository.count()).thenReturn(-5L);
        Assertions.assertFalse(this.toTest.hasInitializedExRates());

        when(this.mockRepository.count()).thenReturn(6L);
        Assertions.assertTrue(this.toTest.hasInitializedExRates());
    }
}
