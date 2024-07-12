package com.example.banking_application.services.impl;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.dtos.UserLoginDto;
import com.example.banking_application.models.entities.Administrator;
import com.example.banking_application.models.entities.enums.Currency;
import com.example.banking_application.repositories.AdministratorRepository;
import com.example.banking_application.services.AdministrationService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdministrationServiceImpl implements AdministrationService {
    private AdministratorRepository administratorRepository;
    private CurrentUser currentUser;

    private ModelMapper modelMapper;

    public AdministrationServiceImpl(AdministratorRepository administratorRepository, ModelMapper modelMapper) {
        this.administratorRepository = administratorRepository;
        this.modelMapper = modelMapper;
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
            administrator.setUsername(String.join(" ", String.valueOf(currencies.get(i))));
            administrator.setPassword(String.join("",String.valueOf(i),String.valueOf(i),String.valueOf(i),String.valueOf(i)));
            administrator.setCurrency(currencies.get(i));
            this.administratorRepository.save(administrator);
            //TODO find branch and set it
            id++;
        }
    }

    @Override
    public boolean loginAdmin(UserLoginDto userLoginDto) {
        Optional<Administrator> searchAdministrator = this.administratorRepository.findByUsername(userLoginDto.getUsername());

        if(searchAdministrator.isEmpty() || !searchAdministrator.get().getPassword().equals(userLoginDto.getPassword())){
            return false;
        }
        Administrator administrator = searchAdministrator.get();
        this.currentUser = this.modelMapper.map(userLoginDto, CurrentUser.class);
        this.currentUser.setId(administrator.getId());
        return true;
    }
}
