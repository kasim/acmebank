package com.acmebank.account_manager.exceptions;

import com.acmebank.account_manager.data.models.responses.ErrorResponse;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
public class AccountManagerExceptionHandler {

    @ExceptionHandler({AccountManagerException.class})
    public final ResponseEntity<ErrorResponse> handleAccountServicesException(final AccountManagerException ase) {
        return ResponseEntity.status(BAD_REQUEST).body(new ErrorResponse(ase.getErrorCode(), ase.getParamsArray()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public final ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(final MethodArgumentNotValidException manve) {
        val errors = new HashMap<String, String>();
        manve.getBindingResult().getAllErrors().forEach((error) -> {
            val fieldName = ((FieldError) error).getField();
            val errorMsg = error.getDefaultMessage();
            errors.put(fieldName, errorMsg);
        });
        return ResponseEntity.status(BAD_REQUEST).body(errors);
    }
}
