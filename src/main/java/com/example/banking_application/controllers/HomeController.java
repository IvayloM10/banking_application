package com.example.banking_application.controllers;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {



    private CurrentUser currentUser;

    private UserService userService;

    public HomeController(CurrentUser currentUser, UserService userService) {
        this.currentUser = currentUser;
        this.userService = userService;
    }

    @GetMapping("/home")
    public String userHomePage(Model model){

        User loggedUser = this.userService.getCurrentUser(this.currentUser.getId());
        model.addAttribute("account", loggedUser.getAccount());
        model.addAttribute("physicalCard", loggedUser.getCard());
       //TODO: for table integration model.addAttribute("transactions",loggedUser.getTransactions());
        model.addAttribute("virtualCard",loggedUser.getVirtualCard());
        return"userHome";
    }



}
