package com.example.banking_application.services.impl;

import com.example.banking_application.models.entities.Administrator;
import com.example.banking_application.models.entities.Branch;
import com.example.banking_application.models.entities.enums.Currency;
import com.example.banking_application.repositories.AdministratorRepository;
import com.example.banking_application.repositories.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class BranchServiceImplTest {
    @Mock
    private BranchRepository mockBranchRepository;

    @Mock
    private AdministratorRepository administratorRepository;

    @InjectMocks
    private BranchServiceImpl branchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInitializeWhenBranchesExist() {
        // Set up the repository to return a non-empty list
        when( this.mockBranchRepository.findAll()).thenReturn(Arrays.asList(new Branch()));

        // Call the initialize method
        branchService.initialize();

        // Verify that save was not called, because branches already exist
        verify(this.mockBranchRepository, never()).save(any(Branch.class));
    }

    @Test
    void testInitializeWhenNoBranchesExist() {
        // Repository to create an empty list
        when(this.mockBranchRepository.findAll()).thenReturn(Arrays.asList());

        // Mock the administrator repository responses
        when(administratorRepository.findByCurrency(Currency.EUR)).thenReturn(new Administrator());
        when(administratorRepository.findByCurrency(Currency.USD)).thenReturn(new Administrator());
        when(administratorRepository.findByCurrency(Currency.JPY)).thenReturn(new Administrator());

        // Call the initialize method
        branchService.initialize();

        // check that the branches are saved
        ArgumentCaptor<Branch> branchCaptor = ArgumentCaptor.forClass(Branch.class);
        verify(this.mockBranchRepository, times(3)).save(branchCaptor.capture());

        List<Branch> savedBranches = branchCaptor.getAllValues();

        assertEquals(3, savedBranches.size());

        // checking branch attributes
        Branch euBranch = savedBranches.get(0);
        assertEquals(1L, euBranch.getId());
        assertEquals("European branch", euBranch.getName());
        assertEquals("Dublin, Ireland", euBranch.getAddress());
        assertEquals(Currency.EUR, euBranch.getCurrency());
        assertEquals("Europe", euBranch.getRegion());

        Branch usBranch = savedBranches.get(1);
        assertEquals(2L, usBranch.getId());
        assertEquals("American branch", usBranch.getName());
        assertEquals("New York, USA", usBranch.getAddress());
        assertEquals(Currency.USD, usBranch.getCurrency());
        assertEquals("North America", usBranch.getRegion());

        Branch asianBranch = savedBranches.get(2);
        assertEquals(3L, asianBranch.getId());
        assertEquals("Asian branch", asianBranch.getName());
        assertEquals("Tokyo, Japan", asianBranch.getAddress());
        assertEquals(Currency.JPY, asianBranch.getCurrency());
        assertEquals("Asia", asianBranch.getRegion());
    }
}
