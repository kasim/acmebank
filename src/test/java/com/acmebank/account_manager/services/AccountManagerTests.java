package com.acmebank.account_manager.services;

import com.acmebank.account_manager.data.entities.Account;
import com.acmebank.account_manager.data.models.requests.TransferRequest;
import com.acmebank.account_manager.data.models.responses.AccountBalance;
import com.acmebank.account_manager.data.models.responses.Transaction;
import com.acmebank.account_manager.data.repositories.AccountRepository;
import com.acmebank.account_manager.exceptions.AccountManagerException;
import com.acmebank.account_manager.shared.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static com.acmebank.account_manager.shared.AccountType.CURRENT;
import static com.acmebank.account_manager.shared.AccountType.SAVING;
import static com.acmebank.account_manager.shared.Currency.*;
import static com.acmebank.account_manager.shared.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AccountManagerTests {
    final AccountManager accountManager;
    final AccountRepository accountRepository;
    static final BigDecimal balance = BigDecimal.valueOf(1000000);
    static final Account currentAccount = Account.builder().id(12345678L).balance(balance).currency(HKD).type(CURRENT).build();
    static final Account savingAccount = Account.builder().id(88888888L).balance(balance).currency(HKD).type(SAVING).build();
    static final long accountNotFoundAccountId = 123456789L;

    @BeforeEach
    void setup() {
        accountRepository.saveAllAndFlush(List.of(currentAccount, savingAccount));
    }

    static Stream<Arguments> accountProvider() {
        return Stream.of(
                Arguments.of(currentAccount.getId(), ResponseEntity.ok(AccountBalance.of(currentAccount.getBalance(), currentAccount.getCurrency(), currentAccount.getType())), null),
                Arguments.of(savingAccount.getId(), ResponseEntity.ok(AccountBalance.of(savingAccount.getBalance(), savingAccount.getCurrency(), savingAccount.getType())), null),
                Arguments.of(accountNotFoundAccountId, null, ACCOUNT_NOT_FOUND)
        );
    }

    @ParameterizedTest
    @MethodSource("accountProvider")
    void givenAccountService_whenGetAccountBalance_thenExpectedMatched(final long accountId, final ResponseEntity expected, final ErrorCode errorCode) {
        if(null == expected) {
            val thrown = assertThrows(AccountManagerException.class, ()-> accountManager.getAccountBalance(accountId));
            assertThat(thrown.getErrorCode()).isEqualTo(errorCode);
        } else {
            val actual = accountManager.getAccountBalance(accountId);
            val actualAccountBalance = actual.getBody();
            val expectedAccountBalance = (AccountBalance) expected.getBody();
            assertThat(actual.getStatusCode()).isEqualTo(OK);
            assertThat(actualAccountBalance.getBalance().compareTo(expectedAccountBalance.getBalance())).isEqualTo(0);
            assertThat(actualAccountBalance.getCurrency()).isEqualTo(expectedAccountBalance.getCurrency());
            assertThat(actualAccountBalance.getType()).isEqualTo(actualAccountBalance.getType());
        }
    }

    static Stream<Arguments> transferProvider() {
        val amount = BigDecimal.valueOf(100);
        return Stream.of(
               Arguments.of(new TransferRequest(currentAccount.getId(), savingAccount.getId(), amount, HKD.name()),
                       ResponseEntity.status(CREATED).body(
                               new Transaction(
                                       1L,
                                       currentAccount.getId(),
                                       AccountBalance.of(currentAccount.getBalance().subtract(amount), currentAccount.getCurrency(), currentAccount.getType()),
                                       savingAccount.getId(),
                                       AccountBalance.of(savingAccount.getBalance().add(amount), savingAccount.getCurrency(), savingAccount.getType()),
                                       Timestamp.valueOf(LocalDateTime.now()))), null),
                Arguments.of(new TransferRequest(accountNotFoundAccountId, savingAccount.getId(), amount, HKD.name()), null, ACCOUNT_NOT_FOUND),
                Arguments.of(new TransferRequest(currentAccount.getId(), savingAccount.getId(), amount, XXX.name()), null, ACCOUNT_NOT_FOUND),
                Arguments.of(new TransferRequest(currentAccount.getId(), savingAccount.getId(), BigDecimal.valueOf(100000000.0), HKD.name()), null, INSUFFICIENT_FUND),
                Arguments.of(new TransferRequest(savingAccount.getId(), savingAccount.getId(), amount, HKD.name()), null, SAME_ACCOUNT)
        );
    }

    @ParameterizedTest
    @MethodSource("transferProvider")
    void givenAccountService_whenTransfer_thenExpectedMatched(final TransferRequest request, final ResponseEntity<Transaction> response, final ErrorCode errorCode) {
        if(null == response) {
            val thrown = assertThrows(AccountManagerException.class, () -> accountManager.transfer(request));
            assertThat(thrown.getErrorCode()).isEqualTo(errorCode);
        } else {
            val actual = accountManager.transfer(request);
            val actualResponse = actual.getBody();
            val expectedResponse = response.getBody();
            assertThat(actualResponse.getFromAccountId()).isEqualTo(expectedResponse.getFromAccountId());
            assertThat(actualResponse.getToAccountId()).isEqualTo(expectedResponse.getToAccountId());
            assertThat(actualResponse.getFromAccountBalance().getBalance().compareTo(expectedResponse.getFromAccountBalance().getBalance())).isEqualTo(0);
            assertThat(actualResponse.getFromAccountBalance().getCurrency()).isEqualTo(expectedResponse.getFromAccountBalance().getCurrency());
            assertThat(actualResponse.getFromAccountBalance().getType()).isEqualTo(expectedResponse.getFromAccountBalance().getType());
            assertThat(actualResponse.getToAccountBalance().getBalance().compareTo(expectedResponse.getToAccountBalance().getBalance())).isEqualTo(0);
            assertThat(actualResponse.getToAccountBalance().getCurrency()).isEqualTo(expectedResponse.getToAccountBalance().getCurrency());
            assertThat(actualResponse.getToAccountBalance().getType()).isEqualTo(expectedResponse.getToAccountBalance().getType());
        }
    }

    @Test
    @SneakyThrows
    void givenAccountService_whenTransferRunsConcurrently_thenExpectedTotalAmountInBothAccountsSameAndRequiredEntitiesLocked() {
        val amount = BigDecimal.valueOf(100);
        val currentToSavingRequest = new TransferRequest(currentAccount.getId(), savingAccount.getId(), amount, HKD.name());
        val numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        for (int i = numberOfThreads; i > 0; i--) {
            executorService.execute(() -> {
                try {
                    accountManager.transfer(currentToSavingRequest);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        val currentAccountBalance = accountManager.getAccountBalance(currentAccount.getId());
        val savingAccountBalance = accountManager.getAccountBalance(savingAccount.getId());
        val currentBalanceAmount = currentAccountBalance.getBody().getBalance();
        val savingBalanceAmount = savingAccountBalance.getBody().getBalance();
        assertThat(currentBalanceAmount.add(savingBalanceAmount)
                .compareTo(balance.multiply(BigDecimal.valueOf(2)))).isEqualTo(0);
        assertThat(currentBalanceAmount.compareTo(balance.subtract(amount))).isEqualTo(0);
        assertThat(savingBalanceAmount.compareTo(balance.add(amount))).isEqualTo(0);
    }
}
