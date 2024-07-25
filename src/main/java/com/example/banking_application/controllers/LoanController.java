package com.example.banking_application.controllers;

import com.example.banking_application.models.dtos.AddLoanDto;
import com.example.banking_application.services.LoanCrudService;
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


    private LoanCrudService loanCrudService;

    private LoanService loanService;
    private UserService userService;


    @ModelAttribute("loan")
    @PreAuthorize("hasAuthority('USER')")
    public AddLoanDto loanDto(){
        return new AddLoanDto();
    }
    public LoanController(LoanCrudService loanCrudService, LoanService loanService, UserService userService) {
        this.loanCrudService = loanCrudService;
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
    public String takeLoan(@SessionAttribute("current") User currentUser, @Valid AddLoanDto loanDto, BindingResult bindingResult, RedirectAttributes redirectAttributes){

        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("loan",loanDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.loan",bindingResult);
            return "redirect:/users/createCard";
        }
        Long currentUserId = this.userService.getCurrentUser(currentUser.getUsername()).getId();
        loanDto.setRequesterId(currentUserId);
       this.loanCrudService.createLoan(loanDto);
       this.loanService.syncUserLoans();


        return "redirect:/home";
    }

    @PostMapping("//loans/send/{id}")
    public String sendLoan(@PathVariable Long id){
        this.loanService.sendLoanForConfirmation(id);

        return "redirect:/home";
    }
}
