package com.example.banking_application.controllers;

import com.example.banking_application.config.SecurityConfig;
import com.example.banking_application.configs.TestSecurityConfig;
import com.example.banking_application.models.dtos.TransactionDto;
import com.example.banking_application.models.entities.Account;
import com.example.banking_application.models.entities.Card;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.models.entities.VirtualCard;
import com.example.banking_application.models.entities.enums.Currency;
import com.example.banking_application.repositories.UserRepository;
import com.example.banking_application.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private CardService cardService;

    @MockBean
    private VirtualCardService virtualCardService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private LoanService loanService;

    @BeforeEach
    public void setUp() {

    }

//    @Test
//    @WithMockUser(username = "testuser", roles = {"USER"})
//    public void testUserHomePage() throws Exception {
//        // Setup mock data
//        User mockUser = new User();
//        mockUser.setId(1L);
//        mockUser.setUsername("testuser");
//        mockUser.setFirstName("John");
//        mockUser.setLastName("Doe");
//
//        Card card = new Card();
//        card.setCardHolder(mockUser);
//
//        VirtualCard virtualCard = new VirtualCard();
//        virtualCard.setCardNumber("333");
//        virtualCard.setCardHolder(mockUser);
//
//        // Mocking the behavior of the services
//        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
//        when(accountService.getUserAccount(mockUser)).thenReturn(new Account());
//        when(cardService.UserCard(mockUser)).thenReturn(card);
//        when(loanService.syncUserLoans(mockUser.getId())).thenReturn(Collections.emptyList());
//        when(virtualCardService.UserVirtualCard(mockUser)).thenReturn(virtualCard);
//
//        // Perform the GET request and verify the result
//        mockMvc.perform(get("/home")
//                        .with(csrf())
//                        .sessionAttr("current", new org.springframework.security.core.userdetails.User("testuser", "password", new ArrayList<>())))
//                .andExpect(status().isOk())
//                .andExpect(view().name("userHome"))
//                .andExpect(model().attributeExists("user", "account", "physicalCard", "transactions", "loans", "virtualCard"))
//                .andExpect(model().attribute("virtualCard", hasProperty("cardNumber", is("333")))); // Check specific properties
//    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testPostTransaction() throws Exception {
        // Create and populate TransactionDto object
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAmountBase(BigDecimal.valueOf(100));
        transactionDto.setDescription("Test transaction");
        transactionDto.setCardNumber("222");
        transactionDto.setPin("2222");
        transactionDto.setAccountHolderName("test");
        transactionDto.setCurrency(String.valueOf(Currency.USD));

        // Mock the service method to do nothing (indicating a successful transaction)
        doNothing().when(userService).makeTransaction(any(TransactionDto.class), anyString());

        // Perform the POST request
        mockMvc.perform(post("/transaction")
                        .param("amount", "100")
                        .param("description", "Test transaction")
                        .param("cardNumber", "222")
                        .param("pin", "2222")
                        .param("accountHolderName", "test")
                        .param("currency", "USD")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser").password("password").roles("USER")))
                .andExpect(status().is3xxRedirection())  // Expect redirection
                .andExpect(redirectedUrl("/home"));      // Expect redirection URL
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testGenerateNewNumber() throws Exception {
        mockMvc.perform(post("/users/virtualCard/generate")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser").password("password").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        // Verify that generateNewNumber was called once with the username
        verify(virtualCardService, times(1)).generateNewNumber("testuser");
    }
}
