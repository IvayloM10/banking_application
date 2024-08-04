package com.example.banking_application;

import com.example.banking_application.config.ForexConfigurations;
import com.example.banking_application.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = BankingApplication.class)
@ActiveProfiles("test")
class BankingApplicationTests {

	@MockBean
	private ForexConfigurations forexService;

	@Test
	void contextLoads() {

	}

}
