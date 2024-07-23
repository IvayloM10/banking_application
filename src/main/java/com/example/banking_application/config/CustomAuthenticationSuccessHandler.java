package com.example.banking_application.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Collection;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();


        HttpSession session = request.getSession();
        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
            User currentUser = (User) authentication.getPrincipal();
            //Adding session due to the fact I lose session to default settings of Spring Security
            session.setAttribute("current", currentUser);
            response.sendRedirect("/admin/home");
        } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("USER"))) {
            User currentUser = (User) authentication.getPrincipal();
            session.setAttribute("current", currentUser);
            response.sendRedirect("/home");
        } else {
            // When there is no such user send it there
            response.sendRedirect("/");
        }
    }
}
