package com.acmebank.account_manager.data.models.responses;

import com.acmebank.account_manager.shared.ErrorCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static java.text.MessageFormat.format;

@Getter
public class ErrorResponse {
    @JsonProperty
    int code;
    @JsonProperty
    String msg;
    public ErrorResponse(ErrorCode errorCode, String... params){
        this.code = errorCode.getCode();
        this.msg = format(errorCode.getMsg(), params);
    }
}
