package com.example.banking_application.controllers;

import com.example.banking_application.models.dtos.AddLoanDto;
import com.example.banking_application.services.LoanCrudService;
import com.example.banking_application.services.LoanService;
import com.example.banking_application.services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(LoanController.class)
public class LoanControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanCrudService loanCrudService;

    @MockBean
    private LoanService loanService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username = "user", authorities = {"USER"})
    public void testGetLoanView() throws Exception {
        mockMvc.perform(get("/users/submit-loan"))
                .andExpect(status().isOk())
                .andExpect(view().name("takeLoan"));
    }

//    @Test
//    @WithMockUser(username = "user", authorities = {"USER"})
//    public void testTakeLoan_WithErrors() throws Exception {
//        MockHttpSession session = new MockHttpSession();
//        session.setAttribute("current", new User("user", "password", List.of(new SimpleGrantedAuthority("USER"))));
//
//        mockMvc.perform(post("/users/submit-loan")
//                        .session(session)
//                        .param("amount", "") // Invalid amount to trigger validation error
//                        .with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/users/submit-loan"))
//                .andExpect(flash().attributeExists("loan"))
//                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.loan"));
//    }

    @Test
    @WithMockUser(username = "user", authorities = {"USER"})
    public void testTakeLoan_Success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("current", new User("user", "password", List.of(new SimpleGrantedAuthority("USER"))));

        AddLoanDto loanDto = new AddLoanDto();
        loanDto.setAmount(BigDecimal.valueOf(10000.0));
        Mockito.when(userService.getCurrentUser("user")).thenReturn(new com.example.banking_application.models.entities.User());

        mockMvc.perform(post("/users/submit-loan")
                        .session(session)
                        .flashAttr("loan", loanDto)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        Mockito.verify(loanCrudService, Mockito.times(1)).createLoan(Mockito.any(AddLoanDto.class));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"USER"})
    public void testSendLoan() throws Exception {
        mockMvc.perform(post("/users/loans/send/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        Mockito.verify(loanService, Mockito.times(1)).sendLoanForConfirmation(1L);
    }

    @Test
    @WithMockUser(username = "user", authorities = {"USER"})
    public void testDeleteLoan() throws Exception {
        mockMvc.perform(delete("/users/loans/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        Mockito.verify(loanService, Mockito.times(1)).deleteLoan(1L);
    }
}
