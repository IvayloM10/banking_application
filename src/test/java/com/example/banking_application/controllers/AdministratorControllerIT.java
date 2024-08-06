package com.example.banking_application.controllers;


import com.example.banking_application.configs.TestSecurityConfig;
import com.example.banking_application.models.entities.Administrator;
import com.example.banking_application.models.entities.Account;
import com.example.banking_application.models.entities.Branch;
import com.example.banking_application.services.AdministrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
public class AdministratorControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdministrationService administrationService;

    private User mockAdminUser;

    @BeforeEach
    public void setup() {
        mockAdminUser = new User(
                "admin",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ADMIN"))
        );
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testAdminView() throws Exception {
        Administrator mockAdmin = new Administrator();
        mockAdmin.setAccount(new Account());
        Branch branch = new Branch();
        branch.setTransaction(Collections.emptyList());
        branch.setLoans(Collections.emptyList());
        mockAdmin.setBranch(branch);

        Mockito.when(administrationService.getCurrentAdmin("admin")).thenReturn(mockAdmin);

        mockMvc.perform(MockMvcRequestBuilders.get("/admin/home"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attributeExists("account"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("transactions"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("loans"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("admin"))
                .andExpect(MockMvcResultMatchers.view().name("administratorHome"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testApproveTransaction() throws Exception {

        Mockito.doNothing().when(administrationService).approveTransaction(Mockito.anyLong(), Mockito.anyString());

        // check the POST request with CSRF
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/transactions/approve/1")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/admin/home"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testRejectTransaction() throws Exception {
        Mockito.doNothing().when(administrationService).rejectTransaction(Mockito.anyLong(), Mockito.anyString());

        mockMvc.perform(MockMvcRequestBuilders.post("/admin/transactions/reject/1") .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/admin/home"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testApproveLoan() throws Exception {
        Mockito.doNothing().when(administrationService).approveLoan(Mockito.anyLong(), Mockito.anyString());

        mockMvc.perform(MockMvcRequestBuilders.post("/admin/loans/approve/1") .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/admin/home"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testRejectLoan() throws Exception {
        // Create a mock session and set the "current" attribute
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("current", new User("admin", "password", List.of(new SimpleGrantedAuthority("ADMIN"))));


        Mockito.doNothing().when(administrationService).rejectLoan(Mockito.anyLong(), Mockito.anyString());

        // check the delete request
        mockMvc.perform(MockMvcRequestBuilders.delete("/admin/loans/reject/1")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .session(session))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/admin/home"));
    }
}

