package com.example.banking_application.models.entities;

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

//    @OneToOne
//    @JoinColumn(name = "account_id", unique = true)
//    private Account account;

    @OneToOne(mappedBy = "cardHolder")
    private Card card;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "requester",fetch = FetchType.EAGER)
    private List<Loan> loans;

    @OneToOne(mappedBy = "cardHolder")
    private VirtualCard virtualCard;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;
}
