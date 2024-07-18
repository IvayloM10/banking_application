package com.example.banking_application.controllers;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.dtos.TransactionDto;
import com.example.banking_application.models.entities.Account;
import com.example.banking_application.models.entities.Card;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.models.entities.VirtualCard;
import com.example.banking_application.services.AccountService;
import com.example.banking_application.services.CardService;
import com.example.banking_application.services.UserService;
import com.example.banking_application.services.VirtualCardService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        model.addAttribute("transactions",loggedUser.getTransactions());
        VirtualCard virtualCard = this.virtualCardService.UserVirtualCard(loggedUser);
        model.addAttribute("virtualCard",loggedUser.getVirtualCard());
        return"userHome";
    }

    @ModelAttribute("transaction")
    public TransactionDto transactionDto(){
        return new TransactionDto();
    }
    @GetMapping("/transaction")
    public String transactionView(){
        return "makeTransaction";
    }

    @PostMapping("/transaction")
    public String transaction(@Valid TransactionDto transactionDto, BindingResult bindingResult, RedirectAttributes redirectAttributes){

        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("transaction",transactionDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.transaction",bindingResult);
            return "redirect:/transaction";
        }

        this.userService.makeTransaction( transactionDto);
        User currentUser1 = this.userService.getCurrentUser();
        Account userAccount = this.accountService.getUserAccount(currentUser1);
        System.out.println(userAccount.getBalance());
        return"redirect:/home";
    }

}
