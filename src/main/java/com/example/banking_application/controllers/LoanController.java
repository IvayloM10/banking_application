package com.example.banking_application.controllers;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.dtos.LoanDto;
import com.example.banking_application.services.LoanService;
import com.example.banking_application.services.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@SessionAttributes("current")
public class LoanController {


    private LoanService loanService;
    private UserService userService;


    @ModelAttribute("loan")
    @PreAuthorize("hasAuthority('USER')")
    public LoanDto loanDto(){
        return new LoanDto();
    }
    public LoanController(LoanService loanService, UserService userService) {
        this.loanService = loanService;
        this.userService = userService;
    }

    @GetMapping("/submit-loan")
    @PreAuthorize("hasAuthority('USER')")
    public String getLoanView(){
        return "takeLoan";
    }

    @PostMapping("/submit-loan")
    @PreAuthorize("hasAuthority('USER')")
    public String takeLoan(@SessionAttribute("current") User currentUser, @Valid LoanDto loanDto, BindingResult bindingResult, RedirectAttributes redirectAttributes){

        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("loan",loanDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.loan",bindingResult);
            return "redirect:/users/createCard";
        }

        this.loanService.sendLoanForConfirmation(loanDto,this.userService.getCurrentUser(currentUser.getUsername()).getId());

        return "redirect:/home";
    }
}
