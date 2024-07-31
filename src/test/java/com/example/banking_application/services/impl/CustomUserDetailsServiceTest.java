package com.example.banking_application.services.impl;

import com.example.banking_application.models.entities.Administrator;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.repositories.AdministratorRepository;
import com.example.banking_application.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomUserDetailsServiceTest {

    private static final String USER_USERNAME ="test";

    private static final String USER_PASSWORD = "pass123";

    private static final String ADMIN_USERNAME = "testAdmin";

    private static final String ADMIN_PASSWORD = "admin123";

    private static final String NON_EXISTENT_USERNAME = "noExist";

    private CustomUserDetailsService toTest;

    private UserRepository mockUserRepository;

    private AdministratorRepository mockAdministratorRepository;

    @BeforeEach
    void setUp(){
        this.mockUserRepository = Mockito.mock(UserRepository.class);
        this.mockAdministratorRepository = Mockito.mock(AdministratorRepository.class);
        this.toTest = new CustomUserDetailsService(this.mockUserRepository, this.mockAdministratorRepository);
    }

    @Test
    void testLoadUserByUsername_FoundUser(){
        //creating a user - Arrange
        User mockUser = new User();
        mockUser.setUsername(USER_USERNAME);
        mockUser.setPassword(USER_PASSWORD);
        when(this.mockUserRepository.findByUsername(USER_USERNAME)).thenReturn(Optional.of(mockUser));

        //Action - calling the method for check
        UserDetails userDetails = this.toTest.loadUserByUsername(USER_USERNAME);

        //Assert - check if the result of the method returns the correct properties
        assertNotNull(userDetails);
        assertEquals(USER_USERNAME, userDetails.getUsername());
        assertEquals(USER_PASSWORD, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("USER")));

        verify(this.mockUserRepository, times(1)).findByUsername(USER_USERNAME);
        // making sure that this never calls in the method to override it and find admin or get an admin first if present
        verify(this.mockAdministratorRepository, never()).findByUsername(anyString());
    }

    @Test
    void testLoadUserByUsername_FoundAdmin(){
        //creating an admin - Arrange
        Administrator mockAdmin = new Administrator();
        mockAdmin.setUsername(ADMIN_USERNAME);
        mockAdmin.setPassword(ADMIN_PASSWORD);
        when(this.mockAdministratorRepository.findByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(mockAdmin));

        //call the method - Action
        UserDetails userDetails = this.toTest.loadUserByUsername(ADMIN_USERNAME);

        //Assert - check the result
        assertNotNull(userDetails);
        assertEquals(ADMIN_USERNAME, userDetails.getUsername());
        assertEquals(ADMIN_PASSWORD, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN")));

        verify(this.mockAdministratorRepository, times(1)).findByUsername(ADMIN_USERNAME);
    }

    @Test
    void testLoadUserOrAdminByUsername_NotFound() {
        // Arrange - call the methods
        when(this.mockUserRepository.findByUsername(NON_EXISTENT_USERNAME)).thenReturn(Optional.empty());
        when(this.mockAdministratorRepository.findByUsername(NON_EXISTENT_USERNAME)).thenReturn(Optional.empty());

        // Action - search for the error class that is excepted
        assertThrows(UsernameNotFoundException.class, () -> this.toTest.loadUserByUsername(NON_EXISTENT_USERNAME));

        //Assert - verify there are any users created, however the one searched is non-existent
        verify(this.mockUserRepository, times(1)).findByUsername(NON_EXISTENT_USERNAME);
        verify(this.mockAdministratorRepository, times(1)).findByUsername(NON_EXISTENT_USERNAME);
    }



}
