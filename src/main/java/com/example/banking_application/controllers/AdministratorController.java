package com.example.banking_application.controllers;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.services.AdministrationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdministratorController {
    private CurrentUser currentUser;
    private AdministrationService administrationService;

    public AdministratorController(CurrentUser currentUser, AdministrationService administrationService) {
        this.currentUser = currentUser;
        this.administrationService = administrationService;
    }

    @GetMapping("/home")
    public String adminView(Model model){

        return "administratorHome";
    }
}
