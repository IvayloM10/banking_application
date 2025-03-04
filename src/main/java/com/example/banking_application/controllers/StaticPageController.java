package com.example.banking_application.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticPageController {

    @GetMapping("/FAQ")
    public String getFAQPage(){
        return "FAQ";
    }

    @GetMapping("/about-us")
    public String getAboutUsPage(){
        return "aboutUs";
    }

    @GetMapping("/customer-stories")
    public String getCustomerStories(){
        return "customerStory";
    }

    @GetMapping("/election")
    public String electionStory(){
        return "USelection.html";
    }
}
