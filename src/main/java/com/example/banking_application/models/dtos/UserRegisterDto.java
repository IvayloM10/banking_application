package com.example.banking_application.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRegisterDto {

    @Size(min=3,max = 40)
    @NotEmpty
    private String username;
    @Size(min=2,max = 40)
    @NotEmpty
    private String firstName;
    @Size(min=3,max = 40)
    @NotEmpty
    private String lastName;
    @Size(min=3,max = 40)
    @Email
    @NotEmpty
    private String email;
    @Size(min=3,max = 40)
    @NotEmpty
    private String password;

    @NotEmpty
    private String confirmPassword;

}
