package com.example.banking_application.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.test.web.servlet.MockMvc;



import java.util.Locale;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LanguageController.class)
public class LanguageControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocaleResolver localeResolver;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testChangeLanguageToBulgarian() throws Exception {
        Mockito.doNothing().when(localeResolver).setLocale(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class), Mockito.eq(new Locale("bg")));

        mockMvc.perform(get("/change-language")
                        .param("lang", "bg")
                        .header("Referer", "/previous-page")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())) // Add CSRF token
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/previous-page"));

        Mockito.verify(localeResolver, Mockito.times(2)).setLocale(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class), Mockito.eq(new Locale("bg")));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testChangeLanguageToGerman() throws Exception {
        Mockito.doNothing().when(localeResolver).setLocale(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class), Mockito.eq(new Locale("de")));

        mockMvc.perform(get("/change-language")
                        .param("lang", "de")
                        .header("Referer", "/another-page"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/another-page"));

        // Verify that setLocale is called twice
        Mockito.verify(localeResolver, Mockito.times(2)).setLocale(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class), Mockito.eq(new Locale("de")));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testChangeLanguageToDefaultEnglish() throws Exception {
        Mockito.doNothing().when(localeResolver).setLocale(
                Mockito.any(HttpServletRequest.class),
                Mockito.any(HttpServletResponse.class),
                Mockito.eq(new Locale("en"))
        );

        // Perform the request with an unknown locale
        mockMvc.perform(get("/change-language")
                        .param("lang", "unknown") // Should fall back to default "en"
                        .header("Referer", "/home")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())) // Include CSRF token
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        // Verify that setLocale is called once with the default locale
        Mockito.verify(localeResolver, Mockito.times(1)).setLocale(
                Mockito.any(HttpServletRequest.class),
                Mockito.any(HttpServletResponse.class),
                Mockito.eq(new Locale("en"))
        );
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testChangeLanguageWithoutReferer() throws Exception {
        Mockito.doNothing().when(localeResolver).setLocale(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class), Mockito.eq(new Locale("en")));

        mockMvc.perform(get("/change-language")
                        .param("lang", "en"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Verify that setLocale is called twice
        Mockito.verify(localeResolver, Mockito.times(2)).setLocale(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class), Mockito.eq(new Locale("en")));
    }
}
