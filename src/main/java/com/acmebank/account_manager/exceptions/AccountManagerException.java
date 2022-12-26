package com.acmebank.account_manager.exceptions;

import com.acmebank.account_manager.shared.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class AccountManagerException extends RuntimeException{
    private final ErrorCode errorCode;
    private final List<String> params;

    public String[] getParamsArray() {
        return this.params.toArray(new String[0]);
    }
}
