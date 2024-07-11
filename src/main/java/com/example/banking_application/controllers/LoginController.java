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

    @ModelAttribute("userLogin")
    public UserLoginDto userLoginDto(){
        return userLoginDto();
    }

    @GetMapping("/login")
    public String loginView(){
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid UserLoginDto userLoginDto, BindingResult bindingResult, RedirectAttributes redirectAttributes){

        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("newUser",userLoginDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newUser",bindingResult);
            return "redirect:/users/register";
        }

        this.userService.login(userLoginDto);


        return "/home";
    }
}
