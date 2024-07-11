package com.example.banking_application.controllers;

import com.example.banking_application.models.dtos.UserRegisterDto;
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
public class RegisterController {
    private UserService userService;

    @ModelAttribute("newUser")
    public UserRegisterDto userRegisterDto(){
        return  new UserRegisterDto();
    }

    @GetMapping("/register")
    public String registerView(){
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid UserRegisterDto userRegisterDto, BindingResult bindingResult, RedirectAttributes redirectAttributes){

        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("newUser",userRegisterDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newUser",bindingResult);
            return "redirect:/users/register";
        }
        boolean registerSuccess = this.userService.register(userRegisterDto);

        if(!registerSuccess){
            redirectAttributes.addFlashAttribute("newUser", userRegisterDto);
            redirectAttributes.addFlashAttribute("userPassMismatch", true);

            return "redirect:/users/register";
        }

        return "redirect:/users/login";
    }
}
