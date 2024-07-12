package com.example.banking_application.inits;

import com.example.banking_application.services.BranchService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BranchInitializer implements CommandLineRunner {

    private BranchService branchService;

    public BranchInitializer(BranchService branchService) {
        this.branchService = branchService;
    }

    @Override
    public void run(String... args) throws Exception {
      this.branchService.initialize();
    }
}
