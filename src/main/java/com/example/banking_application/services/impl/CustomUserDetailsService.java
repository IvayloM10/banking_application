package com.example.banking_application.services.impl;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.entities.Administrator;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.repositories.AdministratorRepository;
import com.example.banking_application.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    public CustomUserDetailsService(UserRepository userRepository, AdministratorRepository administratorRepository) {

        this.userRepository = userRepository;
        this.administratorRepository = administratorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            //UserDetails for the security context is created with role for the user
            return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), Collections.singleton(new SimpleGrantedAuthority("USER")));
        }

        Administrator admin = administratorRepository.findByUsername(username).orElse(null);
        if (admin != null) {
            //UserDetails for the security context is created with role for the user
            return new org.springframework.security.core.userdetails.User(admin.getUsername(), admin.getPassword(), Collections.singleton(new SimpleGrantedAuthority("ADMIN")));
        }
        throw new UsernameNotFoundException("User not found");
    }
}
