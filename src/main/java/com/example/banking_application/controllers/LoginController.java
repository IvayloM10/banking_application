package com.example.banking_application.controllers;

import com.example.banking_application.models.dtos.UserLoginDto;
import com.example.banking_application.services.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class LoginController {

    private UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
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
    public String login(@Valid UserLoginDto userLoginDto, BindingResult bindingResult, RedirectAttributes redirectAttributes){

        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("userLogin",userLoginDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userLogin",bindingResult);
            redirectAttributes.addFlashAttribute("loginError", true);
            return "redirect:/users/login";
        }

        this.userService.login(userLoginDto);


        return "redirect:/home";
    }
}
