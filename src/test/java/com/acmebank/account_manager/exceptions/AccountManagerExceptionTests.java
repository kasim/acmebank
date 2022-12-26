package com.acmebank.account_manager.exceptions;

import com.acmebank.account_manager.data.models.responses.ErrorResponse;
import com.acmebank.account_manager.shared.ErrorCode;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountManagerExceptionTests {

    static Stream<Arguments> errorCodesProvider() {
        ErrorCode[] errorCodes = ErrorCode.values();
        return Arrays.stream(errorCodes).map(e -> Arguments.of(e, List.of(Long.toString(1L))));
    }

    @ParameterizedTest
    @MethodSource("errorCodesProvider")
    void givenErrorCodeAndParam_whenGetParamsArray_thenParamValueShouldBePresented(final ErrorCode code, final List<String> params) {
        val accountServicesException = new AccountManagerException(code, params);
        val errorResponse = new ErrorResponse(accountServicesException.getErrorCode(), accountServicesException.getParamsArray());
        assertThat(errorResponse.getCode()).isEqualTo(code.getCode());
        if(Pattern.compile("\\{(.+?)\\}").matcher(errorResponse.getMsg()).results().count() > 0) {
            params.forEach(m -> assertThat(errorResponse.getMsg()).contains(m));
        }
    }
}
