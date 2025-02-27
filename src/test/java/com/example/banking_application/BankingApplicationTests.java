package com.example.banking_application;


import com.example.banking_application.config.ForexConfigurations;

import com.example.banking_application.configs.TestSecurityConfig;
import com.example.banking_application.models.dtos.ExchangeRateDto;
import com.example.banking_application.services.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;

import static org.mockito.Mockito.when;


@SpringBootTest(classes = BankingApplication.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class BankingApplicationTests {

	@MockBean
	private ForexConfigurations forexConfigurations;

	@MockBean
	private ExchangeRateService exchangeRateService;

	@Test
	void contextLoads() {
		Mockito.when(exchangeRateService.fetchExRates()).thenReturn(new ExchangeRateDto("USD",new HashMap<>()));
	}

}
