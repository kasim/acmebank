package com.acmebank.account_manager.data.repositories;

import com.acmebank.account_manager.data.entities.Account;
import com.acmebank.account_manager.data.entities.Transaction;
import com.acmebank.account_manager.shared.AccountType;
import com.acmebank.account_manager.shared.Currency;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static com.acmebank.account_manager.shared.AccountType.CURRENT;
import static com.acmebank.account_manager.shared.AccountType.SAVING;
import static com.acmebank.account_manager.shared.Currency.HKD;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AccountManagerRepositoriesIntegrationTests {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    final BigDecimal balance = BigDecimal.valueOf(100000);
    final long currentAccountId = 12345678L;
    final long savingAccountId = 88888888L;

    @Test
    @Order(1)
    void givenAccountRepository_whenSaveAccounts_thenOK() {
        createAccount(currentAccountId, balance, HKD, CURRENT);
        createAccount(savingAccountId, balance, HKD, SAVING);
    }

    private void createAccount(final long accountId, final BigDecimal balance, final Currency currency, final AccountType type) {
        val account = Account.builder()
                .id(accountId)
                .currency(currency)
                .balance(balance)
                .type(type).build();
        val storedAccount = accountRepository.save(account);
        assertThat(storedAccount.getId()).isEqualTo(accountId);
        assertThat(storedAccount.getCurrency()).isEqualTo(currency);
        assertThat(storedAccount.getBalance()).isEqualTo(balance);
        assertThat(storedAccount.getType()).isEqualTo(type);
    }

    @Test
    @Order(2)
    void givenTransactionRepository_whenSaveTransaction_thenOK() {
        val fromAccount = accountRepository.findById(currentAccountId);
        val toAccount = accountRepository.findById(savingAccountId);
        val amount = BigDecimal.valueOf(100.00);
        val transaction = Transaction.builder()
                .fromAccount(fromAccount.orElseThrow())
                .toAccount(toAccount.orElseThrow())
                .amount(amount)
                .currency(HKD).build();
        val storedTransaction = transactionRepository.save(transaction);
        assertThat(storedTransaction.getFromAccount()).isEqualTo(fromAccount.orElseThrow());
        assertThat(storedTransaction.getToAccount()).isEqualTo(toAccount.orElseThrow());
        assertThat(storedTransaction.getAmount()).isEqualTo(amount);
        assertThat(storedTransaction.getCurrency()).isEqualTo(HKD);
    }
}
