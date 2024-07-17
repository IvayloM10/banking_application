package com.example.banking_application.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Controller
public class LanguageController {
    private final LocaleResolver localeResolver;

    public LanguageController(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @GetMapping("/change-language")
    public String changeLanguage(@RequestParam("lang") String lang, HttpServletRequest request, HttpServletResponse response) {
        Locale locale;
        switch (lang.toLowerCase()) {
            case "bg":
                locale = new Locale("bg");
                break;
            case "de":
                locale = new Locale("de");
                break;
            case "en":
            default:
                locale = new Locale("en");
                break;
        }
        localeResolver.setLocale(request, response, locale);

        String referer = request.getHeader("Referer");

        if (referer != null) {
            return "redirect:" + referer;
        } else {
            return "redirect:/"; 
        }
    }
}
