package com.acmebank.account_manager.controllers;

import com.acmebank.account_manager.data.models.requests.TransferRequest;
import com.acmebank.account_manager.data.models.responses.AccountBalance;
import com.acmebank.account_manager.data.models.responses.ErrorResponse;
import com.acmebank.account_manager.data.models.responses.Transaction;
import com.acmebank.account_manager.exceptions.AccountManagerException;
import com.acmebank.account_manager.services.AccountManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.acmebank.account_manager.shared.AccountType.CURRENT;
import static com.acmebank.account_manager.shared.AccountType.SAVING;
import static com.acmebank.account_manager.shared.Currency.HKD;
import static com.acmebank.account_manager.shared.Endpoint.GET_ACCOUNT;
import static com.acmebank.account_manager.shared.Endpoint.TRANSFER;
import static com.acmebank.account_manager.shared.ErrorCode.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountManagerController.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AccountManagerControllerTests {
    private final MockMvc mockMvc;
    @MockBean
    private final AccountManager accountManager;
    static final long currentAccountId = 12345678L;
    static final long savingAccountId = 88888888L;
    static final long invalidAccountId = 123456789L;
    static final long insufficientFundAccountId = 11111111L;
    static final long lockAccountId = 99999999L;
    static final BigDecimal balance = BigDecimal.valueOf(1000000);
    static final AccountBalance currentAccountBalance = AccountBalance.of(balance, HKD, CURRENT);
    final AccountManagerException invalidAccountManagerException = new AccountManagerException(ACCOUNT_NOT_FOUND, List.of(Long.toString(invalidAccountId)));
    static final ErrorResponse invalidAccountErrorResponse = new com.acmebank.account_manager.data.models.responses.ErrorResponse(ACCOUNT_NOT_FOUND, new String[]{Long.toString(invalidAccountId)});
    static final ErrorResponse insufficientFundErrorResponse = new com.acmebank.account_manager.data.models.responses.ErrorResponse(INSUFFICIENT_FUND, new String[]{Long.toString(insufficientFundAccountId)});
    static final ErrorResponse sameAccountErrorResponse = new com.acmebank.account_manager.data.models.responses.ErrorResponse(SAME_ACCOUNT, new String[]{});
    static final ErrorResponse lockAccountErrorResponse = new com.acmebank.account_manager.data.models.responses.ErrorResponse(GENERAL_ERROR, new String[]{});
    private final ObjectMapper mapper;

    static Stream<Arguments> getAccountBalanceParamsProvider() {
        return Stream.of(
                Arguments.of(currentAccountId, OK, currentAccountBalance),
                Arguments.of(invalidAccountId, BAD_REQUEST, invalidAccountErrorResponse),
                Arguments.of(lockAccountId, BAD_REQUEST, lockAccountErrorResponse)
        );
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("getAccountBalanceParamsProvider")
    void givenAccountServicesController_whenCallsGetAccountBalance_thenExpectedMatched(final long accountId, final HttpStatus httpStatus, final Object responseObject) {
        when(accountManager.getAccountBalance(currentAccountId))
                .thenReturn(ResponseEntity.ok(currentAccountBalance));
        doThrow(invalidAccountManagerException)
                .when(accountManager).getAccountBalance(invalidAccountId);
        doThrow(new LockAcquisitionException("Account Locked!", new SQLException()))
                .when(accountManager).getAccountBalance(lockAccountId);
        mockMvc.perform(get(GET_ACCOUNT, accountId))
                .andExpect(status().is(httpStatus.value()))
                .andExpect(content().json(mapper.writeValueAsString(responseObject)));
    }

    static Stream<Arguments> getTransferResponseProvider() {
        val validAmount = BigDecimal.valueOf(100.00);
        val validRequest = new TransferRequest(currentAccountId, savingAccountId, validAmount, HKD.name());
        val fromAmount = balance.subtract(validAmount);
        val toAmount = balance.add(validAmount);
        val validResponse = new Transaction(
                1L,
                currentAccountId,
                AccountBalance.of(fromAmount, HKD, CURRENT),
                savingAccountId,
                AccountBalance.of(toAmount, HKD, SAVING),
                Timestamp.valueOf(LocalDateTime.now()));
        val inValidRequest = new TransferRequest(invalidAccountId, savingAccountId, validAmount, HKD.name());
        val insufficientFundRequest = new TransferRequest(insufficientFundAccountId, savingAccountId, validAmount, HKD.name());
        val sameAccountRequest = new TransferRequest(savingAccountId, savingAccountId, validAmount, HKD.name());
        return Stream.of(
                Arguments.of(validRequest, ResponseEntity.status(CREATED).body(validResponse)),
                Arguments.of(inValidRequest, ResponseEntity.status(BAD_REQUEST).body(invalidAccountErrorResponse)),
                Arguments.of(insufficientFundRequest, ResponseEntity.status(BAD_REQUEST).body(insufficientFundErrorResponse)),
                Arguments.of(sameAccountRequest, ResponseEntity.status(BAD_REQUEST).body(sameAccountErrorResponse))
        );
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("getTransferResponseProvider")
    void givenAccountServicesController_whenCallsPostTransfer_thenExpectedMatched(final TransferRequest request, final ResponseEntity response) {
        when(accountManager.transfer(request)).thenReturn(response);
        mockMvc.perform(post(TRANSFER).content(mapper.writeValueAsString(request)).contentType(APPLICATION_JSON))
                .andExpect(status().is(response.getStatusCodeValue()))
                .andExpect(content().json(mapper.writeValueAsString(response.getBody())));
    }
}
