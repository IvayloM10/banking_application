package com.example.banking_application.models.entities;

import com.example.banking_application.models.dtos.LoanDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name= "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,nullable = false)
    private String username;

    @Column(name="first_name",nullable = false)
    private String firstName;

    @Column(name="last_name",nullable = false)
    private String lastName;

    @Column(unique = true,nullable = false)
    private String email;

    @Column(unique = true,nullable = false)
    private String password;

    @OneToOne
    @JoinColumn(name = "account_id", unique = true)
    private Account account;

    @OneToOne(mappedBy = "cardHolder",cascade = CascadeType.ALL)
    private Card card;

    @OneToMany(mappedBy = "user",fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private List<TransactionDetails> transactions = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER)
    private List<LoanDto> loans = new ArrayList<>();

    @OneToOne(mappedBy = "cardHolder")
    private VirtualCard virtualCard;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @OneToMany(mappedBy = "maker", fetch = FetchType.EAGER)
    private List<Transaction> madeTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "receiver",fetch = FetchType.EAGER)
    private List<Transaction> receivedTransactions = new ArrayList<>();

    public String getFullName(){
        return String.join(" ", this.firstName, this.lastName);
    }
}
