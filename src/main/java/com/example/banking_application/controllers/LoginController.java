package com.example.banking_application.controllers;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.dtos.UserLoginDto;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.services.AdministrationService;
import com.example.banking_application.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class LoginController {

    private final UserService userService;
    private final AdministrationService administrationService;

    public LoginController(UserService userService, AdministrationService administrationService) {
        this.userService = userService;
        this.administrationService = administrationService;
    }

    @ModelAttribute("userLogin")
    public UserLoginDto userLoginDto(){
        return new UserLoginDto();
    }

    @GetMapping("/login")

    public String loginView(){
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("userLogin") UserLoginDto userLoginDto, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model, HttpSession session){

        boolean adminLog = this.administrationService.loginAdmin(userLoginDto);
        if(adminLog){
            model.addAttribute("currentUser",this.administrationService.getCurrentUser());
            return "redirect:/admin/home";
        }

        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("userLogin",userLoginDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userLogin",bindingResult);
            redirectAttributes.addFlashAttribute("loginError", true);
            return "redirect:/users/login";
        }

        boolean loginSuccess = this.userService.login(userLoginDto);

        if(!loginSuccess){
            redirectAttributes.addFlashAttribute("userLogin",userLoginDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userLogin",bindingResult);
            redirectAttributes.addFlashAttribute("loginError", true);
            return "redirect:/users/login";
        }

        User currentUser1 = this.userService.getCurrentUser(userLoginDto.getUsername());
        CurrentUser currentUser = new CurrentUser();
        currentUser.setId(currentUser1.getId());
        currentUser.setUsername(currentUser1.getUsername());
        currentUser.setFullName(currentUser1.getFullName());
        session.setAttribute("current", currentUser);

        System.out.println("Login successful, user: " + currentUser.getUsername());

        return "redirect:/home";
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAuthority('USER')")
    public String logout(){
        this.userService.logout();
        return "redirect:/";
    }
}
