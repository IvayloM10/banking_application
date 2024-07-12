package com.example.banking_application.controllers;

import com.example.banking_application.models.dtos.CardDto;
import com.example.banking_application.models.dtos.UserRegisterDto;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.services.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class RegisterController {
    private UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("newUser")
    public UserRegisterDto userRegisterDto(){
        return  new UserRegisterDto();
    }

    @GetMapping("/register")
    public String registerView(){
        return"register";
    }

    @PostMapping("/register")
    public String register(@Valid UserRegisterDto userRegisterDto, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model){

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

        model.addAttribute("userId",userRegisterDto.getUsername());
        return "redirect:/users/createCard";
    }

    @ModelAttribute("card")
    public CardDto cardDto(){
        return new CardDto();
    }
    @GetMapping("/createCard")
    public String cardCreationView(){
        return "createCard";
    }
    @PostMapping("/createCard")
    public String createCard(@Valid CardDto cardDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors() || !cardDto.getConfirmPin().equals(cardDto.getPin())){
            redirectAttributes.addFlashAttribute("card",cardDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.card",bindingResult);
            return "redirect:/users/createCard";
        }
        this.userService.createCardAndAccountForUser(cardDto);
        return "redirect:/users/login";
    }
}
