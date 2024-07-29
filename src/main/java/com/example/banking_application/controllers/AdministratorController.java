package com.example.banking_application.controllers;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.entities.Administrator;
import com.example.banking_application.services.AdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.SessionScope;

@Controller
@RequestMapping("/admin")
@SessionAttributes("current")
public class AdministratorController {


    private final AdministrationService administrationService;



    public AdministratorController(AdministrationService administrationService) {

        this.administrationService = administrationService;
    }

    @GetMapping("/home")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String adminView(@SessionAttribute("current") User currentUser, Model model){
        Administrator currentAdmin = this.administrationService.getCurrentAdmin(currentUser.getUsername());
        model.addAttribute("account", currentAdmin.getAccount());
        model.addAttribute("transactions",currentAdmin.getBranch().getTransaction());
        model.addAttribute("loans",currentAdmin.getBranch().getLoans());
        model.addAttribute("admin",currentAdmin);
        return "administratorHome";
    }

    @PostMapping("/transactions/approve/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String approveTransaction(@SessionAttribute("current") User currentUser,@PathVariable Long id) {
        this.administrationService.approveTransaction(id, currentUser.getUsername());
        return "redirect:/admin/home";
    }

    @PostMapping("/transactions/reject/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String rejectTransaction(@SessionAttribute("current") User currentUser,@PathVariable Long id) {
        this.administrationService.rejectTransaction(id,currentUser.getUsername());
        return "redirect:/admin/home";
    }

    @PostMapping("/loans/approve/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String approveLoan(@SessionAttribute("current") User currentUser, @PathVariable Long id){
        this.administrationService.approveLoan(id, currentUser.getUsername());
        return "redirect:/admin/home";
    }


    @DeleteMapping("/loans/reject/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String rejectLoan(@SessionAttribute("current") User currentUser, @PathVariable Long id){
        this.administrationService.rejectLoan(id, currentUser.getUsername());
        return "redirect:/admin/home";
    }
}
