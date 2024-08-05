package com.example.banking_application.controllers;
import com.example.banking_application.configs.TestSecurityConfig;
import com.example.banking_application.models.dtos.CardDto;
import com.example.banking_application.models.dtos.UserRegisterDto;
import com.example.banking_application.services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(RegisterController.class)
@Import(TestSecurityConfig.class)
public class RegisterControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void testRegisterPageAccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/register"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("register"));
    }

    @Test
    public void testSuccessfulRegistration() throws Exception {
        UserRegisterDto userRegisterDto = new UserRegisterDto();
        userRegisterDto.setUsername("testuser");
        userRegisterDto.setPassword("password");
        userRegisterDto.setConfirmPassword("password");
        userRegisterDto.setFirstName("John");
        userRegisterDto.setLastName("Doe");
        userRegisterDto.setEmail("john.doe@example.com");
        userRegisterDto.setRegion("USA");


        Mockito.when(userService.register(Mockito.any(UserRegisterDto.class))).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/users/register")
                        .param("username", "testuser")
                        .param("password", "password")
                        .param("confirmPassword", "password")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john.doe@example.com")
                        .param("region", "USA")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/users/createCard"));
    }



    @Test
    public void testCardCreationPageAccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/createCard"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("createCard"));
    }

    @Test
    public void testSuccessfulCardCreation() throws Exception {
        CardDto cardDto = new CardDto();
        cardDto.setPin("1234");
        cardDto.setConfirmPin("1234");
        cardDto.setCurrency("USD");
        cardDto.setCardType("DEBIT");


        Mockito.doNothing().when(userService).createCardAndAccountForUser(Mockito.any(CardDto.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/users/createCard")
                        .param("pin", "1234")
                        .param("confirmPin", "1234")
                        .param("currency", "USD")  // Add the required field
                        .param("cardType", "DEBIT") // Add the required field
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/users/login"));
    }

    @Test
    public void testFailedCardCreationDueToValidationErrors() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/users/createCard")
                        .param("pin", "1234")
                        .param("confirmPin", "wrongpin")  // Invalid PIN confirmation
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/users/createCard"))
                .andExpect(MockMvcResultMatchers.flash().attributeExists("card"));
    }
}
