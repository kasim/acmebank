package com.acmebank.account_manager.data.models.requests;

import com.acmebank.account_manager.shared.Currency;
import com.acmebank.account_manager.validations.ValueOfEnum;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Optional;

import static com.acmebank.account_manager.shared.Currency.HKD;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class TransferRequest {
    @PositiveOrZero(message = "Invalid account ID!")
    private long fromAccountId;
    @PositiveOrZero(message = "Invalid account ID!")
    private long toAccountId;
    @DecimalMin(value = "100", message = "Minimum transfer amount is HKD 100!")
    @Digits(integer = 32, fraction = 18, message = "Amount should be with maximum 18 digits fraction and 32 digits integer!")
    private BigDecimal amount;
    @ValueOfEnum(enumClass = Currency.class)
    private String currency;

    public Currency getCurrency() {
        return Currency.valueOf(Optional.ofNullable(this.currency).orElse(HKD.name()));
    }
}
