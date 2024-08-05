package com.example.banking_application;


import com.example.banking_application.config.ForexConfigurations;
import com.example.banking_application.configs.TestRestConfigs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.when;


@SpringBootTest(classes = BankingApplication.class)
@ActiveProfiles("test")
class BankingApplicationTests {

	@MockBean
	private ForexConfigurations forexConfigurations;


	@Test
	void contextLoads() {
	}

}
