package com.acmebank.account_manager.data.models.requests;

import com.acmebank.account_manager.shared.Currency;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static com.acmebank.account_manager.shared.Currency.HKD;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferRequestsTests {

    static Stream<Arguments> currencyTextProvider() {
        return Stream.of(
                Arguments.of(null, HKD),
                Arguments.of("HKD", HKD)
        );
    }

    @ParameterizedTest
    @MethodSource("currencyTextProvider")
    void givenCurrencyString_whenGetCurrency_thenReturnedHKD(final String currencyText, final Currency expected) {
        val request = new TransferRequest(1L, 1L, BigDecimal.ONE, currencyText);
        val currency = request.getCurrency();
        assertThat(currency).isEqualTo(expected);
    }
}
