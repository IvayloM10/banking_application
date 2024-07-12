package com.example.banking_application.services.impl;

import com.example.banking_application.models.entities.Administrator;
import com.example.banking_application.models.entities.Branch;
import com.example.banking_application.models.entities.enums.Currency;
import com.example.banking_application.repositories.AdministratorRepository;
import com.example.banking_application.repositories.BranchRepository;
import com.example.banking_application.services.BranchService;
import org.springframework.stereotype.Service;

@Service
public class BranchServiceImpl implements BranchService {

    private BranchRepository branchRepository;
    private AdministratorRepository administratorRepository;

    public BranchServiceImpl(BranchRepository branchRepository, AdministratorRepository administratorRepository) {
        this.branchRepository = branchRepository;
        this.administratorRepository = administratorRepository;
    }

    @Override
    public void initialize() {
        if(this.branchRepository.findAll().size() > 0){
            return;
        }
        Branch euBranch = new Branch();
        euBranch.setId(1L);
        euBranch.setName("European branch");
        euBranch.setAddress("Dublin, Ireland");
        euBranch.setCurrency(Currency.EUR);
    }

    private Administrator findRightAdministrator(Currency currency){
        return this.administratorRepository.findByCurrency(currency);
    }
}
