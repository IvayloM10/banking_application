package com.example.banking_application.inits;

import com.example.banking_application.services.AdministrationService;
import com.example.banking_application.services.BranchService;
import com.example.banking_application.services.ExchangeRateService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
//@ConditionalOnProperty(name = "mvc.hiddenmethod.filter.forex.api.init-exchange-rates", havingValue = "true")
public class Initializer implements CommandLineRunner {

  private AdministrationService administrationService;
  private BranchService branchService;

  private ExchangeRateService exchangeRateService;

    public Initializer(AdministrationService administrationService, BranchService branchService, ExchangeRateService exchangeRateService) {
        this.administrationService = administrationService;
        this.branchService = branchService;
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!this.exchangeRateService.hasInitializedExRates()) {
            this.exchangeRateService.updateRates(this.exchangeRateService.fetchExRates());
        }
        this.administrationService.initialize();
        this.branchService.initialize();
    }
}
