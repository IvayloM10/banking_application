package com.example.banking_application.services.impl;

import com.example.banking_application.models.dtos.LoanDto;
import com.example.banking_application.models.entities.*;
import com.example.banking_application.repositories.*;
import com.example.banking_application.services.LoanCrudService;
import com.example.banking_application.services.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoanServiceTest {

    @Mock
    private LoanCrudService loanCrudService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private LoanServiceImpl loanService; // Replace with the actual class name

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendLoanForConfirmationSuccess() {
        // Given
        Long loanId = 1L;
        LoanDto loanDto = new LoanDto();
        loanDto.setRequesterId(2L);
        loanDto.setStatus("Pending");

        User requester = new User();
        requester.setId(2L);
        Branch branch = new Branch();
        requester.setBranch(branch);
        branch.setLoans(new ArrayList<>());

        when(loanCrudService.getCurrentLoan(loanId)).thenReturn(loanDto);
        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(modelMapper.map(loanDto, Loan.class)).thenReturn(new Loan());

        // When
        loanService.sendLoanForConfirmation(loanId);

        // Then
        assertEquals("Sent", loanDto.getStatus(), "Loan status should be updated to 'Sent'");
        verify(loanCrudService, times(1)).updateLoan(eq(loanId), any(LoanDto.class));
        verify(userRepository, times(1)).save(requester);
        verify(branchRepository, times(1)).save(branch);
        assertTrue(branch.getLoans().size() == 1, "Branch should have one loan added");
    }

    @Test
    public void testSendLoanForConfirmationRequesterNotFound() {
        // Given
        Long loanId = 1L;
        LoanDto loanDto = new LoanDto();
        loanDto.setRequesterId(2L);

        when(loanCrudService.getCurrentLoan(loanId)).thenReturn(loanDto);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            loanService.sendLoanForConfirmation(loanId);
        });
        assertEquals("Invalid Loan Request ID", thrown.getMessage());
        verify(loanCrudService, times(0)).updateLoan(anyLong(), any(LoanDto.class));
        verify(userRepository, times(0)).save(any(User.class));
        verify(branchRepository, times(0)).save(any(Branch.class));
    }

    @Test
    void testSyncUserLoans() {
        // Given
        Long userId = 1L;

        // Create test data
        LoanDto loanDto1 = new LoanDto();
        loanDto1.setRequesterId(userId);
        LoanDto loanDto2 = new LoanDto();
        loanDto2.setRequesterId(userId);

        Loan loan1 = new Loan();
        Loan loan2 = new Loan();
        List<LoanDto> loanDtos = List.of(loanDto1, loanDto2);
        List<Loan> loans = List.of(loan1, loan2);

        // Mock the behavior of the loanCrudService
        when(loanCrudService.getAllLoans()).thenReturn(loanDtos);

        // Mock the behavior of the modelMapper
        when(modelMapper.map(loanDto1, Loan.class)).thenReturn(loan1);
        when(modelMapper.map(loanDto2, Loan.class)).thenReturn(loan2);

        // When
        List<Loan> result = loanService.syncUserLoans(userId);

        // Then
        verify(loanCrudService, times(1)).getAllLoans();
        verify(modelMapper, times(2)).map(any(LoanDto.class), eq(Loan.class));
        verify(loanRepository, times(1)).saveAllAndFlush(loans);

        assertEquals(2, result.size());
        assertTrue(result.contains(loan1));
        assertTrue(result.contains(loan2), "The result should contain the mapped loan2.");
    }

    @Test
    public void testTransferMoneyToUserAccount_Success() {
        // Arrange
        Long loanId = 1L;

        LoanDto loanDto = new LoanDto();
        loanDto.setRequesterId(2L);
        loanDto.setAmount(BigDecimal.valueOf(100.0));
        loanDto.setAuthorized(false);
        loanDto.setStatus("Pending");

        User requester = new User();
        requester.setId(2L);
        requester.setCard(new Card());
        requester.setAccount(new Account());

        Card requesterCard = requester.getCard();
        requesterCard.setBalance(200.0);

        Account requesterAccount = requester.getAccount();
        requesterAccount.setBalance(500.0);

        when(loanCrudService.getCurrentLoan(loanId)).thenReturn(loanDto);
        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(accountRepository.findByUser(requester)).thenReturn(requesterAccount);

        // Act
        loanService.transferMoneyToUserAccount(loanId);

        // Assert
        assertTrue(loanDto.isAuthorized());
        assertEquals("Received!", loanDto.getStatus());
        verify(loanCrudService, times(1)).updateLoan(eq(loanId), any(LoanDto.class));
        verify(userRepository, times(1)).findById(eq(2L));
        verify(cardRepository, times(1)).save(any(Card.class));
        verify(accountRepository, times(1)).save(any(Account.class));

        assertEquals(300.0, requesterCard.getBalance());
        assertEquals(600.0, requesterAccount.getBalance());
        verify(userRepository, times(1)).save(requester);
    }

    @Test
    public void testTransferMoneyToUserAccount_RequesterNotFound() {
        // Arrange
        Long loanId = 1L;
        LoanDto loanDto = new LoanDto();
        loanDto.setRequesterId(2L);
        loanDto.setAmount(BigDecimal.valueOf(100.0));

        when(loanCrudService.getCurrentLoan(loanId)).thenReturn(loanDto);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // Act
        NullPointerException thrown = assertThrows(NullPointerException.class, () -> {
            loanService.transferMoneyToUserAccount(loanId);
        });
        //Assert
        assertEquals("Loan request could not be found!", thrown.getMessage());
        verify(loanCrudService, times(1)).updateLoan(eq(loanId), any(LoanDto.class));
        verify(userRepository, times(1)).findById(eq(2L));
        verify(cardRepository, times(0)).save(any(Card.class));
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(userRepository, times(0)).save(any(User.class));
    }
    @Test
    public void testGetCurrentLoan_Success() {
        // Given
        Long loanId = 1L;
        Loan expectedLoan = new Loan(); // Create and configure a Loan object as needed
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(expectedLoan));

        // When
        Loan result = loanService.getCurrentLoan(loanId);

        // Then
        assertNotNull(result, "The result should not be null.");
        assertEquals(expectedLoan, result, "The result should be the same as the expected loan.");
        verify(loanRepository, times(1)).findById(eq(loanId));
    }

    @Test
    public void testGetCurrentLoan_NotFound() {
        // Given
        Long loanId = 1L;
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        // When
        Loan result = loanService.getCurrentLoan(loanId);

        // Then
        assertNull(result, "The result should be null.");
        verify(loanRepository, times(1)).findById(eq(loanId));
    }

    @Test
    public void testRejectLoan() {
        // Given
        Long loanId = 1L;

        // When
        loanService.rejectLoan(loanId);

        // Then
        verify(loanCrudService, times(1)).deleteLoan(eq(loanId));
    }

    @Test
    public void testDeleteLoan() {
        // Given
        Long loanId = 1L;

        // When
        loanService.deleteLoan(loanId);

        // Then
        verify(loanRepository, times(1)).deleteById(eq(loanId));
        verify(loanCrudService, times(1)).deleteLoan(eq(loanId));
    }
}
