package com.example.banking_application.services.impl;

import com.example.banking_application.models.entities.Administrator;
import com.example.banking_application.models.entities.enums.Currency;
import com.example.banking_application.repositories.AdministratorRepository;
import com.example.banking_application.services.AdministrationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdministrationServiceImpl implements AdministrationService {
    private AdministratorRepository administratorRepository;

    public AdministrationServiceImpl(AdministratorRepository administratorRepository) {
        this.administratorRepository = administratorRepository;
    }

    @Override
    public void initialize() {
        if(this.administratorRepository.findAll().size() > 0){
            return;
        }
        List<Currency> currencies = List.of(Currency.values());
        Long id = 1L;
        for (int i = 0; i < Currency.values().length; i++){
            Administrator administrator = new Administrator();
            administrator.setId(id);
            administrator.setCurrency(currencies.get(i));
            this.administratorRepository.save(administrator);
            id++;
        }
    }
}
