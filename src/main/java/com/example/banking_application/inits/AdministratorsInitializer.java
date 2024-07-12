package com.example.banking_application.inits;

import com.example.banking_application.services.AdministrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdministratorsInitializer implements CommandLineRunner {

  private AdministrationService administrationService;

    public AdministratorsInitializer(AdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    @Override
    public void run(String... args) throws Exception {
        this.administrationService.initialize();
    }
}
