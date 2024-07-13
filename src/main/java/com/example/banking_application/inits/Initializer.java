package com.example.banking_application.inits;

import com.example.banking_application.services.AdministrationService;
import com.example.banking_application.services.BranchService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Initializer implements CommandLineRunner {

  private AdministrationService administrationService;
  private BranchService branchService;

    public Initializer(AdministrationService administrationService, BranchService branchService) {
        this.administrationService = administrationService;
        this.branchService = branchService;
    }

    @Override
    public void run(String... args) throws Exception {

        this.administrationService.initialize();
        this.branchService.initialize();
    }
}
