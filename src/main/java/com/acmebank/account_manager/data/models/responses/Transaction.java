package com.acmebank.account_manager.data.models.responses;

import lombok.Value;

import java.sql.Timestamp;

@Value
public class Transaction {
    private long transactionId;
    private long fromAccountId;
    private AccountBalance fromAccountBalance;
    private long toAccountId;
    private AccountBalance toAccountBalance;

    private Timestamp createdAt;
}
