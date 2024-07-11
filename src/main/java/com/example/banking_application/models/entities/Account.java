package com.example.banking_application.models.entities;

import com.example.banking_application.models.entities.enums.Currency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="accounts")
@Getter
@Setter
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;
    @OneToOne
    @JoinColumn(name = "user_id",unique = true,nullable = false)
    private User user;

    @Column(name ="account_number", unique = true,nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private double balance;

    @ManyToOne
    private Administrator administrator;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public void reduceAccount(int sum){
        this.balance -= sum;
    }


    public void addIntoAccount(int sum){
        this.balance += sum;
    }


}
