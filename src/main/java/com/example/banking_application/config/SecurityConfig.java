package com.example.banking_application.config;


import com.example.banking_application.repositories.AdministratorRepository;
import com.example.banking_application.repositories.UserRepository;

import com.example.banking_application.services.impl.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
@EnableWebSecurity
public class SecurityConfig{

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository, AdministratorRepository administratorRepository) {
        return new CustomUserDetailsService(userRepository, administratorRepository);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        return new CustomAuthenticationProvider(userDetailsService, passwordEncoder);
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                //removing the session management as per default it does not allow my session
//                .sessionManagement(sessionManagement -> sessionManagement
//                        .sessionFixation(sessionFixation -> sessionFixation.none())
//                )
                .authorizeHttpRequests(authorize -> authorize
                // Resources accessible to everyone
                .requestMatchers("/", "/users/login", "/users/register","/about-us", "/FAQ","/change-language","/users/createCard","/customer-stories","/election").permitAll()
                          // permission for css, js and etc.
                        .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()
                        // Accessible to logged-in users
                .requestMatchers("/home", "/users/submit-loan", "/transaction","/loans/delete/**","/loans/send/**","/users/virtualCard/generate").hasAuthority("USER")
                // Accessible to logged-in administrators
                .requestMatchers("/admin/home","/transactions/approve/**", "/transactions/reject/**","/loans/reject/**","/loans/approve/**").hasAuthority("ADMIN")
                //Other request needs to be authenticated
                .anyRequest().authenticated()
        )
                .formLogin(formLogin -> formLogin
                        .loginPage("/users/login")
                        .loginProcessingUrl("/users/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        //here check for whether admin logs in or user
                        .successHandler(customAuthenticationSuccessHandler())
                        //when you log incorrectly it is sent to index page
                        .failureUrl("/")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/users/logout"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                );


        return httpSecurity.build();
    }

}