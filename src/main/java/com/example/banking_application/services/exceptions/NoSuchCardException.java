package com.example.banking_application.services.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
@Getter
@Setter
public class NoSuchCardException extends RuntimeException {
    private String cardNumber;

    public NoSuchCardException(String message,String cardNumber) {
        super(message);
        this.cardNumber = cardNumber;
    }
}
