package com.example.banking_application.controllers;

import com.example.banking_application.models.dtos.UserLoginDto;
import com.example.banking_application.services.AdministrationService;
import com.example.banking_application.services.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@SessionAttributes("currentUser")
public class LoginController {

    private UserService userService;
    private AdministrationService administrationService;

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
    public String login(@Valid UserLoginDto userLoginDto, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model){

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

        this.userService.login(userLoginDto);


        return "redirect:/home";
    }

    @PostMapping("/logout")
    public String logout(){
        this.userService.logout();
        return "redirect:/";
    }
}
