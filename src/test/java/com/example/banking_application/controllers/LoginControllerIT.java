package com.example.banking_application.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.banking_application.configs.TestSecurityConfig;
import com.example.banking_application.models.dtos.UserLoginDto;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.services.AdministrationService;
import com.example.banking_application.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class LoginControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AdministrationService administrationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoginView() throws Exception {
        mockMvc.perform(get("/users/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    public void testLoginSuccess() throws Exception {
        // Setup
        UserLoginDto userLoginDto = new UserLoginDto();
        userLoginDto.setUsername("user");
        userLoginDto.setPassword("password");


        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("user");
        mockUser.setPassword("password");
        mockUser.setFirstName("First");
        mockUser.setLastName("Last");


        Mockito.when(userService.login(Mockito.any(UserLoginDto.class))).thenReturn(true);
        Mockito.when(userService.getCurrentUser(Mockito.anyString())).thenReturn(mockUser);

        // call the POST request
        mockMvc.perform(post("/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "user")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        // check that the login method was called
        Mockito.verify(userService).login(Mockito.any(UserLoginDto.class));
    }

    @Test
    public void testLoginFailure() throws Exception {
        // Setup
        UserLoginDto userLoginDto = new UserLoginDto();
        userLoginDto.setUsername("user");
        userLoginDto.setPassword("wrongpassword");


        Mockito.when(userService.login(Mockito.any(UserLoginDto.class))).thenReturn(false);

        // call the POST request with incorrect credentials
        mockMvc.perform(post("/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "user")
                        .param("password", "wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"))
                .andExpect(flash().attributeExists("loginError")); // Expecting a flash attribute for the error
    }
    @Test
    public void testLogout() throws Exception {
        mockMvc.perform(post("/users/logout")
                        .with(csrf())
                        .with(user("user").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}
