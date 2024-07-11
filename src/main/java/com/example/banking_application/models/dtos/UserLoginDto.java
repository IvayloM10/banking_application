package com.example.banking_application.models.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserLoginDto {
    @Size(min=3,max=40)
    @NotEmpty
    private String username;
    @Size(min=3,max=40)
    @NotEmpty
    private String password;
}
