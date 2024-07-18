package com.example.banking_application.controllers;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.entities.Administrator;
import com.example.banking_application.services.AdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.annotation.SessionScope;

@Controller
@RequestMapping("/admin")
@SessionAttributes("currentUser")
public class AdministratorController {


    private AdministrationService administrationService;

    public AdministratorController(AdministrationService administrationService) {

        this.administrationService = administrationService;
    }

    @GetMapping("/home")
    public String adminView(@SessionAttribute("currentUser") CurrentUser currentUser, Model model){
        Administrator currentAdmin = this.administrationService.getCurrentAdmin(currentUser.getId());
        model.addAttribute("transactions",currentAdmin.getBranch().getTransaction());
        model.addAttribute("admin",currentAdmin);
        return "administratorHome";
    }


}
