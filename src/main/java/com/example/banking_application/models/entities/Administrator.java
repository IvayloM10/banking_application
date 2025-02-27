package com.example.banking_application.models.entities;

import com.example.banking_application.models.entities.enums.Currency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name="administrators")
@Getter
@Setter
@NoArgsConstructor
public class Administrator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true,nullable = false)
    private String password;

    @OneToOne(mappedBy = "administrator", cascade = CascadeType.ALL)
    private Branch branch;

    @OneToOne
    private Account account;

    @Column
    @Enumerated(EnumType.STRING)
    private Currency currency;


    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }


    public boolean isAuthorized(){
        this.branch.getLoans();
        return false;

        //TODO: implement getting all authorized
    }
}