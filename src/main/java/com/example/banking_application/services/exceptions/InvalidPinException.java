package com.example.banking_application.services.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.EXPECTATION_FAILED)
@Getter
@Setter
public class InvalidPinException extends RuntimeException{
    private String cardNumber;

    public InvalidPinException(String message,String cardNumber) {
       super(message);
        this.cardNumber = cardNumber;
    }
}
