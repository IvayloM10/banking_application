package com.example.banking_application.services.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;


import com.example.banking_application.config.CustomAuthenticationSuccessHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class CustomAuthenticationSuccessHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CustomAuthenticationSuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testOnAuthenticationSuccessForNoRole() throws IOException, ServletException {
        // Arrange
        GrantedAuthority noRoleAuthority = new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return "UNKNOWN";
            }
        };
        User currentUser = new User("username", "password", Collections.singleton(noRoleAuthority));
        when(authentication.getPrincipal()).thenReturn(currentUser);
        when(request.getSession()).thenReturn(session);

        // Act
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(response).sendRedirect("/");
        verify(session, never()).setAttribute(any(String.class), any(Object.class));
    }
}