package com.example.banking_application.controllers;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.dtos.TransactionDto;
import com.example.banking_application.models.entities.Account;
import com.example.banking_application.models.entities.Card;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.models.entities.VirtualCard;
import com.example.banking_application.repositories.UserRepository;
import com.example.banking_application.services.AccountService;
import com.example.banking_application.services.CardService;
import com.example.banking_application.services.UserService;
import com.example.banking_application.services.VirtualCardService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@SessionAttributes("current")
public class HomeController {

    private UserService userService;

    private AccountService accountService;

    private CardService cardService;

    private VirtualCardService virtualCardService;

    private UserRepository userRepository;

    public HomeController(UserService userService, AccountService accountService, CardService cardService, VirtualCardService virtualCardService, UserRepository userRepository) {
        this.userService = userService;
        this.accountService = accountService;
        this.cardService = cardService;
        this.virtualCardService = virtualCardService;
        this.userRepository = userRepository;
    }

    @GetMapping("/home")
    @PreAuthorize("hasAuthority('USER')")
    public String userHomePage(@SessionAttribute("current") org.springframework.security.core.userdetails.User currentUser, Model model){
        String Username = currentUser.getUsername();
        User loggedUser = this.userRepository.findByUsername(currentUser.getUsername()).orElse(null);
        Account userAccount = this.accountService.getUserAccount(loggedUser);
        model.addAttribute("user", loggedUser);
        model.addAttribute("account", userAccount);
        Card userCard = this.cardService.UserCard(loggedUser);
        model.addAttribute("physicalCard", userCard);
        model.addAttribute("transactions",loggedUser.getTransactions());
        model.addAttribute("loans", loggedUser.getLoans());
        VirtualCard virtualCard = this.virtualCardService.UserVirtualCard(loggedUser);
        model.addAttribute("virtualCard",loggedUser.getVirtualCard());
        return"userHome";
    }

    @ModelAttribute("transaction")
    @PreAuthorize("hasAuthority('USER')")
    public TransactionDto transactionDto(){
        return new TransactionDto();
    }
    @GetMapping("/transaction")
    @PreAuthorize("hasAuthority('USER')")
    public String transactionView(){
        return "makeTransaction";
    }

    @PostMapping("/transaction")
    @PreAuthorize("hasAuthority('USER')")
    public String transaction(@SessionAttribute("current") org.springframework.security.core.userdetails.User currentUser,@Valid TransactionDto transactionDto, BindingResult bindingResult, RedirectAttributes redirectAttributes){

        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("transaction",transactionDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.transaction",bindingResult);
            return "redirect:/transaction";
        }

        this.userService.makeTransaction( transactionDto, currentUser.getUsername());
        return"redirect:/home";
    }

}
