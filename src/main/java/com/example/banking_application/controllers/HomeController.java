package com.example.banking_application.controllers;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.entities.Account;
import com.example.banking_application.models.entities.Card;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.models.entities.VirtualCard;
import com.example.banking_application.services.AccountService;
import com.example.banking_application.services.CardService;
import com.example.banking_application.services.UserService;
import com.example.banking_application.services.VirtualCardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {



    private CurrentUser currentUser;

    private UserService userService;

    private AccountService accountService;

    private CardService cardService;

    private VirtualCardService virtualCardService;

    public HomeController(CurrentUser currentUser, UserService userService, AccountService accountService, CardService cardService, VirtualCardService virtualCardService) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.accountService = accountService;
        this.cardService = cardService;
        this.virtualCardService = virtualCardService;
    }

    @GetMapping("/home")
    public String userHomePage(Model model){

        User loggedUser = this.userService.getCurrentUser();
        Account userAccount = this.accountService.getUserAccount(loggedUser);
        model.addAttribute("user", loggedUser);
        model.addAttribute("account", userAccount);
        Card userCard = this.cardService.UserCard(loggedUser);
        model.addAttribute("physicalCard", userCard);
       //TODO: for table integration model.addAttribute("transactions",loggedUser.getTransactions());
        VirtualCard virtualCard = this.virtualCardService.UserVirtualCard(loggedUser);
        model.addAttribute("virtualCard",loggedUser.getVirtualCard());
        return"userHome";
    }



}
