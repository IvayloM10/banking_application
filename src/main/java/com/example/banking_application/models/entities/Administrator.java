package com.example.banking_application.models.entities;

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

    @OneToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @OneToMany(mappedBy = "administrator")
    private List<Account> accounts;



    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public void createACard(){
        //TODO:write implementation that should return email to the user given email
    }

    public boolean isAuthorized(){
      this.branch.getLoans();
      return false;

      //TODO: implement getting all authorized
    }
}
