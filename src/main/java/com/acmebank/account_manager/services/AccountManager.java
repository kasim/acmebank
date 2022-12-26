package com.acmebank.account_manager.services;

import com.acmebank.account_manager.data.entities.Account;
import com.acmebank.account_manager.data.entities.Transaction;
import com.acmebank.account_manager.data.models.requests.TransferRequest;
import com.acmebank.account_manager.data.models.responses.AccountBalance;
import com.acmebank.account_manager.data.repositories.AccountRepository;
import com.acmebank.account_manager.data.repositories.TransactionRepository;
import com.acmebank.account_manager.exceptions.AccountManagerException;
import com.acmebank.account_manager.shared.Currency;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

import static com.acmebank.account_manager.shared.ErrorCode.*;
import static java.util.Collections.EMPTY_LIST;
import static org.springframework.http.HttpStatus.CREATED;

/*
 * Assumed that two accounts are under the same user which authorization has been done on aggregation level.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AccountManager {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public ResponseEntity<AccountBalance> getAccountBalance(final long id) {
        val account = accountRepository.findById(id).orElseThrow(() -> new AccountManagerException(ACCOUNT_NOT_FOUND, List.of(Long.toString(id))));
        return ResponseEntity.ok(AccountBalance.of(account.getBalance(), account.getCurrency(), account.getType()));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, timeout = 1000)
    public ResponseEntity<com.acmebank.account_manager.data.models.responses.Transaction>
        transfer(final TransferRequest transferRequest) {
        val fromAccountId = transferRequest.getFromAccountId();
        val toAccountId = transferRequest.getToAccountId();
        validateFromAndToAccount(fromAccountId, toAccountId);
        val amount = transferRequest.getAmount();
        val currency = transferRequest.getCurrency();
        val fromAccount = transferAccount(fromAccountId, amount, currency, true);
        val toAccount = transferAccount(toAccountId, amount, currency, false);
        val transaction = Transaction.builder().fromAccount(fromAccount).toAccount(toAccount).amount(amount).currency(currency).build();
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        val storedTransaction = transactionRepository.save(transaction);
        return transactionResponse(fromAccountId, fromAccount, toAccountId, toAccount, storedTransaction);
    }

    private void validateFromAndToAccount(final long fromAccountId, final long toAccountId) {
        if(fromAccountId == toAccountId) {
            throw new AccountManagerException(SAME_ACCOUNT, EMPTY_LIST);
        }
    }

    public Account transferAccount(final long accountId, final BigDecimal amount, final Currency currency, final boolean from) {
        val params = List.of(Long.toString(accountId));
        val accountNotFoundException = new AccountManagerException(ACCOUNT_NOT_FOUND, params);
        Supplier exceptionSupplier = () -> accountNotFoundException;
        val account = accountRepository.findById(accountId).orElseThrow(exceptionSupplier);
        if (currency != account.getCurrency()) {
            throw accountNotFoundException;
        }
        val balance = account.getBalance();
        val amountToTransfer = from ? balance.subtract(amount) : balance.add(amount);
        if(amountToTransfer.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountManagerException(INSUFFICIENT_FUND, params);
        }
        return Account.builder().id(account.getId()).balance(amountToTransfer).currency(account.getCurrency()).type(account.getType()).build();
    }

    private ResponseEntity<com.acmebank.account_manager.data.models.responses.Transaction> transactionResponse(
            final long fromAccountId,
            final Account fromAccount,
            final long toAccountId,
            final Account toAccount,
            final Transaction transaction
    ){
        return ResponseEntity.status(CREATED).body(
                new com.acmebank.account_manager.data.models.responses.Transaction(
                transaction.getId(),
                fromAccountId,
                AccountBalance.of(fromAccount.getBalance(), fromAccount.getCurrency(), fromAccount.getType()),
                toAccountId,
                AccountBalance.of(toAccount.getBalance(), toAccount.getCurrency(), toAccount.getType()),
                transaction.getCreateAt()));
    }
}
