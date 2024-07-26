package com.example.banking_application.services.impl;

import com.example.banking_application.models.dtos.AddLoanDto;
import com.example.banking_application.models.dtos.LoanDto;
import com.example.banking_application.repositories.LoanRepository;
import com.example.banking_application.services.LoanCrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


import java.util.List;

@Service
public class LoanCrudServiceImpl implements LoanCrudService {

    private final Logger LOGGER = LoggerFactory.getLogger(LoanCrudService.class);
    private final RestClient loanRestClient;

    private final LoanRepository loanRepository;

    public LoanCrudServiceImpl(
            @Qualifier("loansRestClient") RestClient loanRestClient, LoanRepository loanRepository) {
        this.loanRestClient = loanRestClient;
        this.loanRepository = loanRepository;
    }

    @Override
    public void createLoan(AddLoanDto addLoanDto) {
        LOGGER.info("Creating new loan {}", addLoanDto);

       this.loanRestClient
                .post()
                .uri("/loans")
                .body(addLoanDto)
                .retrieve();
    }

    @Override
    public void updateLoan(Long id, LoanDto loanDto) {
        LOGGER.info("Updating loan with id: {}", id);

        this.loanRestClient
                .put()
                .uri("/loans/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(loanDto)
                .retrieve();
    }

    @Override
    public void deleteLoan(Long id) {

        LOGGER.info("Delete: {}", id);
        loanRestClient.delete()
                .uri("/loans/{id}", id)
                .retrieve();
        this.loanRepository.deleteById(id);
    }

    @Override
    public LoanDto getCurrentLoan(Long id) {
        return   this.loanRestClient
                .get()
                .uri("/loans/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(LoanDto.class);
    }

    @Override
    public List<LoanDto> getAllLoans() {
        LOGGER.info("Get all loans...");

        return   this.loanRestClient
                .get()
                .uri("/loans")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>(){});
    }
}
