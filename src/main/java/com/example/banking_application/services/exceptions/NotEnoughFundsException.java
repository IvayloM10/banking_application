package com.example.banking_application.services.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.EXPECTATION_FAILED)
@Getter
@Setter
public class NotEnoughFundsException extends  RuntimeException{
    private Object id;

    public NotEnoughFundsException(String message, Object id) {
        super(message);
        this.id = id;
    }

}
