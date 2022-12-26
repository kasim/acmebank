package com.acmebank.account_manager.data.models.responses;

import com.acmebank.account_manager.shared.AccountType;
import com.acmebank.account_manager.shared.Currency;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;

@Value
@AllArgsConstructor(staticName = "of")
public class AccountBalance {
    private BigDecimal balance;
    private Currency currency;
    private AccountType type;
}
