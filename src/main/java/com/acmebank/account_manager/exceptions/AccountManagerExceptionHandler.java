package com.acmebank.account_manager.exceptions;

import com.acmebank.account_manager.data.models.responses.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

import static com.acmebank.account_manager.shared.ErrorCode.GENERAL_ERROR;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
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

    @ExceptionHandler({LockAcquisitionException.class})
    public final ResponseEntity<ErrorResponse> handleLockAcquisitionException(final LockAcquisitionException lae) {
        log.error("Encountered LockAcquisitionException {}, it seems there is concurrency occurred in database.", lae);
        return ResponseEntity.status(BAD_REQUEST).body(new ErrorResponse(GENERAL_ERROR, new String[0]));
    }
}
