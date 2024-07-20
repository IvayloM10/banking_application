package com.example.banking_application.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
public class LoanController {

    @GetMapping("/submit-loan")
    public String getLoanView(){
        return "takeLoan";
    }
}
