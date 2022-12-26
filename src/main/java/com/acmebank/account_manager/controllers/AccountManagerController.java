package com.acmebank.account_manager.controllers;

import com.acmebank.account_manager.data.models.requests.TransferRequest;
import com.acmebank.account_manager.data.models.responses.AccountBalance;
import com.acmebank.account_manager.data.models.responses.Transaction;
import com.acmebank.account_manager.services.AccountManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.acmebank.account_manager.shared.Endpoint.GET_ACCOUNT;
import static com.acmebank.account_manager.shared.Endpoint.TRANSFER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AccountManagerController {
    private final AccountManager accountManager;
    @GetMapping(value = GET_ACCOUNT, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<AccountBalance> getAccountBalance(@PathVariable long id) {
        return accountManager.getAccountBalance(id);
    }

    @PostMapping(value = TRANSFER, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<Transaction> transfer(@Valid @RequestBody TransferRequest transferRequest) {
        return accountManager.transfer(transferRequest);
    }
}
