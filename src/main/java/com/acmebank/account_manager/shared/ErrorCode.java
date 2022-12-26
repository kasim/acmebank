package com.acmebank.account_manager.shared;

import lombok.Getter;

@Getter
public enum ErrorCode {
    GENERAL_ERROR(1000, "Service is busy or unavailable, please try again later."),
    ACCOUNT_NOT_FOUND(1001, "Specified account {0} not found!"),
    INSUFFICIENT_FUND(1002, "Insufficient fund in account {0}!"),
    SAME_ACCOUNT(1003, "Cannot transfer from same account!");

    final int code;
    final String msg;

    ErrorCode(final int code, final String msg) {
        this.code = code;
        this.msg = msg;
    }
}
